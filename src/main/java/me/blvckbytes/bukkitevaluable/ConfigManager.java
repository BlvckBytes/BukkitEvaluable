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

import com.google.common.base.Charsets;
import me.blvckbytes.bbconfigmapper.*;
import me.blvckbytes.bbconfigmapper.preprocessor.PreProcessor;
import me.blvckbytes.bbconfigmapper.preprocessor.PreProcessorException;
import me.blvckbytes.bbconfigmapper.preprocessor.PreProcessorInput;
import me.blvckbytes.bbconfigmapper.preprocessor.PreProcessorInputException;
import me.blvckbytes.bukkitevaluable.applicator.EvaluableApplicator;
import me.blvckbytes.bukkitevaluable.applicator.LegacyEvaluableApplicator;
import me.blvckbytes.bukkitevaluable.functions.Base64ToSkinUrlFunction;
import me.blvckbytes.bukkitevaluable.functions.SkinUrlToBase64Function;
import me.blvckbytes.bukkitevaluable.section.ItemStackSection;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.Tuple;
import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigManager implements IConfigManager, IValueConverterRegistry {

  private static final String EXPRESSION_MARKER_SUFFIX = "$";
  private static final String PREPROCESSOR_INPUT_MARKER = "PRE-PROCESSOR-INPUT ";

  private final Map<String, Tuple<IExpressionEvaluator, IConfigMapper>> mapperByFileName;
  private final Map<String, PreProcessorInput> preProcessorInputByFileName;

  private final Logger logger;
  private final Plugin plugin;

  private final AExpressionFunction
    base64ToSkinUrlFunction,
    skinUrlToBase64Function;

  private final String folderName;
  private final File folder;

  private final PreProcessor preProcessor;
  private final EvaluableApplicator applicator;

  private final @Nullable Consumer<EvaluationEnvironmentBuilder> baseEnvironmentConsumer;

  public ConfigManager(
    Plugin plugin,
    String folderName
  ) throws Exception {
    this(plugin, folderName, null);
  }

  public ConfigManager(
    Plugin plugin,
    String folderName,
    @Nullable Consumer<EvaluationEnvironmentBuilder> baseEnvironmentConsumer
  ) throws Exception {
    this.applicator = new LegacyEvaluableApplicator();
    this.mapperByFileName = new HashMap<>();
    this.preProcessorInputByFileName = new HashMap<>();
    this.baseEnvironmentConsumer = baseEnvironmentConsumer;

    this.plugin = plugin;
    this.logger = plugin.getLogger();
    this.folderName = folderName.charAt(0) == '/' ? folderName : ("/" + folderName);
    this.preProcessor = new PreProcessor();

    this.folder = new File(plugin.getDataFolder(), folderName);

    if (!this.folder.exists()) {
      if (!this.folder.mkdirs())
        throw new IllegalStateException("Could not create directories for " + this.folder);
    }

    this.base64ToSkinUrlFunction = new Base64ToSkinUrlFunction();
    this.skinUrlToBase64Function = new SkinUrlToBase64Function();

    loadAndPossiblyMigrateInputFiles();
  }

  public EvaluableApplicator getApplicator() {
    return applicator;
  }

  private void loadAndPossiblyMigrateInputFile(@Nullable Path internalPath, File externalFile) throws Exception {
    var fileName = externalFile.getName().toLowerCase();

    PreProcessorInput internalInput = null;

    if (internalPath != null) {
      internalInput = new PreProcessorInput();

      try {
        var internalFileStream = ConfigManager.class.getResourceAsStream(internalPath.toString());

        if (internalFileStream == null)
          throw new IllegalStateException("Expected " + internalPath + " to exist within jar");

        try (
          var internalFileStreamReader = new InputStreamReader(internalFileStream, Charsets.UTF_8)
        ) {
          internalInput.load(internalFileStreamReader);
        }
      } catch (PreProcessorInputException e) {
        throw new IllegalStateException("Conflict " + e.conflict + " occurred on line " + e.lineNumber + " while trying to load " + internalPath + "\n" + e.lineContents);
      }


      if (!externalFile.exists()) {
        try (
          var writer = new FileWriter(externalFile, Charsets.UTF_8)
        ) {
          internalInput.save(writer);
        }

        preProcessorInputByFileName.put(fileName, internalInput);
        return;
      }
    }

    if (!externalFile.isFile())
      throw new IllegalStateException("Expected file at " + externalFile + ", but found directory");

    var externalInput = new PreProcessorInput();

    try {
      try (
        var externalFileReader = new FileReader(externalFile, Charsets.UTF_8)
      ) {
        externalInput.load(externalFileReader);
      }

      if (internalInput != null) {
        var numExtendedKeys = externalInput.migrateTo(internalInput);

        if (numExtendedKeys > 0)
          this.logger.log(Level.INFO, "Extended " + numExtendedKeys + " new keys on the pre-processor input " + fileName);

        try (
          var writer = new FileWriter(externalFile, Charsets.UTF_8)
        ) {
          externalInput.save(writer);
        }
      }
    } catch (PreProcessorInputException e) {
      throw new IllegalStateException("Conflict " + e.conflict + " occurred on line " + e.lineNumber + " while trying to load " + externalFile + "\n" + e.lineContents);
    }

    preProcessorInputByFileName.put(fileName, externalInput);
  }

  private List<Path> getTextFilesInFolderWithinResources() throws Exception {
    var folderUrl = ConfigManager.class.getResource(folderName);

    if (folderUrl == null)
      throw new IllegalStateException("Could not access resources-folder at " + folderName);

    var folderUri  = folderUrl.toURI();

    Path folderPath;
    FileSystem fileSystem = null;

    if (folderUri.getScheme().equals("jar")) {
      fileSystem = FileSystems.newFileSystem(folderUri, Collections.emptyMap());
      folderPath = fileSystem.getPath(folderName);
    }

    else
      folderPath = Paths.get(folderUri);

    var result = new ArrayList<Path>();

    try (
      var walkStream = Files.walk(folderPath, 1)
    ) {
      for (var walkStreamIterator = walkStream.iterator(); walkStreamIterator.hasNext();) {
        var internalPath = walkStreamIterator.next();

        if (!internalPath.getFileName().toString().endsWith(".txt"))
          continue;

        var parent = internalPath.getParent();

        if (parent == null)
          continue;

        if (!parent.getFileName().toString().equals(folderName.substring(1)))
          continue;

        result.add(internalPath);
      }
    }

    if (fileSystem != null)
      fileSystem.close();

    return result;
  }

  public void loadAndPossiblyMigrateInputFiles() throws Exception {
    for (var textFilePath : getTextFilesInFolderWithinResources())
      loadAndPossiblyMigrateInputFile(textFilePath, new File(folder, textFilePath.getFileName().toString()));

    // Allow to load (yet-)unknown files from the folder (obviously without prior migration).
    // This is very useful while creating new translations.
    for (var fileInFolder : Objects.requireNonNull(folder.listFiles())) {
      var fileName = fileInFolder.getName();

      if (!fileName.endsWith(".txt"))
        continue;

      if (preProcessorInputByFileName.containsKey(fileName))
        continue;

      loadAndPossiblyMigrateInputFile(null, fileInFolder);
    }
  }

  @Override
  public IConfigMapper getMapper(String fileName) throws FileNotFoundException {
    Tuple<IExpressionEvaluator, IConfigMapper> mapper = mapperByFileName.get(fileName.toLowerCase());

    if (mapper == null)
      throw new FileNotFoundException("Could not find the config at " + folderName + "/" + fileName);

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
    if (type == BukkitEvaluable.class)
      return (value, evaluator) -> new BukkitEvaluable(value, evaluator, applicator);

    if (type == IItemBuildable.class)
      return (value, evaluator) -> ((ItemStackSection) value).asItem();

    return null;
  }

  private String getPluginResourcePath(String fileName) {
    return folderName.substring(1) + "/" + fileName;
  }

  private int extendConfig(String fileName, YamlConfig config) throws Exception {
    var resourcePath = getPluginResourcePath(fileName);

    try (
      InputStream resourceStream = this.plugin.getResource(resourcePath)
    ) {
      if (resourceStream == null)
        throw new IllegalStateException("Could not load resource file at " + resourcePath);

      YamlConfig resourceConfig = new YamlConfig(null, this.logger, null);

      try (
        var resourceStreamReader = new InputStreamReader(resourceStream, Charsets.UTF_8)
      ) {
        resourceConfig.load(resourceStreamReader);
      }

      return config.extendMissingKeys(resourceConfig);
    }
  }

  private void saveConfig(YamlConfig config, String fileName) throws Exception {
    File file = new File(this.folder, fileName);

    if (file.exists() && !file.isFile())
      throw new IllegalStateException("Tried to write file; unexpected directory at " + file);

    try (
      FileOutputStream outputStream = new FileOutputStream(file);
      OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream)
    ) {
      config.save(outputWriter);
    }
  }

  private @Nullable String parsePreProcessorFileName(String fileName, YamlConfig config) {
    var file = new File(this.folder, fileName);
    var configHeader = config.getHeader();
    var inputMarkerIndex = configHeader.indexOf(PREPROCESSOR_INPUT_MARKER);

    if (inputMarkerIndex < 0)
      return null;

    inputMarkerIndex += PREPROCESSOR_INPUT_MARKER.length();
    var inputArgumentBegin = inputMarkerIndex;

    while (inputMarkerIndex < configHeader.length()) {
      var currentChar = configHeader.charAt(inputMarkerIndex);

      if (currentChar == ' ' || currentChar == '\n') {
        --inputMarkerIndex;
        break;
      }

      ++inputMarkerIndex;
    }

    var inputArgument = configHeader.substring(inputArgumentBegin, inputMarkerIndex + 1);

    if (!inputArgument.endsWith(".txt")) {
      logger.warning("Invalid pre-processor input in header-comment of file " + file + " with value \"" + inputArgument + "\"");
      return null;
    }

    return inputArgument;
  }

  public ConfigMapper loadConfig(String fileName) throws Exception {
    boolean hasBeenCreated = false;

    File file = new File(this.folder, fileName);

    if (file.exists()) {
      if (file.isDirectory())
        throw new IllegalStateException("Tried to read file; unexpected directory at " + file);
    } else {
      this.plugin.saveResource(getPluginResourcePath(fileName), true);
      hasBeenCreated = true;
    }

    try (
      var inputStream = new FileInputStream(file);
      var inputStreamReader = new InputStreamReader(inputStream, Charsets.UTF_8)
    ) {
      GPEEE evaluator = new GPEEE(logger);
      YamlConfig config = new YamlConfig(evaluator, this.logger, EXPRESSION_MARKER_SUFFIX);

      config.load(inputStreamReader);

      if (!hasBeenCreated) {
        int numExtendedKeys = extendConfig(fileName, config);

        if (numExtendedKeys > 0) {
          this.logger.log(Level.INFO, "Extended " + numExtendedKeys + " new keys on the configuration " + fileName);
          saveConfig(config, fileName);
        }
      }

      var preProcessorFileName = parsePreProcessorFileName(fileName, config);

      if (preProcessorFileName != null) {
        var preProcessorInput = preProcessorInputByFileName.get(preProcessorFileName);

        if (preProcessorInput == null)
          throw new IllegalStateException("Could not locate pre-processor input named " + preProcessorFileName + " as requested in " + file);

        preProcessor.forEachScalarValue(config, scalarNode -> {
          var nodeValue = scalarNode.getValue();

          try {
            var result = preProcessor.preProcess(nodeValue, preProcessorInput);
            preProcessor.setScalarValue(scalarNode, result.a);
            return result.b;
          } catch (PreProcessorException e) {
            int lineNumber = scalarNode.getStartMark().getLine();
            logger.severe("An error (" + e.conflict + ") occurred while pre-processing " + file + " at line " + lineNumber);
            logger.severe("Conflicting position (beginning of output): " + nodeValue.substring(e.charIndex).trim());
            throw e;
          }
        });

        config.clearKeyCache();

        logger.info("Applied pre-processor input " + preProcessorFileName + " to " + file);

        var preProcessorFileNameWithoutExtension = preProcessorFileName.substring(0, preProcessorFileName.lastIndexOf('.'));
        var outputFile = new File(file.getParentFile(), "result." + preProcessorFileNameWithoutExtension + ".yml");

        config.save(new FileWriter(outputFile));
        logger.info("Saved read-only pre-processed version of " + file + " at " + outputFile);
      }

      Object lutValue = config.get("lut");
      Map<?, ?> lut = lutValue instanceof Map ? (Map<?, ?>) lutValue : new HashMap<>();

      EvaluationEnvironmentBuilder baseEnvironment = new EvaluationEnvironmentBuilder()
        .withFunction("base64_to_skin_url", base64ToSkinUrlFunction)
        .withFunction("skin_url_to_base64", skinUrlToBase64Function);

      // Allow for external additions to the base environment
      if (baseEnvironmentConsumer != null)
        baseEnvironmentConsumer.accept(baseEnvironment);

      // Enable support for expressions within the LUT also
      baseEnvironment
        .withStaticVariable("lut", evaluateLeafExpressions(evaluator, baseEnvironment.build(), lut))
          .withValueInterpreter(new BukkitValueInterpreter());

      evaluator.setBaseEnvironment(baseEnvironment);

      ConfigMapper mapper = new ConfigMapper(config, this.logger, evaluator, this);
      mapperByFileName.put(fileName.toLowerCase(), new Tuple<>(evaluator, mapper));
      return mapper;
    }
  }

  private Object evaluateLeafExpressions(GPEEE evaluator, IEvaluationEnvironment environment, Object input) {
    if (input instanceof List<?> list) {
      var result = new ArrayList<>();

      for (var entry : list)
        result.add(evaluateLeafExpressions(evaluator, environment, entry));

      return result;
    }

    if (input instanceof Map<?,?> map) {
      var result = new HashMap<>();

      for (var entry : map.entrySet())
        result.put(entry.getKey(), evaluateLeafExpressions(evaluator, environment, entry.getValue()));

      return result;
    }

    while (input instanceof AExpression)
      input = evaluator.evaluateExpression((AExpression) input, environment);

    return input;
  }
}
