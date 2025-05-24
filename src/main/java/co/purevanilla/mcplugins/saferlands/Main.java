package co.purevanilla.mcplugins.saferlands;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        this.getLogger().info("Enabling plugin...");
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new SaferHandler(this), this);
    }
}