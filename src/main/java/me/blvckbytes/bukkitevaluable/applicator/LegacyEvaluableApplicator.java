package me.blvckbytes.bukkitevaluable.applicator;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LegacyEvaluableApplicator implements EvaluableApplicator {

  @Override
  public void setDisplayName(ItemMeta meta, BukkitEvaluable evaluable, IEvaluationEnvironment environment) {
    var displayNameMessage = evaluable.asScalar(ScalarType.STRING, environment);
    meta.setDisplayName(translateColors(displayNameMessage));
  }

  @Override
  public void setLore(ItemMeta meta, BukkitEvaluable evaluable, IEvaluationEnvironment environment, boolean override) {
    List<String> loreLines = meta.getLore();

    if (loreLines == null)
      loreLines = new ArrayList<>();

    if (override)
      loreLines.clear();

    for (var loreLineMessage : evaluable.asList(ScalarType.STRING_PRESERVE_NULLS, environment)) {
      if (loreLineMessage == null)
        continue;

      loreLines.add(translateColors(loreLineMessage));
    }

    meta.setLore(loreLines);
  }

  @Override
  public void sendMessage(CommandSender receiver, BukkitEvaluable evaluable, IEvaluationEnvironment environment) {
    if (evaluable.value instanceof Collection<?>) {
      for (var message : evaluable.asList(ScalarType.STRING, environment))
        receiver.sendMessage(translateColors(message));

      return;
    }

    var message = translateColors(evaluable.asScalar(ScalarType.STRING, environment));
    receiver.sendMessage(message);
  }

  private String translateColors(String input) {
    return ChatColor.translateAlternateColorCodes('&', input);
  }
}
