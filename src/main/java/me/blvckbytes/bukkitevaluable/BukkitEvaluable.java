package me.blvckbytes.bukkitevaluable;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import me.blvckbytes.bbconfigmapper.ConfigValue;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class BukkitEvaluable extends ConfigValue {

  private static final Map<Class<?>, Map<String, Object>> enumCache;

  static {
    enumCache = new HashMap<>();
  }

  public BukkitEvaluable(@Nullable Object value, IExpressionEvaluator evaluator) {
    super(value, evaluator);
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

  public @Nullable Color asColor(IEvaluationEnvironment environment) {
    String stringValue = asString(environment);

    // Try to parse an enum name
    Color enumValue = parseEnum(Color.class, stringValue);
    if (enumValue != null)
      return enumValue;

    // Assume it's an RGB color
    String[] parts = stringValue.split(" ");

    // Malformed
    if (parts.length != 3)
      return null;

    try {
      // Parse RGB parts
      return Color.fromRGB(
        Integer.parseInt(parts[0]),
        Integer.parseInt(parts[1]),
        Integer.parseInt(parts[2])
      );
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public @Nullable Object asEnumerationConstant(Class<?> enumerationClass, IEvaluationEnvironment environment) {
    return parseEnum(enumerationClass, asString(environment));
  }

  public Set<Object> asEnumerationConstantSet(Class<?> enumerationClass, IEvaluationEnvironment environment) {
    Set<String> values = asSet(ScalarType.STRING, environment);
    Set<Object> constants = new HashSet<>();

    for (String value : values) {
      Object constant = parseEnum(enumerationClass, value);

      if (constant == null)
        continue;

      constants.add(constant);
    }

    return constants;
  }

  //=========================================================================//
  //                                Overrides                                //
  //=========================================================================//

  @Override
  @SuppressWarnings("unchecked")
  protected <T> T interpretScalar(@Nullable Object input, ScalarType type, IEvaluationEnvironment env) {
    T scalar = super.interpretScalar(input, type, env);

    if (scalar instanceof String)
      scalar = (T) ChatColor.translateAlternateColorCodes('&', (String) scalar);

    return scalar;
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
          .collect(Collectors.toList());

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

    cacheEnumLookup(type, value, result);
    return (T) result;
  }

  private static Optional<Object> performEnumCacheLookup(Class<?> type, String value) {
    Map<String, Object> constantCache = enumCache.get(type);

    if (constantCache.containsKey(value))
      return Optional.of(constantCache.get(value));

    return Optional.empty();
  }

  private static void cacheEnumLookup(Class<?> type, String value, Object constant) {
    enumCache.computeIfAbsent(type, key -> new HashMap<>()).put(value, constant);
  }
}
