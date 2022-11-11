package ir.syrent.velocityonlinetime.spigot.command.onlinetime

import ir.syrent.velocityonlinetime.spigot.VelocityOnlineTimeSpigot
import ir.syrent.velocityonlinetime.spigot.command.library.PluginCommand
import ir.syrent.velocityonlinetime.spigot.storage.Message
import ir.syrent.velocityonlinetime.spigot.utils.sendMessage
import ir.syrent.velocityonlinetime.utils.TextReplacement
import ir.syrent.velocityonlinetime.utils.Utils.format
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class OnlineTimeCommand(
    private val plugin: VelocityOnlineTimeSpigot
) : PluginCommand("onlinetime", "velocityonlinetime.command.onlinetime", true) {

    init {
        this.register()
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (sender !is Player) {
            sender.sendMessage(Message.ONLY_PLAYERS)
            return
        }

        val onlineTime = plugin.playersOnlineTime[sender.name]?.getTotal() ?: 0
        sender.sendMessage(Message.ONLINETIME_USE, TextReplacement("time", onlineTime.format()))
    }
}