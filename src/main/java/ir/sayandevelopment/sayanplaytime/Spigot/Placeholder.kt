package ir.sayandevelopment.sayanplaytime.Spigot;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class Placeholder extends PlaceholderExpansion {

    @Override
    public String getAuthor() {
        return "Syrent231";
    }

    @Override
    public String getIdentifier() {
        return "playtime";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("playtime")) {
            try {
                long total_time = SpigotMain.SQL.getPlayerPlayTime(player.getUniqueId(), "total_time");
                long seconds = total_time / 1000;
                int hours = (int) (seconds / 3600);
                int minutes = (int) ((seconds % 3600) / 60);
                return String.format("%sh %sm", hours, minutes);
            } catch (Exception e) {
                return "-";
            }
        }

        return "-"; // Placeholder is unknown by the Expansion
    }
}
