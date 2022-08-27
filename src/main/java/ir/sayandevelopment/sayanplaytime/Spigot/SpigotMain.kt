package ir.sayandevelopment.sayanplaytime.Spigot

import org.bukkit.plugin.java.JavaPlugin
import ir.sayandevelopment.sayanplaytime.Spigot.SpigotMain
import ir.sayandevelopment.sayanplaytime.database.MySQL
import org.bukkit.Bukkit
import ir.sayandevelopment.sayanplaytime.database.SQL

class SpigotMain : JavaPlugin() {
    override fun onEnable() {
        val host = "localhost"
        val database = "server"
        val user = "server"
        val pass = "yG%@NU6wz}i#)ZQN"
        val port = 3306
        SQL = MySQL(null, host, port, database, user, pass)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Placeholder().register()
        }
    }

    companion object {
        var SQL: SQL? = null
    }
}