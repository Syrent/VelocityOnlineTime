package ir.syrent.velocityonlinetime.spigot.hook

import ir.syrent.velocityonlinetime.spigot.VelocityOnlineTimeSpigot
import ir.syrent.velocityonlinetime.spigot.ruom.Ruom
import ir.syrent.velocityonlinetime.utils.Utils.format
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class PlaceholderAPIHook constructor(plugin: VelocityOnlineTimeSpigot, name: String) : Dependency(name) {

    init {
        if (exists) {
            val expansion = OnlineTimeExpansion(plugin)
            expansion.register()
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Add onlinetime placeholder"
        )
    }

    class OnlineTimeExpansion(
        private val plugin: VelocityOnlineTimeSpigot
    ) : PlaceholderExpansion() {
        override fun getAuthor(): String {
            return Ruom.getPlugin().description.authors.joinToString(", ")
        }

        override fun getIdentifier(): String {
            return Ruom.getPlugin().name.lowercase()
        }

        override fun getVersion(): String {
            return Ruom.getPlugin().description.version
        }

        override fun persist(): Boolean {
            return true // This is required or else PlaceholderAPI will unregister the Expansion on reload
        }

        override fun onRequest(player: OfflinePlayer, params: String): String {
            return if (params.equals("onlinetime", true)) {
                try {
                    return plugin.playersOnlineTime[player.name]?.getTotal()?.format() ?: 0L.format()
                } catch (e: Exception) {
                    "-"
                }
            } else "-"
        }
    }

}