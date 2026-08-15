package org.zeroBzeroT.chatCo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class WordFilter {

    private Set<String> words = new HashSet<>();
    private Set<String> whitelist = new HashSet<>();
    private int minWordLength = Integer.MAX_VALUE;
    private boolean fuzzy = false;
    private String replacement = "bobba";
    private boolean loaded = false;

    public synchronized void load(File wordlist, File whitelistFile, boolean fuzzy, String replacement) {
        this.fuzzy = fuzzy;
        this.replacement = replacement == null ? "bobba" : replacement;
        this.words = readSet(wordlist);
        this.whitelist = readSet(whitelistFile);
        this.minWordLength = Integer.MAX_VALUE;
        for (String w : words) {
            if (w.length() < minWordLength) minWordLength = w.length();
        }
        this.loaded = true;
    }

    public synchronized String apply(String message) {
        if (!loaded || words.isEmpty() || message == null || message.isEmpty()) {
            return message;
        }

        StringBuilder out = new StringBuilder(message);
        String normalized = leetNormalize(message.toLowerCase(Locale.ROOT));

        int n = normalized.length();
        int i = 0;
        while (i < n) {
            while (i < n && !isLetter(normalized.charAt(i))) i++;
            if (i >= n) break;
            int start = i;
            while (i < n && isLetter(normalized.charAt(i))) i++;
            int tokenLen = i - start;
            if (tokenLen < minWordLength) continue;
            if (isMatch(normalized, start, tokenLen)) {
                int end = i;
                if (replacement.isEmpty()) {
                    int clearEnd = end;
                    if (clearEnd < out.length() && out.charAt(clearEnd) == ' ') {
                        clearEnd++;
                    }
                    for (int k = start; k < clearEnd; k++) {
                        out.setCharAt(k, ' ');
                    }
                } else {
                    for (int k = start; k < end; k++) {
                        out.setCharAt(k, replacement.charAt(Math.min(k - start, replacement.length() - 1)));
                    }
                }
            }
        }
        return out.toString();
    }

    private boolean isMatch(String normalized, int start, int len) {
        for (String wl : whitelist) {
            if (wl.length() == len && regionMatchesIgnoreCase(normalized, start, wl, 0, len)) {
                return false;
            }
        }
        for (String w : words) {
            if (w.length() != len) continue;
            if (regionMatchesIgnoreCase(normalized, start, w, 0, len)) return true;
        }
        if (!fuzzy) return false;
        if (len < 4) return false;
        for (String w : words) {
            if (Math.abs(w.length() - len) > 1) continue;
            if (levenRegion(normalized, start, len, w)) return true;
        }
        return false;
    }

    private static boolean regionMatchesIgnoreCase(String s, int sOff, String other, int oOff, int len) {
        for (int k = 0; k < len; k++) {
            char a = s.charAt(sOff + k);
            char b = other.charAt(oOff + k);
            if (a == b) continue;
            char al = (a >= 'A' && a <= 'Z') ? (char) (a + 32) : a;
            char bl = (b >= 'A' && b <= 'Z') ? (char) (b + 32) : b;
            if (al != bl) return false;
        }
        return true;
    }

    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static String leetNormalize(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '0': b.append('o'); break;
                case '1': b.append('i'); break;
                case '3': b.append('e'); break;
                case '4': b.append('a'); break;
                case '5': b.append('s'); break;
                case '7': b.append('t'); break;
                case '@': b.append('a'); break;
                case '$': b.append('s'); break;
                default:  b.append(c);
            }
        }
        return b.toString();
    }

    private static boolean levenRegion(String s, int sOff, int sLen, String t) {
        int tLen = t.length();
        int[] prev = new int[tLen + 1];
        int[] curr = new int[tLen + 1];
        for (int j = 0; j <= tLen; j++) prev[j] = j;
        for (int i = 1; i <= sLen; i++) {
            curr[0] = i;
            char sc = (s.charAt(sOff + i - 1) >= 'A' && s.charAt(sOff + i - 1) <= 'Z')
                ? (char) (s.charAt(sOff + i - 1) + 32) : s.charAt(sOff + i - 1);
            for (int j = 1; j <= tLen; j++) {
                char tc = t.charAt(j - 1);
                int cost = sc == tc ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[tLen] <= 1;
    }

    private static Set<String> readSet(File f) {
        Set<String> out = new HashSet<>();
        if (f == null || !f.exists()) return out;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                String t = line.trim().toLowerCase(Locale.ROOT);
                if (t.isEmpty() || t.startsWith("#")) continue;
                out.add(t);
            }
        } catch (IOException e) {
            // best-effort
        }
        return out;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
