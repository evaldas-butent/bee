package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;

/**
 * Contains column properties and methods for managing them.
 */

public class ColumnDescription implements BeeSerializable, HasInfo, HasOptions {

  public enum ColType {
    DATA("DataColumn", false),
    RELATED("RelColumn", false),
    CALCULATED("CalcColumn", true),
    ID("IdColumn", true),
    VERSION("VerColumn", true),
    SELECTION("SelectionColumn", true),
    ACTION("ActionColumn", true);

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

  private enum Serial {
    COL_TYPE, NAME, CAPTION, READ_ONLY, WIDTH, SOURCE, RELATION,
    MIN_WIDTH, MAX_WIDTH, SORTABLE, VISIBLE, FORMAT, HOR_ALIGN, HAS_FOOTER, SHOW_WIDTH,
    VALIDATION, EDITABLE, CARRY, EDITOR, MIN_VALUE, MAX_VALUE, REQUIRED, ITEM_KEY,
    RENDERER_DESCR, RENDER, RENDER_TOKENS, VALUE_TYPE, PRECISION, SCALE, RENDER_COLUMNS,
    SEARCH_BY, SORT_BY, HEADER_STYLE, BODY_STYLE, FOOTER_STYLE, DYN_STYLES, CELL_TYPE,
    UPDATE_MODE, AUTO_FIT, OPTIONS, ELEMENT_TYPE
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
  private Relation relation = null;

  private Integer minWidth = null;
  private Integer maxWidth = null;
  private String autoFit = null;

  private Boolean sortable = null;
  private Boolean visible = null;

  private String format = null;
  private String horAlign = null;

  private Boolean hasFooter = null;
  private Boolean showWidth = null;

  private Calculation validation = null;
  private Calculation editable = null;
  private Calculation carry = null;

  private RendererDescription rendererDescription = null;
  private Calculation render = null;
  private List<RenderableToken> renderTokens = null;

  private EditorDescription editor = null;

  private String itemKey = null;
  
  private String minValue = null;
  private String maxValue = null;
  
  private Boolean required = null;

  private ValueType valueType = null;
  private Integer precision = null;
  private Integer scale = null;
  
  private List<String> renderColumns = null;
  private String sortBy = null;
  private String searchBy = null;

  private StyleDeclaration headerStyle = null;
  private StyleDeclaration bodyStyle = null;
  private StyleDeclaration footerStyle = null;

  private Collection<ConditionalStyleDeclaration> dynStyles = null;

  private CellType cellType = null;
  private RefreshType updateMode = null;
  private String elementType = null;

  private String options = null;
  
  private boolean relationInitialized = false;
  
  public ColumnDescription(ColType colType, String name) {
    Assert.notNull(colType);
    Assert.notEmpty(name);
    this.colType = colType;
    this.name = name;
    
    setVisible(true);
  }

  private ColumnDescription() {
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
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
        case RELATION:
          setRelation(Relation.restore(value));
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
        case SEARCH_BY:
          setSearchBy(value);
          break;
        case SORT_BY:
          setSortBy(value);
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
        case REQUIRED:
          setRequired(BeeUtils.toBooleanOrNull(value));
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
          String[] scs = Codec.beeDeserializeCollection(value);
          if (ArrayUtils.isEmpty(scs)) {
            setDynStyles(null);
          } else {
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
        case UPDATE_MODE:
          setUpdateMode(RefreshType.getByCode(value));
          break;
        case RENDERER_DESCR:
          setRendererDescription(RendererDescription.restore(value));
          break;
        case RENDER:
          setRender(Calculation.restore(value));
          break;
        case RENDER_TOKENS:
          setRenderTokens(RenderableToken.restoreList(value));
          break;
        case ITEM_KEY:
          setItemKey(value);
          break;
        case AUTO_FIT:
          setAutoFit(value);
          break;
        case OPTIONS:
          setOptions(value);
          break;
        case ELEMENT_TYPE:
          setElementType(value);
          break;
        case RENDER_COLUMNS:
          String[] cols = Codec.beeDeserializeCollection(value);
          if (ArrayUtils.isEmpty(cols)) {
            setRenderColumns(null);
          } else {
            setRenderColumns(Lists.newArrayList(cols));
          }
          break;
      }
    }
  }

  public String getAutoFit() {
    return autoFit;
  }

  public StyleDeclaration getBodyStyle() {
    return bodyStyle;
  }

  public String getCaption() {
    return caption;
  }

