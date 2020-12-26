package ghast;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

@UtilityClass
@SuppressWarnings("unused")
public class BuildHelper {

	public Skull placeSkull(Location location, BlockFace face) {
		Block block = location.getWorld().getBlockAt(location);
		block.setType(Material.SKULL);
		Skull skull = (Skull) block.getState();
		skull.setRotation(face);
		org.bukkit.material.Skull skullMaterial = (org.bukkit.material.Skull) skull.getData();
		skullMaterial.setFacingDirection(BlockFace.SELF);

		return skull;
	}

	public Sign placeSignWall(Location location, BlockFace face) {
		Block block = location.getWorld().getBlockAt(location);
		block.setType(Material.WALL_SIGN);

		Sign sign = (Sign) block.getState();
		org.bukkit.material.Sign signMaterial = (org.bukkit.material.Sign) sign.getData();
		signMaterial.setFacingDirection(face);

		return sign;
	}
}
