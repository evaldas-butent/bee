package com.butent.bee.client.dom;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;

import java.util.Collection;
import java.util.List;

/**
 * Contains utility functions used for working with Cascade Style Sheets (CSS).
 */

public class StyleUtils {

  /**
   * Contains possible font sizes.
   */

  public enum FontSize implements HasCssName {
    XX_SMALL {
      public String getCssName() {
        return FONT_SIZE_XX_SMALL;
      }
    },
    X_SMALL {
      public String getCssName() {
        return FONT_SIZE_X_SMALL;
      }
    },
    SMALL {
      public String getCssName() {
        return FONT_SIZE_SMALL;
      }
    },
    MEDIUM {
      public String getCssName() {
        return FONT_SIZE_MEDIUM;
      }
    },
    LARGE {
      public String getCssName() {
        return FONT_SIZE_LARGE;
      }
    },
    X_LARGE {
      public String getCssName() {
        return FONT_SIZE_X_LARGE;
      }
    },
    XX_LARGE {
      public String getCssName() {
        return FONT_SIZE_XX_LARGE;
      }
    },

    SMALLER {
      public String getCssName() {
        return FONT_SIZE_SMALLER;
      }
    },
    LARGER {
      public String getCssName() {
        return FONT_SIZE_LARGER;
      }
    }
  }

  /**
   * Contains possible font variations, like normal and small caps.
   */

  public enum FontVariant implements HasCssName {
    NORMAL {
      public String getCssName() {
        return FONT_VARIANT_NORMAL;
      }
    },
    SMALL_CAPS {
      public String getCssName() {
        return FONT_VARIANT_SMALL_CAPS;
      }
    }
  }

  /**
   * Contains possible scroll bar configurations, from none, to vertical, horizontal and both.
   */

  public enum ScrollBars {
    NONE, HORIZONTAL, VERTICAL, BOTH
  }

  /**
   * Specifies available ways how white space inside an element is handled.
   */

  public enum WhiteSpace implements HasCssName {
    NORMAL {
      public String getCssName() {
        return WHITE_SPACE_NORMAL;
      }
    },
    NOWRAP {
      public String getCssName() {
        return WHITE_SPACE_NOWRAP;
      }
    },
    PRE {
      public String getCssName() {
        return WHITE_SPACE_PRE;
      }
    },
    PRE_LINE {
      public String getCssName() {
        return WHITE_SPACE_PRE_LINE;
      }
    },
    PRE_WRAP {
      public String getCssName() {
        return WHITE_SPACE_PRE_WRAP;
      }
    }
  }

  public static final String ACTIVE_BLANK = "bee-activeBlank";
  public static final String ACTIVE_CONTENT = "bee-activeContent";

  public static final String CONFIG_PANEL = "bee-configPanel";
  
  public static final String DND_SOURCE = "bee-dndSource";
  public static final String DND_OVER = "bee-dndOver";

  public static final String WINDOW_CAPTION = "bee-WindowCaption";
  public static final String WINDOW_MESSAGE = "bee-WindowMessage";
  public static final String WINDOW_HEADER = "bee-WindowHeader";
  public static final String WINDOW_FOOTER = "bee-WindowFooter";

  public static final String STYLE_WIDTH = "width";
  public static final String STYLE_MIN_WIDTH = "minWidth";
  public static final String STYLE_MAX_WIDTH = "maxWidth";

  public static final String STYLE_HEIGHT = "height";
  public static final String STYLE_MIN_HEIGHT = "minHeight";
  public static final String STYLE_MAX_HEIGHT = "maxHeight";

  public static final String STYLE_LEFT = "left";
  public static final String STYLE_RIGHT = "right";
  public static final String STYLE_TOP = "top";
  public static final String STYLE_BOTTOM = "bottom";

  public static final String STYLE_BORDER_LEFT = "borderLeft";
  public static final String STYLE_BORDER_RIGHT = "borderRight";
  public static final String STYLE_BORDER_TOP = "borderTop";
  public static final String STYLE_BORDER_BOTTOM = "borderBottom";

  public static final String STYLE_BORDER_WIDTH = "borderWidth";
  public static final String STYLE_BORDER_STYLE = "borderStyle";
  public static final String STYLE_BORDER_COLOR = "borderColor";

  public static final String STYLE_OUTLINE_WIDTH = "outlineWidth";
  public static final String STYLE_OUTLINE_STYLE = "outlineStyle";
  public static final String STYLE_OUTLINE_COLOR = "outlineColor";
  
  public static final String STYLE_BORDER_COLLAPSE = "borderCollapse";
  public static final String STYLE_TABLE_LAYOUT = "tableLayout";
  public static final String STYLE_VERTICAL_ALIGN = "verticalAlign";

  public static final String STYLE_OVERFLOW = "overflow";
  public static final String STYLE_OVERFLOW_X = "overflowX";
  public static final String STYLE_OVERFLOW_Y = "overflowY";

  public static final String STYLE_FONT_STYLE = "fontStyle";
  public static final String STYLE_FONT_VARIANT = "fontVariant";
  public static final String STYLE_FONT_WEIGHT = "fontWeight";
  public static final String STYLE_FONT_SIZE = "fontSize";
  public static final String STYLE_FONT_FAMILY = "fontFamily";

  public static final String STYLE_WHITE_SPACE = "whiteSpace";

  public static final String STYLE_PADDING = "padding";
  public static final String STYLE_PADDING_TOP = "paddingTop";
  public static final String STYLE_PADDING_RIGHT = "paddingRight";
  public static final String STYLE_PADDING_LEFT = "paddingLeft";
  public static final String STYLE_PADDING_BOTTOM = "paddingBottom";

  public static final String STYLE_MARGIN = "margin";
  public static final String STYLE_MARGIN_TOP = "marginTop";
  public static final String STYLE_MARGIN_RIGHT = "marginRight";
  public static final String STYLE_MARGIN_LEFT = "marginLeft";
  public static final String STYLE_MARGIN_BOTTOM = "marginBottom";

  public static final String STYLE_Z_INDEX = "zIndex";

  public static final String STYLE_POSITION = "position";

  public static final String STYLE_BACKGROUND_IMAGE = "backgroundImage";
  public static final String STYLE_BACKGROUND_COLOR = "backgroundColor";
  public static final String STYLE_COLOR = "color";

  public static final String STYLE_CURSOR = "cursor";

  public static final String STYLE_TEXT_ALIGN = "textAlign";
  public static final String STYLE_TEXT_DECORATION = "textDecoration";

  public static final String STYLE_LIST_IMAGE = "listStyleImage";
  public static final String STYLE_LIST_POSITION = "listStylePosition";
  public static final String STYLE_LIST_TYPE = "listStyleType";
  
  public static final String VALUE_AUTO = "auto";
  public static final String VALUE_FIXED = "fixed";
  public static final String VALUE_HIDDEN = "hidden";
  public static final String VALUE_INHERIT = "inherit";
  public static final String VALUE_SCROLL = "scroll";

  public static final String NAME_HORIZONTAL = "horizontal";
  public static final String NAME_VERTICAL = "vertical";
  public static final String NAME_DISABLED = "disabled";
  public static final String NAME_ENABLED = "enabled";
  public static final String NAME_FOCUSED = "focused";

  public static final String NAME_UNSELECTABLE = "unselectable";

  public static final String NAME_CONTENT_BOX = "contentBox";
  public static final String NAME_FLEX_BOX_HORIZONTAL = "flexBox-horizontal";
  public static final String NAME_FLEX_BOX_VERTICAL = "flexBox-vertical";
  public static final String NAME_FLEX_BOX_CENTER = "flexBox-center";
  public static final String NAME_FLEXIBLE = "flexible";
  public static final String NAME_OCCUPY = "occupy";

  public static final String NAME_SCARY = "bee-afraid";
  public static final String NAME_SUPER_SCARY = "bee-very-afraid";
  
  public static final String NAME_ERROR = "bee-error";
  public static final String NAME_REQUIRED = "bee-required";

  public static final String FONT_SIZE_XX_SMALL = "xx-small";
  public static final String FONT_SIZE_X_SMALL = "x-small";
  public static final String FONT_SIZE_SMALL = "small";
  public static final String FONT_SIZE_MEDIUM = "medium";
  public static final String FONT_SIZE_LARGE = "large";
  public static final String FONT_SIZE_X_LARGE = "x-large";
  public static final String FONT_SIZE_XX_LARGE = "xx-large";

