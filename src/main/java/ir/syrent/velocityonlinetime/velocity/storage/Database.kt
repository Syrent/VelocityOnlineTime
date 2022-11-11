package ir.syrent.velocityonlinetime.velocity.storage

import ir.syrent.velocityonlinetime.velocity.OnlinePlayer
import ir.syrent.velocityonlinetime.velocity.storage.mysql.MySQLDatabase
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.configuration.ConfigurateYamlConfig
import me.mohamad82.ruom.database.Priority
import me.mohamad82.ruom.database.Query
import me.mohamad82.ruom.database.mysql.MySQLCredentials
import java.util.*
import java.util.concurrent.CompletableFuture

object Database {

    private lateinit var database: me.mohamad82.ruom.database.Database

    init {
        load()
    }

    fun load() {
        val storageYaml = ConfigurateYamlConfig("storage.yml")
        storageYaml.create()
        storageYaml.load()
        val storageRoot = storageYaml.root!!

        val mySQLRoot = storageRoot.node("mysql")
        val credentials = MySQLCredentials.mySQLCredentials(
            mySQLRoot.node("address").string,
            mySQLRoot.node("port").int,
            mySQLRoot.node("database").string,
            mySQLRoot.node("ssl").boolean,
            mySQLRoot.node("username").string,
            mySQLRoot.node("password").string
        )

        database = MySQLDatabase(credentials, mySQLRoot.node("pooling_size").int)

        try {
            database.connect()
            VRuom.log("Connected to database.")
        } catch (_: Exception) {
            VRuom.warn("Can't connect to database.")
            VRuom.warn("Please make sure to set your database information correctly in storage.yml file.")
        }


        createdMySQLTable()
    }

    private fun createdMySQLTable() {
        database.queueQuery(Query.query("CREATE TABLE IF NOT EXISTS velocityonlinetime (UUID VARCHAR(64) UNIQUE, name VARCHAR(16), time BIGINT);"), Priority.HIGHEST)
        database.queueQuery(Query.query("CREATE TABLE IF NOT EXISTS velocityonlinetime_weekly (UUID VARCHAR(64) UNIQUE, name VARCHAR(16), time BIGINT);"), Priority.HIGHEST)
        database.queueQuery(Query.query("CREATE TABLE IF NOT EXISTS velocityonlinetime_daily (UUID VARCHAR(64) UNIQUE, name VARCHAR(16), time BIGINT);"), Priority.HIGHEST)
    }

    /*private fun createSQLiteTable() {
        var serverValues = ""
        VRuom.getServer().allServers.forEach { registeredServer ->
            val serverName = registeredServer.serverInfo.name.lowercase()
            serverValues += "$serverName BIGINT, "
        }
        serverValues = serverValues.substring(0, serverValues.length - 2)

        database.queueQuery(Query.query("CREATE TABLE IF NOT EXISTS velocityonlinetime (UUID VARCHAR(64) UNIQUE, name VARCHAR(16), time BIGINT, $serverValues);"), Priority.HIGHEST)
        database.queueQuery(Query.query("CREATE TABLE IF NOT EXISTS velocityonlinetime_weekly (UUID VARCHAR(64) UNIQUE, name VARCHAR(16), time BIGINT, $serverValues );"), Priority.HIGHEST)
        database.queueQuery(Query.query("CREATE TABLE IF NOT EXISTS velocityonlinetime_daily (UUID VARCHAR(64) UNIQUE, name VARCHAR(16), time BIGINT, $serverValues );"), Priority.HIGHEST)
    }*/

    private fun createColumns() {
        createdMySQLTable()
        VRuom.getServer().allServers.forEach { registeredServer ->
            val serverName = registeredServer.serverInfo.name.lowercase()

            database.queueQuery(Query.query("ALTER TABLE velocityonlinetime ADD COLUMN IF NOT EXISTS $serverName BIGINT;"), Priority.HIGH)
            database.queueQuery(Query.query("ALTER TABLE velocityonlinetime_daily ADD COLUMN IF NOT EXISTS $serverName BIGINT;"), Priority.HIGH)
        }
    }

