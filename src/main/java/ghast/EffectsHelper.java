package ghast;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

@UtilityClass
@SuppressWarnings("unused")
public class EffectsHelper {

	public void playSound(Location location, Sound sound, float pitch) {
		location.getWorld().playSound(location, sound, SoundCategory.MASTER, 1.0f, pitch);
	}
}

