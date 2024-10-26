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

package me.blvckbytes.bukkitevaluable;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import me.blvckbytes.bbconfigmapper.ConfigValue;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.applicator.EvaluableApplicator;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class BukkitEvaluable extends ConfigValue {

  private static final Map<Class<?>, Map<String, Object>> enumCache;

  static {
    enumCache = new HashMap<>();
  }

  public final EvaluableApplicator applicator;

  public BukkitEvaluable(
    @Nullable Object value,
    @Nullable IExpressionEvaluator evaluator,
    EvaluableApplicator applicator
  ) {
    super(value, evaluator);

    this.applicator = applicator;
  }

  public TextComponent asTextComponent(IEvaluationEnvironment environment) {
    return new TextComponent(asString(environment));
  }

  public @Nullable XMaterial asXMaterial(IEvaluationEnvironment environment) {
    return XMaterial.matchXMaterial(asString(environment)).orElse(null);
  }

  public @Nullable Enchantment asEnchantment(IEvaluationEnvironment environment) {
    XEnchantment xEnchantment = XEnchantment.matchXEnchantment(asString(environment)).orElse(null);
    return xEnchantment == null ? null : xEnchantment.getEnchant();
  }

  public @Nullable PotionType asPotionType(IEvaluationEnvironment environment) {
    String stringValue = asString(environment);

    // Try to get the abstracted type
    Optional<XPotion> xPotion = XPotion.matchXPotion(stringValue);
    if (xPotion.isPresent())
      return xPotion.get().getPotionType();

    // Otherwise, try to parse directly
    // Useful for types like AWKWARD, MUNDANE, ...
    try {
      return PotionType.valueOf(stringValue.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ignored) {}

    return null;
  }

  public @Nullable PotionEffectType asPotionEffectType(IEvaluationEnvironment environment) {
    XPotion potion = XPotion.matchXPotion(asString(environment)).orElse(null);
    return potion == null ? null : potion.getPotionEffectType();
  }

  public @Nullable java.awt.Color asJavaColor(IEvaluationEnvironment environment) {
    String stringValue = asString(environment);
    return parseRGBNotation(stringValue, java.awt.Color::new);
  }

  public @Nullable Color asBukkitColor(IEvaluationEnvironment environment) {
    String stringValue = asString(environment);

    // Try to parse an enum name
    Color enumValue = parseEnum(Color.class, stringValue);
    if (enumValue != null)
      return enumValue;

    return parseRGBNotation(stringValue, Color::fromRGB);
  }

  private <T> @Nullable T parseRGBNotation(String value, FUnsafeTriFunction<Integer, Integer, Integer, T, Exception> parser) {
    // Assume it's an RGB color
    String[] parts = value.split(" ");

    // Malformed
    if (parts.length != 3)
      return null;

    try {
      // Parse RGB parts
      return parser.apply(
        Integer.parseInt(parts[0]),
        Integer.parseInt(parts[1]),
        Integer.parseInt(parts[2])
      );
    } catch (Exception e) {
      return null;
    }
  }

  public <T> @Nullable T asEnumerationConstant(Class<T> enumerationClass, IEvaluationEnvironment environment) {
    return parseEnum(enumerationClass, asString(environment));
  }

  public <T> Set<T> asEnumerationConstantSet(Class<T> enumerationClass, IEvaluationEnvironment environment) {
    Set<String> values = asSet(ScalarType.STRING, environment);
    Set<T> constants = new HashSet<>();

    for (String value : values) {
      T constant = parseEnum(enumerationClass, value);

      if (constant == null)
        continue;

      constants.add(constant);
    }

    return constants;
  }

  //=========================================================================//
  //                                Utilities                                //
  //=========================================================================//

  private String asString(IEvaluationEnvironment environment) {
    return asScalar(ScalarType.STRING, environment);
  }

  //=========================================================================//
  //                               Enum Lookup                               //
  //=========================================================================//

  /**
   * Parses an "enum" from either true enum constants or static self-typed
   * constant declarations within the specified class
   * @param type Target type class
   * @param value String value to parse
   * @return Optional constant, empty if there was no such constant
   */
  @SuppressWarnings("unchecked")
  private static <T> @Nullable T parseEnum(Class<T> type, String value) {
    value = value.trim().toLowerCase(Locale.ROOT);

    Optional<Object> cacheLookup = performEnumCacheLookup(type, value);
    Object result = null;

    if (cacheLookup.isPresent())
      return (T) cacheLookup.get();

    // Parse enums
    if (type.isEnum()) {
      for (T enumConstant : type.getEnumConstants()) {
        if (!((Enum<?>) enumConstant).name().toLowerCase().equals(value))
          continue;

        result = enumConstant;
        break;
      }
    }

    // Parse classes with static constants
    else {
      try {
        List<Field> constants = Arrays.stream(type.getDeclaredFields())
          .filter(field -> field.getType().equals(type) && Modifier.isStatic(field.getModifiers()))
          .toList();

        for (Field constant : constants) {
          if (!constant.getName().toLowerCase().equals(value))
            continue;

          result = constant.get(null);
          break;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    if (result != null)
      cacheEnumLookup(type, value, result);

    return (T) result;
  }

  //=========================================================================//
  //                                Applicator                               //
  //=========================================================================//

  // Convenient relays, as to not have to provide the instance
  // Not necessarily clean design, but I just want to move on for the time being...

  public void setDisplayName(ItemMeta meta, IEvaluationEnvironment environment) {
    applicator.setDisplayName(meta, this, environment);
  }

  public void setLore(ItemMeta meta, IEvaluationEnvironment environment, boolean override) {
    applicator.setLore(meta, this, environment, override);
  }

  public void sendMessage(CommandSender receiver, IEvaluationEnvironment environment) {
    applicator.sendMessage(receiver, this, environment);
  }

  private static Optional<Object> performEnumCacheLookup(Class<?> type, String value) {
    Map<String, Object> constantCache = enumCache.get(type);

    if (constantCache != null && constantCache.containsKey(value))
      return Optional.of(constantCache.get(value));

    return Optional.empty();
  }

  private static void cacheEnumLookup(Class<?> type, String value, Object constant) {
    enumCache.computeIfAbsent(type, key -> new HashMap<>()).put(value, constant);
  }
}
