package com.qwqaq.libraryregister.utils;

/**
 * Created by Zneia on 2017/10/2.
 */

public class StringEscapeUtil {
    public static String escapeCSV(String str) {
        if (str == null || str.trim().length() < 1) {
            return "\"\"";
        }

        String csv = str.replaceAll("\"","\"\"");
        return "\"" + csv + "\"";
    }
}
