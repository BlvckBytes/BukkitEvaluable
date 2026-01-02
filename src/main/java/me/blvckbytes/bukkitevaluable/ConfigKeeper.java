package me.blvckbytes.bukkitevaluable;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigKeeper<T extends AConfigSection> {

  private final ConfigManager configManager;
  private final String fileName;
  private final Class<T> rootSectionType;
  private final Map<ReloadPriority, List<Runnable>> reloadListenersByPriority;

  public T rootSection;

  public ConfigKeeper(
    ConfigManager configManager,
    String fileName,
    Class<T> rootSectionType
  ) throws Exception {
    this.configManager = configManager;
    this.fileName = fileName;
    this.rootSectionType = rootSectionType;
    this.reloadListenersByPriority = new HashMap<>();
    this.rootSection = loadRootSection(true);
  }

  public void registerReloadListener(Runnable listener, ReloadPriority priority) {
    reloadListenersByPriority.computeIfAbsent(priority, key -> new ArrayList<>()).add(listener);
  }

  public void registerReloadListener(Runnable listener) {
    registerReloadListener(listener, ReloadPriority.MEDIUM);
  }

  public void reload() throws Exception {
    this.rootSection = loadRootSection(false);

    for (var priority : ReloadPriority.VALUES_IN_CALL_ORDER) {
      var listeners = reloadListenersByPriority.get(priority);

      if (listeners == null)
        continue;

      for (var listener : listeners)
        listener.run();
    }
  }

  private T loadRootSection(boolean initial) throws Exception {
    // Called in ConfigManager's constructor already on startup
    if (!initial)
      this.configManager.loadAndPossiblyMigrateInputFiles();

    var result = this.configManager.loadConfig(fileName).mapSection(null, rootSectionType);
    buildAndSetBukkitEvaluableFallbacks(result, new ArrayList<>());
    return result;
  }

  /*
    This is a bit out-of-place here, but I do not have access to the applicator inside
    AConfigSection-s, because the mapper knows nothing about applicators, and even if it did,
    I don't know the full path for each field which needs a descriptive fallback-value.

    Patching them in recursively like this is hacky, yes, but I really need to move on atm.
   */
  private void buildAndSetBukkitEvaluableFallbacks(AConfigSection section, List<String> parentPaths) throws Exception {
    for (var field : section.getClass().getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers()))
        continue;

      if (field.isAnnotationPresent(CSIgnore.class))
        continue;

      var fieldValue = field.get(section);

      if (AConfigSection.class.isAssignableFrom(field.getType()) && fieldValue != null) {
        parentPaths.add(field.getName());
        buildAndSetBukkitEvaluableFallbacks((AConfigSection) fieldValue, parentPaths);
        parentPaths.removeLast();
        continue;
      }

      if (!BukkitEvaluable.class.isAssignableFrom(field.getType()))
        continue;

      if (field.isAnnotationPresent(Nullable.class))
        continue;

      if (fieldValue != null)
        continue;

      var value = makeMissingSignallingValue(field, parentPaths);

      if (Modifier.isPrivate(field.getModifiers()))
        field.setAccessible(true);

      field.set(section, value);
    }
  }

  private BukkitEvaluable makeMissingSignallingValue(Field field, List<String> parentPaths) {
    var fullPath = new StringBuilder();

    for (var i = 0; i < parentPaths.size(); ++i) {
      if (i != 0)
        fullPath.append('.');

      fullPath.append(parentPaths.get(i));
    }

    if(!fullPath.isEmpty())
      fullPath.append('.');

    fullPath.append(field.getName());

    return new BukkitEvaluable("§cMissing config-key at " + fullPath, null, configManager.getApplicator());
  }
}
