package ir.syrent.velocityonlinetime.spigot

import ir.syrent.velocityonlinetime.spigot.bridge.BukkitBridgeListener
import ir.syrent.velocityonlinetime.spigot.dependency.PlaceholderAPI
import ir.syrent.velocityonlinetime.spigot.listener.PlayerJoinListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class VelocityOnlineTimeSpigot : JavaPlugin() {

    val playersOnlineTime = mutableMapOf<String, Long>()

    override fun onEnable() {
        this.server.messenger.registerOutgoingPluginChannel(this, VELOCITYONLINETIME_CHANNEL)
        server.messenger.registerIncomingPluginChannel(this, VELOCITYONLINETIME_CHANNEL, BukkitBridgeListener(this))

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPI(this).register()
        }
    }

    override fun onDisable() {
        server.messenger.unregisterOutgoingPluginChannel(this)
        server.messenger.unregisterIncomingPluginChannel(this)
    }

    private fun registerListeners() {
        PlayerJoinListener(this)
    }

    companion object {
        lateinit var instance: VelocityOnlineTimeSpigot
            private set
        /**
         * The name should be same name that used in Velocity main class
         * @see ir.syrent.velocityonlinetime.VelocityOnlineTime
         */
        const val VELOCITYONLINETIME_CHANNEL = "velocityonlinetime:main"
    }
}