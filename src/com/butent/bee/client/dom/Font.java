package com.butent.bee.client.dom;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.dom.StyleUtils.FontSize;
import com.butent.bee.client.dom.StyleUtils.FontVariant;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.RangeMap;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

/**
 * Enables to operate with various parameters of fonts used by the system.
 */

public class Font implements HasInfo, Transformable {

  private static final double UNKNOWN = -1.0;
  private static final RangeMap<Unit> DEFAULT_UNITS =
    RangeMap.create(null, 4.0, Unit.EM, 4.0, null, Unit.PX);
  
  public static Font copyOf(Font original) {
    if (original == null) {
      return null;
    }
    return new Font(original.getStyle(), original.getVariant(), original.getWeight(),
        original.getAbsoluteSize(), original.getSizeValue(), original.getSizeUnit(),
        original.getFamily());
  }
  
  public static Font merge(Font... fonts) {
    if (fonts == null || fonts.length <= 0) {
      return null;
    }
    Font result = null;
    
    for (Font font : fonts) {
      if (font == null) {
        continue;
      }
      if (result == null) {
        result = copyOf(font);
        continue;
      }
      
      if (font.getStyle() != null) {
        result.setStyle(font.getStyle());
      }
      if (font.getVariant() != null) {
        result.setVariant(font.getVariant());
      }
      if (font.getWeight() != null) {
        result.setWeight(font.getWeight());
      }
      if (font.getAbsoluteSize() != null) {
        result.setAbsoluteSize(font.getAbsoluteSize());
      }
      if (font.getSizeValue() > 0) {
        result.setSizeValue(font.getSizeValue());
        result.setSizeUnit(font.getSizeUnit());
      }
      if (font.getFamily() != null) {
        result.setFamily(font.getFamily());
      }
    }
    return result;
  }

  public static Font parse(String input) {
    if (BeeUtils.isEmpty(input)) {
      return null;
    }
    return parse(BeeUtils.split(input, BeeConst.STRING_SPACE));
  }
  
  public static Font parse(String[] input) {
    if (input == null || input.length <= 0) {
      return null;
    }
    Font font = new Font();

    for (String s : input) {
      if (BeeUtils.isEmpty(s)) {
        continue;
      }

      FontStyle fontStyle = StyleUtils.parseFontStyle(s);
      if (fontStyle != null) {
        font.setStyle(fontStyle);
        continue;
      }

      FontWeight fontWeight = StyleUtils.parseFontWeight(s);
      if (fontWeight != null) {
        font.setWeight(fontWeight);
        continue;
      }

      FontSize fontSize = StyleUtils.parseFontSize(s);
      if (fontSize != null) {
        font.setAbsoluteSize(fontSize);
        continue;
      }

      FontVariant fontVariant = StyleUtils.parseFontVariant(s);
      if (fontVariant != null) {
        font.setVariant(fontVariant);
        continue;
      }

      Unit unit = StyleUtils.parseUnit(s);
      if (unit != null) {
        font.setSizeUnit(unit);
        continue;
      }

      if (BeeUtils.isDouble(s)) {
        font.setSizeValue(BeeUtils.toDouble(s));
        continue;
      }

      font.setFamily(s);
    }
    return font;
  }

  private FontStyle style;
  private FontVariant variant;
  private FontWeight weight;
  private FontSize absoluteSize;
  private double sizeValue;
  private Unit sizeUnit;
  private String family;

  public Font(double sizeValue, Unit sizeUnit) {
    this(null, null, null, null, sizeValue, sizeUnit, null);
  }

  public Font(FontSize absoluteSize) {
    this(null, null, null, absoluteSize, UNKNOWN, null, null);
  }

  public Font(FontStyle style) {
    this(style, null, null, null, UNKNOWN, null, null);
  }

  public Font(FontStyle style, FontVariant variant) {
    this(style, variant, null, null, UNKNOWN, null, null);
  }

  public Font(FontStyle style, FontVariant variant, FontWeight weight) {
    this(style, variant, weight, null, UNKNOWN, null, null);
  }

  public Font(FontStyle style, FontVariant variant, FontWeight weight,
      double sizeValue, Unit sizeUnit) {
    this(style, variant, weight, null, sizeValue, sizeUnit, null);
  }

  public Font(FontStyle style, FontVariant variant, FontWeight weight,
      double sizeValue, Unit sizeUnit, String family) {
    this(style, variant, weight, null, sizeValue, sizeUnit, family);
  }

  public Font(FontStyle style, FontVariant variant, FontWeight weight, FontSize absoluteSize) {
    this(style, variant, weight, absoluteSize, UNKNOWN, null, null);
  }

  public Font(FontStyle style, FontVariant variant, FontWeight weight, FontSize absoluteSize,
      String family) {
    this(style, variant, weight, absoluteSize, UNKNOWN, null, family);
  }

  public Font(FontVariant variant) {
    this(null, variant, null, null, UNKNOWN, null, null);
  }

  public Font(FontWeight weight) {
    this(null, null, weight, null, UNKNOWN, null, null);
  }

  public Font(String family) {
    this(null, null, null, null, UNKNOWN, null, family);
  }

  private Font() {
    this(null, null, null, null, UNKNOWN, null, null);
  }

  private Font(FontStyle style, FontVariant variant, FontWeight weight, FontSize absoluteSize,
      double sizeValue, Unit sizeUnit, String family) {
    this.style = style;
    this.variant = variant;
    this.weight = weight;
    this.absoluteSize = absoluteSize;
    this.sizeValue = sizeValue;
    this.sizeUnit = sizeUnit;
    this.family = family;
  }