  public static final String FONT_SIZE_SMALLER = "smaller";
  public static final String FONT_SIZE_LARGER = "larger";

  public static final String FONT_VARIANT_NORMAL = "normal";
  public static final String FONT_VARIANT_SMALL_CAPS = "small-caps";

  public static final String WHITE_SPACE_NORMAL = "normal";
  public static final String WHITE_SPACE_NOWRAP = "nowrap";
  public static final String WHITE_SPACE_PRE = "pre";
  public static final String WHITE_SPACE_PRE_WRAP = "pre-wrap";
  public static final String WHITE_SPACE_PRE_LINE = "pre-line";

  public static final String BORDER_COLLAPSE = "collapse";
  public static final String BORDER_SEPARATE = "separate";

  public static final String POSITION_ABSOLUTE = "absolute";
  
  public static final String CSS_BORDER_WIDTH = "border-width";
  public static final String CSS_BORDER_LEFT_WIDTH = "border-left-width";
  public static final String CSS_BORDER_RIGHT_WIDTH = "border-right-width";
  public static final String CSS_BORDER_TOP_WIDTH = "border-top-width";
  public static final String CSS_BORDER_BOTTOM_WIDTH = "border-bottom-width";

  public static final String CSS_TEXT_ALIGN = "text-align";
  public static final String CSS_Z_INDEX = "z-index";

  public static final String CSS_FONT_STYLE = "font-style";
  public static final String CSS_FONT_VARIANT = "font-variant";
  public static final String CSS_FONT_WEIGHT = "font-weight";
  public static final String CSS_FONT_SIZE = "font-size";
  public static final String CSS_FONT_FAMILY = "font-family";
  
  public static final SafeStyles PREFAB_POSITION_ABSOLUTE =
      buildStyle(STYLE_POSITION, POSITION_ABSOLUTE);
  
  private static final char CLASS_NAME_SEPARATOR = ' ';
  private static final Splitter CLASS_NAME_SPLITTER =
      Splitter.on(CLASS_NAME_SEPARATOR).omitEmptyStrings().trimResults();
  
  private static final char ADD_CLASS = '+';
  private static final char REMOVE_CLASS = '-';
  private static final char REPLACE_CLASS = '=';

  private static final String DEFINITION_SEPARATOR = ";";
  private static final String NAME_VALUE_SEPARATOR = ":";
  
  private static final Splitter DEFINITION_SPLITTER =
    Splitter.on(DEFINITION_SEPARATOR).omitEmptyStrings().trimResults();
  
  private static final String PROPERTY_CSS_TEXT = "cssText";

  private static final char NAME_DELIMITER = '-';

  public static int addClassName(NodeList<Element> nodes, String className) {
    Assert.notNull(nodes);
    Assert.notEmpty(className);

    int cnt = nodes.getLength();
    for (int i = 0; i < cnt; i++) {
      nodes.getItem(i).addClassName(className);
    }
    return cnt;
  }

  public static void addStyleDependentName(Element el, String style) {
    setStyleDependentName(el, style, true);
  }
  
  public static void addStyleName(String id, String style) {
    setStyleName(DomUtils.getElement(id), style, true);
  }

  public static void alwaysScroll(Element el, ScrollBars scroll) {
    Assert.notNull(el);
    alwaysScroll(el.getStyle(), scroll);
  }

  public static void alwaysScroll(Style st, ScrollBars scroll) {
    setOverflow(st, scroll, VALUE_SCROLL);
  }

  public static void alwaysScroll(UIObject obj, ScrollBars scroll) {
    Assert.notNull(obj);
    alwaysScroll(obj.getElement(), scroll);
  }
  
  public static void apply(Style st, String styles) {
    List<Property> properties = parseStyles(styles);
    if (properties != null) {
      for (Property property : properties) {
        st.setProperty(camelize(property.getName()), property.getValue());
      }
    }
  }

  public static void autoHeight(Element el) {
    Assert.notNull(el);
    autoHeight(el.getStyle());
  }

  public static void autoHeight(Style st) {
    Assert.notNull(st);
    st.setProperty(STYLE_HEIGHT, VALUE_AUTO);
  }

  public static void autoHeight(UIObject obj) {
    Assert.notNull(obj);
    autoHeight(obj.getElement());
  }

  public static void autoScroll(Element el, ScrollBars scroll) {
    Assert.notNull(el);
    autoScroll(el.getStyle(), scroll);
  }

  public static void autoScroll(Style st, ScrollBars scroll) {
    setOverflow(st, scroll, VALUE_AUTO);
  }

  public static void autoScroll(UIObject obj, ScrollBars scroll) {
    Assert.notNull(obj);
    autoScroll(obj.getElement(), scroll);
  }

  public static void autoWidth(Element el) {
    Assert.notNull(el);
    autoWidth(el.getStyle());
  }

  public static void autoWidth(Style st) {
    Assert.notNull(st);
    st.setProperty(STYLE_WIDTH, VALUE_AUTO);
  }

  public static void autoWidth(UIObject obj) {
    Assert.notNull(obj);
    autoWidth(obj.getElement());
  }

  public static SafeStyles buildBorderBottomWidth(double value, Unit unit) {
    return buildBorderBottomWidth(toCssLength(value, unit));
  }

  public static SafeStyles buildBorderBottomWidth(int width) {
    return buildBorderBottomWidth(width, Unit.PX);
  }

  public static SafeStyles buildBorderBottomWidth(String value) {
    return buildStyle(CSS_BORDER_BOTTOM_WIDTH, value);
  }

  public static SafeStyles buildBorderLeftWidth(double value, Unit unit) {
    return buildBorderLeftWidth(toCssLength(value, unit));
  }

  public static SafeStyles buildBorderLeftWidth(int width) {
    return buildBorderLeftWidth(width, Unit.PX);
  }

  public static SafeStyles buildBorderLeftWidth(String value) {
    return buildStyle(CSS_BORDER_LEFT_WIDTH, value);
  }

  public static SafeStyles buildBorderRightWidth(double value, Unit unit) {
    return buildBorderRightWidth(toCssLength(value, unit));
  }

  public static SafeStyles buildBorderRightWidth(int width) {
    return buildBorderRightWidth(width, Unit.PX);
  }

  public static SafeStyles buildBorderRightWidth(String value) {
    return buildStyle(CSS_BORDER_RIGHT_WIDTH, value);
  }

  public static SafeStyles buildBorderTopWidth(double value, Unit unit) {
    return buildBorderTopWidth(toCssLength(value, unit));
  }

  public static SafeStyles buildBorderTopWidth(int width) {
    return buildBorderTopWidth(width, Unit.PX);
  }

  public static SafeStyles buildBorderTopWidth(String value) {
    return buildStyle(CSS_BORDER_TOP_WIDTH, value);
  }

  public static SafeStyles buildBorderWidth(double value, Unit unit) {
    return buildBorderWidth(toCssLength(value, unit));
  }

  public static SafeStyles buildBorderWidth(int width) {
    return buildBorderWidth(width, Unit.PX);
  }

  public static SafeStyles buildBorderWidth(String value) {
    return buildStyle(CSS_BORDER_WIDTH, value);
  }

  public static String buildClasses(String... styleNames) {
    return BeeUtils.concat(CLASS_NAME_SEPARATOR, styleNames);
  }

  public static SafeStyles buildFontFamily(String family) {
    return buildStyle(CSS_FONT_FAMILY, family);
  }

  public static SafeStyles buildFontSize(double size, Unit unit) {
    Assert.isPositive(size);
    Assert.notNull(unit);
    return buildStyle(CSS_FONT_SIZE, toCssLength(size, unit));
  }

  public static SafeStyles buildFontSize(FontSize size) {
    Assert.notNull(size);
    return buildStyle(CSS_FONT_SIZE, size.getCssName());
  }

  public static SafeStyles buildFontStyle(FontStyle style) {
    Assert.notNull(style);
    return buildStyle(CSS_FONT_STYLE, style.getCssName());
  }

  public static SafeStyles buildFontVariant(FontVariant variant) {
    Assert.notNull(variant);
    return buildStyle(CSS_FONT_VARIANT, variant.getCssName());
  }

