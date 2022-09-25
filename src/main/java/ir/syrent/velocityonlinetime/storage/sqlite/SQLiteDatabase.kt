package ir.syrent.velocityonlinetime.storage.sqlite

import com.velocitypowered.api.scheduler.ScheduledTask
import ir.syrent.velocityonlinetime.VelocityOnlineTime
import ir.syrent.velocityonlinetime.storage.sqlite.exception.SQLiteException
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.database.Query
import me.mohamad82.ruom.database.sqlite.SQLiteExecutor
import java.io.File
import java.sql.SQLException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

open class SQLiteDatabase(dbFile: File?) : SQLiteExecutor(dbFile, Logger.getLogger(VRuom.getPlugin().name)) {

    private var queueTask: ScheduledTask? = null
    override fun connect() {
        super.connect()
        queueTask = startQueue()
    }

    override fun shutdown() {
        try {
            connection.close()
            queue.clear()
            queueTask!!.cancel()
        } catch (e: SQLException) {
            throw SQLiteException(e.message)
        }
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
                tick()
                tick(this)
            }
        }
        return VRuom.getServer().scheduler.buildTask(VelocityOnlineTime.getInstance(), runnable).schedule()
    }

    private fun tick(runnable: Runnable) {
        VRuom.runAsync(runnable, 50, TimeUnit.MILLISECONDS)
    }

    override fun onQueryFail(query: Query) {
        VRuom.error("Failed to perform a query in the sqlite database. Stacktrace:")
        VRuom.debug("Statement: " + query.statement)
    }

    override fun onQueryRemoveDueToFail(query: Query) {
        VRuom.warn(
            """
    This query has been removed from the sqlite queue as it exceeded the maximum failures. It's more likely to see some stuff break because of this failure, Please report this bug to the developers.
    Developer(s) of this plugin: ${VRuom.getPlugin().authors}
    """.trimIndent()
        )
    }
}