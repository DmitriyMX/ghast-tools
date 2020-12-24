package ghast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.text.MessageFormat.format;

@SuppressWarnings("unused")
public final class AssetsManager {

	private AssetsManager() {
		// this is utility class
	}

	public static void saveTo(String resourceName, Path targetPath) {
		URL resourceUrl = AssetsManager.class.getClassLoader().getResource(resourceName);
		if (resourceUrl == null) {
			throw new AssetsException(format("Asset \"{0}\" not found", resourceName));
		}

		try (InputStream inputStream = resourceUrl.openStream();
			 OutputStream outputStream = Files.newOutputStream(targetPath)) {

			byte[] buffer = new byte[8192];
			int count;
			while((count = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, count);
			}
		} catch (IOException e) {
			throw new AssetsException(format("Error save asset \"{0}\": {1}", resourceName, e.getMessage()), e);
		}
	}

	public static void saveTo(String resourceName, File targetPath) {
		saveTo(resourceName, targetPath.toPath());
	}
}
