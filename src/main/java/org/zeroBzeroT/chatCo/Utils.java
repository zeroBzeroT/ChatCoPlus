package org.zeroBzeroT.chatCo;

import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

    /**
     * Determine if a player is in vanish mode. Works with most Vanish-Plugins.
     *
     * @param player - the player.
     * @return TRUE if the player is invisible, FALSE otherwise.
     */
    public static boolean isVanished(Player player) {
        if (player != null && player.hasMetadata("vanished") && !player.getMetadata("vanished").isEmpty()) {
            return player.getMetadata("vanished").getFirst().asBoolean();
        }

        return false;
    }

    /**
     * Saves a stream to a file
     */
    public static void saveStreamToFile(final InputStream stream, final File file) {
        try {
            final OutputStream out = Files.newOutputStream(file.toPath());
            final byte[] buf = new byte[1024];
            int len;

            while ((len = stream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * returns a formatted Date/Time string
     */
    public static String now() {
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }
}
