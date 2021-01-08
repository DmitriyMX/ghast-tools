package ghast.assets;

import ghast.GhastTools;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static java.text.MessageFormat.format;

@UtilityClass
@SuppressWarnings("unused")
public class AssetsManager {

    private static final String ERROR_NOT_FOUND = "Asset \"{0}\" not found";
    private static final String ERROR_OPEN = "Error open asset \"{0}\": {1}";

    //region getAsInputStream methods
    public InputStream getAsInputStream(String resourceName, String defaultResourceName, boolean saveDefault) {
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

    public InputStream getAsInputStream(String resourceName, String defaultResourceName) {
        return getAsInputStream(resourceName, defaultResourceName, true);
    }

    public InputStream getAsInputStream(String resourceName, boolean saveDefault) {
        return getAsInputStream(resourceName, resourceName, saveDefault);
    }

    public InputStream getAsInputStream(String resourceName) {
        return getAsInputStream(resourceName, resourceName, true);
    }
    //endregion

    //region getAsReader methods
    public Reader getAsReader(String resourceName, String defaultResourceName, boolean saveDefault) {
        return new InputStreamReader(getAsInputStream(resourceName, defaultResourceName, saveDefault));
    }

    public Reader getAsReader(String resourceName, String defaultResourceName) {
        return new InputStreamReader(getAsInputStream(resourceName, defaultResourceName, true));
    }

    public Reader getAsReader(String resourceName, boolean saveDefault) {
        return new InputStreamReader(getAsInputStream(resourceName, resourceName, saveDefault));
    }

    public Reader getAsReader(String resourceName) {
        return new InputStreamReader(getAsInputStream(resourceName, resourceName, true));
    }
    //endregion

    //region getAsString methods
    public String getAsString(String resourceName, String defaultResourceName, Charset charset, boolean saveDefault) {
        try (InputStream inputStream = getAsInputStream(resourceName, defaultResourceName, saveDefault);
             Scanner scanner = new Scanner(inputStream, charset.name()).useDelimiter("\\A")) {

            return scanner.next();
        } catch (IOException e) {
            throw new AssetsException(format(ERROR_OPEN, resourceName, e.getMessage()), e);
        }
    }

    public String getAsString(String resourceName, String defaultResourceName, Charset charset) {
        return getAsString(resourceName, defaultResourceName, charset, true);
    }

    public String getAsString(String resourceName, String defaultResourceName, boolean saveDefault) {
        return getAsString(resourceName, defaultResourceName, StandardCharsets.UTF_8, saveDefault);
    }

    public String getAsString(String resourceName, String defaultResourceName) {
        return getAsString(resourceName, defaultResourceName, StandardCharsets.UTF_8, true);
    }

    public String getAsString(String resourceName, Charset charset, boolean saveDefault) {
        return getAsString(resourceName, resourceName, charset, saveDefault);
    }

    public String getAsString(String resourceName, Charset charset) {
        return getAsString(resourceName, resourceName, charset, true);
    }

    public String getAsString(String resourceName, boolean saveDefault) {
        return getAsString(resourceName, resourceName, StandardCharsets.UTF_8, saveDefault);
    }

    public String getAsString(String resourceName) {
        return getAsString(resourceName, resourceName, StandardCharsets.UTF_8, true);
    }
    //endregion

    private URL getResourceUrl(String resourceName) {
        URL resourceUrl = AssetsManager.class.getClassLoader().getResource(resourceName);
        if (resourceUrl == null) {
            throw new AssetsException(format(ERROR_NOT_FOUND, resourceName));
        }

        return resourceUrl;
    }

    private void doSaveTo(URL resourceUrl, Path saveToPath) {
        try {
            Files.createDirectories(saveToPath.getParent());

            try (InputStream inputStream = resourceUrl.openStream();
                 OutputStream outputStream = Files.newOutputStream(saveToPath)) {

                byte[] buffer = new byte[8192];
                int count;
                while ((count = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, count);
                }
                outputStream.flush();
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
            throw new AssetsException(format(ERROR_OPEN, pathToResource, e.getMessage()), e);
        }
    }

    private InputStream openResource(URL resourceUrl) {
        try {
            return resourceUrl.openStream();
        } catch (IOException e) {
            throw new AssetsException(format(ERROR_OPEN, resourceUrl, e.getMessage()), e);
        }
    }
}
