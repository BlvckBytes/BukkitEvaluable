package me.blvckbytes.bukkitevaluable;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;

import java.util.ArrayList;
import java.util.List;

public class ConfigKeeper<T extends AConfigSection> {

  private final ConfigManager configManager;
  private final String fileName;
  private final Class<T> rootSectionType;
  private final List<Runnable> reloadListeners;

  public T rootSection;

  public ConfigKeeper(
    ConfigManager configManager,
    String fileName,
    Class<T> rootSectionType
  ) throws Exception {
    this.configManager = configManager;
    this.fileName = fileName;
    this.rootSectionType = rootSectionType;
    this.reloadListeners = new ArrayList<>();
    this.rootSection = loadRootSection(true);
  }

  public void registerReloadListener(Runnable listener) {
    this.reloadListeners.add(listener);
  }

  public void reload() throws Exception {
    this.rootSection = loadRootSection(false);

    for (var listener : this.reloadListeners)
      listener.run();
  }

  private T loadRootSection(boolean initial) throws Exception {
    // Called in ConfigManager's constructor already on startup
    if (!initial)
      this.configManager.loadAndPossiblyMigrateInputFiles();

    return this.configManager.loadConfig(fileName).mapSection(null, rootSectionType);
  }
}
