package com.falcon.car.obd;

import java.io.IOException;

/**
 * A byte pipe to a vehicle communication interface.
 *
 * <p>Everything above this interface - the ELM327 handshake, PID decoding, DTC
 * parsing - is transport agnostic, so adding BLE, Wi-Fi or USB later means
 * adding an implementation here and nothing else.
 */
public interface ObdConnection {

    /** Opens the link. Blocking; call off the main thread. */
    void open() throws IOException;

    void close();

    boolean isOpen();

    /**
     * Writes one command and returns the adapter's reply with the terminating
     * prompt removed. Blocking.
     */
    String send(String command, long timeoutMs) throws IOException;

    /** Human readable name for the status line. */
    String getName();
}
