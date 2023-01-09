package me.blvckbytes.bukkitevaluable.section;

import lombok.Getter;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 06/28/2022

  Represents a custom effect applied to a potion item stack.

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
public class ItemStackCustomEffectSection implements IConfigSection {

  private @Nullable BukkitEvaluable effect;
  private @Nullable BukkitEvaluable duration;
  private @Nullable BukkitEvaluable amplifier;
  private @Nullable Boolean ambient;
  private @Nullable Boolean particles;
  private @Nullable Boolean icon;

  public @Nullable PotionEffect asEffect(IEvaluationEnvironment environment) {
    // Cannot create an effect object without the effect itself
    PotionEffectType type = this.effect == null ? null : this.effect.asPotionEffectType(environment);

    if (type == null || duration == null)
      return null;

    Integer amplifier = this.amplifier == null ? null : this.amplifier.<Long>asScalar(ScalarType.LONG, environment).intValue();
    Integer duration = this.duration == null ? null : this.duration.<Long>asScalar(ScalarType.LONG, environment).intValue();

    return new PotionEffect(
      // Default to a minute of duration
      type, duration == null ? 20 * 60 : duration,
      // Default to no amplifier
      amplifier == null ? 0 : amplifier,
      // Default boolean flags to false
      ambient != null && ambient,
      particles != null && particles,
      icon != null && icon
    );
  }

  public boolean describesEffect(@Nullable PotionEffect effect, IEvaluationEnvironment environment) {
    if (effect == null)
      return false;

    PotionEffectType type = this.effect == null ? null : this.effect.asPotionEffectType(environment);
    if (type != null && effect.getType() != type)
      return false;

    Integer duration = this.duration == null ? null : this.duration.<Long>asScalar(ScalarType.LONG, environment).intValue();
    if (duration != null && effect.getDuration() != duration)
      return false;

    Integer amplifier = this.amplifier == null ? null : this.amplifier.<Long>asScalar(ScalarType.LONG, environment).intValue();
    if (amplifier != null && effect.getAmplifier() != amplifier)
      return false;

    if (this.ambient != null && effect.isAmbient() != this.ambient)
      return false;

    if (this.particles != null && effect.hasParticles() != this.particles)
      return false;

    if (this.icon != null && effect.hasIcon() != this.icon)
      return false;

    // All checks passed
    return true;
  }
}
