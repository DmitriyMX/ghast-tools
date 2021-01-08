package ghast;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import ru.dmitriymx.reflection.ReflectionClass;
import ru.dmitriymx.reflection.ReflectionObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@UtilityClass
@SuppressWarnings("unused")
public class BuildHelper {

    private final Class<?> CLASS_BLOCKPOSITION = getClassForName("net.minecraft.server.v1_12_R1.BlockPosition");
    private final Class<?> CLASS_GAMEPROFILE = getClassForName("com.mojang.authlib.GameProfile");

    /**
     * Установка черепа.
     * <p>
     * После установки, необходимо выполнить <code>skull.update(true);</code>
     *
     * @param location место установки.
     * @param face куда будет повёрнут череп.
     * @return Блок типа {@link Skull}
     */
    public Skull placeSkull(Location location, BlockFace face) {
        Location fixedLocation = GhastTools.copyLocation(location);
        fixedLocation.setZ(fixedLocation.getZ() - 1);

        Block block = location.getWorld().getBlockAt(location);
        block.setType(Material.SKULL);

        Skull skull = (Skull) block.getState();
        skull.setRotation(face);

        org.bukkit.material.Skull skullMaterial = (org.bukkit.material.Skull) skull.getData();
        skullMaterial.setFacingDirection(BlockFace.SELF);

        return skull;
    }

    /**
     * Установка головы игрока.
     * <p>
     * После установки, необходимо выполнить <code>skull.update(true);</code>
     *
     * @param location место установки.
     * @param face куда будет повёрнута голова.
     * @return Блок типа {@link Skull}
     */
    public static Skull placePlayerHead(Location location, BlockFace face) {
        Location fixedLocation = GhastTools.copyLocation(location);
        fixedLocation.setZ(fixedLocation.getZ() - 1);

        Block block = fixedLocation.getWorld().getBlockAt(fixedLocation);
        block.setType(Material.SKULL);

        Skull skull = (Skull) block.getState();
        skull.setSkullType(SkullType.PLAYER);
        skull.setRotation(face);

        org.bukkit.material.Skull skullMaterial = (org.bukkit.material.Skull) skull.getData();
        skullMaterial.setFacingDirection(BlockFace.SELF);
        return skull;
    }

    /**
     * Установка текстурированной головы игрока.
     *
     * @param location место установки.
     * @param face куда будет повёрнута голова.
     * @param skinUrl URL на текстуру
     * @return Блок типа {@link Skull}
     */
    public static Skull placePlayerHead(Location location, BlockFace face, String skinUrl) {
        Skull playerHead = placePlayerHead(location, face);
        playerHead.update(true);
        setPlayerHeadSkin(playerHead, skinUrl);

        return playerHead;
    }

    /**
     * Установка текстуры для головы игрока.
     *
     * @param skull блок головы игрока
     * @param skinUrl URL на текстуру
     */
    public static void setPlayerHeadSkin(Skull skull, String skinUrl) {
        //TODO заменить рефлексию на "фантомные" классы
        ReflectionObject refobjBlockPosition = new ReflectionClass(CLASS_BLOCKPOSITION)
                .constructor(double.class, double.class, double.class)
                .newInstance(skull.getX(), skull.getY(), skull.getZ());

        new ReflectionObject(skull.getWorld())
                .method("getHandle").invoke()
                .method("getTileEntity", CLASS_BLOCKPOSITION)
                    .invoke(refobjBlockPosition.getOriginalObject())
                .method("setGameProfile", CLASS_GAMEPROFILE)
                    .invoke(getRefObjPlayerProfile(skinUrl).getOriginalObject());
    }

    public Sign placeSignWall(Location location, BlockFace face) {
        Block block = location.getWorld().getBlockAt(location);
        block.setType(Material.WALL_SIGN);

        Sign sign = (Sign) block.getState();
        org.bukkit.material.Sign signMaterial = (org.bukkit.material.Sign) sign.getData();
        signMaterial.setFacingDirection(face);

        return sign;
    }

    private ReflectionObject getRefObjPlayerProfile(String url){
        ReflectionObject refobjProperty = new ReflectionClass(
                    getClassForName("com.mojang.authlib.properties.Property"))
                .constructor(String.class, String.class)
                .newInstance("textures", Base64.getEncoder()
                        .encodeToString(("{textures:{SKIN:{url:\"" + url + "\"}}}").getBytes(StandardCharsets.UTF_8)));

        ReflectionObject refobjGameProfile = new ReflectionClass(CLASS_GAMEPROFILE)
                .constructor(UUID.class, String.class)
                .newInstance(UUID.randomUUID(), null);
        refobjGameProfile
                .method("getProperties").invoke()
                .method("put", Object.class, Object.class)
                    .invoke("textures", refobjProperty.getOriginalObject());

        return refobjGameProfile;
    }

    private Class<?> getClassForName(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
