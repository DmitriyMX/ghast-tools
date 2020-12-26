package ghast;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

@UtilityClass
@SuppressWarnings("unused")
public class GhastTools {

	private static WeakReference<Plugin> refPlugin;

	@SuppressWarnings("java:S2696")
	public void setPlugin(Plugin plugin) {
		if (plugin == null) {
			refPlugin = null;
		} else {
			refPlugin = new WeakReference<>(plugin);
		}
	}

	@SuppressWarnings("java:S112")
	public YamlConfiguration loadConfig(boolean saveDefault) {
		if (saveDefault) {
			getPlugin().saveDefaultConfig();
		}

		try (InputStream inputStream = AssetsManager.loadResource("config.yml", saveDefault)) {
			return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
		} catch (IOException e) {
			throw new RuntimeException("Error load config: " + e.getMessage(), e);
		}
	}

	public YamlConfiguration loadConfig() {
		return loadConfig(true);
	}

	@SuppressWarnings("java:S112")
	Plugin getPlugin() {
		if (refPlugin == null) {
			throw new RuntimeException("Plugin not set.");
		}

		Plugin plugin = refPlugin.get();
		if (plugin == null) {
			throw new RuntimeException("Plugin not set.");
		}

		return plugin;
	}
}
