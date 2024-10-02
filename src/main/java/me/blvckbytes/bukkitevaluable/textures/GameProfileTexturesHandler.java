package me.blvckbytes.bukkitevaluable.textures;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class GameProfileTexturesHandler implements TexturesHandler {

  private static final Class<?> HEAD_META_CLASS;
  private static @Nullable Method setProfileMethod;
  private static @Nullable Field profileField;

  static {
    HEAD_META_CLASS = new ItemStack(Material.PLAYER_HEAD).getItemMeta().getClass();

    try {
      setProfileMethod = HEAD_META_CLASS.getMethod("setProfile");
      setProfileMethod.setAccessible(true);
    } catch (Exception e) {
      setProfileMethod = null;
    }

    try {
      profileField = HEAD_META_CLASS.getDeclaredField("profile");
      profileField.setAccessible(true);
    } catch (Exception e) {
      profileField = null;
    }
  }

  @Override
  public void setBase64Textures(ItemMeta meta, String base64Textures) {
    GameProfile profile = new GameProfile(UUID.randomUUID(), "");
    profile.getProperties().put("textures", new Property("textures", base64Textures));

    if (!(meta instanceof SkullMeta))
      return;

    // if available, we use setProfile(GameProfile) so that it sets both the profile field and the
    // serialized profile field for us. If the serialized profile field isn't set
    // ItemStack#isSimilar() and ItemStack#equals() throw an error.

    try {
      // If setProfile is available, this is the preferred method, as it will also set
      // the serialized profile field without which bukkit will panic on ItemStack#isSimilar() or ItemStack#equals()
      if (setProfileMethod != null) {
        setProfileMethod.invoke(meta, profile);
        return;
      }

      // Method unavailable, just set the GameProfile field
      if (profileField != null) {
        profileField.set(meta, profile);
        return;
      }

      throw new IllegalStateException("Couldn't apply head-textures, as neither the method nor the field were accessible");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public @Nullable String getBase64Textures(ItemMeta meta) {
    if (profileField == null || !(meta instanceof SkullMeta skullMeta))
      return null;

    try {
      return ((GameProfile) profileField.get(meta))
        .getProperties()
        .get("textures")
        .iterator()
        .next()
        .value();
    } catch (Exception e) {
      return null;
    }
  }
}
