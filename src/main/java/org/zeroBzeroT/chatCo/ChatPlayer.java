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
    public final Player player;
    public final UUID playerUUID;
    public boolean chatDisabled;
    public boolean tellsDisabled;
    public String LastMessenger;
    public String LastReceiver;
    private File IgnoreList;
    private List<String> ignores;
    private List<String> ignoredBy;


    public ChatPlayer(final Player p) throws IOException {
        player = p;
        playerUUID = p.getUniqueId();
        chatDisabled = false;
        tellsDisabled = false;
        LastMessenger = null;
        LastReceiver = null;

        // create the ignore-list
        saveIgnoreList("");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveIgnoreList(final String p) throws IOException {
    File oldIgnores = new File(Main.dataFolder, "/ignorelists/" + this.player.getName() + ".txt");
    this.IgnoreList = new File(Main.dataFolder, "/ignorelists/" + this.playerUUID + ".txt");
    File ignoredByFile = new File(Main.dataFolder, "/ignorelists/" + Bukkit.getPlayer(p).getUniqueId() + "_ignoredByPlayers.txt");

    if (oldIgnores.exists()) {
        oldIgnores.renameTo(this.IgnoreList);
    }

    if (!this.IgnoreList.exists()) {
        this.IgnoreList.getParentFile().mkdir();
        final FileWriter fwo = new FileWriter(this.IgnoreList, true);
        final BufferedWriter bwo = new BufferedWriter(fwo);
        bwo.close();
    }

    if (!p.isEmpty()) {
        if (!this.isIgnored(p)) {
            final FileWriter fwo = new FileWriter(this.IgnoreList, true);
            final BufferedWriter bwo = new BufferedWriter(fwo);
            bwo.write(p);
            bwo.newLine();
            bwo.close();
        } else {
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

    // in diesem ganzen code ist kein einziges kommentar lol
    if (!ignoredByFile.exists()) {
        ignoredByFile.getParentFile().mkdirs();
        ignoredByFile.createNewFile();
    }

    final FileWriter fwoBy = new FileWriter(ignoredByFile, true);
    final BufferedWriter bwoBy = new BufferedWriter(fwoBy);
    bwoBy.write(this.player.getName() + " ignores you");
    bwoBy.newLine();
    bwoBy.close();

    this.updateIgnoreList();
}


    public void unIgnoreAll() throws IOException {
        final FileWriter fwo = new FileWriter(this.IgnoreList, false);
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
        final FileInputStream file = new FileInputStream(this.IgnoreList);
        final InputStreamReader fileReader = new InputStreamReader(file);
        final BufferedReader inIgnores = new BufferedReader(fileReader);
        String data = inIgnores.readLine();
        this.ignores = new ArrayList<>();
    
        while (data != null) {
            this.ignores.add(data);
            data = inIgnores.readLine();
        }
        file.close();
    
        // Ensure ignoredBy is always initialized
        this.ignoredBy = new ArrayList<>();
    
        File ignoredByFile = new File(Main.dataFolder, "/ignorelists/" + this.playerUUID + "_ignoredByPlayers.txt");
    
        if (ignoredByFile.exists()) {
            final FileInputStream ignoredByStream = new FileInputStream(ignoredByFile);
            final InputStreamReader ignoredByReader = new InputStreamReader(ignoredByStream);
            final BufferedReader inIgnoredBy = new BufferedReader(ignoredByReader);
            String ignoredByData = inIgnoredBy.readLine();
    
            while (ignoredByData != null) {
                this.ignoredBy.add(ignoredByData);
                ignoredByData = inIgnoredBy.readLine();
            }
            ignoredByStream.close();
        }
    }


    public boolean isIgnored(final String p) {
    return this.ignores.contains(p);
}

public boolean isIgnoredBy(final String p) {
    return this.ignoredBy.contains(p + " ignores you");
}


    public List<String> getIgnoreList() {
        return this.ignores;
    }
    public List<String> getIgnoredByList() {
        return this.ignoredBy;
    }
}
