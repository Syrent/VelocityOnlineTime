package ir.syrent.velocityonlinetime.velocity.listener

import com.google.common.io.ByteStreams
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import ir.syrent.velocityonlinetime.velocity.VelocityOnlineTime
import ir.syrent.velocityonlinetime.velocity.storage.Database
import me.mohamad82.ruom.VRUoMPlugin
import me.mohamad82.ruom.VRuom
import java.nio.charset.StandardCharsets

class PluginMessageListener(
    private val plugin: VelocityOnlineTime
) {

    init {
        VRuom.registerListener(this)
    }

    @Subscribe
    private fun onPluginMessage(event: PluginMessageEvent) {
        if (!event.identifier.equals(VelocityOnlineTime.VELOCITYONLINETIME_CHANNEL)) return

        val rawMessage = String(event.data, StandardCharsets.UTF_8)
        val message = JsonParser.parseString(rawMessage.substring(2)).asJsonObject
        val player = message.get("player").asString


        Database.getPlayerOnlineTime(player).whenComplete { time, _ ->
            val playerData = JsonObject()
            playerData.addProperty("player", player)
            playerData.addProperty("onlinetime", time)
            val byteArrayDataOutput = ByteStreams.newDataOutput()
            byteArrayDataOutput.writeUTF(Gson().toJson(playerData))

            VRUoMPlugin.getServer().allPlayers.iterator().next().currentServer.get().sendPluginMessage(
                VelocityOnlineTime.VELOCITYONLINETIME_CHANNEL, byteArrayDataOutput.toByteArray())
        }
    }
}