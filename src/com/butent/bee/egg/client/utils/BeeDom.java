package com.butent.bee.egg.client.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.Transformable;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.StringProp;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public abstract class BeeDom {
  static final class ElementAttribute extends JavaScriptObject implements
      Transformable {
    protected ElementAttribute() {
    }

    public native String getName() /*-{
      return this.name;
    }-*/;

    public native String getValue() /*-{
      return this.value;
    }-*/;

    public native void setName(String nm) /*-{
      this.name = nm;
    }-*/;

    public native void setValue(String v) /*-{
      this.value = v;
    }-*/;

    public String transform() {
      return getName() + BeeConst.DEFAULT_VALUE_SEPARATOR + getValue();
    }
  }

  private static String TAG_INPUT = "input";

  private static String ATTRIBUTE_SERVICE = "data-svc";
  private static String ATTRIBUTE_STAGE = "data-stg";

  private static String DEFAULT_NAME_PREFIX = "b";

  private static int MAX_GENERATIONS = 100;

  public static String createUniqueId() {
    return DOM.createUniqueId();
  }

  public static String createUniqueName() {
    return BeeUtils.createUniqueName(DEFAULT_NAME_PREFIX);
  }

  public static String getId(UIObject obj) {
    Assert.notNull(obj);
    return obj.getElement().getId();
  }

  public static String setId(UIObject obj, String id) {
    Assert.notNull(obj);
    Assert.notEmpty(id);

    String s = id.trim();
    obj.getElement().setId(s);

    return s;
  }

  public static String setId(UIObject obj) {
    return setId(obj, createUniqueId());
  }

  public static int getTabIndex(Widget w) {
    Assert.notNull(w);
    return w.getElement().getTabIndex();
  }

  public static int setTabIndex(Widget w, int idx) {
    Assert.notNull(w);
    w.getElement().setTabIndex(idx);
    return idx;
  }

  public static String getService(Widget w) {
    return getAttribute(w, ATTRIBUTE_SERVICE);
  }

  public static String setService(Widget w, String svc) {
    return setAttribute(w, ATTRIBUTE_SERVICE, svc);
  }

  public static String getStage(Widget w) {
    return getAttribute(w, ATTRIBUTE_STAGE);
  }

  public static String setStage(Widget w, String stg) {
    return setAttribute(w, ATTRIBUTE_STAGE, stg);
  }

  public static String getAttribute(Widget w, String name) {
    Assert.notNull(w);
    Assert.notEmpty(name);

    return w.getElement().getAttribute(name);
  }

  public static String setAttribute(Widget w, String name, String value) {
    Assert.notNull(w);
    Assert.notEmpty(name);
    Assert.notEmpty(value);

    String s = name.trim();
    w.getElement().setAttribute(name.trim().toLowerCase(), value.trim());
    return s;
  }

  public static void removeAttribute(Widget w, String name) {
    Assert.notNull(w);
    Assert.notEmpty(name);

    w.getElement().removeAttribute(name);
  }

  public static native NodeList<Element> getElementsByName(String name) /*-{
    return $doc.getElementsByName(name);
  }-*/;

  public static List<Widget> getSiblings(Widget w) {
    Assert.notNull(w);

    Widget p = w.getParent();
    if (!(p instanceof HasWidgets)) {
      return null;
    }

    List<Widget> sib = new ArrayList<Widget>();

    for (Iterator<Widget> it = ((HasWidgets) p).iterator(); it.hasNext();) {
      sib.add(it.next());
    }

    return sib;
  }

  public static native JsArray<ElementAttribute> getNativeAttributes(Element el) /*-{
    return el.attributes;
  }-*/;

  public static List<StringProp> getAttributes(Element el) {
    Assert.notNull(el);

    JsArray<ElementAttribute> arr = getNativeAttributes(el);
    if (arr == null)
      return null;

    List<StringProp> lst = new ArrayList<StringProp>();
    ElementAttribute attr;

    for (int i = 0; i < arr.length(); i++) {
      attr = arr.get(i);
      lst.add(new StringProp(attr.getName(), attr.getValue()));
    }

    return lst;
  }

  public static boolean isInputElement(Element el) {
    Assert.notNull(el);
    return el.getTagName().equalsIgnoreCase(TAG_INPUT);
  }

  public static List<String> getAncestry(Widget w) {
    Assert.notNull(w);
    List<String> lst = new ArrayList<String>();

    Widget p = w.getParent();
    if (p == null) {
      return lst;
    }

    for (int i = 0; i < MAX_GENERATIONS; i++) {
      lst.add(BeeUtils.concat(1, i, p.getClass().getName(), p.getElement()
          .getId(), p.getStyleName()));

      p = p.getParent();
      if (p == null) {
        break;
      }
    }

    return lst;
  }

  public static PopupPanel parentPopup(Widget w) {
    Assert.notNull(w);

    Widget p = w;
    for (int i = 0; i < MAX_GENERATIONS; i++) {
      if (p instanceof PopupPanel) {
        return (PopupPanel) p;
      }

      p = p.getParent();
      if (p == null) {
        break;
      }
    }

    return null;
  }

}