  public static SafeStyles buildFontWeight(FontWeight weight) {
    Assert.notNull(weight);
    return buildStyle(CSS_FONT_WEIGHT, weight.getCssName());
  }

  public static SafeStyles buildHeight(double value, Unit unit) {
    return buildStyle(STYLE_HEIGHT, toCssLength(value, unit));
  }

  public static SafeStyles buildHeight(int height) {
    return buildHeight(height, Unit.PX);
  }
  
  public static SafeStyles buildLeft(double value, Unit unit) {
    return buildStyle(STYLE_LEFT, toCssLength(value, unit));
  }

  public static SafeStyles buildLeft(int left) {
    return buildLeft(left, Unit.PX);
  }

  public static SafeStyles buildMargin(String value) {
    return buildStyle(STYLE_MARGIN, value);
  }

  public static SafeStyles buildPadding(String value) {
    return buildStyle(STYLE_PADDING, value);
  }

  public static SafeStyles buildStyle(SafeStyles... styles) {
    Assert.notNull(styles);
    Assert.parameterCount(styles.length, 1);

    SafeStylesBuilder ssb = new SafeStylesBuilder();
    for (SafeStyles style : styles) {
      if (style != null) {
        ssb.append(style);
      }
    }
    return ssb.toSafeStyles();
  }

  public static SafeStyles buildStyle(String name, int value) {
    return buildStyle(name, BeeUtils.toString(value));
  }
  
  public static SafeStyles buildStyle(String name, String value) {
    Assert.notEmpty(name);
    Assert.notEmpty(value);
    return SafeStylesUtils.fromTrustedString(name + NAME_VALUE_SEPARATOR + value
        + DEFINITION_SEPARATOR);
  }

  public static SafeStyles buildTop(double value, Unit unit) {
    return buildStyle(STYLE_TOP, toCssLength(value, unit));
  }
  
  public static SafeStyles buildTop(int top) {
    return buildTop(top, Unit.PX);
  }
  
  public static SafeStyles buildWidth(double value, Unit unit) {
    return buildStyle(STYLE_WIDTH, toCssLength(value, unit));
  }

  public static SafeStyles buildWidth(int width) {
    return buildWidth(width, Unit.PX);
  }

  public static SafeStyles buildZIndex(int value) {
    return buildStyle(CSS_Z_INDEX, value);
  }
  
  public static void clearDisplay(Element el) {
    Assert.notNull(el);
    if (!BeeUtils.isEmpty(el.getStyle().getDisplay())) {
      el.getStyle().clearDisplay();
    }
  }

  public static void clearDisplay(String id) {
    clearDisplay(DomUtils.getElement(id));
  }

  public static void clearDisplay(UIObject obj) {
    Assert.notNull(obj);
    clearDisplay(obj.getElement());
  }

  public static void clearFont(Element el) {
    Assert.notNull(el);
    clearFont(el.getStyle());
  }
  
  public static void clearFont(Style st) {
    Assert.notNull(st);

    if (!BeeUtils.isEmpty(st.getFontStyle())) {
      st.clearFontStyle();
    }
    if (hasProperty(st, STYLE_FONT_VARIANT)) {
      st.clearProperty(STYLE_FONT_VARIANT);
    }
    if (!BeeUtils.isEmpty(st.getFontWeight())) {
      st.clearFontWeight();
    }

    if (!BeeUtils.isEmpty(st.getFontSize())) {
      st.clearFontSize();
    }
    if (hasProperty(st, STYLE_FONT_FAMILY)) {
      st.clearProperty(StyleUtils.STYLE_FONT_FAMILY);
    }
  }

  public static void clearFont(String id) {
    clearFont(DomUtils.getElement(id));
  }

  public static void clearFont(UIObject obj) {
    Assert.notNull(obj);
    clearFont(obj.getElement());
  }
  
  public static void clearTableLayout(Element el) {
    Assert.notNull(el);
    clearTableLayout(el.getStyle());
  }

  public static void clearTableLayout(Style st) {
    Assert.notNull(st);
    st.clearProperty(STYLE_TABLE_LAYOUT);
  }

  public static void clearTableLayout(UIObject obj) {
    Assert.notNull(obj);
    clearTableLayout(obj.getElement());
  }

  public static void collapseBorders(Element el) {
    Assert.notNull(el);
    collapseBorders(el.getStyle());
  }

  public static void collapseBorders(Style st) {
    Assert.notNull(st);
    st.setProperty(STYLE_BORDER_COLLAPSE, BORDER_COLLAPSE);
  }

  public static void collapseBorders(UIObject obj) {
    Assert.notNull(obj);
    collapseBorders(obj.getElement());
  }

  public static boolean containsClassName(Element el, String className) {
    if (el == null || BeeUtils.isEmpty(className)) {
      return false;
    }
    return indexOfClassName(className, el.getClassName()) >= 0;
  }

  public static void copyBorder(Style src, Style dst) {
    copyProperties(src, dst, STYLE_BORDER_WIDTH, STYLE_BORDER_STYLE, STYLE_BORDER_COLOR, 
        STYLE_BORDER_LEFT, STYLE_BORDER_RIGHT, STYLE_BORDER_TOP, STYLE_BORDER_BOTTOM);
  }

  public static void copyBox(Style src, Style dst) {
    copyRectangle(src, dst);
    copyPadding(src, dst);
    copyBorder(src, dst);
    copyMargin(src, dst);
  }

  public static void copyBox(Element src, Element dst) {
    Assert.notNull(src);
    Assert.notNull(dst);
    if (src.equals(dst)) {
      return;
    }
    copyBox(src.getStyle(), dst.getStyle());
  }

  public static void copyFont(Element src, Element dst) {
    Assert.notNull(src);
    Assert.notNull(dst);
    if (src.equals(dst)) {
      return;
    }
    copyFont(src.getStyle(), dst.getStyle());
  }
  
  public static void copyFont(Style src, Style dst) {
    copyProperties(src, dst, STYLE_FONT_STYLE, STYLE_FONT_VARIANT, STYLE_FONT_WEIGHT,
        STYLE_FONT_SIZE, STYLE_FONT_FAMILY);
  }
  
  public static void copyMargin(Style src, Style dst) {
    copyProperties(src, dst, STYLE_MARGIN,
        STYLE_MARGIN_LEFT, STYLE_MARGIN_RIGHT, STYLE_MARGIN_TOP, STYLE_MARGIN_BOTTOM);
  }

  public static void copyPadding(Style src, Style dst) {
    copyProperties(src, dst, STYLE_PADDING,
        STYLE_PADDING_LEFT, STYLE_PADDING_RIGHT, STYLE_PADDING_TOP, STYLE_PADDING_BOTTOM);
  }

  public static void copyProperties(Element src, Element dst, String... names) {
    Assert.notNull(src);
    Assert.notNull(dst);
    if (src.equals(dst)) {
      return;
    }
    copyProperties(src.getStyle(), dst.getStyle(), names);
  }
  
  public static void copyProperties(Style src, Style dst, String... names) {
    Assert.notNull(src);
    Assert.notNull(dst);
    Assert.notNull(names);
    Assert.isPositive(names.length);
    if (src.equals(dst)) {
      return;
    }
    
    for (String name : names) {
      if (!BeeUtils.isEmpty(name)) {
        copyStyleProperty(src, dst, name);
      }
    }
  }

  public static void copyRectangle(Element src, Element dst) {
    Assert.notNull(src);
    Assert.notNull(dst);
    if (src.equals(dst)) {
      return;
    }
    copyRectangle(src.getStyle(), dst.getStyle());
  }
  
  public static void copyRectangle(Style src, Style dst) {
    copyProperties(src, dst, STYLE_LEFT, STYLE_RIGHT, STYLE_TOP, STYLE_BOTTOM,
        STYLE_WIDTH, STYLE_HEIGHT);
  }

  public static void copySize(Element src, Element dst) {
    Assert.notNull(src);
    Assert.notNull(dst);
    if (src.equals(dst)) {
      return;
    }
    copySize(src.getStyle(), dst.getStyle());
  }

  public static void copySize(Style src, Style dst) {
    copyProperties(src, dst, STYLE_WIDTH, STYLE_HEIGHT);
  }
  
  public static void fillHorizontal(Element el) {
    Assert.notNull(el);
    fillHorizontal(el.getStyle());
  }

