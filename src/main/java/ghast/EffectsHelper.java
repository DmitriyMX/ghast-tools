package ghast;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.util.Vector;

@UtilityClass
@SuppressWarnings("unused")
public class EffectsHelper {

	public void playSound(Location location, Sound sound, float pitch) {
		location.getWorld().playSound(location, sound, SoundCategory.MASTER, 1.0f, pitch);
	}

	public void particle(Location location, Particle particle, double dx, double dy, double dz, double speed, int amount) {
		location.getWorld().spawnParticle(particle, location, amount, dx, dy, dz, speed);
	}

	//TODO нужно проверить
	public void particle(Location location, Particle particle, Vector vector, double speed, int amount) {
		particle(location, particle, vector.getX(), vector.getY(), vector.getZ(), speed, amount);
	}
}

