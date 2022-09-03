package ir.syrent.velocityonlinetime.spigot.bridge

import ir.syrent.velocityonlinetime.spigot.VelocityOnlineTimeSpigot
import me.mohamad82.ruom.utils.GsonUtils
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.nio.charset.StandardCharsets

class BukkitBridgeListener(
    private val plugin: VelocityOnlineTimeSpigot
) : PluginMessageListener {

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        val rawMessage = String(message, StandardCharsets.UTF_8).substring(2)

        // Legacy minecraft version still use old Gson#getParser
        val onlinePlayerData = GsonUtils.getParser().parse(rawMessage).asJsonObject

        val playerName = onlinePlayerData["player"].asString
        val onlinetime = onlinePlayerData["onlinetime"].asLong

        plugin.playersOnlineTime[playerName] = onlinetime
    }
}
