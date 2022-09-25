package ir.syrent.velocityonlinetime.spigot.listener

import com.google.common.io.ByteStreams
import com.google.gson.Gson
import com.google.gson.JsonObject
import ir.syrent.velocityonlinetime.spigot.Ruom
import ir.syrent.velocityonlinetime.spigot.VelocityOnlineTimeSpigot
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(
    private val plugin: VelocityOnlineTimeSpigot
): Listener {

    init {
        Ruom.registerListener(this)
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        val playerData = JsonObject()
        playerData.addProperty("request", "onlinetime")
        playerData.addProperty("username", player.name)
        playerData.addProperty("uuid", player.name)

        val byteArrayDataOutput = ByteStreams.newDataOutput()
        byteArrayDataOutput.writeUTF(Gson().toJson(playerData))
        player.sendPluginMessage(plugin, VelocityOnlineTimeSpigot.VELOCITYONLINETIME_CHANNEL, byteArrayDataOutput.toByteArray())
    }
}