    fun updateServerOnlineTime(uuid: UUID, name: String, time: Float, serverName: String): CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()
        database.queueQuery(Query.query("INSERT INTO velocityonlinetime (UUID, name, ${serverName}) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', $serverName = $time;"))
            .completableFuture.whenComplete { _, _ ->
                completableFuture.complete(true)
            }
        return completableFuture
    }

    fun updateDailyServerOnlineTime(uuid: UUID, name: String, time: Float, serverName: String): CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()
        database.queueQuery(Query.query("INSERT INTO velocityonlinetime_daily (UUID, name, ${serverName}) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', $serverName = $time;"))
            .completableFuture.whenComplete { _, _ ->
                completableFuture.complete(true)
            }
        return completableFuture
    }

    fun updateWeeklyOnlineTime(uuid: UUID, name: String, time: Float): CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()
        database.queueQuery(Query.query("INSERT INTO velocityonlinetime_weekly (UUID, name, time) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', time = $time;"))
            .completableFuture.whenComplete { _, _ ->
                completableFuture.complete(true)
            }
        return completableFuture
    }

    fun updateDailyOnlineTime(uuid: UUID, name: String, time: Float): CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()
        database.queueQuery(Query.query("INSERT INTO velocityonlinetime_daily (UUID, name, time) VALUES ('$uuid','$name','$time') ON DUPLICATE KEY UPDATE name = '$name', time = $time;"))
            .completableFuture.whenComplete { _, _ ->
                completableFuture.complete(true)
            }
        return completableFuture
    }

    fun updateOnlineTime(uuid: UUID): CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()
        val timeCompletableFuture = CompletableFuture<Long>()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime WHERE UUID='$uuid'"))
            .completableFuture.whenComplete { result, _ ->
                if (result.next()) {
                    var time: Long = 0
                    for (serverName in VRuom.getServer().allServers.map { it.serverInfo.name }) {
                        time += result.getLong(serverName)
                    }
                    timeCompletableFuture.complete(time)
                }
            }


        timeCompletableFuture.whenComplete { result, _ ->
            database.queueQuery(Query.query("INSERT INTO velocityonlinetime (UUID) VALUES ('$uuid') ON DUPLICATE KEY UPDATE time = $result;"))
                .completableFuture.whenComplete { _, _ ->
                    completableFuture.complete(true)
                }
        }

        return completableFuture
    }

    fun updateDailyTotalOnlineTime(uuid: UUID): CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()
        val timeCompletableFuture = CompletableFuture<Long>()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime_daily WHERE UUID='$uuid'"))
            .completableFuture.whenComplete { result, _ ->
                if (result.next()) {
                    var time: Long = 0
                    for (serverName in VRuom.getServer().allServers.map { it.serverInfo.name }) {
                        time += result.getLong(serverName)
                    }
                    timeCompletableFuture.complete(time)
                }
        }

        timeCompletableFuture.whenComplete { result, _ ->
            database.queueQuery(Query.query("INSERT INTO velocityonlinetime_daily (UUID) VALUES ('$uuid') ON DUPLICATE KEY UPDATE time = $result;"))
                .completableFuture.whenComplete { _, _ ->
                    completableFuture.complete(true)
                }
        }

        return completableFuture
    }

    fun getPlayerOnlineTime(uuid: UUID, serverName: String): CompletableFuture<Long> {
        val completableFuture = CompletableFuture<Long>()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime WHERE UUID='$uuid'"))
            .completableFuture.whenComplete { result, _ ->
                var time: Long = 0
                while (result.next()) {
                    time += result.getLong(serverName)
                }
                completableFuture.complete(time)
            }
        return completableFuture
    }

    fun getPlayerOnlineTime(uuid: UUID): CompletableFuture<Long> {
        val completableFuture = CompletableFuture<Long>()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime WHERE UUID='$uuid'"))
            .completableFuture.whenComplete { result, _ ->
                var time: Long = 0
                while (result.next()) {
                    time += result.getLong("time")
                }
                completableFuture.complete(time)
            }
        return completableFuture
    }

    fun getDailyOnlineTime(uuid: UUID, serverName: String): CompletableFuture<Long> {
        val completableFuture = CompletableFuture<Long>()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime_daily WHERE UUID='$uuid'"))
            .completableFuture.whenComplete { result, _ ->
                var time: Long = 0
                while (result.next()) {
                    time += result.getLong(serverName)
                }
                completableFuture.complete(time)
            }
        return completableFuture
    }

    fun getWeeklyOnlineTime(uuid: UUID): CompletableFuture<Long> {
        val completableFuture = CompletableFuture<Long>()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime_weekly WHERE UUID='$uuid'"))
            .completableFuture.whenComplete { result, _ ->
                var time: Long = 0
                while (result.next()) {
                    time += result.getLong("time")
                }
                completableFuture.complete(time)
            }

        return completableFuture
    }

    fun getDailyOnlineTime(uuid: UUID): CompletableFuture<Long> {
        val completableFuture = CompletableFuture<Long>()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime_daily WHERE UUID='$uuid'"))
            .completableFuture.whenComplete { result, _ ->
                var time: Long = 0
                while (result.next()) {
                    time += result.getLong("time")
                }
                completableFuture.complete(time)
            }
        return completableFuture
    }

    fun getPlayerOnlineTime(userName: String): CompletableFuture<Long> {
        val completableFuture = CompletableFuture<Long>()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime WHERE name='$userName'"))
            .completableFuture.whenComplete { result, _ ->
                var time: Long = 0
                while (result.next()) {
                    time += result.getLong("time")
                }
                completableFuture.complete(time)
            }

        return completableFuture
    }

    fun getPlayerOnlineTime(userName: String, serverName: String): CompletableFuture<Long> {
        val completableFuture = CompletableFuture<Long>()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime WHERE name='$userName'"))
            .completableFuture.whenComplete { result, _ ->
                var time: Long = 0
                while (result.next()) {
                    time += result.getLong(serverName)
                }
                completableFuture.complete(time)
            }

        return completableFuture
    }

    fun getTopOnlineTimes(limit: Int): CompletableFuture<List<OnlinePlayer>> {
        val completableFuture = CompletableFuture<List<OnlinePlayer>>()
        val onlinePlayers: MutableList<OnlinePlayer> = ArrayList()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime ORDER BY time DESC LIMIT $limit;"))
            .completableFuture.whenComplete { result, _ ->
                while (result.next()) {
                    val uuid = result.getString("UUID")
                    val time = result.getLong("time")
                    val name = result.getString("name")
                    onlinePlayers.add(OnlinePlayer(UUID.fromString(uuid), name, time))
                }
                completableFuture.complete(onlinePlayers)
            }

        return completableFuture
    }

    val dailyOnlineTimes: CompletableFuture<List<OnlinePlayer>>
        get() {
            val completableFuture = CompletableFuture<List<OnlinePlayer>>()
            val onlinePlayers: MutableList<OnlinePlayer> = ArrayList()

            database.queueQuery(Query.query("SELECT * FROM velocityonlinetime_daily ORDER BY time;"))
                .completableFuture.whenComplete { result, _ ->
                    while (result.next()) {
                        val uuid = result.getString("UUID")
                        val time = result.getLong("time")
                        val name = result.getString("name")
                        onlinePlayers.add(OnlinePlayer(UUID.fromString(uuid), name, time))
                    }
                    completableFuture.complete(onlinePlayers)
                }

            return completableFuture
        }

    fun getWeeklyTops(limit: Int): CompletableFuture<List<OnlinePlayer>> {
        val completableFuture = CompletableFuture<List<OnlinePlayer>>()
        val onlinePlayers: MutableList<OnlinePlayer> = ArrayList()

        database.queueQuery(Query.query("SELECT * FROM velocityonlinetime_weekly ORDER BY time DESC LIMIT $limit;"))
            .completableFuture.whenComplete { result, _ ->
                while (result.next()) {
                    val uuid = result.getString("UUID")
                    val time = result.getLong("time")
                    val name = result.getString("name")
                    onlinePlayers.add(OnlinePlayer(UUID.fromString(uuid), name, time))
                }
                completableFuture.complete(onlinePlayers)
            }

        return completableFuture
    }

    fun getWeeklyTop(): CompletableFuture<OnlinePlayer> {
        val completableFuture = CompletableFuture<OnlinePlayer>()

        getWeeklyTops(1).whenComplete { onlinePlayers, _ ->
            completableFuture.complete(onlinePlayers[0])
        }

        return completableFuture
    }

    fun resetWeekly() {
        database.queueQuery(Query.query("DELETE FROM velocityonlinetime_weekly;"))
    }

    fun resetDaily() {
        database.queueQuery(Query.query("DELETE FROM velocityonlinetime_daily;"))
    }

    fun shutdown() {
        database.shutdown()
    }
}