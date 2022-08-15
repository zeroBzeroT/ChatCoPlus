package com.gmail.fyrvelm.chatco;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CCPlayer {
    public final Player player;
    public final String playerName;
    public boolean chatDisabled;
    public boolean tellsDisabled;
    public String LastMessenger;
    public String LastReceiver;
    private File IgnoreList;
    private List<String> ignores;

    public CCPlayer(final Player p, final String pN) throws IOException {
        player = p;
        playerName = pN;
        chatDisabled = false;
        tellsDisabled = false;
        LastMessenger = null;
        LastReceiver = null;
        saveIgnoreList(""); // "" means only create the file
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
    }

    public boolean isIgnored(final String p) {
        return this.ignores.contains(p);
    }

    public List<String> getIgnoreList() {
        return this.ignores;
    }
}
