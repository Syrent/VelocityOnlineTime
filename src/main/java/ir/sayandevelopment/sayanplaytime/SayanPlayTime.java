package ir.sayandevelopment.sayanplaytime;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import ir.sayandevelopment.sayanplaytime.database.MySQL;
import ir.sayandevelopment.sayanplaytime.database.SQL;
import net.dv8tion.jda.api.JDABuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Plugin(
        id = "sayanplaytime",
        name = "SayanPlayTime",
        version = BuildConstants.VERSION,
        url = "sayandevelopment.ir",
        authors = {"Syrent"}
)
public class SayanPlayTime {

    public static SayanPlayTime INSTANCE;
    public static SQL SQL;
    public static net.dv8tion.jda.api.JDA JDA;
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public SayanPlayTime(ProxyServer server, Logger logger) {
        INSTANCE = this;

        this.server = server;
        this.logger = logger;

        try {
            JDA = JDABuilder.createDefault("ODQ2MDM4NDQwNTIwMDU2OTAy.YKpssg.T4S1xJxc1lGERaZEXTQUxhR8Fcg").build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        String host = "45.81.16.84";
        String database = "server";
        String user = "server";
        String pass = "yG%@NU6wz}i#)ZQN";
        int port = 3306;

        SQL = new MySQL(host, port, database, user, pass);

        try {
            logger.info("Connecting to SQL...");
            SQL.openConnection();
            SQL.createTable();
            logger.info("Connected to SQL.");
        } catch (Exception ex) {
            logger.error("Error while connecting to SQL.");
            ex.printStackTrace();
            return;
        }

        new DiscordManager();

        server.getScheduler()
                .buildTask(this, () -> {
                    getLogger().info("Registering Discord event listener...");
                    JDA.addEventListener(new DiscordManager());
                    getLogger().info("Discord event listener successfully registered.");
                    checkTime();
                })
                .delay(10L, TimeUnit.SECONDS)
                .schedule();

        server.getAllServers().forEach(registeredServer -> {
            try {
                SayanPlayTime.SQL.createColumn(registeredServer.getServerInfo().getName());
            } catch (Exception e) {
                SayanPlayTime.INSTANCE.getLogger().warn("Can not create gamemode columne in database. GameMode: " + registeredServer.getServerInfo().getName());
                SayanPlayTime.INSTANCE.getLogger().warn("Error message:");
                e.printStackTrace();
            }
        });

        server.getEventManager().register(this, new EventListener());
        server.getCommandManager().register("playtime", new PlayTimeCommand());
        CommandMeta meta = server.getCommandManager().metaBuilder("playtime")
                .aliases("onlinetime", "pt", "ot").build();
        server.getCommandManager().register(meta, new PlayTimeCommand());
    }

    private void checkTime() {
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicBoolean staffDone = new AtomicBoolean(false);
        LuckPerms api = LuckPermsProvider.get();
        MiniMessage formatter = MiniMessage.get();

        getServer().getScheduler().buildTask(this, () -> {
            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hours == 0) {
                if (!staffDone.get()) {
                    try {
                        DiscordManager.getInstance().sendDailyMessage();
                        try {
                            SQL.resetDaily();
                            SQL.resetDaily();
                            SQL.resetDaily();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        staffDone.set(true);
                    } catch (Exception ignored) {
                    }
                }

                if (!done.get()) {
                    if (day == 7) {
                        try {
                            String username = SQL.getWeeklyTops(1).get(0).getUserName();
                            User user = api.getUserManager().getUser(username);
                            if (user != null) {
                                user.data().add(Node.builder("group.baron").expiry(7, TimeUnit.DAYS).build());
                                api.getUserManager().saveUser(user);
                            }
                            getLogger().info(String.format("%s won weekly rank!", username));
                            getServer().getAllPlayers().forEach(player -> {
                                player.sendMessage(formatter.deserialize("<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                                        " <gradient:#F2E205:#F2A30F>PlayTime</gradient> " +
                                        "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"));
                                player.sendMessage(formatter.deserialize(String.format(
                                        PlayTimeCommand.PREFIX + "<bold><color:#F2E205>%s Be Onvan Top PlayTime Hafte Barande Rank VIP Shod!", username
                                )));
                            });

                            DiscordManager.getInstance().sendWinnerMessage();

                            try {
                                SQL.resetWeekly();
                                SQL.resetWeekly();
                                SQL.resetWeekly();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            done.set(true);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }).repeat(1, TimeUnit.SECONDS).schedule();
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }
}
