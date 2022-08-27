package ir.sayandevelopment.sayanplaytime.Spigot

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import java.lang.Exception

class Placeholder : PlaceholderExpansion() {
    override fun getAuthor(): String {
        return "Syrent231"
    }

    override fun getIdentifier(): String {
        return "playtime"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun persist(): Boolean {
        return true // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    override fun onRequest(player: OfflinePlayer, params: String): String? {
        return if (params.equals("playtime", ignoreCase = true)) {
            try {
                val total_time = SpigotMain.SQL.getPlayerPlayTime(player.uniqueId, "total_time")
                val seconds = total_time / 1000
                val hours = (seconds / 3600).toInt()
                val minutes = (seconds % 3600 / 60).toInt()
                "${hours}h ${minutes}m"
            } catch (e: Exception) {
                "-"
            }
        } else "-"
        // Placeholder is unknown by the Expansion
    }
}