  public static void fillHorizontal(Style st) {
    Assert.notNull(st);

    if (!isZero(st.getLeft())) {
      st.setLeft(0, Unit.PX);
    }
    if (hasProperty(st, STYLE_RIGHT)) {
      st.clearRight();
    }

    st.setWidth(100, Unit.PCT);
  }

  public static void fillHorizontal(UIObject obj) {
    Assert.notNull(obj);
    fillHorizontal(obj.getElement());
  }

  public static void fillVertical(Element el) {
    Assert.notNull(el);
    fillVertical(el.getStyle());
  }

  public static void fillVertical(Style st) {
    Assert.notNull(st);

    if (!isZero(st.getTop())) {
      st.setTop(0, Unit.PX);
    }
    if (hasProperty(st, STYLE_BOTTOM)) {
      st.clearBottom();
    }

    st.setHeight(100, Unit.PCT);
  }

  public static void fillVertical(UIObject obj) {
    Assert.notNull(obj);
    fillVertical(obj.getElement());
  }

  public static void fixedTableLayout(Element el) {
    Assert.notNull(el);
    fixedTableLayout(el.getStyle());
  }

  public static void fixedTableLayout(Style st) {
    Assert.notNull(st);
    st.setProperty(STYLE_TABLE_LAYOUT, VALUE_FIXED);
  }

  public static void fixedTableLayout(UIObject obj) {
    Assert.notNull(obj);
    fixedTableLayout(obj.getElement());
  }

  public static void fullHeight(Element el) {
    Assert.notNull(el);
    fullHeight(el.getStyle());
  }

  public static void fullHeight(Style st) {
    Assert.notNull(st);
    st.setHeight(100, Unit.PCT);
  }

  public static void fullHeight(UIObject obj) {
    Assert.notNull(obj);
    fullHeight(obj.getElement());
  }

  public static void fullWidth(Element el) {
    Assert.notNull(el);
    fullWidth(el.getStyle());
  }

  public static void fullWidth(Style st) {
    Assert.notNull(st);
    st.setWidth(100, Unit.PCT);
  }

  public static void fullWidth(UIObject obj) {
    Assert.notNull(obj);
    fullWidth(obj.getElement());
  }

  public static String getCssText(Element el) {
    Assert.notNull(el);
    return getCssText(el.getStyle());
  }

  public static String getCssText(Style st) {
    Assert.notNull(st);
    return st.getProperty(PROPERTY_CSS_TEXT);
  }

  public static String getCssText(String id) {
    return getCssText(DomUtils.getElement(id));
  }

  public static String getCssText(UIObject obj) {
    Assert.notNull(obj);
    return getCssText(obj.getElement());
  }
  
  public static int getHeight(Element el) {
    Assert.notNull(el);
    return getHeight(el.getStyle());
  }

  public static int getHeight(Style st) {
    Assert.notNull(st);
    return BeeUtils.val(st.getHeight());
  }

  public static int getHeight(String id) {
    return getHeight(DomUtils.getElement(id));
  }

  public static int getHeight(UIObject obj) {
    Assert.notNull(obj);
    return getHeight(obj.getElement());
  }

  public static int getLeft(Element el) {
    Assert.notNull(el);
    return getLeft(el.getStyle());
  }

  public static int getLeft(Style st) {
    Assert.notNull(st);
    return BeeUtils.val(st.getLeft());
  }
  
  public static int getLeft(String id) {
    return getLeft(DomUtils.getElement(id));
  }

  public static int getLeft(UIObject obj) {
    Assert.notNull(obj);
    return getLeft(obj.getElement());
  }

  public static int getParentZIndex(Element el, boolean computed, boolean incl) {
    return BeeUtils.toInt(getParentProperty(el, STYLE_Z_INDEX, computed, null, incl));
  }

  public static int getParentZIndex(UIObject obj, boolean computed, boolean incl) {
    Assert.notNull(obj);
    return getParentZIndex(obj.getElement(), computed, incl);
  }
  
  public static Rectangle getRectangle(Element el) {
    Assert.notNull(el);
    return getRectangle(el.getStyle());
  }

  public static Rectangle getRectangle(Style st) {
    Assert.notNull(st);
    return new Rectangle(st);
  }

  public static Rectangle getRectangle(String id) {
    return getRectangle(DomUtils.getElement(id));
  }

  public static Rectangle getRectangle(UIObject obj) {
    Assert.notNull(obj);
    return getRectangle(obj.getElement());
  }

  public static ScrollBars getScroll(Element el) {
    Assert.notNull(el);
    return getScroll(el.getStyle());
  }

  public static ScrollBars getScroll(Style st) {
    Assert.notNull(st);

    if (isScroll(st.getOverflow())) {
      return ScrollBars.BOTH;
    }
    if (isScroll(st.getOverflowX())) {
      return ScrollBars.HORIZONTAL;
    }
    if (isScroll(st.getOverflowY())) {
      return ScrollBars.VERTICAL;
    }
    return ScrollBars.NONE;
  }

  public static ScrollBars getScroll(UIObject obj) {
    Assert.notNull(obj);
    return getScroll(obj.getElement());
  }

  public static List<Property> getStyleInfo(Style st) {
    return JsUtils.getInfo(st);
  }
  
  public static String getStylePrimaryName(Element el) {
    Assert.notNull(el);
    String className = el.getClassName();
    if (BeeUtils.isEmpty(className)) {
      return BeeConst.STRING_EMPTY;
    }

    int idx = className.indexOf(CLASS_NAME_SEPARATOR);
    if (idx >= 0) {
      return className.substring(0, idx);
    }
    return className;
  }

  public static int getTop(Element el) {
    Assert.notNull(el);
    return getTop(el.getStyle());
  }

  public static int getTop(Style st) {
    Assert.notNull(st);
    return BeeUtils.val(st.getTop());
  }

  public static int getTop(String id) {
    return getTop(DomUtils.getElement(id));
  }

  public static int getTop(UIObject obj) {
    Assert.notNull(obj);
    return getTop(obj.getElement());
  }

  public static int getWidth(Element el) {
    Assert.notNull(el);
    return getWidth(el.getStyle());
  }

  public static int getWidth(Style st) {
    Assert.notNull(st);
    return BeeUtils.val(st.getWidth());
  }

  public static int getWidth(String id) {
    return getWidth(DomUtils.getElement(id));
  }

  public static int getWidth(UIObject obj) {
    Assert.notNull(obj);
    return getWidth(obj.getElement());
  }
  
  public static int getZIndex(Element el) {
    Assert.notNull(el);
    return getZIndex(el.getStyle());
  }
  
  public static int getZIndex(Style st) {
    Assert.notNull(st);
    return BeeUtils.toInt(st.getZIndex());
  }

  public static int getZIndex(String id) {
    return getZIndex(DomUtils.getElement(id));
  }

  public static int getZIndex(UIObject obj) {
    Assert.notNull(obj);
    return getZIndex(obj.getElement());
  }

  public static void hideDisplay(Element el) {
    Assert.notNull(el);
    el.getStyle().setDisplay(Display.NONE);
  }

  public static void hideDisplay(String id) {
    hideDisplay(DomUtils.getElement(id));
  }

  public static void hideDisplay(UIObject obj) {
    Assert.notNull(obj);
    hideDisplay(obj.getElement());
  }

  public static void hideScroll(Element el, ScrollBars scroll) {
    Assert.notNull(el);
    hideScroll(el.getStyle(), scroll);
  }

  public static void hideScroll(Style st, ScrollBars scroll) {
    setOverflow(st, scroll, VALUE_HIDDEN);
  }

  public static void hideScroll(UIObject obj, ScrollBars scroll) {
    Assert.notNull(obj);
    hideScroll(obj.getElement(), scroll);
  }

