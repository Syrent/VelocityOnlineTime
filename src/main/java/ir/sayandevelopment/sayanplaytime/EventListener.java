package ir.sayandevelopment.sayanplaytime;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EventListener {

    Map<UUID, MilliCounter> onlinePlayers = new HashMap<>();

    @Subscribe
    public void onPostLogin(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        try {
            Optional<RegisteredServer> registeredServer = event.getPreviousServer();
            if (registeredServer.isPresent()) {
                String username = player.getUsername();
                String gameMode = registeredServer.get().getServerInfo().getName();
                UUID uuid = player.getUniqueId();

                if (onlinePlayers.containsKey(player.getUniqueId())) {
                    MilliCounter milliCounter = onlinePlayers.get(uuid);
                    milliCounter.stop();

                    float databasePlayTime = SayanPlayTime.SQL.getPlayerPlayTime(uuid, gameMode);
                    float finalPlayTime = milliCounter.get() + databasePlayTime;
                    SayanPlayTime.SQL.updateGameModePlayTime(uuid, username, finalPlayTime, gameMode);
                    float weeklyPlayTime = SayanPlayTime.SQL.getWeeklyPlayTime(uuid);
                    float finalWeeklyPlayTime = milliCounter.get() + weeklyPlayTime;
                    SayanPlayTime.SQL.updateWeeklyPlayTime(uuid, username, finalWeeklyPlayTime);
                    SayanPlayTime.SQL.updateTotalPlayTime(uuid);

                    if (player.hasPermission("sayanplaytime.staff.daily")) {
                        float databaseDailyPlayTime = SayanPlayTime.SQL.getDailyPlayTime(uuid, gameMode);
                        float finalDailyPlayTime = milliCounter.get() + databaseDailyPlayTime;
                        SayanPlayTime.SQL.updateDailyGameModePlayTime(uuid, username, finalDailyPlayTime, gameMode);
                        float dailyPlayTime = SayanPlayTime.SQL.getDailyPlayTime(uuid);
                        finalDailyPlayTime = milliCounter.get() + dailyPlayTime;
                        SayanPlayTime.SQL.updateDailyPlayTime(uuid, username, finalDailyPlayTime);
                        SayanPlayTime.SQL.updateDailyTotalPlayTime(uuid);
                    }

                    onlinePlayers.remove(uuid);
                }
            }
            MilliCounter milliCounter = new MilliCounter();
            milliCounter.start();
            onlinePlayers.put(player.getUniqueId(), milliCounter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String username = player.getUsername();
        String gameMode = event.getPlayer().getCurrentServer().get().getServerInfo().getName();

        try {
            if (onlinePlayers.containsKey(uuid)) {
                MilliCounter milliCounter = onlinePlayers.get(uuid);
                milliCounter.stop();
                float databasePlayTime = SayanPlayTime.SQL.getPlayerPlayTime(uuid, gameMode);
                float finalPlayTime = milliCounter.get() + databasePlayTime;
                SayanPlayTime.SQL.updateGameModePlayTime(uuid, username, finalPlayTime, gameMode);
                float weeklyPlayTime = SayanPlayTime.SQL.getWeeklyPlayTime(uuid);
                float finalWeeklyPlayTime = milliCounter.get() + weeklyPlayTime;
                SayanPlayTime.SQL.updateWeeklyPlayTime(uuid, username, finalWeeklyPlayTime);
                SayanPlayTime.SQL.updateTotalPlayTime(uuid);

                if (player.hasPermission("sayanplaytime.staff.daily")) {
                    float databaseDailyPlayTime = SayanPlayTime.SQL.getDailyPlayTime(uuid, gameMode);
                    float finalDailyPlayTime = milliCounter.get() + databaseDailyPlayTime;
                    SayanPlayTime.SQL.updateDailyGameModePlayTime(uuid, username, finalDailyPlayTime, gameMode);
                    float dailyPlayTime = SayanPlayTime.SQL.getDailyPlayTime(uuid);
                    finalDailyPlayTime = milliCounter.get() + dailyPlayTime;
                    SayanPlayTime.SQL.updateDailyPlayTime(uuid, username, finalDailyPlayTime);
                    SayanPlayTime.SQL.updateDailyTotalPlayTime(uuid);
                }

                onlinePlayers.remove(uuid);
            }
        } catch (Exception e) {
            SayanPlayTime.INSTANCE.getLogger().warn("Can not create player data in database. Player name: " + username);
            SayanPlayTime.INSTANCE.getLogger().warn("Error message:");
            e.printStackTrace();
        }
    }
}
