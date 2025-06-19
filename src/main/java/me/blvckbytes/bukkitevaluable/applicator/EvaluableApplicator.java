package me.blvckbytes.bukkitevaluable.applicator;

import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public interface EvaluableApplicator {

  void setDisplayName(ItemMeta meta, BukkitEvaluable evaluable, IEvaluationEnvironment environment);

  void setLore(ItemMeta meta, BukkitEvaluable evaluable, IEvaluationEnvironment environment, boolean override);

  void sendMessage(CommandSender receiver, BukkitEvaluable evaluable, IEvaluationEnvironment environment);

  void sendActionBarMessage(CommandSender receiver, BukkitEvaluable evaluable, IEvaluationEnvironment environment);

  void sendTitles(
    CommandSender receiver,
    @Nullable BukkitEvaluable title, @Nullable IEvaluationEnvironment titleEnvironment,
    @Nullable BukkitEvaluable subTitle, @Nullable IEvaluationEnvironment subTitleEnvironment,
    int fadeIn, int stay, int fadeOut
  );

}
