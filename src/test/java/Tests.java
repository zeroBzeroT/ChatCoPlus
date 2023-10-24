import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

public class Tests {
    @Test
    public void testPlayer() {
        Player player = PowerMockito.mock(Player.class);
        // STUB FOR TESTS
    }

    @Test
    public void testSplit() {
        String legacyMessage = "%LIGHT_PURPLE%To %RECEIVER%: ";

        for (ChatColor color : ChatColor.values()) {
            legacyMessage = legacyMessage.replace("%" + color.name() + "%", color.toString());
        }

        String[] parts;
        TextComponent messagePlayer;
        String targetName = "Olaf";

        legacyMessage = legacyMessage.replace("%SENDER%", "Bianca");
        parts = legacyMessage.split("%RECEIVER%", 2);
    }
}
