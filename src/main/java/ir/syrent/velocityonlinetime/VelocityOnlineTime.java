package ir.syrent.velocityonlinetime;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import ir.syrent.velocityonlinetime.controller.DiscordController;
import ir.syrent.velocityonlinetime.database.MySQL;
import ir.syrent.velocityonlinetime.database.SQL;
import ir.syrent.velocityonlinetime.listener.DisconnectListener;
import ir.syrent.velocityonlinetime.listener.SeverConnectedListener;
import ir.syrent.velocityonlinetime.storage.Settings;
import org.slf4j.Logger;

/**
 * Velocity version of BungeeOnlineTime (https://github.com/R3fleXi0n/BungeeOnlineTime) with extra features
 * like weekly rewards, per server online time, discord integration, staff monitoring,  and more.
 *
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
public class VelocityOnlineTime {

    private static VelocityOnlineTime instance;
    public static VelocityOnlineTime getInstance() {
        return instance;
    }

    public SQL mySQL;
    public ProxyServer server;
    public Logger logger;
    public DiscordController discordController;

    @Inject
    public VelocityOnlineTime(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        Settings.INSTANCE.load();

        initializeMySQL();

        discordController = new DiscordController(this);

        registerListeners();
        registerCommands();
    }

    public void initializeMySQL() {
        String host = Settings.INSTANCE.getHost();
        String database = Settings.INSTANCE.getDatabase();
        String username = Settings.INSTANCE.getUsername();
        String password = Settings.INSTANCE.getPassword();
        int port = Settings.INSTANCE.getPort();

        mySQL = new MySQL(host, port, database, username, password);

        logger.info("Connecting to MySQL...");
        mySQL.openConnection();
        mySQL.createTable();
        logger.info("Connected to MySQL.");
    }

    /**
     * Plugin starts a MilliCounter ${@link ir.syrent.velocityonlinetime.utils.MilliCounter}  whenever
     * a player joins the server and ends it when the player disconnects or changes the server.
     * Whenever the player disconnect or change the server, the plugin will save his online time (LeaveTime - JoinTime) to database.
     */
    public void registerListeners() {
        server.getEventManager().register(this, new SeverConnectedListener(this));
        server.getEventManager().register(this, new DisconnectListener(this));
    }

    public void registerCommands() {
        CommandMeta meta = server.getCommandManager().metaBuilder("onlinetime").aliases("onlinetime", "pt", "ot", "playtime").build();
        server.getCommandManager().register(meta, new OnlineTimeCommand(this, discordController));
    }
}
