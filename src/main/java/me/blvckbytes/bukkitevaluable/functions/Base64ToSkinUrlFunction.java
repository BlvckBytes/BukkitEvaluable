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

package me.blvckbytes.bukkitevaluable.functions;

import com.google.gson.JsonParser;
import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.functions.ExpressionFunctionArgument;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Decodes the passed base64 encoded string, parses it as JSON and
 * returns the extracted textures.SKIN.url string value
 */
public class Base64ToSkinUrlFunction extends AExpressionFunction {

  private final JsonParser jsonParser;

  public Base64ToSkinUrlFunction() {
    this.jsonParser = new JsonParser();
  }

  @Override
  public Object apply(IEvaluationEnvironment environment, List<@Nullable Object> args) {
    String base64String = nonNull(args, 0);

    byte[] decodedBytes = Base64.getDecoder().decode(base64String);
    String decodedString = new String(decodedBytes);

    // {"textures":{"SKIN":{"url": ?}}}
    return this.jsonParser.parse(decodedString)
      .getAsJsonObject()
      .getAsJsonObject("textures")
      .getAsJsonObject("SKIN")
      .get("url")
      .getAsString();
  }

  @Override
  public @Nullable List<ExpressionFunctionArgument> getArguments() {
    List<ExpressionFunctionArgument> arguments = new ArrayList<>();

    arguments.add(new ExpressionFunctionArgument("input", "Input base64 string", true, String.class));

    return arguments;
  }
}
