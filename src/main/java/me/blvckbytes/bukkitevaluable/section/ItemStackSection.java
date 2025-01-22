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

import com.cryptomorin.xseries.XMaterial;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.EPatchFlag;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.bukkitevaluable.ItemBuilder;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemStackSection extends AConfigSection {

  private @Nullable BukkitEvaluable amount;
  private @Nullable BukkitEvaluable type;

  private @Nullable BukkitEvaluable name;
  private boolean disallowName;

  private @Nullable BukkitEvaluable lore;
  private boolean disallowLore;

  private @Nullable BukkitEvaluable flags;
  private MatchingMode flagsMatchingMode;
  private boolean disallowFlags;

  private @Nullable BukkitEvaluable color;

  private ItemStackEnchantmentSection @Nullable [] enchantments;
  private MatchingMode enchantmentsMatchingMode;
  private boolean disallowEnchantments;

  private @Nullable BukkitEvaluable textures;

  private @Nullable ItemStackBaseEffectSection baseEffect;

  private ItemStackCustomEffectSection @Nullable [] customEffects;
  private MatchingMode customEffectsMatchingMode;
  private boolean disallowCustomEffects;

  private ItemStackBannerPatternSection @Nullable [] bannerPatterns;
  private MatchingMode bannerPatternsMatchingMode;
  private boolean disallowBannerPatterns;

  private @CSAlways List<EPatchFlag> patchFlags;

  public ItemStackSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    flagsMatchingMode = MatchingMode.HAS_AT_LEAST;
    enchantmentsMatchingMode = MatchingMode.HAS_AT_LEAST;
    customEffectsMatchingMode = MatchingMode.HAS_AT_LEAST;
    bannerPatternsMatchingMode = MatchingMode.HAS_AT_LEAST;
  }

  /**
   * Create an item stack builder from the parameters of this section
   */
  public IItemBuildable asItem() {
    return new ItemBuilder(new ItemStack(Material.BARRIER), 1)
      .setType(type)
      .setAmount(amount)
      .extendBannerPatterns(bannerPatterns)
      .setName(name)
      .extendLore(lore)
      .extendFlags(flags)
      .extendEnchantments(enchantments)
      .setColor(color)
      .setTextures(textures)
      .setBaseEffect(baseEffect)
      .extendCustomEffects(customEffects);
  }

  public @Nullable BukkitEvaluable getAmount() {
    return amount;
  }

  public @Nullable BukkitEvaluable getType() {
    return type;
  }

  public @Nullable BukkitEvaluable getName() {
    return name;
  }

  public @Nullable BukkitEvaluable getLore() {
    return lore;
  }

  public @Nullable BukkitEvaluable getFlags() {
    return flags;
  }

  public @Nullable BukkitEvaluable getColor() {
    return color;
  }

  public ItemStackEnchantmentSection[] getEnchantments() {
    return enchantments;
  }

  public @Nullable BukkitEvaluable getTextures() {
    return textures;
  }

  public ItemStackBaseEffectSection getBaseEffect() {
    return baseEffect;
  }

  public ItemStackBannerPatternSection[] getBannerPatterns() {
    return bannerPatterns;
  }

  public ItemStackCustomEffectSection[] getCustomEffects() {
    return customEffects;
  }

  public List<EPatchFlag> getPatchFlags() {
    return patchFlags;
  }

  private boolean addMismatchAndPossiblyBreak(
    ComparisonMismatch mismatch,
    Set<ComparisonMismatch> mismatches,
    @Nullable Set<ComparisonMismatch> nonBreakers
  ) {
    mismatches.add(mismatch);
    return nonBreakers == null || !nonBreakers.contains(mismatch);
  }

  /**
   * Checks whether this section describes the provided item by testing the item against
   * each of the available properties and returning a set of comparison mismatches
   * @param item Item in question
   * @param nonBreakers Optional set of mismatches which do not early-return
   * @param environment Environment to evaluate this description in
   * @return Set of mismatches, if it's empty, the description matched
   */
  public Set<ComparisonMismatch> describesItem(
    @Nullable ItemStack item,
    @Nullable Set<ComparisonMismatch> nonBreakers,
    IEvaluationEnvironment environment
  ) {
    Set<ComparisonMismatch> mismatches = new HashSet<>();

    if (item == null) {
      mismatches.add(ComparisonMismatch.IS_NULL);
      return mismatches;
    }

    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      mismatches.add(ComparisonMismatch.META_UNREADABLE);
      return mismatches;
    }

    if (type != null) {
      XMaterial material = type.asXMaterial(environment);
      if (material != null && !material.isSimilar(item)) {
        if (addMismatchAndPossiblyBreak(ComparisonMismatch.TYPE_MISMATCH, mismatches, nonBreakers))
          return mismatches;
      }
    }

    if (amount != null) {
      if (amount.<Long>asScalar(ScalarType.LONG, environment).intValue() != item.getAmount()) {
        if (addMismatchAndPossiblyBreak(ComparisonMismatch.AMOUNT_MISMATCH, mismatches, nonBreakers))
          return mismatches;
      }
    }

    if (disallowName && meta.hasDisplayName()) {
      if (addMismatchAndPossiblyBreak(ComparisonMismatch.DISPLAY_NAME_MISMATCH, mismatches, nonBreakers))
        return mismatches;
    }

    if (name != null) {
      if (!name.asScalar(ScalarType.STRING, environment).equals(meta.getDisplayName())) {
        if (addMismatchAndPossiblyBreak(ComparisonMismatch.DISPLAY_NAME_MISMATCH, mismatches, nonBreakers))
          return mismatches;
      }
    }

    if (disallowLore && meta.hasLore()) {
      if (addMismatchAndPossiblyBreak(ComparisonMismatch.LORE_MISMATCH, mismatches, nonBreakers))
        return mismatches;
    }

    if (lore != null) {
      if (doCollectionsDiffer(lore.asList(ScalarType.STRING, environment), meta.getLore())) {
        if (addMismatchAndPossiblyBreak(ComparisonMismatch.LORE_MISMATCH, mismatches, nonBreakers))
          return mismatches;
      }
    }

    if (!areFlagsSatisfied(meta, environment)) {
      if (addMismatchAndPossiblyBreak(ComparisonMismatch.FLAGS_MISMATCH, mismatches, nonBreakers))
        return mismatches;
    }

    if (!isColorSatisfied(meta, environment)) {
      if (addMismatchAndPossiblyBreak(ComparisonMismatch.COLOR_MISMATCH, mismatches, nonBreakers))
        return mismatches;
    }

    if (!areEnchantmentsSatisfied(meta, environment)) {
      if (addMismatchAndPossiblyBreak(ComparisonMismatch.ENCHANTMENTS_MISMATCH, mismatches, nonBreakers))
        return mismatches;
    }

    if (!areTexturesSatisfied(meta, environment)) {
      if (addMismatchAndPossiblyBreak(ComparisonMismatch.TEXTURES_MISMATCH, mismatches, nonBreakers))
        return mismatches;
    }

    if (!isBaseEffectSatisfied(meta, environment)) {
      if (addMismatchAndPossiblyBreak(ComparisonMismatch.BASE_EFFECT_MISMATCH, mismatches, nonBreakers))
        return mismatches;
    }

    if (!areCustomEffectsSatisfied(meta, environment)) {
      if (addMismatchAndPossiblyBreak(ComparisonMismatch.CUSTOM_EFFECTS_MISMATCH, mismatches, nonBreakers))
        return mismatches;
    }

    if (!areBannerPatternsSatisfied(meta, environment)) {
      if (addMismatchAndPossiblyBreak(ComparisonMismatch.BANNER_PATTERNS_MISMATCH, mismatches, nonBreakers))
        return mismatches;
    }

    return mismatches;
  }

  private boolean areFlagsSatisfied(ItemMeta meta, IEvaluationEnvironment environment) {
    var metaFlags = meta.getItemFlags();

    if (disallowFlags && !metaFlags.isEmpty())
      return false;

    if (flags == null)
      return true;

    var sectionFlags = flags.asEnumerationConstantSet(ItemFlag.class, environment);

    switch (flagsMatchingMode) {
      case HAS_NOT -> {
        for (var sectionFlag : sectionFlags) {
          if (metaFlags.contains(sectionFlag))
            return false;
        }
      }
      case HAS_AT_LEAST, HAS_EXACT -> {
        for (var sectionFlag : sectionFlags) {
          if (!metaFlags.contains(sectionFlag))
            return false;
        }

        if (flagsMatchingMode == MatchingMode.HAS_EXACT) {
          if (metaFlags.size() != sectionFlags.size())
            return false;
        }
      }
    }

    return true;
  }

  private boolean areBannerPatternsSatisfied(ItemMeta meta, IEvaluationEnvironment environment) {
    if (disallowBannerPatterns && meta instanceof BannerMeta bannerMeta && !bannerMeta.getPatterns().isEmpty())
      return false;

    if (bannerPatterns == null)
      return true;

    if (!(meta instanceof BannerMeta bannerMeta))
      return false;

    switch (bannerPatternsMatchingMode) {
      case HAS_NOT -> {
        for (ItemStackBannerPatternSection patternSection : bannerPatterns) {
          if (patternSection.isContainedByMeta(bannerMeta, environment))
            return false;
        }
      }
      case HAS_AT_LEAST, HAS_EXACT -> {
        for (ItemStackBannerPatternSection patternSection : bannerPatterns) {
          if (!patternSection.isContainedByMeta(bannerMeta, environment))
            return false;
        }

        if (bannerPatternsMatchingMode == MatchingMode.HAS_EXACT) {
          if (bannerMeta.getPatterns().size() != bannerPatterns.length)
            return false;
        }
      }
    }

    return true;
  }

  private boolean areCustomEffectsSatisfied(ItemMeta meta, IEvaluationEnvironment environment) {
    if (disallowCustomEffects && meta instanceof PotionMeta potionMeta && potionMeta.hasCustomEffects())
      return false;

    if (customEffects == null)
      return true;

    if (!(meta instanceof PotionMeta potionMeta))
      return false;

    switch (customEffectsMatchingMode) {
      case HAS_NOT -> {
        for (ItemStackCustomEffectSection effectSection : customEffects) {
          if (effectSection.isContainedByMeta(potionMeta, environment))
            return false;
        }
      }
      case HAS_AT_LEAST, HAS_EXACT -> {
        for (ItemStackCustomEffectSection effectSection : customEffects) {
          if (effectSection.isContainedByMeta(potionMeta, environment))
            return false;
        }

        if (customEffectsMatchingMode == MatchingMode.HAS_EXACT) {
          if (potionMeta.getCustomEffects().size() != customEffects.length)
            return false;
        }
      }
    }

    return true;
  }

  private boolean isColorSatisfied(ItemMeta meta, IEvaluationEnvironment environment) {
    if (color == null)
      return true;

    Color bukkitColor = color.asBukkitColor(environment);

    if (color == null)
      return true;

    // FIXME: In order to support future added meta types with colors, this should be accessed using reflect, as all methods have the same signature

    if (meta instanceof PotionMeta)
      return ((PotionMeta) meta).getColor().equals(bukkitColor);

    else if (meta instanceof LeatherArmorMeta)
      return ((LeatherArmorMeta) meta).getColor().equals(bukkitColor);

    else if (meta instanceof MapMeta)
      return ((MapMeta) meta).getColor().equals(bukkitColor);

    return false;
  }

  private boolean areEnchantmentsSatisfied(ItemMeta meta, IEvaluationEnvironment environment) {
    if (disallowEnchantments && meta.hasEnchants())
      return false;

    if (enchantments == null)
      return true;

    switch (enchantmentsMatchingMode) {
      case HAS_NOT -> {
        for (ItemStackEnchantmentSection enchantmentSection : enchantments) {
          if (enchantmentSection.isContainedByMeta(meta,environment) == CheckResult.MATCHING_SECTION)
            return false;
        }
      }

      case HAS_AT_LEAST, HAS_EXACT -> {
        var validSectionCount = 0;

        for (ItemStackEnchantmentSection enchantmentSection : enchantments) {
          var result = enchantmentSection.isContainedByMeta(meta,environment);

          if (enchantmentSection.isContainedByMeta(meta,environment) != CheckResult.MATCHING_SECTION)
            return false;

          if (result != CheckResult.INVALID_SECTION)
            ++validSectionCount;
        }

        if (enchantmentsMatchingMode == MatchingMode.HAS_EXACT) {
          if (validSectionCount != meta.getEnchants().size())
            return false;
        }
      }
    }

    return true;
  }

  private boolean isBaseEffectSatisfied(ItemMeta meta, IEvaluationEnvironment environment) {
    if (baseEffect == null)
      return true;

    if (!(meta instanceof PotionMeta))
      return false;

    return baseEffect.describesData(((PotionMeta) meta).getBasePotionData(), environment);
  }

  private boolean areTexturesSatisfied(ItemMeta meta, IEvaluationEnvironment environment) {
    if (textures == null)
      return true;

    // Rather fail safely if the handler is not available
    if (ItemBuilder.texturesHandler == null)
      return false;

    String itemTextures = ItemBuilder.texturesHandler.getBase64Textures(meta);
    String envTextures = textures.asScalar(ScalarType.STRING, environment);

    return envTextures.equals(itemTextures);
  }

  /**
   * Checks whether two collections contain the exact same items
   * @param a Collection A
   * @param b Collection B
   */
  private <T> boolean doCollectionsDiffer(@Nullable Collection<T> a, @Nullable Collection<T> b) {
    if (a == null && b == null)
      return false;

    if (a == null || b == null)
      return true;

    if (a.size() != b.size())
      return true;

    return !a.containsAll(b);
  }
}
