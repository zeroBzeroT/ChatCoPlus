package org.zeroBzeroT.chatCo;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;

import static org.zeroBzeroT.chatCo.Utils.componentFromLegacyText;

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
            if (plugin.getConfig().getString("ChatCo.chatPrefixes." + color.name()) != null && message.startsWith(plugin.getConfig().getString("ChatCo.chatPrefixes." + color.name()))) {

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
            if ((permissionConfig.getBoolean("ChatCo.chatColors." + color.name(), false) || player.hasPermission("ChatCo.chatColors." + color.name()))
                    && plugin.getConfig().getString("ChatCo.chatColors." + color.name()) != null) {
                message = message.replace(plugin.getConfig().getString("ChatCo.chatColors." + color.name()), color.toString());
            }
        }

        return message;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        // Set format to the plain message, since the player is not needed
        String oldFormat = event.getFormat();
        event.setFormat("%2$s");

        // Plain message
        final Player player = event.getPlayer();
        String legacyMessage = replacePrefixColors(event.getMessage(), player);
        legacyMessage = replaceInlineColors(legacyMessage, player);

        // Do not send empty messages
        if (ChatColor.stripColor(legacyMessage).trim().length() == 0) {
            event.setCancelled(true);
            return;
        }

        // Message text
        TextComponent messageText = componentFromLegacyText(legacyMessage);

        // Sender name
        TextComponent messageSender = componentFromLegacyText(player.getDisplayName());
        if (plugin.getConfig().getBoolean("ChatCo.whisperOnClick", true)) {
            messageSender.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/w " + player.getName() + " "));
            messageSender.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Whisper to " + player.getName()).create()));
        }

        // Message
        TextComponent message = new TextComponent();
        message.addExtra(componentFromLegacyText("<"));
        message.addExtra(messageSender);
        message.addExtra(componentFromLegacyText("> "));
        message.addExtra(messageText);

        // Send to the players
        for (Player recipient : event.getRecipients()) {
            try {
                ChatPlayer chatPlayer = plugin.getChatPlayer(recipient);

                if ((!chatPlayer.chatDisabled || !plugin.getConfig().getBoolean("ChatCo.chatDisableEnabled", true)) &&
                        (!chatPlayer.isIgnored(player.getName()) || !plugin.getConfig().getBoolean("ChatCo.ignoresEnabled", true))) {
                    recipient.spigot().sendMessage(message);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        // Do not send it to the players again - no event cancelling, so that other plugins can process the chat
        event.getRecipients().clear();

        // Write back the old format
        event.setFormat(oldFormat);
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
