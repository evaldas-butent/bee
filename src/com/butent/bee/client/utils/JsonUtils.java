package com.butent.bee.client.utils;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import com.butent.bee.shared.utils.BeeUtils;

/**
 * Contains methods for transforming data into and from {@code JSON} type data structure.
 */

public class JsonUtils {

  public static String getString(JSONObject obj, String key) {
    if (obj == null || BeeUtils.isEmpty(key)) {
      return null;
    }
    return toString(obj.get(key));
  }

  public static boolean isEmpty(JSONValue value) {
    if (value == null) {
      return true;
    }

    if (value.isBoolean() != null) {
      return !value.isBoolean().booleanValue();
    } else if (value.isNumber() != null) {
      return BeeUtils.isEmpty(value.isNumber().doubleValue());
    } else if (value.isString() != null) {
      return BeeUtils.isEmpty(value.isString().stringValue());
    } else if (value.isArray() != null) {
      return value.isArray().size() <= 0;
    } else if (value.isObject() != null) {
      return value.isObject().size() <= 0;
    } else {
      return true;
    }
  }

  public static JSONObject toJson(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    JSONValue value = JSONParser.parseStrict(s);
    if (value == null) {
      return null;
    }
    return value.isObject();
  }

  public static String toString(JSONValue value) {
    if (value == null) {
      return null;
    }

    if (value.isString() != null) {
      return value.isString().stringValue();
    } else if (value.isBoolean() != null) {
      return BeeUtils.toString(value.isBoolean().booleanValue());
    } else if (value.isNumber() != null) {
      return BeeUtils.toString(value.isNumber().doubleValue());
    } else {
      return null;
    }
  }

  private JsonUtils() {
  }
}
