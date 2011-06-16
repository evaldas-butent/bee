package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;

/**
 * Contains column properties and methods for managing them.
 */

public class ColumnDescription implements BeeSerializable, HasInfo {

  /**
   * Contains a list of possible column types.
   */
  
  public enum CellType {
    HTML("html");
    
    public static CellType getByCode(String code) {
      if (!BeeUtils.isEmpty(code)) {
        for (CellType type : CellType.values()) {
          if (BeeUtils.same(type.getCode(), code)) {
            return type;
          }
        }
      }
      return null;
    }
    
    private String code;
    
    private CellType(String code) {
      this.code = code;
    }
    
    public String getCode() {
      return code;
    }
  }

  public enum ColType {
    DATA("BeeDataColumn", false),
    RELATED("BeeRelColumn", false),
    CALCULATED("BeeCalcColumn", true),
    ID("BeeIdColumn", true),
    VERSION("BeeVerColumn", true);

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
    private final boolean readOnly;

    private ColType(String tagName, boolean readOnly) {
      this.tagName = tagName;
      this.readOnly = readOnly;
    }

    public String getTagName() {
      return tagName;
    }

    public boolean isReadOnly() {
      return readOnly;
    }
  }

  /**
   * Contains a list of serializable members of column object.
   */

  private enum SerializationMember {
    COL_TYPE, NAME, CAPTION, READ_ONLY, WIDTH, SOURCE, REL_SOURCE, REL_VIEW, REL_COLUMN,
    MIN_WIDTH, MAX_WIDTH, SORTABLE, VISIBLE, FORMAT, HOR_ALIGN, HAS_FOOTER, SHOW_WIDTH,
    VALIDATION, EDITABLE, CARRY, EDITOR, MIN_VALUE, MAX_VALUE,
    CALC, VALUE_TYPE, PRECISION, SCALE,
    HEADER_STYLE, BODY_STYLE, FOOTER_STYLE, DYN_STYLES, CELL_TYPE
  }

