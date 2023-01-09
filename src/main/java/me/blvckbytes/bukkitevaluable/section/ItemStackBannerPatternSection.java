package me.blvckbytes.bukkitevaluable.section;

import lombok.Getter;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.jetbrains.annotations.Nullable;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 06/28/2022

  Represents a banner pattern set on a banner item stack.

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
public class ItemStackBannerPatternSection implements IConfigSection {

  private @Nullable BukkitEvaluable pattern;
  private @Nullable BukkitEvaluable color;

  public @Nullable Pattern asPattern(IEvaluationEnvironment environment) {
    PatternType pattern = this.pattern == null ? null : (PatternType) this.pattern.asEnumerationConstant(PatternType.class, environment);
    DyeColor color = this.color == null ? null : (DyeColor) this.pattern.asEnumerationConstant(DyeColor.class, environment);

    // Cannot construct a pattern with missing data
    if (pattern == null || color == null)
      return null;

    return new Pattern(color, pattern);
  }
}
