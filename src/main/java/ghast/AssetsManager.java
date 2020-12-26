package ghast;

import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.text.MessageFormat.format;

@UtilityClass
@SuppressWarnings("unused")
public class AssetsManager {

	private static final String ERROR_NOT_FOUND = "Asset \"{0}\" not found";

	public void saveTo(String resourceName, Path targetPath) {
		URL resourceUrl = getResourceUrl(resourceName);

		Path saveToPath;
		if (Files.isDirectory(targetPath)) {
			saveToPath = targetPath.resolve(resourceName);
		} else {
			saveToPath = targetPath;
		}

		doSaveTo(resourceUrl, saveToPath);
	}

	public void saveTo(String resourceName, File targetPath) {
		saveTo(resourceName, targetPath.toPath());
	}

	public InputStream loadResource(String resourceName, String defaultResourceName, boolean saveDefault) {
		Plugin plugin = GhastTools.getPlugin();
		InputStream inputStream;

		Path pathToResource = plugin.getDataFolder().toPath().resolve(resourceName);
		if (Files.exists(pathToResource)) {
			inputStream = openResource(pathToResource);
		} else if (defaultResourceName != null) {
			URL resourceUrl = getResourceUrl(defaultResourceName);

			if (saveDefault) {
				doSaveTo(resourceUrl, pathToResource);
				inputStream = openResource(pathToResource);
			} else {
				inputStream = openResource(resourceUrl);
			}
		} else {
			throw new AssetsException(format(ERROR_NOT_FOUND, resourceName));
		}

		return inputStream;
	}

	public InputStream loadResource(String resourceName, String defaultResourceName) {
		return loadResource(resourceName, defaultResourceName, true);
	}

	public InputStream loadResource(String resourceName, boolean saveDefault) {
		return loadResource(resourceName, resourceName, saveDefault);
	}

	public InputStream loadResource(String resourceName) {
		return loadResource(resourceName, resourceName, true);
	}

	private URL getResourceUrl(String resourceName) {
		URL resourceUrl = AssetsManager.class.getClassLoader().getResource(resourceName);
		if (resourceUrl == null) {
			throw new AssetsException(format(ERROR_NOT_FOUND, resourceName));
		}

		return resourceUrl;
	}

	private void doSaveTo(URL resourceUrl, Path saveToPath) {
		try (InputStream inputStream = resourceUrl.openStream();
			 OutputStream outputStream = Files.newOutputStream(saveToPath)) {

			byte[] buffer = new byte[8192];
			int count;
			while ((count = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, count);
			}
		} catch (IOException e) {
			throw new AssetsException(format("Error save asset \"{0}\" to \"{1}\": {2}",
					resourceUrl, saveToPath, e.getMessage()), e);
		}
	}

	private InputStream openResource(Path pathToResource) {
		try {
			return Files.newInputStream(pathToResource);
		} catch (IOException e) {
			throw new AssetsException(format("Error open asset \"{0}\": {1}", pathToResource, e.getMessage()), e);
		}
	}

	private InputStream openResource(URL resourceUrl) {
		try {
			return resourceUrl.openStream();
		} catch (IOException e) {
			throw new AssetsException(format("Error open asset \"{0}\": {1}", resourceUrl, e.getMessage()), e);
		}
	}
}
