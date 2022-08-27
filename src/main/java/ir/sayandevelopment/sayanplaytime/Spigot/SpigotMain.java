package ir.sayandevelopment.sayanplaytime.Spigot;

import ir.sayandevelopment.sayanplaytime.database.MySQL;
import ir.sayandevelopment.sayanplaytime.database.SQL;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotMain extends JavaPlugin {

    public static SQL SQL;

    @Override
    public void onEnable() {
        String host = "localhost";
        String database = "server";
        String user = "server";
        String pass = "yG%@NU6wz}i#)ZQN";
        int port = 3306;

        SQL = new MySQL(null, host, port, database, user, pass);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholder().register();
        }
    }
}
