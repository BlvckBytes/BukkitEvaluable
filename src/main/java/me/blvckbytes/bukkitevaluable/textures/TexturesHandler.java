package me.blvckbytes.bukkitevaluable.textures;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public interface TexturesHandler {

  void setBase64Textures(ItemMeta meta, String base64Textures);

  @Nullable String getBase64Textures(ItemMeta meta);

}
