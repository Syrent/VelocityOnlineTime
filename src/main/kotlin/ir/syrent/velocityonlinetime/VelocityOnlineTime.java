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

@Plugin(
        id = "velocityonlinetime",
        name = "VelocityOnlineTime",
        version = BuildConstants.VERSION,
        description = "OnlineTime plugin for velocity servers",
        url = "syrent.ir",
        authors = {"Syrent"}
)
public class VelocityOnlineTime {

    public SQL sql;
    public DiscordController discordController;
    public final ProxyServer server;
    public final Logger logger;

    @Inject
    public VelocityOnlineTime(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        initializeMySQL();
        Settings.INSTANCE.load();
        discordController = new DiscordController(this);

        registerListeners();
        registerCommands();

    }

    public void initializeMySQL() {
        // TODO: Read data from yaml file
        String host = Settings.INSTANCE.getHost();
        String database = Settings.INSTANCE.getDatabase();
        String username = Settings.INSTANCE.getUsername();
        String password = Settings.INSTANCE.getPassword();
        int port = Settings.INSTANCE.getPort();

        sql = new MySQL(this, host, port, database, username, password);

        try {
            logger.info("Connecting to sql...");
            sql.openConnection();
            sql.createTable();
            logger.info("Connected to sql.");
        } catch (Exception e) {
            logger.error("Error while connecting to sql. Please send error to plugin developer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void registerListeners() {
        server.getEventManager().register(this, new SeverConnectedListener(this));
        server.getEventManager().register(this, new DisconnectListener(this));
    }

    public void registerCommands() {
        server.getCommandManager().register("onlinetime", new OnlineTimeCommand(this, discordController));
        CommandMeta meta = server.getCommandManager().metaBuilder("onlinetime").aliases("onlinetime", "pt", "ot").build();
        server.getCommandManager().register(meta, new OnlineTimeCommand(this, discordController));
    }
}
