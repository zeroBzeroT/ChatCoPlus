package org.zeroBzeroT.chatCo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.zeroBzeroT.chatCo.Utils.now;

public record Whispers(Main plugin) {
    public Component whisperFormat(Boolean isSending, final Player sender, final Player target) {
        String messageFormat = isSending ? plugin.getConfig().getString("ChatCo.whisperFormat.send") : plugin.getConfig().getString("ChatCo.whisperFormat.receive");

        String[] parts;
        String name;

        assert messageFormat != null;

        if (isSending) {
            //legacyMessage = legacyMessage.replace("%SENDER%", sender.getName());
            parts = messageFormat.split("%RECEIVER%", 2);
            name = target.getName();
        } else {
            //legacyMessage = legacyMessage.replace("%RECEIVER%", target.getName());
            parts = messageFormat.split("%SENDER%", 2);
            name = sender.getName();
        }

        // Part before player name
        var message = Component.text(parts[0]);

        // Player name
        var messagePlayer = Component.text(name);

        if (plugin.getConfig().getBoolean("ChatCo.whisperOnClick", true)) {
            messagePlayer = messagePlayer.clickEvent(ClickEvent.suggestCommand("/w " + name + " "));
            messagePlayer = messagePlayer.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Whisper to " + name)));
        }

        message = message.append(messagePlayer);

        // Part after player name
        if (parts.length == 2) {
            message = message.append(Component.text(parts[1]));
        }

        return message;
    }

    void sendPrivateMessage(Player sender, Player receiver, String message) {
        boolean doNotSend = false;
        boolean isIgnoring = false;
        ChatPlayer target = plugin.getChatPlayer(receiver);

        if (target != null && target.tellsDisabled) {
            doNotSend = true;
        }

        if (target != null && target.isIgnored(sender.getName())) {
            isIgnoring = true;
        }

        Component senderMessage = whisperFormat(true, sender, receiver);
        Component receiverMessage = whisperFormat(false, sender, receiver);

        var color = NamedTextColor.NAMES.value(Objects.requireNonNull(plugin.getConfig().getString("ChatCo.whisperFormat.color")));

        receiverMessage = receiverMessage.append(Component.text(message)).color(color);
        senderMessage = senderMessage.append(Component.text(message)).color(color);

        sender.sendMessage(senderMessage);

        if (isIgnoring && plugin.getConfig().getBoolean("ChatCo.ignoreMessageEnabled", true)) {
            sender.sendMessage(Component.text(receiver.getName() + " is ignoring you.", NamedTextColor.RED));
        } else if (doNotSend && plugin.getConfig().getBoolean("ChatCo.chatDisabledMessageEnabled", true)) {
            sender.sendMessage(Component.text(receiver.getName() + "'s chat is disabled.", NamedTextColor.RED));
        } else if (!doNotSend && !isIgnoring) {
            receiver.sendMessage(receiverMessage);

            if (target != null) target.setLastMessenger(sender);
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

    public boolean onCommand(final @NotNull Main plugin, final @NotNull CommandSender sender, final @NotNull Command cmd, final @NotNull String commandLabel, final String[] args) {
        if (cmd.getName().equalsIgnoreCase("tell") || cmd.getName().equalsIgnoreCase("msg") || cmd.getName().equalsIgnoreCase("t") || cmd.getName().equalsIgnoreCase("w") || cmd.getName().equalsIgnoreCase("whisper") || cmd.getName().equalsIgnoreCase("pm")) {
            if (args.length < 2) {
                sender.sendMessage(Component.text("Usage: /w <player> <message>", NamedTextColor.YELLOW));
                return true;
            }

            final Player target = Bukkit.getPlayerExact(args[0]);

            if (target == null || Utils.isVanished(target)) {
                sender.sendMessage(Component.text(args[0] + " is offline.", NamedTextColor.RED));
                return true;
            }

            if (plugin.getConfig().getBoolean("ChatCo.newCommands", true)) {
                String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                sendPrivateMessage((Player) sender, target, message);
                plugin.getChatPlayer((Player) sender).setLastReceiver(target);
            } else if (cmd.getName().equalsIgnoreCase("tell") || cmd.getName().equalsIgnoreCase("w") || cmd.getName().equalsIgnoreCase("msg")) {
                String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                sendPrivateMessage((Player) sender, target, message);
                plugin.getChatPlayer((Player) sender).setLastReceiver(target);
            }

            return true;
        } else if (plugin.getConfig().getBoolean("ChatCo.lastCommand", true) && (cmd.getName().equalsIgnoreCase("l") || cmd.getName().equalsIgnoreCase("last"))) {
            if (args.length == 0) {
                sender.sendMessage(Component.text("Usage: /l <message>", NamedTextColor.YELLOW));
                return true;
            }

            final Player target = plugin.getChatPlayer((Player) sender).getLastReceiver();

            if ((target == null && plugin.getChatPlayer((Player) sender).LastReceiver != null) || Utils.isVanished(target)) {
                sender.sendMessage(Component.text("The last person you sent a private message to is offline.", NamedTextColor.RED));
            } else if (target == null) {
                sender.sendMessage(Component.text("You have not initiated any private message in this session.", NamedTextColor.RED));
            } else {
                String message = Arrays.stream(args).skip(0).collect(Collectors.joining(" "));
                sendPrivateMessage((Player) sender, target, message);
            }

            return true;
        } else if (plugin.getConfig().getBoolean("ChatCo.replyCommands", true) && (cmd.getName().equalsIgnoreCase("r") || cmd.getName().equalsIgnoreCase("reply"))) {
            if (args.length == 0) {
                sender.sendMessage(Component.text("Usage: /r <message>", NamedTextColor.YELLOW));
                return true;
            }

            final Player target = plugin.getChatPlayer((Player) sender).getLastMessenger();

            if ((target == null && plugin.getChatPlayer((Player) sender).LastMessenger != null) || Utils.isVanished(target)) {
                sender.sendMessage(Component.text("The last person you received a private message from is offline.", NamedTextColor.RED));
            } else if (target == null) {
                sender.sendMessage(Component.text("You have not received any private messages in this session.", NamedTextColor.RED));
            } else {
                String message = Arrays.stream(args).skip(0).collect(Collectors.joining(" "));
                sendPrivateMessage((Player) sender, target, message);
            }

            return true;
        }

        // did not process the command
        return false;
    }
}
