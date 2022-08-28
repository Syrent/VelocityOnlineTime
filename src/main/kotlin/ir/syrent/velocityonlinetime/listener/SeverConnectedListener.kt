package ir.syrent.velocityonlinetime.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import ir.syrent.velocityonlinetime.VelocityOnlineTime
import ir.syrent.velocityonlinetime.utils.MilliCounter
import java.lang.Exception
import java.util.*

class SeverConnectedListener(
    private val plugin: VelocityOnlineTime
) {
    private var onlinePlayers: MutableMap<UUID, MilliCounter> = HashMap()

    @Subscribe
    fun onPostLogin(event: ServerConnectedEvent) {
        val player = event.player
        try {
            val registeredServer = event.previousServer
            if (registeredServer.isPresent) {
                val username = player.username
                val gameMode = registeredServer.get().serverInfo.name
                val uuid = player.uniqueId
                if (onlinePlayers.containsKey(player.uniqueId)) {
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
            }
            val milliCounter = MilliCounter()
            milliCounter.start()
            onlinePlayers[player.uniqueId] = milliCounter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}