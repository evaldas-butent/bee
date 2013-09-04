package com.butent.bee.client.dom;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
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
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.style.ComputedStyles;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains necessary functions for reading and changing DOM information.
 */
public final class DomUtils {

  public static final String TAG_ASIDE = "aside";
  public static final String TAG_AUDIO = "audio";
  public static final String TAG_BUTTON = "button";
  public static final String TAG_CANVAS = "canvas";
  public static final String TAG_DIV = "div";
  public static final String TAG_HEAD = "head";
  public static final String TAG_IMG = "img";
  public static final String TAG_INPUT = "input";
  public static final String TAG_LABEL = "label";
  public static final String TAG_METER = "meter";
  public static final String TAG_OPTION = "option";
  public static final String TAG_PROGRESS = "progress";
  public static final String TAG_SELECT = "select";
  public static final String TAG_SPAN = "span";
  public static final String TAG_SVG = "svg";
  public static final String TAG_TABLE = "table";
  public static final String TAG_TD = "td";
  public static final String TAG_TEXT_AREA = "textarea";
  public static final String TAG_TH = "th";
  public static final String TAG_TR = "tr";
  public static final String TAG_VIDEO = "video";

  public static final String DEFAULT_ID_PREFIX = "bee";

  public static final String ATTRIBUTE_CHECKED = "checked";
  public static final String ATTRIBUTE_COL_SPAN = "colSpan";
  public static final String ATTRIBUTE_DEFAULT_CHECKED = "defaultChecked";
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
  public static final String ATTRIBUTE_VALUE = "value";

  public static final String ATTRIBUTE_DATA_INDEX = "data-idx";
  public static final String ATTRIBUTE_DATA_COLUMN = "data-col";
  public static final String ATTRIBUTE_DATA_ROW = "data-row";
  public static final String ATTRIBUTE_ROLE = "data-role";

  public static final String TYPE_SEARCH = "search";

  public static final String VALUE_TRUE = "true";

  public static final int MAX_GENERATIONS = 1000;

  public static final String ALL_TAGS = "*";

  private static final String DEFAULT_NAME_PREFIX = "b";

  private static final String ID_SEPARATOR = "-";

  private static int idCounter;

  private static int scrollBarWidth = -1;
  private static int scrollBarHeight = -1;

  private static int textBoxOffsetWidth = -1;
  private static int textBoxOffsetHeight = -1;
  private static int textBoxClientWidth = -1;
  private static int textBoxClientHeight = -1;

  private static int checkBoxOffsetWidth = -1;
  private static int checkBoxOffsetHeight = -1;
  private static int checkBoxClientWidth = -1;
  private static int checkBoxClientHeight = -1;

  private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

  private static final Collection<String> TABLE_CELL_TAGS = Sets.newHashSet(TAG_TD, TAG_TH);

  public static void allowSelection(Element elem) {
    Assert.notNull(elem);
    elem.removeClassName(StyleUtils.NAME_UNSELECTABLE);
  }

  public static void allowSelection(UIObject obj) {
    Assert.notNull(obj);
    preventSelection(obj.getElement());
  }

  public static void clear(Node nd) {
    Assert.notNull(nd);
    while (nd.getFirstChild() != null) {
      nd.removeChild(nd.getFirstChild());
    }
  }

  public static void clearTitle(UIObject obj) {
    Assert.notNull(obj);
    obj.setTitle(null);
  }

  public static Element createButton(String html) {
    ButtonElement elem = Document.get().createPushButtonElement();
    if (!BeeUtils.isEmpty(html)) {
      elem.setInnerHTML(html);
    }
    return elem;
  }

  public static DdElement createDdElement() {
    return (DdElement) createElement(DdElement.TAG);
  }

  public static Element createDefinitionItem(boolean term, String html) {
    Element elem;
    if (term) {
      elem = createElement(DtElement.TAG);
    } else {
      elem = createElement(DdElement.TAG);
    }

    if (!BeeUtils.isEmpty(html)) {
      elem.setInnerHTML(html);
    }
    return elem;
  }

