package ir.sayandevelopment.sayanplaytime.database

import ir.sayandevelopment.sayanplaytime.SayanPlayTime
import ir.sayandevelopment.sayanplaytime.OnlinePlayer
import java.lang.Exception
import java.sql.Connection
import java.util.*

abstract class SQL(
    private val plugin: SayanPlayTime
) {

    var connection: Connection? = null

    abstract fun openConnection()

    private val isClosed = connection == null || connection!!.isClosed || !(connection!!.isValid(0))

    fun createTable() {
        execute("CREATE TABLE IF NOT EXISTS sayanplaytime_playtime (uuid VARCHAR(64) UNIQUE, name VARCHAR(16), total_time BIGINT);")
        execute("CREATE TABLE IF NOT EXISTS sayanplaytime_weekly (uuid VARCHAR(64) UNIQUE, name VARCHAR(16), time BIGINT);")
        execute("CREATE TABLE IF NOT EXISTS sayanplaytime_daily (uuid VARCHAR(64) UNIQUE, name VARCHAR(16), total_time BIGINT);")
        createServerColumns()
    }

    private fun createServerColumns() {
        plugin.server.allServers.forEach { registeredServer ->
            val serverName = registeredServer.serverInfo.name
            
            try {
                plugin.logger.info("Creating $serverName in the playtime database...")
                execute("ALTER TABLE sayanplaytime_playtime ADD COLUMN IF NOT EXISTS $serverName BIGINT;")
                execute("ALTER TABLE sayanplaytime_daily ADD COLUMN IF NOT EXISTS $serverName BIGINT;")
            } catch (e: Exception) {
                plugin.logger.error("Could not create server column in database. Server: $serverName")
                plugin.logger.error("Error message: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun updateServerPlaytime(uuid: UUID, name: String, time: Float, server: String) {
        execute("INSERT INTO sayanplaytime_playtime (uuid, name, ${server}) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', $server = $time;")
    }

    fun updateDailyServerPlaytime(uuid: UUID, name: String, time: Float, server: String) {
        execute("INSERT INTO sayanplaytime_daily (uuid, name, ${server}) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', $server = $time;")
    }

    fun updateWeeklyPlayTime(uuid: UUID, name: String, time: Float) {
        execute("INSERT INTO sayanplaytime_weekly (uuid, name, time) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', time = $time;")
    }

    fun updateDailyPlayTime(uuid: UUID, name: String, time: Float) {
        execute("INSERT INTO sayanplaytime_daily (uuid, name, total_time) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', total_time = $time;")
    }

    fun updateTotalPlayTime(uuid: UUID) {
        var totalTime: Long = 0

        for (server in plugin.server.allServers.map { it.serverInfo.name }) {
            val result = resultExecute("SELECT * FROM sayanplaytime_playtime WHERE uuid='$uuid'", server)
            totalTime += result
        }

        execute("INSERT INTO sayanplaytime_playtime (uuid) VALUES ('$uuid') ON DUPLICATE KEY UPDATE total_time = $totalTime;")
    }

    fun updateDailyTotalPlayTime(uuid: UUID) {
        var totalTime: Long = 0

        for (serverName in plugin.server.allServers.map { it.serverInfo.name }) {
            val result = resultExecute("SELECT * FROM sayanplaytime_daily WHERE uuid='$uuid'", serverName)
            totalTime += result
        }

        execute("INSERT INTO sayanplaytime_daily (uuid) VALUES ('$uuid') ON DUPLICATE KEY UPDATE total_time = $totalTime;")
    }

    fun getPlayerPlayTime(uuid: UUID, server: String): Long {
        return getServerInfo("SELECT * FROM sayanplaytime_playtime WHERE uuid = '$uuid';", server)
    }

    fun getDailyPlayTime(uuid: UUID, server: String): Long {
        return getServerInfo("SELECT * FROM sayanplaytime_daily WHERE uuid = '$uuid';", server)
    }

    fun getWeeklyPlayTime(uuid: UUID): Long {
        return getServerInfo("SELECT * FROM sayanplaytime_weekly WHERE uuid = '$uuid';", "time")
    }

    fun getDailyPlayTime(uuid: UUID): Long {
        return getServerInfo("SELECT * FROM sayanplaytime_daily WHERE uuid = '$uuid';", "total_time")
    }

    fun getPlayerPlayTime(userName: String, server: String): Long {
        return getServerInfo("SELECT * FROM sayanplaytime_playtime WHERE name = '$userName';", server)
    }

    fun getTopPlayTimes(limit: Int): List<OnlinePlayer> {
        if (isClosed) openConnection()

        val onlinePlayers: MutableList<OnlinePlayer> = ArrayList()
        val statement = connection!!.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM sayanplaytime_playtime ORDER BY total_time DESC LIMIT $limit;")

        while (resultSet.next()) {
            val uuid = resultSet.getString("uuid")
            val name = resultSet.getString("name")
            val time = resultSet.getLong("total_time")

            onlinePlayers.add(OnlinePlayer(UUID.fromString(uuid), name, time))
        }

        resultSet.close()
        statement.close()
        return onlinePlayers
    }

    val dailyPlayTimes: List<OnlinePlayer>
        get() {
            if (isClosed) openConnection()

            val onlinePlayers: MutableList<OnlinePlayer> = ArrayList()
            val statement = connection!!.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM sayanplaytime_daily ORDER BY total_time;")

            while (resultSet.next()) {
                val uuid = resultSet.getString("uuid")
                val name = resultSet.getString("name")
                val time = resultSet.getLong("total_time")

                onlinePlayers.add(OnlinePlayer(UUID.fromString(uuid), name, time))
            }

            resultSet.close()
            statement.close()
            return onlinePlayers
        }

    fun getWeeklyTops(limit: Int): List<OnlinePlayer> {
        if (isClosed) openConnection()

        val onlinePlayers: MutableList<OnlinePlayer> = ArrayList()
        val statement = connection!!.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM sayanplaytime_weekly ORDER BY time DESC LIMIT $limit;")

        while (resultSet.next()) {
            val uuid = resultSet.getString("uuid")
            val name = resultSet.getString("name")
            val time = resultSet.getLong("time")

            onlinePlayers.add(OnlinePlayer(UUID.fromString(uuid), name, time))
        }

        resultSet.close()
        statement.close()
        return onlinePlayers
    }

    private fun getServerInfo(sql: String, server: String): Long {
        if (isClosed) openConnection()

        val statement = connection!!.createStatement()
        val resultSet = statement.executeQuery(sql)
        var time: Long = 0

        if (resultSet.next()) {
            time = resultSet.getLong(server)
        }

        resultSet.close()
        statement.close()
        return time
    }

    fun getTopOnlineTimes(limit: Int): Map<UUID, Long> {
        val playerMap: MutableMap<UUID, Long> = HashMap()
        if (isClosed) openConnection()

        val statement = connection!!.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM sayanplaytime_playtime ORDER BY total_time DESC LIMIT $limit;")

        while (resultSet.next()) {
            val uuid = UUID.fromString(resultSet.getString("uuid"))
            val time = resultSet.getLong("time")
            playerMap[uuid] = time
        }

        resultSet.close()
        statement.close()
        return playerMap
    }

    fun resetWeekly() {
        val sql = "DELETE FROM sayanplaytime_weekly;"
        execute(sql)
    }

    fun resetDaily() {
        execute("DELETE FROM sayanplaytime_daily;")
    }

    private fun execute(sql: String) {
        if (isClosed) openConnection()

        val statement = connection!!.createStatement()
        statement.executeUpdate(sql)
        statement.close()
    }

    private fun resultExecute(sql: String, server: String): Long {
        if (isClosed) openConnection()

        val statement = connection!!.createStatement()
        val resultSet = statement.executeQuery(sql)
        var time: Long = 0

        while (resultSet.next()) {
            time = resultSet.getLong(server)
        }

        statement.close()
        return time
    }
}