package ir.syrent.velocityonlinetime.velocity.bridge

import com.google.common.io.ByteStreams
import com.google.gson.JsonObject
import com.velocitypowered.api.proxy.server.RegisteredServer
import ir.syrent.velocityonlinetime.velocity.VelocityOnlineTime
import ir.syrent.velocityonlinetime.velocity.storage.Database
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.utils.GsonUtils

@Suppress("UnstableApiUsage")
class VelocityBridgeManager(
    private val plugin: VelocityOnlineTime,
    private val bridge: VelocityBridge
) {

    fun sendPlayerOnlineTime(name: String) {
        val messageJson = JsonObject()
        Database.getPlayerOnlineTime(name).whenComplete { onlineTime, _ ->
            messageJson.addProperty("type", "OnlineTime")
            messageJson.addProperty("name", name)
            messageJson.addProperty("onlinetime", onlineTime)
            Database.getPlayerOnlineTime(name).whenComplete { weeklyOnlineTime, _ ->
                messageJson.addProperty("weekly_onlinetime", weeklyOnlineTime)
                sendPluginMessage(messageJson)
            }
        }

    }

    private fun sendPluginMessage(messageJson: JsonObject) {
        val byteArrayInputStream = ByteStreams.newDataOutput()
        byteArrayInputStream.writeUTF(GsonUtils.get().toJson(messageJson))

        bridge.sendPluginMessage(byteArrayInputStream.toByteArray())
    }

    private fun sendPluginMessage(messageJson: JsonObject, server: RegisteredServer) {
        val byteArrayInputStream = ByteStreams.newDataOutput()
        byteArrayInputStream.writeUTF(GsonUtils.get().toJson(messageJson))

        bridge.sendPluginMessage(byteArrayInputStream.toByteArray(), server)
    }

    fun handleMessage(messageJson: JsonObject) {
        when (messageJson["type"].asString) {
            "OnlineTime" -> {
                val name = messageJson["name"].asString
                sendPlayerOnlineTime(name)
            }
            else -> {
                VRuom.warn("Unsupported message type: ${messageJson["type"].asString}")
            }
        }
    }
}