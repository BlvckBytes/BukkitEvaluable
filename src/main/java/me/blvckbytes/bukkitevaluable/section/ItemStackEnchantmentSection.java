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

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.Tuple;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

public class ItemStackEnchantmentSection extends AConfigSection {

  private @Nullable BukkitEvaluable enchantment;
  private @Nullable BukkitEvaluable level;

  public ItemStackEnchantmentSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  public @Nullable Tuple<Enchantment, Integer> asEnchantment(IEvaluationEnvironment environment) {
    // Try to parse the enchantment, if provided
    Enchantment enchantment = this.enchantment == null ? null : this.enchantment.asEnchantment(environment);

    // No enchantment provided
    if (enchantment == null)
      return null;

    Integer level = this.level == null ? null : this.level.<Long>asScalar(ScalarType.LONG, environment).intValue();

    // Fall back to level 1 if not provided
    return new Tuple<>(enchantment, level == null ? 1 : level);
  }

  public BukkitEvaluable getEnchantment() {
    return enchantment;
  }

  public BukkitEvaluable getLevel() {
    return level;
  }

  public boolean describesEnchantment(FEnchantmentPresenceChecker presenceChecker, IEvaluationEnvironment environment) {
    if (this.enchantment != null) {
      Enchantment bukkitEnchantment = this.enchantment.asEnchantment(environment);
      if (!presenceChecker.apply(bukkitEnchantment, null))
        return false;
    }

    if (this.level != null) {
      if (!presenceChecker.apply(null, this.level.<Long>asScalar(ScalarType.LONG, environment).intValue()))
        return false;
    }

    return true;
  }
}
