package ir.syrent.velocityonlinetime.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import ir.syrent.velocityonlinetime.VelocityOnlineTime
import ir.syrent.velocityonlinetime.storage.Database
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.utils.MilliCounter
import java.util.*

class SeverConnectedListener(
    private val plugin: VelocityOnlineTime
) {
    private var onlinePlayers: MutableMap<UUID, MilliCounter> = HashMap()

    init {
        VRuom.registerListener(this)
    }

    @Subscribe
    fun onServerConnected(event: ServerConnectedEvent) {
        val player = event.player
        val previousServer = event.previousServer

        if (previousServer.isPresent) {
            val username = player.username
            val serverName = previousServer.get().serverInfo.name ?: event.server.serverInfo.name
            val uuid = player.uniqueId

            if (onlinePlayers.containsKey(player.uniqueId)) {
                val milliCounter = onlinePlayers[uuid]
                milliCounter!!
                milliCounter.stop()

                Database.getPlayerOnlineTime(uuid, serverName).whenComplete { currentOnlineTime, _ ->
                    val newOnlineTime = milliCounter.get() + currentOnlineTime

                    Database.getWeeklyOnlineTime(uuid).whenComplete { currentWeeklyOnlineTime, _ ->
                        val newWeeklyOnlineTime = milliCounter.get() + currentWeeklyOnlineTime

                        Database.updateServerOnlineTime(uuid, username, newOnlineTime, serverName).whenComplete { _, _ ->
                            Database.updateWeeklyOnlineTime(uuid, username, newWeeklyOnlineTime).whenComplete { _, _ ->
                                Database.updateOnlineTime(uuid).whenComplete { _, _ ->
                                    if (player.hasPermission("velocityonlinetime.staff.daily")) {
                                        Database.getDailyOnlineTime(uuid).whenComplete { databaseDailyOnlineTime, _ ->
                                            var finalDailyOnlineTime = milliCounter.get() + databaseDailyOnlineTime

                                            Database.getDailyOnlineTime(uuid).whenComplete { dailyOnlineTime, _ ->
                                                Database.updateDailyServerOnlineTime(uuid, username, finalDailyOnlineTime, serverName).whenComplete { _, _ ->
                                                    finalDailyOnlineTime = milliCounter.get() + dailyOnlineTime

                                                    Database.updateDailyOnlineTime(uuid, username, finalDailyOnlineTime).whenComplete { _, _ ->
                                                        Database.updateDailyTotalOnlineTime(uuid)
                                                    }
                                                }

                                            }
                                        }
                                    }

                                    onlinePlayers.remove(uuid)
                                }
                            }
                        }
                    }
                }

            }
        }

        val milliCounter = MilliCounter()
        milliCounter.start()
        onlinePlayers[player.uniqueId] = milliCounter
    }
}