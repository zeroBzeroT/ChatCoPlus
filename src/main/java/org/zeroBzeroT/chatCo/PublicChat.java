package org.zeroBzeroT.chatCo;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
import java.util.regex.Pattern;

public class PublicChat implements Listener {
    public final Main plugin;
    private final FileConfiguration permissionConfig;

    public static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_\\\\+.~#?&/=]*");

    public PublicChat(final Main plugin) {
        this.plugin = plugin;
        File customConfig = Main.PermissionConfig;
        permissionConfig = YamlConfiguration.loadConfiguration(customConfig);
    }

    public Component replacePrefixColors(Component message, final Player player) {
        String messagePlain = PlainTextComponentSerializer.plainText().serialize(message);

        for (String color : NamedTextColor.NAMES.keys()) {
            String configKey = "ChatCo.chatPrefixes." + color;
            String configValue = plugin.getConfig().getString(configKey);

            if (configValue != null && messagePlain.startsWith(configValue)) {
                if (permissionConfig.getBoolean(configKey, false) || player.hasPermission(configKey)) {
                    return message.color(NamedTextColor.NAMES.value(color));
                }
            }
        }

        return message;
    }

    public Component replaceInlineColors(Component message, final Player player) {
        for (String color : NamedTextColor.NAMES.keys()) {
            String configKey = "ChatCo.chatColors." + color;
            String configValue = plugin.getConfig().getString(configKey);

            if (configValue != null) {
                if (permissionConfig.getBoolean(configKey, false) || player.hasPermission(configKey)) {
                    return message.replaceText(TextReplacementConfig.builder()
                            .match(Pattern.quote(configValue) + ".*$")
                            .replacement(s -> s.content(s.content().substring(configValue.length())).color(NamedTextColor.NAMES.value(color)))
                            .build());
                }
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

        // Message text
        Component messageText = Component.text(legacyMessage);

        // Player
        final Player player = event.getPlayer();

        // Replace color codes
        messageText = replacePrefixColors(messageText, player);
        messageText = replaceInlineColors(messageText, player);

        // Clickable links
        messageText = replaceUrls(messageText);

        // Sender name
        Component sender = player.displayName();

        if (plugin.getConfig().getBoolean("ChatCo.whisperOnClick", true)) {
            sender = sender.clickEvent(ClickEvent.suggestCommand("/w " + player.getName() + " "));
            sender = sender.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Whisper to " + player.getName())));
        }

        // Build Message
        TextComponent message = Component.text("").append(Component.text("<")).append(sender).append(Component.text("> ")).append(messageText);

        // Send to the players
        if (!plugin.getConfig().getBoolean("ChatCo.chatDisabled", false)) {
            for (Audience recipient : event.viewers()) {
                try {
                    if (recipient instanceof Player) {
                        ChatPlayer chatPlayer = plugin.getChatPlayer((Player) recipient);

                        if (chatPlayer.chatDisabled) continue;

                        if (chatPlayer.isIgnored(player.getName()) && plugin.getConfig().getBoolean("ChatCo.ignoresEnabled", true))
                            continue;
                    }

                    recipient.sendMessage(message);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
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
