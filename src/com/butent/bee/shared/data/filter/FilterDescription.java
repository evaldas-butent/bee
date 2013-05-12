package com.butent.bee.shared.data.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
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

public class FilterDescription implements BeeSerializable, HasInfo, Comparable<FilterDescription> {

  public static final String TAG_FILTER = "filter";

  private static final String ATTR_INITIAL = "initial";
  private static final String ATTR_ORDINAL = "ordinal";
  private static final String ATTR_EDITABLE = "editable";
  private static final String ATTR_REMOVABLE = "removable";

  public static FilterDescription create(String key, Map<String, String> attributes) {
    if (BeeUtils.isEmpty(key) || BeeUtils.isEmpty(attributes)) {
      return null;
    }

    String name = attributes.get(UiConstants.ATTR_NAME);
    if (BeeUtils.isEmpty(name)) {
      return null;
    }

    String label = attributes.get(UiConstants.ATTR_LABEL);
    if (BeeUtils.isEmpty(label)) {
      return null;
    }

    String value = attributes.get(UiConstants.ATTR_VALUE);
    if (BeeUtils.isEmpty(value)) {
      return null;
    }

    FilterDescription filterDescription = new FilterDescription();
    filterDescription.setName(name);
    filterDescription.setLabel(label);
    filterDescription.setValue(createValue(key, value));

    if (attributes.containsKey(ATTR_INITIAL)) {
      filterDescription.setInitial(BeeUtils.toBooleanOrNull(attributes.get(ATTR_INITIAL)));
    }
    if (attributes.containsKey(ATTR_ORDINAL)) {
      filterDescription.setOrdinal(BeeUtils.toIntOrNull(attributes.get(ATTR_ORDINAL)));
    }

    if (attributes.containsKey(ATTR_EDITABLE)) {
      filterDescription.setEditable(BeeUtils.toBooleanOrNull(attributes.get(ATTR_EDITABLE)));
    }
    if (attributes.containsKey(ATTR_REMOVABLE)) {
      filterDescription.setRemovable(BeeUtils.toBooleanOrNull(attributes.get(ATTR_REMOVABLE)));
    }

    return filterDescription;
  }

  public static String createValue(Map<String, String> values) {
    Assert.notEmpty(values);
    return Codec.beeSerialize(values);
  }
  
  public static String createValue(String key, String value) {
    Assert.notEmpty(key);
    Assert.notEmpty(value);
    
    Map<String, String> values = Maps.newHashMap();
    values.put(key, value);
    
    return createValue(values);
  }
  
  public static FilterDescription predefined(String name, String label, String key, String value) {
    Assert.notEmpty(name);
    Assert.notEmpty(label);

    FilterDescription filterDescription = new FilterDescription();
    filterDescription.setName(name);
    filterDescription.setLabel(label);
    filterDescription.setValue(createValue(key, value));

    filterDescription.setEditable(false);
    filterDescription.setRemovable(false);

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

  public static FilterDescription userDefined(String label, Map<String, String> values) {
    Assert.notEmpty(label);

    FilterDescription filterDescription = new FilterDescription();
    String name = BeeUtils.join(BeeConst.STRING_UNDER, System.currentTimeMillis(),
        BeeUtils.randomString(6));
    filterDescription.setName(name);
    filterDescription.setLabel(label);
    filterDescription.setValue(createValue(values));

    filterDescription.setEditable(true);
    filterDescription.setRemovable(true);

    return filterDescription;
  }

  private String name = null;
  private String label = null;
  private String value = null;

  private Boolean initial = null;
  private Integer ordinal = null;

  private Boolean editable = null;
  private Boolean removable = null;

  public FilterDescription(String name, String label, String value, Boolean initial,
      Integer ordinal, Boolean editable, Boolean removable) {
    super();
    this.name = name;
    this.label = label;
    this.value = value;
    this.initial = initial;
    this.ordinal = ordinal;
    this.editable = editable;
    this.removable = removable;
  }

  protected FilterDescription() {
    super();
  }

  @Override
  public int compareTo(FilterDescription o) {
    if (getOrdinal() == null) {
      if (o.getOrdinal() == null) {
        return BeeConst.COMPARE_EQUAL;
      } else {
        return BeeConst.COMPARE_MORE;
      }
    } else if (o.getOrdinal() == null) {
      return BeeConst.COMPARE_LESS;
    } else {
      return getOrdinal().compareTo(o.getOrdinal());
    }
  }

  public FilterDescription copy() {
    FilterDescription result = new FilterDescription();
    
    result.setName(getName());
    result.setLabel(getLabel());
    result.setValue(getValue());

    result.setInitial(getInitial());
    result.setOrdinal(getOrdinal());

    result.setEditable(getEditable());
    result.setRemovable(getRemovable());

    return result;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 7);

    int i = 0;
    setName(arr[i++]);
    setLabel(arr[i++]);
    setValue(arr[i++]);

    setInitial(BeeUtils.toBooleanOrNull(arr[i++]));
    setOrdinal(BeeUtils.toIntOrNull(arr[i++]));

    setEditable(BeeUtils.toBooleanOrNull(arr[i++]));
    setRemovable(BeeUtils.toBooleanOrNull(arr[i++]));
  }

  public Boolean getEditable() {
    return editable;
  }

  @Override
  public List<Property> getInfo() {
    return PropertyUtils.createProperties("Name", getName(),
        "Label", getLabel(),
        "Value", getValue(),
        "Initial", getInitial(),
        "Ordinal", getOrdinal(),
        "Editable", getEditable(),
        "Removable", getRemovable());
  }

  public Boolean getInitial() {
    return initial;
  }

  public String getLabel() {
    return label;
  }

  public String getName() {
    return name;
  }

  public Integer getOrdinal() {
    return ordinal;
  }

  public Boolean getRemovable() {
    return removable;
  }

  public String getValue() {
    return value;
  }

  public Map<String, String> getValues() {
    Map<String, String> values = Maps.newHashMap();
    
    String[] arr = Codec.beeDeserializeCollection(getValue());
    Assert.notNull(arr);
    
    for (int i = 0; i < arr.length - 1; i += 2) {
      values.put(arr[i], arr[i + 1]);
    }
    
    return values;
  }
  
  public boolean isEditable() {
    return BeeUtils.isTrue(getEditable());
  }

  public boolean isInitial() {
    return BeeUtils.isTrue(getInitial());
  }

  public boolean isRemovable() {
    return BeeUtils.isTrue(getRemovable());
  }

  @Override
  public String serialize() {
    return Codec.beeSerialize(new Object[] {getName(), getLabel(), getValue(),
        getInitial(), getOrdinal(), getEditable(), getRemovable()});
  }

  public void setEditable(Boolean editable) {
    this.editable = editable;
  }

  public void setInitial(Boolean initial) {
    this.initial = initial;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setOrdinal(Integer ordinal) {
    this.ordinal = ordinal;
  }

  public void setRemovable(Boolean removable) {
    this.removable = removable;
  }

  private void setName(String name) {
    this.name = name;
  }

  private void setValue(String value) {
    this.value = value;
  }
}
