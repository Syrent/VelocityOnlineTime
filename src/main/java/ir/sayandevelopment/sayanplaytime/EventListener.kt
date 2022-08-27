package ir.sayandevelopment.sayanplaytime

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import java.lang.Exception
import java.util.*

class EventListener(
    private val plugin: SayanPlayTime
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
                    val databasePlayTime: Float = plugin.sql.getPlayerPlayTime(uuid, gameMode).toFloat()
                    val finalPlayTime = milliCounter.get() + databasePlayTime
                    plugin.sql.updateServerPlaytime(uuid, username, finalPlayTime, gameMode)
                    val weeklyPlayTime: Float = plugin.sql.getWeeklyPlayTime(uuid).toFloat()
                    val finalWeeklyPlayTime = milliCounter.get() + weeklyPlayTime
                    plugin.sql.updateWeeklyPlayTime(uuid, username, finalWeeklyPlayTime)
                    plugin.sql.updateTotalPlayTime(uuid)
                    if (player.hasPermission("sayanplaytime.staff.daily")) {
                        val databaseDailyPlayTime: Float = plugin.sql.getDailyPlayTime(uuid, gameMode).toFloat()
                        var finalDailyPlayTime = milliCounter.get() + databaseDailyPlayTime
                        plugin.sql.updateDailyServerPlaytime(uuid, username, finalDailyPlayTime, gameMode)
                        val dailyPlayTime: Float = plugin.sql.getDailyPlayTime(uuid).toFloat()
                        finalDailyPlayTime = milliCounter.get() + dailyPlayTime
                        plugin.sql.updateDailyPlayTime(uuid, username, finalDailyPlayTime)
                        plugin.sql.updateDailyTotalPlayTime(uuid)
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
                val databasePlayTime: Float = plugin.sql.getPlayerPlayTime(uuid, gameMode).toFloat()
                val finalPlayTime = milliCounter.get() + databasePlayTime
                plugin.sql.updateServerPlaytime(uuid, username, finalPlayTime, gameMode)
                val weeklyPlayTime: Float = plugin.sql.getWeeklyPlayTime(uuid).toFloat()
                val finalWeeklyPlayTime = milliCounter.get() + weeklyPlayTime
                plugin.sql.updateWeeklyPlayTime(uuid, username, finalWeeklyPlayTime)
                plugin.sql.updateTotalPlayTime(uuid)
                if (player.hasPermission("sayanplaytime.staff.daily")) {
                    val databaseDailyPlayTime: Float = plugin.sql.getDailyPlayTime(uuid, gameMode).toFloat()
                    var finalDailyPlayTime = milliCounter.get() + databaseDailyPlayTime
                    plugin.sql.updateDailyServerPlaytime(uuid, username, finalDailyPlayTime, gameMode)
                    val dailyPlayTime: Float = plugin.sql.getDailyPlayTime(uuid).toFloat()
                    finalDailyPlayTime = milliCounter.get() + dailyPlayTime
                    plugin.sql.updateDailyPlayTime(uuid, username, finalDailyPlayTime)
                    plugin.sql.updateDailyTotalPlayTime(uuid)
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