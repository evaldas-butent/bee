package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;

public class GridColumn implements BeeSerializable, HasInfo {
  
  public enum ColType {
    DATA("BeeDataColumn"),
    RELATED("BeeRelColumn"),
    CALCULATED("BeeCalcColumn"),
    ID("BeeIdColumn"),
    VERSION("BeeVerColumn");

    public static ColType getColType(String tagName) {
      if (!BeeUtils.isEmpty(tagName)) {
        for (ColType type : ColType.values()) {
          if (BeeUtils.same(type.getTagName(), tagName)) {
            return type;
          }
        }
      }
      return null;
    }

    private final String tagName;

    private ColType(String tagName) {
      this.tagName = tagName;
    }

    public String getTagName() {
      return tagName;
    }
  }
 
  private enum SerializationMember {
    TYPE, NAME, CAPTION, READ_ONLY, WIDTH, SOURCE, REL_SOURCE, RELATION,
    MIN_WIDTH, MAX_WIDTH, SORTABLE, VISIBLE, FORMAT, HAS_FOOTER, SHOW_WIDTH,
    VALIDATION, EDITABLE, CARRY, EDITOR, MIN_VALUE, MAX_VALUE, STEP_VALUE,
    CALC, HEADER_STYLE, BODY_STYLE, FOOTER_STYLE, DYN_STYLES 
  }
  
