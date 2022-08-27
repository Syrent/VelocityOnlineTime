package ir.sayandevelopment.sayanplaytime;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import ir.sayandevelopment.sayanplaytime.database.MySQL;
import ir.sayandevelopment.sayanplaytime.database.SQL;
import ir.syrent.sayanskyblock.storage.Settings;
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
//    public DiscordController discordController;
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
//        discordController = new DiscordController(this);

        registerListeners();
        registerCommands();

        Settings.INSTANCE.load();
        Settings.INSTANCE.refresh();
    }

    public void initializeMySQL() {
        // TODO: Read data from yaml file
        String host = "localhost";
        String database = "root";
        String user = "root";
        String pass = "";
        int port = 3306;

        sql = new MySQL(this, host, port, database, user, pass);

//        try {
//            logger.info("Connecting to sql...");
//            sql.openConnection();
//            sql.createTable();
//            logger.info("Connected to sql.");
//        } catch (Exception e) {
//            logger.error("Error while connecting to sql. Please send error to plugin developer: " + e.getMessage());
//            e.printStackTrace();
//        }
    }

    public void registerListeners() {
        server.getEventManager().register(this, new EventListener(this));
    }

    public void registerCommands() {
        /*server.getCommandManager().register("playtime", new PlayTimeCommand(this, discordController));
        CommandMeta meta = server.getCommandManager().metaBuilder("playtime").aliases("onlinetime", "pt", "ot").build();
        server.getCommandManager().register(meta, new PlayTimeCommand(this, discordController));*/
    }
}
