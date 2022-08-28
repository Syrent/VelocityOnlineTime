package ir.syrent.velocityonlinetime.spigot

import org.bukkit.plugin.java.JavaPlugin
import ir.syrent.velocityonlinetime.database.MySQL
import org.bukkit.Bukkit
import ir.syrent.velocityonlinetime.database.SQL
import ir.syrent.velocityonlinetime.storage.Settings

class SpigotMain : JavaPlugin() {

    var sql: SQL? = null

    override fun onEnable() {
        val host = Settings.host
        val database = Settings.database
        val username = Settings.username
        val password = Settings.password
        val port = Settings.port
        sql = MySQL(
            null,
            host,
            port,
            database,
            username,
            password
        )
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Placeholder(this).register()
        }
    }
}