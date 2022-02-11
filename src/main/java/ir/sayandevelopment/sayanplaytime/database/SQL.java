package ir.sayandevelopment.sayanplaytime.database;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import ir.sayandevelopment.sayanplaytime.PPlayer;
import ir.sayandevelopment.sayanplaytime.SayanPlayTime;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public abstract class SQL {

    private Connection connection;

    public abstract void openConnection() throws Exception;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isClosed() throws SQLException {
        return connection == null || connection.isClosed() || !connection.isValid(0);
    }

    public void createTable() throws Exception {
        String sql;
        sql = "CREATE TABLE IF NOT EXISTS sayanplaytime_playtime (uuid VARCHAR(64) UNIQUE, name VARCHAR(16), total_time BIGINT);";
        execute(sql);
        sql = "CREATE TABLE IF NOT EXISTS sayanplaytime_weekly (uuid VARCHAR(64) UNIQUE, name VARCHAR(16), time BIGINT);";
        execute(sql);
        sql = "CREATE TABLE IF NOT EXISTS sayanplaytime_daily (uuid VARCHAR(64) UNIQUE, name VARCHAR(16), total_time BIGINT);";
        execute(sql);
    }

    public void createColumn(String gameMode) throws Exception {
        SayanPlayTime.INSTANCE.getLogger().info("Creating " + gameMode.toLowerCase() + " in the playtime database...");
        String sql = String.format(
                "ALTER TABLE sayanplaytime_playtime ADD COLUMN IF NOT EXISTS %s BIGINT;", gameMode.toLowerCase()
        );
        execute(sql);
        sql = String.format(
                "ALTER TABLE sayanplaytime_daily ADD COLUMN IF NOT EXISTS %s BIGINT;", gameMode.toLowerCase()
        );
        execute(sql);
    }

    public void updateGameModePlayTime(UUID uuid, String name, float time, String gameMode) throws Exception {
        String sql = String.format("INSERT INTO sayanplaytime_playtime (uuid, name, %s) VALUES ('%s','%s','%s') ON DUPLICATE KEY UPDATE name = '%s', %s = %s;", gameMode.toLowerCase(), uuid, name, time, name, gameMode.toLowerCase(), time);
        execute(sql);
    }

    public void updateDailyGameModePlayTime(UUID uuid, String name, float time, String gameMode) throws Exception {
        String sql = String.format("INSERT INTO sayanplaytime_daily (uuid, name, %s) VALUES ('%s','%s','%s') ON DUPLICATE KEY UPDATE name = '%s', %s = %s;", gameMode.toLowerCase(), uuid, name, time, name, gameMode.toLowerCase(), time);
        execute(sql);
    }

    public void updateWeeklyPlayTime(UUID uuid, String name, float time) throws Exception {
        String sql = String.format("INSERT INTO sayanplaytime_weekly (uuid, name, time) VALUES ('%s','%s','%s') ON DUPLICATE KEY UPDATE name = '%s', time = %s;", uuid, name, time, name, time);
        execute(sql);
    }


    public void updateDailyPlayTime(UUID uuid, String name, float time) throws Exception {
        String sql = String.format("INSERT INTO sayanplaytime_daily (uuid, name, total_time) VALUES ('%s','%s','%s') ON DUPLICATE KEY UPDATE name = '%s', total_time = %s;", uuid, name, time, name, time);
        execute(sql);
    }

    public void updateTotalPlayTime(UUID uuid) throws Exception {
        float total_time = 0;
        String sql;

        for (RegisteredServer server : SayanPlayTime.INSTANCE.getServer().getAllServers()) {
            String gameMode = server.getServerInfo().getName().toLowerCase();
            sql = String.format("SELECT * FROM sayanplaytime_playtime WHERE uuid='%s'", uuid.toString());
            float result = resultExecute(sql, gameMode);
            total_time = total_time + result;
        }

        sql = String.format("INSERT INTO sayanplaytime_playtime (uuid) VALUES ('%s') ON DUPLICATE KEY UPDATE total_time = %s;", uuid.toString(), total_time);
        execute(sql);
    }

    public void updateDailyTotalPlayTime(UUID uuid) throws Exception {
        float total_time = 0;
        String sql;

        for (RegisteredServer server : SayanPlayTime.INSTANCE.getServer().getAllServers()) {
            String gameMode = server.getServerInfo().getName().toLowerCase();
            sql = String.format("SELECT * FROM sayanplaytime_daily WHERE uuid='%s'", uuid.toString());
            float result = resultExecute(sql, gameMode);
            total_time = total_time + result;
        }

        sql = String.format("INSERT INTO sayanplaytime_daily (uuid) VALUES ('%s') ON DUPLICATE KEY UPDATE total_time = %s;", uuid.toString(), total_time);
        execute(sql);
    }

    public long getPlayerPlayTime(UUID uuid, String gameMode) throws Exception {
        String sql = String.format("SELECT * FROM sayanplaytime_playtime WHERE uuid = '%s';", uuid);
        return getInfo(sql, gameMode);
    }

    public long getDailyPlayTime(UUID uuid, String gameMode) throws Exception {
        String sql = String.format("SELECT * FROM sayanplaytime_daily WHERE uuid = '%s';", uuid);
        return getInfo(sql, gameMode);
    }

    public long getWeeklyPlayTime(UUID uuid) throws Exception {
        String sql = String.format("SELECT * FROM sayanplaytime_weekly WHERE uuid = '%s';", uuid);
        return getInfo(sql, "time");
    }


    public long getDailyPlayTime(UUID uuid) throws Exception {
        String sql = String.format("SELECT * FROM sayanplaytime_daily WHERE uuid = '%s';", uuid);
        return getInfo(sql, "total_time");
    }

    public long getPlayerPlayTime(String userName, String gameMode) throws Exception {
        String sql = String.format("SELECT * FROM sayanplaytime_playtime WHERE name = '%s';", userName);
        return getInfo(sql, gameMode);
    }

    public List<PPlayer> getTopPlayTimes(int amount) throws Exception {
        String sql = String.format("SELECT * FROM sayanplaytime_playtime ORDER BY total_time DESC LIMIT %s;", amount);

        if (isClosed()) {
            openConnection();
        }

        List<PPlayer> pPlayers = new ArrayList<>();

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {
            String uuid = resultset.getString("uuid");
            String name = resultset.getString("name");
            long time = resultset.getLong("total_time");

            pPlayers.add(new PPlayer(UUID.fromString(uuid), name, time));
        }

        resultset.close();
        statement.close();

        return pPlayers;
    }

    public List<PPlayer> getDailyPlayTimes() throws Exception {
        String sql = "SELECT * FROM sayanplaytime_daily ORDER BY total_time;";

        if (isClosed()) {
            openConnection();
        }

        List<PPlayer> pPlayers = new ArrayList<>();

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {
            String uuid = resultset.getString("uuid");
            String name = resultset.getString("name");
            long time = resultset.getLong("total_time");

            pPlayers.add(new PPlayer(UUID.fromString(uuid), name, time));
        }

        resultset.close();
        statement.close();

        return pPlayers;
    }



    public List<PPlayer> getWeeklyTops(int amount) throws Exception {
        String sql = String.format("SELECT * FROM sayanplaytime_weekly ORDER BY time DESC LIMIT %s;", amount);

        if (isClosed()) {
            openConnection();
        }

        List<PPlayer> pPlayers = new ArrayList<>();

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {
            String uuid = resultset.getString("uuid");
            String name = resultset.getString("name");
            long time = resultset.getLong("time");

            pPlayers.add(new PPlayer(UUID.fromString(uuid), name, time));
        }

        resultset.close();
        statement.close();

        return pPlayers;
    }

    private long getInfo(String sql, String gameMode) throws Exception {
        if (isClosed()) {
            openConnection();
        }

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);

        long time = 0;
        if (resultset.next()) {
            time = resultset.getLong(gameMode.toLowerCase());
        }

        resultset.close();
        statement.close();

        return time;
    }

    public Map<UUID, Long> getTopOnlineTimes(int amount) throws Exception {

        Map<UUID, Long> playerMap = new HashMap<>();

        String sql = "SELECT * FROM sayanplaytime_playtime ORDER BY total_time DESC LIMIT " + amount + ";";

        if (isClosed()) {
            openConnection();
        }

        Statement statement = connection.createStatement();
        ResultSet resultset = statement.executeQuery(sql);
        while (resultset.next()) {
            UUID uuid = UUID.fromString(resultset.getString("uuid"));
            long time = resultset.getLong("time");

            playerMap.put(uuid, time);
        }

        resultset.close();
        statement.close();

        return playerMap;
    }

    public void resetWeekly() throws Exception {
        String sql = "DELETE FROM sayanplaytime_weekly;";
        execute(sql);
    }

    public void resetDaily() throws Exception {
        String sql = "DELETE FROM sayanplaytime_daily;";
        execute(sql);
    }

    private void execute(String sql) throws Exception {
        if (isClosed()) {
            openConnection();
        }

        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    private float resultExecute(String sql, String gameMode) throws Exception {
        if (isClosed()) {
            openConnection();
        }

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        float time = 0;
        while (resultSet.next()) {
            time = resultSet.getInt(gameMode.toLowerCase());
        }
        statement.close();
        return time;
    }
}
