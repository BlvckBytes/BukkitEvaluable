/*
 * MIT License
 *
 * Copyright (c) 2023 BlvckBytes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.blvckbytes.bukkitevaluable;

import me.blvckbytes.bbconfigmapper.*;
import me.blvckbytes.bukkitboilerplate.IFileHandler;
import me.blvckbytes.bukkitevaluable.section.ItemStackSection;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.utilitytypes.Tuple;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager implements IConfigManager, IValueConverterRegistry {

  private static final String EXPRESSION_MARKER_SUFFIX = "$";

  private final Map<String, Tuple<IExpressionEvaluator, IConfigMapper>> mapperByPath;
  private final GPEEELogRedirect gpeeeLogger;
  private final IFileHandler fileHandler;

  public ConfigManager(
    IConfigPathsProvider pathsProvider,
    GPEEELogRedirect gpeeeLogger,
    IFileHandler fileHandler
  ) throws Exception {
    this.mapperByPath = new HashMap<>();
    this.fileHandler = fileHandler;
    this.gpeeeLogger = gpeeeLogger;
    this.loadConfigs(pathsProvider.getConfigPaths());
  }

  @Override
  public IConfigMapper getMapper(String path) throws FileNotFoundException {
    Tuple<IExpressionEvaluator, IConfigMapper> mapper = mapperByPath.get(path.toLowerCase());

    if (mapper == null)
      throw new FileNotFoundException("Could not find the config at " + path);

    return mapper.b;
  }

  @Override
  public @Nullable Class<?> getRequiredTypeFor(Class<?> type) {
    if (type == BukkitEvaluable.class)
      return Object.class;
    if (type == IItemBuildable.class)
      return ItemStackSection.class;
    return null;
  }

  @Override
  public @Nullable FValueConverter getConverterFor(Class<?> type) {
    if (type == BukkitEvaluable.class) {
      return (value, evaluator) -> {
        if (value == null)
          return null;

        return new BukkitEvaluable(value, evaluator);
      };
    }

    if (type == IItemBuildable.class) {
      return (value, evaluator) -> {
        if (value == null)
          return null;
        return ((ItemStackSection) value).asItem();
      };
    }
    return null;
  }

  private void loadConfigs(String[] paths) throws Exception {
    for (String path : paths)
      loadConfig(path);
  }

  private void loadConfig(String path) throws Exception {
    if (!this.fileHandler.doesFileExist(path))
      this.fileHandler.saveResource(path);

    try (
      FileInputStream inputStream = this.fileHandler.openForReading(path);
    ) {
      if (inputStream == null)
        throw new IllegalStateException("Could not load configuration file at " + path);

      GPEEE evaluator = new GPEEE(gpeeeLogger);
      YamlConfig config = new YamlConfig(evaluator, this.gpeeeLogger, EXPRESSION_MARKER_SUFFIX);

      try (
        InputStreamReader streamReader = new InputStreamReader(inputStream);
      ) {
        config.load(streamReader);
      }

      Object lutValue = config.get("lut");
      Map<?, ?> lut = lutValue instanceof Map ? (Map<?, ?>) lutValue : new HashMap<>();

      EvaluationEnvironmentBuilder baseEnvironment = new EvaluationEnvironmentBuilder()
        .withStaticVariable("lut", lut)
        .withValueInterpreter(BukkitValueInterpreter.getInstance());

      evaluator.setBaseEnvironment(baseEnvironment);

      ConfigMapper mapper = new ConfigMapper(config, gpeeeLogger, evaluator, this);
      mapperByPath.put(path.toLowerCase(), new Tuple<>(evaluator, mapper));
    }
  }
}
