package me.blvckbytes.bukkitevaluable.section;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.Getter;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.EPatchFlag;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.bukkitevaluable.ItemBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 06/28/2022

  Represents the properties of a fully describeable item stack.

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
public class ItemStackSection implements IConfigSection {

  private @Nullable BukkitEvaluable amount;
  private @Nullable BukkitEvaluable type;
  private @Nullable BukkitEvaluable name;
  private @Nullable BukkitEvaluable lore;
  private @Nullable BukkitEvaluable flags;
  private @Nullable BukkitEvaluable color;
  private @Nullable ItemStackEnchantmentSection[] enchantments;
  private @Nullable BukkitEvaluable textures;
  private @Nullable ItemStackBaseEffectSection baseEffect;
  private @Nullable ItemStackCustomEffectSection[] customEffects;
  private @Nullable ItemStackBannerPatternSection[] bannerPatterns;
  private @Nullable List<EPatchFlag> patchFlags;

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

  // FIXME: What a mess... clean this up!

  public @Nullable Set<ComparisonMismatch> describesItem(@Nullable ItemStack item, @Nullable Set<ComparisonMismatch> nonBreakers, IEvaluationEnvironment environment) {
    Set<ComparisonMismatch> results = new HashSet<>();

    if (item == null) {
      results.add(ComparisonMismatch.IS_NULL);
      return results;
    }

    XMaterial m = type == null ? null : type.asXMaterial(environment);

    if (m != null && !m.isSimilar(item)) {
      results.add(ComparisonMismatch.TYPE_MISMATCH);

      if (nonBreakers == null || !nonBreakers.contains(ComparisonMismatch.TYPE_MISMATCH))
        return results;
    }

    if (amount != null) {
      int am = amount.<Long>asScalar(ScalarType.LONG, environment).intValue();
      if (am != item.getAmount()) {
        results.add(ComparisonMismatch.AMOUNT_MISMATCH);

        if (nonBreakers == null || !nonBreakers.contains(ComparisonMismatch.AMOUNT_MISMATCH))
          return results;
      }
    }

    // Compare display name
    if (!checkMeta(item, name, meta -> {
      assert name != null;
      return name.asScalar(ScalarType.STRING, environment).equals(meta.getDisplayName());
    })) {
      results.add(ComparisonMismatch.DISPLAY_NAME_MISMATCH);

      if (nonBreakers == null || !nonBreakers.contains(ComparisonMismatch.DISPLAY_NAME_MISMATCH))
        return results;
    }

    // Compare lore lines for equality (and order)
    if (!checkMeta(item, lore, meta -> {
      assert lore != null;
      return lore.asList(ScalarType.STRING, environment).equals(meta.getLore());
    })) {
      results.add(ComparisonMismatch.LORE_MISMATCH);

      if (nonBreakers == null || !nonBreakers.contains(ComparisonMismatch.LORE_MISMATCH))
        return results;
    }

    // Compare flag entries for equality (ignoring order)
    if (!checkMeta(item, flags, meta -> {
      assert flags != null;
      return flags.asEnumerationConstantSet(ItemFlag.class, environment).equals(meta.getItemFlags());
    })) {
      results.add(ComparisonMismatch.FLAGS_MISMATCH);

      if (nonBreakers == null || !nonBreakers.contains(ComparisonMismatch.FLAGS_MISMATCH))
        return results;
    }

    // Compare either potion color or leather color
    if (!checkMeta(item, color, meta -> {
      assert color != null;
      Color bukkitColor = color.asColor(environment);

      if (meta instanceof PotionMeta)
        return ((PotionMeta) meta).getColor().equals(bukkitColor);

      if (meta instanceof LeatherArmorMeta) {

        return ((LeatherArmorMeta) meta).getColor().equals(bukkitColor);
      }

      // Not color-able
      return false;
    })) {
      results.add(ComparisonMismatch.COLOR_MISMATCH);

      if (nonBreakers == null || !nonBreakers.contains(ComparisonMismatch.COLOR_MISMATCH))
        return results;
    }

    // Check for the presence of all enchantments at the right levels (ignoring order)
    if (!checkMeta(item, enchantments, meta -> {
      for (ItemStackEnchantmentSection ench : enchantments) {
        Enchantment e = ench.getEnchantment() == null ? null : ench.getEnchantment().asEnchantment(environment);

        // Cannot compare
        if (e == null)
          continue;

        Integer level = ench.getLevel() == null ? null : ench.getLevel().<Long>asScalar(ScalarType.LONG, environment).intValue();

        if (!(
          // Contains this enchantment at any levej
          meta.hasEnchant(e) &&
          // Contains at a matching level, if required
          (level == null || meta.getEnchantLevel(e) == level)
        ))
          return false;
      }
      // All enchantments matched
      return true;
    })) {
      results.add(ComparisonMismatch.ENCHANTMENTS_MISMATCH);

      if (nonBreakers == null || !nonBreakers.contains(ComparisonMismatch.ENCHANTMENTS_MISMATCH))
        return results;
    }

    // Compare for head textures
    if (!checkMeta(item, textures, meta -> {
      // Not a skull
      if (!(meta instanceof SkullMeta))
        return false;

      SkullMeta sm = (SkullMeta) meta;
      OfflinePlayer owner = sm.getOwningPlayer();

      // Has no head owner
      if (owner == null)
        return false;

      try {
        Field profileField = meta.getClass().getDeclaredField("profile");
        profileField.setAccessible(true);
        GameProfile profile = (GameProfile) profileField.get(sm);
        PropertyMap pm = profile.getProperties();
        Collection<Property> targets = pm.get("textures");

        // Does not contain any textures
        if (targets.size() == 0)
          return false;

        assert textures != null;
        String texturesValue = textures.asScalar(ScalarType.STRING, environment);
        return targets.stream().anyMatch(prop -> prop.getValue().equals(texturesValue));
      } catch (Exception ignored) {}

      return false;
    })) {
      results.add(ComparisonMismatch.TEXTURES_MISMATCH);

      if (nonBreakers == null || !nonBreakers.contains(ComparisonMismatch.TEXTURES_MISMATCH))
        return results;
    }

    // Compare the base potion effect
    if (!checkMeta(item, baseEffect, meta -> {
      // Not a potion
      if (!(meta instanceof PotionMeta))
        return false;

      assert baseEffect != null;
      return baseEffect.describesData(((PotionMeta) meta).getBasePotionData(), environment);
    })) {
      results.add(ComparisonMismatch.BASE_EFFECT_MISMATCH);

      if (nonBreakers == null || !nonBreakers.contains(ComparisonMismatch.BASE_EFFECT_MISMATCH))
        return results;
    }

    // Check for the presence of all custom effects (ignoring order)
    if (!checkMeta(item, customEffects, meta -> {
      // Nothing to compare
      if (customEffects.length == 0)
        return true;

      if (!(meta instanceof PotionMeta))
        return false;

      for (ItemStackCustomEffectSection effectSection : customEffects) {

        boolean anyMatched = false;
        for (PotionEffect customEffect : ((PotionMeta) meta).getCustomEffects()) {
          if (!effectSection.describesEffect(customEffect, environment))
            continue;

          anyMatched = true;
          break;
        }

        // Current custom effect is not represented within the custom effects of the potion
        if (!anyMatched)
          return false;
      }

      // All effects present
      return true;
    })) {
      results.add(ComparisonMismatch.CUSTOM_EFFECTS_MISMATCH);

      if (nonBreakers == null || !nonBreakers.contains(ComparisonMismatch.CUSTOM_EFFECTS_MISMATCH))
        return results;
    }

    // TODO: Compare banner patterns

    return results.size() > 0 ? results : null;
  }

  /**
   * Check a local parameter against the item's metadata. Whenever the
   * local value is null, this function returns true, and if it's present
   * but there's no metadata or the metadata property mismatches, it returns false
   * @param item Item to check the meta of
   * @param value Local parameter to use
   * @param checker Checker function
   */
  private boolean checkMeta(ItemStack item, @Nullable Object value, Function<ItemMeta, Boolean> checker) {
    // Value not present, basically a wildcard
    if (value == null)
      return true;

    // Fails if there is either no meta to compare against at all or if the checker failed
    return item.getItemMeta() != null && checker.apply(item.getItemMeta());
  }
}
