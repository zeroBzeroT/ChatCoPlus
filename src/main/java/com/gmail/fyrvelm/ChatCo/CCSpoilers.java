package com.gmail.fyrvelm.ChatCo;

import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.Listener;

public class CCSpoilers implements Listener
{
    ChatCo plugin;
    String[] Spoiler;
    
    public CCSpoilers(final ChatCo plugin) {
        this.Spoiler = new String[5];
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final String check = event.getMessage();
        if (check.startsWith("[SPOILER]") && check.endsWith("[/SPOILER]")) {
            for (int i = 4; i > 0; --i) {
                this.Spoiler[i] = this.Spoiler[i - 1];
            }
            final int Length = event.getMessage().length();
            final Player name = event.getPlayer();
            this.Spoiler[0] = "[ " + event.getMessage().substring(9, Length - 10) + " ] by R%)[".replace("R%)", ChatColor.RED.toString()) + name.getDisplayName() + "]";
            event.setMessage(ChatColor.BLACK + "SPOILER");
        }
        else {
            event.setMessage(check);
        }
    }
    
    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final String inputText = event.getMessage();
        if (inputText.startsWith("/show spoiler")) {
            final int length = inputText.length();
            event.setCancelled(true);
            if (length > 14) {
                final char parser = inputText.charAt(14);
                final int numberOfPrints = Character.digit(parser, 15);
                if (numberOfPrints > 5 || numberOfPrints < 1) {
                    player.sendMessage("The server only stores the last 5 spoilers to have been made.");
                }
                else if (numberOfPrints < 5 && numberOfPrints > 1) {
                    for (int i = 0; i < numberOfPrints; ++i) {
                        player.sendMessage("Spoiler [" + (i + 1) + "]: " + this.Spoiler[i]);
                    }
                }
            }
            else {
                player.sendMessage(this.Spoiler[0]);
            }
        }
    }
}