  public static Element createDiv(String html) {
    DivElement element = Document.get().createDivElement();
    if (!BeeUtils.isEmpty(html)) {
      element.setInnerHTML(html);
    }
    return element;
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

  public static Element createListItem(String html) {
    LIElement elem = Document.get().createLIElement();
    if (!BeeUtils.isEmpty(html)) {
      elem.setInnerHTML(html);
    }
    return elem;
  }

  public static Element createOption(String html) {
    OptionElement elem = Document.get().createOptionElement();
    if (!BeeUtils.isEmpty(html)) {
      elem.setInnerHTML(html);
    }
    return elem;
  }

  public static Element createRadio(String name, String html) {
    Assert.notEmpty(name);

    SpanElement elem = Document.get().createSpanElement();
    InputElement input = Document.get().createRadioInputElement(name);
    LabelElement label = Document.get().createLabelElement();

    if (!BeeUtils.isEmpty(html)) {
      label.setInnerHTML(html);
    }

    String s = createUniqueId("ri");
    input.setId(s);
    label.setHtmlFor(s);

    elem.appendChild(input);
    elem.appendChild(label);

    return elem;
  }

  public static Element createSpan(String html) {
    SpanElement elem = Document.get().createSpanElement();
    if (!BeeUtils.isEmpty(html)) {
      elem.setInnerHTML(html);
    }
    return elem;
  }

  public static Element createSvg(String tag) {
    return createElementNs(SVG_NAMESPACE, tag);
  }

  public static TableCellElement createTableCell() {
    return Document.get().createTDElement();
  }

  public static Element createTableCell(String html) {
    TableCellElement elem = createTableCell();
    if (!BeeUtils.isEmpty(html)) {
      elem.setInnerHTML(html);
    }
    return elem;
  }

  public static TableRowElement createTableRow() {
    return Document.get().createTRElement();
  }

  public static Element createTableRow(List<String> cellContent) {
    TableRowElement rowElement = createTableRow();

    if (cellContent != null) {
      for (String text : cellContent) {
        rowElement.appendChild(createTableCell(text));
      }
    }
    return rowElement;
  }

  public static String createUniqueId() {
    return createUniqueId(DEFAULT_ID_PREFIX);
  }

  public static String createUniqueId(String prefix) {
    idCounter++;
    return prefix.trim() + ID_SEPARATOR + idCounter;
  }

  public static String createUniqueName() {
    return NameUtils.createUniqueName(DEFAULT_NAME_PREFIX);
  }

  public static void enableChildren(HasWidgets parent, boolean enabled) {
    Assert.notNull(parent);
    for (Widget child : parent) {
      if (child instanceof HasEnabled) {
        ((HasEnabled) child).setEnabled(enabled);
      }
    }
  }

  public static String ensureId(Element elem, String prefix) {
    Assert.notNull(elem);

    String id = elem.getId();
    if (BeeUtils.isEmpty(id)) {
      id = createId(elem, BeeUtils.notEmpty(prefix, DEFAULT_ID_PREFIX));
    }
    return id;
  }

  public static String ensureId(UIObject obj, String prefix) {
    Assert.notNull(obj);
    return ensureId(obj.getElement(), prefix);
  }

  public static boolean focus(UIObject obj) {
    if (obj instanceof Focusable && isEnabled(obj) && isVisible(obj)) {
      ((Focusable) obj).setFocus(true);
      return true;
    } else {
      return false;
    }
  }

  public static native Element getActiveElement() /*-{
    return $doc.activeElement;
  }-*/;

  public static List<String> getAncestry(Widget w) {
    Assert.notNull(w);
    List<String> lst = new ArrayList<String>();

    Widget p = w.getParent();
    if (p == null) {
      return lst;
    }

    for (int i = 0; i < MAX_GENERATIONS; i++) {
      lst.add(BeeUtils.joinWords(transformClass(p), p.getElement().getId(), p.getStyleName()));

      p = p.getParent();
      if (p == null) {
        break;
      }
    }
    return lst;
  }

  public static String getAttribute(UIObject obj, String name) {
    Assert.notNull(obj);
    Assert.notEmpty(name);

    return obj.getElement().getAttribute(name);
  }

  public static int getCheckBoxClientHeight() {
    if (checkBoxClientHeight <= 0) {
      calculateCheckBoxSize();
    }
    return checkBoxClientHeight;
  }

  public static int getCheckBoxClientWidth() {
    if (checkBoxClientWidth <= 0) {
      calculateCheckBoxSize();
    }
    return checkBoxClientWidth;
  }

  public static int getCheckBoxOffsetHeight() {
    if (checkBoxOffsetHeight <= 0) {
      calculateCheckBoxSize();
    }
    return checkBoxOffsetHeight;
  }

  public static int getCheckBoxOffsetWidth() {
    if (checkBoxOffsetWidth <= 0) {
      calculateCheckBoxSize();
    }
    return checkBoxOffsetWidth;
  }

  public static Widget getChild(Widget root, String id) {
    Assert.notNull(root);
    Assert.notEmpty(id);

    if (root.isAttached()) {
      return getPhysicalChild(root, id);
    } else {
      return getLogicalChild(root, id);
    }
  }

  public static Element getChildByDataIndex(Element parent, int dataIndex, boolean recurse) {
    if (parent == null || BeeConst.isUndef(dataIndex)) {
      return null;
    }

    for (Element child = parent.getFirstChildElement(); child != null; child =
        child.getNextSiblingElement()) {
      if (getDataIndexInt(child) == dataIndex) {
        return child;
      }
      
      if (recurse) {
        Element element = getChildByDataIndex(child, dataIndex, recurse);
        if (element != null) {
          return element;
        }
      }
    }
    return null;
  }

  public static Widget getChildByElement(Widget root, Element elem) {
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
      return getChildByElement(((HasOneWidget) root).getWidget(), elem);
    }
    if (!(root instanceof HasWidgets)) {
      return root;
    }

    Widget ret = null;
    Widget found;
    for (Widget child : (HasWidgets) root) {
      found = getChildByElement(child, elem);
      if (found != null) {
        ret = found;
        break;
      }
    }
    return ret;
  }