  public void applyTo(Element el) {
    Assert.notNull(el);
    applyTo(el.getStyle());
  }

  public void applyTo(Style st) {
    Assert.notNull(st);

    if (getStyle() != null) {
      st.setFontStyle(getStyle());
    }
    if (getVariant() != null) {
      StyleUtils.setFontVariant(st, getVariant());
    }
    if (getWeight() != null) {
      st.setFontWeight(getWeight());
    }

    if (getSizeValue() > 0 && getSizeUnit() != null) {
      st.setFontSize(getSizeValue(), getSizeUnit());
    } else if (getAbsoluteSize() != null) {
      StyleUtils.setFontSize(st, getAbsoluteSize());
    } else if (getSizeValue() > 0) {
      st.setFontSize(getSizeValue(), DEFAULT_UNITS.get(getSizeValue()));
    }

    if (!BeeUtils.isEmpty(getFamily())) {
      StyleUtils.setFontFamily(st, getFamily());
    }
  }

  public void applyTo(UIObject obj) {
    Assert.notNull(obj);
    applyTo(obj.getElement());
  }

  public SafeStyles buildCss() {
    if (isEmpty()) {
      return null;
    }
    
    SafeStylesBuilder builder = new SafeStylesBuilder();

    if (getStyle() != null) {
      builder.append(StyleUtils.buildFontStyle(getStyle()));
    }
    if (getVariant() != null) {
      builder.append(StyleUtils.buildFontVariant(getVariant()));
    }
    if (getWeight() != null) {
      builder.append(StyleUtils.buildFontWeight(getWeight()));
    }

    if (getSizeValue() > 0 && getSizeUnit() != null) {
      builder.append(StyleUtils.buildFontSize(getSizeValue(), getSizeUnit()));
    } else if (getAbsoluteSize() != null) {
      builder.append(StyleUtils.buildFontSize(getAbsoluteSize()));
    } else if (getSizeValue() > 0) {
      builder.append(StyleUtils.buildFontSize(getSizeValue(), DEFAULT_UNITS.get(getSizeValue())));
    }
    
    if (!BeeUtils.isEmpty(getFamily())) {
      builder.append(StyleUtils.buildFontFamily(getFamily()));
    }
    return builder.toSafeStyles();
  }
  
  public FontSize getAbsoluteSize() {
    return absoluteSize;
  }

  public String getFamily() {
    return family;
  }

  public List<Property> getInfo() {
    List<Property> info = Lists.newArrayList();

    if (getStyle() != null) {
      info.add(new Property("Font Style", getStyle().getCssName()));
    }
    if (getVariant() != null) {
      info.add(new Property("Font Variant", getVariant().getCssName()));
    }
    if (getWeight() != null) {
      info.add(new Property("Font Weight", getWeight().getCssName()));
    }

    if (getAbsoluteSize() != null) {
      info.add(new Property("Font Absolute Size", getAbsoluteSize().getCssName()));
    }
    if (getSizeValue() > 0) {
      info.add(new Property("Font Size Value", BeeUtils.toString(getSizeValue())));
    }
    if (getSizeUnit() != null) {
      info.add(new Property("Font Size Unit", getSizeUnit().getType()));
    }

    if (!BeeUtils.isEmpty(getFamily())) {
      info.add(new Property("Font Family", getFamily()));
    }

    PropertyUtils.addWhenEmpty(info, getClass());
    return info;
  }

  public Unit getSizeUnit() {
    return sizeUnit;
  }

  public double getSizeValue() {
    return sizeValue;
  }

  public FontStyle getStyle() {
    return style;
  }

  public FontVariant getVariant() {
    return variant;
  }

  public FontWeight getWeight() {
    return weight;
  }

  public boolean isEmpty() {
    return getStyle() == null && getVariant() == null && getWeight() == null
        && getAbsoluteSize() == null && getSizeValue() <= 0 && getFamily() == null;
  }

  public void removeFrom(Element el) {
    Assert.notNull(el);
    removeFrom(el.getStyle());
  }

  public void removeFrom(Style st) {
    Assert.notNull(st);

    if (getStyle() != null) {
      st.clearFontStyle();
    }
    if (getVariant() != null) {
      st.clearProperty(StyleUtils.STYLE_FONT_VARIANT);
    }
    if (getWeight() != null) {
      st.clearFontWeight();
    }

    if (getAbsoluteSize() != null || getSizeValue() > 0) {
      st.clearFontSize();
    }

    if (!BeeUtils.isEmpty(getFamily())) {
      st.clearProperty(StyleUtils.STYLE_FONT_FAMILY);
    }
  }

  public void removeFrom(UIObject obj) {
    Assert.notNull(obj);
    removeFrom(obj.getElement());
  }

  public void setAbsoluteSize(FontSize absoluteSize) {
    this.absoluteSize = absoluteSize;
  }

  public void setFamily(String family) {
    this.family = family;
  }

  public void setSizeUnit(Unit sizeUnit) {
    this.sizeUnit = sizeUnit;
  }

  public void setSizeValue(double sizeValue) {
    this.sizeValue = sizeValue;
  }

  public void setStyle(FontStyle style) {
    this.style = style;
  }

  public void setVariant(FontVariant variant) {
    this.variant = variant;
  }

  public void setWeight(FontWeight weight) {
    this.weight = weight;
  }

  @Override
  public String transform() {
    SafeStyles css = buildCss();
    if (css == null) {
      return "Font instance is empty"; 
    }
    return css.asString();
  }
}