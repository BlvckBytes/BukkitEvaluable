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
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class ItemStackCustomEffectSection extends AConfigSection {

  private @Nullable BukkitEvaluable effect;
  private @Nullable BukkitEvaluable duration;
  private @Nullable BukkitEvaluable amplifier;
  private @Nullable Boolean ambient;
  private @Nullable Boolean particles;
  private @Nullable Boolean icon;

  public ItemStackCustomEffectSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

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

  public boolean describesEffect(PotionEffect effect, IEvaluationEnvironment environment) {
    if (this.effect != null) {
      PotionEffectType type = this.effect.asPotionEffectType(environment);
      if (type != null && type != effect.getType())
        return false;
    }

    if (this.duration != null) {
      if (this.duration.<Long>asScalar(ScalarType.LONG, environment).intValue() != effect.getDuration())
        return false;
    }

    if (this.amplifier != null) {
      if (this.amplifier.<Long>asScalar(ScalarType.LONG, environment).intValue() != effect.getDuration())
        return false;
    }

    if (this.ambient != null && effect.isAmbient() != this.ambient)
      return false;

    if (this.particles != null && effect.isAmbient() != this.particles)
      return false;

    return this.icon == null || this.icon == effect.hasIcon();
  }
}
