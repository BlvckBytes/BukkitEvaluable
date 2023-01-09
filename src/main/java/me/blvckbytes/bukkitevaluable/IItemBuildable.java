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

  default ItemStack build(IEvaluationEnvironment environment) {
    throw new UnsupportedOperationException("This buildable does not support templating");
  }

  ItemStack build();

  IItemBuildable copy();

  IItemBuildable patch(ItemStackSection data);

}
