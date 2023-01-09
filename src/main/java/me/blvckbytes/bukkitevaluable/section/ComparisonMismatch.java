package me.blvckbytes.bukkitevaluable.section;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 10/10/2022

  Represents all available comparison mismatches when checking the description
  an ItemStackSection provides against a bukkit ItemStack.

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
public enum ComparisonMismatch {
  IS_NULL,
  TYPE_MISMATCH,
  AMOUNT_MISMATCH,
  DISPLAY_NAME_MISMATCH,
  LORE_MISMATCH,
  FLAGS_MISMATCH,
  COLOR_MISMATCH,
  ENCHANTMENTS_MISMATCH,
  TEXTURES_MISMATCH,
  BASE_EFFECT_MISMATCH,
  CUSTOM_EFFECTS_MISMATCH
}
