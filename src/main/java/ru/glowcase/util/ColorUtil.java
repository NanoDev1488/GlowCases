package ru.glowcase.util;

import net.md_5.bungee.api.ChatColor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtil() {}

    /**
     * Colorizes a string supporting both standard codes (&a, &l) and hex colors (&#rrggbb).
     */
    public static String color(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            try {
                matcher.appendReplacement(buffer, ChatColor.of("#" + hexCode).toString());
            } catch (NoSuchMethodError | Exception e) {
                // Fallback for older versions if ChatColor.of is not supported
                matcher.appendReplacement(buffer, "");
            }
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Colorizes a list of strings.
     */
    public static List<String> color(List<String> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<String> colored = new ArrayList<>(list.size());
        for (String s : list) {
            colored.add(color(s));
        }
        return colored;
    }
}
