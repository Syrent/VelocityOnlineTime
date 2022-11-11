package ir.syrent.velocityonlinetime.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.ChannelIdentifier
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import ir.syrent.velocityonlinetime.BuildConstants
import ir.syrent.velocityonlinetime.velocity.command.OnlineTimeCommand
import ir.syrent.velocityonlinetime.velocity.listener.DisconnectListener
import ir.syrent.velocityonlinetime.velocity.listener.PluginMessageListener
import ir.syrent.velocityonlinetime.velocity.listener.SeverConnectedListener
import ir.syrent.velocityonlinetime.velocity.storage.Database
import ir.syrent.velocityonlinetime.velocity.storage.Settings
import me.mohamad82.ruom.VRUoMPlugin
import org.slf4j.Logger
import java.nio.file.Path

/**
 * Velocity version of BungeeOnlineTime (https://github.com/R3fleXi0n/BungeeOnlineTime) with extra features
 * like weekly rewards, per server online time, discord integration, staff monitoring,  and more.
 * You only need to install plugin in Velocity plugins folder,
 * But if you want to use plugin placeholders using PlaceholderAPI you need to also install plugin in back-end server.
 */
@Plugin(
    id = "velocityonlinetime",
    name = "VelocityOnlineTime",
    version = BuildConstants.VERSION,
    description = "OnlineTime plugin for velocity servers",
    url = "syrent.ir",
    authors = ["Syrent"]
)
class VelocityOnlineTime @Inject constructor(
    server: ProxyServer,
    logger: Logger,
    @DataDirectory dataDirectory: Path
) : VRUoMPlugin(server, logger, dataDirectory) {

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        instance = this
        getDataDirectory().toFile().mkdir()
        getServer().channelRegistrar.register(VELOCITYONLINETIME_CHANNEL)
        Settings
        Database
        registerListeners()
        registerCommands()
    }

    /**
     * Plugin starts a MilliCounter $[me.mohamad82.ruom.utils.MilliCounter]  whenever
     * a player joins the server and ends it when the player disconnects or changes the server.
     * Whenever the player disconnect or change the server, the plugin will save his online time (LeaveTime - JoinTime) to database.
     */
    fun registerListeners() {
        SeverConnectedListener(this)
        DisconnectListener(this)
        PluginMessageListener(this)
    }

    fun registerCommands() {
        OnlineTimeCommand(this)
    }

    companion object {
        var instance: VelocityOnlineTime? = null
            private set

        /**
         * Create new minecraft channel identifier
         * The name should be same name that used in [ir.syrent.velocityonlinetime.spigot.VelocityOnlineTimeSpigot]
         */
        val VELOCITYONLINETIME_CHANNEL: ChannelIdentifier =
            MinecraftChannelIdentifier.create("velocityonlinetime", "main")
    }
}