package org.zeroBzeroT.chatCo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Main extends JavaPlugin {
    public static File PermissionConfig;
    public static File WhisperLog;
    public static File dataFolder;
    private static File Configuration;
    private static File Help;
    public final Collection<ChatPlayer> playerList;
    private final PublicChat publicChat;
    private final Spoilers spoilerListener;
    private final Whispers whisperListener;
    public boolean checkForChatDisable;
    public boolean checkForIgnores;

    public Main() {
        this.publicChat = new PublicChat(this);
        this.spoilerListener = new Spoilers();
        this.whisperListener = new Whispers(this);
        this.checkForChatDisable = false;
        this.checkForIgnores = false;
        this.playerList = Collections.synchronizedCollection(new ArrayList<>());
    }

    public void onDisable() {
        this.playerList.clear();
    }

    public void onEnable() {
        this.checkFiles();
        this.readConfig(0);
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(this.publicChat, this);

        if (this.getConfig().getBoolean("ChatCo.chatDisableEnabled", true)) {
            this.checkForChatDisable = true;
        }

        if (this.getConfig().getBoolean("ChatCo.ignoresEnabled", true)) {
            this.checkForIgnores = true;
        }

        if (this.getConfig().getBoolean("ChatCo.WhisperChangesEnabled", true)) {
            pm.registerEvents(this.whisperListener, this);
        }

        if (this.getConfig().getBoolean("ChatCo.SpoilersEnabled", false)) {
            pm.registerEvents(this.spoilerListener, this);
        }
    }

    private void readConfig(final int change) {
        switch (change) {
            case 3:
                this.getConfig().set("ChatCo.SpoilersEnabled", true);
                break;
            case 4:
                this.getConfig().set("ChatCo.SpoilersEnabled", false);
                break;
            case 5:
                this.getConfig().set("ChatCo.WhisperChangesEnabled", true);
                break;
            case 6:
                this.getConfig().set("ChatCo.WhisperChangesEnabled", false);
                break;
            case 7:
                this.getConfig().set("ChatCo.NewCommands", true);
                break;
            case 8:
                this.getConfig().set("ChatCo.NewCommands", false);
                break;
            case 9:
                this.getConfig().set("ChatCo.WhisperLog", true);
                break;
            case 10:
                this.getConfig().set("ChatCo.WhisperLog", false);
                break;
        }

        this.saveConfig();
        this.reloadConfig();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void checkFiles() {
        Main.dataFolder = this.getDataFolder();
        Main.Configuration = new File(Main.dataFolder, "config.yml");
        Main.PermissionConfig = new File(Main.dataFolder, "permissionConfig.yml");
        Main.WhisperLog = new File(Main.dataFolder, "whisperlog.txt");
        Main.Help = new File(Main.dataFolder, "help.txt");

        if (!Main.WhisperLog.exists()) {
            Main.WhisperLog.getParentFile().mkdirs();
            this.copy(this.getResource("whisperlog.txt"), Main.WhisperLog);
        }

        if (!Main.Help.exists()) {
            Main.Help.getParentFile().mkdirs();
            this.copy(this.getResource("help.txt"), Main.Help);
        }

        if (!Main.Configuration.exists()) {
            this.saveDefaultConfig();
        }

        if (!Main.PermissionConfig.exists()) {
            Main.PermissionConfig.getParentFile().mkdirs();
            this.copy(this.getResource("permissionConfig.yml"), Main.PermissionConfig);
        }
    }

    private void copy(final InputStream in, final File file) {
        try {
            final OutputStream out = Files.newOutputStream(file.toPath());
            final byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (sender instanceof org.bukkit.entity.Player) {
            if (cmd.getName().equalsIgnoreCase("togglechat") && this.getConfig().getBoolean("toggleChatEnabled", true)) {
                try {
                    if (this.toggleChat((org.bukkit.entity.Player) sender)) {
                        sender.sendMessage(ChatColor.RED + "Your chat is now disabled until you type /togglechat or relog");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Your chat has been re-enabled, type /togglechat to disable it again");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("toggletells")) {
                try {
                    if (this.toggleTells((org.bukkit.entity.Player) sender)) {
                        sender.sendMessage(ChatColor.RED + "You will no longer receive tells, type /toggletells to see them again");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You now receive tells, type /toggletells to disable them again");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("unignoreall") && this.getConfig().getBoolean("ignoresEnabled", true)) {
                try {
                    this.unIgnoreAll((org.bukkit.entity.Player) sender);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("ignore") && this.getConfig().getBoolean("ignoresEnabled", true)) {
                try {
                    if (args.length < 1) {
                        sender.sendMessage(ChatColor.RED + "You forgot to type the name of the player");
                        return true;
                    }

                    if (args[0].length() > 16) {
                        sender.sendMessage(ChatColor.RED + "You entered an invalid player name");
                        return true;
                    }

                    final org.bukkit.entity.Player ignorable = Bukkit.getServer().getPlayer(args[0]);

                    if (ignorable == null) {
                        sender.sendMessage(ChatColor.RED + "You have entered a player who does not exist or is offline");
                        return true;
                    }

                    this.ignorePlayer((org.bukkit.entity.Player) sender, args[0]);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (cmd.getName().equalsIgnoreCase("ignorelist") && this.getConfig().getBoolean("ignoresEnabled", true)) {
                try {
                    sender.sendMessage(ChatColor.WHITE + "Ignored players:");
                    int i = 0;

                    for (final String ignores : this.getCCPlayer((org.bukkit.entity.Player) sender).getIgnoreList()) {
                        sender.sendMessage(ChatColor.WHITE + "" + ChatColor.ITALIC + ignores);
                        ++i;
                    }

                    sender.sendMessage(ChatColor.WHITE + "" + i + " players ignored");
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (args.length > 0) {
            if (cmd.getName().equalsIgnoreCase("chatco")) {
                if (args[1] == null) {
                    sender.sendMessage(ChatColor.RED + "You forgot to specify whether you wanted to enable or disable the component (/chatco <component> <e|d>)");
                    return true;
                }

                if (args[0].equalsIgnoreCase("spoilers")) {
                    if (args[1].equalsIgnoreCase("e")) {
                        this.readConfig(3);
                        sender.sendMessage("Spoilers enabled");
                    } else if (args[1].equalsIgnoreCase("d")) {
                        this.readConfig(4);
                        sender.sendMessage("Spoilers disabled");
                    }
                }

                if (args[0].equalsIgnoreCase("whispers")) {
                    if (args[1].equalsIgnoreCase("e")) {
                        this.readConfig(5);
                        sender.sendMessage("Whisper changes enabled");
                    } else if (args[1].equalsIgnoreCase("d")) {
                        this.readConfig(6);
                        sender.sendMessage("Whisper changes disabled");
                    }
                }

                if (args[0].equalsIgnoreCase("newcommands")) {
                    if (args[1].equalsIgnoreCase("e")) {
                        this.readConfig(7);
                        sender.sendMessage("New Whisper commands enabled");
                    } else if (args[1].equalsIgnoreCase("d")) {
                        this.readConfig(8);
                        sender.sendMessage("New whisper commands disabled");
                    }
                }

                if (args[0].equalsIgnoreCase("whisperlog")) {
                    if (args[1].equalsIgnoreCase("e")) {
                        this.readConfig(9);
                        sender.sendMessage("Whisper logging enabled");
                    } else if (args[1].equalsIgnoreCase("d")) {
                        this.readConfig(10);
                        sender.sendMessage("Whisper logging disabled");
                    }
                }

                return true;
            }
        }
        return false;
    }

    public ChatPlayer getCCPlayer(final org.bukkit.entity.Player p) throws IOException {
        for (final ChatPlayer cp : this.playerList) {
            if (cp.playerName.equals(p.getName())) {
                return cp;
            }
        }

        final ChatPlayer ccp = new ChatPlayer(p);
        this.playerList.add(ccp);
        return ccp;
    }

    private boolean toggleChat(final org.bukkit.entity.Player p) throws IOException {
        if (this.getCCPlayer(p).chatDisabled) {
            return this.getCCPlayer(p).chatDisabled = false;
        }

        return this.getCCPlayer(p).chatDisabled = true;
    }

    private boolean toggleTells(final org.bukkit.entity.Player p) throws IOException {
        if (this.getCCPlayer(p).tellsDisabled) {
            return this.getCCPlayer(p).tellsDisabled = false;
        }

        return this.getCCPlayer(p).tellsDisabled = true;
    }

    private void ignorePlayer(final org.bukkit.entity.Player p, final String target) throws IOException {
        String message = ChatColor.WHITE + "Chat messages from " + target + " will be ";

        if (this.getCCPlayer(p).isIgnored(target)) {
            message += "shown";
        } else {
            message += "hidden";
        }

        p.sendMessage(message);
        this.getCCPlayer(p).saveIgnoreList(target);
    }

    private void unIgnoreAll(final org.bukkit.entity.Player p) throws IOException {
        this.getCCPlayer(p).unIgnoreAll();
        String message = ChatColor.WHITE + "Ignore list deleted";
        p.sendMessage(message);
    }

    public void remove(org.bukkit.entity.Player player) {
        playerList.removeIf(p -> p.player.equals(player));
    }
}

