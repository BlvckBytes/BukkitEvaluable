package me.blvckbytes.bukkitevaluable.section;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.CommandUpdater;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.bukkit.command.Command;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public abstract class ACommandSection extends AConfigSection {

  private @Nullable BukkitEvaluable name;
  private @Nullable BukkitEvaluable aliases;

  @CSIgnore
  public String evaluatedName;

  @CSIgnore
  private List<String> evaluatedAliases;

  @CSIgnore
  public final String initialName;

  public ACommandSection(String initialName, EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
    this.initialName = initialName;
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    this.evaluatedName = name != null ? name.asScalar(ScalarType.STRING, builtBaseEnvironment) : initialName;
    this.evaluatedAliases = aliases != null ? aliases.asList(ScalarType.STRING, builtBaseEnvironment) : List.of();
  }

  public void apply(Command command, CommandUpdater commandUpdater) {
    if (!commandUpdater.tryUnregisterCommand(command))
      return;

    command.setAliases(evaluatedAliases);
    command.setName(evaluatedName);

    commandUpdater.tryRegisterCommand(command);
  }

  public boolean isLabel(String label) {
    if (evaluatedName.equalsIgnoreCase(label))
      return true;

    for (var evaluatedAlias : evaluatedAliases) {
      if (evaluatedAlias.equalsIgnoreCase(label))
        return true;
    }

    return false;
  }
}
