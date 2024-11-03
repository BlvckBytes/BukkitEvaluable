package me.blvckbytes.bukkitevaluable.applicator;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/*
  By obtaining bukkit's CraftChatMessage-utility, the classes for IChatBaseComponent as well as
  the ChatSerializer are easily accessible too, without having to decide based on the server-version.

  ChatSerializer#fromJson(JsonElement, Registry) is a public static method, thus, for efficiency,
  a MethodHandle is obtained, instead of accessing it via reflection.
 */
public abstract class EvaluableApplicatorBase implements EvaluableApplicator {

  private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

  private final Object minecraftRegistry;
  private final MethodHandle chatComponentFromJsonElement;

  protected EvaluableApplicatorBase() throws Exception {
    var bukkitPackage = Bukkit.getServer().getClass().getPackageName();

    var craftRegistryClass = Class.forName(bukkitPackage + ".CraftRegistry");
    var getRegistryMethod = locateMethodByNameTransitively(craftRegistryClass, "getMinecraftRegistry", 0);

    if (getRegistryMethod == null)
      throw new IllegalStateException("Could not locate CraftRegistry#getMinecraftRegistry");

    minecraftRegistry = getRegistryMethod.invoke(null);

    if (minecraftRegistry == null)
      throw new IllegalStateException("Could not retrieve the minecraft-registry by invoking CraftRegistry#getMinecraftRegistry");

    var craftChatMessageClass = Class.forName(bukkitPackage + ".util.CraftChatMessage");
    var fromJsonMethod = locateMethodByNameTransitively(craftChatMessageClass, "fromJSON", 1);

    if (fromJsonMethod == null)
      throw new IllegalStateException("Could not locate " + craftChatMessageClass.getName() + "#fromJSON");

    var chatComponentClass = fromJsonMethod.getReturnType();


    Class<?> chatSerializerClass = null;

    // Sub-Class: IChatBaseComponent#ChatSerializer
    for (var declaredClass : chatComponentClass.getDeclaredClasses()) {
      var modifiers = declaredClass.getModifiers();

      if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)))
        continue;

      if (JsonSerializer.class.isAssignableFrom(declaredClass))
        continue;

      chatSerializerClass = declaredClass;
      break;
    }

    if (chatSerializerClass == null)
      throw new IllegalStateException("Could not locate the ChatSerializer-class");

    Method _deserializeJsonMethod = null;

    for (var declaredMethod : chatSerializerClass.getDeclaredMethods()) {
      var modifiers = declaredMethod.getModifiers();

      if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)))
        continue;

      if (!chatComponentClass.isAssignableFrom(declaredMethod.getReturnType()))
        continue;

      if (!(declaredMethod.getParameterCount() > 0 && declaredMethod.getParameterTypes()[0] == JsonElement.class))
        continue;

      _deserializeJsonMethod = declaredMethod;
      break;
    }

    if (_deserializeJsonMethod == null)
      throw new IllegalStateException("Could not locate deserialize-method");

    // Lookup MethodHandle based on the information gathered by reflection
    MethodType deserializeJsonMethodType = MethodType.methodType(_deserializeJsonMethod.getReturnType(), _deserializeJsonMethod.getParameterTypes());
    chatComponentFromJsonElement = PUBLIC_LOOKUP.findStatic(_deserializeJsonMethod.getDeclaringClass(), _deserializeJsonMethod.getName(), deserializeJsonMethodType);
  }

  public @Nullable Object createChatComponent(JsonElement json) throws Throwable {
    return chatComponentFromJsonElement.invoke(json, minecraftRegistry);
  }

  private @Nullable Method locateMethodByNameTransitively(Class<?> containerClass, String methodName, int argCount) {
    var currentClass = containerClass;

    while (currentClass != null && currentClass != Object.class) {
      for (var declaredMethod : currentClass.getDeclaredMethods()) {
        if (!declaredMethod.getName().equals(methodName))
          continue;

        if (declaredMethod.getParameterCount() != argCount)
          continue;

        declaredMethod.setAccessible(true);
        return declaredMethod;
      }

      currentClass = currentClass.getSuperclass();
    }

    return null;
  }
}
