package ir.syrent.velocityonlinetime.spigot.dependency

import ir.syrent.velocityonlinetime.spigot.VelocityOnlineTimeSpigot
import ir.syrent.velocityonlinetime.utils.Utils.format
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class PlaceholderAPI(
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
        return if (params.equals("onlinetime", true)) {
            try {
                return plugin.playersOnlineTime[player.name]?.format() ?: 0L.format()
            } catch (e: Exception) {
                "-"
            }
        } else "-"
    }
}