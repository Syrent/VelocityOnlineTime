package ir.syrent.velocityonlinetime

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import ir.syrent.velocityonlinetime.controller.DiscordController
import ir.syrent.velocityonlinetime.utils.Utils
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.*
import java.util.concurrent.CompletableFuture

class OnlineTimeCommand(
    private val plugin: VelocityOnlineTime,
    private val discordController: DiscordController
) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val player = invocation.source() as Player
        val args = invocation.arguments()
        val formatter = MiniMessage.miniMessage()
        if (args.isEmpty()) {
            plugin.server.scheduler.buildTask(plugin) {
                try {
                    val totalTime: Long = plugin.sql.getPlayerOnlineTime(player.uniqueId, "total_time")
                    val seconds = totalTime / 1000
                    val hours = (seconds / 3600).toInt()
                    val minutes = (seconds % 3600 / 60).toInt()

                    player.sendMessage(formatter.deserialize(PREFIX + "<color:#00F3FF>Total onlinetime:</color> <color:#C0D3EF>${hours}h ${minutes}m"))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }.schedule()
        } else {
            if (args[0].equals("get", ignoreCase = true)) {
                if (args.size == 2) {
                    val userName = args[1]
                    plugin.server.scheduler.buildTask(plugin) {
                        try {
                            val totalTime: Long = plugin.sql.getPlayerOnlineTime(userName, "total_time")
                            if (totalTime == 0L) {
                                player.sendMessage(formatter.deserialize("$PREFIX<color:#D72D32>Player not found!"))
                                return@buildTask
                            }
                            val seconds = totalTime / 1000
                            val hours = (seconds / 3600).toInt()
                            val minutes = (seconds % 3600 / 60).toInt()
                            player.sendMessage(formatter.deserialize("$PREFIX<color:#C1D6F1>$userName<color:#00F3FF>'s Total onlinetime:</color> <color:#C0D3EF>${hours}h ${minutes}m"))
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }.schedule()
                }
                if (args.size == 3) {
                    val userName = args[1]
                    plugin.server.scheduler.buildTask(plugin) {
                        try {
                            val totalTime: Long =
                                plugin.sql.getPlayerOnlineTime(userName, args[2].lowercase(Locale.getDefault()))
                            if (totalTime == 0L) {
                                player.sendMessage(formatter.deserialize("$PREFIX<color:#D72D32>Player onlinetime is empty on <color:#C1D6F1>${Utils.capitalize(args[2])}</color>!"))
                                return@buildTask
                            }
                            val seconds = totalTime / 1000
                            val hours = (seconds / 3600).toInt()
                            val minutes = (seconds % 3600 / 60).toInt()
                            player.sendMessage(formatter.deserialize("$PREFIX<color:#C1D6F1>$userName</color><color:#00F3FF>'s onlinetime in <color:#C1D6F1>${Utils.capitalize(args[2])}</color>:</color> <color:#C0D3EF>${hours}h ${minutes}m"))
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }.schedule()
                }
            } else if (args[0].equals("top", ignoreCase = true)) {
                if (args.size == 2) {
                    if (args[1].equals("week", ignoreCase = true) || args[1].equals("weekly", ignoreCase = true)) {
                        try {
                            val onlinePlayers: List<OnlinePlayer> = plugin.sql.getWeeklyTops(5)
                            player.sendMessage(
                                formatter.deserialize(
                                    "<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                                            " <gradient:#F2E205:#F2A30F>OnlineTime</gradient> " +
                                            "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"
                                )
                            )
                            for (i in 0..4) {
                                val seconds = onlinePlayers[i].time / 1000
                                val hours = (seconds / 3600).toInt()
                                val minutes = (seconds % 3600 / 60).toInt()
                                player.sendMessage(formatter.deserialize("<color:#EE9900>[<color:#F9BD03>${i + 1}<color:#EE9900>] <color:#C1D6F1>${onlinePlayers[i].userName}</color><color:#00F3FF> | </color> <color:#C0D3EF>${hours}h ${minutes}m"))
                            }
                        } catch (ignored: Exception) {
                        }
                        return
                    }
                }
                plugin.server.scheduler.buildTask(plugin) {
                    try {
                        val onlinePlayers: List<OnlinePlayer> = plugin.sql.getTopOnlineTimes(5)
                        player.sendMessage(
                            formatter.deserialize(
                                "<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                                        " <gradient:#F2E205:#F2A30F>OnlineTime</gradient> " +
                                        "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"
                            )
                        )
                        for (i in 0..4) {
                            val seconds = onlinePlayers[i].time / 1000
                            val hours = (seconds / 3600).toInt()
                            val minutes = (seconds % 3600 / 60).toInt()
                            player.sendMessage(formatter.deserialize("<color:#EE9900>[<color:#F9BD03>${i + 1}<color:#EE9900>] <color:#C1D6F1>${onlinePlayers[i].userName}</color><color:#00F3FF> | </color> <color:#C0D3EF>${hours}h ${minutes}m"))
                        }
                    } catch (ignored: Exception) {
                    }
                }.schedule()
            } else if (args[0].equals("weekly", ignoreCase = true)) {
                plugin.server.scheduler.buildTask(plugin) {
                    try {
                        player.sendMessage(
                            formatter.deserialize(
                                "<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                                        " <gradient:#F2E205:#F2A30F>OnlineTime</gradient> " +
                                        "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"
                            )
                        )
                        for (i in 0..4) {
                            val seconds: Long = plugin.sql.getWeeklyOnlineTime(player.uniqueId) / 1000
                            val hours = (seconds / 3600).toInt()
                            val minutes = (seconds % 3600 / 60).toInt()
                            player.sendMessage(formatter.deserialize("$PREFIX<color:#C1D6F1>${player.username}<color:#00F3FF>'s Total onlinetime:</color> <color:#C0D3EF>${hours}h ${minutes}m"))
                        }
                    } catch (ignored: Exception) {
                    }
                }.schedule()
            } else if (args[0].equals("debug", ignoreCase = true)) {
                /*if (!player.hasPermission("velocityonlinetime.admin"))
                   return;*/
                if (args.size == 2) {
                    if (args[1].equals("discord", ignoreCase = true)) {
                        try {
                            discordController.sendDailyMessage()
                            try {
                                plugin.sql.resetDaily()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } catch (ignored: Exception) {
                        }
                        //DiscordManager.getInstance().sendWinnerMessage();
                    }
                }
            } else if (args[0].equals("help", ignoreCase = true)) {
                player.sendMessage(
                    formatter.deserialize(
                        "<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                                " <gradient:#F2E205:#F2A30F>OnlineTime</gradient> " +
                                "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"
                    )
                )
                player.sendMessage(formatter.deserialize("<color:#F2E205>/onlinetime"))
                player.sendMessage(formatter.deserialize("<color:#F2E205>/onlinetime weekly"))
                player.sendMessage(formatter.deserialize("<color:#F2E205>/onlinetime <color:#00F3FF><gamemode>"))
                player.sendMessage(formatter.deserialize("<color:#F2E205>/onlinetime get <color:#00F3FF><user>"))
                player.sendMessage(formatter.deserialize("<color:#F2E205>/onlinetime get <color:#00F3FF><user> <gamemode>"))
                player.sendMessage(formatter.deserialize("<color:#F2E205>/onlinetime top"))
                player.sendMessage(formatter.deserialize("<color:#F2E205>/onlinetime top weekly"))
                player.sendMessage(formatter.deserialize("<color:#F2E205>/onlinetime help"))
            } else {
                plugin.server.scheduler.buildTask(plugin) {
                    try {
                        val totalTime: Long = plugin.sql.getPlayerOnlineTime(
                            player.uniqueId, args[0].lowercase(
                                Locale.getDefault()
                            )
                        )
                        if (totalTime == 0L) {
                            player.sendMessage(formatter.deserialize("$PREFIX<color:#D72D32>You don't have any data in <color:#C1D6F1>${Utils.capitalize(args[0])}</color>!"))
                            return@buildTask
                        }
                        val seconds = totalTime / 1000
                        val hours = (seconds / 3600).toInt()
                        val minutes = (seconds % 3600 / 60).toInt()
                        player.sendMessage(formatter.deserialize("$PREFIX<color:#C1D6F1>${Utils.capitalize(args[0])}</color><color:#00F3FF> onlinetime:</color> <color:#C0D3EF>${hours}h ${minutes}m"))
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }.schedule()
            }
        }
    }

    override fun suggest(invocation: SimpleCommand.Invocation): List<String> {
        val list: MutableList<String> = ArrayList()
        val args = invocation.arguments()
        plugin.logger.warn("Arg Length: " + args.size)
        if (args.size <= 1) {
            list.add("help")
            list.add("weekly")
            list.add("get")
            list.add("top")
            list.addAll(plugin.server.allServers.map { it.serverInfo.name })
        } else {
            if (args[0].equals("get", ignoreCase = true)) {
                if (args.size >= 3) {
                    for (server in plugin.server.allServers) {
                        if (args[2].isEmpty()) {
                            list.add(server.serverInfo.name)
                        } else {
                            if (server.serverInfo.name.lowercase(Locale.getDefault()).lowercase(Locale.getDefault())
                                    .startsWith(
                                        args[2]
                                    )
                            ) {
                                list.add(server.serverInfo.name)
                            }
                        }
                    }
                } else {
                    for (player in plugin.server.allPlayers) {
                        if (args[1].isEmpty()) {
                            list.add(player.username)
                        } else {
                            if (player.username.lowercase(Locale.getDefault()).lowercase(Locale.getDefault())
                                    .startsWith(
                                        args[1]
                                    )
                            ) {
                                list.add(player.username)
                            }
                        }
                    }
                }
            } else if (args[0].equals("top", ignoreCase = true)) {
                list.add("weekly")
            }
        }
        return list
    }

    override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<List<String>> {
        val listCompletableFuture = CompletableFuture<List<String>>()
        val list: MutableList<String> = ArrayList()
        val args = invocation.arguments()
        if (args.size <= 1) {
            list.add("help")
            list.add("weekly")
            list.add("get")
            list.add("top")
            list.addAll(plugin.server.allServers.map { it.serverInfo.name })
        } else {
            if (args[0].equals("get", ignoreCase = true)) {
                if (args.size >= 3) {
                    for (server in plugin.server.allServers) {
                        if (args[2].isEmpty()) {
                            list.add(server.serverInfo.name)
                        } else {
                            if (server.serverInfo.name.lowercase(Locale.getDefault()).lowercase(Locale.getDefault())
                                    .startsWith(
                                        args[2]
                                    )
                            ) {
                                list.add(server.serverInfo.name)
                            }
                        }
                    }
                } else {
                    for (player in plugin.server.allPlayers) {
                        if (args[1].isEmpty()) {
                            list.add(player.username)
                        } else {
                            if (player.username.lowercase(Locale.getDefault()).lowercase(Locale.getDefault())
                                    .startsWith(
                                        args[1]
                                    )
                            ) {
                                list.add(player.username)
                            }
                        }
                    }
                }
            } else if (args[0].equals("top", ignoreCase = true)) {
                list.add("weekly")
            }
        }
        listCompletableFuture.complete(list)
        return listCompletableFuture
    }

    companion object {
        const val PREFIX = "<gradient:#F2E205:#F2A30F>OnlineTime</gradient> <color:#555197>| "
    }
}