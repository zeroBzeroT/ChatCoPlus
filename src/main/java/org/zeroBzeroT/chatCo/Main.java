package org.zeroBzeroT.chatCo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import static org.zeroBzeroT.chatCo.Utils.saveStreamToFile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Main extends JavaPlugin {
    public static File PermissionConfig;
    public static File WhisperLog;
    public static File dataFolder;
    private static File Help;
    public Collection<ChatPlayer> playerList;
    private Whispers whispers;
    
    @Override
    public void onDisable() {
        playerList.clear();
    }

    @Override
    public void onEnable() {
        playerList = Collections.synchronizedCollection(new ArrayList<>());

        // Config defaults
        getConfig().options().copyDefaults(true);
        getConfig().options().parseComments(true);

        saveResourceFiles();
        toggleConfigValue(0);

        final PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new PublicChat(this), this);

        if (getConfig().getBoolean("ChatCo.whisperChangesEnabled", true)) {
            whispers = new Whispers(this);
        }

        if (getConfig().getBoolean("ChatCo.spoilersEnabled", false)) {
            pm.registerEvents(new Spoilers(), this);
        }

        // Load Plugin Metrics
        if (getConfig().getBoolean("ChatCo.bStats", true)) {
            new Metrics(this, 16309);
        }
    }

    private void toggleConfigValue(final int change) {
        switch (change) {
            case 3:
                getConfig().set("ChatCo.spoilersEnabled", true);
                break;
            case 4:
                getConfig().set("ChatCo.spoilersEnabled", false);
                break;
            case 5:
                getConfig().set("ChatCo.whisperChangesEnabled", true);
                break;
            case 6:
                getConfig().set("ChatCo.whisperChangesEnabled", false);
                break;
            case 7:
                getConfig().set("ChatCo.newCommands", true);
                break;
            case 8:
                getConfig().set("ChatCo.newCommands", false);
                break;
            case 9:
                getConfig().set("ChatCo.whisperLog", true);
                break;
            case 10:
                getConfig().set("ChatCo.whisperLog", false);
                break;
        }

        saveConfig();
        reloadConfig();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveResourceFiles() {
        Main.dataFolder = getDataFolder();
        Main.PermissionConfig = new File(Main.dataFolder, "permissionConfig.yml");
        Main.WhisperLog = new File(Main.dataFolder, "whisperlog.txt");
        Main.Help = new File(Main.dataFolder, "help.txt");

        if (!Main.WhisperLog.exists()) {
            Main.WhisperLog.getParentFile().mkdirs();
            saveStreamToFile(getResource("whisperlog.txt"), Main.WhisperLog);
        }

        if (!Main.Help.exists()) {
            Main.Help.getParentFile().mkdirs();
            saveStreamToFile(getResource("help.txt"), Main.Help);
        }

        // Save the default config file, if it does not exist
        saveDefaultConfig();

        if (!Main.PermissionConfig.exists()) {
            Main.PermissionConfig.getParentFile().mkdirs();
            saveStreamToFile(getResource("permissionConfig.yml"), Main.PermissionConfig);
        }
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd, final @NotNull String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            if (cmd.getName().equalsIgnoreCase("togglechat") && getConfig().getBoolean("toggleChatEnabled", true)) {
                if (toggleChat((Player) sender)) {
                    sender.sendMessage(Component.text("Your chat is now disabled until you type /togglechat or relog.", NamedTextColor.RED));
                } else {
                    sender.sendMessage(Component.text("Your chat has been re-enabled, type /togglechat to disable it again.", NamedTextColor.RED));
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("toggletells")) {
                if (toggleTells((Player) sender)) {
                    sender.sendMessage(Component.text("You will no longer receive tells, type /toggletells to see them again.", NamedTextColor.RED));
                } else {
                    sender.sendMessage(Component.text("You now receive tells, type /toggletells to disable them again.", NamedTextColor.RED));
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("unignoreall") && getConfig().getBoolean("ignoresEnabled", true)) {
                try {
                    unIgnoreAll((Player) sender);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("ignore") && getConfig().getBoolean("ignoresEnabled", true)) {
                try {
                    if (args.length < 1) {
                        sender.sendMessage(Component.text("You forgot to type the name of the player.", NamedTextColor.RED));
                        return true;
                    }

                    if (args[0].length() > 16) {
                        sender.sendMessage(Component.text("You entered an invalid player name.", NamedTextColor.RED));
                        return true;
                    }

                    final Player ignorable = Bukkit.getServer().getPlayer(args[0]);

                    if (ignorable == null) {
                        sender.sendMessage(Component.text("You have entered a player who does not exist or is offline.", NamedTextColor.RED));
                        return true;
                    }

                    ignorePlayer((Player) sender, args[0]);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (cmd.getName().equalsIgnoreCase("ignorelist") && getConfig().getBoolean("ignoresEnabled", true)) {
                sender.sendMessage(Component.text("Ignored players:", NamedTextColor.YELLOW));
                int i = 0;

                for (final String ignores : getChatPlayer((Player) sender).getIgnoresFile()) {
                    sender.sendMessage(Component.text(ignores, NamedTextColor.YELLOW, TextDecoration.ITALIC));
                    ++i;
                }

                sender.sendMessage(Component.text("You have " + i + " players ignored.", NamedTextColor.YELLOW));
                return true;
            } else if (whispers != null && whispers.onCommand(this, sender, cmd, commandLabel, args)) {
                // Command was processed by the whisper module
                return true;
            } else if (cmd.getName().equalsIgnoreCase("whoignore")) {
                List<String> ignoredByList = getChatPlayer((Player) sender).getIgnoredByList();
            
                if (ignoredByList.isEmpty()) {
                    sender.sendMessage(Component.text("No one is ignoring you.", NamedTextColor.YELLOW));
                } else {
                    sender.sendMessage(Component.text("Players ignoring you:", NamedTextColor.YELLOW));
                    for (String name : ignoredByList) {
                        sender.sendMessage(Component.text(name, NamedTextColor.RED));
                    }
                }
                return true;
            }
            
        }

        if (cmd.getName().equalsIgnoreCase("chatco")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                saveConfig();
                sender.sendMessage("Config reloaded");
                return true;
            }

            if (args.length >= 2) {
                if (args[0].equalsIgnoreCase("spoilers")) {
                    if (args[1].equalsIgnoreCase("e")) {
                        toggleConfigValue(3);
                        sender.sendMessage("Spoilers enabled");
                    } else if (args[1].equalsIgnoreCase("d")) {
                        toggleConfigValue(4);
                        sender.sendMessage("Spoilers disabled");
                    }
                }

                if (args[0].equalsIgnoreCase("whispers")) {
                    if (args[1].equalsIgnoreCase("e")) {
                        toggleConfigValue(5);
                        sender.sendMessage("Whisper changes enabled");
                    } else if (args[1].equalsIgnoreCase("d")) {
                        toggleConfigValue(6);
                        sender.sendMessage("Whisper changes disabled");
                    }
                }

                if (args[0].equalsIgnoreCase("newcommands")) {
                    if (args[1].equalsIgnoreCase("e")) {
                        toggleConfigValue(7);
                        sender.sendMessage("New Whisper commands enabled");
                    } else if (args[1].equalsIgnoreCase("d")) {
                        toggleConfigValue(8);
                        sender.sendMessage("New whisper commands disabled");
                    }
                }

                if (args[0].equalsIgnoreCase("whisperlog")) {
                    if (args[1].equalsIgnoreCase("e")) {
                        toggleConfigValue(9);
                        sender.sendMessage("Whisper logging enabled");
                    } else if (args[1].equalsIgnoreCase("d")) {
                        toggleConfigValue(10);
                        sender.sendMessage("Whisper logging disabled");
                    }
                }

                return true;
            }
        }

        return false;
    }

    public ChatPlayer getChatPlayer(final Player p) {
        for (final ChatPlayer chatPlayer : playerList) {
            if (chatPlayer.playerUUID.equals(p.getUniqueId())) {
                return chatPlayer;
            }
        }

        ChatPlayer newChatPlayer = null;

        try {
            newChatPlayer = new ChatPlayer(p);
            playerList.add(newChatPlayer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newChatPlayer;
    }

    private boolean toggleChat(final Player p) {
        if (getChatPlayer(p).chatDisabled) {
            return getChatPlayer(p).chatDisabled = false;
        }

        return getChatPlayer(p).chatDisabled = true;
    }

    private boolean toggleTells(final Player p) {
        if (getChatPlayer(p).tellsDisabled) {
            return getChatPlayer(p).tellsDisabled = false;
        }

        return getChatPlayer(p).tellsDisabled = true;
    }

    private void ignorePlayer(final Player p, final String target) throws IOException {
        Component message = Component.text("Chat messages from " + target + " will be ");

        if (getChatPlayer(p).isIgnored(target)) {
            message = message.append(Component.text("shown."));
        } else {
            message = message.append(Component.text("hidden."));
        }

        p.sendMessage(message.color(NamedTextColor.YELLOW));
        getChatPlayer(p).saveIgnoreList(target);
    }

    private void unIgnoreAll(final Player p) throws IOException {
        getChatPlayer(p).unIgnoreAll();
        p.sendMessage(Component.text("Ignore list deleted.", NamedTextColor.YELLOW));
    }

    public void remove(Player player) {
        playerList.removeIf(p -> p.playerUUID.equals(player.getUniqueId()));
    }
}