  public static GridColumn restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    GridColumn column = new GridColumn();
    column.deserialize(s);
    return column;
  }
  
  private ColType type;
  private String name;
  private String caption = null;
  private boolean readOnly = false;
  private Integer width = null;

  private String source = null;
  private String relSource = null;
  private String relation = null;

  private Integer minWidth = null;
  private Integer maxWidth = null;
  
  private boolean sortable = true;
  private boolean visible = true;
  private String format = null;
  
  private boolean hasFooter = true;
  private boolean showWidth = true;
  
  private Calculation validation = null;
  private Calculation editable = null;
  private Calculation carry = null;
  
  private String editor = null;
  
  private String minValue = null;
  private String maxValue = null;
  private String stepValue = null;
  
  private Calculation calc = null;

  private Style headerStyle = null;
  private Style bodyStyle = null;
  private Style footerStyle = null;
  
  private Collection<ConditionalStyle> dynStyles = null;
  
  public GridColumn(ColType type, String name, String caption, boolean readOnly, Integer width) {
    Assert.notEmpty(type);
    Assert.notEmpty(name);
    this.type = type;
    this.name = name;
    this.caption = caption;
    this.readOnly = readOnly;
    this.width = width;
  }

  private GridColumn() {
  }

  @Override
  public void deserialize(String s) {
    SerializationMember[] members = SerializationMember.values();
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMember member = members[i];
      String value = arr[i];

      switch (member) {
        case TYPE:
          setType(ColType.getColType(value));
          break;
        case NAME:
          setName(value);
          break;
        case CAPTION:
          setCaption(value);
          break;
        case READ_ONLY:
          setReadOnly(BeeUtils.toBoolean(value));
          break;
        case WIDTH:
          setWidth(BeeUtils.toIntOrNull(value));
          break;
        case SOURCE:
          setSource(value);
          break;
        case REL_SOURCE:
          setRelSource(value);
          break;
        case RELATION:
          setRelation(value);
          break;
        case CALC:
          setCalc(Calculation.restore(value));
          break;
        case CARRY:
          setCarry(Calculation.restore(value));
          break;
        case EDITABLE:
          setEditable(Calculation.restore(value));
          break;
        case EDITOR:
          setEditor(value);
          break;
        case FORMAT:
          setFormat(value);
          break;
        case HAS_FOOTER:
          setHasFooter(BeeUtils.toBoolean(value));
          break;
        case MAX_VALUE:
          setMaxValue(value);
          break;
        case MAX_WIDTH:
          setMaxWidth(BeeUtils.toIntOrNull(value));
          break;
        case MIN_VALUE:
          setMinValue(value);
          break;
        case MIN_WIDTH:
          setMinWidth(BeeUtils.toIntOrNull(value));
          break;
        case SHOW_WIDTH:
          setShowWidth(BeeUtils.toBoolean(value));
          break;
        case SORTABLE:
          setSortable(BeeUtils.toBoolean(value));
          break;
        case STEP_VALUE:
          setStepValue(value);
          break;
        case VALIDATION:
          setValidation(Calculation.restore(value));
          break;
        case VISIBLE:
          setVisible(BeeUtils.toBoolean(value));
          break;
        case BODY_STYLE:
          setBodyStyle(Style.restore(value));
          break;
        case DYN_STYLES:
          if (BeeUtils.isEmpty(value)) {
            setDynStyles(null);
          } else {
            String[] scs = Codec.beeDeserialize(value);
            List<ConditionalStyle> lst = Lists.newArrayList();
            for (String z : scs) {
              lst.add(ConditionalStyle.restore(z));
            }
            setDynStyles(lst);
          }
          break;
        case FOOTER_STYLE:
          setFooterStyle(Style.restore(value));
          break;
        case HEADER_STYLE:
          setHeaderStyle(Style.restore(value));
          break;
      }
    }
  }

  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties(
        "Type", getType(),
        "Name", getName(),
        "Caption", getCaption(),
        "Read Only", isReadOnly(),
        "Width", getWidth(),
        "Source", getSource(),
        "Rel Source", getRelSource(),
        "Relation", getRelation(),
        "Min Width", getMinWidth(),
        "Max Width", getMaxWidth(),
        "Sortable", isSortable(),
        "Visible", isVisible(),
        "Format", getFormat(),
        "Has Footer", hasFooter(),
        "Show Width", showWidth(),
        "Min Value", getMinValue(),
        "Max Value", getMaxValue(),
        "Step Value", getStepValue(),
        "Calc", getCalc());
    
    if (getValidation() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Validation", getValidation().getInfo());
    }
    if (getEditable() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Editable", getEditable().getInfo());
    }
    if (getCarry() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Carry", getCarry().getInfo());
    }
    if (getCalc() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Calc", getCalc().getInfo());
    }
    
    if (getHeaderStyle() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Header Style", getHeaderStyle().getInfo());
    }
    if (getBodyStyle() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Body Style", getBodyStyle().getInfo());
    }
    if (getFooterStyle() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Footer Style", getFooterStyle().getInfo());
    }
    
    if (getDynStyles() != null && !getDynStyles().isEmpty()) {
      int cnt = getDynStyles().size();
      info.add(new Property("Dyn Styles", BeeUtils.bracket(cnt)));
      int i = 0;
      for (ConditionalStyle conditionalStyle : getDynStyles()) {
        i++;
        if (conditionalStyle != null) {
          PropertyUtils.appendChildrenToProperties(info, "Dyn Style " + BeeUtils.progress(i, cnt),
              conditionalStyle.getInfo());
        }
      }
    }
    
    PropertyUtils.addWhenEmpty(info, getClass());
    return info;
  }

  public String getName() {
    return name;
  }

  @Override
  public String serialize() {
    SerializationMember[] members = SerializationMember.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMember member : members) {
      switch (member) {
        case TYPE:
          arr[i++] = (getType() == null) ? null : getType().getTagName();
          break;
        case NAME:
          arr[i++] = getName();
          break;
        case CAPTION:
          arr[i++] = getCaption();
          break;
        case READ_ONLY:
          arr[i++] = isReadOnly();
          break;
        case WIDTH:
          arr[i++] = getWidth();
          break;
        case SOURCE:
          arr[i++] = getSource();
          break;
        case REL_SOURCE:
          arr[i++] = getRelSource();
          break;
        case RELATION:
          arr[i++] = getRelation();
          break;
        case CALC:
          arr[i++] = getCalc();
          break;
        case CARRY:
          arr[i++] = getCarry();
          break;
        case EDITABLE:
          arr[i++] = getEditable();
          break;
        case EDITOR:
          arr[i++] = getEditor();
          break;
        case FORMAT:
          arr[i++] = getFormat();
          break;
        case HAS_FOOTER:
          arr[i++] = hasFooter();
          break;
        case MAX_VALUE:
          arr[i++] = getMaxValue();
          break;
        case MAX_WIDTH:
          arr[i++] = getMaxWidth();
          break;
        case MIN_VALUE:
          arr[i++] = getMinValue();
          break;
        case MIN_WIDTH:
          arr[i++] = getMinWidth();
          break;
        case SHOW_WIDTH:
          arr[i++] = showWidth(); 
          break;
        case SORTABLE:
          arr[i++] = isSortable();
          break;
        case STEP_VALUE:
          arr[i++] = getStepValue();
          break;
        case VALIDATION:
          arr[i++] = getValidation();
          break;
        case VISIBLE:
          arr[i++] = isVisible();
          break;
        case BODY_STYLE:
          arr[i++] = getBodyStyle();
          break;
        case DYN_STYLES:
          arr[i++] = getDynStyles();
          break;
        case FOOTER_STYLE:
          arr[i++] = getFooterStyle();
          break;
        case HEADER_STYLE:
          arr[i++] = getHeaderStyle();
          break;
      }
    }
    return Codec.beeSerializeAll(arr);
  }

  public void setCalc(Calculation calc) {
    this.calc = calc;
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

  public void setRelSource(String relSource) {
    this.relSource = relSource;
  }

  public void setSource(String source) {
    this.source = source;
  }

  private Style getBodyStyle() {
    return bodyStyle;
  }

  private Calculation getCalc() {
    return calc;
  }

  private String getCaption() {
    return caption;
  }

  private Calculation getCarry() {
    return carry;
  }

  private Collection<ConditionalStyle> getDynStyles() {
    return dynStyles;
  }

  private Calculation getEditable() {
    return editable;
  }

  private String getEditor() {
    return editor;
  }

  private Style getFooterStyle() {
    return footerStyle;
  }

  private String getFormat() {
    return format;
  }

  private Style getHeaderStyle() {
    return headerStyle;
  }

  private String getMaxValue() {
    return maxValue;
  }

  private Integer getMaxWidth() {
    return maxWidth;
  }

  private String getMinValue() {
    return minValue;
  }

  private Integer getMinWidth() {
    return minWidth;
  }

  private String getRelation() {
    return relation;
  }

  private String getRelSource() {
    return relSource;
  }

  private String getSource() {
    return source;
  }

  private String getStepValue() {
    return stepValue;
  }

  private ColType getType() {
    return type;
  }

  private Calculation getValidation() {
    return validation;
  }

  private Integer getWidth() {
    return width;
  }

  private boolean hasFooter() {
    return hasFooter;
  }

  private boolean isReadOnly() {
    return readOnly;
  }

  private boolean isSortable() {
    return sortable;
  }

  private boolean isVisible() {
    return visible;
  }

  private void setBodyStyle(Style bodyStyle) {
    this.bodyStyle = bodyStyle;
  }

  private void setCaption(String caption) {
    this.caption = caption;
  }

  private void setCarry(Calculation carry) {
    this.carry = carry;
  }

  private void setDynStyles(Collection<ConditionalStyle> dynStyles) {
    this.dynStyles = dynStyles;
  }

  private void setEditable(Calculation editable) {
    this.editable = editable;
  }

  private void setEditor(String editor) {
    this.editor = editor;
  }

  private void setFooterStyle(Style footerStyle) {
    this.footerStyle = footerStyle;
  }

  private void setFormat(String format) {
    this.format = format;
  }

  private void setHasFooter(boolean hasFooter) {
    this.hasFooter = hasFooter;
  }

  private void setHeaderStyle(Style headerStyle) {
    this.headerStyle = headerStyle;
  }

  private void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
  }

  private void setMaxWidth(Integer maxWidth) {
    this.maxWidth = maxWidth;
  }

  private void setMinValue(String minValue) {
    this.minValue = minValue;
  }

  private void setMinWidth(Integer minWidth) {
    this.minWidth = minWidth;
  }

  private void setName(String name) {
    this.name = name;
  }

  private void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  private void setShowWidth(boolean showWidth) {
    this.showWidth = showWidth;
  }

  private void setSortable(boolean sortable) {
    this.sortable = sortable;
  }

  private void setStepValue(String stepValue) {
    this.stepValue = stepValue;
  }

  private void setType(ColType type) {
    this.type = type;
  }

  private void setValidation(Calculation validation) {
    this.validation = validation;
  }

  private void setVisible(boolean visible) {
    this.visible = visible;
  }

  private void setWidth(Integer width) {
    this.width = width;
  }

  private boolean showWidth() {
    return showWidth;
  }
}
