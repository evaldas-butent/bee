package com.butent.bee.shared.data;

import com.google.common.collect.Lists;

import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enables using custom properties in data objects.
 */

@SuppressWarnings("serial")
public class CustomProperties extends HashMap<String, Object> implements HasInfo, Transformable {
  public static CustomProperties create() {
    return new CustomProperties();
  }

  @Override
  public CustomProperties clone() {
    CustomProperties properties = create();
    for (Map.Entry<String, Object> entry : entrySet()) {
      properties.put(entry.getKey(), entry.getValue());
    }
    return properties;
  }

  public List<Property> getInfo() {
    List<Property> lst = Lists.newArrayList();
    if (isEmpty()) {
      return lst;
    }

    PropertyUtils.addProperty(lst, "Custom Properties", size());
    for (Map.Entry<String, Object> entry : entrySet()) {
      PropertyUtils.addProperty(lst, entry.getKey(), entry.getValue());
    }
    return lst;
  }

  public String transform() {
    return BeeUtils.transformMap(this);
  }
}
