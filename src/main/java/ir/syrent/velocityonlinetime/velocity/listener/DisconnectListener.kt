package ir.syrent.velocityonlinetime.velocity.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import ir.syrent.velocityonlinetime.velocity.VelocityOnlineTime
import ir.syrent.velocityonlinetime.velocity.storage.Database
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.utils.MilliCounter
import java.util.*

class DisconnectListener(
    private val plugin: VelocityOnlineTime
) {
    private var onlinePlayers: MutableMap<UUID, MilliCounter> = HashMap()

    init {
        VRuom.registerListener(this)
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val username = player.username
        val serverConnection = player.currentServer
        if (!serverConnection.isPresent) return
        val serverName = serverConnection.get().serverInfo.name

        if (onlinePlayers.containsKey(uuid)) {
            val milliCounter = onlinePlayers[uuid]
            milliCounter!!
            milliCounter.stop()

            Database.getPlayerOnlineTime(uuid, serverName).whenComplete { currentOnlineTime, _ ->
                val newOnlineTime = milliCounter.get() + currentOnlineTime

                Database.getWeeklyOnlineTime(uuid).whenComplete { currentWeeklyOnlineTime, _ ->
                    val newWeeklyOnlinetime = milliCounter.get() + currentWeeklyOnlineTime

                    Database.updateServerOnlineTime(uuid, username, newOnlineTime, serverName).whenComplete { _, _ ->
                        Database.updateWeeklyOnlineTime(uuid, username, newWeeklyOnlinetime).whenComplete { _, _ ->
                            Database.updateOnlineTime(uuid).whenComplete { _, _ ->
                                if (player.hasPermission("velocityonlinetime.staff.daily")) {
                                    Database.getDailyOnlineTime(uuid).whenComplete { currentDailyOnlineTime, _ ->
                                        var newDailyServerOnlineTime = milliCounter.get() + currentDailyOnlineTime

                                        Database.getDailyOnlineTime(uuid).whenComplete { dailyOnlineTime, _ ->
                                            Database.updateDailyServerOnlineTime(uuid, username, newDailyServerOnlineTime, serverName).whenComplete { _, _ ->
                                                newDailyServerOnlineTime = milliCounter.get() + dailyOnlineTime

                                                Database.updateDailyOnlineTime(uuid, username, newDailyServerOnlineTime).whenComplete { _, _ ->
                                                    Database.updateDailyTotalOnlineTime(uuid)
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
    }
}