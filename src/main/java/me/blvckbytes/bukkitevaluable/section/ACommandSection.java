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
  private final String fallbackName;

  public ACommandSection(String fallbackName, EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
    this.fallbackName = fallbackName;
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    this.evaluatedName = name != null ? name.asScalar(ScalarType.STRING, builtBaseEnvironment) : fallbackName;
    this.evaluatedAliases = aliases != null ? aliases.asList(ScalarType.STRING, builtBaseEnvironment) : List.of();
  }

  public void apply(Command command, CommandUpdater commandUpdater) {
    if (!commandUpdater.tryUnregisterCommand(command))
      return;

    command.setAliases(evaluatedAliases);
    command.setName(evaluatedName);

    commandUpdater.tryRegisterCommand(command);
  }
}
