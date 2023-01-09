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

import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface FEnchantmentPresenceChecker {

  /**
   * Checks whether the given enchantment information is present at the
   * target that his checker operates on, where if both values are omitted
   * the function has to return {@code true}. If both the enchantment and
   * the level are available, that enchantment has to be present at the
   * given level. Otherwise, any enchantment of this type or any enchantment
   * with a matching level satisfy the checker.
   * @param enchantment Enchantment to check for
   * @param level Level to check for
   * @return True on matches, false on absence
   */
  boolean apply(@Nullable Enchantment enchantment, @Nullable Integer level);

}
