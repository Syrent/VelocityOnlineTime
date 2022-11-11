package ir.syrent.velocityonlinetime.spigot.command.onlinetime

import ir.syrent.velocityonlinetime.spigot.VelocityOnlineTimeSpigot
import ir.syrent.velocityonlinetime.spigot.command.library.SubCommand
import ir.syrent.velocityonlinetime.spigot.storage.Message
import ir.syrent.velocityonlinetime.spigot.utils.sendMessage
import ir.syrent.velocityonlinetime.utils.TextReplacement
import ir.syrent.velocityonlinetime.utils.Utils.capitalize
import ir.syrent.velocityonlinetime.utils.Utils.format
import org.bukkit.command.CommandSender

class TopSubcommand(
    private val plugin: VelocityOnlineTimeSpigot
) : SubCommand("top", "velocityonlinetime.command.onlinetime.top", false) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            sender.sendMessage(Message.ONLINETIME_GET_USAGE)
            return
        }

        when (args.size) {
            1 -> {
                if (args[0].equals("week", true)) {
                    plugin.
                }
                val target = args[0]

                val onlineTime = plugin.playersOnlineTime[target]?.getTotal() ?: 0
                if (onlineTime < 1) {
                    sender.sendMessage(Message.PLAYER_NOT_FOUND)
                    return
                }

                sender.sendMessage(Message.ONLINETIME_GET_USE, TextReplacement("player", target), TextReplacement("time", onlineTime.format()))
            }
            2 -> {
                val target = args[0]
                val server = args[1]

                val onlineTime = plugin.playersOnlineTime[target]?.getServer(server) ?: 0

                if (onlineTime < 1) {
                    sender.sendMessage(Message.PLAYER_NOT_FOUND_SERVER, TextReplacement("player", target), TextReplacement("server", server.capitalize()))
                    return
                }

                sender.sendMessage(Message.ONLINETIME_GET_SERVER_USE, TextReplacement("player", target), TextReplacement("server", server.capitalize()), TextReplacement("time", onlineTime.format()))
            }
            else -> sender.sendMessage(Message.ONLINETIME_GET_USAGE)
        }
    }
}