  public static void insertClassName(Element el, String className, String beforeName) {
    Assert.notNull(el);
    Assert.notEmpty(className);
    Assert.notEmpty(beforeName);
    Assert.isFalse(BeeUtils.same(className, beforeName));

    String classes = el.getClassName();
    int beforeIndex = indexOfClassName(beforeName, classes);
    if (beforeIndex < 0) {
      el.addClassName(className);
      return;
    }

    int classIndex = indexOfClassName(className, classes);
    if (classIndex >= 0 && classIndex == beforeIndex - 1) {
      return;
    }

    StringBuilder sb = new StringBuilder();
    for (String name : CLASS_NAME_SPLITTER.split(classes.trim())) {
      if (classIndex >= 0 && BeeUtils.same(name, className)) {
        continue;
      }
      if (BeeUtils.same(name, beforeName)) {
        if (sb.length() > 0) {
          sb.append(CLASS_NAME_SEPARATOR);
        }
        sb.append(className);
      }

      if (sb.length() > 0) {
        sb.append(CLASS_NAME_SEPARATOR);
      }
      sb.append(name);
    }
    el.setClassName(sb.toString());
  }

  public static int insertClassName(NodeList<Element> nodes, String className, String beforeName) {
    Assert.notNull(nodes);

    int cnt = nodes.getLength();
    for (int i = 0; i < cnt; i++) {
      insertClassName(nodes.getItem(i), className, beforeName);
    }
    return cnt;
  }