  public static ColumnDescription restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    ColumnDescription column = new ColumnDescription();
    column.deserialize(s);
    return column;
  }

  private ColType colType;
  private String name;
  private String caption = null;
  private Boolean readOnly = null;
  private Integer width = null;

  private String source = null;
  private String relSource = null;
  private String relView = null;
  private String relColumn = null;

  private Integer minWidth = null;
  private Integer maxWidth = null;

  private Boolean sortable = null;
  private Boolean visible = null;

  private String format = null;
  private String horAlign = null;

  private Boolean hasFooter = null;
  private Boolean showWidth = null;

  private Calculation validation = null;
  private Calculation editable = null;
  private Calculation carry = null;

  private EditorDescription editor = null;

  private String minValue = null;
  private String maxValue = null;

  private Calculation calc = null;
  private ValueType valueType = null;
  private Integer precision = null;
  private Integer scale = null;

  private StyleDeclaration headerStyle = null;
  private StyleDeclaration bodyStyle = null;
  private StyleDeclaration footerStyle = null;

  private Collection<ConditionalStyleDeclaration> dynStyles = null;

  private CellType cellType = null;

  public ColumnDescription(ColType colType, String name) {
    Assert.notEmpty(colType);
    Assert.notEmpty(name);
    this.colType = colType;
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
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      switch (member) {
        case COL_TYPE:
          setColType(ColType.getColType(value));
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
        case REL_SOURCE:
          setRelSource(value);
          break;
        case REL_VIEW:
          setRelView(value);
          break;
        case REL_COLUMN:
          setRelColumn(value);
          break;
        case CALC:
          setCalc(Calculation.restore(value));
          break;
        case VALUE_TYPE:
          setValueType(ValueType.getByTypeCode(value));
          break;
        case PRECISION:
          setPrecision(BeeUtils.toIntOrNull(value));
          break;
        case SCALE:
          setScale(BeeUtils.toIntOrNull(value));
          break;
        case CARRY:
          setCarry(Calculation.restore(value));
          break;
        case EDITABLE:
          setEditable(Calculation.restore(value));
          break;
        case EDITOR:
          setEditor(EditorDescription.restore(value));
          break;
        case FORMAT:
          setFormat(value);
          break;
        case HOR_ALIGN:
          setHorAlign(value);
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
        case CELL_TYPE:
          setCellType(CellType.getByCode(value));
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

  public CellType getCellType() {
    return cellType;
  }

  public ColType getColType() {
    return colType;
  }

  public Collection<ConditionalStyleDeclaration> getDynStyles() {
    return dynStyles;
  }

  public Calculation getEditable() {
    return editable;
  }

  public EditorDescription getEditor() {
    return editor;
  }

  public StyleDeclaration getFooterStyle() {
    return footerStyle;
  }

  public String getFormat() {
    return format;
  }

  public StyleDeclaration getHeaderStyle() {
    return headerStyle;
  }

  public String getHorAlign() {
    return horAlign;
  }

  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties(
        "Col Type", getColType(),
        "Name", getName(),
        "Caption", getCaption(),
        "Read Only", isReadOnly(),
        "Width", getWidth(),
        "Source", getSource(),
        "Rel Source", getRelSource(),
        "Rel View", getRelView(),
        "Rel Column", getRelColumn(),
        "Min Width", getMinWidth(),
        "Max Width", getMaxWidth(),
        "Sortable", isSortable(),
        "Visible", isVisible(),
        "Format", getFormat(),
        "Horizontal Alignment", getHorAlign(),
        "Has Footer", hasFooter(),
        "Show Width", showWidth(),
        "Min Value", getMinValue(),
        "Max Value", getMaxValue(),
        "Value Type", getValueType(),
        "Precision", getPrecision(),
        "Scale", getScale(),
        "Cell Type", getCellType());

    if (getValidation() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Validation", getValidation().getInfo());
    }
    if (getEditable() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Editable", getEditable().getInfo());
    }
    if (getEditor() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Editor", getEditor().getInfo());
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

  public String getMaxValue() {
    return maxValue;
  }

  public Integer getMaxWidth() {
    return maxWidth;
  }

  public String getMinValue() {
    return minValue;
  }

  public Integer getMinWidth() {
    return minWidth;
  }

  public String getName() {
    return name;
  }

  public Integer getPrecision() {
    return precision;
  }

  public String getRelColumn() {
    return relColumn;
  }

  public String getRelSource() {
    return relSource;
  }

  public String getRelView() {
    return relView;
  }

  public Integer getScale() {
    return scale;
  }

  public String getSource() {
    return source;
  }

  public Calculation getValidation() {
    return validation;
  }

  public ValueType getValueType() {
    return valueType;
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
        case COL_TYPE:
          arr[i++] = (getColType() == null) ? null : getColType().getTagName();
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
        case REL_VIEW:
          arr[i++] = getRelView();
          break;
        case REL_COLUMN:
          arr[i++] = getRelColumn();
          break;
        case CALC:
          arr[i++] = getCalc();
          break;
        case VALUE_TYPE:
          arr[i++] = (getValueType() == null) ? null : getValueType().getTypeCode();
          break;
        case PRECISION:
          arr[i++] = getPrecision();
          break;
        case SCALE:
          arr[i++] = getScale();
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
        case HOR_ALIGN:
          arr[i++] = getHorAlign();
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
        case CELL_TYPE:
          arr[i++] = (getCellType() == null) ? null : getCellType().getCode();
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

  public void setCellType(CellType cellType) {
    this.cellType = cellType;
  }

  public void setDynStyles(Collection<ConditionalStyleDeclaration> dynStyles) {
    this.dynStyles = dynStyles;
  }

  public void setEditable(Calculation editable) {
    this.editable = editable;
  }

  public void setEditor(EditorDescription editor) {
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

  public void setHorAlign(String horAlign) {
    this.horAlign = horAlign;
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

  public void setPrecision(Integer precision) {
    this.precision = precision;
  }

  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setRelColumn(String relColumn) {
    this.relColumn = relColumn;
  }

  public void setRelSource(String relSource) {
    this.relSource = relSource;
  }

  public void setRelView(String relView) {
    this.relView = relView;
  }

  public void setScale(Integer scale) {
    this.scale = scale;
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

  public void setValidation(Calculation validation) {
    this.validation = validation;
  }

  public void setValueType(ValueType valueType) {
    this.valueType = valueType;
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

  private void setColType(ColType colType) {
    this.colType = colType;
  }

  private void setName(String name) {
    this.name = name;
  }
}
