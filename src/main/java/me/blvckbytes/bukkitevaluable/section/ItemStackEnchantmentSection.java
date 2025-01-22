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

package me.blvckbytes.bukkitevaluable.section;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class ItemStackEnchantmentSection extends AConfigSection {

  private BukkitEvaluable enchantment;
  private @Nullable BukkitEvaluable level;

  public ItemStackEnchantmentSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (enchantment == null)
      throw new MappingError("Property \"enchantment\" of an item's contained enchantment cannot be absent");
  }

  public CheckResult isContainedByMeta(ItemMeta meta, IEvaluationEnvironment environment) {
    var expectedEnchantment = enchantment.asEnchantment(environment);

    if (expectedEnchantment == null)
      return CheckResult.INVALID_SECTION;

    var containedLevel = meta.getEnchantLevel(expectedEnchantment);

    if (containedLevel == 0)
      return CheckResult.MISMATCHING_SECTION;

    if (level == null)
      return CheckResult.MATCHING_SECTION;

    var expectedLevel = level.asScalar(ScalarType.INT, environment);

    if (expectedLevel == containedLevel)
      return CheckResult.MATCHING_SECTION;

    return CheckResult.MISMATCHING_SECTION;
  }

  public BukkitEvaluable getEnchantment() {
    return enchantment;
  }

  public BukkitEvaluable getLevel() {
    return level;
  }
}
