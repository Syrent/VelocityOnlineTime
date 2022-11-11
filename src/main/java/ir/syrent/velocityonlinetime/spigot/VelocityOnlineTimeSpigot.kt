package ir.syrent.velocityonlinetime.spigot

import com.google.gson.JsonObject
import io.papermc.lib.PaperLib
import ir.syrent.velocityonlinetime.spigot.bridge.BukkitBridge
import ir.syrent.velocityonlinetime.spigot.bridge.BukkitBridgeManager
import ir.syrent.velocityonlinetime.spigot.command.onlinetime.OnlineTimeCommand
import ir.syrent.velocityonlinetime.spigot.core.OnlineTime
import ir.syrent.velocityonlinetime.spigot.hook.DependencyManager
import ir.syrent.velocityonlinetime.spigot.listener.PlayerJoinListener
import ir.syrent.velocityonlinetime.spigot.ruom.Ruom
import ir.syrent.velocityonlinetime.spigot.ruom.adventure.AdventureApi
import ir.syrent.velocityonlinetime.spigot.ruom.messaging.BukkitMessagingEvent
import ir.syrent.velocityonlinetime.spigot.storage.Settings
import ir.syrent.velocityonlinetime.spigot.storage.Settings.bstats
import ir.syrent.velocityonlinetime.spigot.storage.Settings.velocitySupport
import ir.syrent.velocityonlinetime.spigot.utils.ServerVersion
import ir.syrent.velocityonlinetime.utils.component
import org.bstats.bukkit.Metrics
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class VelocityOnlineTimeSpigot : JavaPlugin() {

    var bridgeManager: BukkitBridgeManager? = null

    val playersOnlineTime = mutableMapOf<String, OnlineTime>()

    override fun onEnable() {
        instance = this
        dataFolder.mkdir()

        initializeInstances()
        sendFiglet()
        sendWarningMessages()
        initializeCommands()
        initializeListeners()


        if (velocitySupport) {
            initializePluginChannels()
        }

        if (bstats) {
            enableMetrics()
        }
    }

    fun initializeInstances() {
        AdventureApi.initialize()
        Settings
    }

    private fun sendFiglet() {
        sendConsoleMessage("<dark_purple>__      __  _            _ _          ____        _ _         _______ _                ")
        sendConsoleMessage("<dark_purple>\\ \\    / / | |          (_) |        / __ \\      | (_)       |__   __(_)               ")
        sendConsoleMessage("<dark_purple> \\ \\  / /__| | ___   ___ _| |_ _   _| |  | |_ __ | |_ _ __   ___| |   _ _ __ ___   ___ ")
        sendConsoleMessage("<dark_purple>  \\ \\/ / _ \\ |/ _ \\ / __| | __| | | | |  | | '_ \\| | | '_ \\ / _ \\ |  | | '_ ` _ \\ / _ \\")
        sendConsoleMessage("<dark_purple>   \\  /  __/ | (_) | (__| | |_| |_| | |__| | | | | | | | | |  __/ |  | | | | | | |  __/")
        sendConsoleMessage("<dark_purple>    \\/ \\___|_|\\___/ \\___|_|\\__|\\__, |\\____/|_| |_|_|_|_| |_|\\___|_|  |_|_| |_| |_|\\___|")
        sendConsoleMessage("<dark_purple>                                __/ |                                                  ")
        sendConsoleMessage("<dark_purple>                               |___/                                                   v${Ruom.getServer().pluginManager.getPlugin("VelocityVanish")?.description?.version ?: " Unknown"}")
        sendConsoleMessage(" ")
        sendConsoleMessage("<white>Wiki: <blue><u>https://github.com/Syrent/VelocityOnlineTime/wiki</u></blue>")
        sendConsoleMessage(" ")
    }

    private fun sendWarningMessages() {
        if (!ServerVersion.supports(16)) {
            Ruom.warn("Your running your server on a legacy minecraft version (< 16).")
            Ruom.warn("This plugin is not tested on legacy versions, so it may not work properly.")
            Ruom.warn("Please consider updating your server to 1.16.5 or higher.")
        }

        PaperLib.suggestPaper(this)
        DependencyManager
    }

    private fun initializePluginChannels() {
        val bridge = BukkitBridge()
        bridgeManager = BukkitBridgeManager(bridge, this)

        object : BukkitMessagingEvent(bridge) {
            override fun onPluginMessageReceived(player: Player, jsonObject: JsonObject) {
                bridgeManager!!.handleMessage(jsonObject)
            }
        }
    }

    private fun initializeCommands() {
        OnlineTimeCommand(this)
    }

    private fun initializeListeners() {
        PlayerJoinListener(this)
    }

    private fun enableMetrics() {
        val pluginID = 16780
        Metrics(this, pluginID)
    }

    override fun onDisable() {
        Ruom.shutdown()
    }

    private fun sendConsoleMessage(message: String) {
        AdventureApi.get().sender(server.consoleSender).sendMessage(message.component())
    }

    companion object {
        lateinit var instance: VelocityOnlineTimeSpigot
            private set
    }

}