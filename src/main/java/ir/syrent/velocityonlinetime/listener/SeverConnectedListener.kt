package ir.syrent.velocityonlinetime.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import ir.syrent.velocityonlinetime.VelocityOnlineTime
import ir.syrent.velocityonlinetime.utils.MilliCounter
import java.util.*

class SeverConnectedListener(
    private val plugin: VelocityOnlineTime
) {
    private var onlinePlayers: MutableMap<UUID, MilliCounter> = HashMap()

    @Subscribe
    fun onServerConnected(event: ServerConnectedEvent) {
        val player = event.player
        val previousServer = event.previousServer

        if (previousServer.isPresent) {
            val username = player.username
            val gameMode = previousServer.get().serverInfo.name ?: event.server.serverInfo.name
            val uuid = player.uniqueId

            if (onlinePlayers.containsKey(player.uniqueId)) {
                val milliCounter = onlinePlayers[uuid]
                milliCounter!!
                milliCounter.stop()

                val currentOnlineTime = plugin.mySQL.getPlayerOnlineTime(uuid, gameMode).toFloat()
                val newOnlineTime = milliCounter.get() + currentOnlineTime
                val currentWeeklyOnlineTime = plugin.mySQL.getWeeklyOnlineTime(uuid).toFloat()
                val newWeeklyOnlineTime = milliCounter.get() + currentWeeklyOnlineTime

                plugin.mySQL.updateServerOnlineTime(uuid, username, newOnlineTime, gameMode)
                plugin.mySQL.updateWeeklyOnlineTime(uuid, username, newWeeklyOnlineTime)
                plugin.mySQL.updateTotalOnlineTime(uuid)

                if (player.hasPermission("velocityonlinetime.staff.daily")) {
                    val databaseDailyOnlineTime = plugin.mySQL.getDailyOnlineTime(uuid, gameMode).toFloat()
                    var finalDailyOnlineTime = milliCounter.get() + databaseDailyOnlineTime
                    val dailyOnlineTime = plugin.mySQL.getDailyOnlineTime(uuid).toFloat()

                    plugin.mySQL.updateDailyServerOnlineTime(uuid, username, finalDailyOnlineTime, gameMode)
                    finalDailyOnlineTime = milliCounter.get() + dailyOnlineTime
                    plugin.mySQL.updateDailyOnlineTime(uuid, username, finalDailyOnlineTime)
                    plugin.mySQL.updateDailyTotalOnlineTime(uuid)
                }

                onlinePlayers.remove(uuid)
            }
        }

        val milliCounter = MilliCounter()
        milliCounter.start()
        onlinePlayers[player.uniqueId] = milliCounter
    }
}