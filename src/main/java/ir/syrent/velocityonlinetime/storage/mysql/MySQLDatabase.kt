package ir.syrent.velocityonlinetime.storage.mysql

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.velocitypowered.api.scheduler.ScheduledTask
import me.mohamad82.ruom.VRUoMPlugin
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.database.Query
import me.mohamad82.ruom.database.mysql.MySQLCredentials
import me.mohamad82.ruom.database.mysql.MySQLExecutor
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class MySQLDatabase(credentials: MySQLCredentials?, poolingSize: Int) : MySQLExecutor(credentials, poolingSize, THREAD_FACTORY) {

    private var queueTask: ScheduledTask? = null
    override fun connect() {
        super.connect("com.mysql.cj.jdbc.Driver")
        queueTask = startQueue()
    }

    override fun shutdown() {
        queueTask!!.cancel()
        queue.clear()
        hikari.close()
    }

    override fun scheduleShutdown(): CompletableFuture<Void> {
        val completableFuture = CompletableFuture<Void>()
        VRuom.runAsync({
            if (isQueueEmpty) {
                shutdown()
                completableFuture.complete(null)
            }
        }, 0, TimeUnit.SECONDS, 50, TimeUnit.MILLISECONDS)
        return completableFuture
    }

    private fun startQueue(): ScheduledTask {
        val runnable = object : Runnable {
            override fun run() {
                if (poolingUsed >= poolingSize) {
                    tick(this)
                    return
                }
                tick()
                tick(this)
            }
        }
        return VRuom.getServer().scheduler.buildTask(VRUoMPlugin.get(), runnable).schedule()
    }

    override fun executeQuery(query: Query): CompletableFuture<Int> {
        val completableFuture = CompletableFuture<Int>()
        val runnable = Runnable {
            val connection = createConnection()
            try {
                val preparedStatement = query.createPreparedStatement(connection)
                var resultSet: ResultSet? = null
                if (query.statement.startsWith("INSERT") ||
                    query.statement.startsWith("UPDATE") ||
                    query.statement.startsWith("DELETE") ||
                    query.statement.startsWith("CREATE") ||
                    query.statement.startsWith("ALTER")
                ) preparedStatement.executeUpdate() else resultSet = preparedStatement.executeQuery()
                query.completableFuture.complete(resultSet)
                closeConnection(connection)
                completableFuture.complete(Query.StatusCode.FINISHED.code)
            } catch (e: SQLException) {
                VRuom.error("Failed to perform a query in the mysql database. Stacktrace:")
                VRuom.error("Statement: " + query.statement)
                e.printStackTrace()
                query.increaseFailedAttempts()
                if (query.failedAttempts > failAttemptRemoval) {
                    closeConnection(connection)
                    completableFuture.complete(Query.StatusCode.FINISHED.code)
                    VRuom.warn(
                        """
                              This query has been removed from the queue as it exceeded the maximum failures. It's more likely to see some stuff break because of this failure, Please report this bug to the developers.
                              Developer(s) of this plugin: ${VRuom.getPlugin().authors}
                              """.trimIndent()
                    )
                }
                closeConnection(connection)
                completableFuture.complete(Query.StatusCode.FAILED.code)
            }
        }
        threadPool.submit(runnable)
        return completableFuture
    }

    fun tick(runnable: Runnable?) {
        VRuom.runAsync(runnable, 50, TimeUnit.MILLISECONDS)
    }

    private fun createConnection(): Connection? {
        return try {
            hikari.connection
        } catch (e: SQLException) {
            VRuom.error("Failed to establish mysql connection!")
            e.printStackTrace()
            null
        }
    }

    private fun closeConnection(connection: Connection?) {
        try {
            connection!!.close()
        } catch (e: SQLException) {
            VRuom.error("Failed to close a mysql connection!")
            e.printStackTrace()
        }
    }

    override fun onQueryFail(query: Query) {}
    override fun onQueryRemoveDueToFail(query: Query) {}

    companion object {
        private val THREAD_FACTORY = ThreadFactoryBuilder().setNameFormat(VRuom.getPlugin().name.lowercase(Locale.getDefault()) + "-mysql-thread-%d").build()
    }
}
