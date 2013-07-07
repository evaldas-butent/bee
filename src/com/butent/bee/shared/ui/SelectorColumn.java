package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

public final class SelectorColumn implements BeeSerializable, HasInfo {

  private enum Serial {
    SOURCE, CLASSES, STYLE, HOR_ALIGN, VERT_ALIGN, RENDERER_DESCR, RENDER, RENDER_TOKENS, 
    ITEM_KEY, RENDER_COLUMNS
  }

  public static SelectorColumn create(Map<String, String> attributes,
      RendererDescription rendererDescription, Calculation render,
      List<RenderableToken> renderTokens) {

    SelectorColumn selectorColumn = new SelectorColumn();
    selectorColumn.setAttributes(attributes);

    if (rendererDescription != null) {
      selectorColumn.setRendererDescription(rendererDescription);
    }
    if (render != null) {
      selectorColumn.setRender(render);
    }
    if (!BeeUtils.isEmpty(renderTokens)) {
      selectorColumn.setRenderTokens(renderTokens);
    }
    
    return selectorColumn;
  }

  public static SelectorColumn restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    SelectorColumn column = new SelectorColumn();
    column.deserialize(s);
    return column;
  }

  private String source;

  private String classes;
  private String style;

  private String horAlign;
  private String vertAlign;

  private RendererDescription rendererDescription;
  private Calculation render;
  private List<RenderableToken> renderTokens;

  private String itemKey;
  private List<String> renderColumns;
  
  private SelectorColumn() {
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
        case SOURCE:
          setSource(value);
          break;
        case CLASSES:
          setClasses(value);
          break;
        case STYLE:
          setStyle(value);
          break;
        case HOR_ALIGN:
          setHorAlign(value);
          break;
        case VERT_ALIGN:
          setVertAlign(value);
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

  public String getClasses() {
    return classes;
  }

  public String getHorAlign() {
    return horAlign;
  }

  @Override
  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties(
        "Source", getSource(),
        "Classes", getClasses(),
        "Style", getStyle(),
        "Horizontal Alignment", getHorAlign(),
        "Vertical Alignment", getVertAlign(),
        "Item Key", getItemKey(),
        "Render Columns", getRenderColumns());

    if (getRendererDescription() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Renderer",
          getRendererDescription().getInfo());
    }
    if (getRender() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Render", getRender().getInfo());
    }
    if (getRenderTokens() != null) {
      PropertyUtils.appendWithIndex(info, "Render Tokens", "token", getRenderTokens());
    }

    PropertyUtils.addWhenEmpty(info, getClass());
    return info;
  }

  public String getItemKey() {
    return itemKey;
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

  public String getSource() {
    return source;
  }

  public String getStyle() {
    return style;
  }

  public String getVertAlign() {
    return vertAlign;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case SOURCE:
          arr[i++] = getSource();
          break;
        case CLASSES:
          arr[i++] = getClasses();
          break;
        case STYLE:
          arr[i++] = getStyle();
          break;
        case HOR_ALIGN:
          arr[i++] = getHorAlign();
          break;
        case VERT_ALIGN:
          arr[i++] = getVertAlign();
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
        case RENDER_COLUMNS:
          arr[i++] = getRenderColumns();
          break;
      }
    }
    return Codec.beeSerialize(arr);
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

      if (BeeUtils.same(key, UiConstants.ATTR_SOURCE)) {
        setSource(value.trim());

      } else if (BeeUtils.same(key, UiConstants.ATTR_CLASS)) {
        setClasses(value.trim());
      } else if (BeeUtils.same(key, UiConstants.ATTR_STYLE)) {
        setStyle(value.trim());

      } else if (BeeUtils.same(key, UiConstants.ATTR_HORIZONTAL_ALIGNMENT)) {
        setHorAlign(value.trim());
      } else if (BeeUtils.same(key, UiConstants.ATTR_VERTICAL_ALIGNMENT)) {
        setVertAlign(value.trim());

      } else if (BeeUtils.same(key, RendererDescription.ATTR_RENDER_COLUMNS)) {
        setRenderColumns(NameUtils.toList(value.trim()));
      } else if (BeeUtils.same(key, HasItems.ATTR_ITEM_KEY)) {
        setItemKey(value.trim());
      }
    }
  }

  public void setClasses(String classes) {
    this.classes = classes;
  }

  public void setHorAlign(String horAlign) {
    this.horAlign = horAlign;
  }

  public void setItemKey(String itemKey) {
    this.itemKey = itemKey;
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

  public void setSource(String source) {
    this.source = source;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public void setVertAlign(String vertAlign) {
    this.vertAlign = vertAlign;
  }
}
