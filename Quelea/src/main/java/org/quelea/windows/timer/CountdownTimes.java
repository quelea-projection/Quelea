/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.timer;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsing and formatting of the times a countdown timer works with: a
 * duration ("5:00") or a time of day to count down to ("09:30", "9:30am").
 * <p>
 * Kept free of JavaFX so it can be unit tested.
 */
public final class CountdownTimes {

    /**
     * The longest duration a timer can count down, in seconds.
     */
    public static final int MAX_DURATION = 3600;

    /**
     * "9:30", "09.30", "21:30", "9:30am", "9:30 PM", "9am", "9 pm".
     */
    private static final Pattern TIME_OF_DAY = Pattern.compile("(\\d{1,2})(?:[:.](\\d{1,2}))?(am|pm|a\\.m\\.|p\\.m\\.)?");

    private CountdownTimes() {
    }

    /**
     * Parse a countdown duration such as "5:00", "5.00", "5,00" or "5m30s".
     *
     * @param text the text to parse.
     * @return the duration in seconds, or -1 if the text isn't a valid
     * duration of at most an hour.
     */
    public static int parseDuration(String text) {
        if (text == null) {
            return -1;
        }
        String value = stripSpaces(text).toLowerCase(Locale.ROOT);
        String[] parts;
        if (value.contains(":")) {
            parts = value.split(":", -1);
        } else if (value.contains(".")) {
            parts = value.split("\\.", -1);
        } else if (value.contains(";")) {
            parts = value.split(";", -1);
        } else if (value.contains(",")) {
            parts = value.split(",", -1);
        } else if (value.contains("m")) {
            parts = value.split("m", -1);
            if (parts.length == 2) {
                parts[1] = parts[1].isEmpty() ? "0" : parts[1].replace("s", "");
            }
        } else {
            return -1;
        }
        if (parts.length != 2) {
            return -1;
        }
        try {
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            if (minutes < 0 || seconds < 0 || seconds >= 60) {
                return -1;
            }
            int total = minutes * 60 + seconds;
            return total > MAX_DURATION ? -1 : total;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Parse a time of day in 24 hour ("09:30", "21:30") or 12 hour ("9:30am",
     * "9:30 PM", "9pm") form.
     *
     * @param text the text to parse.
     * @return the time, or null if the text isn't a valid time of day.
     */
    public static LocalTime parseTimeOfDay(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = TIME_OF_DAY.matcher(stripSpaces(text).toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            return null;
        }
        int hour = Integer.parseInt(matcher.group(1));
        int minute = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
        String meridiem = matcher.group(3);
        if (meridiem == null) {
            if (matcher.group(2) == null) {
                return null; // "9" on its own is too ambiguous
            }
            if (hour > 23) {
                return null;
            }
        } else {
            if (hour < 1 || hour > 12) {
                return null;
            }
            hour = hour % 12 + (meridiem.startsWith("p") ? 12 : 0);
        }
        if (minute > 59) {
            return null;
        }
        return LocalTime.of(hour, minute);
    }

    /**
     * Format a time of day the way it's shown and saved: "09:30".
     *
     * @param time the time.
     * @return the time as HH:mm.
     */
    public static String formatTimeOfDay(LocalTime time) {
        return String.format(Locale.ROOT, "%02d:%02d", time.getHour(), time.getMinute());
    }

    /**
     * Work out how many whole seconds are left until a time of day. A time
     * that has already passed today gives 0, so a "service starts at 09:30"
     * timer shows 00:00 once the service has started.
     *
     * @param target the time to count down to.
     * @param now the current time.
     * @return the seconds left, rounded up so 00:00 shows at the target time.
     */
    public static int secondsUntil(LocalTime target, LocalTime now) {
        Duration left = Duration.between(now, target);
        if (left.isNegative() || left.isZero()) {
            return 0;
        }
        long millis = left.toMillis();
        return (int) ((millis + 999) / 1000);
    }

    /**
     * Format a number of seconds left as "05:00", or "1:05:00" once it's an
     * hour or more.
     *
     * @param seconds the seconds left.
     * @return the formatted countdown.
     */
    public static String formatRemaining(int seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        if (hours > 0) {
            return String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format(Locale.ROOT, "%02d:%02d", minutes, secs);
    }

    private static String stripSpaces(String text) {
        // Includes the non-breaking spaces some locales put before AM/PM
        return text.replaceAll("[\\s\\u00A0\\u202F]", "");
    }
}
