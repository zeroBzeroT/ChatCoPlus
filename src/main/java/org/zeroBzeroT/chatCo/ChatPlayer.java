package org.zeroBzeroT.chatCo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatPlayer {
    public final String name;
    public final UUID playerUUID;
    public boolean chatDisabled;
    public boolean tellsDisabled;
    public String LastMessenger;
    public String LastReceiver;
    private File IgnoresFile;
    private List<String> ignores;

    public ChatPlayer(final Player p) throws IOException {
        name = p.getName();
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
        File oldIgnores = new File(Main.dataFolder, "/ignorelists/" + this.name + ".txt");
        this.IgnoresFile = new File(Main.dataFolder, "/ignorelists/" + this.playerUUID + ".txt");

        if (oldIgnores.exists()) {
            oldIgnores.renameTo(this.IgnoresFile);
        }

        if (!this.IgnoresFile.exists()) {
            this.IgnoresFile.getParentFile().mkdir();
            final FileWriter fwo = new FileWriter(this.IgnoresFile, true);
            final BufferedWriter bwo = new BufferedWriter(fwo);
            bwo.close();
        }

        if (!p.isEmpty()) {
            if (!this.isIgnored(p)) {
                final FileWriter fwo = new FileWriter(this.IgnoresFile, true);
                final BufferedWriter bwo = new BufferedWriter(fwo);
                bwo.write(p);
                bwo.newLine();
                bwo.close();
            } else {
                this.ignores.remove(p);
                this.ignores.remove("");
                final FileWriter fwo = new FileWriter(this.IgnoresFile);
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
        final FileInputStream file = new FileInputStream(this.IgnoresFile);
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

    public List<String> getIgnoresFile() {
        return this.ignores;
    }
}
