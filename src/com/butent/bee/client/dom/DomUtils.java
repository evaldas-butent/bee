package com.butent.bee.client.dom;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.BeeStyle;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.utils.JreEmulation;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DomUtils {
  static final class ElementAttribute extends JavaScriptObject implements Transformable {
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

  public static final String TAG_AUDIO = "audio";
  public static final String TAG_BUTTON = "button";
  public static final String TAG_CANVAS = "canvas";
  public static final String TAG_DIV = "div";
  public static final String TAG_HEAD = "head";
  public static final String TAG_INPUT = "input";
  public static final String TAG_LABEL = "label";
  public static final String TAG_METER = "meter";
  public static final String TAG_OPTION = "option";
  public static final String TAG_PROGRESS = "progress";
  public static final String TAG_SPAN = "span";
  public static final String TAG_SVG = "svg";
  public static final String TAG_TABLE = "table";
  public static final String TAG_TD = "td";
  public static final String TAG_TH = "th";
  public static final String TAG_VIDEO = "video";

  public static final String DEFAULT_ID_PREFIX = "bee";

  public static final String BUTTON_ID_PREFIX = "bu";
  public static final String HTML_ID_PREFIX = "html";
  public static final String LABEL_ID_PREFIX = "lbl";
  public static final String LIST_ITEM_ID_PREFIX = "li";
  public static final String RADIO_ID_PREFIX = "rb";
  public static final String OPTION_ID_PREFIX = "opt";
  public static final String TABLE_CELL_ID_PREFIX = "td";

  public static final String ATTRIBUTE_CELL_PADDING = "cellPadding";
  public static final String ATTRIBUTE_CELL_SPACING = "cellSpacing";
  public static final String ATTRIBUTE_CHECKED = "checked";
  public static final String ATTRIBUTE_COL_SPAN = "colSpan";
  public static final String ATTRIBUTE_DRAGGABLE = "draggable";
  public static final String ATTRIBUTE_HIGH = "high";
  public static final String ATTRIBUTE_LOW = "low";
  public static final String ATTRIBUTE_MIN = "min";
  public static final String ATTRIBUTE_MAX = "max";
  public static final String ATTRIBUTE_OPTIMUM = "optimum";
  public static final String ATTRIBUTE_PLACEHOLDER = "placeholder";
  public static final String ATTRIBUTE_POSITION = "position";
  public static final String ATTRIBUTE_ROW_SPAN = "rowSpan";
  public static final String ATTRIBUTE_STEP = "step";
  public static final String ATTRIBUTE_TYPE = "type";
  public static final String ATTRIBUTE_VALUE = "value";

  public static final String ATTRIBUTE_SERVICE = "data-svc";
  public static final String ATTRIBUTE_STAGE = "data-stg";
  
  public static final String TYPE_SEARCH = "search";

  public static final String VALUE_TRUE = "true";

  private static final String DEFAULT_NAME_PREFIX = "b";

  private static final String ID_SEPARATOR = "-";

  private static final int MAX_GENERATIONS = 100;
  
  private static int idCounter = 0;

  private static int scrollbarWidth = -1;
  private static int scrollbarHeight = -1;

  private static int textBoxOffsetWidth = -1;
  private static int textBoxOffsetHeight = -1;
  private static int textBoxClientWidth = -1;
  private static int textBoxClientHeight = -1;
  
  public static Element createButton(String text) {
    return createButton(text, false, null);
  }

  public static Element createButton(String text, boolean asHtml, String id) {
    Assert.notEmpty(text);

    ButtonElement elem = Document.get().createPushButtonElement();
    if (asHtml) {
      elem.setInnerHTML(text);
    } else {
      elem.setInnerText(text);
    }

    String s;
    if (BeeUtils.isEmpty(id)) {
      s = createUniqueId(BUTTON_ID_PREFIX);
    } else {
      s = id.trim();
    }
    elem.setId(s);

    return elem;
  }

  public static DdElement createDdElement() {
    return (DdElement) createElement(DdElement.TAG);
  }

  public static Element createDefinitionItem(boolean term, String text) {
    return createDefinitionItem(term, text, false, null);
  }

  public static Element createDefinitionItem(boolean term, String text, boolean asHtml) {
    return createDefinitionItem(term, text, asHtml, null);
  }

  public static Element createDefinitionItem(boolean term, String text, boolean asHtml, String id) {
    Element elem;
    if (term) {
      elem = createElement(DtElement.TAG);
    } else {
      elem = createElement(DdElement.TAG);
    }
    
    if (!BeeUtils.isEmpty(text)) {
      if (asHtml) {
        elem.setInnerHTML(text);
      } else {
        elem.setInnerText(text);
      }
    }

    String s;
    if (BeeUtils.isEmpty(id)) {
      s = createUniqueId(elem.getTagName().toLowerCase());
    } else {
      s = id.trim();
    }
    elem.setId(s);

    return elem;
  }

  public static DtElement createDtElement() {
    return (DtElement) createElement(DtElement.TAG);
  }

  public static native Element createElement(Document doc, String tag) /*-{
    return doc.createElement(tag);
  }-*/;

  public static Element createElement(String tag) {
    Assert.notEmpty(tag);
    return createElement(Document.get(), tag);
  }

  public static native Element createElementNs(Document doc, String ns, String tag) /*-{
    return doc.createElementNS(ns, tag);
  }-*/;

  public static Element createElementNs(String ns, String tag) {
    Assert.notEmpty(ns);
    Assert.notEmpty(tag);
    return createElementNs(Document.get(), ns, tag);
  }
  
  public static Element createHtml(String html) {
    return createHtml(html, null);
  }

  public static Element createHtml(String html, String id) {
    Assert.notEmpty(html);

    SpanElement elem = Document.get().createSpanElement();
    elem.setInnerHTML(html);

    String s;
    if (BeeUtils.isEmpty(id)) {
      s = createUniqueId(HTML_ID_PREFIX);
    } else {
      s = id.trim();
    }
    elem.setId(s);

    return elem;
  }

  public static String createId(Element elem, String prefix) {
    Assert.notNull(elem);
    Assert.notEmpty(prefix);

    String id = createUniqueId(prefix);
    elem.setId(id);

    return id;
  }

  public static String createId(UIObject obj, String prefix) {
    Assert.notNull(obj);
    return createId(obj.getElement(), prefix);
  }

  public static Element createLabel(String text) {
    return createLabel(text, null, false);
  }

  public static Element createLabel(String text, boolean asDiv) {
    return createLabel(text, null, asDiv);
  }

  public static Element createLabel(String text, String id, boolean asDiv) {
    Assert.notEmpty(text);
    
    Element elem;
    if (asDiv) {
      elem = Document.get().createDivElement();
    } else {
      elem = Document.get().createSpanElement();
    }

    elem.setInnerText(text);

    String s;
    if (BeeUtils.isEmpty(id)) {
      s = createUniqueId(LABEL_ID_PREFIX);
    } else {
      s = id.trim();
    }
    elem.setId(s);

    return elem;
  }

  public static Element createListItem(String text) {
    return createListItem(text, false, null);
  }

  public static Element createListItem(String text, boolean asHtml) {
    return createListItem(text, asHtml, null);
  }

  public static Element createListItem(String text, boolean asHtml, String id) {
    LIElement elem = Document.get().createLIElement();
    
    if (!BeeUtils.isEmpty(text)) {
      if (asHtml) {
        elem.setInnerHTML(text);
      } else {
        elem.setInnerText(text);
      }
    }

    String s;
    if (BeeUtils.isEmpty(id)) {
      s = createUniqueId(LIST_ITEM_ID_PREFIX);
    } else {
      s = id.trim();
    }
    elem.setId(s);

    return elem;
  }

  public static Element createOption(String text) {
    return createOption(text, false, null);
  }

  public static Element createOption(String text, boolean asHtml) {
    return createOption(text, asHtml, null);
  }

  public static Element createOption(String text, boolean asHtml, String id) {
    OptionElement elem = Document.get().createOptionElement();

    if (!BeeUtils.isEmpty(text)) {
      if (asHtml) {
        elem.setInnerHTML(text);
      } else {
        elem.setInnerText(text);
      }
    }

    String s;
    if (BeeUtils.isEmpty(id)) {
      s = createUniqueId(OPTION_ID_PREFIX);
    } else {
      s = id.trim();
    }
    elem.setId(s);

    return elem;
  }

  public static Element createRadio(String name, String text) {
    return createRadio(name, text, null);
  }

  public static Element createRadio(String name, String text, String id) {
    Assert.notEmpty(name);
    Assert.notEmpty(text);

    SpanElement elem = Document.get().createSpanElement();
    InputElement input = Document.get().createRadioInputElement(name);
    LabelElement label = Document.get().createLabelElement();

    label.setInnerText(text);

    String s = createUniqueId("ri");
    input.setId(s);
    label.setHtmlFor(s);

    elem.appendChild(input);
    elem.appendChild(label);

    if (BeeUtils.isEmpty(id)) {
      s = createUniqueId(RADIO_ID_PREFIX);
    } else {
      s = id.trim();
    }
    elem.setId(s);

    return elem;
  }
  
  public static Element createTableCell(String text) {
    return createTableCell(text, false, null);
  }

  public static Element createTableCell(String text, boolean asHtml) {
    return createTableCell(text, asHtml, null);
  }

  public static Element createTableCell(String text, boolean asHtml, String id) {
    TableCellElement elem = Document.get().createTDElement();

    if (!BeeUtils.isEmpty(text)) {
      if (asHtml) {
        elem.setInnerHTML(text);
      } else {
        elem.setInnerText(text);
      }
    }

    String s;
    if (BeeUtils.isEmpty(id)) {
      s = createUniqueId(TABLE_CELL_ID_PREFIX);
    } else {
      s = id.trim();
    }
    elem.setId(s);

    return elem;
  }

  public static String createUniqueId() {
    return createUniqueId(DEFAULT_ID_PREFIX);
  }

  public static String createUniqueId(String prefix) {
    idCounter++;
    return prefix.trim() + ID_SEPARATOR + idCounter;
  }

  public static String createUniqueName() {
    return BeeUtils.createUniqueName(DEFAULT_NAME_PREFIX);
  }

  public static String ensureId(Element elem, String prefix) {
    Assert.notNull(elem);

    String id = elem.getId();
    if (BeeUtils.isEmpty(id)) {
      id = createId(elem, BeeUtils.ifString(prefix, DEFAULT_ID_PREFIX));
    }

    return id;
  }

  public static String ensureId(UIObject obj, String prefix) {
    Assert.notNull(obj);
    return ensureId(obj.getElement(), prefix);
  }

  public static List<String> getAncestry(Widget w) {
    Assert.notNull(w);
    List<String> lst = new ArrayList<String>();

    Widget p = w.getParent();
    if (p == null) {
      return lst;
    }

    for (int i = 0; i < MAX_GENERATIONS; i++) {
      lst.add(BeeUtils.concat(1, transformClass(p), p.getElement().getId(), p.getStyleName()));

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

  public static List<Property> getAttributes(Element el) {
    Assert.notNull(el);

    JsArray<ElementAttribute> arr = getNativeAttributes(el);
    if (arr == null) {
      return null;
    }

    List<Property> lst = new ArrayList<Property>();
    ElementAttribute attr;

    for (int i = 0; i < arr.length(); i++) {
      attr = arr.get(i);
      lst.add(new Property(attr.getName(), attr.getValue()));
    }

    return lst;
  }

  public static int getCellPadding(Element elem) {
    if (isTableElement(elem)) {
      return elem.getPropertyInt(ATTRIBUTE_CELL_PADDING);
    } else {
      return 0;
    }
  }

  public static int getCellSpacing(Element elem) {
    if (isTableElement(elem)) {
      return elem.getPropertyInt(ATTRIBUTE_CELL_SPACING);
    } else {
      return 0;
    }
  }

  public static List<Property> getChildrenInfo(Widget w) {
    Assert.notNull(w);
    List<Property> lst = new ArrayList<Property>();

    if (w instanceof HasWidgets) {
      for (Iterator<Widget> it = ((HasWidgets) w).iterator(); it.hasNext();) {
        Widget child = it.next();
        PropertyUtils.addProperty(lst, JreEmulation.getSimpleName(child), getId(child));
      }
    }

    return lst;
  }

  public static int getClientHeight() {
    return Document.get().getClientHeight();
  }

  public static int getClientWidth() {
    return Document.get().getClientWidth();
  }
  
  public static int getColSpan(Element elem) {
    if (isTableCellElement(elem)) {
      return elem.getPropertyInt(ATTRIBUTE_COL_SPAN);
    } else {
      return 0;
    }
  }

  public static Direction getDirection(String s) {
    Assert.notEmpty(s);
    Direction dir = null;

    for (Direction z : Direction.values()) {
      if (BeeUtils.same(z.name(), s)) {
        dir = z;
        break;
      }
      if (BeeUtils.startsSame(z.name(), s)) {
        dir = (dir == null) ? z : null;
      }
    }

    return dir;
  }

  public static List<Property> getElementInfo(Element el) {
    Assert.notNull(el);
    List<Property> lst = new ArrayList<Property>();

    PropertyUtils.addProperties(lst,
        "Absolute Bottom", el.getAbsoluteBottom(),
        "Absolute Left", el.getAbsoluteLeft(),
        "Absolute Right", el.getAbsoluteRight(),
        "Absolute Top", el.getAbsoluteTop(),
        "Class Name", el.getClassName(),
        "Client Height", el.getClientHeight(),
        "Client Width", el.getClientWidth(),
        "Dir", el.getDir(),
        "Id", el.getId(),
        "First Child Element", transformElement(el.getFirstChildElement()),
        "Inner HTML", el.getInnerHTML(),
        "Inner Text", el.getInnerText(),
        "Lang", el.getLang(),
        "Next Sibling Element", transformElement(el.getNextSiblingElement()),
        "Offset Height", el.getOffsetHeight(),
        "Offset Left", el.getOffsetLeft(),
        "Offset Parent", transformElement(el.getOffsetParent()),
        "Offset Top", el.getOffsetTop(),
        "Offset Width", el.getOffsetWidth(),
        "Scroll Height", el.getScrollHeight(),
        "Scroll Left", el.getScrollLeft(),
        "Scroll Top", el.getScrollTop(),
        "Scroll Width", el.getScrollWidth(),
        "Tab Index", el.getTabIndex(),
        "Tag Name", el.getTagName(),
        "Title", el.getTitle());

    return lst;
  }

  public static native NodeList<Element> getElementsByName(String name) /*-{
    return $doc.getElementsByName(name);
  }-*/;

  public static HeadElement getHead() {
    NodeList<Element> nodes = Document.get().getElementsByTagName(TAG_HEAD);
    if (nodes != null && nodes.getLength() > 0) {
      return HeadElement.as(nodes.getItem(0));
    }
    return null;
  }

  public static String getId(UIObject obj) {
    Assert.notNull(obj);
    return obj.getElement().getId();
  }

  public static List<ExtendedProperty> getInfo(Object obj, String prefix, int depth) {
    Assert.notNull(obj);
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    if (obj instanceof Element) {
      PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, prefix, "Element"),
          getElementInfo((Element) obj));
    }
    if (obj instanceof Node) {
      PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, prefix, "Node"),
          getNodeInfo((Node) obj));
    }

    if (obj instanceof Widget) {
      PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, prefix, "Widget"),
          getWidgetInfo((Widget) obj));
    }
    if (obj instanceof UIObject) {
      PropertyUtils.appendExtended(lst, getUIObjectExtendedInfo((UIObject) obj, prefix));
    }

    if (obj instanceof HasWidgets && depth > 0) {
      Widget w;
      int i = 0;
      String p;

      for (Iterator<Widget> iter = ((HasWidgets) obj).iterator(); iter.hasNext();) {
        w = iter.next();
        p = BeeUtils.concat(BeeConst.DEFAULT_PROPERTY_SEPARATOR, prefix, i++);

        if (depth == 1) {
          PropertyUtils.appendExtended(lst, getWidgetExtendedInfo(w, p));
        } else {
          getInfo(w, p, depth--);
        }
      }
    }
    return lst;
  }
  
  public static InputElement getInputElement(Element elem) {
    Assert.notNull(elem);
    InputElement input;

    if (isInputElement(elem)) {
      input = elem.cast();
    } else {
      NodeList<Element> lst = elem.getElementsByTagName(TAG_INPUT);
      if (lst.getLength() == 1) {
        input = lst.getItem(0).cast();
      } else {
        input = null;
      }
    }
    
    return input;
  }
  
  public static native String getNamespaceUri(Node nd) /*-{
    return nd.namespaceURI;
  }-*/;

  public static native JsArray<ElementAttribute> getNativeAttributes(Element el) /*-{
    return el.attributes;
  }-*/;
  
  public static List<Property> getNodeInfo(Node nd) {
    Assert.notNull(nd);
    List<Property> lst = new ArrayList<Property>();

    PropertyUtils.addProperties(lst,
        "Child Count", nd.getChildCount(),
        "First Child", transformNode(nd.getFirstChild()),
        "Last Child", transformNode(nd.getLastChild()),
        "Next Sibling", transformNode(nd.getNextSibling()),
        "Node Name", nd.getNodeName(),
        "Node Type", nd.getNodeType(),
        "Node Value", nd.getNodeValue(),
        "Parent Element", transformElement(nd.getParentElement()),
        "Parent Node", transformNode(nd.getParentNode()),
        "Previous Sibling", transformNode(nd.getPreviousSibling()),
        "Has Child Nodes", nd.hasChildNodes(),
        "Has Parent Element", nd.hasParentElement());

    return lst;
  }
  
  public static String getParentId(Element elem, boolean find) {
    Assert.notNull(elem);
    
    Element parent = elem.getParentElement();
    if (parent == null) {
      return null;
    }
    
    String id = parent.getId();
    if (!find || !BeeUtils.isEmpty(id)) {
      return id;
    }
    
    return getParentId(parent, find);
  }

  public static List<Property> getPathInfo(Widget w) {
    Assert.notNull(w);
    List<Property> lst = new ArrayList<Property>();

    for (Widget p = w; p != null; p = p.getParent()) {
      PropertyUtils.addProperty(lst, JreEmulation.getSimpleName(p), getId(p));
    }
    return lst;
  }
  
  public static int getRowSpan(Element elem) {
    if (isTableCellElement(elem)) {
      return elem.getPropertyInt(ATTRIBUTE_ROW_SPAN);
    } else {
      return 0;
    }
  }

  public static int getScrollbarHeight() {
    if (scrollbarHeight <= 0) {
      calculateScrollbarSize();
    }
    return scrollbarHeight;
  }

  public static int getScrollbarWidth() {
    if (scrollbarWidth <= 0) {
      calculateScrollbarSize();
    }
    return scrollbarWidth;
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

  public static List<Property> getStyleInfo(Style st) {
    Assert.notNull(st);
    List<Property> lst = new ArrayList<Property>();

    PropertyUtils.addProperties(lst,
        "Background Color", st.getBackgroundColor(),
        "Background Image", st.getBackgroundImage(), 
        "Border Color", st.getBorderColor(),
        "Border Style", st.getBorderStyle(),
        "Border Width", st.getBorderWidth(),
        "Bottom", st.getBottom(),
        "Color", st.getColor(),
        "Cursor", st.getCursor(),
        "Display", st.getDisplay(),
        "Font Size", st.getFontSize(),
        "Font Style", st.getFontStyle(),
        "Font Weight", st.getFontWeight(),
        "Height", st.getHeight(),
        "Left", st.getLeft(),
        "List Style Type", st.getListStyleType(),
        "Margin", st.getMargin(),
        "Margin Bottom", st.getMarginBottom(),
        "Margin Left", st.getMarginLeft(),
        "Margin Right", st.getMarginRight(),
        "Margin Top", st.getMarginTop(),
        "Opacity", st.getOpacity(),
        "Overflow", st.getOverflow(),
        "Padding", st.getPadding(),
        "Padding Bottom", st.getPaddingBottom(),
        "Padding Left", st.getPaddingLeft(),
        "Padding Right", st.getPaddingRight(),
        "Padding Top", st.getPaddingTop(),
        "Position", st.getPosition(),
        "Right", st.getRight(),
        "Text Decoration", st.getTextDecoration(),
        "Top", st.getTop(),
        "Vertical Align", st.getVerticalAlign(),
        "Visibility", st.getVisibility(),
        "Width", st.getWidth(),
        "Z Index", st.getZIndex());

    return lst;
  }

  public static int getTabIndex(Widget w) {
    Assert.notNull(w);
    return w.getElement().getTabIndex();
  }

  public static String getText(Element elem) {
    if (elem == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return elem.getInnerText();
    }
  }

  public static String getText(UIObject obj) {
    if (obj == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return getText(obj.getElement());
    }
  }

  public static int getTextBoxClientHeight() {
    if (textBoxClientHeight <= 0) {
      calculateTextBoxSize();
    }
    return textBoxClientHeight;
  }

  public static int getTextBoxClientWidth() {
    if (textBoxClientWidth <= 0) {
      calculateTextBoxSize();
    }
    return textBoxClientWidth;
  }

  public static int getTextBoxOffsetHeight() {
    if (textBoxOffsetHeight <= 0) {
      calculateTextBoxSize();
    }
    return textBoxOffsetHeight;
  }

  public static int getTextBoxOffsetWidth() {
    if (textBoxOffsetWidth <= 0) {
      calculateTextBoxSize();
    }
    return textBoxOffsetWidth;
  }
  
  public static List<ExtendedProperty> getUIObjectExtendedInfo(UIObject obj, String prefix) {
    Assert.notNull(obj);
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, prefix, "UI Object"),
        getUIObjectInfo(obj));

    Element el = obj.getElement();
    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, prefix, "Element"),
        getElementInfo(el));

    Style st = el.getStyle();
    if (st != null) {
      PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, prefix, "Style"),
          getStyleInfo(st));
    }
    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, prefix, "Node"),
        getNodeInfo(el));

    return lst;
  }

  public static List<Property> getUIObjectInfo(UIObject obj) {
    Assert.notNull(obj);
    List<Property> lst = new ArrayList<Property>();

    PropertyUtils.addProperties(lst,
        "Absolute Left", obj.getAbsoluteLeft(),
        "Absolute Top", obj.getAbsoluteTop(),
        "Class", transformClass(obj),
        "Offset Height", obj.getOffsetHeight(),
        "Offset Width", obj.getOffsetWidth(),
        "Style Name", obj.getStyleName(),
        "Style Primary Name", obj.getStylePrimaryName(),
        "Title", obj.getTitle(),
        "Visible", obj.isVisible());

    return lst;
  }

  public static int getValueInt(String id) {
    Assert.notEmpty(id);
    Element elem = DOM.getElementById(id);
    Assert.notNull(elem, "id " + id + " element not found");
    
    if (JsUtils.hasProperty(elem, ATTRIBUTE_VALUE)) {
      return getValueInt(elem);
    }
    
    int value = 0;
    boolean found = false;
    
    NodeList<Node> children = elem.getChildNodes();
    int len = (children == null) ? 0 : children.getLength();
    for (int i = 0; i < len; i++) {
      Node nd = children.getItem(i);
      if (Element.is(nd) && JsUtils.hasProperty(nd, ATTRIBUTE_VALUE)) {
        value = getValueInt(Element.as(nd));
        found = true;
        break;
      }
    }

    Assert.isTrue(found, "id " + id + " element has no value and no valuable children");
    return value;
  }
  
  public static int getValueInt(UIObject obj) {
    Assert.notNull(obj);
    return getValueInt(obj.getElement());
  }
  
  public static int getValueInt(Element elem) {
    Assert.notNull(elem);
    return elem.getPropertyInt(ATTRIBUTE_VALUE);
  }
  
  public static Widget getWidget(Widget root, String id) {
    Assert.notNull(root);
    Assert.notEmpty(id);
    
    return getWidget(root, DOM.getElementById(id));
  }
  
  public static Widget getWidget(Widget root, Element elem) {
    if (root == null || elem == null) {
      return null;
    }
    
    if (root.getElement() == elem) {
      return root;
    }
    if (!root.getElement().isOrHasChild(elem)) {
      return null;
    }
    if (root instanceof HasOneWidget) {
      return getWidget(((HasOneWidget) root).getWidget(), elem);
    }
    
    Widget ret = null; 
    if (root instanceof HasWidgets) {
      Widget found;
      for (Iterator<Widget> it = ((HasWidgets) root).iterator(); it.hasNext(); ) {
        found = getWidget(it.next(), elem);
        if (found != null) {
          ret = found;
          break;
        }
      }
    }
    return ret;
  }

  public static int getWidgetCount(HasWidgets container) {
    Assert.notNull(container);
    int c = 0;
    for (Iterator<Widget> it = container.iterator(); it.hasNext();) {
      it.next();
      c++;
    }
    return c;
  }

  public static List<ExtendedProperty> getWidgetExtendedInfo(Widget w, String prefix) {
    Assert.notNull(w);
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, prefix, "Widget"),
        getWidgetInfo(w));
    PropertyUtils.appendExtended(lst, getUIObjectExtendedInfo(w, prefix));

    return lst;
  }

  public static List<Property> getWidgetInfo(Widget w) {
    Assert.notNull(w);
    List<Property> lst = new ArrayList<Property>();

    PropertyUtils.addProperties(lst,
        "Class", transformClass(w),
        "Layout Data", w.getLayoutData(),
        "Parent", transformClass(w.getParent()),
        "Attached", w.isAttached());

    return lst;
  }
  
  public static void injectExternalScript(String src) {
    Assert.notEmpty(src);
    Document doc = Document.get();
    ScriptElement script = doc.createScriptElement();
    script.setType("text/javascript");
    script.setSrc(src);
    doc.getBody().appendChild(script);
  }

  public static void injectExternalStyle(String css) {
    Assert.notEmpty(css);
    HeadElement head = getHead();
    Assert.notNull(head, "<head> element not found");

    LinkElement link = Document.get().createLinkElement();
    link.setType("text/css");
    link.setRel("stylesheet");
    link.setHref(css);
    head.appendChild(link);
  }

  public static boolean isChecked(String id) {
    Assert.notEmpty(id);
    Element elem = DOM.getElementById(id);
    Assert.notNull(elem, "id " + id + " element not found");
    return isChecked(elem);
  }
  
  public static boolean isChecked(UIObject obj) {
    Assert.notNull(obj);
    return isChecked(obj.getElement());
  }

  public static boolean isChecked(Element elem) {
    Assert.notNull(elem);
    InputElement input = getInputElement(elem);
    Assert.notNull(input, "input element not found");

    return input.getPropertyBoolean(ATTRIBUTE_CHECKED);
  }

  public static boolean isDirection(String s) {
    if (BeeUtils.isEmpty(s)) {
      return false;
    } else {
      return getDirection(s) != null;
    }
  }
  
  public static boolean isInputElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_INPUT);
  }

  public static boolean isLabelElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_LABEL);
  }

  public static boolean isTableCellElement(Element el) {
    return isTdElement(el) || isThElement(el);
  }
  
  public static boolean isTableElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_TABLE);
  }

  public static boolean isTdElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_TD);
  }

  public static boolean isThElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_TH);
  }
  
  public static void logChildren(Widget w) {
    Assert.notNull(w);
    List<Property> lst = getChildrenInfo(w);

    for (int i = 0; i < lst.size(); i++) {
      BeeKeeper.getLog().info(BeeUtils.progress(i + 1, lst.size()),
          lst.get(i).getName(), lst.get(i).getValue());
    }
    BeeKeeper.getLog().addSeparator();
  }

  public static void logPath(Widget w) {
    Assert.notNull(w);
    List<Property> lst = getPathInfo(w);

    for (int i = 0; i < lst.size(); i++) {
      BeeKeeper.getLog().info(BeeUtils.progress(i + 1, lst.size()),
          lst.get(i).getName(), lst.get(i).getValue());
    }
    BeeKeeper.getLog().addSeparator();
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
  
  public static void preventChildSelection(Element elem, boolean recurse, String... tags) {
    Assert.notNull(elem);
    NodeList<Node> children = elem.getChildNodes();
    if (children == null) {
      return;
    }
    int tagCnt = tags.length;
    Element child;
    
    for (int i = 0; i < children.getLength(); i++) {
      if (!Element.is(children.getItem(i))) {
        continue;
      }
      child = Element.as(children.getItem(i));
      if (tagCnt <= 0 || BeeUtils.inListSame(child.getTagName(), tags)) {
        preventSelection(child);
      }
      
      if (recurse) {
        preventChildSelection(child, recurse, tags);
      }
    }
  }

  public static void preventSelection(UIObject obj) {
    Assert.notNull(obj);
    preventSelection(obj.getElement());
  }
  
  public static void preventSelection(Element elem) {
    Assert.notNull(elem);
    elem.addClassName(BeeStyle.NAME_UNSELECTABLE);
  }

  public static void removeAttribute(Widget w, String name) {
    Assert.notNull(w);
    Assert.notEmpty(name);

    w.getElement().removeAttribute(name);
  }

  public static void setAttribute(UIObject obj, String name, int value) {
    setAttribute(obj, name, Integer.toString(value));
  }
  
  public static void setAttribute(UIObject obj, String name, String value) {
    Assert.notNull(obj);
    Assert.notEmpty(name);
    Assert.notEmpty(value);

    obj.getElement().setAttribute(name.trim().toLowerCase(), value.trim());
  }

  public static void setCheckValue(Element elem, boolean value) {
    Assert.notNull(elem);
    InputElement input = getInputElement(elem);
    Assert.notNull(input, "input element not found");

    input.setChecked(value);
    input.setDefaultChecked(value);
  }

  public static void setColSpan(Element elem, int span) {
    Assert.isTrue(isTableCellElement(elem), "not a table cell element");
    Assert.isPositive(span);
    
    TableCellElement.as(elem).setColSpan(span);
  }

  public static void setDraggable(UIObject obj) {
    Assert.notNull(obj);
    setDraggable(obj.getElement());
  }

  public static void setDraggable(Element elem) {
    Assert.notNull(elem);
    elem.setAttribute(ATTRIBUTE_DRAGGABLE, VALUE_TRUE);
  }
  
  public static Widget setHeight(Widget w, int height) {
    return setHeight(w, height, Unit.PX);
  }

  public static Widget setHeight(Widget w, int height, Unit unit) {
    Assert.notNull(w);
    Assert.nonNegative(height);
    w.getElement().getStyle().setHeight(height, unit);
    return w;
  }

  public static void setId(UIObject obj, String id) {
    Assert.notNull(obj);
    Assert.notEmpty(id);

    String s = id.trim();
    obj.getElement().setId(s);
  }
  
  public static void setInputType(UIObject obj, String type) {
    Assert.notNull(obj);
    setInputType(obj.getElement(), type);
  }

  public static void setInputType(Element elem, String type) {
    assertInputElement(elem);
    Assert.notEmpty(type);
    elem.setAttribute(ATTRIBUTE_TYPE, type);
  }

  public static void setMax(Widget w, int max) {
    setAttribute(w, ATTRIBUTE_MAX, max);
  }

  public static void setMin(Widget w, int min) {
    setAttribute(w, ATTRIBUTE_MIN, min);
  }

  public static boolean setPlaceholder(UIObject obj, String value) {
    Assert.notNull(obj);
    return setPlaceholder(obj.getElement(), value);
  }
  
  public static boolean setPlaceholder(Element elem, String value) {
    assertInputElement(elem);
    Assert.notEmpty(value);
    
    if (Features.supportsAttributePlaceholder()) {
      elem.setAttribute(ATTRIBUTE_PLACEHOLDER, value);
      return true;
    } else {
      return false;
    }
  }
  
  public static void setRowSpan(Element elem, int span) {
    Assert.isTrue(isTableCellElement(elem), "not a table cell element");
    Assert.isPositive(span);
    
    TableCellElement.as(elem).setRowSpan(span);
  }
  
  public static boolean setSearch(UIObject obj) {
    Assert.notNull(obj);
    return setSearch(obj.getElement());
  }

  public static boolean setSearch(Element elem) {
    assertInputElement(elem);
    if (Features.supportsInputSearch()) {
      setInputType(elem, TYPE_SEARCH);
      return true;
    } else {
      return false;
    }
  }
  
  public static void setSelected(Element elem, boolean selected) {
    Assert.notNull(elem);
    
    OptionElement.as(elem).setSelected(selected);
    OptionElement.as(elem).setDefaultSelected(selected);
  }

  public static void setService(Widget w, String svc) {
    setAttribute(w, ATTRIBUTE_SERVICE, svc);
  }

  public static void setStage(Widget w, String stg) {
    setAttribute(w, ATTRIBUTE_STAGE, stg);
  }

  public static void setStep(Widget w, int step) {
    setAttribute(w, ATTRIBUTE_STEP, step);
  }

  public static void setTabIndex(Widget w, int idx) {
    Assert.notNull(w);
    w.getElement().setTabIndex(idx);
  }

  public static void setText(String id, String text) {
    Assert.notEmpty(id);
    Element elem = DOM.getElementById(id);
    Assert.notNull(elem, "id " + id + " element not found");

    elem.setInnerText(text);
  }

  public static Widget setWidth(Widget w, int width) {
    return setWidth(w, width, Unit.PX);
  }

  public static Widget setWidth(Widget w, int width, Unit unit) {
    Assert.notNull(w);
    Assert.nonNegative(width);
    w.getElement().getStyle().setWidth(width, unit);
    return w;
  }
  
  public static String transform(Object obj) {
    if (obj == null) {
      return BeeConst.STRING_EMPTY;
    }

    if (obj instanceof Element) {
      return transformElement((Element) obj);
    }
    if (obj instanceof Node) {
      return transformNode((Node) obj);
    }

    if (obj instanceof Widget) {
      return transformWidget((Widget) obj);
    }
    if (obj instanceof UIObject) {
      return transformUIObject((UIObject) obj);
    }

    return BeeUtils.transform(obj);
  }

  public static String transformClass(Object obj) {
    if (obj == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return JreEmulation.getSimpleName(obj);
    }
  }

  public static String transformElement(Element el) {
    if (el == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.concat(1, el.getTagName(), el.getId(), el.getClassName());
    }
  }

  public static String transformUIObject(UIObject obj) {
    if (obj == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.concat(1, JreEmulation.getSimpleName(obj), obj.getElement().getId(),
          obj.getStyleName());
    }
  }

  public static String transformWidget(Widget w) {
    if (w == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.concat(1, JreEmulation.getSimpleName(w), w.getElement().getId(),
          w.getStyleName());
    }
  }
  
  private static void assertInputElement(Element elem) {
    Assert.isTrue(isInputElement(elem), "not an input element");
  }

  private static void calculateScrollbarSize() {
    Element elem = DOM.createDiv();
    elem.getStyle().setVisibility(Visibility.HIDDEN);
    elem.getStyle().setWidth(100, Unit.PX);
    elem.getStyle().setHeight(100, Unit.PX);
    elem.getStyle().setBorderWidth(0, Unit.PX);
    elem.getStyle().setMargin(0, Unit.PX);
    elem.getStyle().setOverflow(Overflow.SCROLL);
    
    Element body = Document.get().getBody();
    body.appendChild(elem);

    int w1 = elem.getOffsetWidth();
    int h1 = elem.getOffsetHeight();

    int w2 = elem.getClientWidth();
    int h2 = elem.getClientHeight();

    body.removeChild(elem);

    scrollbarWidth = w1 - w2;
    scrollbarHeight = h1 - h2;    
  }
  
  private static void calculateTextBoxSize() {
    Element elem = DOM.createInputText();

    Element body = Document.get().getBody();
    body.appendChild(elem);

    textBoxOffsetWidth = elem.getOffsetWidth();
    textBoxOffsetHeight = elem.getOffsetHeight();

    textBoxClientWidth = elem.getClientWidth();
    textBoxClientHeight = elem.getClientHeight();

    body.removeChild(elem);
  }

  private static String transformNode(Node nd) {
    if (nd == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.concat(1, nd.getNodeName(), nd.getNodeValue());
    }
  }
  
  private DomUtils() {
  }
}
