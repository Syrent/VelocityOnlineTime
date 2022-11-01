package ir.syrent.velocityonlinetime.command

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import ir.syrent.velocityonlinetime.VelocityOnlineTime
import ir.syrent.velocityonlinetime.controller.DiscordController
import ir.syrent.velocityonlinetime.storage.Database
import ir.syrent.velocityonlinetime.storage.Message
import ir.syrent.velocityonlinetime.storage.Settings
import ir.syrent.velocityonlinetime.utils.TextReplacement
import ir.syrent.velocityonlinetime.utils.Utils.format
import ir.syrent.velocityonlinetime.utils.Utils.toComponent
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.string.StringUtils
import java.util.*
import java.util.concurrent.CompletableFuture

class OnlineTimeCommand(
    private val plugin: VelocityOnlineTime,
    private val discord: DiscordController
) : SimpleCommand {
    init {
        VRuom.registerCommand(
            "onlinetime",
            listOf("onlinetime", "pt", "ot", "playtime"),
            this
        )
    }

    override fun execute(invocation: SimpleCommand.Invocation) {
        val player = invocation.source() as Player
        val args = invocation.arguments()

        if (args.isEmpty()) {
            Database.getPlayerOnlineTime(player.uniqueId).whenComplete { time, _ ->
                player.sendMessage(Settings.formatMessage(Message.ONLINETIME_USE, TextReplacement("time", time.format())).toComponent())
            }
        } else {
            if (args[0].equals("get", true)) {
                if (args.size == 2) {
                    val userName = args[1]

                    Database.getPlayerOnlineTime(userName).whenComplete { time, _ ->
                        if (time < 1) {
                            player.sendMessage(Settings.formatMessage(Message.PLAYER_NOT_FOUND).toComponent())
                            return@whenComplete
                        }

                        player.sendMessage(Settings.formatMessage(Message.ONLINETIME_GET_USE, TextReplacement("time", time.format()), TextReplacement("player", userName)).toComponent())
                    }
                }
                if (args.size == 3) {
                    val userName = args[1]

                    Database.getPlayerOnlineTime(userName, args[2].lowercase()).whenComplete { time, _ ->
                        if (time < 1) {
                            player.sendMessage(Settings.formatMessage(Message.PLAYER_NOT_FOUND_SERVER, TextReplacement("server", StringUtils.capitalize(args[2]))).toComponent())
                            return@whenComplete
                        }

                        player.sendMessage(Settings.formatMessage(Message.ONLINETIME_GET_SERVER_USE, TextReplacement("time", time.format()), TextReplacement("player", userName), TextReplacement("server", StringUtils.capitalize(args[2]))).toComponent())
                    }
                }
            } else if (args[0].equals("top", true)) {
                if (args.size == 2) {
                    if (args[1].equals("week", true) || args[1].equals("weekly", ignoreCase = true)) {
                        Database.getWeeklyTops(5).whenComplete { onlinePlayers, _ ->
                            player.sendMessage(Settings.formatMessage(Message.HEADER).toComponent())

                            for ((index, onlinePlayer) in onlinePlayers.withIndex()) {
                                player.sendMessage(Settings.formatMessage(Message.ONLINETIME_TOP_WEEK_USE, TextReplacement("position", (index + 1).toString()), TextReplacement("player", onlinePlayer.userName), TextReplacement("time", onlinePlayer.time.format())).toComponent())
                            }
                        }

                        return
                    }
                }

                Database.getTopOnlineTimes(5).whenComplete { onlinePlayers, _ ->
                    player.sendMessage(Settings.formatMessage(Message.HEADER).toComponent())

                    for ((index, onlinePlayer) in onlinePlayers.withIndex()) {
                        player.sendMessage(Settings.formatMessage(Message.ONLINETIME_TOP_USE, TextReplacement("position", (index + 1).toString()), TextReplacement("player", onlinePlayer.userName), TextReplacement("time", onlinePlayer.time.format())).toComponent())
                    }
                }
            } else if (args[0].equals("week", true) || args[0].equals("weekly",  true)) {
                Database.getWeeklyOnlineTime(player.uniqueId).whenComplete { time, _ ->
                    player.sendMessage(Settings.formatMessage(Message.HEADER).toComponent())
                    player.sendMessage(Settings.formatMessage(Message.ONLINETIME_WEEK_USE, TextReplacement("time", time.format())).toComponent())
                }
            } else if (args[0].equals("debug", true)) {
                if (!player.hasPermission("velocityonlinetime.admin")) return

                if (args.size == 2) {
                    if (args[1].equals("discord", true)) {
                            discord.sendDailyMessage()
                            Database.resetWeekly()
                    }
                }
            } else if (args[0].equals("help", true)) {
                player.sendMessage(Settings.formatMessage(Message.HEADER).toComponent())
                player.sendMessage("<color:#F2E205>/onlinetime".toComponent())
                player.sendMessage("<color:#F2E205>/onlinetime weekly".toComponent())
                player.sendMessage("<color:#F2E205>/onlinetime <color:#00F3FF><server>".toComponent())
                player.sendMessage("<color:#F2E205>/onlinetime get <color:#00F3FF><user>".toComponent())
                player.sendMessage("<color:#F2E205>/onlinetime get <color:#00F3FF><user> <server>".toComponent())
                player.sendMessage("<color:#F2E205>/onlinetime top".toComponent())
                player.sendMessage("<color:#F2E205>/onlinetime top weekly".toComponent())
                player.sendMessage("<color:#F2E205>/onlinetime help".toComponent())
            } else {
                Database.getPlayerOnlineTime(player.uniqueId, args[0].lowercase()).whenComplete { time, _ ->
                    if (time < 1) {
                        player.sendMessage(Settings.formatMessage(Message.PLAYER_NOT_FOUND_SERVER, TextReplacement("server", StringUtils.capitalize(args[0]))).toComponent())
                        return@whenComplete
                    }

                    player.sendMessage(Settings.formatMessage(Message.ONLINETIME_SERVER_USE, TextReplacement("time", time.format()), TextReplacement("server", StringUtils.capitalize(args[0]))).toComponent())
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