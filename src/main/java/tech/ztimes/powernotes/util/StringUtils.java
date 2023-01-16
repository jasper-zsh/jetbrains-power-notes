package tech.ztimes.powernotes.util;

public class StringUtils {
    public static String maxLength(String str, int maxLength) {
        if (str.length() > maxLength) {
            return str.substring(0, maxLength) + "...";
        }
        return str;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
