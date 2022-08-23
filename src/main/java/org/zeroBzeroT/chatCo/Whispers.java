package org.zeroBzeroT.chatCo;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Whispers implements Listener {
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    public final Main plugin;

    public Whispers(final Main plugin) {
        this.plugin = plugin;
    }

    public static String now() {
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
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

    public String whisperFormat(final String who, final Player sender, final Player target) {
        String data;

        if (who.equalsIgnoreCase("sender")) {
            data = this.plugin.getConfig().getString("ChatCo.WhisperFormat.Send");
        } else {
            data = this.plugin.getConfig().getString("ChatCo.WhisperFormat.Receive");
        }

        data = data.replace("*UNDERLINE*", ChatColor.UNDERLINE.toString());
        data = data.replace("*ITALIC*", ChatColor.ITALIC.toString());
        data = data.replace("*STRIKETHROUGH*", ChatColor.STRIKETHROUGH.toString());
        data = data.replace("*BOLD*", ChatColor.BOLD.toString());
        data = data.replace("*WHITE*", ChatColor.WHITE.toString());
        data = data.replace("*RED*", ChatColor.RED.toString());
        data = data.replace("*BLACK*", ChatColor.BLACK.toString());
        data = data.replace("*DARK_RED*", ChatColor.DARK_RED.toString());
        data = data.replace("*DARK_GRAY*", ChatColor.DARK_GRAY.toString());
        data = data.replace("*DARK_BLUE*", ChatColor.DARK_BLUE.toString());
        data = data.replace("*DARK_PURPLE*", ChatColor.DARK_PURPLE.toString());
        data = data.replace("*BLUE*", ChatColor.BLUE.toString());
        data = data.replace("*LIGHT_PURPLE*", ChatColor.LIGHT_PURPLE.toString());
        data = data.replace("*DARK_GREEN*", ChatColor.DARK_GREEN.toString());
        data = data.replace("*GOLD*", ChatColor.GOLD.toString());
        data = data.replace("*GREEN*", ChatColor.GREEN.toString());
        data = data.replace("*YELLOW*", ChatColor.YELLOW.toString());
        data = data.replace("*DARK_AQUA*", ChatColor.DARK_AQUA.toString());
        data = data.replace("*AQUA*", ChatColor.AQUA.toString());
        data = data.replace("*GRAY*", ChatColor.GRAY.toString());
        data = data.replace("*NORM*", ChatColor.RESET.toString());
        data = data.replace("RECEIVER", target.getName());
        data = data.replace("SENDER", sender.getName());

        return data;
    }

    // high event priority (lower rank) makes it easier for other plugins to block commands of this plugin
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) throws IOException {
        boolean doNotSend = false;
        boolean isIgnoring = false;
        String inputText = event.getMessage();
        final String[] args = inputText.split(" ");
        final Player sender = event.getPlayer();

        if (this.plugin.getConfig().getBoolean("ChatCo.LastCommands", true) && (args[0].equals("/l") || args[0].equals("/last"))) {
            Player target = null;

            if (args.length == 1) {
                sender.sendMessage(ChatColor.WHITE + "Usage: /l <message>");
                event.setCancelled(true);
                return;
            }

            if (this.plugin.getCCPlayer(sender).getLastReceiver() != null) {
                target = this.plugin.getCCPlayer(sender).getLastReceiver();
            }

            if (target == null && this.plugin.getCCPlayer(sender).LastReceiver != null) {
                sender.sendMessage(ChatColor.RED + "The last person you sent a private message to is offline.");
            } else if (target == null) {
                sender.sendMessage(ChatColor.RED + "You have not initiated any private message in this session.");
            } else {
                try {
                    if (this.plugin.getCCPlayer(target).tellsDisabled) {
                        doNotSend = true;
                    }
                } catch (Exception ignored) {
                }

                if (this.plugin.checkForIgnores && this.plugin.getCCPlayer(target).isIgnored(sender.getName())) {
                    isIgnoring = true;
                }

                final StringBuilder message = new StringBuilder();

                for (int z = 1; z < args.length; ++z) {
                    if (z > 1) {
                        message.append(" ");
                    }
                    message.append(args[z]);
                }

                final String senderMessage = this.whisperFormat("sender", sender, target);
                final String receiverMessage = this.whisperFormat("target", sender, target);
                final String result = String.valueOf(receiverMessage) + message;
                sender.sendMessage(String.valueOf(senderMessage) + message);

                if (isIgnoring && this.plugin.getConfig().getBoolean("ChatCo.ignoreMessageEnabled", true)) {
                    sender.sendMessage(ChatColor.RED + target.getName() + " is ignoring you.");
                }

                if (doNotSend && this.plugin.getConfig().getBoolean("ChatCo.chatDisabledMessageEnabled", true)) {
                    sender.sendMessage(ChatColor.RED + target.getName() + "'s chat is disabled.");
                }

                if (!doNotSend && !isIgnoring) {
                    target.sendMessage(result);
                    this.plugin.getCCPlayer(target).setLastMessenger(sender);
                }

                if (doNotSend || isIgnoring) {
                    inputText = "***WAS NOT SENT*** " + inputText;
                }

                // Logging
                if (this.plugin.getConfig().getBoolean("ChatCo.WhisperLog", false)) {
                    this.whisperLog(inputText, sender.getName());
                }

                if (this.plugin.getConfig().getBoolean("ChatCo.WhisperMonitoring", true)) {
                    this.plugin.getLogger().info(sender.getName() + ": " + inputText);
                }
            }

            event.setCancelled(true);
        } else if (this.plugin.getConfig().getBoolean("ChatCo.ReplyCommands", true) && (args[0].equals("/r") || args[0].equals("/reply"))) {
            Player target = null;

            if (args.length == 1) {
                sender.sendMessage(ChatColor.WHITE + "Usage: /r <message>");
                event.setCancelled(true);
                return;
            }

            if (this.plugin.getCCPlayer(sender).getLastMessenger() != null) {
                target = this.plugin.getCCPlayer(sender).getLastMessenger();
            }

            if (target == null && this.plugin.getCCPlayer(sender).LastMessenger != null) {
                sender.sendMessage(ChatColor.RED + "The last person you received a private message from is offline.");
            } else if (target == null) {
                sender.sendMessage(ChatColor.RED + "You have not received any private messages in this session.");
            } else {
                try {
                    if (this.plugin.getCCPlayer(target).tellsDisabled) {
                        doNotSend = true;
                    }
                } catch (Exception ignored) {
                }

                if (this.plugin.checkForIgnores && this.plugin.getCCPlayer(target).isIgnored(sender.getName())) {
                    isIgnoring = true;
                }

                final StringBuilder message = new StringBuilder();

                for (int z = 1; z < args.length; ++z) {
                    if (z > 1) {
                        message.append(" ");
                    }
                    message.append(args[z]);
                }

                final String senderMessage = this.whisperFormat("sender", sender, target);
                final String receiverMessage = this.whisperFormat("target", sender, target);
                final String result = String.valueOf(receiverMessage) + message;
                sender.sendMessage(String.valueOf(senderMessage) + message);

                if (isIgnoring && this.plugin.getConfig().getBoolean("ChatCo.ignoreMessageEnabled", true)) {
                    sender.sendMessage(ChatColor.RED + target.getName() + " is ignoring you.");
                }

                if (doNotSend && this.plugin.getConfig().getBoolean("ChatCo.chatDisabledMessageEnabled", true)) {
                    sender.sendMessage(ChatColor.RED + target.getName() + "'s chat is disabled.");
                }

                if (!doNotSend && !isIgnoring) {
                    target.sendMessage(result);
                    this.plugin.getCCPlayer(target).setLastMessenger(sender);
                }

                if (doNotSend || isIgnoring) {
                    inputText = "***WAS NOT SENT*** " + inputText;
                }

                // Logging
                if (this.plugin.getConfig().getBoolean("ChatCo.WhisperLog", false)) {
                    this.whisperLog(inputText, sender.getName());
                }

                if (this.plugin.getConfig().getBoolean("ChatCo.WhisperMonitoring", true)) {
                    this.plugin.getLogger().info(sender.getName() + ": " + inputText);
                }
            }

            event.setCancelled(true);
        } else if (args[0].equals("/tell") || args[0].equals("/msg") || args[0].equals("/t") || args[0].equals("/w") || args[0].equals("/whisper") || args[0].equals("/pm")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.WHITE + "Usage: /w <player> <message>");
                event.setCancelled(true);
                return;
            }

            final Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                event.setCancelled(true);
                sender.sendMessage(ChatColor.RED + args[1] + " is offline.");
                return;
            }

            if (this.plugin.getConfig().getBoolean("ChatCo.NewCommands", true)) {
                try {
                    if (this.plugin.getCCPlayer(target).tellsDisabled) {
                        doNotSend = true;
                    }
                } catch (Exception ignored) {
                }

                if (this.plugin.checkForIgnores && this.plugin.getCCPlayer(target).isIgnored(sender.getName())) {
                    isIgnoring = true;
                }

                event.setCancelled(true);

                final StringBuilder message = new StringBuilder();

                for (int z = 2; z < args.length; ++z) {
                    if (z > 2) {
                        message.append(" ");
                    }
                    message.append(args[z]);
                }

                final String senderMessage = this.whisperFormat("sender", sender, target);
                final String receiverMessage = this.whisperFormat("target", sender, target);
                final String result = String.valueOf(receiverMessage) + message;
                sender.sendMessage(String.valueOf(senderMessage) + message);

                if (isIgnoring && this.plugin.getConfig().getBoolean("ChatCo.ignoreMessageEnabled", true)) {
                    sender.sendMessage(ChatColor.RED + target.getName() + " is ignoring you.");
                }

                if (doNotSend && this.plugin.getConfig().getBoolean("ChatCo.chatDisabledMessageEnabled", true)) {
                    sender.sendMessage(ChatColor.RED + target.getName() + "'s chat is disabled.");
                }

                if (!doNotSend && !isIgnoring) {
                    target.sendMessage(result);
                    this.plugin.getCCPlayer(target).setLastMessenger(sender);
                    this.plugin.getCCPlayer(sender).setLastReceiver(target);
                }

                if (doNotSend || isIgnoring) {
                    inputText = "***WAS NOT SENT*** " + inputText;
                }

                // Logging
                if (this.plugin.getConfig().getBoolean("ChatCo.WhisperLog", false)) {
                    this.whisperLog(inputText, sender.getName());
                }

                if (this.plugin.getConfig().getBoolean("ChatCo.WhisperMonitoring", true)) {
                    this.plugin.getLogger().info(sender.getName() + ": " + inputText);
                }
            } else {
                if (this.plugin.getCCPlayer(target).tellsDisabled) {
                    doNotSend = true;
                }

                if (this.plugin.checkForIgnores && this.plugin.getCCPlayer(target).isIgnored(sender.getName())) {
                    isIgnoring = true;
                }

                if (inputText.toLowerCase().startsWith("/tell ") || inputText.toLowerCase().startsWith("/w ") || inputText.toLowerCase().startsWith("/msg ")) {
                    event.setCancelled(true);
                    final StringBuilder message = new StringBuilder();

                    for (int z = 2; z < args.length; ++z) {
                        if (z > 2) {
                            message.append(" ");
                        }
                        message.append(args[z]);
                    }

                    final String senderMessage = this.whisperFormat("sender", sender, target);
                    final String receiverMessage = this.whisperFormat("target", sender, target);
                    final String result = String.valueOf(receiverMessage) + message;
                    sender.sendMessage(String.valueOf(senderMessage) + message);

                    if (isIgnoring && this.plugin.getConfig().getBoolean("ChatCo.ignoreMessageEnabled", true)) {
                        sender.sendMessage(String.valueOf(ChatColor.RED.toString()) + target + " is ignoring you.");
                    }

                    if (doNotSend && this.plugin.getConfig().getBoolean("ChatCo.chatDisabledMessageEnabled", true)) {
                        sender.sendMessage(String.valueOf(ChatColor.RED.toString()) + target + "'s chat is disabled.");
                    }

                    if (!doNotSend && !isIgnoring) {
                        target.sendMessage(result);
                        this.plugin.getCCPlayer(target).setLastMessenger(sender);
                        this.plugin.getCCPlayer(sender).setLastReceiver(target);
                    }

                    if (doNotSend || isIgnoring) {
                        inputText = "***WAS NOT SENT*** " + inputText;
                    }

                    // Logging
                    if (this.plugin.getConfig().getBoolean("ChatCo.WhisperLog", false)) {
                        this.whisperLog(inputText, sender.getName());
                    }

                    if (this.plugin.getConfig().getBoolean("ChatCo.WhisperMonitoring", true)) {
                        this.plugin.getLogger().info(sender.getName() + ": " + inputText);
                    }
                }
            }

            event.setCancelled(true);
        }
    }
}