  public static boolean isUnitFragment(char ch) {
    boolean ok = false;

    for (Unit constant : Unit.class.getEnumConstants()) {
      if (constant.getType().indexOf(ch) >= 0) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  public static void makeAbsolute(Element el) {
    Assert.notNull(el);
    el.getStyle().setPosition(Position.ABSOLUTE);
  }

  public static void makeAbsolute(String id) {
    makeAbsolute(DomUtils.getElement(id));
  }

  public static void makeAbsolute(UIObject obj) {
    Assert.notNull(obj);
    makeAbsolute(obj.getElement());
  }

  public static void makeFlexible(Element el) {
    Assert.notNull(el);
    el.addClassName(NAME_FLEXIBLE);
  }

  public static void makeFlexible(UIObject obj) {
    Assert.notNull(obj);
    obj.addStyleName(NAME_FLEXIBLE);
  }
  
  public static void makeRelative(Element el) {
    Assert.notNull(el);
    el.getStyle().setPosition(Position.RELATIVE);
  }

  public static void makeRelative(String id) {
    makeRelative(DomUtils.getElement(id));
  }

  public static void makeRelative(UIObject obj) {
    Assert.notNull(obj);
    makeRelative(obj.getElement());
  }
  
  public static Pair<Double, Unit> parseCssLength(String input) {
    Assert.notEmpty(input);
    Double value = null;
    Unit unit = null;

    String s = input.trim();
    int p = s.length() - 1;
    while (p >= 0 && isUnitFragment(s.charAt(p))) {
      p--;
    }

    if (p >= 0 && p < s.length() - 1) {
      String v = s.substring(0, p + 1).trim();
      String u = s.substring(p + 1).trim();
      if (BeeUtils.isDouble(v)) {
        value = BeeUtils.toDouble(v);
        unit = parseUnit(u);
      }
    }
    return new Pair<Double, Unit>(value, unit);
  }

  public static <E extends Enum<?> & HasCssName> E parseCssName(Class<E> clazz, String input) {
    Assert.notNull(clazz);
    Assert.notEmpty(input);

    for (E constant : clazz.getEnumConstants()) {
      if (BeeUtils.same(constant.getCssName(), input)) {
        return constant;
      }
    }
    return null;
  }

  public static FontSize parseFontSize(String input) {
    return parseCssName(FontSize.class, input);
  }

  public static FontStyle parseFontStyle(String input) {
    return parseCssName(FontStyle.class, input);
  }

  public static FontVariant parseFontVariant(String input) {
    return parseCssName(FontVariant.class, input);
  }

  public static FontWeight parseFontWeight(String input) {
    return parseCssName(FontWeight.class, input);
  }

  public static ScrollBars parseScrollBars(String input) {
    Assert.notEmpty(input);

    for (ScrollBars sb : ScrollBars.values()) {
      if (BeeUtils.same(sb.name(), input)) {
        return sb;
      }
    }
    return null;
  }

  public static ScrollBars parseScrollBars(String input, ScrollBars def) {
    if (BeeUtils.isEmpty(input)) {
      return def;
    }
    ScrollBars sb = parseScrollBars(input);
    if (sb == null) {
      return def;
    }
    return sb;
  }
  
  public static Unit parseUnit(String input) {
    Assert.notEmpty(input);

    for (Unit unit : Unit.values()) {
      if (BeeUtils.same(unit.getType(), input)) {
        return unit;
      }
    }
    return null;
  }

  public static Unit parseUnit(String input, Unit defUnit) {
    if (BeeUtils.isEmpty(input)) {
      return defUnit;
    }
    Unit unit = parseUnit(input);
    if (unit == null) {
      return defUnit;
    }
    return unit;
  }
  
  public static int removeClassName(NodeList<Element> nodes, String className) {
    Assert.notNull(nodes);
    Assert.notEmpty(className);

    int cnt = nodes.getLength();
    for (int i = 0; i < cnt; i++) {
      nodes.getItem(i).removeClassName(className);
    }
    return cnt;
  }

  public static void removeStyleDependentName(Element el, String style) {
    setStyleDependentName(el, style, false);
  }
  
  public static void removeStyleName(String id, String style) {
    setStyleName(DomUtils.getElement(id), style, false);
  }
  
  public static void setAppearance(Element el, String classes, String styles) {
    Assert.notNull(el);

    if (!BeeUtils.isEmpty(classes)) {
      updateClasses(el, classes);
    }
    if (!BeeUtils.isEmpty(styles)) {
      apply(el.getStyle(), styles);
    }
  }

  public static void setAppearance(String id, String className, String styles) {
    setAppearance(DomUtils.getElement(id), className, styles);
  }
  
  public static void setAppearance(UIObject obj, String className, String styles) {
    Assert.notNull(obj);
    setAppearance(obj.getElement(), className, styles);
  }
  
  public static void setBorderBottomWidth(Element el, int px) {
    Assert.notNull(el);
    setBorderBottomWidth(el.getStyle(), px);
  }

  public static void setBorderBottomWidth(Style st, int px) {
    Assert.notNull(st);
    Assert.nonNegative(px);
    st.setPropertyPx(STYLE_BORDER_BOTTOM, px);
  }

  public static void setBorderBottomWidth(UIObject obj, int px) {
    setBorderBottomWidth(obj.getElement(), px);
  }

  public static void setBorderLeftWidth(Element el, int px) {
    Assert.notNull(el);
    setBorderLeftWidth(el.getStyle(), px);
  }

  public static void setBorderLeftWidth(Style st, int px) {
    Assert.notNull(st);
    Assert.nonNegative(px);
    st.setPropertyPx(STYLE_BORDER_LEFT, px);
  }

  public static void setBorderLeftWidth(UIObject obj, int px) {
    Assert.notNull(obj);
    setBorderLeftWidth(obj.getElement(), px);
  }

  public static void setBorderRightWidth(Element el, int px) {
    Assert.notNull(el);
    setBorderRightWidth(el.getStyle(), px);
  }

  public static void setBorderRightWidth(Style st, int px) {
    Assert.notNull(st);
    Assert.nonNegative(px);
    st.setPropertyPx(STYLE_BORDER_RIGHT, px);
  }
  
  public static void setBorderRightWidth(UIObject obj, int px) {
    Assert.notNull(obj);
    setBorderRightWidth(obj.getElement(), px);
  }

  public static void setBorderTopWidth(Element el, int px) {
    Assert.notNull(el);
    setBorderTopWidth(el.getStyle(), px);
  }

  public static void setBorderTopWidth(Style st, int px) {
    Assert.notNull(st);
    Assert.nonNegative(px);
    st.setPropertyPx(STYLE_BORDER_TOP, px);
  }

  public static void setBorderTopWidth(UIObject obj, int px) {
    Assert.notNull(obj);
    setBorderTopWidth(obj.getElement(), px);
  }

  public static void setBottom(Element el, int px) {
    Assert.notNull(el);
    setBottom(el.getStyle(), px);
  }

  public static void setBottom(Style st, int px) {
    Assert.notNull(st);
    st.setBottom(px, Unit.PX);
  }

  public static void setBottom(String id, int px) {
    setBottom(DomUtils.getElement(id), px);
  }

  public static void setBottom(UIObject obj, int px) {
    Assert.notNull(obj);
    setBottom(obj.getElement(), px);
  }
  
  public static void setCssText(Element el, String text) {
    Assert.notNull(el);
    setCssText(el.getStyle(), text);
  }

  public static void setCssText(Style st, String text) {
    Assert.notNull(st);
    Assert.notNull(text);
    st.setProperty(PROPERTY_CSS_TEXT, text);
  }

  public static void setCssText(String id, String text) {
    setCssText(DomUtils.getElement(id), text);
  }

  public static void setCssText(UIObject obj, String text) {
    Assert.notNull(obj);
    setCssText(obj.getElement(), text);
  }
  
  public static void setDisplay(Element el, Display value) {
    Assert.notNull(el);
    el.getStyle().setDisplay(value);
  }
  
  public static void setDisplay(String id, Display value) {
    setDisplay(DomUtils.getElement(id), value);
  }

  public static void setDisplay(UIObject obj, Display value) {
    Assert.notNull(obj);
    setDisplay(obj.getElement(), value);
  }
  
  public static void setFontFamily(Element el, String family) {
    Assert.notNull(el);
    setFontFamily(el.getStyle(), family);
  }

  public static void setFontFamily(Style st, String family) {
    Assert.notNull(st);
    Assert.notEmpty(family);
    st.setProperty(STYLE_FONT_FAMILY, family);
  }

  public static void setFontFamily(UIObject obj, String family) {
    Assert.notNull(obj);
    setFontFamily(obj.getElement(), family);
  }

  public static void setFontSize(Element el, double size, Unit unit) {
    Assert.notNull(el);
    Assert.isPositive(size);
    Assert.notNull(unit);
    el.getStyle().setFontSize(size, unit);
  }

  public static void setFontSize(Element el, FontSize size) {
    Assert.notNull(el);
    setFontSize(el.getStyle(), size);
  }

  public static void setFontSize(Style st, FontSize size) {
    Assert.notNull(st);
    Assert.notNull(size);
    setFontSize(st, size.getCssName());
  }

  public static void setFontSize(UIObject obj, double size, Unit unit) {
    Assert.notNull(obj);
    setFontSize(obj.getElement(), size, unit);
  }

  public static void setFontSize(UIObject obj, FontSize size) {
    Assert.notNull(obj);
    setFontSize(obj.getElement(), size);
  }

  public static void setFontSizePx(Element el, double size) {
    Assert.notNull(el);
    setFontSizePx(el.getStyle(), size);
  }

  public static void setFontSizePx(Style st, double size) {
    Assert.notNull(st);
    Assert.isPositive(size);
    st.setFontSize(size, Unit.PX);
  }

  public static void setFontSizePx(UIObject obj, double size) {
    Assert.notNull(obj);
    setFontSizePx(obj.getElement(), size);
  }

  public static void setFontVariant(Element el, FontVariant variant) {
    Assert.notNull(el);
    setFontVariant(el.getStyle(), variant);
  }

  public static void setFontVariant(Style st, FontVariant variant) {
    Assert.notNull(st);
    Assert.notNull(variant);
    st.setProperty(STYLE_FONT_VARIANT, variant.getCssName());
  }

  public static void setFontVariant(UIObject obj, FontVariant variant) {
    Assert.notNull(obj);
    setFontVariant(obj.getElement(), variant);
  }

  public static void setHeight(Element el, int px) {
    Assert.notNull(el);
    setHeight(el.getStyle(), px);
  }

  public static void setHeight(Style st, int px) {
    Assert.notNull(st);
    st.setHeight(px, Unit.PX);
  }

  public static void setHeight(String id, int px) {
    setHeight(DomUtils.getElement(id), px);
  }

  public static void setHeight(UIObject obj, int px) {
    Assert.notNull(obj);
    setHeight(obj.getElement(), px);
  }

  public static void setHorizontalPadding(Element el, int px) {
    Assert.notNull(el);
    setHorizontalPadding(el.getStyle(), px);
  }
  
  public static void setHorizontalPadding(Element el, double value, Unit unit) {
    Assert.notNull(el);
    setHorizontalPadding(el.getStyle(), value, unit);
  }

  public static void setHorizontalPadding(Style st, int px) {
    setHorizontalPadding(st, px, Unit.PX);
  }

  public static void setHorizontalPadding(Style st, double value, Unit unit) {
    Assert.notNull(st);
    Assert.notNull(unit);
    st.setPaddingLeft(value, unit);
    st.setPaddingRight(value, unit);
  }

  public static void setHorizontalPadding(String id, int px) {
    setHorizontalPadding(DomUtils.getElement(id), px);
  }

  public static void setHorizontalPadding(String id, double value, Unit unit) {
    setHorizontalPadding(DomUtils.getElement(id), value, unit);
  }

  public static void setHorizontalPadding(UIObject obj, int px) {
    Assert.notNull(obj);
    setHorizontalPadding(obj.getElement(), px);
  }

  public static void setHorizontalPadding(UIObject obj, double value, Unit unit) {
    Assert.notNull(obj);
    setHorizontalPadding(obj.getElement(), value, unit);
  }
  
  public static void setLeft(Element el, int px) {
    Assert.notNull(el);
    setLeft(el.getStyle(), px);
  }

  public static void setLeft(Style st, int px) {
    Assert.notNull(st);
    st.setLeft(px, Unit.PX);
  }

  public static void setLeft(String id, int px) {
    setLeft(DomUtils.getElement(id), px);
  }

  public static void setLeft(UIObject obj, int px) {
    Assert.notNull(obj);
    setLeft(obj.getElement(), px);
  }

  public static void setMaxHeight(Element el, double value, Unit unit) {
    Assert.notNull(el);
    setMaxHeight(el.getStyle(), value, unit);
  }

  public static void setMaxHeight(Element el, int px) {
    Assert.notNull(el);
    setMaxHeight(el.getStyle(), px);
  }

  public static void setMaxHeight(Style st, double value, Unit unit) {
    Assert.notNull(st);
    Assert.isPositive(value);
    Assert.notNull(unit);
    st.setProperty(STYLE_MAX_HEIGHT, value, unit);
  }

  public static void setMaxHeight(Style st, int px) {
    Assert.notNull(st);
    Assert.isPositive(px);
    st.setPropertyPx(STYLE_MAX_HEIGHT, px);
  }

  public static void setMaxHeight(UIObject obj, double value, Unit unit) {
    Assert.notNull(obj);
    setMaxHeight(obj.getElement(), value, unit);
  }

  public static void setMaxHeight(UIObject obj, int px) {
    Assert.notNull(obj);
    setMaxHeight(obj.getElement(), px);
  }

  public static void setMaxWidth(Element el, double value, Unit unit) {
    Assert.notNull(el);
    setMaxWidth(el.getStyle(), value, unit);
  }

  public static void setMaxWidth(Element el, int px) {
    Assert.notNull(el);
    setMaxWidth(el.getStyle(), px);
  }

  public static void setMaxWidth(Style st, double value, Unit unit) {
    Assert.notNull(st);
    Assert.isPositive(value);
    Assert.notNull(unit);
    st.setProperty(STYLE_MAX_WIDTH, value, unit);
  }

  public static void setMaxWidth(Style st, int px) {
    Assert.notNull(st);
    Assert.isPositive(px);
    st.setPropertyPx(STYLE_MAX_WIDTH, px);
  }

  public static void setMaxWidth(UIObject obj, double value, Unit unit) {
    Assert.notNull(obj);
    setMaxWidth(obj.getElement(), value, unit);
  }

  public static void setMaxWidth(UIObject obj, int px) {
    Assert.notNull(obj);
    setMaxWidth(obj.getElement(), px);
  }

  public static void setMinHeight(Element el, double value, Unit unit) {
    Assert.notNull(el);
    setMinHeight(el.getStyle(), value, unit);
  }

  public static void setMinHeight(Element el, int px) {
    Assert.notNull(el);
    setMinHeight(el.getStyle(), px);
  }

  public static void setMinHeight(Style st, double value, Unit unit) {
    Assert.notNull(st);
    Assert.isPositive(value);
    Assert.notNull(unit);
    st.setProperty(STYLE_MIN_HEIGHT, value, unit);
  }

  public static void setMinHeight(Style st, int px) {
    Assert.notNull(st);
    Assert.isPositive(px);
    st.setPropertyPx(STYLE_MIN_HEIGHT, px);
  }

  public static void setMinHeight(UIObject obj, double value, Unit unit) {
    Assert.notNull(obj);
    setMinHeight(obj.getElement(), value, unit);
  }

  public static void setMinHeight(UIObject obj, int px) {
    Assert.notNull(obj);
    setMinHeight(obj.getElement(), px);
  }

  public static void setMinWidth(Element el, double value, Unit unit) {
    Assert.notNull(el);
    setMinWidth(el.getStyle(), value, unit);
  }

  public static void setMinWidth(Element el, int px) {
    Assert.notNull(el);
    setMinWidth(el.getStyle(), px);
  }

  public static void setMinWidth(Style st, double value, Unit unit) {
    Assert.notNull(st);
    Assert.isPositive(value);
    Assert.notNull(unit);
    st.setProperty(STYLE_MIN_WIDTH, value, unit);
  }

  public static void setMinWidth(Style st, int px) {
    Assert.notNull(st);
    Assert.isPositive(px);
    st.setPropertyPx(STYLE_MIN_WIDTH, px);
  }

  public static void setMinWidth(UIObject obj, double value, Unit unit) {
    Assert.notNull(obj);
    setMinWidth(obj.getElement(), value, unit);
  }

  public static void setMinWidth(UIObject obj, int px) {
    Assert.notNull(obj);
    setMinWidth(obj.getElement(), px);
  }

  public static void setOpacity(Element el, double value) {
    Assert.notNull(el);
    el.getStyle().setOpacity(value);
  }

  public static void setOpacity(UIObject obj, double value) {
    Assert.notNull(obj);
    setOpacity(obj.getElement(), value);
  }

  public static void setOpacity(String id, double value) {
    setOpacity(DomUtils.getElement(id), value);
  }
  
  public static void setOverflow(Element el, ScrollBars scroll, String value) {
    Assert.notNull(el);
    setOverflow(el.getStyle(), scroll, value);
  }

  public static void setOverflow(Style st, ScrollBars scroll, String value) {
    Assert.notNull(st);
    Assert.notNull(scroll);
    Assert.notEmpty(value);

    switch (scroll) {
      case BOTH:
        st.setProperty(STYLE_OVERFLOW, value);
        break;
      case HORIZONTAL:
        st.setProperty(STYLE_OVERFLOW_X, value);
        break;
      case VERTICAL:
        st.setProperty(STYLE_OVERFLOW_Y, value);
        break;
      case NONE:
        clearStyleProperty(st, STYLE_OVERFLOW);
        clearStyleProperty(st, STYLE_OVERFLOW_X);
        clearStyleProperty(st, STYLE_OVERFLOW_Y);
        break;
      default:
        Assert.untouchable();
    }
  }

  public static void setOverflow(UIObject obj, ScrollBars scroll, String value) {
    Assert.notNull(obj);
    setOverflow(obj.getElement(), scroll, value);
  }

  public static void setRectangle(Element el, int left, int top, int width, int height) {
    Assert.notNull(el);
    setRectangle(el.getStyle(), left, top, width, height);
  }

  public static void setRectangle(Style st, int left, int top, int width, int height) {
    Assert.notNull(st);
    st.setLeft(left, Unit.PX);
    st.setTop(top, Unit.PX);
    st.setWidth(width, Unit.PX);
    st.setHeight(height, Unit.PX);
  }

  public static void setRectangle(String id, int left, int top, int width, int height) {
    setRectangle(DomUtils.getElement(id), left, top, width, height);
  }

  public static void setRectangle(UIObject obj, int left, int top, int width, int height) {
    Assert.notNull(obj);
    setRectangle(obj.getElement(), left, top, width, height);
  }
  
  public static void setRight(Element el, int px) {
    Assert.notNull(el);
    setRight(el.getStyle(), px);
  }

  public static void setRight(Style st, int px) {
    Assert.notNull(st);
    st.setRight(px, Unit.PX);
  }

  public static void setRight(String id, int px) {
    setRight(DomUtils.getElement(id), px);
  }

  public static void setRight(UIObject obj, int px) {
    Assert.notNull(obj);
    setRight(obj.getElement(), px);
  }
  
  public static void setSize(Element el, int width, int height) {
    Assert.notNull(el);
    setSize(el.getStyle(), width, height);
  }

  public static void setSize(Style st, int width, int height) {
    Assert.notNull(st);
    st.setWidth(width, Unit.PX);
    st.setHeight(height, Unit.PX);
  }

  public static void setSize(String id, int width, int height) {
    setSize(DomUtils.getElement(id), width, height);
  }

  public static void setSize(UIObject obj, int width, int height) {
    Assert.notNull(obj);
    setSize(obj.getElement(), width, height);
  }
  
  public static void setStyleDependentName(Element el, String style, boolean add) {
    Assert.notNull(el);
    Assert.notEmpty(style);

    String primary = getStylePrimaryName(el);
    Assert.notEmpty(primary, "element has no primary style");

    setStyleName(el, primary + BeeConst.CHAR_MINUS + style.trim(), add);
  }

  public static void setStyleName(Element el, String st, boolean add) {
    Assert.notNull(el);
    Assert.notEmpty(st);

    if (add) {
      el.addClassName(st.trim());
    } else {
      el.removeClassName(st.trim());
    }
  }

  public static int setStyleProperty(NodeList<Element> nodes, String name,
      double value, Unit unit) {
    Assert.notNull(nodes);
    Assert.notEmpty(name);
    Assert.notNull(unit);

    return setStyleProperty(nodes, name, value + unit.getType());
  }

  public static int setStyleProperty(NodeList<Element> nodes, String name, String value) {
    Assert.notNull(nodes);
    Assert.notEmpty(name);

    int cnt = nodes.getLength();
    for (int i = 0; i < cnt; i++) {
      nodes.getItem(i).getStyle().setProperty(name, value);
    }
    return cnt;
  }

  public static int setStylePropertyPx(NodeList<Element> nodes, String name, int value) {
    Assert.notNull(nodes);
    Assert.notEmpty(name);

    return setStyleProperty(nodes, name, value, Unit.PX);
  }

  public static void setTextAlign(Element el, HorizontalAlignmentConstant align) {
    Assert.notNull(el);
    setTextAlign(el.getStyle(), align);
  }

  public static void setTextAlign(Style st, HorizontalAlignmentConstant align) {
    Assert.notNull(st);
    Assert.notNull(align);
    st.setProperty(STYLE_TEXT_ALIGN, align.getTextAlignString());
  }

  public static void setTextAlign(String id, HorizontalAlignmentConstant align) {
    setTextAlign(DomUtils.getElement(id), align);
  }

  public static void setTextAlign(UIObject obj, HorizontalAlignmentConstant align) {
    Assert.notNull(obj);
    setTextAlign(obj.getElement(), align);
  }
  
  public static void setTop(Element el, int px) {
    Assert.notNull(el);
    setTop(el.getStyle(), px);
  }

  public static void setTop(Style st, int px) {
    Assert.notNull(st);
    st.setTop(px, Unit.PX);
  }

  public static void setTop(String id, int px) {
    setTop(DomUtils.getElement(id), px);
  }

  public static void setTop(UIObject obj, int px) {
    Assert.notNull(obj);
    setTop(obj.getElement(), px);
  }

  public static void setWhiteSpace(Element el, WhiteSpace value) {
    Assert.notNull(el);
    setWhiteSpace(el.getStyle(), value);
  }

  public static void setWhiteSpace(Style st, WhiteSpace value) {
    Assert.notNull(st);
    Assert.notNull(value);
    st.setProperty(STYLE_WHITE_SPACE, value.getCssName());
  }

  public static void setWhiteSpace(UIObject obj, WhiteSpace value) {
    Assert.notNull(obj);
    setWhiteSpace(obj.getElement(), value);
  }

  public static void setWidth(Element el, int px) {
    Assert.notNull(el);
    setWidth(el.getStyle(), px);
  }

  public static void setWidth(Style st, int px) {
    Assert.notNull(st);
    st.setWidth(px, Unit.PX);
  }

  public static void setWidth(String id, int px) {
    setWidth(DomUtils.getElement(id), px);
  }

  public static void setWidth(UIObject obj, int px) {
    Assert.notNull(obj);
    setWidth(obj.getElement(), px);
  }

  public static void setWordWrap(Element el, boolean wrap) {
    Assert.notNull(el);
    setWordWrap(el.getStyle(), wrap);
  }

  public static void setWordWrap(Style st, boolean wrap) {
    setWhiteSpace(st, wrap ? WhiteSpace.NORMAL : WhiteSpace.NOWRAP);
  }

  public static void setWordWrap(UIObject obj, boolean wrap) {
    Assert.notNull(obj);
    setWordWrap(obj.getElement(), wrap);
  }

  public static void setZIndex(Element el, int value) {
    Assert.notNull(el);
    setZIndex(el.getStyle(), value);
  }

  public static void setZIndex(Style st, int value) {
    Assert.notNull(st);
    st.setZIndex(value);
  }

  public static void setZIndex(String id, int value) {
    setZIndex(DomUtils.getElement(id), value);
  }

  public static void setZIndex(UIObject obj, int value) {
    Assert.notNull(obj);
    setZIndex(obj.getElement(), value);
  }

  public static String toCssLength(double value, Unit unit) {
    Assert.notNull(unit);
    return BeeUtils.toString(value) + unit.getType();
  }
  
  public static SafeStyles toSafeStyles(String s) {
    Assert.notEmpty(s);
    if (s.trim().endsWith(DEFINITION_SEPARATOR)) {
      return SafeStylesUtils.fromTrustedString(s.trim()); 
    } else {
      return SafeStylesUtils.fromTrustedString(s.trim() + DEFINITION_SEPARATOR); 
    }
  }

  public static void unhideDisplay(Element el) {
    Assert.notNull(el);
    if (BeeUtils.same(el.getStyle().getDisplay(), Display.NONE.getCssName())) {
      el.getStyle().clearDisplay();
    }
  }

  public static void unhideDisplay(String id) {
    unhideDisplay(DomUtils.getElement(id));
  }
  
  public static void unhideDisplay(UIObject obj) {
    Assert.notNull(obj);
    unhideDisplay(obj.getElement());
  }
  
  public static void updateClasses(Element el, String classes) {
    Assert.notNull(el);
    Assert.notNull(classes);
    
    if (BeeUtils.same(classes, String.valueOf(REMOVE_CLASS))) {
      if (!BeeUtils.isEmpty(el.getClassName())) {
        el.setClassName(BeeConst.STRING_EMPTY);
      }
      return;
    }
    
    if (BeeUtils.isPrefixOrSuffix(classes, REPLACE_CLASS)) {
      el.setClassName(BeeUtils.removePrefixAndSuffix(classes, REPLACE_CLASS));
      return;
    }

    for (String name : CLASS_NAME_SPLITTER.split(classes.trim())) {
      if (BeeUtils.isPrefixOrSuffix(name, ADD_CLASS)) {
        String z = BeeUtils.removePrefixAndSuffix(name, ADD_CLASS);
        if (!BeeUtils.isEmpty(z)) {
          el.addClassName(z);
        }

      } else if (BeeUtils.isPrefixOrSuffix(name, REMOVE_CLASS)) {
          String z = BeeUtils.removePrefixAndSuffix(name, REMOVE_CLASS);
          if (!BeeUtils.isEmpty(z)) {
            el.removeClassName(z);
          }
  
      } else if (BeeUtils.isPrefixOrSuffix(name, REPLACE_CLASS)) {
        el.setClassName(BeeUtils.removePrefixAndSuffix(name, REPLACE_CLASS));

      } else {
        el.addClassName(name);
      }
    }
  }

  public static void updateClasses(UIObject obj, String classes) {
    Assert.notNull(obj);
    updateClasses(obj.getElement(), classes);
  }
  
  public static void zeroLeft(Element el) {
    Assert.notNull(el);
    zeroLeft(el.getStyle());
  }

  public static void zeroLeft(Style st) {
    Assert.notNull(st);
    st.setPropertyPx(STYLE_LEFT, 0);
  }

  public static void zeroLeft(UIObject obj) {
    Assert.notNull(obj);
    zeroLeft(obj.getElement());
  }

  public static void zeroTop(Element el) {
    Assert.notNull(el);
    zeroTop(el.getStyle());
  }

  public static void zeroTop(Style st) {
    Assert.notNull(st);
    st.setPropertyPx(STYLE_TOP, 0);
  }

  public static void zeroTop(UIObject obj) {
    Assert.notNull(obj);
    zeroTop(obj.getElement());
  }

  public static void zeroWidth(Element el) {
    Assert.notNull(el);
    zeroWidth(el.getStyle());
  }

  public static void zeroWidth(Style st) {
    Assert.notNull(st);
    st.setPropertyPx(STYLE_WIDTH, 0);
  }

  public static void zeroWidth(UIObject obj) {
    Assert.notNull(obj);
    zeroWidth(obj.getElement());
  }
  
  private static String camelize(String name) {
    return BeeUtils.camelize(name, NAME_DELIMITER);
  }

  private static void clearStyleProperty(Style style, String name) {
    if (!BeeUtils.isEmpty(style.getProperty(name))) {
      style.clearProperty(name);
    }
  }
  
  private static void copyStyleProperty(Style src, Style dst, String name) {
    String value = src.getProperty(name);
    if (!BeeUtils.equalsTrim(value, dst.getProperty(name))) {
      dst.setProperty(name, value);
    }
  }
  
  private static String getParentProperty(Element el, String name, boolean computed,
      Collection<String> ignore, boolean incl) {
    Assert.notNull(el);
    Assert.notEmpty(name);
    
    if (incl) {
      String value = el.getStyle().getProperty(name);
      boolean ok = !BeeUtils.isEmpty(value);
      
      if (!ok && computed) {
        value = ComputedStyles.get(el, name);
        ok = !BeeUtils.isEmpty(value);
      }
      
      if (ok) {
        if (BeeUtils.isEmpty(ignore)) {
          ok = !BeeUtils.inListSame(value, VALUE_AUTO, VALUE_INHERIT);
        } else {
          ok = !ignore.contains(value);
        }
      }

      if (ok) {
        return value;
      }
    }

    Element parent = el.getParentElement();
    if (parent == null) {
      return null;
    }
    return getParentProperty(parent, name, computed, ignore, true);
  }

  private static boolean hasProperty(Style st, String name) {
    if (st == null || BeeUtils.isEmpty(name)) {
      return false;
    } else {
      return !BeeUtils.isEmpty(st.getProperty(name));
    }
  }

  private static int indexOfClassName(String className, String classes) {
    if (className == null || !BeeUtils.context(className.trim(), classes)) {
      return BeeConst.UNDEF;
    }

    int idx = 0;
    for (String name : CLASS_NAME_SPLITTER.split(classes.trim())) {
      if (BeeUtils.same(name, className)) {
        return idx;
      }
      idx++;
    }
    return BeeConst.UNDEF;
  }

  private static boolean isScroll(String value) {
    return BeeUtils.inListSame(value, VALUE_AUTO, VALUE_SCROLL);
  }

  private static boolean isZero(String s) {
    if (BeeUtils.isEmpty(s)) {
      return false;
    }
    if (s.trim().charAt(0) != BeeConst.CHAR_ZERO) {
      return false;
    }
    boolean ok = true;

    for (int i = 1; i < s.trim().length(); i++) {
      char z = s.trim().charAt(i);

      if (z == BeeConst.CHAR_ZERO || z == BeeConst.CHAR_SPACE || z == BeeConst.CHAR_POINT) {
        continue;
      } else {
        if (Character.isDigit(z)) {
          ok = false;
        }
        break;
      }
    }
    return ok;
  }
  
  private static List<Property> parseStyles(String styles) {
    Assert.notEmpty(styles);
    List<Property> result = Lists.newArrayList();
    
    for (String style : DEFINITION_SPLITTER.split(styles)) {
      String name = BeeUtils.getPrefix(style, NAME_VALUE_SEPARATOR);
      String value = BeeUtils.getSuffix(style, NAME_VALUE_SEPARATOR);
      if (!BeeUtils.isEmpty(name) && !BeeUtils.isEmpty(value)) {
        result.add(new Property(name, value));
      }
    }
    return result;
  }

  private static void setFontSize(Style st, String value) {
    Assert.notNull(st);
    Assert.notEmpty(value);
    st.setProperty(STYLE_FONT_SIZE, value);
  }

  private StyleUtils() {
  }
}
