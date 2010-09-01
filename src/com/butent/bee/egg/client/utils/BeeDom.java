package com.butent.bee.egg.client.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    @Override
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

  public static String getId(Widget w) {
    if (w == null)
      return null;
    else
      return w.getElement().getId();
  }

  public static String setId(Widget w, String id) {
    if (w == null || BeeUtils.isEmpty(id))
      return null;
    else {
      String s = id.trim();
      w.getElement().setId(s);
      return s;
    }
  }

  public static String setId(Widget w) {
    return setId(w, createUniqueId());
  }

  public static int getTabIndex(Widget w) {
    if (w == null)
      return BeeConst.INDEX_UNKNOWN;
    else
      return w.getElement().getTabIndex();
  }

  public static int setTabIndex(Widget w, int idx) {
    if (w == null)
      return BeeConst.INDEX_UNKNOWN;
    else {
      w.getElement().setTabIndex(idx);
      return idx;
    }
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
    if (w == null || BeeUtils.isEmpty(name))
      return null;
    else
      return w.getElement().getAttribute(name);
  }

  public static String setAttribute(Widget w, String name, String value) {
    if (w == null || BeeUtils.isEmpty(name) || BeeUtils.isEmpty(value))
      return null;
    else {
      String s = name.trim();
      w.getElement().setAttribute(name.trim().toLowerCase(), value.trim());
      return s;
    }
  }

  public static void removeAttribute(Widget w, String name) {
    if (w != null && !BeeUtils.isEmpty(name))
      w.getElement().removeAttribute(name);
  }

  public static native NodeList<Element> getElementsByName(String name) /*-{
                                                                        return $doc.getElementsByName(name);
                                                                        }-*/;

  public static List<Widget> getSiblings(Widget w) {
    if (w == null)
      return null;

    Widget p = w.getParent();
    if (!(p instanceof HasWidgets))
      return null;

    List<Widget> sib = new ArrayList<Widget>();

    for (Iterator<Widget> it = ((HasWidgets) p).iterator(); it.hasNext();)
      sib.add(it.next());

    return sib;
  }

  public static native JsArray<ElementAttribute> getNativeAttributes(Element el) /*-{
                                                                                 return el.attributes;
                                                                                 }-*/;

  public static List<StringProp> getAttributes(Element el) {
    if (el == null)
      return null;

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
    if (el == null)
      return false;
    else
      return el.getTagName().equalsIgnoreCase(TAG_INPUT);
  }

  public static List<String> getAncestry(Widget w) {
    if (w == null)
      return null;

    Widget p = w.getParent();
    if (p == null)
      return null;

    List<String> lst = new ArrayList<String>();

    for (int i = 0; i < MAX_GENERATIONS; i++) {
      lst.add(BeeUtils.concat(1, i, p.getClass().getName(), p.getElement()
          .getId(), p.getStyleName()));

      p = p.getParent();
      if (p == null)
        break;
    }

    return lst;
  }

  public static PopupPanel parentPopup(Widget w) {
    if (w == null)
      return null;

    Widget p = w;
    for (int i = 0; i < MAX_GENERATIONS; i++) {
      if (p instanceof PopupPanel)
        return (PopupPanel) p;

      p = p.getParent();
      if (p == null)
        break;
    }

    return null;
  }

}
