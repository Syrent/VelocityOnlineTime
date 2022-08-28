package ir.sayandevelopment.sayanplaytime;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import ir.sayandevelopment.sayanplaytime.controller.DiscordController;
import ir.sayandevelopment.sayanplaytime.database.MySQL;
import ir.sayandevelopment.sayanplaytime.database.SQL;
import ir.sayandevelopment.sayanplaytime.listener.DisconnectListener;
import ir.sayandevelopment.sayanplaytime.listener.SeverConnectedListener;
import ir.sayandevelopment.sayanplaytime.storage.Settings;
import org.slf4j.Logger;

@Plugin(
        id = "sayanplaytime",
        name = "SayanPlayTime",
        version = BuildConstants.VERSION,
        description = "Playtime plugin for velocity servers",
        url = "sayandevelopment.ir",
        authors = {"Syrent"}
)
public class SayanPlayTime {

    public SQL sql;
    public DiscordController discordController;
    public final ProxyServer server;
    public final Logger logger;

    @Inject
    public SayanPlayTime(ProxyServer server, Logger logger) {
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
        server.getCommandManager().register("playtime", new PlayTimeCommand(this, discordController));
        CommandMeta meta = server.getCommandManager().metaBuilder("playtime").aliases("onlinetime", "pt", "ot").build();
        server.getCommandManager().register(meta, new PlayTimeCommand(this, discordController));
    }
}
