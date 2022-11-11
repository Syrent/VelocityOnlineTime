package ir.syrent.velocityonlinetime.velocity.command

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import ir.syrent.velocityonlinetime.utils.TextReplacement
import ir.syrent.velocityonlinetime.utils.Utils.format
import ir.syrent.velocityonlinetime.utils.Utils.toComponent
import ir.syrent.velocityonlinetime.velocity.VelocityOnlineTime
import ir.syrent.velocityonlinetime.velocity.storage.Database
import ir.syrent.velocityonlinetime.velocity.storage.Message
import ir.syrent.velocityonlinetime.velocity.utils.sendMessage
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.string.StringUtils
import java.util.*
import java.util.concurrent.CompletableFuture

class OnlineTimeCommand(
    private val plugin: VelocityOnlineTime,
) : SimpleCommand {
    init {
        VRuom.registerCommand(
            "onlinetime",
            listOf("onlinetime", "pt", "ot", "playtime"),
            this
        )
    }

    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = invocation.source()
        val args = invocation.arguments()

        if (args.isEmpty()) {
            if (sender !is Player) {
                sender.sendMessage(Message.ONLY_PLAYERS)
                return
            }

            Database.getPlayerOnlineTime(sender.uniqueId).whenComplete { time, _ ->
                sender.sendMessage(Message.ONLINETIME_USE, TextReplacement("time", time.format()))
            }
        } else {
            if (args[0].equals("get", true)) {
                if (args.size == 2) {
                    val userName = args[1]

                    Database.getPlayerOnlineTime(userName).whenComplete { time, _ ->
                        if (time < 1) {
                            sender.sendMessage(Message.PLAYER_NOT_FOUND)
                            return@whenComplete
                        }

                        sender.sendMessage(Message.ONLINETIME_GET_USE, TextReplacement("time", time.format()), TextReplacement("player", userName))
                    }
                }
                if (args.size == 3) {
                    val userName = args[1]

                    Database.getPlayerOnlineTime(userName, args[2].lowercase()).whenComplete { time, _ ->
                        if (time < 1) {
                            sender.sendMessage(Message.PLAYER_NOT_FOUND_SERVER, TextReplacement("server", StringUtils.capitalize(args[2])))
                            return@whenComplete
                        }

                        sender.sendMessage(Message.ONLINETIME_GET_SERVER_USE, TextReplacement("time", time.format()), TextReplacement("player", userName), TextReplacement("server", StringUtils.capitalize(args[2])))
                    }
                }
            } else if (args[0].equals("top", true)) {
                if (args.size == 2) {
                    if (args[1].equals("week", true) || args[1].equals("weekly", ignoreCase = true)) {
                        Database.getWeeklyTops(5).whenComplete { onlinePlayers, _ ->
                            for ((index, onlinePlayer) in onlinePlayers.withIndex()) {
                                sender.sendMessage(Message.ONLINETIME_TOP_WEEK_USE, TextReplacement("position", (index + 1).toString()), TextReplacement("player", onlinePlayer.userName), TextReplacement("time", onlinePlayer.time.format()))
                            }
                        }

                        return
                    }
                }

                Database.getTopOnlineTimes(5).whenComplete { onlinePlayers, _ ->
                    for ((index, onlinePlayer) in onlinePlayers.withIndex()) {
                        sender.sendMessage(Message.ONLINETIME_TOP_USE, TextReplacement("position", (index + 1).toString()), TextReplacement("player", onlinePlayer.userName), TextReplacement("time", onlinePlayer.time.format()))
                    }
                }
            } else if (args[0].equals("week", true) || args[0].equals("weekly",  true)) {
                if (sender !is Player) {
                    sender.sendMessage(Message.PLAYER_NOT_FOUND)
                    return
                }

                Database.getWeeklyOnlineTime(sender.uniqueId).whenComplete { time, _ ->
                    sender.sendMessage(Message.ONLINETIME_WEEK_USE, TextReplacement("time", time.format()))
                }
            } else if (args[0].equals("debug", true)) {
                if (!sender.hasPermission("velocityonlinetime.admin")) return

                if (args.size == 2) {
                    if (args[1].equals("discord", true)) {
                            Database.resetWeekly()
                    }
                }
            } else if (args[0].equals("help", true)) {
                sender.sendMessage("<color:#F2E205>/onlinetime".toComponent())
                sender.sendMessage("<color:#F2E205>/onlinetime weekly".toComponent())
                sender.sendMessage("<color:#F2E205>/onlinetime <color:#00F3FF><server>".toComponent())
                sender.sendMessage("<color:#F2E205>/onlinetime get <color:#00F3FF><user>".toComponent())
                sender.sendMessage("<color:#F2E205>/onlinetime get <color:#00F3FF><user> <server>".toComponent())
                sender.sendMessage("<color:#F2E205>/onlinetime top".toComponent())
                sender.sendMessage("<color:#F2E205>/onlinetime top weekly".toComponent())
                sender.sendMessage("<color:#F2E205>/onlinetime help".toComponent())
            } else {
                if (sender !is Player) {
                    sender.sendMessage(Message.PLAYER_NOT_FOUND)
                    return
                }

                Database.getPlayerOnlineTime(sender.uniqueId, args[0].lowercase()).whenComplete { time, _ ->
                    if (time < 1) {
                        sender.sendMessage(Message.PLAYER_NOT_FOUND_SERVER, TextReplacement("server", StringUtils.capitalize(args[0])))
                        return@whenComplete
                    }

                    sender.sendMessage(Message.ONLINETIME_SERVER_USE, TextReplacement("time", time.format()), TextReplacement("server", StringUtils.capitalize(args[0])))
                }
            }
        }
    }

    override fun suggest(invocation: SimpleCommand.Invocation): List<String> {
        val list = mutableListOf<String>()
        val args = invocation.arguments()

        if (args.size <= 1) {
            list.add("help")
            list.add("weekly")
            list.add("get")
            list.add("top")
            list.addAll(VRuom.getServer().allServers.map { it.serverInfo.name })
        } else {
            if (args[0].equals("get", true)) {
                if (args.size >= 3) {
                    for (server in VRuom.getServer().allServers) {
                        if (args[2].isEmpty()) {
                            list.add(server.serverInfo.name)
                        } else {
                            if (server.serverInfo.name.lowercase(Locale.getDefault()).lowercase(Locale.getDefault()).startsWith(args[2])) {
                                list.add(server.serverInfo.name)
                            }
                        }
                    }
                } else {
                    for (player in VRuom.getServer().allPlayers) {
                        if (args[1].isEmpty()) {
                            list.add(player.username)
                        } else {
                            if (player.username.lowercase(Locale.getDefault()).lowercase(Locale.getDefault()).startsWith(args[1])) {
                                list.add(player.username)
                            }
                        }
                    }
                }
            } else if (args[0].equals("top", true)) {
                list.add("weekly")
            }
        }

        return if (args.isNotEmpty()) list.filter { it.lowercase().startsWith(args.last().lowercase()) }.sorted() else list
    }

    override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<List<String>> {
        val listCompletableFuture = CompletableFuture<List<String>>()
        val list = mutableListOf<String>()
        val args = invocation.arguments()

        if (args.size <= 1) {
            list.add("help")
            list.add("weekly")
            list.add("get")
            list.add("top")
            list.addAll(VRuom.getServer().allServers.map { it.serverInfo.name })
        } else {
            if (args[0].equals("get", true)) {
                if (args.size >= 3) {
                    for (server in VRuom.getServer().allServers) {
                        if (args[2].isEmpty()) {
                            list.add(server.serverInfo.name)
                        } else {
                            if (server.serverInfo.name.lowercase(Locale.getDefault()).lowercase(Locale.getDefault()).startsWith(args[2])) {
                                list.add(server.serverInfo.name)
                            }
                        }
                    }
                } else {
                    for (player in VRuom.getServer().allPlayers) {
                        if (args[1].isEmpty()) {
                            list.add(player.username)
                        } else {
                            if (player.username.lowercase(Locale.getDefault()).lowercase(Locale.getDefault()).startsWith(args[1])) {
                                list.add(player.username)
                            }
                        }
                    }
                }
            } else if (args[0].equals("top", true)) {
                list.add("weekly")
            }
        }

        listCompletableFuture.complete(if (args.isNotEmpty()) list.filter { it.lowercase().startsWith(args.last().lowercase()) }.sorted() else list)
        return listCompletableFuture
    }
}