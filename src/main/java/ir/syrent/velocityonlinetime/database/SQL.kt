package ir.syrent.velocityonlinetime.database

import ir.syrent.velocityonlinetime.OnlinePlayer
import ir.syrent.velocityonlinetime.VelocityOnlineTime
import java.sql.Connection
import java.util.*

/**
 * Because of similarities in the database queries between MySQL and SQLite, this class is used to abstract the database queries.
 */
abstract class SQL {

    var connection: Connection? = null

    abstract fun openConnection()

    private val isClosed = connection == null || connection!!.isClosed || !(connection!!.isValid(0))

    fun createTable() {
        execute("CREATE TABLE IF NOT EXISTS velocityonlinetime_onlinetime (uuid VARCHAR(64) UNIQUE, name VARCHAR(16), total_time BIGINT);")
        execute("CREATE TABLE IF NOT EXISTS velocityonlinetime_weekly (uuid VARCHAR(64) UNIQUE, name VARCHAR(16), time BIGINT);")
        execute("CREATE TABLE IF NOT EXISTS velocityonlinetime_daily (uuid VARCHAR(64) UNIQUE, name VARCHAR(16), total_time BIGINT);")
        createServerColumns()
    }

    private fun createServerColumns() {
        VelocityOnlineTime.getInstance().server.allServers.forEach { registeredServer ->
            val serverName = registeredServer.serverInfo.name
            
            try {
                VelocityOnlineTime.getInstance().logger.info("Creating $serverName in the onlinetime database...")
                execute("ALTER TABLE velocityonlinetime_onlinetime ADD COLUMN IF NOT EXISTS $serverName BIGINT;")
                execute("ALTER TABLE velocityonlinetime_daily ADD COLUMN IF NOT EXISTS $serverName BIGINT;")
            } catch (e: Exception) {
                VelocityOnlineTime.getInstance().logger.error("Could not create server column in database. Server: $serverName")
                VelocityOnlineTime.getInstance().logger.error("Error message: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun updateServerOnlineTime(uuid: UUID, name: String, time: Float, server: String) {
        execute("INSERT INTO velocityonlinetime_onlinetime (uuid, name, ${server}) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', $server = $time;")
    }

    fun updateDailyServerOnlineTime(uuid: UUID, name: String, time: Float, server: String) {
        execute("INSERT INTO velocityonlinetime_daily (uuid, name, ${server}) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', $server = $time;")
    }

    fun updateWeeklyOnlineTime(uuid: UUID, name: String, time: Float) {
        execute("INSERT INTO velocityonlinetime_weekly (uuid, name, time) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', time = $time;")
    }

    fun updateDailyOnlineTime(uuid: UUID, name: String, time: Float) {
        execute("INSERT INTO velocityonlinetime_daily (uuid, name, total_time) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', total_time = $time;")
    }

    fun updateTotalOnlineTime(uuid: UUID) {
        var totalTime: Long = 0

        for (server in VelocityOnlineTime.getInstance().server.allServers.map { it.serverInfo.name }) {
            val result = resultExecute("SELECT * FROM velocityonlinetime_onlinetime WHERE uuid='$uuid'", server)
            totalTime += result
        }

        execute("INSERT INTO velocityonlinetime_onlinetime (uuid) VALUES ('$uuid') ON DUPLICATE KEY UPDATE total_time = $totalTime;")
    }

    fun updateDailyTotalOnlineTime(uuid: UUID) {
        var totalTime: Long = 0

        for (serverName in VelocityOnlineTime.getInstance().server.allServers.map { it.serverInfo.name }) {
            val result = resultExecute("SELECT * FROM velocityonlinetime_daily WHERE uuid='$uuid'", serverName)
            totalTime += result
        }

        execute("INSERT INTO velocityonlinetime_daily (uuid) VALUES ('$uuid') ON DUPLICATE KEY UPDATE total_time = $totalTime;")
    }

    fun getPlayerOnlineTime(uuid: UUID, server: String): Long {
        return getServerInfo("SELECT * FROM velocityonlinetime_onlinetime WHERE uuid = '$uuid';", server)
    }

    fun getDailyOnlineTime(uuid: UUID, server: String): Long {
        return getServerInfo("SELECT * FROM velocityonlinetime_daily WHERE uuid = '$uuid';", server)
    }

    fun getWeeklyOnlineTime(uuid: UUID): Long {
        return getServerInfo("SELECT * FROM velocityonlinetime_weekly WHERE uuid = '$uuid';", "time")
    }

    fun getDailyOnlineTime(uuid: UUID): Long {
        return getServerInfo("SELECT * FROM velocityonlinetime_daily WHERE uuid = '$uuid';", "total_time")
    }

    fun getPlayerOnlineTime(userName: String, server: String): Long {
        return getServerInfo("SELECT * FROM velocityonlinetime_onlinetime WHERE name = '$userName';", server)
    }

    fun getTopOnlineTimes(limit: Int): List<OnlinePlayer> {
        if (isClosed) openConnection()

        val onlinePlayers: MutableList<OnlinePlayer> = ArrayList()
        val statement = connection!!.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM velocityonlinetime_onlinetime ORDER BY total_time DESC LIMIT $limit;")

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

    val dailyOnlineTimes: List<OnlinePlayer>
        get() {
            if (isClosed) openConnection()

            val onlinePlayers: MutableList<OnlinePlayer> = ArrayList()
            val statement = connection!!.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM velocityonlinetime_daily ORDER BY total_time;")

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
        val resultSet = statement.executeQuery("SELECT * FROM velocityonlinetime_weekly ORDER BY time DESC LIMIT $limit;")

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

    fun resetWeekly() {
        val sql = "DELETE FROM velocityonlinetime_weekly;"
        execute(sql)
    }

    fun resetDaily() {
        execute("DELETE FROM velocityonlinetime_daily;")
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