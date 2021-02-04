package ghast;

import ghast.assets.AssetsManager;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.Reader;
import java.lang.ref.WeakReference;

@UtilityClass
@SuppressWarnings("unused")
public class GhastTools {

    private static WeakReference<Plugin> refPlugin;

    public void setPlugin(Plugin plugin) {
        refPlugin = plugin == null ? null : new WeakReference<>(plugin);
    }

    public Plugin getPlugin() {
        if (refPlugin == null) {
            throw new RuntimeException("Plugin not set.");
        }

        Plugin plugin = refPlugin.get();
        if (plugin == null) {
            throw new RuntimeException("Plugin not set.");
        }

        return plugin;
    }

    public YamlConfiguration loadConfig(boolean saveDefault) {
        if (saveDefault) {
            getPlugin().saveDefaultConfig();
        }

        try (Reader reader = AssetsManager.getAsReader("config.yml", saveDefault)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            throw new RuntimeException("Error load config: " + e.getMessage(), e);
        }
    }

    public YamlConfiguration loadConfig() {
        return loadConfig(true);
    }

    public Location copyLocation(Location location) {
        return new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }
}