  public static Element getChildById(Element parent, String id) {
    Assert.notEmpty(id);
    NodeList<Element> children = getChildren(parent);
    if (children == null) {
      return null;
    }

    for (int i = 0; i < children.getLength(); i++) {
      Element child = children.getItem(i);
      if (BeeUtils.same(id, child.getId())) {
        return child;
      }
    }
    return null;
  }

  public static Widget getChildById(HasWidgets parent, String id) {
    Assert.notNull(parent);
    Assert.notEmpty(id);

    for (Widget child : parent) {
      if (idEquals(child, id)) {
        return child;
      }
    }
    return null;
  }

  public static int getChildOffsetHeight(Widget parent, String id) {
    Widget child = getChild(parent, id);
    if (child == null) {
      return BeeConst.UNDEF;
    } else {
      return child.getOffsetHeight();
    }
  }

  public static int getChildOffsetWidth(Widget parent, String id) {
    Widget child = getChild(parent, id);
    if (child == null) {
      return BeeConst.UNDEF;
    } else {
      return child.getOffsetWidth();
    }
  }

  public static Widget getChildQuietly(Widget root, String id) {
    if (root == null || BeeUtils.isEmpty(id)) {
      return null;
    }

    if (root.isAttached()) {
      return getChildByElement(root, DOM.getElementById(id));
    } else {
      return getLogicalChild(root, id);
    }
  }

  public static NodeList<Element> getChildren(Element parent) {
    Assert.notNull(parent);
    return parent.getElementsByTagName(ALL_TAGS);
  }

