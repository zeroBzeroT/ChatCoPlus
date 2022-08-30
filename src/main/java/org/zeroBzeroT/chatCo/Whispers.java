package org.zeroBzeroT.chatCo;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.zeroBzeroT.chatCo.Utils.componentFromLegacyText;
import static org.zeroBzeroT.chatCo.Utils.now;

public class Whispers implements Listener {
    public final Main plugin;

    public Whispers(final Main plugin) {
        this.plugin = plugin;
    }

    /**
     * high event priority (lower rank) makes it easier for other plugins to block commands of this plugin
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final String[] args = event.getMessage().split(" ");
        final Player sender = event.getPlayer();

        if (plugin.getConfig().getBoolean("ChatCo.lastCommand", true) && (args[0].equalsIgnoreCase("/l") || args[0].equalsIgnoreCase("/last"))) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.WHITE + "Usage: /l <message>");
                event.setCancelled(true);
                return;
            }

            final Player target = plugin.getChatPlayer(sender).getLastReceiver();

            if ((target == null && plugin.getChatPlayer(sender).LastReceiver != null)
                    || Utils.isVanished(target)) {
                sender.sendMessage(ChatColor.RED + "The last person you sent a private message to is offline");
            } else if (target == null) {
                sender.sendMessage(ChatColor.RED + "You have not initiated any private message in this session");
            } else {
                String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                sendPrivateMessage(sender, target, message);
            }

            event.setCancelled(true);
        } else if (plugin.getConfig().getBoolean("ChatCo.replyCommands", true) && (args[0].equalsIgnoreCase("/r") || args[0].equalsIgnoreCase("/reply"))) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.WHITE + "Usage: /r <message>");
                event.setCancelled(true);
                return;
            }

            final Player target = plugin.getChatPlayer(sender).getLastMessenger();

            if ((target == null && plugin.getChatPlayer(sender).LastMessenger != null)
                    || Utils.isVanished(target)) {
                sender.sendMessage(ChatColor.RED + "The last person you received a private message from is offline");
            } else if (target == null) {
                sender.sendMessage(ChatColor.RED + "You have not received any private messages in this session");
            } else {
                String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                sendPrivateMessage(sender, target, message);
            }

            event.setCancelled(true);
        } else if (args[0].equalsIgnoreCase("/tell") || args[0].equalsIgnoreCase("/msg") || args[0].equalsIgnoreCase("/t") || args[0].equalsIgnoreCase("/w") || args[0].equalsIgnoreCase("/whisper") || args[0].equalsIgnoreCase("/pm")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.WHITE + "Usage: /w <player> <message>");
                event.setCancelled(true);
                return;
            }

            final Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null || Utils.isVanished(target)) {
                sender.sendMessage(ChatColor.RED + args[1] + " is offline");
                event.setCancelled(true);
                return;
            }

            if (plugin.getConfig().getBoolean("ChatCo.newCommands", true)) {
                String message = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
                sendPrivateMessage(sender, target, message);
                event.setCancelled(true);
                plugin.getChatPlayer(sender).setLastReceiver(target);
            } else if (args[0].equalsIgnoreCase("/tell ") || args[0].equalsIgnoreCase("/w ") || args[0].equalsIgnoreCase("/msg ")) {
                String message = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
                sendPrivateMessage(sender, target, message);
                event.setCancelled(true);
                plugin.getChatPlayer(sender).setLastReceiver(target);
            }
        }
    }

    public TextComponent whisperFormat(Boolean send, final Player sender, final Player target) {
        String legacyMessage = send ? plugin.getConfig().getString("ChatCo.whisperFormat.send") : plugin.getConfig().getString("ChatCo.whisperFormat.receive");

        for (ChatColor color : ChatColor.values()) {
            legacyMessage = legacyMessage.replace("%" + color.name() + "%", color.toString());
        }

        String[] parts;
        String name;

        if (send) {
            legacyMessage = legacyMessage.replace("%SENDER%", sender.getName());
            parts = legacyMessage.split("%RECEIVER%", 2);
            name = target.getName();
        } else {
            legacyMessage = legacyMessage.replace("%RECEIVER%", target.getName());
            parts = legacyMessage.split("%SENDER%", 2);
            name = sender.getName();
        }

        // Part before player name
        TextComponent message = componentFromLegacyText(parts[0]);

        // Player name
        TextComponent messagePlayer = componentFromLegacyText(name);

        if (plugin.getConfig().getBoolean("ChatCo.whisperOnClick", true)) {
            messagePlayer.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/w " + name + " "));
            messagePlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Whisper to " + name).create()));
        }

        if (messagePlayer.getColor() == net.md_5.bungee.api.ChatColor.WHITE)
            messagePlayer.setColor(message.getColor());
        message.addExtra(messagePlayer);

        // Part after player name
        if (parts.length == 2) {
            TextComponent part1 = componentFromLegacyText(parts[1]);
            if (part1.getColor() == net.md_5.bungee.api.ChatColor.WHITE)
                message.setColor(message.getColor());
            message.addExtra(part1);
        }

        return message;
    }

    private void sendPrivateMessage(Player sender, Player receiver, String message) {
        boolean doNotSend = false;
        boolean isIgnoring = false;
        ChatPlayer target = plugin.getChatPlayer(receiver);

        if (target != null && target.tellsDisabled) {
            doNotSend = true;
        }

        if (target != null && target.isIgnored(sender.getName())) {
            isIgnoring = true;
        }

        final TextComponent senderMessage = whisperFormat(true, sender, receiver);
        final TextComponent receiverMessage = whisperFormat(false, sender, receiver);

        receiverMessage.addExtra(message);
        senderMessage.addExtra(message);

        sender.spigot().sendMessage(senderMessage);

        if (isIgnoring && plugin.getConfig().getBoolean("ChatCo.ignoreMessageEnabled", true)) {
            sender.sendMessage(ChatColor.RED + receiver.getName() + " is ignoring you");
        } else if (doNotSend && plugin.getConfig().getBoolean("ChatCo.chatDisabledMessageEnabled", true)) {
            sender.sendMessage(ChatColor.RED + receiver.getName() + "'s chat is disabled");
        } else if (!doNotSend && !isIgnoring) {
            receiver.spigot().sendMessage(receiverMessage);

            if (target != null)
                target.setLastMessenger(sender);
        }

        // Logging
        String logText = message;

        if (doNotSend || isIgnoring) {
            logText = "***WAS NOT SENT*** " + logText;
        }
        if (plugin.getConfig().getBoolean("ChatCo.whisperLog", false)) {
            whisperLog(logText, sender.getName());
        }
        if (plugin.getConfig().getBoolean("ChatCo.whisperMonitoring", false)) {
            plugin.getLogger().info(sender.getName() + ": " + logText);
        }
    }

    public void whisperLog(final String text, final String sender) {
        try {
            final FileWriter fwo = new FileWriter(Main.WhisperLog, true);
            final BufferedWriter bwo = new BufferedWriter(fwo);
            bwo.write(now() + " " + sender + ": " + text);
            bwo.newLine();
            bwo.close();
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }
}
