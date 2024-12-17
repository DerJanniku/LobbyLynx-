package org.derjannik.lobbyLynx.util;

public class TimeUtils {
    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        StringBuilder time = new StringBuilder();
        if (days > 0) time.append(days).append("d ");
        if (hours > 0) time.append(hours % 24).append("h ");
        if (minutes > 0) time.append(minutes % 60).append("m ");
        time.append(seconds % 60).append("s");

        return time.toString();
    }
}