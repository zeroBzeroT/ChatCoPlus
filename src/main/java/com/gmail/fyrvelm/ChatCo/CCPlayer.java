package com.gmail.fyrvelm.ChatCo;

import java.util.ArrayList;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import org.bukkit.Bukkit;
import java.util.Iterator;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.io.File;
import org.bukkit.entity.Player;

public class CCPlayer
{
    public Player player;
    public String playerName;
    public boolean chatDisabled;
    public boolean tellsDisabled;
    public String LastMessenger;
    public long timeUnset;
    private File IgnoreList;
    private List<String> ignores;
    
    public CCPlayer(final Player p, final String pN) throws IOException {
        this.player = p;
        this.playerName = pN;
        this.chatDisabled = false;
        this.tellsDisabled = false;
        this.LastMessenger = null;
        this.saveIgnoreList("");
    }
    
    public void saveIgnoreList(final String p) throws IOException {
        this.IgnoreList = new File(ChatCo.dataFolder, "/ignorelists/" + this.playerName + ".txt");
        if (!this.IgnoreList.exists()) {
            this.IgnoreList.getParentFile().mkdir();
            final FileWriter fwo = new FileWriter(this.IgnoreList, true);
            final BufferedWriter bwo = new BufferedWriter(fwo);
            bwo.close();
        }
        if (!p.equals("")) {
            if (!this.isIgnored(p)) {
                final FileWriter fwo = new FileWriter(this.IgnoreList, true);
                final BufferedWriter bwo = new BufferedWriter(fwo);
                bwo.write(p);
                bwo.newLine();
                bwo.close();
            }
            else {
                this.ignores.remove(p);
                this.ignores.remove("");
                final FileWriter fwo = new FileWriter(this.IgnoreList);
                final BufferedWriter bwo = new BufferedWriter(fwo);
                for (final String print : this.ignores) {
                    bwo.write(print);
                    bwo.newLine();
                }
                bwo.close();
            }
        }
        this.updateIgnoreList();
    }
    
    public void setLastMessenger(final Player sender) {
        this.LastMessenger = sender.getName();
    }
    
    public Player getLastMessenger() {
        if (this.LastMessenger != null) {
            return Bukkit.getPlayerExact(this.LastMessenger);
        }
        return null;
    }
    
    private void updateIgnoreList() throws IOException {
        final FileInputStream file = new FileInputStream(this.IgnoreList);
        final InputStreamReader fileReader = new InputStreamReader(file);
        final BufferedReader inIgnores = new BufferedReader(fileReader);
        String data = inIgnores.readLine();
        this.ignores = new ArrayList<String>();
        while (data != null) {
            this.ignores.add(data);
            data = inIgnores.readLine();
        }
        file.close();
    }
    
    public boolean isIgnored(final String p) {
        return this.ignores.contains(p);
    }
    
    public List<String> getIgnoreList() {
        return this.ignores;
    }
}
