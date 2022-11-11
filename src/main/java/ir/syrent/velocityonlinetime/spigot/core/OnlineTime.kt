package ir.syrent.velocityonlinetime.spigot.core

import ir.syrent.velocityonlinetime.spigot.storage.Settings

data class OnlineTime(
    private val player: String,
    private var server: HashMap<String, Long>,
    private var serverWeekly: HashMap<String, Long>,
) {

    fun set(server: HashMap<String, Long>, serverWeekly: HashMap<String, Long>) {
        this.server = server
        this.serverWeekly = serverWeekly
    }

    fun update(servers: Map<String, Long>, weekly: Boolean) {
        for (server in servers) {
            if (weekly) {
                this.serverWeekly[server.key] = server.value
            } else {
                this.server[server.key] = server.value
            }
        }
    }

    fun getTotal(): Long {
        return this.server["total"] ?: 0
    }

    fun getServer(server: String): Long {
        return if (Settings.velocitySupport) this.server[server] ?: 0 else getTotal()
    }
}