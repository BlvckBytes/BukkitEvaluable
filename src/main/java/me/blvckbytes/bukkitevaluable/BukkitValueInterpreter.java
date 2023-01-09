package me.blvckbytes.bukkitevaluable;

import lombok.Getter;
import me.blvckbytes.gpeee.interpreter.StandardValueInterpreter;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

public class BukkitValueInterpreter extends StandardValueInterpreter {

  @Getter
  private static final BukkitValueInterpreter instance = new BukkitValueInterpreter();

  private BukkitValueInterpreter() {}

  @Override
  public String asString(@Nullable Object value) {
    return ChatColor.translateAlternateColorCodes('&', super.asString(value));
  }
}
