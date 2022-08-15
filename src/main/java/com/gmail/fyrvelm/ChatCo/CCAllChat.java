package com.gmail.fyrvelm.ChatCo;

import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public class CCAllChat implements Listener
{
    public ChatCo plugin;
    private FileConfiguration permissionConfig;
    private File customConfig;
    private static String CANCEL;
    
    static {
        CCAllChat.CANCEL = "14CANCELIMMEDIATELY14712381230412A42088";
    }
    
    public CCAllChat(final ChatCo plugin) {
        this.plugin = plugin;
    }
    
    public String ColorManager(String msg, final Player player) {
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Green") || player.hasPermission("ChatCo.ChatPrefixes.Green")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Green").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Green"))) {
            msg = ChatColor.GREEN + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Blue") || player.hasPermission("ChatCo.ChatPrefixes.Blue")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Blue").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Blue"))) {
            msg = ChatColor.BLUE + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Red") || player.hasPermission("ChatCo.ChatPrefixes.Red")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Red").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Red"))) {
            msg = ChatColor.RED + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Aqua") || player.hasPermission("ChatCo.ChatPrefixes.Aqua")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Aqua").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Aqua"))) {
            msg = ChatColor.AQUA + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Gold") || player.hasPermission("ChatCo.ChatPrefixes.Gold")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Gold").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Gold"))) {
            msg = ChatColor.GOLD + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Yellow") || player.hasPermission("ChatCo.ChatPrefixes.Yellow")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Yellow").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Yellow"))) {
            msg = ChatColor.YELLOW + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Gray") || player.hasPermission("ChatCo.ChatPrefixes.Gray")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Gray").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Gray"))) {
            msg = ChatColor.GRAY + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Black") || player.hasPermission("ChatCo.ChatPrefixes.Black")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Black").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Black"))) {
            msg = ChatColor.BLACK + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Dark_Green") || player.hasPermission("ChatCo.ChatPrefixes.Dark_Green")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Green").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Green"))) {
            msg = ChatColor.DARK_GREEN + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Dark_Red") || player.hasPermission("ChatCo.ChatPrefixes.Dark_Red")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Red").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Red"))) {
            msg = ChatColor.DARK_RED + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Dark_Gray") || player.hasPermission("ChatCo.ChatPrefixes.Dark_Gray")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Gray").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Gray"))) {
            msg = ChatColor.DARK_GRAY + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Dark_Blue") || player.hasPermission("ChatCo.ChatPrefixes.Dark_Blue")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Blue").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Blue"))) {
            msg = ChatColor.DARK_BLUE + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Dark_Aqua") || player.hasPermission("ChatCo.ChatPrefixes.Dark_Aqua")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Aqua").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Aqua"))) {
            msg = ChatColor.DARK_AQUA + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Dark_Purple") || player.hasPermission("ChatCo.ChatPrefixes.Dark_Purple")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Purple").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Dark_Purple"))) {
            msg = ChatColor.DARK_PURPLE + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Light_Purple") || player.hasPermission("ChatCo.ChatPrefixes.Light_Purple")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Light_Purple").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Light_Purple"))) {
            msg = ChatColor.LIGHT_PURPLE + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Underline") || player.hasPermission("ChatCo.ChatPrefixes.Underline")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Underline").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Underline"))) {
            msg = ChatColor.UNDERLINE + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Italic") || player.hasPermission("ChatCo.ChatPrefixes.Italic")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Italic").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Italic"))) {
            msg = ChatColor.ITALIC + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Bold") || player.hasPermission("ChatCo.ChatPrefixes.Bold")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Bold").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Bold"))) {
            msg = ChatColor.BOLD + msg;
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ChatPrefixes.Strikethrough") || player.hasPermission("ChatCo.ChatPrefixes.Strikethrough")) && !this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Strikethrough").equals("!#") && msg.startsWith(this.plugin.getConfig().getString("ChatCo.ChatPrefixes.Strikethrough"))) {
            msg = ChatColor.STRIKETHROUGH + msg;
        }
        return msg;
    }
    
    public String ColorCodeManager(String data, final Player player) {
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.White") || player.hasPermission("ChatCo.ColorCodes.White")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.White").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.White"), ChatColor.WHITE.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Red") || player.hasPermission("ChatCo.ColorCodes.Red")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Red").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Red"), ChatColor.RED.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Black") || player.hasPermission("ChatCo.ColorCodes.Black")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Black").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Black"), ChatColor.BLACK.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Dark_Red") || player.hasPermission("ChatCo.ColorCodes.Dark_Red")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Red").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Red"), ChatColor.DARK_RED.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Dark_Gray") || player.hasPermission("ChatCo.ColorCodes.Dark_Gray")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Gray").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Gray"), ChatColor.DARK_GRAY.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Dark_Blue") || player.hasPermission("ChatCo.ColorCodes.Dark_Blue")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Blue").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Blue"), ChatColor.DARK_BLUE.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Dark_Purple") || player.hasPermission("ChatCo.ColorCodes.Dark_Purple")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Purple").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Purple"), ChatColor.DARK_PURPLE.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Blue") || player.hasPermission("ChatCo.ColorCodes.Blue")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Blue").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Blue"), ChatColor.BLUE.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Light_Purple") || player.hasPermission("ChatCo.ColorCodes.Light_Purple")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Light_Purple").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Light_Purple"), ChatColor.LIGHT_PURPLE.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Dark_Green") || player.hasPermission("ChatCo.ColorCodes.Dark_Green")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Green").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Green"), ChatColor.DARK_GREEN.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Gold") || player.hasPermission("ChatCo.ColorCodes.Gold")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Gold").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Gold"), ChatColor.GOLD.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Green") || player.hasPermission("ChatCo.ColorCodes.Green")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Green").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Green"), ChatColor.GREEN.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Yellow") || player.hasPermission("ChatCo.ColorCodes.Yellow")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Yellow").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Yellow"), ChatColor.YELLOW.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Dark_Aqua") || player.hasPermission("ChatCo.ColorCodes.Dark_Aqua")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Aqua").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Dark_Aqua"), ChatColor.DARK_AQUA.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Aqua") || player.hasPermission("ChatCo.ColorCodes.Aqua")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Aqua").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Aqua"), ChatColor.AQUA.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Gray") || player.hasPermission("ChatCo.ColorCodes.Gray")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Gray").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Gray"), ChatColor.GRAY.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Bold") || player.hasPermission("ChatCo.ColorCodes.Bold")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Bold").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Bold"), ChatColor.BOLD.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Italic") || player.hasPermission("ChatCo.ColorCodes.Italic")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Italic").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Italic"), ChatColor.ITALIC.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Underline") || player.hasPermission("ChatCo.ColorCodes.Underline")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Underline").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Underline"), ChatColor.UNDERLINE.toString());
        }
        if ((this.permissionConfig.getBoolean("ChatCo.ColorCodes.Strikethrough") || player.hasPermission("ChatCo.ColorCodes.Strikethrough")) && !this.plugin.getConfig().getString("ChatCo.ChatColors.Strikethrough").equals("!#")) {
            data = data.replace(this.plugin.getConfig().getString("ChatCo.ChatColors.Strikethrough"), ChatColor.STRIKETHROUGH.toString());
        }
        if (data.length() == 2 && (data.contains(ChatColor.WHITE.toString()) || data.contains(ChatColor.RED.toString()) || data.contains(ChatColor.BLACK.toString()) || data.contains(ChatColor.DARK_RED.toString()) || data.contains(ChatColor.DARK_GRAY.toString()) || data.contains(ChatColor.DARK_BLUE.toString()) || data.contains(ChatColor.DARK_PURPLE.toString()) || data.contains(ChatColor.BLUE.toString()) || data.contains(ChatColor.LIGHT_PURPLE.toString()) || data.contains(ChatColor.DARK_GREEN.toString()) || data.contains(ChatColor.GOLD.toString()) || data.contains(ChatColor.GREEN.toString()) || data.contains(ChatColor.YELLOW.toString()) || data.contains(ChatColor.DARK_AQUA.toString()) || data.contains(ChatColor.AQUA.toString()) || data.contains(ChatColor.GRAY.toString()) || data.contains(ChatColor.BOLD.toString()) || data.contains(ChatColor.ITALIC.toString()) || data.contains(ChatColor.UNDERLINE.toString()) || data.contains(ChatColor.STRIKETHROUGH.toString()))) {
            return CCAllChat.CANCEL;
        }
        return data;
    }
    
    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) throws IOException {
        if (event.isCancelled()) {
            return;
        }
        this.customConfig = ChatCo.Configuration2;
        this.permissionConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(this.customConfig);
        final Player player = event.getPlayer();
        final Player[] recipients = event.getRecipients().toArray(new Player[0]);
        CCPlayer cp = null;
        for (int i = 0; i < recipients.length; ++i) {
            try {
                cp = this.plugin.getCCPlayer(recipients[i]);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            if ((cp.chatDisabled && this.plugin.checkForChatDisable) || (cp.isIgnored(player.getName()) && this.plugin.checkForIgnores)) {
                event.getRecipients().remove(recipients[i]);
            }
        }
        String msg = this.ColorManager(event.getMessage(), player);
        msg = this.ColorCodeManager(msg, player);
        if (msg == CCAllChat.CANCEL) {
            event.setCancelled(true);
        }
        else {
            event.setMessage(msg);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        this.plugin.playerlist.remove(e.getPlayer());
    }
    
    @EventHandler
    public void onPlayerKick(final PlayerKickEvent e) {
        this.plugin.playerlist.remove(e.getPlayer());
    }
}
