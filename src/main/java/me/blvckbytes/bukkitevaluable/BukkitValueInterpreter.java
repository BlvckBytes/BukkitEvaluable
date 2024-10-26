package me.blvckbytes.bukkitevaluable;

import me.blvckbytes.gpeee.interpreter.StandardValueInterpreter;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

public class BukkitValueInterpreter extends StandardValueInterpreter {

  @Override
  public String asString(@Nullable Object value) {
    return ChatColor.translateAlternateColorCodes('&', super.asString(value));
  }
}
