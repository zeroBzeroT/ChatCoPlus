package org.zeroBzeroT.chatCo;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.Objects;

public class PublicChat implements Listener {
    public final Main plugin;
    private final FileConfiguration permissionConfig;

    public PublicChat(final Main plugin) {
        this.plugin = plugin;
        File customConfig = Main.PermissionConfig;
        permissionConfig = YamlConfiguration.loadConfiguration(customConfig);
    }

    public String replacePrefixColors(String message, final Player player) {
        for (ChatColor color : ChatColor.values()) {
            if (plugin.getConfig().getString("ChatCo.chatPrefixes." + color.name()) != null && message.startsWith(Objects.requireNonNull(plugin.getConfig().getString("ChatCo.chatPrefixes." + color.name())))) {

                // check for global or player permission
                if (permissionConfig.getBoolean("ChatCo.chatPrefixes." + color.name(), false) || player.hasPermission("ChatCo.chatPrefixes." + color.name())) {
                    message = color + message;
                }

                // break here since we found a prefix color code
                break;
            }
        }

        return message;
    }

    public String replaceInlineColors(String message, final Player player) {
        for (ChatColor color : ChatColor.values()) {
            if ((permissionConfig.getBoolean("ChatCo.chatColors." + color.name(), false) || player.hasPermission("ChatCo.chatColors." + color.name())) && plugin.getConfig().getString("ChatCo.chatColors." + color.name()) != null) {
                message = message.replace(Objects.requireNonNull(plugin.getConfig().getString("ChatCo.chatColors." + color.name())), color.toString());
            }
        }

        return message;
    }

    /**
     * See <a href="https://docs.advntr.dev/text.html">Text (Chat Components)</a>
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        // Plain message
        final Player player = event.getPlayer();

        String legacyMessage = LegacyComponentSerializer.legacyAmpersand().serialize(event.message());
        legacyMessage = replacePrefixColors(legacyMessage, player);
        legacyMessage = replaceInlineColors(legacyMessage, player);

        // Do not send empty messages
        if (legacyMessage.trim().isEmpty()) {
            event.setCancelled(true);
            return;
        }

        // Message text
        Component messageText = Component.text(legacyMessage);

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
