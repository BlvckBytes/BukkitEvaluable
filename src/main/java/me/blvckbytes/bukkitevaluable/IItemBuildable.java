package me.blvckbytes.bukkitevaluable;

import me.blvckbytes.bukkitevaluable.section.ItemStackSection;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.inventory.ItemStack;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/12/2022

  Represents an ItemStack which can be built for a customizable viewer.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published
  by the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
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
