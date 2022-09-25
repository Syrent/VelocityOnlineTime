package ir.syrent.velocityonlinetime.spigot;

import org.bukkit.plugin.java.JavaPlugin;

public class RUoMPlugin extends JavaPlugin {

    private static RUoMPlugin instance;

    public RUoMPlugin() {
        instance = this;
    }

    public static RUoMPlugin get() {
        return instance;
    }

}
