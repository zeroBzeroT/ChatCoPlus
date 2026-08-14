package org.zeroBzeroT.chatCo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatPlayer {
    public final String name;
    public final UUID playerUUID;
    public boolean chatDisabled;
    public boolean tellsDisabled;
    public String LastMessenger;
    public String LastReceiver;
    private File IgnoresFile;
    private List<String> ignores;
    private List<String> ignoredBy;


    public ChatPlayer(final Player p) throws IOException {
        name = p.getName();
        playerUUID = p.getUniqueId();
        chatDisabled = false;
        tellsDisabled = false;
        LastMessenger = null;
        LastReceiver = null;
        ignoredBy = new ArrayList<>();
        
        // create the ignore-list
        saveIgnoreList("");
    }
    

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveIgnoreList(final String p) throws IOException {
        File oldIgnores = new File(Main.dataFolder, "/ignorelists/" + this.name + ".txt");
        this.IgnoresFile = new File(Main.dataFolder, "/ignorelists/" + this.playerUUID + ".txt");
        File ignoredByFile = new File(Main.dataFolder, "/ignorelists/" + Bukkit.getPlayer(p).getUniqueId() + "_ignoredByPlayers.txt");
    
        if (oldIgnores.exists()) {
            oldIgnores.renameTo(this.IgnoresFile);
        }
    
        if (!this.IgnoresFile.exists()) {
            this.IgnoresFile.getParentFile().mkdir();
            new FileWriter(this.IgnoresFile, true).close();
        }
    
        if (!p.isEmpty()) {
            if (!this.isIgnored(p)) {
                BufferedWriter bwo = new BufferedWriter(new FileWriter(this.IgnoresFile, true));
                bwo.write(p);
                bwo.newLine();
                bwo.close();
            } else {
                this.ignores.remove(p);
                this.ignores.remove("");
                BufferedWriter bwo = new BufferedWriter(new FileWriter(this.IgnoresFile));
                for (String print : this.ignores) {
                    bwo.write(print);
                    bwo.newLine();
                }
                bwo.close();
            }
        }
    
        // ignoredBy speichern
        if (!ignoredByFile.exists()) {
            ignoredByFile.getParentFile().mkdirs();
            ignoredByFile.createNewFile();
        }
    
        BufferedWriter bwoBy = new BufferedWriter(new FileWriter(ignoredByFile, true));
        bwoBy.write(this.name + " ignores you");
        bwoBy.newLine();
        bwoBy.close();
    
        this.updateIgnoreList();
    }
    

    public void unIgnoreAll() throws IOException {
        final FileWriter fwo = new FileWriter(this.IgnoresFile, false);
        final BufferedWriter bwo = new BufferedWriter(fwo);
        bwo.flush();
        bwo.close();

        this.updateIgnoreList();
    }

    public Player getLastMessenger() {
        if (this.LastMessenger != null) {
            return Bukkit.getPlayerExact(this.LastMessenger);
        }

        return null;
    }

    public void setLastMessenger(final Player sender) {
        this.LastMessenger = sender.getName();
    }

    public Player getLastReceiver() {
        if (this.LastReceiver != null) {
            return Bukkit.getPlayerExact(this.LastReceiver);
        }

        return null;
    }

    public void setLastReceiver(final Player sender) {
        this.LastReceiver = sender.getName();
    }

    private void updateIgnoreList() throws IOException {
        BufferedReader inIgnores = new BufferedReader(new InputStreamReader(new FileInputStream(this.IgnoresFile)));
        this.ignores = new ArrayList<>();
        String data;
        while ((data = inIgnores.readLine()) != null) {
            this.ignores.add(data);
        }
        inIgnores.close();
    
        this.ignoredBy = new ArrayList<>();
        File ignoredByFile = new File(Main.dataFolder, "/ignorelists/" + this.playerUUID + "_ignoredByPlayers.txt");
    
        if (ignoredByFile.exists()) {
            BufferedReader inIgnoredBy = new BufferedReader(new InputStreamReader(new FileInputStream(ignoredByFile)));
            String ignoredByData;
            while ((ignoredByData = inIgnoredBy.readLine()) != null) {
                this.ignoredBy.add(ignoredByData);
            }
            inIgnoredBy.close();
        }
    }
    

    public boolean isIgnored(final String p) {
        return this.ignores.contains(p);
    }

    public List<String> getIgnoresFile() {
        return this.ignores;
    }
    public boolean isIgnoredBy(final String p) {
        return this.ignoredBy.contains(p + " ignores you");
    }
    
    public List<String> getIgnoredByList() {
        return this.ignoredBy;
    }
    
}
