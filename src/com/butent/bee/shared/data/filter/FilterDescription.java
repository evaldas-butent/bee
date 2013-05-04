package com.butent.bee.shared.data.filter;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

public class FilterDescription implements BeeSerializable, HasInfo {

  public static final String TAG_FILTER = "filter";
  
  private static final String ATTR_VALUES = "values";
  private static final String ATTR_INITIAL = "initial";
  private static final String ATTR_ORDINAL = "ordinal";
  
  public static FilterDescription create(Map<String, String> attributes) {
    if (BeeUtils.isEmpty(attributes)) {
      return null;
    }
    
    String label = attributes.get(UiConstants.ATTR_LABEL);
    if (BeeUtils.isEmpty(label)) {
      return null;
    }

    String values = attributes.get(ATTR_VALUES);
    if (BeeUtils.isEmpty(values)) {
      return null;
    }
    
    FilterDescription filterDescription = new FilterDescription();
    filterDescription.setLabel(label);
    filterDescription.setValues(values);
    
    if (attributes.containsKey(ATTR_INITIAL)) {
      filterDescription.setInitial(BeeUtils.toBooleanOrNull(attributes.get(ATTR_INITIAL)));
    }
    if (attributes.containsKey(ATTR_ORDINAL)) {
      filterDescription.setOrdinal(BeeUtils.toIntOrNull(attributes.get(ATTR_ORDINAL)));
    }
    
    return filterDescription;
  }

  public static FilterDescription restore(String s) {
    Assert.notEmpty(s);
    
    FilterDescription filterDescription = new FilterDescription();
    filterDescription.deserialize(s);
    
    return filterDescription;
  }

  public static List<FilterDescription> restoreList(String serialized) {
    Assert.notEmpty(serialized);
    List<FilterDescription> result = Lists.newArrayList();
    
    String[] arr = Codec.beeDeserializeCollection(serialized);
    if (ArrayUtils.isEmpty(arr)) {
      return result;
    }
    
    for (String s : arr) {
      if (!BeeUtils.isEmpty(s)) {
        result.add(restore(s));
      }
    }
    
    return result;
  }
  
  private String label;
  private String values;

  private Boolean initial;
  private Integer ordinal;

  private FilterDescription() {
    super();
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 4);
    
    int i = 0;
    setLabel(arr[i++]);
    setValues(arr[i++]);
    setInitial(BeeUtils.toBooleanOrNull(arr[i++]));
    setOrdinal(BeeUtils.toIntOrNull(arr[i++]));
  }

  @Override
  public List<Property> getInfo() {
    return PropertyUtils.createProperties("Label", getLabel(), "Values", getValues(),
        "Initial", getInitial(), "Ordinal", getOrdinal());
  }

  public String getLabel() {
    return label;
  }

  public Integer getOrdinal() {
    return ordinal;
  }

  public String getValues() {
    return values;
  }

  public boolean isInitial() {
    return BeeUtils.isTrue(getInitial());
  }

  @Override
  public String serialize() {
    return Codec.beeSerialize(new Object[] {getLabel(), getValues(), getInitial(), getOrdinal()});
  }

  private Boolean getInitial() {
    return initial;
  }

  private void setInitial(Boolean initial) {
    this.initial = initial;
  }

  private void setLabel(String label) {
    this.label = label;
  }

  private void setOrdinal(Integer ordinal) {
    this.ordinal = ordinal;
  }

  private void setValues(String values) {
    this.values = values;
  }
}
