package ir.syrent.velocityonlinetime;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import ir.syrent.velocityonlinetime.controller.DiscordController;
import ir.syrent.velocityonlinetime.listener.DisconnectListener;
import ir.syrent.velocityonlinetime.listener.PluginMessageListener;
import ir.syrent.velocityonlinetime.listener.SeverConnectedListener;
import ir.syrent.velocityonlinetime.storage.Database;
import ir.syrent.velocityonlinetime.storage.Settings;
import me.mohamad82.ruom.VRUoMPlugin;
import org.slf4j.Logger;

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
        authors = {"Syrent"}
)
public class VelocityOnlineTime extends VRUoMPlugin {

    private static VelocityOnlineTime instance;
    public static VelocityOnlineTime getInstance() {
        return instance;
    }

    public DiscordController discordController;

    /**
     * Create new minecraft channel identifier
     * The name should be same name that used in {@link ir.syrent.velocityonlinetime.spigot.VelocityOnlineTimeSpigot}
     */
    public static final ChannelIdentifier VELOCITYONLINETIME_CHANNEL = MinecraftChannelIdentifier.create("velocityonlinetime", "main");

    @Inject
    public VelocityOnlineTime(ProxyServer server, Logger logger) {
        super(server, logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        getServer().getChannelRegistrar().register(VELOCITYONLINETIME_CHANNEL);
        Settings.INSTANCE.load();
        Database.INSTANCE.load();

        discordController = new DiscordController(this);

        registerListeners();
        registerCommands();
    }

    /**
     * Plugin starts a MilliCounter ${@link ir.syrent.velocityonlinetime.utils.MilliCounter}  whenever
     * a player joins the server and ends it when the player disconnects or changes the server.
     * Whenever the player disconnect or change the server, the plugin will save his online time (LeaveTime - JoinTime) to database.
     */
    public void registerListeners() {
        new SeverConnectedListener(this);
        new DisconnectListener(this);
        new PluginMessageListener(this);
    }

    public void registerCommands() {
        new OnlineTimeCommand(this, discordController);
    }
}
