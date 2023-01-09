package me.blvckbytes.bukkitevaluable.section;

import lombok.Getter;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.Tuple;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 06/28/2022

  Represents an enchantment applied to an item stack.

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
@Getter
public class ItemStackEnchantmentSection implements IConfigSection {

  private @Nullable BukkitEvaluable enchantment;
  private @Nullable BukkitEvaluable level;

  public @Nullable Tuple<Enchantment, Integer> asEnchantment(IEvaluationEnvironment environment) {
    // Try to parse the enchantment, if provided
    Enchantment enchantment = this.enchantment == null ? null : this.enchantment.asEnchantment(environment);

    // No enchantment provided
    if (enchantment == null)
      return null;

    Integer level = this.level == null ? null : this.level.<Long>asScalar(ScalarType.LONG, environment).intValue();

    // Fall back to level 1 if not provided
    return Tuple.of(enchantment, level == null ? 1 : level);
  }

  public boolean describesEnchantment(Enchantment enchantment, int level, IEvaluationEnvironment environment) {
    if (this.enchantment != null) {
      Enchantment bukkitEnchantment = this.enchantment.asEnchantment(environment);
      if (bukkitEnchantment != null && !bukkitEnchantment.equals(enchantment))
        return false;
    }

    if (this.level != null) {
      if (this.level.<Long>asScalar(ScalarType.LONG, environment).intValue() != level)
        return false;
    }

    return true;
  }
}