  public static List<Property> getChildrenInfo(Widget w) {
    Assert.notNull(w);
    List<Property> lst = new ArrayList<Property>();

    if (w instanceof HasWidgets) {
      for (Widget child : (HasWidgets) w) {
        PropertyUtils.addProperty(lst, NameUtils.getName(child), getId(child));
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

  public static String getDataColumn(Element elem) {
    return (elem == null) ? null : elem.getAttribute(ATTRIBUTE_DATA_COLUMN);
  }

  public static int getDataIndexInt(Element elem) {
    String value = (elem == null) ? null : elem.getAttribute(ATTRIBUTE_DATA_INDEX);
    return BeeUtils.isEmpty(value) ? BeeConst.UNDEF : BeeUtils.toInt(value);
  }

  public static long getDataIndexLong(Element elem) {
    String value = (elem == null) ? null : elem.getAttribute(ATTRIBUTE_DATA_INDEX);
    return BeeUtils.isEmpty(value) ? BeeConst.UNDEF : BeeUtils.toLong(value);
  }
  
  public static String getDataRow(Element elem) {
    return (elem == null) ? null : elem.getAttribute(ATTRIBUTE_DATA_ROW);
  }

  public static Element getElement(String id) {
    Assert.notEmpty(id);
    Element el = DOM.getElementById(id);
    Assert.notNull(el, "id " + id + " element not found");
    return el;
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

  public static Element getElementQuietly(String id) {
    if (BeeUtils.isEmpty(id)) {
      return null;
    } else {
      return DOM.getElementById(id);
    }
  }

  public static List<Element> getElementsByAttributeValue(Element root, String name, String value) {
    return getElementsByAttributeValue(root, name, value, null, null);
  }

  public static List<Element> getElementsByAttributeValue(Element root, String name, String value,
      Element excludeElement, Element cutoffElement) {
    Collection<Element> exclude = (excludeElement == null) ? null : Sets.newHashSet(excludeElement);
    Collection<Element> cutoff = (cutoffElement == null) ? null : Sets.newHashSet(cutoffElement);

    return getElementsByAttributeValueUsingCollectionFilters(root, name, value, exclude, cutoff);
  }

  public static List<Element> getElementsByAttributeValueUsingCollectionFilters(Element root,
      String name, String value, Collection<Element> exclude, Collection<Element> cutoff) {
    List<Element> result = Lists.newArrayList();
    if (root == null || BeeUtils.isEmpty(name)) {
      return result;
    }

    if (BeeUtils.same(root.getAttribute(name), value)
        && (exclude == null || !exclude.contains(root))) {
      result.add(root);
    }
    if (cutoff != null && cutoff.contains(root)) {
      return result;
    }

    NodeList<Element> children = getChildren(root);
    if (children == null) {
      return result;
    }

    for (int i = 0; i < children.getLength(); i++) {
      result.addAll(getElementsByAttributeValueUsingCollectionFilters(children.getItem(i),
          name, value, exclude, cutoff));
    }
    return result;
  }

  public static native NodeList<Element> getElementsByName(String name) /*-{
    return $doc.getElementsByName(name);
  }-*/;

  public static Element getFirstVisibleChild(Element parent) {
    if (parent == null) {
      return null;
    }

    for (Element child = parent.getFirstChildElement(); child != null; child =
        child.getNextSiblingElement()) {
      if (UIObject.isVisible(child)) {
        return child;
      }
    }
    return null;
  }

  public static HeadElement getHead() {
    NodeList<Element> nodes = Document.get().getElementsByTagName(TAG_HEAD);
    if (nodes != null && nodes.getLength() > 0) {
      return HeadElement.as(nodes.getItem(0));
    }
    return null;
  }

  public static String getHtml(String id) {
    Element elem = getElement(id);
    return elem.getInnerHTML();
  }

  public static String getId(UIObject obj) {
    Assert.notNull(obj);
    return obj.getElement().getId();
  }

  public static List<ExtendedProperty> getInfo(Object obj, String prefix, int depth) {
    Assert.notNull(obj);
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    if (obj instanceof Element) {
      PropertyUtils.appendChildrenToExtended(lst, BeeUtils.joinWords(prefix, "Element"),
          getElementInfo((Element) obj));
    }
    if (obj instanceof Node) {
      PropertyUtils.appendChildrenToExtended(lst, BeeUtils.joinWords(prefix, "Node"),
          getNodeInfo((Node) obj));
    }

    if (obj instanceof Widget) {
      PropertyUtils.appendChildrenToExtended(lst, BeeUtils.joinWords(prefix, "Widget"),
          getWidgetInfo((Widget) obj));
    }
    if (obj instanceof UIObject) {
      PropertyUtils.appendExtended(lst, getUIObjectExtendedInfo((UIObject) obj, prefix));
    }

    if (obj instanceof HasWidgets && depth > 0) {
      int i = 0;
      String p;

      for (Widget child : (HasWidgets) obj) {
        p = BeeUtils.join(BeeConst.DEFAULT_PROPERTY_SEPARATOR, prefix, i++);
        PropertyUtils.appendExtended(lst, getInfo(child, p, depth - 1));
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

  public static Widget getLogicalChild(Widget root, String id) {
    if (root == null || BeeUtils.isEmpty(id)) {
      return null;
    }

    if (idEquals(root, id)) {
      return root;
    }

    if (root instanceof HasOneWidget) {
      return getLogicalChild(((HasOneWidget) root).getWidget(), id);
    }
    if (!(root instanceof HasWidgets)) {
      return null;
    }

    Widget ret = null;
    Widget found;
    for (Widget child : (HasWidgets) root) {
      found = getLogicalChild(child, id);
      if (found != null) {
        ret = found;
        break;
      }
    }
    return ret;
  }

  public static native String getNamespaceUri(Node nd) /*-{
    return nd.namespaceURI;
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

  public static int getOuterHeight(Element elem) {
    Assert.notNull(elem);
    return elem.getOffsetHeight() + ComputedStyles.getPixels(elem, StyleUtils.STYLE_MARGIN_TOP)
        + ComputedStyles.getPixels(elem, StyleUtils.STYLE_MARGIN_BOTTOM);
  }

  public static native String getOuterHtml(Element elem) /*-{
    if (elem == null) {
      return "";
    }
    if (elem.outerHTML) {
      return elem.outerHTML;
    }

    if (elem.innerHTML) {
      var attributes = elem.attributes;
      var attrs = "";
      for ( var i = 0; i < attributes.length; i++) {
        attrs += " " + attributes[i].name + "=\"" + attributes[i].value + "\"";
      }
      return "<" + elem.tagName + attrs + ">" + elem.innerHTML + "</" + elem.tagName + ">";
    }

    return new XMLSerializer().serializeToString(elem);
  }-*/;

  public static int getOuterWidth(Element elem) {
    Assert.notNull(elem);
    return elem.getOffsetWidth() + ComputedStyles.getPixels(elem, StyleUtils.STYLE_MARGIN_LEFT)
        + ComputedStyles.getPixels(elem, StyleUtils.STYLE_MARGIN_RIGHT);
  }

  public static TableCellElement getParentCell(Element child, boolean incl) {
    Element parent = getParentElement(child, TABLE_CELL_TAGS, incl);
    if (isTableCellElement(parent)) {
      return TableCellElement.as(parent);
    } else {
      return null;
    }
  }

  public static TableCellElement getParentCell(UIObject obj, boolean incl) {
    Element parent = getParentElement(obj, TABLE_CELL_TAGS, incl);
    if (isTableCellElement(parent)) {
      return TableCellElement.as(parent);
    } else {
      return null;
    }
  }

  public static int getParentClientHeight(Widget widget) {
    Assert.notNull(widget);
    Assert.notNull(widget.getParent(), "Widget is orphan");
    return widget.getParent().getElement().getClientHeight();
  }

  public static int getParentClientWidth(Widget widget) {
    Assert.notNull(widget);
    Assert.notNull(widget.getParent(), "Widget is orphan");
    return widget.getParent().getElement().getClientWidth();
  }

  public static Element getParentElement(Element child, Collection<String> tagNames, boolean incl) {
    if (child == null) {
      return null;
    }

    if (incl && BeeUtils.containsSame(tagNames, child.getTagName())) {
      return child;
    } else {
      return getParentElement(child.getParentElement(), tagNames, true);
    }
  }

  public static Element getParentElement(Element child, String tagName, boolean incl) {
    if (child == null) {
      return null;
    }

    if (incl && BeeUtils.same(child.getTagName(), tagName)) {
      return child;
    } else {
      return getParentElement(child.getParentElement(), tagName, true);
    }
  }

  public static Element getParentElement(UIObject obj, Collection<String> tagNames, boolean incl) {
    if (obj == null) {
      return null;
    } else {
      return getParentElement(obj.getElement(), tagNames, incl);
    }
  }

  public static Element getParentElement(UIObject obj, String tagName, boolean incl) {
    if (obj == null) {
      return null;
    } else {
      return getParentElement(obj.getElement(), tagName, incl);
    }
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

  public static TableRowElement getParentRow(Element child, boolean incl) {
    Element parent = getParentElement(child, TAG_TR, incl);
    if (isTableRowElement(parent)) {
      return TableRowElement.as(parent);
    } else {
      return null;
    }
  }

  public static Widget getPhysicalChild(Widget root, String id) {
    Assert.notNull(root);
    return getChildByElement(root, getElement(id));
  }

  public static int getRelativeLeft(Element parent, Element child) {
    Assert.notNull(parent);
    Assert.notNull(child);
    Assert.isTrue(parent.isOrHasChild(child), "Parent does not contain child");
    if (parent == child) {
      return 0;
    }

    int left = 0;
    Element elem = child;
    while (elem != null) {
      left += elem.getOffsetLeft();
      elem = elem.getOffsetParent();
      if (elem == null || elem == parent || !elem.isOrHasChild(child)) {
        break;
      }
    }
    return left;
  }

  public static int getRelativeTop(Element parent, Element child) {
    Assert.notNull(parent);
    Assert.notNull(child);
    Assert.isTrue(parent.isOrHasChild(child), "Parent does not contain child");
    if (parent == child) {
      return 0;
    }

    int top = 0;
    Element elem = child;
    while (elem != null) {
      top += elem.getOffsetTop();
      elem = elem.getOffsetParent();
      if (elem == null || elem == parent || !elem.isOrHasChild(child)) {
        break;
      }
    }
    return top;
  }

  public static String getRole(Element el) {
    return Assert.notNull(el).getAttribute(ATTRIBUTE_ROLE);
  }

  public static int getRowSpan(Element elem) {
    if (isTableCellElement(elem)) {
      return elem.getPropertyInt(ATTRIBUTE_ROW_SPAN);
    } else {
      return 0;
    }
  }

  public static int getScrollBarHeight() {
    if (scrollBarHeight < 0) {
      calculateScrollBarSize();
    }
    return scrollBarHeight;
  }

  public static int getScrollBarWidth() {
    if (scrollBarWidth < 0) {
      calculateScrollBarSize();
    }
    return scrollBarWidth;
  }

  public static List<Widget> getSiblings(Widget w) {
    Assert.notNull(w);

    Widget p = w.getParent();
    if (!(p instanceof HasWidgets)) {
      return null;
    }

    List<Widget> sib = new ArrayList<Widget>();
    for (Widget c : (HasWidgets) p) {
      sib.add(c);
    }
    return sib;
  }

  public static int getTabIndex(Element el) {
    Assert.notNull(el);
    return el.getTabIndex();
  }

  public static int getTabIndex(UIObject obj) {
    Assert.notNull(obj);
    return obj.getElement().getTabIndex();
  }

  public static String getText(String id) {
    Element elem = getElement(id);
    return elem.getInnerText();
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

    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.joinWords(prefix, "UI Object"),
        getUIObjectInfo(obj));

    Element el = obj.getElement();
    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.joinWords(prefix, "Element"),
        getElementInfo(el));

    Style st = el.getStyle();
    if (st != null) {
      PropertyUtils.appendChildrenToExtended(lst, BeeUtils.joinWords(prefix, "Style"),
          StyleUtils.getStyleInfo(st));
    }
    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.joinWords(prefix, "Computed"),
        new ComputedStyles(el).getInfo());

    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.joinWords(prefix, "Node"),
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

  public static String getValue(Element elem) {
    Assert.notNull(elem);
    return elem.getPropertyString(ATTRIBUTE_VALUE);
  }

  public static int getValueInt(Element elem) {
    Assert.notNull(elem);
    return elem.getPropertyInt(ATTRIBUTE_VALUE);
  }

  public static int getValueInt(String id) {
    Element elem = getElement(id);

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

  public static List<Element> getVisibleChildren(Element parent) {
    List<Element> result = Lists.newArrayList();
    if (parent == null) {
      return result;
    }

    for (Element child = parent.getFirstChildElement(); child != null; child =
        child.getNextSiblingElement()) {
      if (UIObject.isVisible(child)) {
        result.add(child);
      }
    }
    return result;
  }

  public static Widget getWidget(String id) {
    Widget root = BeeKeeper.getScreen().getScreenPanel();
    return (root == null) ? null : getPhysicalChild(root, id);
  }

  public static int getWidgetCount(HasWidgets container) {
    Assert.notNull(container);
    return Iterables.size(container);
  }

  public static List<ExtendedProperty> getWidgetExtendedInfo(Widget w, String prefix) {
    Assert.notNull(w);
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.joinWords(prefix, "Widget"),
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

  public static boolean idEquals(Element el, String id) {
    if (el == null || BeeUtils.isEmpty(id)) {
      return false;
    } else {
      return BeeUtils.same(el.getId(), id);
    }
  }

  public static boolean idEquals(UIObject obj, String id) {
    if (obj == null) {
      return false;
    } else {
      return idEquals(obj.getElement(), id);
    }
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

  public static boolean isActive(Element elem) {
    if (elem == null) {
      return false;
    } else {
      return elem.equals(getActiveElement());
    }
  }

  public static boolean isButtonElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_BUTTON);
  }

  public static boolean isChecked(Element elem) {
    Assert.notNull(elem);
    InputElement input = getInputElement(elem);
    Assert.notNull(input, "input element not found");

    return input.getPropertyBoolean(ATTRIBUTE_CHECKED);
  }

  public static boolean isChecked(String id) {
    Element elem = getElement(id);
    return isChecked(elem);
  }

  public static boolean isChecked(UIObject obj) {
    Assert.notNull(obj);
    return isChecked(obj.getElement());
  }

  public static boolean isEmpty(HasWidgets container) {
    if (container == null) {
      return true;
    }
    return !container.iterator().hasNext();
  }

  public static boolean isEnabled(UIObject obj) {
    Assert.notNull(obj);
    if (obj instanceof HasEnabled) {
      return ((HasEnabled) obj).isEnabled();
    } else {
      return true;
    }
  }

  public static boolean isImageElement(JavaScriptObject obj) {
    if (obj != null && Element.is(obj)) {
      return Element.as(obj).getTagName().equalsIgnoreCase(TAG_IMG);
    } else { 
      return false;
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

  public static boolean isOrHasAncestor(Element el, String id) {
    if (el == null || BeeUtils.isEmpty(id)) {
      return false;
    } else if (BeeUtils.same(id, el.getId())) {
      return true;
    } else {
      return isOrHasAncestor(el.getParentElement(), id);
    }
  }

  public static boolean isSelectElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_SELECT);
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

  public static boolean isTableRowElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_TR);
  }

  public static boolean isTdElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_TD);
  }

  public static boolean isTextAreaElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_TEXT_AREA);
  }

  public static boolean isThElement(Element el) {
    if (el == null) {
      return false;
    }
    return el.getTagName().equalsIgnoreCase(TAG_TH);
  }

  public static boolean isVisible(Element el) {
    Assert.notNull(el);

    for (Element p = el; p != null; p = p.getParentElement()) {
      if (!UIObject.isVisible(p)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isVisible(UIObject obj) {
    Assert.notNull(obj);
    return isVisible(obj.getElement());
  }

  public static void makeFocusable(Element el) {
    Assert.notNull(el);
    if (getTabIndex(el) < 0) {
      el.setTabIndex(0);
    }
  }

  public static void makeFocusable(UIObject obj) {
    Assert.notNull(obj);
    makeFocusable(obj.getElement());
  }

  public static void moveBy(Element el, int dx, int dy) {
    Assert.notNull(el);
    moveBy(el.getStyle(), dx, dy);
  }

  public static void moveBy(NodeList<Element> elements, int dx, int dy) {
    Assert.notNull(elements);
    if (dx == 0 && dy == 0) {
      return;
    }
    for (int i = 0; i < elements.getLength(); i++) {
      moveBy(elements.getItem(i), dx, dy);
    }
  }

  public static void moveBy(String id, int dx, int dy) {
    moveBy(getElement(id), dx, dy);
  }

  public static void moveBy(Style st, int dx, int dy) {
    if (dx != 0) {
      StyleUtils.setLeft(st, StyleUtils.getLeft(st) + dx);
    }
    if (dy != 0) {
      StyleUtils.setTop(st, StyleUtils.getTop(st) + dy);
    }
  }

  public static void moveBy(UIObject obj, int dx, int dy) {
    Assert.notNull(obj);
    moveBy(obj.getElement(), dx, dy);
  }

  public static void moveHorizontalBy(Element el, int dx) {
    Assert.notNull(el);
    moveBy(el.getStyle(), dx, 0);
  }

  public static void moveHorizontalBy(NodeList<Element> elements, int dx) {
    Assert.notNull(elements);
    if (dx == 0) {
      return;
    }
    for (int i = 0; i < elements.getLength(); i++) {
      moveHorizontalBy(elements.getItem(i), dx);
    }
  }

  public static void moveHorizontalBy(String id, int dx) {
    moveHorizontalBy(getElement(id), dx);
  }

  public static void moveHorizontalBy(UIObject obj, int dx) {
    Assert.notNull(obj);
    moveHorizontalBy(obj.getElement(), dx);
  }

  public static void moveVerticalBy(Element el, int dy) {
    Assert.notNull(el);
    moveBy(el.getStyle(), 0, dy);
  }

  public static void moveVerticalBy(NodeList<Element> elements, int dy) {
    Assert.notNull(elements);
    if (dy == 0) {
      return;
    }
    for (int i = 0; i < elements.getLength(); i++) {
      moveVerticalBy(elements.getItem(i), dy);
    }
  }

  public static void moveVerticalBy(String id, int dy) {
    moveVerticalBy(getElement(id), dy);
  }

  public static void moveVerticalBy(UIObject obj, int dy) {
    Assert.notNull(obj);
    moveVerticalBy(obj.getElement(), dy);
  }

  public static void preventSelection(Element elem) {
    Assert.notNull(elem);
    elem.addClassName(StyleUtils.NAME_UNSELECTABLE);
  }

  public static void preventSelection(UIObject obj) {
    Assert.notNull(obj);
    preventSelection(obj.getElement());
  }

  public static void removeAttribute(UIObject obj, String name) {
    Assert.notNull(obj);
    Assert.notEmpty(name);

    obj.getElement().removeAttribute(name);
  }

  public static void removeMax(UIObject obj) {
    removeAttribute(obj, ATTRIBUTE_MAX);
  }

  public static void removeMin(UIObject obj) {
    removeAttribute(obj, ATTRIBUTE_MIN);
  }

  public static void removeStep(UIObject obj) {
    removeAttribute(obj, ATTRIBUTE_STEP);
  }

  public static void resizeBy(Element el, int dw, int dh) {
    Assert.notNull(el);
    resizeBy(el.getStyle(), dw, dh);
  }

  public static void resizeBy(NodeList<Element> elements, int dw, int dh) {
    Assert.notNull(elements);
    if (dw == 0 && dh == 0) {
      return;
    }
    for (int i = 0; i < elements.getLength(); i++) {
      resizeBy(elements.getItem(i), dw, dh);
    }
  }

  public static void resizeBy(String id, int dw, int dh) {
    resizeBy(getElement(id), dw, dh);
  }

  public static void resizeBy(Style st, int dw, int dh) {
    if (dw != 0) {
      StyleUtils.setWidth(st, StyleUtils.getWidth(st) + dw);
    }
    if (dh != 0) {
      StyleUtils.setHeight(st, StyleUtils.getHeight(st) + dh);
    }
  }

  public static void resizeBy(UIObject obj, int dw, int dh) {
    Assert.notNull(obj);
    resizeBy(obj.getElement(), dw, dh);
  }

  public static void resizeHorizontalBy(Element el, int dw) {
    Assert.notNull(el);
    resizeHorizontalBy(el.getStyle(), dw);
  }

  public static void resizeHorizontalBy(NodeList<Element> elements, int dw) {
    Assert.notNull(elements);
    if (dw == 0) {
      return;
    }
    for (int i = 0; i < elements.getLength(); i++) {
      resizeHorizontalBy(elements.getItem(i), dw);
    }
  }

  public static void resizeHorizontalBy(String id, int dw) {
    resizeHorizontalBy(getElement(id), dw);
  }

  public static void resizeHorizontalBy(Style st, int dw) {
    resizeBy(st, dw, 0);
  }

  public static void resizeHorizontalBy(UIObject obj, int dw) {
    Assert.notNull(obj);
    resizeHorizontalBy(obj.getElement(), dw);
  }

  public static void resizeVerticalBy(Element el, int dh) {
    Assert.notNull(el);
    resizeVerticalBy(el.getStyle(), dh);
  }

  public static void resizeVerticalBy(NodeList<Element> elements, int dh) {
    Assert.notNull(elements);
    if (dh == 0) {
      return;
    }
    for (int i = 0; i < elements.getLength(); i++) {
      resizeVerticalBy(elements.getItem(i), dh);
    }
  }

  public static void resizeVerticalBy(String id, int dh) {
    resizeVerticalBy(getElement(id), dh);
  }

  public static void resizeVerticalBy(Style st, int dh) {
    resizeBy(st, 0, dh);
  }

  public static void resizeVerticalBy(UIObject obj, int dh) {
    Assert.notNull(obj);
    resizeVerticalBy(obj.getElement(), dh);
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

  public static void setColSpan(TableCellElement elem, int span) {
    Assert.notNull(elem, "not a table cell element");
    Assert.isPositive(span);

    TableCellElement.as(elem).setColSpan(span);
  }

  public static void setDataColumn(Element elem, int col) {
    Assert.notNull(elem);
    elem.setAttribute(ATTRIBUTE_DATA_COLUMN, Integer.toString(col));
  }

  public static void setDataIndex(Element elem, int idx) {
    Assert.notNull(elem);
    elem.setAttribute(ATTRIBUTE_DATA_INDEX, Integer.toString(idx));
  }

  public static void setDataIndex(Element elem, long idx) {
    Assert.notNull(elem);
    elem.setAttribute(ATTRIBUTE_DATA_INDEX, Long.toString(idx));
  }

  public static void setDraggable(Element elem) {
    Assert.notNull(elem);
    elem.setAttribute(ATTRIBUTE_DRAGGABLE, VALUE_TRUE);
  }

  public static void setDraggable(UIObject obj) {
    Assert.notNull(obj);
    setDraggable(obj.getElement());
  }

  public static void setFocus(Element elem, boolean focus) {
    Assert.notNull(elem);
    if (focus) {
      elem.focus();
    } else {
      elem.blur();
    }
  }

  public static void setFocus(UIObject obj, boolean focus) {
    Assert.notNull(obj);
    setFocus(obj.getElement(), focus);
  }

  public static void setHtml(String id, String html) {
    Element elem = getElement(id);
    elem.setInnerHTML(html);
  }

  public static void setId(UIObject obj, String id) {
    Assert.notNull(obj);
    Assert.notEmpty(id);

    String s = id.trim();
    obj.getElement().setId(s);
  }

  public static boolean setInputType(Element elem, String type) {
    assertInputElement(elem);
    Assert.notEmpty(type);
    
    if (Features.supportsInputType(type)) {
      setType(InputElement.as(elem), type);
      return true;
    } else {
      return false;
    }
  }

  public static boolean setInputType(UIObject obj, String type) {
    Assert.notNull(obj);
    return setInputType(obj.getElement(), type);
  }

  public static void setMax(UIObject obj, int max) {
    setAttribute(obj, ATTRIBUTE_MAX, max);
  }

  public static void setMin(UIObject obj, int min) {
    setAttribute(obj, ATTRIBUTE_MIN, min);
  }

  public static boolean setPlaceholder(Element elem, String value) {
    if ((isInputElement(elem) || isTextAreaElement(elem))
        && Features.supportsAttributePlaceholder()) {
      elem.setAttribute(ATTRIBUTE_PLACEHOLDER, LocaleUtils.maybeLocalize(value));
      return true;
    } else {
      return false;
    }
  }

  public static boolean setPlaceholder(UIObject obj, String value) {
    Assert.notNull(obj);
    return setPlaceholder(obj.getElement(), value);
  }

  public static void setRowSpan(Element elem, int span) {
    Assert.isTrue(isTableCellElement(elem), "not a table cell element");
    Assert.isPositive(span);

    TableCellElement.as(elem).setRowSpan(span);
  }

  public static boolean setSearch(Element elem) {
    return setInputType(elem, TYPE_SEARCH);
  }

  public static boolean setSearch(UIObject obj) {
    Assert.notNull(obj);
    return setSearch(obj.getElement());
  }

  public static void setSelected(Element elem, boolean selected) {
    Assert.notNull(elem);

    OptionElement.as(elem).setSelected(selected);
    OptionElement.as(elem).setDefaultSelected(selected);
  }

  public static void setStep(UIObject obj, int step) {
    setAttribute(obj, ATTRIBUTE_STEP, step);
  }

  public static void setTabIndex(Widget w, int idx) {
    Assert.notNull(w);
    w.getElement().setTabIndex(idx);
  }

  public static void setText(String id, String text) {
    Element elem = getElement(id);
    elem.setInnerText(text);
  }

  public static String transformClass(Object obj) {
    if (obj == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return NameUtils.getName(obj);
    }
  }

  public static String transformElement(Element el) {
    if (el == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.joinWords(el.getTagName(), el.getId(), el.getClassName());
    }
  }

  public static String transformUIObject(UIObject obj) {
    if (obj == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.joinWords(NameUtils.getName(obj), obj.getElement().getId(),
          obj.getStyleName());
    }
  }

  public static String transformWidget(Widget w) {
    if (w == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.joinWords(NameUtils.getName(w), w.getElement().getId(), w.getStyleName());
    }
  }

  public static com.google.gwt.user.client.Element upcast(Element elem) {
    return (com.google.gwt.user.client.Element) elem;
  }

  private static void assertInputElement(Element elem) {
    Assert.isTrue(isInputElement(elem), "not an input element");
  }

  private static void calculateCheckBoxSize() {
    Element elem = DOM.createInputCheck();

    Element body = Document.get().getBody();
    body.appendChild(elem);

    checkBoxOffsetWidth = elem.getOffsetWidth();
    checkBoxOffsetHeight = elem.getOffsetHeight();

    checkBoxClientWidth = elem.getClientWidth();
    checkBoxClientHeight = elem.getClientHeight();

    body.removeChild(elem);
  }

  private static void calculateScrollBarSize() {
    Element elem = DOM.createDiv();

    elem.getStyle().setPosition(Position.ABSOLUTE);

    StyleUtils.setLeft(elem, -1000);
    StyleUtils.setTop(elem, -1000);
    StyleUtils.setWidth(elem, 100);
    StyleUtils.setHeight(elem, 100);

    elem.getStyle().setOverflow(Overflow.SCROLL);

    Element body = Document.get().getBody();
    body.appendChild(elem);

    int w1 = elem.getOffsetWidth();
    int h1 = elem.getOffsetHeight();

    int w2 = elem.getClientWidth();
    int h2 = elem.getClientHeight();

    body.removeChild(elem);

    scrollBarWidth = w1 - w2;
    scrollBarHeight = h1 - h2;
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

  private static native void setType(InputElement el, String tp) /*-{
    el.type = tp;
  }-*/;

  private static String transformNode(Node nd) {
    if (nd == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.joinWords(nd.getNodeName(), nd.getNodeValue());
    }
  }

  private DomUtils() {
  }
}
