package ir.syrent.velocityonlinetime.spigot

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class Placeholder(
    private val plugin: VelocityOnlineTimeSpigot
) : PlaceholderExpansion() {

    override fun getAuthor(): String {
        return "Syrent231"
    }

    override fun getIdentifier(): String {
        return "onlinetime"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun persist(): Boolean {
        return true // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    override fun onRequest(player: OfflinePlayer, params: String): String {
        return if (params.equals("onlinetime", ignoreCase = true)) {
            try {
                val totalTime = plugin.sql?.getPlayerOnlineTime(player.uniqueId, "total_time")
                val seconds = totalTime?.div(1000)
                val hours = (seconds?.div(3600))?.toInt()
                val minutes = ((seconds?.rem(3600) ?: 3600) / 60).toInt()
                "${hours}h ${minutes}m"
            } catch (e: Exception) {
                "-"
            }
        } else "-"
    }
}