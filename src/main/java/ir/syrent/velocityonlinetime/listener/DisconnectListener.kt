package ir.syrent.velocityonlinetime.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import ir.syrent.velocityonlinetime.VelocityOnlineTime
import ir.syrent.velocityonlinetime.utils.MilliCounter
import java.util.*

class DisconnectListener(
    private val plugin: VelocityOnlineTime
) {
    private var onlinePlayers: MutableMap<UUID, MilliCounter> = HashMap()

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

            val currentOnlineTime = plugin.mySQL.getPlayerOnlineTime(uuid, serverName).toFloat()
            val newOnlineTime = milliCounter.get() + currentOnlineTime
            val currentWeeklyOnlineTime = plugin.mySQL.getWeeklyOnlineTime(uuid).toFloat()
            val newWeeklyOnlinetime = milliCounter.get() + currentWeeklyOnlineTime

            plugin.mySQL.updateServerOnlineTime(uuid, username, newOnlineTime, serverName)
            plugin.mySQL.updateWeeklyOnlineTime(uuid, username, newWeeklyOnlinetime)
            plugin.mySQL.updateTotalOnlineTime(uuid)

            if (player.hasPermission("velocityonlinetime.staff.daily")) {
                val currentDailyOnlineTime: Float = plugin.mySQL.getDailyOnlineTime(uuid, serverName).toFloat()
                var newDailyServerOnlineTime = milliCounter.get() + currentDailyOnlineTime
                val dailyOnlineTime: Float = plugin.mySQL.getDailyOnlineTime(uuid).toFloat()

                plugin.mySQL.updateDailyServerOnlineTime(uuid, username, newDailyServerOnlineTime, serverName)
                newDailyServerOnlineTime = milliCounter.get() + dailyOnlineTime
                plugin.mySQL.updateDailyOnlineTime(uuid, username, newDailyServerOnlineTime)
                plugin.mySQL.updateDailyTotalOnlineTime(uuid)
            }

            onlinePlayers.remove(uuid)
        }
    }
}