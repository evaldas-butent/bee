package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

/**
 * Handles xml descriptions of editor type user interface components.
 */

public class EditorDescription implements BeeSerializable, HasInfo, HasOptions {

  /**
   * Contains serializable members of a editor type user interface components.
   */

  private enum SerializationMember {
    TYPE, STEP_VALUE, CHARACTER_WIDTH, VISIBLE_LINES, FORMAT,
    WIDTH, HEIGHT, MIN_WIDTH, MIN_HEIGHT, ON_ENTRY, OPTIONS, ITEMS
  }

  private static final String ATTR_STEP_VALUE = "stepValue";
  private static final String ATTR_CHARACTER_WIDTH = "characterWidth";
  private static final String ATTR_VISIBLE_LINES = "visibleLines";
  private static final String ATTR_FORMAT = "format";
  private static final String ATTR_WIDTH = "width";
  private static final String ATTR_HEIGHT = "height";
  private static final String ATTR_MIN_WIDTH = "minWidth";
  private static final String ATTR_MIN_HEIGHT = "minHeight";
  private static final String ATTR_ON_ENTRY = "onEntry";
  private static final String ATTR_OPTIONS = "options";
  
  public static EditorDescription restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    EditorDescription editor = new EditorDescription();
    editor.deserialize(s);
    if (editor.isEmpty()) {
      return null;
    }
    return editor;
  }

  private EditorType type;

  private Integer stepValue = null;

  private Integer characterWidth = null;
  private Integer visibleLines = null;

  private String format = null;

  private Integer width = null;
  private Integer height = null;
  
  private Integer minWidth = null;
  private Integer minHeight = null;
  
  private EditorAction onEntry = null;
  
  private String options = null;

  private List<String> items = null;

  public EditorDescription(EditorType type) {
    this.type = type;
  }

  private EditorDescription() {
  }

  public void deserialize(String s) {
    SerializationMember[] members = SerializationMember.values();
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMember member = members[i];
      String value = arr[i];
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      switch (member) {
        case TYPE:
          setType(EditorType.getByTypeCode(value));
          break;
        case STEP_VALUE:
          setStepValue(BeeUtils.toIntOrNull(value));
          break;
        case CHARACTER_WIDTH:
          setCharacterWidth(BeeUtils.toIntOrNull(value));
          break;
        case VISIBLE_LINES:
          setVisibleLines(BeeUtils.toIntOrNull(value));
          break;
        case FORMAT:
          setFormat(value.trim());
          break;
        case WIDTH:
          setWidth(BeeUtils.toIntOrNull(value));
          break;
        case HEIGHT:
          setHeight(BeeUtils.toIntOrNull(value));
          break;
        case MIN_WIDTH:
          setMinWidth(BeeUtils.toIntOrNull(value));
          break;
        case MIN_HEIGHT:
          setMinHeight(BeeUtils.toIntOrNull(value));
          break;
        case ON_ENTRY:
          setOnEntry(EditorAction.getByCode(value));
          break;
        case OPTIONS:
          setOptions(value.trim());
          break;
        case ITEMS:
          if (BeeUtils.isEmpty(value)) {
            setItems(null);
          } else {
            List<String> lst = Lists.newArrayList(Codec.beeDeserialize(value));
            setItems(lst);
          }
          break;
      }
    }
  }

  public Integer getCharacterWidth() {
    return characterWidth;
  }

  public String getFormat() {
    return format;
  }

  public Integer getHeight() {
    return height;
  }
  
  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties(
      "Type", getType(),
      "Step Value", getStepValue(),
      "Character Width", getCharacterWidth(),
      "Visible Lines", getVisibleLines(),
      "Format", getFormat(),
      "Width", getWidth(),
      "Height", getHeight(),
      "Min Width", getMinWidth(),
      "Min Height", getMinHeight(),
      "On Entry", getOnEntry(),
      "Options", getOptions());
    
    if (getItems() != null) {
      info.add(new Property("Items", BeeUtils.bracket(getItems().size())));
      for (int i = 0; i < getItems().size(); i++) {
        info.add(new Property(BeeUtils.concat(1, "Item", i + 1), getItems().get(i)));
      }
    }

    if (isEmpty()) {
      PropertyUtils.addWhenEmpty(info, getClass());
      return info;
    }
    return info;
  }

  public List<String> getItems() {
    return items;
  }

  public Integer getMinHeight() {
    return minHeight;
  }

  public Integer getMinWidth() {
    return minWidth;
  }

  public EditorAction getOnEntry() {
    return onEntry;
  }

  public String getOptions() {
    return options;
  }

  public Integer getStepValue() {
    return stepValue;
  }

  public EditorType getType() {
    return type;
  }

  public Integer getVisibleLines() {
    return visibleLines;
  }

  public Integer getWidth() {
    return width;
  }

  public String serialize() {
    SerializationMember[] members = SerializationMember.values();
    Object[] arr = new Object[members.length];

    for (int i = 0; i < members.length; i++) {
      switch (members[i]) {
        case TYPE:
          arr[i] = (getType() == null) ? null : getType().getTypeCode();
          break;
        case STEP_VALUE:
          arr[i] = getStepValue();
          break;
        case CHARACTER_WIDTH:
          arr[i] = getCharacterWidth();
          break;
        case VISIBLE_LINES:
          arr[i] = getVisibleLines();
          break;
        case FORMAT:
          arr[i] = getFormat();
          break;
        case WIDTH:
          arr[i] = getWidth();
          break;
        case HEIGHT:
          arr[i] = getHeight();
          break;
        case MIN_WIDTH:
          arr[i] = getMinWidth();
          break;
        case MIN_HEIGHT:
          arr[i] = getMinHeight();
          break;
        case ON_ENTRY:
          arr[i] = (getOnEntry() == null) ? null : getOnEntry().getCode();
          break;
        case OPTIONS:
          arr[i] = getOptions();
          break;
        case ITEMS:
          arr[i] = getItems();
          break;
      }
    }
    return Codec.beeSerializeAll(arr);
  }

  public void setAttributes(Map<String, String> attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return;
    }

    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
      String key = attribute.getKey();
      String value = attribute.getValue();
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      if (BeeUtils.same(key, ATTR_STEP_VALUE)) {
        setStepValue(BeeUtils.toIntOrNull(value));
      } else if (BeeUtils.same(key, ATTR_CHARACTER_WIDTH)) {
        setCharacterWidth(BeeUtils.toIntOrNull(value));
      } else if (BeeUtils.same(key, ATTR_VISIBLE_LINES)) {
        setVisibleLines(BeeUtils.toIntOrNull(value));
      } else if (BeeUtils.same(key, ATTR_FORMAT)) {
        setFormat(value.trim());
      } else if (BeeUtils.same(key, ATTR_WIDTH)) {
        setWidth(BeeUtils.toIntOrNull(value));
      } else if (BeeUtils.same(key, ATTR_HEIGHT)) {
        setHeight(BeeUtils.toIntOrNull(value));
      } else if (BeeUtils.same(key, ATTR_MIN_WIDTH)) {
        setMinWidth(BeeUtils.toIntOrNull(value));
      } else if (BeeUtils.same(key, ATTR_MIN_HEIGHT)) {
        setMinHeight(BeeUtils.toIntOrNull(value));
      } else if (BeeUtils.same(key, ATTR_ON_ENTRY)) {
        setOnEntry(EditorAction.getByCode(value));
      } else if (BeeUtils.same(key, ATTR_OPTIONS)) {
        setOptions(value.trim());
      }
    }
  }

  public void setItems(List<String> items) {
    this.items = items;
  }

  public void setOptions(String options) {
    this.options = options;
  }

  private boolean isEmpty() {
    return getType() == null;
  }

  private void setCharacterWidth(Integer characterWidth) {
    this.characterWidth = characterWidth;
  }

  private void setFormat(String format) {
    this.format = format;
  }

  private void setHeight(Integer height) {
    this.height = height;
  }

  private void setMinHeight(Integer minHeight) {
    this.minHeight = minHeight;
  }

  private void setMinWidth(Integer minWidth) {
    this.minWidth = minWidth;
  }

  private void setOnEntry(EditorAction onEntry) {
    this.onEntry = onEntry;
  }

  private void setStepValue(Integer stepValue) {
    this.stepValue = stepValue;
  }

  private void setType(EditorType type) {
    this.type = type;
  }

  private void setVisibleLines(Integer visibleLines) {
    this.visibleLines = visibleLines;
  }

  private void setWidth(Integer width) {
    this.width = width;
  }
}
