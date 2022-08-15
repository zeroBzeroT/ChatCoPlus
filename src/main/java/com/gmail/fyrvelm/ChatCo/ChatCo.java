package com.gmail.fyrvelm.ChatCo;

import java.util.Iterator;
import org.bukkit.Bukkit;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatCo extends JavaPlugin
{
    private final CCAllChat allChat;
    private final CCSpoilers spoilerListener;
    private final CCWhispers whisperListener;
    private static File Configuration;
    public static File Configuration2;
    private static File Help;
    public static File WhisperLog;
    public boolean checkForChatDisable;
    public boolean checkForIgnores;
    public final ArrayList<CCPlayer> playerlist;
    public static File dataFolder;
    
    public ChatCo() {
        this.allChat = new CCAllChat(this);
        this.spoilerListener = new CCSpoilers(this);
        this.whisperListener = new CCWhispers(this);
        this.checkForChatDisable = false;
        this.checkForIgnores = false;
        this.playerlist = new ArrayList<CCPlayer>();
    }
    
    public void onDisable() {
        this.playerlist.clear();
    }
    
    public void onEnable() {
        this.checkFiles();
        this.readConfig(0);
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents((Listener)this.allChat, (Plugin)this);
        if (this.getConfig().getBoolean("ChatCo.chatDisableEnabled", true)) {
            this.checkForChatDisable = true;
        }
        if (this.getConfig().getBoolean("ChatCo.ignoresEnabled", true)) {
            this.checkForIgnores = true;
        }
        if (this.getConfig().getBoolean("ChatCo.WhisperChangesEnabled", true)) {
            pm.registerEvents((Listener)this.whisperListener, (Plugin)this);
        }
        if (this.getConfig().getBoolean("ChatCo.SpoilersEnabled", false)) {
            pm.registerEvents((Listener)this.spoilerListener, (Plugin)this);
        }
    }
    
    private void readConfig(final int change) {
        if (change == 3) {
            this.getConfig().set("ChatCo.SpoilersEnabled", (Object)true);
        }
        if (change == 4) {
            this.getConfig().set("ChatCo.SpoilersEnabled", (Object)false);
        }
        if (change == 5) {
            this.getConfig().set("ChatCo.WhisperChangesEnabled", (Object)true);
        }
        if (change == 6) {
            this.getConfig().set("ChatCo.WhisperChangesEnabled", (Object)false);
        }
        if (change == 7) {
            this.getConfig().set("ChatCo.NewCommands", (Object)true);
        }
        if (change == 8) {
            this.getConfig().set("ChatCo.NewCommands", (Object)false);
        }
        if (change == 9) {
            this.getConfig().set("ChatCo.WhisperLog", (Object)true);
        }
        if (change == 10) {
            this.getConfig().set("ChatCo.WhisperLog", (Object)false);
        }
        this.saveConfig();
        this.reloadConfig();
    }
    
    private void checkFiles() {
        ChatCo.dataFolder = this.getDataFolder();
        ChatCo.Configuration = new File(ChatCo.dataFolder, "config.yml");
        ChatCo.Configuration2 = new File(ChatCo.dataFolder, "permissionConfig.yml");
        ChatCo.WhisperLog = new File(ChatCo.dataFolder, "whisperlog.txt");
        ChatCo.Help = new File(ChatCo.dataFolder, "help.txt");
        if (!ChatCo.WhisperLog.exists()) {
            ChatCo.WhisperLog.getParentFile().mkdirs();
            this.copy(this.getResource("whisperlog.txt"), ChatCo.WhisperLog);
        }
        if (!ChatCo.Help.exists()) {
            ChatCo.Help.getParentFile().mkdirs();
            this.copy(this.getResource("help.txt"), ChatCo.Help);
        }
        if (!ChatCo.Configuration.exists()) {
            this.saveDefaultConfig();
        }
        if (!ChatCo.Configuration2.exists()) {
            ChatCo.Configuration2.getParentFile().mkdirs();
            this.copy(this.getResource("permissionConfig.yml"), ChatCo.Configuration2);
        }
    }
    
    private void copy(final InputStream in, final File file) {
        try {
            final OutputStream out = new FileOutputStream(file);
            final byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            if (cmd.getName().equalsIgnoreCase("togglechat") && this.getConfig().getBoolean("toggleChatEnabled", true)) {
                try {
                    if (this.toggleChat((Player)sender)) {
                        sender.sendMessage(String.valueOf(ChatColor.RED.toString()) + "Your chat is now disabled until you type /togglechat or relog.");
                    }
                    else {
                        sender.sendMessage(String.valueOf(ChatColor.RED.toString()) + "Your chat has been re-enabled, type /togglechat to disable it again.");
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            if (cmd.getName().equalsIgnoreCase("toggletells")) {
                try {
                    if (this.toggleTells((Player)sender)) {
                        sender.sendMessage(String.valueOf(ChatColor.RED.toString()) + "You will no longer receive tells, type /toggletells to see them again.");
                    }
                    else {
                        sender.sendMessage(String.valueOf(ChatColor.RED.toString()) + "You now receive tells, type /toggletells to disable them again.");
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            if (cmd.getName().equalsIgnoreCase("ignore") && this.getConfig().getBoolean("ignoresEnabled", true)) {
                try {
                    System.out.println("Attempting to ignore player " + args[0]);
                    if (args.length < 1) {
                        sender.sendMessage("You forgot to type the name of the player.");
                        return true;
                    }
                    if (args[0].length() > 16) {
                        sender.sendMessage("You entered an invalid player name.");
                        return true;
                    }
                    final Player ignorable = Bukkit.getServer().getPlayer(args[0]);
                    if (ignorable == null) {
                        sender.sendMessage("You entered a player who doesn't exist or is offline.");
                        return true;
                    }
                    this.ignorePlayer((Player)sender, args[0]);
                    return true;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (cmd.getName().equalsIgnoreCase("ignorelist") && this.getConfig().getBoolean("ignoresEnabled", true)) {
                try {
                    sender.sendMessage(String.valueOf(ChatColor.RED.toString()) + "Ignored players:");
                    int i = 0;
                    for (final String ignores : this.getCCPlayer((Player)sender).getIgnoreList()) {
                        sender.sendMessage(String.valueOf(ChatColor.RED.toString()) + ChatColor.ITALIC + ignores);
                        ++i;
                    }
                    sender.sendMessage(String.valueOf(ChatColor.RED.toString()) + i + " players ignored.");
                    return true;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (args.length > 0) {
            final Player player = null;
            final boolean b = sender instanceof Player;
            if (cmd.getName().equalsIgnoreCase("chatco")) {
                if (args[1] == null && player == null) {
                    sender.sendMessage("You forgot to specify whether you wanted to enable or disable the component (chatco component e/ed)");
                    return true;
                }
                if (player == null && args[0].equalsIgnoreCase("spoilers")) {
                    if (player == null && args[1].equalsIgnoreCase("e")) {
                        this.readConfig(3);
                        sender.sendMessage("Spoilers enabled");
                    }
                    else if (player == null && args[1].equalsIgnoreCase("d")) {
                        this.readConfig(4);
                        sender.sendMessage("Spoilers disabled");
                    }
                }
                if (player == null && args[0].equalsIgnoreCase("whispers")) {
                    if (player == null && args[1].equalsIgnoreCase("e")) {
                        this.readConfig(5);
                        sender.sendMessage("Whisper changes enabled");
                    }
                    else if (player == null && args[1].equalsIgnoreCase("d")) {
                        this.readConfig(6);
                        sender.sendMessage("Whisper changes disabled");
                    }
                }
                if (player == null && args[0].equalsIgnoreCase("newcommands")) {
                    if (player == null && args[1].equalsIgnoreCase("e")) {
                        this.readConfig(7);
                        sender.sendMessage("New Whisper commands enabled");
                    }
                    else if (player == null && args[1].equalsIgnoreCase("d")) {
                        this.readConfig(8);
                        sender.sendMessage("New whisper commands disabled");
                    }
                }
                if (player == null && args[0].equalsIgnoreCase("whisperlog")) {
                    if (player == null && args[1].equalsIgnoreCase("e")) {
                        this.readConfig(9);
                        sender.sendMessage("Whisperlog enabled");
                    }
                    else if (player == null && args[1].equalsIgnoreCase("d")) {
                        this.readConfig(10);
                        sender.sendMessage("Whisperlog disabled");
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    public CCPlayer getCCPlayer(final Player p) throws IOException {
        for (final CCPlayer cp : this.playerlist) {
            if (cp.playerName.equals(p.getName())) {
                return cp;
            }
        }
        final CCPlayer ccp = new CCPlayer(p, p.getName());
        this.playerlist.add(ccp);
        return ccp;
    }
    
    private boolean toggleChat(final Player p) throws IOException {
        if (this.getCCPlayer(p).chatDisabled) {
            return this.getCCPlayer(p).chatDisabled = false;
        }
        return this.getCCPlayer(p).chatDisabled = true;
    }
    
    private boolean toggleTells(final Player p) throws IOException {
        if (this.getCCPlayer(p).tellsDisabled) {
            return this.getCCPlayer(p).tellsDisabled = false;
        }
        return this.getCCPlayer(p).tellsDisabled = true;
    }
    
    private void ignorePlayer(final Player p, final String target) throws IOException {
        String message = String.valueOf(ChatColor.RED.toString()) + ChatColor.ITALIC + target + ChatColor.RESET + ChatColor.RED;
        if (this.getCCPlayer(p).isIgnored(target)) {
            message = String.valueOf(message) + " unignored.";
        }
        else {
            message = String.valueOf(message) + " ignored.";
        }
        p.sendMessage(message);
        this.getCCPlayer(p).saveIgnoreList(target);
    }
}
