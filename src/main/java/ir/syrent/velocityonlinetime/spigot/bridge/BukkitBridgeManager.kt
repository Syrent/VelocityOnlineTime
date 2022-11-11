package ir.syrent.velocityonlinetime.spigot.bridge

import com.google.common.io.ByteStreams
import com.google.gson.JsonObject
import ir.syrent.velocityonlinetime.spigot.VelocityOnlineTimeSpigot
import ir.syrent.velocityonlinetime.spigot.core.OnlineTime
import ir.syrent.velocityonlinetime.spigot.ruom.Ruom
import ir.syrent.velocityonlinetime.velocity.bridge.Bridge
import me.mohamad82.ruom.utils.GsonUtils
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage")
class BukkitBridgeManager(
    val bridge: Bridge,
    private val plugin: VelocityOnlineTimeSpigot
) {

    fun sendPlayerOnlineTimeRequest(sender: Player) {
        val messageJson = JsonObject()
        messageJson.addProperty("type", "OnlineTime")
        messageJson.addProperty("name", sender.name)

        sendPluginMessage(sender, messageJson)
    }

    private fun sendPluginMessage(sender: Player, messageJson: JsonObject) {
        val byteArrayInputStream = ByteStreams.newDataOutput()
        byteArrayInputStream.writeUTF(GsonUtils.get().toJson(messageJson))

        bridge.sendPluginMessage(sender, byteArrayInputStream.toByteArray())
    }

    fun handleMessage(messageJson: JsonObject) {
        when (val type = messageJson["type"].asString) {
            "OnlineTime" -> {
                val name = messageJson["name"].asString
                val serverOnlineTime = messageJson["onlinetime"].asLong
                val serverWeeklyOnlineTime = messageJson["weekly_onlinetime"].asLong
                plugin.playersOnlineTime[name] = OnlineTime(name, serverOnlineTime, serverWeeklyOnlineTime)
            }
            else -> {
                Ruom.warn("Unsupported plugin message received from internal channel: $type")
            }
        }
    }

}