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

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.Nullable;

public class ItemStackBannerPatternSection extends AConfigSection {

  private @Nullable BukkitEvaluable pattern;
  private @Nullable BukkitEvaluable color;

  public ItemStackBannerPatternSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  public @Nullable Pattern asPattern(IEvaluationEnvironment environment) {
    PatternType pattern = this.pattern == null ? null : this.pattern.asEnumerationConstant(PatternType.class, environment);
    DyeColor color = this.color == null ? null : this.pattern.asEnumerationConstant(DyeColor.class, environment);

    // Cannot construct a pattern with missing data
    if (pattern == null || color == null)
      return null;

    return new Pattern(color, pattern);
  }

  public BukkitEvaluable getPattern() {
    return pattern;
  }

  public BukkitEvaluable getColor() {
    return color;
  }

  public boolean isContainedByMeta(BannerMeta meta, IEvaluationEnvironment environment) {
    for (var currentPattern : meta.getPatterns()) {
      if (describesPattern(currentPattern, environment))
        return true;
    }

    return false;
  }

  public boolean describesPattern(Pattern pattern, IEvaluationEnvironment environment) {
    if (this.pattern != null) {
      PatternType type = this.pattern.asEnumerationConstant(PatternType.class, environment);
      if (type != null && !type.equals(pattern.getPattern()))
        return false;
    }

    if (this.color != null) {
      DyeColor color = this.color.asEnumerationConstant(DyeColor.class, environment);
      if (color != null && !color.equals(pattern.getColor()))
        return false;
    }

    return true;
  }
}
