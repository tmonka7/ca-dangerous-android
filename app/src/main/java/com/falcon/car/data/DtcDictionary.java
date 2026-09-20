package com.falcon.car.data;

import android.content.Context;
import android.content.res.Configuration;

import androidx.core.os.ConfigurationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Bundled trouble code descriptions, one asset per language.
 *
 * <p>The tables hold the systematic SAE J2012 generic families only. Anything
 * absent - manufacturer-specific codes above all - is described by
 * {@link DtcDecoder} from the structure of the code, so lookups never invent
 * wording. Regenerate the assets with {@code tools/gen_dtc.sh}.
 */
public final class DtcDictionary {

    private static final String ASSET_DIR = "dtc";
    private static final String FALLBACK_LANGUAGE = "en";

    private static Map<String, String> entries = Collections.emptyMap();
    private static String loadedLanguage;

    private DtcDictionary() {
    }

    /**
     * Description for a code, or null when it is not in the table. Loads the
     * table for the current language on first use.
     */
    public static synchronized String lookup(Context context, String code) {
        String language = languageOf(context);
        if (!language.equals(loadedLanguage)) {
            entries = read(context, language);
            loadedLanguage = language;
        }
        return entries.get(code.toUpperCase(Locale.US));
    }

    /** Number of codes in the loaded table, for the settings screen. */
    public static synchronized int size(Context context) {
        lookup(context, "P0000");
        return entries.size();
    }

    private static String languageOf(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        Locale locale = ConfigurationCompat.getLocales(configuration).get(0);
        if (locale == null) {
            return FALLBACK_LANGUAGE;
        }
        String language = locale.getLanguage();
        if ("ja".equals(language) || "zh".equals(language)) {
            return language;
        }
        return FALLBACK_LANGUAGE;
    }

    private static Map<String, String> read(Context context, String language) {
        Map<String, String> loaded = readAsset(context, language);
        if (loaded.isEmpty() && !FALLBACK_LANGUAGE.equals(language)) {
            loaded = readAsset(context, FALLBACK_LANGUAGE);
        }
        return loaded;
    }

    private static Map<String, String> readAsset(Context context, String language) {
        Map<String, String> loaded = new HashMap<>();
        String path = ASSET_DIR + "/" + language + ".tsv";

        InputStream stream = null;
        BufferedReader reader = null;
        try {
            stream = context.getAssets().open(path);
            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                int tab = line.indexOf('\t');
                if (tab <= 0) {
                    continue;
                }
                loaded.put(line.substring(0, tab), line.substring(tab + 1));
            }
        } catch (IOException e) {
            return Collections.emptyMap();
        } finally {
            closeQuietly(reader);
            closeQuietly(stream);
        }
        return loaded;
    }

    private static void closeQuietly(java.io.Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            // Nothing useful to do.
        }
    }
}