  public Calculation getCarry() {
    return carry;
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

  public String getElementType() {
    return elementType;
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

  @Override
  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties(
        "Col Type", getColType(),
        "Name", getName(),
        "Caption", getCaption(),
        "Read Only", isReadOnly(),
        "Width", getWidth(),
        "Source", getSource(),
        "Min Width", getMinWidth(),
        "Max Width", getMaxWidth(),
        "Auto Fit", getAutoFit(),
        "Sortable", isSortable(),
        "Visible", isVisible(),
        "Format", getFormat(),
        "Horizontal Alignment", getHorAlign(),
        "Has Footer", hasFooter(),
        "Show Width", showWidth(),
        "Min Value", getMinValue(),
        "Max Value", getMaxValue(),
        "Required", isRequired(),
        "Value Type", getValueType(),
        "Precision", getPrecision(),
        "Scale", getScale(),
        "Render Columns", getRenderColumns(),
        "Search By", getSearchBy(),
        "Sort By", getSortBy(),
        "Cell Type", getCellType(),
        "Update Mode", getUpdateMode(),
        "Item Key", getItemKey(),
        "Element Type", getElementType(),
        "Options", getOptions());

    if (getRelation() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Relation", getRelation().getInfo());
      PropertyUtils.addProperty(info, "Relation Initialized", isRelationInitialized());
    }
    
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

    if (getRendererDescription() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Renderer", getRendererDescription().getInfo());
    }
    if (getRender() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Render", getRender().getInfo());
    }
    if (getRenderTokens() != null) {
      PropertyUtils.appendWithIndex(info, "Render Tokens", "token", getRenderTokens());
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

  public String getItemKey() {
    return itemKey;
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

  @Override
  public String getOptions() {
    return options;
  }

  public Integer getPrecision() {
    return precision;
  }

  public Relation getRelation() {
    return relation;
  }

  public Calculation getRender() {
    return render;
  }

  public List<String> getRenderColumns() {
    return renderColumns;
  }
  
  public RendererDescription getRendererDescription() {
    return rendererDescription;
  }
  
  public List<RenderableToken> getRenderTokens() {
    return renderTokens;
  }

  public Integer getScale() {
    return scale;
  }

  public String getSearchBy() {
    return searchBy;
  }

  public String getSortBy() {
    return sortBy;
  }

  public String getSource() {
    return source;
  }

  public RefreshType getUpdateMode() {
    return updateMode;
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

  public boolean isRelationInitialized() {
    return relationInitialized;
  }

  public Boolean isRequired() {
    return required;
  }

  public Boolean isSortable() {
    return sortable;
  }

  public Boolean isVisible() {
    return visible;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
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
        case RELATION:
          arr[i++] = getRelation();
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
        case SEARCH_BY:
          arr[i++] = getSearchBy();
          break;
        case SORT_BY:
          arr[i++] = getSortBy();
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
        case REQUIRED:
          arr[i++] = isRequired();
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
        case UPDATE_MODE:
          arr[i++] = (getUpdateMode() == null) ? null : getUpdateMode().getCode();
          break;
        case RENDERER_DESCR:
          arr[i++] = getRendererDescription();
          break;
        case RENDER:
          arr[i++] = getRender();
          break;
        case RENDER_TOKENS:
          arr[i++] = getRenderTokens();
          break;
        case ITEM_KEY:
          arr[i++] = getItemKey();
          break;
        case AUTO_FIT:
          arr[i++] = getAutoFit();
          break;
        case OPTIONS:
          arr[i++] = getOptions();
          break;
        case ELEMENT_TYPE:
          arr[i++] = getElementType();
          break;
        case RENDER_COLUMNS:
          arr[i++] = getRenderColumns();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setAutoFit(String autoFit) {
    this.autoFit = autoFit;
  }

  public void setBodyStyle(StyleDeclaration bodyStyle) {
    this.bodyStyle = bodyStyle;
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

  public void setElementType(String elementType) {
    this.elementType = elementType;
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

  public void setItemKey(String itemKey) {
    this.itemKey = itemKey;
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

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  public void setPrecision(Integer precision) {
    this.precision = precision;
  }

  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }
  
  public void setRelation(Relation relation) {
    this.relation = relation;
  }

  public void setRelationInitialized(boolean relationInitialized) {
    this.relationInitialized = relationInitialized;
  }

  public void setRender(Calculation render) {
    this.render = render;
  }

  public void setRenderColumns(List<String> renderColumns) {
    this.renderColumns = renderColumns;
  }

  public void setRendererDescription(RendererDescription rendererDescription) {
    this.rendererDescription = rendererDescription;
  }

  public void setRenderTokens(List<RenderableToken> renderTokens) {
    this.renderTokens = renderTokens;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }

  public void setScale(Integer scale) {
    this.scale = scale;
  }
  
  public void setSearchBy(String searchBy) {
    this.searchBy = searchBy;
  }

  public void setShowWidth(Boolean showWidth) {
    this.showWidth = showWidth;
  }

  public void setSortable(Boolean sortable) {
    this.sortable = sortable;
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  public void setSource(String source) {
    this.source = source;
  }
  
  public void setUpdateMode(RefreshType updateMode) {
    this.updateMode = updateMode;
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

  private void setColType(ColType colType) {
    this.colType = colType;
  }

  private void setName(String name) {
    this.name = name;
  }
}
