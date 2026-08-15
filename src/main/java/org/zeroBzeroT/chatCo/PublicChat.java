package org.zeroBzeroT.chatCo;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PublicChat implements Listener {
    public final Main plugin;
    private final FileConfiguration permissionConfig;
    private volatile Map<String, String> chatPrefixes = Collections.emptyMap();
    private volatile List<InlineColor> chatInlineColors = Collections.emptyList();

    private record InlineColor(String configKey, Pattern pattern, int triggerLength, NamedTextColor color) {}

    public static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_\\\\+.~#?&/=]*");

    public PublicChat(final Main plugin) {
        this.plugin = plugin;
        File customConfig = Main.PermissionConfig;
        permissionConfig = YamlConfiguration.loadConfiguration(customConfig);
        reload();
    }

    public void reload() {
        Map<String, String> prefixes = new HashMap<>();
        List<InlineColor> inlines = new ArrayList<>();

        for (String color : NamedTextColor.NAMES.keys()) {
            String prefixKey = "ChatCo.chatPrefixes." + color;
            String prefixValue = plugin.getConfig().getString(prefixKey);
            if (prefixValue != null) {
                prefixes.put(color, prefixValue);
            }

            String inlineKey = "ChatCo.chatColors." + color;
            String inlineValue = plugin.getConfig().getString(inlineKey);
            if (inlineValue != null) {
                inlines.add(new InlineColor(
                    inlineKey,
                    Pattern.compile(Pattern.quote(inlineValue) + ".*$"),
                    inlineValue.length(),
                    NamedTextColor.NAMES.value(color)
                ));
            }
        }

        this.chatPrefixes = prefixes;
        this.chatInlineColors = inlines;
    }

    public Component replacePrefixColors(Component message, final Player player) {
        String messagePlain = PlainTextComponentSerializer.plainText().serialize(message);

        for (Map.Entry<String, String> entry : chatPrefixes.entrySet()) {
            String color = entry.getKey();
            String configValue = entry.getValue();
            String configKey = "ChatCo.chatPrefixes." + color;

            if (messagePlain.startsWith(configValue)) {
                if (permissionConfig.getBoolean(configKey, false) || player.hasPermission(configKey)) {
                    return message.color(NamedTextColor.NAMES.value(color));
                }
            }
        }

        return message;
    }

    public Component replaceInlineColors(Component message, final Player player) {
        for (InlineColor ic : chatInlineColors) {
            if (permissionConfig.getBoolean(ic.configKey(), false) || player.hasPermission(ic.configKey())) {
                final NamedTextColor namedColor = ic.color();
                final int triggerLen = ic.triggerLength();
                return message.replaceText(TextReplacementConfig.builder()
                        .match(ic.pattern())
                        .replacement(s -> s.content(s.content().substring(triggerLen)).color(namedColor))
                        .build());
            }
        }

        return message;
    }

    private Component replaceUrls(Component component) {
        return component.replaceText(
                TextReplacementConfig.builder()
                        .match(DEFAULT_URL_PATTERN)
                        .replacement(url -> url
                                .decorate(TextDecoration.UNDERLINED)
                                .clickEvent(ClickEvent.openUrl(url.content()))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(url.content())))
                        )
                        .build()
        );
    }

    private Component buildMessage(Player senderPlayer, Component sender, String text) {
        Component messageText = Component.text(text);
        messageText = replacePrefixColors(messageText, senderPlayer);
        messageText = replaceInlineColors(messageText, senderPlayer);
        messageText = replaceUrls(messageText);
        return Component.text("").append(Component.text("<")).append(sender).append(Component.text("> ")).append(messageText);
    }

    /**
     * See <a href="https://docs.advntr.dev/text.html">Text (Chat Components)</a>
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        String legacyMessage = LegacyComponentSerializer.legacyAmpersand().serialize(event.message());

        // Do not send empty messages
        if (legacyMessage.trim().isEmpty()) {
            event.viewers().clear();
            return;
        }

        // Player
        final Player player = event.getPlayer();
        final boolean gated = plugin.isGated(player);

        Component sender = player.displayName();

        if (plugin.getConfig().getBoolean("ChatCo.whisperOnClick", true)) {
            sender = sender.clickEvent(ClickEvent.suggestCommand("/w " + player.getName() + " "));
            sender = sender.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Whisper to " + player.getName())));
        }

        // Ghost blocked links: only the sender sees his own message, unfiltered
        if (plugin.getDomainBlocker().isBlocked(player, legacyMessage)) {
            player.sendMessage(buildMessage(player, sender, legacyMessage));
            plugin.getDomainBlocker().log(player, "Chat message", legacyMessage);

            // Cancel, so that the message does not reach the console or any other plugin
            event.viewers().clear();
            event.setCancelled(true);
            return;
        }

        // Apply gate filters (word filter + link block) to the outgoing text
        if (gated) {
            if (plugin.getConfig().getBoolean("ChatCo.playtimeGate.wordFilter.enabled", true)
                    && plugin.getWordFilter() != null && plugin.getWordFilter().isLoaded()) {
                legacyMessage = plugin.getWordFilter().apply(legacyMessage);
            }

            if (plugin.getConfig().getBoolean("ChatCo.playtimeGate.linkBlock.enabled", true)
                    && plugin.getLinkBlocker() != null && plugin.getLinkBlocker().isLoaded()) {
                legacyMessage = plugin.getLinkBlocker().apply(legacyMessage);
            }
        }

        // If filtering emptied the message, drop it entirely
        if (gated && legacyMessage.trim().isEmpty()) {
            event.viewers().clear();
            event.setCancelled(true);
            return;
        }

        // Send to the players, per-recipient so gated viewers can see a filtered version
        if (!plugin.getConfig().getBoolean("ChatCo.chatDisabled", false)) {
            for (Audience recipient : event.viewers()) {
                try {
                    if (recipient instanceof Player) {
                        ChatPlayer chatPlayer = plugin.getChatPlayer((Player) recipient);

                        if (chatPlayer.chatDisabled) continue;

                        if (chatPlayer.isIgnored(player.getName()) && plugin.getConfig().getBoolean("ChatCo.ignoresEnabled", true))
                            continue;
                    }

                    // per recipient gate: gated viewers get the sanitized text, others get the original
                    String recipientText = legacyMessage;
                    if (recipient instanceof Player && plugin.isGated((Player) recipient)) {
                        if (plugin.getConfig().getBoolean("ChatCo.playtimeGate.wordFilter.enabled", true)
                                && plugin.getWordFilter() != null && plugin.getWordFilter().isLoaded()) {
                            recipientText = plugin.getWordFilter().apply(recipientText);
                        }
                        if (plugin.getConfig().getBoolean("ChatCo.playtimeGate.linkBlock.enabled", true)
                                && plugin.getLinkBlocker() != null && plugin.getLinkBlocker().isLoaded()) {
                            recipientText = plugin.getLinkBlocker().apply(recipientText);
                        }
                        if (recipientText.trim().isEmpty()) continue;
                    }

                    Component recipientMessage = buildMessage(player, sender, recipientText);
                    recipient.sendMessage(recipientMessage);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        // 1 time rules notice for gated senders (do we need to store it in Redis so it persists across restart? probably not)
        if (gated
                && plugin.getConfig().getBoolean("ChatCo.playtimeGate.rulesNotice.enabled", true)) {
            ChatPlayer chatPlayer = plugin.getChatPlayer(player);
            if (!chatPlayer.rulesNoticeSent) {
                String rulesMsg = plugin.getConfig().getString("ChatCo.playtimeGate.rulesNotice.message");
                if (rulesMsg != null && !rulesMsg.isEmpty()) {
                    for (String line : rulesMsg.split("\\n")) {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
                    }
                }
                chatPlayer.rulesNoticeSent = true;
            }
        }

        // Do not send it to the players again - no event cancelling, so that other plugins can process the chat
        event.viewers().clear();
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        plugin.remove(e.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(final PlayerKickEvent e) {
        plugin.remove(e.getPlayer());
    }
}
