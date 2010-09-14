package com.butent.bee.egg.client.utils;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.Transformable;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BeeDom {
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

  private static String DEFAULT_ID_PREFIX = "b";
  private static String ID_SEPARATOR = "-";
  private static int ID_COUNTER = 0;

  private static int MAX_GENERATIONS = 100;

  public static String createId(UIObject obj, String prefix) {
    Assert.notNull(obj);
    Assert.notEmpty(prefix);

    String id = createUniqueId(prefix);
    obj.getElement().setId(id);

    return id;
  }

  public static String createUniqueId() {
    return createUniqueId(DEFAULT_ID_PREFIX);
  }

  public static String createUniqueId(String prefix) {
    ID_COUNTER++;
    return prefix.trim() + ID_SEPARATOR + ID_COUNTER;
  }

  public static String createUniqueName() {
    return BeeUtils.createUniqueName(DEFAULT_NAME_PREFIX);
  }

  public static List<String> getAncestry(Widget w) {
    Assert.notNull(w);
    List<String> lst = new ArrayList<String>();

    Widget p = w.getParent();
    if (p == null) {
      return lst;
    }

    for (int i = 0; i < MAX_GENERATIONS; i++) {
      lst.add(BeeUtils.concat(1, i, p.getClass().getName(),
          p.getElement().getId(), p.getStyleName()));

      p = p.getParent();
      if (p == null) {
        break;
      }
    }

    return lst;
  }

  public static String getAttribute(Widget w, String name) {
    Assert.notNull(w);
    Assert.notEmpty(name);

    return w.getElement().getAttribute(name);
  }

  public static List<StringProp> getAttributes(Element el) {
    Assert.notNull(el);

    JsArray<ElementAttribute> arr = getNativeAttributes(el);
    if (arr == null) {
      return null;
    }

    List<StringProp> lst = new ArrayList<StringProp>();
    ElementAttribute attr;

    for (int i = 0; i < arr.length(); i++) {
      attr = arr.get(i);
      lst.add(new StringProp(attr.getName(), attr.getValue()));
    }

    return lst;
  }

  public static String getClassQuietly(Object obj) {
    if (obj == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return obj.getClass().getName();
    }
  }

  public static int getClientHeight() {
    return Document.get().getClientHeight();
  }

  public static int getClientWidth() {
    return Document.get().getClientWidth();
  }

  public static List<StringProp> getElementInfo(Element el) {
    Assert.notNull(el);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Absolute Bottom", el.getAbsoluteBottom(),
        "Absolute Left", el.getAbsoluteLeft(), "Absolute Right",
        el.getAbsoluteRight(), "Absolute Top", el.getAbsoluteTop(),
        "Class Name", el.getClassName(), "Client Height", el.getClientHeight(),
        "Client Width", el.getClientWidth(), "Dir", el.getDir(), "Id",
        el.getId(), "First Child Element",
        transformElement(el.getFirstChildElement()), "Inner HTML",
        el.getInnerHTML(), "Inner Text", el.getInnerText(), "Lang",
        el.getLang(), "Next Sibling Element",
        transformElement(el.getNextSiblingElement()), "Offset Height",
        el.getOffsetHeight(), "Offset Left", el.getOffsetLeft(),
        "Offset Parent", transformElement(el.getOffsetParent()), "Offset Top",
        el.getOffsetTop(), "Offset Width", el.getOffsetWidth(),
        "Scroll Height", el.getScrollHeight(), "Scroll Left",
        el.getScrollLeft(), "Scroll Top", el.getScrollTop(), "Scroll Width",
        el.getScrollWidth(), "Tab Index", el.getTabIndex(), "Tag Name",
        el.getTagName(), "Title", el.getTitle());

    return lst;
  }

  public static native NodeList<Element> getElementsByName(String name) /*-{
    return $doc.getElementsByName(name);
  }-*/;

  public static String getId(UIObject obj) {
    Assert.notNull(obj);
    return obj.getElement().getId();
  }

  public static List<SubProp> getInfo(Object obj, String prefix, int depth) {
    Assert.notNull(obj);
    List<SubProp> lst = new ArrayList<SubProp>();

    if (obj instanceof Element) {
      PropUtils.appendString(lst, BeeUtils.concat(1, prefix, "Element"),
          getElementInfo((Element) obj));
    }
    if (obj instanceof Node) {
      PropUtils.appendString(lst, BeeUtils.concat(1, prefix, "Node"),
          getNodeInfo((Node) obj));
    }

    if (obj instanceof Widget) {
      PropUtils.appendString(lst, BeeUtils.concat(1, prefix, "Widget"),
          getWidgetInfo((Widget) obj));
    }
    if (obj instanceof UIObject) {
      PropUtils.appendSub(lst, getUIObjectExtendedInfo((UIObject) obj, prefix));
    }

    if (obj instanceof HasWidgets && depth > 0) {
      Widget w;
      int i = 0;
      String p;

      for (Iterator<Widget> iter = ((HasWidgets) obj).iterator(); iter.hasNext();) {
        w = iter.next();
        p = BeeUtils.concat(BeeConst.DEFAULT_PROPERTY_SEPARATOR, prefix, i++);

        if (depth == 1) {
          PropUtils.appendSub(lst, getWidgetExtendedInfo(w, p));
        } else {
          getInfo(w, p, depth--);
        }
      }
    }

    return lst;
  }

  public static native JsArray<ElementAttribute> getNativeAttributes(Element el) /*-{
    return el.attributes;
  }-*/;

  public static List<StringProp> getNodeInfo(Node nd) {
    Assert.notNull(nd);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Child Count", nd.getChildCount(), "First Child",
        transformNode(nd.getFirstChild()), "Last Child",
        transformNode(nd.getLastChild()), "Next Sibling",
        transformNode(nd.getNextSibling()), "Node Name", nd.getNodeName(),
        "Node Type", nd.getNodeType(), "Node Value", nd.getNodeValue(),
        "Parent Element", transformElement(nd.getParentElement()),
        "Parent Node", transformNode(nd.getParentNode()), "Previous Sibling",
        transformNode(nd.getPreviousSibling()), "Has Child Nodes",
        nd.hasChildNodes(), "Has Parent Element", nd.hasParentElement());

    return lst;
  }

  public static String getService(Widget w) {
    return getAttribute(w, ATTRIBUTE_SERVICE);
  }

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

  public static String getStage(Widget w) {
    return getAttribute(w, ATTRIBUTE_STAGE);
  }

  public static List<StringProp> getStyleInfo(Style st) {
    Assert.notNull(st);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Background Color", st.getBackgroundColor(),
        "Background Image", st.getBackgroundImage(), "Border Color",
        st.getBorderColor(), "Border Style", st.getBorderStyle(),
        "Border Width", st.getBorderWidth(), "Bottom", st.getBottom(), "Color",
        st.getColor(), "Cursor", st.getCursor(), "Display", st.getDisplay(),
        "Font Size", st.getFontSize(), "Font Style", st.getFontStyle(),
        "Font Weight", st.getFontWeight(), "Height", st.getHeight(), "Left",
        st.getLeft(), "List Style Type", st.getListStyleType(), "Margin",
        st.getMargin(), "Margin Bottom", st.getMarginBottom(), "Margin Left",
        st.getMarginLeft(), "Margin Right", st.getMarginRight(), "Margin Top",
        st.getMarginTop(), "Opacity", st.getOpacity(), "Overflow",
        st.getOverflow(), "Padding", st.getPadding(), "Padding Bottom",
        st.getPaddingBottom(), "Padding Left", st.getPaddingLeft(),
        "Padding Right", st.getPaddingRight(), "Padding Top",
        st.getPaddingTop(), "Position", st.getPosition(), "Right",
        st.getRight(), "Text Decoration", st.getTextDecoration(), "Top",
        st.getTop(), "Vertical Align", st.getVerticalAlign(), "Visibility",
        st.getVisibility(), "Width", st.getWidth(), "Z Index", st.getZIndex());

    return lst;
  }

  public static int getTabIndex(Widget w) {
    Assert.notNull(w);
    return w.getElement().getTabIndex();
  }

  public static List<SubProp> getUIObjectExtendedInfo(UIObject obj,
      String prefix) {
    Assert.notNull(obj);
    List<SubProp> lst = new ArrayList<SubProp>();

    PropUtils.appendString(lst, BeeUtils.concat(1, prefix, "UI Object"),
        getUIObjectInfo(obj));

    Element el = obj.getElement();
    PropUtils.appendString(lst, BeeUtils.concat(1, prefix, "Element"),
        getElementInfo(el));

    Style st = el.getStyle();
    if (st != null) {
      PropUtils.appendString(lst, BeeUtils.concat(1, prefix, "Style"),
          getStyleInfo(st));
    }
    PropUtils.appendString(lst, BeeUtils.concat(1, prefix, "Node"),
        getNodeInfo(el));

    return lst;
  }

  public static List<StringProp> getUIObjectInfo(UIObject obj) {
    Assert.notNull(obj);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Absolute Left", obj.getAbsoluteLeft(),
        "Absolute Top", obj.getAbsoluteTop(), "Class", getClassQuietly(obj),
        "Offset Height", obj.getOffsetHeight(), "Offset Width",
        obj.getOffsetWidth(), "Style Name", obj.getStyleName(),
        "Style Primary Name", obj.getStylePrimaryName(), "Title",
        obj.getTitle(), "Visible", obj.isVisible());

    return lst;
  }

  public static List<SubProp> getWidgetExtendedInfo(Widget w, String prefix) {
    Assert.notNull(w);
    List<SubProp> lst = new ArrayList<SubProp>();

    PropUtils.appendString(lst, BeeUtils.concat(1, prefix, "Widget"),
        getWidgetInfo(w));
    PropUtils.appendSub(lst, getUIObjectExtendedInfo(w, prefix));

    return lst;
  }

  public static List<StringProp> getWidgetInfo(Widget w) {
    Assert.notNull(w);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Class", getClassQuietly(w), "Layout Data",
        w.getLayoutData(), "Parent", getClassQuietly(w.getParent()),
        "Attached", w.isAttached());

    return lst;
  }

  public static boolean isInputElement(Element el) {
    Assert.notNull(el);
    return el.getTagName().equalsIgnoreCase(TAG_INPUT);
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

  public static void removeAttribute(Widget w, String name) {
    Assert.notNull(w);
    Assert.notEmpty(name);

    w.getElement().removeAttribute(name);
  }

  public static String setAttribute(Widget w, String name, String value) {
    Assert.notNull(w);
    Assert.notEmpty(name);
    Assert.notEmpty(value);

    String s = name.trim();
    w.getElement().setAttribute(name.trim().toLowerCase(), value.trim());
    return s;
  }

  public static String setId(UIObject obj, String id) {
    Assert.notNull(obj);
    Assert.notEmpty(id);

    String s = id.trim();
    obj.getElement().setId(s);

    return s;
  }

  public static String setService(Widget w, String svc) {
    return setAttribute(w, ATTRIBUTE_SERVICE, svc);
  }

  public static String setStage(Widget w, String stg) {
    return setAttribute(w, ATTRIBUTE_STAGE, stg);
  }

  public static int setTabIndex(Widget w, int idx) {
    Assert.notNull(w);
    w.getElement().setTabIndex(idx);
    return idx;
  }

  private static String transformElement(Element el) {
    if (el == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.concat(1, el.getTagName(), el.getId());
    }
  }

  private static String transformNode(Node nd) {
    if (nd == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.concat(1, nd.getNodeName(), nd.getNodeValue());
    }
  }

}
