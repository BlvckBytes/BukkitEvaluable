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

import lombok.Getter;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

@Getter
public class ItemStackBaseEffectSection implements IConfigSection {

  private @Nullable BukkitEvaluable type;
  private @Nullable Boolean extended;
  private @Nullable Boolean upgraded;

  public @Nullable PotionData asData(IEvaluationEnvironment environment) {
    boolean _upgraded = upgraded != null && upgraded;
    boolean _extended = extended != null && extended;

    PotionType type = this.type == null ? null : this.type.asPotionType(environment);

    if (type == null)
      return null;

    // Potions cannot be both extended and upgraded at the same
    // time, focus the priority on the upgraded flag
    return new PotionData(type, !_upgraded && _extended, _upgraded);
  }

  public boolean describesData(PotionData data, IEvaluationEnvironment environment) {
    if (this.type != null) {
      PotionType potionType = this.type.asPotionType(environment);
      if (potionType != null && !potionType.equals(data.getType()))
        return false;
    }

    if (this.extended != null) {
      if (this.extended != data.isExtended())
        return false;
    }

    if (this.upgraded != null) {
      if (this.upgraded != data.isExtended())
        return false;
    }

    return true;
  }
}
