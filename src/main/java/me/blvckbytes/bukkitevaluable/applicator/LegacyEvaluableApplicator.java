package me.blvckbytes.bukkitevaluable.applicator;

import com.google.gson.JsonObject;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.gpeee.parser.expression.AExpression;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LegacyEvaluableApplicator extends EvaluableApplicatorBase {

  public LegacyEvaluableApplicator() throws Exception {}

  @Override
  public void setDisplayName(ItemMeta meta, BukkitEvaluable evaluable, IEvaluationEnvironment environment) {
    var displayNameMessage = evaluable.asScalar(ScalarType.STRING, environment);
    meta.setDisplayName(displayNameMessage);
  }

  @Override
  public void setLore(ItemMeta meta, BukkitEvaluable evaluable, IEvaluationEnvironment environment, boolean override) {
    List<String> loreLines = meta.getLore();

    if (loreLines == null)
      loreLines = new ArrayList<>();

    if (override)
      loreLines.clear();

    for (var loreLineMessage : evaluable.asList(ScalarType.STRING_PRESERVE_NULLS, environment)) {
      if (loreLineMessage == null)
        continue;

      loreLines.add(loreLineMessage);
    }

    meta.setLore(loreLines);
  }

  @Override
  public void sendMessage(CommandSender receiver, BukkitEvaluable evaluable, IEvaluationEnvironment environment) {
    var value = evaluable.asRawObject(environment);

    if (value instanceof Collection<?> collection) {
      collection.forEach(item -> {

        if (item instanceof AExpression itemExpression) {
          var expressionValue = evaluable.evaluator.evaluateExpression(itemExpression, environment);

          if (expressionValue instanceof Collection<?> resultLines) {
            resultLines.forEach(resultLine -> receiver.sendMessage(String.valueOf(resultLine)));
            return;
          }

          receiver.sendMessage(String.valueOf(expressionValue));
          return;
        }

        receiver.sendMessage(String.valueOf(item));
      });

      return;
    }

    receiver.sendMessage(evaluable.asScalar(ScalarType.STRING, environment));
  }

  @Override
  public Object asChatComponent(BukkitEvaluable evaluable, IEvaluationEnvironment environment) throws Throwable {
    var componentJson = new JsonObject();
    componentJson.addProperty("text", evaluable.asScalar(ScalarType.STRING, environment));
    return createChatComponent(componentJson);
  }

  @Override
  public void sendActionBarMessage(CommandSender receiver, BukkitEvaluable evaluable, IEvaluationEnvironment environment) {
    if (!(receiver instanceof Player player))
      return;

    var message = evaluable.asScalar(ScalarType.STRING, environment);
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
  }

  @Override
  public void sendTitles(
    CommandSender receiver,
    @Nullable BukkitEvaluable title, @Nullable IEvaluationEnvironment titleEnvironment,
    @Nullable BukkitEvaluable subTitle, @Nullable IEvaluationEnvironment subTitleEnvironment,
    int fadeIn, int stay, int fadeOut
  ) {
    if (!(receiver instanceof Player player))
      return;

    String titleMessage = null, subTitleMessage = null;

    if (title != null && titleEnvironment != null)
      titleMessage = title.asScalar(ScalarType.STRING, titleEnvironment);

    if (subTitle != null && subTitleEnvironment != null)
      subTitleMessage = subTitle.asScalar(ScalarType.STRING, subTitleEnvironment);

    if (titleMessage == null && subTitleMessage == null)
      return;

    player.sendTitle(titleMessage, subTitleMessage, fadeIn, stay, fadeOut);
  }
}
