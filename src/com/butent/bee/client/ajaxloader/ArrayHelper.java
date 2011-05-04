package com.butent.bee.client.ajaxloader;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayBoolean;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.shared.Assert;

/**
 * Converts data into javascript arrays.
 */

public class ArrayHelper {
  public static byte[] toJavaArrayBytes(JsArrayInteger bytes) {
    int length = bytes.length();
    byte[] ret = new byte[length];
    for (int i = 0; i < length; i++) {
      ret[i] = (byte) bytes.get(i);
    }
    return ret;
  }

  public static <J extends JavaScriptObject> JsArray<J> toJsArray(J... objects) {
    Assert.notNull(objects);
    JsArray<J> result = JsArray.createArray().cast();
    for (int i = 0; i < objects.length; i++) {
      result.set(i, objects[i]);
    }
    nativePatchConstructorForSafari(result);
    return result;
  }

  public static JsArrayBoolean toJsArrayBoolean(boolean... bits) {
    Assert.notNull(bits);
    JsArrayBoolean result = JsArrayBoolean.createArray().cast();
    for (int i = 0; i < bits.length; i++) {
      result.set(i, bits[i]);
    }
    nativePatchConstructorForSafari(result);
    return result;
  }

  public static JsArrayInteger toJsArrayInteger(int... integers) {
    Assert.notNull(integers);
    JsArrayInteger result = JsArrayInteger.createArray().cast();
    for (int i = 0; i < integers.length; i++) {
      result.set(i, integers[i]);
    }
    nativePatchConstructorForSafari(result);
    return result;
  }

  public static JsArrayNumber toJsArrayNumber(double... numbers) {
    Assert.notNull(numbers);
    JsArrayNumber result = JsArrayNumber.createArray().cast();
    for (int i = 0; i < numbers.length; i++) {
      result.set(i, numbers[i]);
    }
    nativePatchConstructorForSafari(result);
    return result;
  }

  public static JsArrayString toJsArrayString(String... strings) {
    Assert.notNull(strings);
    JsArrayString result = JsArrayString.createArray().cast();
    for (int i = 0; i < strings.length; i++) {
      result.set(i, strings[i]);
    }
    nativePatchConstructorForSafari(result);
    return result;
  }

  private static native void nativePatchConstructorForSafari(JavaScriptObject result) /*-{
		result.constructor = $wnd.Array;
  }-*/;
}
