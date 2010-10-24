package com.butent.bee.egg.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeStyle implements BeeModule {
  public static final String ACTIVE_BLANK = "bee-activeBlank";
  public static final String ACTIVE_CONTENT = "bee-activeContent";

  public static final String STYLE_LEFT = "left";
  public static final String STYLE_WIDTH = "width";
  public static final String STYLE_RIGHT = "right";

  public static final String STYLE_TOP = "top";
  public static final String STYLE_HEIGHT = "height";
  public static final String STYLE_BOTTOM = "bottom";

  public static final String STYLE_BORDER_TOP = "borderTop";
  public static final String STYLE_BORDER_BOTTOM = "borderBottom";

  public static final String STYLE_TABLE_LAYOUT = "tableLayout";

  public static final String VALUE_AUTO = "auto";
  public static final String VALUE_FIXED = "fixed";

  public void autoHeight(UIObject obj) {
    Assert.notNull(obj);
    autoHeight(obj.getElement());
  }

  public void autoHeight(Element el) {
    Assert.notNull(el);
    autoHeight(el.getStyle());
  }

  public void autoHeight(Style st) {
    Assert.notNull(st);
    st.setProperty(STYLE_HEIGHT, VALUE_AUTO);
  }
  
  public void autoWidth(UIObject obj) {
    Assert.notNull(obj);
    autoWidth(obj.getElement());
  }

  public void autoWidth(Element el) {
    Assert.notNull(el);
    autoWidth(el.getStyle());
  }

  public void autoWidth(Style st) {
    Assert.notNull(st);
    st.setProperty(STYLE_WIDTH, VALUE_AUTO);
  }

  public void clearTableLayout(UIObject obj) {
    Assert.notNull(obj);
    clearTableLayout(obj.getElement());
  }

  public void clearTableLayout(Element el) {
    Assert.notNull(el);
    clearTableLayout(el.getStyle());
  }

  public void clearTableLayout(Style st) {
    Assert.notNull(st);
    st.clearProperty(STYLE_TABLE_LAYOUT);
  }

  public void end() {
  }

  public void fillHorizontal(UIObject obj) {
    Assert.notNull(obj);
    fillHorizontal(obj.getElement());
  }

  public void fillHorizontal(Element el) {
    Assert.notNull(el);
    fillHorizontal(el.getStyle());
  }

  public void fillHorizontal(Style st) {
    Assert.notNull(st);

    if (!isZero(st.getLeft())) {
      st.setLeft(0, Unit.PX);
    }
    if (hasProperty(st, STYLE_RIGHT)) {
      st.clearRight();
    }

    st.setWidth(100, Unit.PCT);
  }

  public void fillVertical(UIObject obj) {
    Assert.notNull(obj);
    fillVertical(obj.getElement());
  }

  public void fillVertical(Element el) {
    Assert.notNull(el);
    fillVertical(el.getStyle());
  }

  public void fillVertical(Style st) {
    Assert.notNull(st);

    if (!isZero(st.getTop())) {
      st.setTop(0, Unit.PX);
    }
    if (hasProperty(st, STYLE_BOTTOM)) {
      st.clearBottom();
    }

    st.setHeight(100, Unit.PCT);
  }

  public void fixedTableLayout(UIObject obj) {
    Assert.notNull(obj);
    fixedTableLayout(obj.getElement());
  }

  public void fixedTableLayout(Element el) {
    Assert.notNull(el);
    fixedTableLayout(el.getStyle());
  }

  public void fixedTableLayout(Style st) {
    Assert.notNull(st);
    st.setProperty(STYLE_TABLE_LAYOUT, VALUE_FIXED);
  }

  public void fullHeight(UIObject obj) {
    Assert.notNull(obj);
    fullHeight(obj.getElement());
  }

  public void fullHeight(Element el) {
    Assert.notNull(el);
    fullHeight(el.getStyle());
  }

  public void fullHeight(Style st) {
    Assert.notNull(st);
    st.setHeight(100, Unit.PCT);
  }

  public void fullWidht(UIObject obj) {
    Assert.notNull(obj);
    fullWidht(obj.getElement());
  }

  public void fullWidht(Element el) {
    Assert.notNull(el);
    fullWidth(el.getStyle());
  }

  public void fullWidth(Style st) {
    Assert.notNull(st);
    st.setWidth(100, Unit.PCT);
  }
  
  public String getName() {
    return getClass().getName();
  }

  public int getPriority(int p) {
    switch (p) {
      case PRIORITY_INIT:
        return DO_NOT_CALL;
      case PRIORITY_START:
        return DO_NOT_CALL;
      case PRIORITY_END:
        return DO_NOT_CALL;
      default:
        return DO_NOT_CALL;
    }
  }

  public void init() {
  }

  public void setBorderBottomWidth(UIObject obj, int px) {
    Assert.notNull(obj);
    setBorderBottomWidth(obj.getElement(), px);
  }

  public void setBorderBottomWidth(Element el, int px) {
    Assert.notNull(el);
    setBorderBottomWidth(el.getStyle(), px);
  }

  public void setBorderBottomWidth(Style st, int px) {
    Assert.notNull(st);
    Assert.nonNegative(px);
    st.setPropertyPx(STYLE_BORDER_BOTTOM, px);
  }

  public void setBorderTopWidth(UIObject obj, int px) {
    Assert.notNull(obj);
    setBorderTopWidth(obj.getElement(), px);
  }

  public void setBorderTopWidth(Element el, int px) {
    Assert.notNull(el);
    setBorderTopWidth(el.getStyle(), px);
  }

  public void setBorderTopWidth(Style st, int px) {
    Assert.notNull(st);
    Assert.nonNegative(px);
    st.setPropertyPx(STYLE_BORDER_TOP, px);
  }

  public void start() {
  }

  public void zeroWidth(UIObject obj) {
    Assert.notNull(obj);
    zeroWidth(obj.getElement());
  }

  public void zeroWidth(Element el) {
    Assert.notNull(el);
    zeroWidth(el.getStyle());
  }

  public void zeroWidth(Style st) {
    Assert.notNull(st);
    st.setPropertyPx(STYLE_WIDTH, 0);
  }

  private boolean hasProperty(Style st, String name) {
    if (st == null || BeeUtils.isEmpty(name)) {
      return false;
    } else {
      return !BeeUtils.isEmpty(st.getProperty(name));
    }
  }

  private boolean isZero(String s) {
    if (BeeUtils.isEmpty(s)) {
      return false;
    }
    if (s.trim().charAt(0) != BeeConst.CHAR_ZERO) {
      return false;
    }
    boolean ok = true;

    for (int i = 1; i < s.trim().length(); i++) {
      char z = s.trim().charAt(i);

      if (z == BeeConst.CHAR_ZERO || z == BeeConst.CHAR_SPACE
          || z == BeeConst.CHAR_POINT) {
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
}
