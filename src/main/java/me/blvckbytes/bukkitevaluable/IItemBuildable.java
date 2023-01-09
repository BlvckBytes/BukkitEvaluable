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

import me.blvckbytes.bukkitevaluable.section.ItemStackSection;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.inventory.ItemStack;

public interface IItemBuildable {

  /**
   * Build the item by evaluating it's templates in a specific evaluation environment
   * @param environment Environment to use when evaluating the templates
   */
  default ItemStack build(IEvaluationEnvironment environment) {
    throw new UnsupportedOperationException("This buildable does not support environments");
  }

  /**
   * Build the item without support for a templating evaluation environment
   */
  ItemStack build();

  /**
   * Creates a carbon copy of this item which can then be modified
   * without affecting the original instance in any way
   */
  IItemBuildable copy();

  /**
   * Create a patched carbon copy of this item by applying all properties of the
   * provided section according to it's patch flag values
   * @param data Data to use when patching
   * @return Patched carbon copy
   */
  IItemBuildable patch(ItemStackSection data);

}
