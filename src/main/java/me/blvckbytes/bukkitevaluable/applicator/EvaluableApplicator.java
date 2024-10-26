package me.blvckbytes.bukkitevaluable.applicator;

import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.meta.ItemMeta;

public interface EvaluableApplicator {

  void setDisplayName(ItemMeta meta, BukkitEvaluable evaluable, IEvaluationEnvironment environment);

  void setLore(ItemMeta meta, BukkitEvaluable evaluable, IEvaluationEnvironment environment, boolean override);

  void sendMessage(CommandSender receiver, BukkitEvaluable evaluable, IEvaluationEnvironment environment);

}
