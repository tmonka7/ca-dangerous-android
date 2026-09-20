package com.falcon.car.obd;

import android.os.Handler;
import android.os.Looper;

import com.falcon.car.data.model.Dtc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Owns the one live adapter link for the whole app.
 *
 * <p>All adapter traffic runs on a single background thread, which keeps
 * request and reply in step - an ELM327 has no request identifiers, so two
 * overlapping commands would read each other's answers. Results come back on
 * the main thread.
 */
public final class ObdManager {

    public enum State {
        DISCONNECTED, CONNECTING, CONNECTED, FAILED
    }

    public interface StateListener {
        void onObdState(State state, String detail);
    }

    public interface ResultCallback<T> {
        void onSuccess(T value);

        void onFailure(String message);
    }

    private static ObdManager instance;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<StateListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean busy = new AtomicBoolean(false);

    private volatile ElmSession session;
    private volatile State state = State.DISCONNECTED;
    private volatile String detail = "";

    private Runnable pollTick;

    private ObdManager() {
    }

    public static synchronized ObdManager get() {
        if (instance == null) {
            instance = new ObdManager();
        }
        return instance;
    }

    // --------------------------------------------------------------- state

    public State getState() {
        return state;
    }

    public boolean isConnected() {
        return state == State.CONNECTED;
    }

    /** Negotiated protocol or the last error, for the status line. */
    public String getDetail() {
        return detail;
    }

    public String getAdapterName() {
        ElmSession current = session;
        return current == null ? "" : current.getConnection().getName();
    }

    public String getProtocolName() {
        ElmSession current = session;
        return current == null ? "" : current.getProtocolName();
    }

    public void addListener(StateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StateListener listener) {
        listeners.remove(listener);
    }

    private void publish(State newState, String newDetail) {
        state = newState;
        detail = newDetail == null ? "" : newDetail;
        main.post(() -> {
            for (StateListener listener : listeners) {
                listener.onObdState(state, detail);
            }
        });
    }

    // ---------------------------------------------------------- connection

    /** Opens the link and runs the ELM327 handshake off the main thread. */
    public void connect(ObdConnection connection) {
        disconnect();
        publish(State.CONNECTING, connection.getName());

        executor.execute(() -> {
            ElmSession candidate = new ElmSession(connection);
            try {
                connection.open();
                candidate.initialize();
                session = candidate;
                publish(State.CONNECTED, candidate.getProtocolName());
            } catch (IOException e) {
                connection.close();
                session = null;
                publish(State.FAILED, message(e));
            }
        });
    }

    public void disconnect() {
        stopPolling();
        ElmSession current = session;
        session = null;
        if (current != null) {
            executor.execute(() -> current.getConnection().close());
        }
        publish(State.DISCONNECTED, "");
    }

    // ------------------------------------------------------------- reading

    /** Reads one sweep of the given parameters. Unsupported PIDs are omitted. */
    public void readLive(List<Pid> pids, ResultCallback<Map<Pid, Double>> callback) {
        submit(callback, current -> {
            Map<Pid, Double> values = new EnumMap<>(Pid.class);
            for (Pid pid : pids) {
                Double value = current.readPid(pid);
                if (value != null) {
                    values.put(pid, value);
                }
            }
            return values;
        });
    }

    /** Reads the freeze frame stored with the first confirmed fault. */
    public void readFreezeFrame(List<Pid> pids, ResultCallback<Map<Pid, Double>> callback) {
        submit(callback, current -> {
            Map<Pid, Double> values = new EnumMap<>(Pid.class);
            for (Pid pid : pids) {
                Double value = current.readFreezeFrame(pid);
                if (value != null) {
                    values.put(pid, value);
                }
            }
            return values;
        });
    }

    /** Stored, pending and permanent codes in one pass. */
    public void readDtcs(ResultCallback<List<Dtc>> callback) {
        submit(callback, current -> {
            List<Dtc> all = new ArrayList<>();
            for (Dtc.Status status : Dtc.Status.values()) {
                String hex = current.requestOrEmpty(status.getMode());
                all.addAll(DtcParser.parse(hex, status));
            }
            return all;
        });
    }

    /**
     * Clears codes and the freeze frame. The caller must confirm first - this
     * is not reversible and it resets the readiness monitors.
     */
    public void clearDtcs(ResultCallback<Boolean> callback) {
        submit(callback, current -> {
            current.request("04");
            return Boolean.TRUE;
        });
    }

    // ------------------------------------------------------------- polling

    /**
     * Repeats {@link #readLive} on an interval. Ticks are skipped while an
     * earlier read is outstanding, so a slow bus cannot queue up work.
     */
    public void startPolling(List<Pid> pids, long intervalMs,
                             ResultCallback<Map<Pid, Double>> callback) {
        stopPolling();
        pollTick = new Runnable() {
            @Override
            public void run() {
                if (!isConnected()) {
                    return;
                }
                if (!busy.get()) {
                    readLive(pids, callback);
                }
                main.postDelayed(this, intervalMs);
            }
        };
        main.post(pollTick);
    }

    public void stopPolling() {
        if (pollTick != null) {
            main.removeCallbacks(pollTick);
            pollTick = null;
        }
    }

    // ------------------------------------------------------------ plumbing

    private interface Work<T> {
        T run(ElmSession session) throws IOException;
    }

    private <T> void submit(ResultCallback<T> callback, Work<T> work) {
        final ElmSession current = session;
        if (current == null || !isConnected()) {
            main.post(() -> callback.onFailure("Not connected"));
            return;
        }
        executor.execute(() -> {
            busy.set(true);
            try {
                T value = work.run(current);
                main.post(() -> callback.onSuccess(value));
            } catch (IOException e) {
                String text = message(e);
                main.post(() -> callback.onFailure(text));
                if (!current.getConnection().isOpen()) {
                    publish(State.FAILED, text);
                }
            } finally {
                busy.set(false);
            }
        });
    }

    private static String message(IOException e) {
        String text = e.getMessage();
        return text == null || text.isEmpty() ? e.getClass().getSimpleName() : text;
    }

    /** Reads the lamp, fault count and readiness monitors in one request. */
    public void readStatus(ResultCallback<MonitorStatus> callback) {
        submit(callback, current -> {
            MonitorStatus status = current.readStatus();
            if (status == null) {
                throw new ObdException("No status from the ECU");
            }
            return status;
        });
    }
}
