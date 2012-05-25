package com.butent.bee.client.ui;

import com.google.common.collect.Lists;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.ConditionalStyleDeclaration;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.RefreshType;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.ui.RenderableToken;
import com.butent.bee.shared.ui.RendererDescription;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class WidgetDescription implements HasInfo {

  private static final String ATTR_PARENT = "parent";

  private static final String ATTR_MIN_VALUE = "minValue";
  private static final String ATTR_MAX_VALUE = "maxValue";
  private static final String ATTR_REQUIRED = "required";

  private static final String ATTR_ON_FOCUS = "onFocus";
  
  private final FormWidget widgetType;
  private final String widgetId;
  private final String widgetName;

  private String parentName = null;
  
  private Collection<ConditionalStyleDeclaration> dynStyles = null;

  private String source = null;
  private Relation relation = null;

  private RendererDescription rendererDescription = null;
  private Calculation render = null;
  private List<RenderableToken> renderTokens = null;

  private String renderColumns = null;
  private String itemKey = null;

  private String caption = null;
  private Boolean readOnly = null;
  
  private Calculation validation = null;
  private Calculation editable = null;
  private Calculation carry = null;

  private String minValue = null;
  private String maxValue = null;
  private Boolean required = null;
  private Boolean nullable = null;
  private Boolean hasDefaults = null;
  
  private boolean disablable = false;
  
  private EditorAction onFocus = null;
  private RefreshType updateMode = null;
  
  private AbstractCellRenderer renderer = null;
  
  public WidgetDescription(FormWidget widgetType, String widgetId, String widgetName) {
    this.widgetType = widgetType;
    this.widgetId = widgetId;
    this.widgetName = widgetName;
  }

  public void addRenderToken(RenderableToken renderToken) {
    if (renderToken != null) {
      if (getRenderTokens() == null) {
        this.renderTokens = Lists.newArrayList(renderToken);
      } else {
        getRenderTokens().add(renderToken);
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof WidgetDescription) {
      return BeeUtils.equalsTrim(getWidgetId(), ((WidgetDescription) obj).getWidgetId());
    } else {
      return false;
    }
  }

  public String getCaption() {
    return caption;
  }

  public Calculation getCarry() {
    return carry;
  }

  public Collection<ConditionalStyleDeclaration> getDynStyles() {
    return dynStyles;
  }

  public Calculation getEditable() {
    return editable;
  }

  public Boolean getHasDefaults() {
    return hasDefaults;
  }

  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties(
        "Widget Type", getWidgetType(),
        "Widget Id", getWidgetId(),
        "Widget Name", getWidgetName(),
        "Parent Name", getParentName(),
        "Caption", getCaption(),
        "Read Only", getReadOnly(),
        "Source", getSource(),
        "Min Value", getMinValue(),
        "Max Value", getMaxValue(),
        "Required", getRequired(),
        "Nullable", getNullable(),
        "Has Defaults", getHasDefaults(),
        "Render Columns", getRenderColumns(),
        "Item Key", getItemKey(),
        "On Focus", getOnFocus(),
        "Update Mode", getUpdateMode());

    if (getRelation() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Relation", getRelation().getInfo());
    }
    
    if (getValidation() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Validation", getValidation().getInfo());
    }
    if (getEditable() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Editable", getEditable().getInfo());
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

  public String getMinValue() {
    return minValue;
  }

  public Boolean getNullable() {
    return nullable;
  }

  public EditorAction getOnFocus() {
    return onFocus;
  }
  
  public String getParentName() {
    return parentName;
  }

  public Boolean getReadOnly() {
    return readOnly;
  }

  public Relation getRelation() {
    return relation;
  }

  public Calculation getRender() {
    return render;
  }

  public String getRenderColumns() {
    return renderColumns;
  }

  public AbstractCellRenderer getRenderer() {
    return renderer;
  }

  public RendererDescription getRendererDescription() {
    return rendererDescription;
  }

  public List<RenderableToken> getRenderTokens() {
    return renderTokens;
  }

  public Boolean getRequired() {
    return required;
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

  public String getWidgetId() {
    return widgetId;
  }

  public String getWidgetName() {
    return widgetName;
  }

  public FormWidget getWidgetType() {
    return widgetType;
  }

  @Override
  public int hashCode() {
    return (getWidgetId() == null) ? 0 : getWidgetId().trim().hashCode();
  }

  public boolean isDisablable() {
    return disablable;
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

      if (BeeUtils.same(key, ATTR_PARENT)) {
        setParentName(value.trim());

      } else if (BeeUtils.same(key, UiConstants.ATTR_CAPTION)) {
        setCaption(value.trim());
      } else if (BeeUtils.same(key, UiConstants.ATTR_READ_ONLY)) {
        setReadOnly(BeeUtils.toBooleanOrNull(value));
      } else if (BeeUtils.same(key, UiConstants.ATTR_SOURCE)) {
        setSource(value.trim());

      } else if (BeeUtils.same(key, ATTR_MIN_VALUE)) {
        setMinValue(value.trim());
      } else if (BeeUtils.same(key, ATTR_MAX_VALUE)) {
        setMaxValue(value.trim());
      } else if (BeeUtils.same(key, ATTR_REQUIRED)) {
        setRequired(BeeUtils.toBooleanOrNull(value));

      } else if (BeeUtils.same(key, RendererDescription.ATTR_RENDER_COLUMNS)) {
        setRenderColumns(value.trim());
      } else if (BeeUtils.same(key, HasItems.ATTR_ITEM_KEY)) {
        setItemKey(value.trim());

      } else if (BeeUtils.same(key, ATTR_ON_FOCUS)) {
        setOnFocus(EditorAction.getByCode(value));
      } else if (BeeUtils.same(key, RefreshType.ATTR_UPDATE_MODE)) {
        setUpdateMode(RefreshType.getByCode(value));
      }
    }
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setCarry(Calculation carry) {
    this.carry = carry;
  }

  public void setDisablable(boolean disablable) {
    this.disablable = disablable;
  }

  public void setDynStyles(Collection<ConditionalStyleDeclaration> dynStyles) {
    this.dynStyles = dynStyles;
  }

  public void setEditable(Calculation editable) {
    this.editable = editable;
  }

  public void setHasDefaults(Boolean hasDefaults) {
    this.hasDefaults = hasDefaults;
  }

  public void setItemKey(String itemKey) {
    this.itemKey = itemKey;
  }

  public void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
  }

  public void setMinValue(String minValue) {
    this.minValue = minValue;
  }

  public void setNullable(Boolean nullable) {
    this.nullable = nullable;
  }

  public void setOnFocus(EditorAction onFocus) {
    this.onFocus = onFocus;
  }

  public void setParentName(String parentName) {
    this.parentName = parentName;
  }

  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setRelation(Relation relation) {
    this.relation = relation;
  }

  public void setRender(Calculation render) {
    this.render = render;
  }

  public void setRenderColumns(String renderColumns) {
    this.renderColumns = renderColumns;
  }

  public void setRenderer(AbstractCellRenderer renderer) {
    this.renderer = renderer;
  }

  public void setRendererDescription(RendererDescription rendererDescription) {
    this.rendererDescription = rendererDescription;
  }

  public void setRequired(Boolean required) {
    this.required = required;
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
}
