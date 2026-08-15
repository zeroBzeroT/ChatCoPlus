package org.zeroBzeroT.chatCo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkBlocker {

    public static final Pattern URL_PATTERN = Pattern.compile(
        "https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_\\\\+.~#?&/=]*");

    private Set<String> domainAllowlist = new HashSet<>();
    private String replacement = "[link removed]";
    private boolean loaded = false;

    public synchronized void load(File whitelistFile, String replacement) {
        this.replacement = replacement == null ? "[link removed]" : replacement;
        this.domainAllowlist = new HashSet<>();
        if (whitelistFile != null && whitelistFile.exists()) {
            try (BufferedReader r = new BufferedReader(new FileReader(whitelistFile))) {
                String line;
                while ((line = r.readLine()) != null) {
                    String t = line.trim().toLowerCase(Locale.ROOT);
                    if (t.isEmpty() || t.startsWith("#")) continue;
                    if (t.startsWith("http://")) t = t.substring(7);
                    else if (t.startsWith("https://")) t = t.substring(8);
                    if (t.endsWith("/")) t = t.substring(0, t.length() - 1);
                    domainAllowlist.add(t);
                }
            } catch (IOException ignored) {}
        }
        this.loaded = true;
    }

    public synchronized String apply(String message) {
        if (!loaded || message == null || message.isEmpty()) return message;
        Matcher m = URL_PATTERN.matcher(message);
        StringBuilder out = new StringBuilder(message.length());
        int last = 0;
        while (m.find()) {
            String url = m.group();
            if (isAllowed(url)) {
                continue;
            }
            out.append(message, last, m.start());
            if (!replacement.isEmpty()) {
                out.append(replacement);
            } else if (out.length() > 0 && out.charAt(out.length() - 1) == ' ') {
                out.setLength(out.length() - 1);
            }
            last = m.end();
        }
        out.append(message, last, message.length());
        return out.toString();
    }

    private boolean isAllowed(String url) {
        String host = extractHost(url);
        if (host == null) return false;
        return domainAllowlist.contains(host);
    }

    private static String extractHost(String url) {
        String s = url.toLowerCase(Locale.ROOT);
        if (s.startsWith("https://")) s = s.substring(8);
        else if (s.startsWith("http://")) s = s.substring(7);
        int slash = s.indexOf('/');
        if (slash >= 0) s = s.substring(0, slash);
        int colon = s.indexOf(':');
        if (colon >= 0) s = s.substring(0, colon);
        return s.isEmpty() ? null : s;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
