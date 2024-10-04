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
    this.rootSection = loadRootSection();
  }

  public void registerReloadListener(Runnable listener) {
    this.reloadListeners.add(listener);
  }

  public void reload() throws Exception {
    this.rootSection = loadRootSection();

    for (var listener : this.reloadListeners)
      listener.run();
  }

  private T loadRootSection() throws Exception {
    return this.configManager.loadConfig(fileName).mapSection(null, rootSectionType);
  }
}
