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
        val serverConnection = event.player.currentServer
        if (!serverConnection.isPresent) return
        val gameMode = serverConnection.get().serverInfo.name
        try {
            if (onlinePlayers.containsKey(uuid)) {
                val milliCounter = onlinePlayers[uuid]
                milliCounter!!.stop()
                val databaseOnlineTime: Float = plugin.sql.getPlayerOnlineTime(uuid, gameMode).toFloat()
                val finalOnlineTime = milliCounter.get() + databaseOnlineTime
                plugin.sql.updateServerOnlineTime(uuid, username, finalOnlineTime, gameMode)
                val weeklyOnlineTime: Float = plugin.sql.getWeeklyOnlineTime(uuid).toFloat()
                val finalWeeklyOnlineTime = milliCounter.get() + weeklyOnlineTime
                plugin.sql.updateWeeklyOnlineTime(uuid, username, finalWeeklyOnlineTime)
                plugin.sql.updateTotalOnlineTime(uuid)
                if (player.hasPermission("velocityonlinetime.staff.daily")) {
                    val databaseDailyOnlineTime: Float = plugin.sql.getDailyOnlineTime(uuid, gameMode).toFloat()
                    var finalDailyOnlineTime = milliCounter.get() + databaseDailyOnlineTime
                    plugin.sql.updateDailyServerOnlineTime(uuid, username, finalDailyOnlineTime, gameMode)
                    val dailyOnlineTime: Float = plugin.sql.getDailyOnlineTime(uuid).toFloat()
                    finalDailyOnlineTime = milliCounter.get() + dailyOnlineTime
                    plugin.sql.updateDailyOnlineTime(uuid, username, finalDailyOnlineTime)
                    plugin.sql.updateDailyTotalOnlineTime(uuid)
                }
                onlinePlayers.remove(uuid)
            }
        } catch (e: Exception) {
            plugin.logger.warn("Can not create player data in database. Player name: $username")
            plugin.logger.warn("Error message:")
            e.printStackTrace()
        }
    }
}