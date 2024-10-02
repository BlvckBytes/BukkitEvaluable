package me.blvckbytes.bukkitevaluable.textures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class PlayerProfileTexturesHandler implements TexturesHandler {

  private static final Gson gson = new GsonBuilder().create();

  @Override
  public void setBase64Textures(ItemMeta meta, String base64Textures) {
    if (!(meta instanceof SkullMeta skullMeta))
      return;

    String url = extractUrl(base64Textures);

    if (url == null)
      return;

    try {
      skullMeta.setOwnerProfile(getProfile(url));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public @Nullable String getBase64Textures(ItemMeta meta) {
    if (!(meta instanceof SkullMeta skullMeta))
      return null;

    PlayerProfile profile = skullMeta.getOwnerProfile();

    if (profile == null)
      return null;

    URL skinUrl = profile.getTextures().getSkin();

    if (skinUrl == null)
      return null;

    return Base64.getEncoder().encodeToString(
      ("{\"textures\":{\"SKIN\":{\"url\":\"" + skinUrl + "\"}}}").getBytes(StandardCharsets.UTF_8)
    );
  }

  private @Nullable String extractUrl(String base64Textures) {
    try {
      String textures = new String(Base64.getDecoder().decode(base64Textures));

      return gson.fromJson(textures, JsonObject.class)
        .getAsJsonObject("textures")
        .getAsJsonObject("SKIN")
        .get("url")
        .getAsString();
    } catch (Exception e) {
      return null;
    }
  }

  private PlayerProfile getProfile(String url) throws MalformedURLException {
    PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
    PlayerTextures textures = profile.getTextures();
    textures.setSkin(new URL(url));
    profile.setTextures(textures);
    return profile;
  }
}
