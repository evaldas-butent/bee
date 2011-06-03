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

public class ColumnDescription implements BeeSerializable, HasInfo {

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
    TYPE, NAME, CAPTION, READ_ONLY, WIDTH, SOURCE, REL_TABLE, REL_FIELD,
    MIN_WIDTH, MAX_WIDTH, SORTABLE, VISIBLE, FORMAT, HAS_FOOTER, SHOW_WIDTH,
    VALIDATION, EDITABLE, CARRY, EDITOR, MIN_VALUE, MAX_VALUE, STEP_VALUE,
    CALC, HEADER_STYLE, BODY_STYLE, FOOTER_STYLE, DYN_STYLES
  }

  public static ColumnDescription restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    ColumnDescription column = new ColumnDescription();
    column.deserialize(s);
    return column;
  }

  private ColType type;
  private String name;
  private String caption = null;
  private Boolean readOnly = null;
  private Integer width = null;

  private String source = null;
  private String relTable = null;
  private String relField = null;

  private Integer minWidth = null;
  private Integer maxWidth = null;

  private Boolean sortable = null;
  private Boolean visible = null;
  private String format = null;

  private Boolean hasFooter = null;
  private Boolean showWidth = null;

  private Calculation validation = null;
  private Calculation editable = null;
  private Calculation carry = null;

  private String editor = null;

  private String minValue = null;
  private String maxValue = null;
  private String stepValue = null;

  private Calculation calc = null;

  private StyleDeclaration headerStyle = null;
  private StyleDeclaration bodyStyle = null;
  private StyleDeclaration footerStyle = null;

  private Collection<ConditionalStyleDeclaration> dynStyles = null;

  public ColumnDescription(ColType type, String name) {
    Assert.notEmpty(type);
    Assert.notEmpty(name);
    this.type = type;
    this.name = name;
  }

  private ColumnDescription() {
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
          setReadOnly(BeeUtils.toBooleanOrNull(value));
          break;
        case WIDTH:
          setWidth(BeeUtils.toIntOrNull(value));
          break;
        case SOURCE:
          setSource(value);
          break;
        case REL_TABLE:
          setRelTable(value);
          break;
        case REL_FIELD:
          setRelField(value);
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
          setHasFooter(BeeUtils.toBooleanOrNull(value));
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
          setShowWidth(BeeUtils.toBooleanOrNull(value));
          break;
        case SORTABLE:
          setSortable(BeeUtils.toBooleanOrNull(value));
          break;
        case STEP_VALUE:
          setStepValue(value);
          break;
        case VALIDATION:
          setValidation(Calculation.restore(value));
          break;
        case VISIBLE:
          setVisible(BeeUtils.toBooleanOrNull(value));
          break;
        case BODY_STYLE:
          setBodyStyle(StyleDeclaration.restore(value));
          break;
        case DYN_STYLES:
          if (BeeUtils.isEmpty(value)) {
            setDynStyles(null);
          } else {
            String[] scs = Codec.beeDeserialize(value);
            List<ConditionalStyleDeclaration> lst = Lists.newArrayList();
            for (String z : scs) {
              lst.add(ConditionalStyleDeclaration.restore(z));
            }
            setDynStyles(lst);
          }
          break;
        case FOOTER_STYLE:
          setFooterStyle(StyleDeclaration.restore(value));
          break;
        case HEADER_STYLE:
          setHeaderStyle(StyleDeclaration.restore(value));
          break;
      }
    }
  }

  public StyleDeclaration getBodyStyle() {
    return bodyStyle;
  }

  public Calculation getCalc() {
    return calc;
  }

  public String getCaption() {
    return caption;
  }

  public Collection<ConditionalStyleDeclaration> getDynStyles() {
    return dynStyles;
  }

  public StyleDeclaration getFooterStyle() {
    return footerStyle;
  }

  public StyleDeclaration getHeaderStyle() {
    return headerStyle;
  }

  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties(
        "Type", getType(),
        "Name", getName(),
        "Caption", getCaption(),
        "Read Only", isReadOnly(),
        "Width", getWidth(),
        "Source", getSource(),
        "Rel Table", getRelTable(),
        "Rel Field", getRelField(),
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
        "Editor", getEditor());

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
      for (ConditionalStyleDeclaration conditionalStyle : getDynStyles()) {
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

  public Integer getMaxWidth() {
    return maxWidth;
  }

  public Integer getMinWidth() {
    return minWidth;
  }

  public String getName() {
    return name;
  }

  public String getRelField() {
    return relField;
  }

  public String getSource() {
    return source;
  }

  public ColType getType() {
    return type;
  }

  public Integer getWidth() {
    return width;
  }

  public Boolean hasFooter() {
    return hasFooter;
  }

  public Boolean isReadOnly() {
    return readOnly;
  }

  public Boolean isSortable() {
    return sortable;
  }

  public Boolean isVisible() {
    return visible;
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
        case REL_TABLE:
          arr[i++] = getRelTable();
          break;
        case REL_FIELD:
          arr[i++] = getRelField();
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

  public void setBodyStyle(StyleDeclaration bodyStyle) {
    this.bodyStyle = bodyStyle;
  }

  public void setCalc(Calculation calc) {
    this.calc = calc;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setCarry(Calculation carry) {
    this.carry = carry;
  }

  public void setDynStyles(Collection<ConditionalStyleDeclaration> dynStyles) {
    this.dynStyles = dynStyles;
  }

  public void setEditable(Calculation editable) {
    this.editable = editable;
  }

  public void setEditor(String editor) {
    this.editor = editor;
  }

  public void setFooterStyle(StyleDeclaration footerStyle) {
    this.footerStyle = footerStyle;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setHasFooter(Boolean hasFooter) {
    this.hasFooter = hasFooter;
  }

  public void setHeaderStyle(StyleDeclaration headerStyle) {
    this.headerStyle = headerStyle;
  }

  public void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
  }

  public void setMaxWidth(Integer maxWidth) {
    this.maxWidth = maxWidth;
  }

  public void setMinValue(String minValue) {
    this.minValue = minValue;
  }

  public void setMinWidth(Integer minWidth) {
    this.minWidth = minWidth;
  }

  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setRelField(String relField) {
    this.relField = relField;
  }

  public void setRelTable(String relTable) {
    this.relTable = relTable;
  }

  public void setShowWidth(Boolean showWidth) {
    this.showWidth = showWidth;
  }

  public void setSortable(Boolean sortable) {
    this.sortable = sortable;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public void setStepValue(String stepValue) {
    this.stepValue = stepValue;
  }

  public void setValidation(Calculation validation) {
    this.validation = validation;
  }

  public void setVisible(Boolean visible) {
    this.visible = visible;
  }

  public void setWidth(Integer width) {
    this.width = width;
  }

  public Boolean showWidth() {
    return showWidth;
  }

  private Calculation getCarry() {
    return carry;
  }

  private Calculation getEditable() {
    return editable;
  }

  private String getEditor() {
    return editor;
  }

  private String getFormat() {
    return format;
  }

  private String getMaxValue() {
    return maxValue;
  }

  private String getMinValue() {
    return minValue;
  }

  private String getRelTable() {
    return relTable;
  }

  private String getStepValue() {
    return stepValue;
  }

  private Calculation getValidation() {
    return validation;
  }

  private void setName(String name) {
    this.name = name;
  }

  private void setType(ColType type) {
    this.type = type;
  }
}
