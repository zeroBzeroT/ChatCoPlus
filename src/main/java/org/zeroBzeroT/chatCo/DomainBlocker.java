package org.zeroBzeroT.chatCo;

import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ghost blocking of file sharing links.
 * <p>
 * A message that contains one of the configured domains - or any subdomain of it - is only echoed
 * back to its sender. It never reaches the other players, the console or any other plugin.
 */
public class DomainBlocker {
    /**
     * Permission that lets a player post blocked links anyway.
     */
    public static final String BYPASS_PERMISSION = "ChatCo.linkBlock.bypass";

    /**
     * Legacy color codes, both the ampersand and the section sign variant.
     */
    private static final Pattern COLOR_CODES = Pattern.compile("[&§][0-9a-fk-orA-FK-OR]");

    /**
     * Characters that are invisible in chat but break up a domain name.
     */
    private static final Pattern INVISIBLE = Pattern.compile("[\\u00a0\\u180e\\u200b-\\u200f\\u2028\\u2029\\u202a-\\u202e\\u2060-\\u2064\\ufeff]");

    /**
     * Bracketed dots like mega[.]nz, mega(dot)nz or mega{punkt}nz.
     */
    private static final Pattern BRACKETED_DOT = Pattern.compile("\\s*[(\\[{<]\\s*(?:\\.|dot|punkt)\\s*[)\\]}>]\\s*");

    /**
     * Spelled out dots like mega dot nz.
     */
    private static final Pattern SPELLED_DOT = Pattern.compile("\\s+(?:dot|punkt)\\s+");

    /**
     * Whitespace around a dot like mega . nz.
     */
    private static final Pattern SPACED_DOT = Pattern.compile("\\s*\\.\\s*");

    /**
     * A comma used in place of a dot like mega,nz.
     */
    private static final Pattern COMMA_DOT = Pattern.compile("(?<=[a-z0-9]),(?=[a-z0-9])");

    /**
     * A valid entry of the domain list, e.g. mega.nz or litterbox.catbox.moe.
     */
    private static final Pattern DOMAIN = Pattern.compile("[a-z0-9-]+(?:\\.[a-z0-9-]+)+");

    private final Main plugin;

    /**
     * Matches any configured domain and all of its subdomains. Null if the feature is off.
     */
    private Pattern domainPattern;

    public DomainBlocker(final Main plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Rebuilds the domain pattern from blockedDomains.txt.
     */
    public void reload() {
        if (!plugin.getConfig().getBoolean("ChatCo.linkBlock.enabled", true)) {
            domainPattern = null;
            return;
        }

        domainPattern = buildPattern(readDomains());

        if (domainPattern == null) {
            plugin.getLogger().warning("No domains to block, " + Main.BlockedDomains.getName() + " is empty.");
        }
    }

    /**
     * Reads the domain list, skipping empty lines and comments.
     */
    private List<String> readDomains() {
        final List<String> domains = new ArrayList<>();

        try {
            for (final String line : Files.readAllLines(Main.BlockedDomains.toPath(), StandardCharsets.UTF_8)) {
                final String domain = line.trim().toLowerCase();

                if (domain.isEmpty() || domain.startsWith("#")) {
                    continue;
                }

                // Guard against typos: a line without a dot would block a plain word
                if (DOMAIN.matcher(domain).matches()) {
                    domains.add(domain);
                } else {
                    plugin.getLogger().warning("Ignoring invalid domain in " + Main.BlockedDomains.getName() + ": " + domain);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not read " + Main.BlockedDomains.getName() + ": " + e.getMessage());
        }

        return domains;
    }

    /**
     * Builds a pattern that matches any of the domains and all of their subdomains.
     *
     * @return the pattern or null, if there is no domain to block.
     */
    static Pattern buildPattern(final List<String> domains) {
        final Set<String> quoted = new LinkedHashSet<>();

        for (final String domain : domains) {
            final String normalized = domain.trim().toLowerCase();

            if (!normalized.isEmpty()) {
                quoted.add(Pattern.quote(normalized));
            }
        }

        if (quoted.isEmpty()) {
            return null;
        }

        // <subdomains>.<blocked domain>, not preceded or followed by more of a label
        return Pattern.compile("(?<![a-z0-9.-])(?:[a-z0-9_-]+\\.)*(?:" + String.join("|", quoted) + ")(?![a-z0-9-])");
    }

    /**
     * @return TRUE if the message of that player has to be ghost blocked, FALSE otherwise.
     */
    public boolean isBlocked(final Player player, final String message) {
        if (domainPattern == null || message == null || player.hasPermission(BYPASS_PERMISSION)) {
            return false;
        }

        return domainPattern.matcher(normalize(message)).find();
    }

    /**
     * Writes a blocked message to the server log, if enabled.
     */
    public void log(final Player player, final String source, final String message) {
        if (plugin.getConfig().getBoolean("ChatCo.linkBlock.logToConsole", true)) {
            plugin.getLogger().info("[LinkBlock] " + source + " of " + player.getName() + " was blocked: " + message);
        }
    }

    /**
     * Strips color codes and undoes the usual ways of writing a domain without writing it.
     */
    static String normalize(final String message) {
        String normalized = COLOR_CODES.matcher(message).replaceAll("");
        normalized = INVISIBLE.matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase();
        normalized = BRACKETED_DOT.matcher(normalized).replaceAll(Matcher.quoteReplacement("."));
        normalized = SPELLED_DOT.matcher(normalized).replaceAll(Matcher.quoteReplacement("."));
        normalized = SPACED_DOT.matcher(normalized).replaceAll(Matcher.quoteReplacement("."));
        normalized = COMMA_DOT.matcher(normalized).replaceAll(Matcher.quoteReplacement("."));

        return normalized;
    }
}
