package ir.syrent.velocityonlinetime.spigot.bridge

import ir.syrent.velocityonlinetime.spigot.ruom.Ruom
import ir.syrent.velocityonlinetime.spigot.ruom.messaging.BukkitMessagingChannel
import ir.syrent.velocityonlinetime.velocity.bridge.Bridge
import org.bukkit.entity.Player

class BukkitBridge : Bridge, BukkitMessagingChannel("velocityonlinetime", "main") {

    override fun sendPluginMessage(sender: Any, messageByte: ByteArray) {
        if (sender !is Player) {
            throw IllegalArgumentException("Given object is not a bukkit player")
        }
        sender.sendPluginMessage(Ruom.getPlugin(), "velocityonlinetime:main", messageByte)
    }

    override fun sendPluginMessage(messageByte: ByteArray) {
        throw IllegalStateException("Only proxies can send plugin message without player instances")
    }

}