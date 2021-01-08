package ghast;

import lombok.experimental.UtilityClass;

import java.util.logging.Level;

import static java.text.MessageFormat.format;

@UtilityClass
@SuppressWarnings("unused")
public class XLog {

    //region Debug
    public void debug(String pattern, Object... objects) {
        if (objects.length > 1 && objects[objects.length - 1] instanceof Throwable) {
            Throwable throwable = (Throwable) objects[objects.length - 1];
            Object[] values = new Object[objects.length - 1];
            System.arraycopy(objects, 0, values, 0, values.length);

            debug(format(pattern, values), throwable);
        } else {
            debug(format(pattern, objects));
        }
    }

    public void debug(String message, Throwable throwable) {
        GhastTools.getPlugin().getLogger().log(Level.FINE, message, throwable);
    }

    public void debug(String message) {
        GhastTools.getPlugin().getLogger().fine(message);
    }
    //endregion

    //region Info
    public void info(String pattern, Object... objects) {
        info(format(pattern, objects));
    }

    public void info(String message) {
        GhastTools.getPlugin().getLogger().info(message);
    }
    //endregion

    //region Warning
    public void warn(String pattern, Object... objects) {
        warn(format(pattern, objects));
    }

    public void warn(String message) {
        GhastTools.getPlugin().getLogger().warning(message);
    }
    //endregion

    //region Error
    public void error(String pattern, Object... objects) {
        if (objects.length > 1 && objects[objects.length - 1] instanceof Throwable) {
            Throwable throwable = (Throwable) objects[objects.length - 1];
            Object[] values = new Object[objects.length - 1];
            System.arraycopy(objects, 0, values, 0, values.length);

            error(format(pattern, values), throwable);
        } else {
            error(format(pattern, objects));
        }
    }

    public void error(String message) {
        GhastTools.getPlugin().getLogger().severe(message);
    }

    public void error(String message, Throwable throwable) {
        GhastTools.getPlugin().getLogger().log(Level.SEVERE, message, throwable);
    }
    //endregion

}
