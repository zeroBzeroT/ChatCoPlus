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

    public Player getLastMessenger() {
        if (this.LastMessenger != null) {
            return Bukkit.getPlayerExact(this.LastMessenger);
        }

        return null;
    }

    public void setLastMessenger(final Player sender) {
        this.LastMessenger = sender.getName();
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
