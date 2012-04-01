package com.butent.bee.client.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CDATASection;
import com.google.gwt.xml.client.Comment;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.ProcessingInstruction;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.ConditionalStyleDeclaration;
import com.butent.bee.shared.ui.RendererDescription;
import com.butent.bee.shared.ui.RendererType;
import com.butent.bee.shared.ui.StyleDeclaration;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

/**
 * Contains utility functions for working with xml on the client side.
 */

public class XmlUtils {

  private static final Map<Short, String> NODE_TYPES = Maps.newHashMap();

  static {
    NODE_TYPES.put(Node.ELEMENT_NODE, "Element");
    NODE_TYPES.put(Node.ATTRIBUTE_NODE, "Attribute");
    NODE_TYPES.put(Node.TEXT_NODE, "Text");
    NODE_TYPES.put(Node.CDATA_SECTION_NODE, "CDATA Section");
    NODE_TYPES.put(Node.ENTITY_REFERENCE_NODE, "Entity Reference");
    NODE_TYPES.put(Node.ENTITY_NODE, "Entity");
    NODE_TYPES.put(Node.PROCESSING_INSTRUCTION_NODE, "Processing Instruction");
    NODE_TYPES.put(Node.COMMENT_NODE, "Comment");
    NODE_TYPES.put(Node.DOCUMENT_NODE, "Document");
    NODE_TYPES.put(Node.DOCUMENT_TYPE_NODE, "Document Type");
    NODE_TYPES.put(Node.DOCUMENT_FRAGMENT_NODE, "Document Fragment");
    NODE_TYPES.put(Node.NOTATION_NODE, "Notation");
  }

  public static String createString(String rootName, Object... nodes) {
    Assert.notEmpty(rootName);
    Assert.notNull(nodes);
    Assert.parameterCount(nodes.length + 1, 3);

    return transformDocument(createDoc(rootName, nodes));
  }

  public static String fromVars(String rootName, String... names) {
    Assert.notEmpty(rootName);
    Assert.notNull(names);
    Assert.parameterCount(names.length + 1, 2);

    Object[] nodes = new Object[names.length * 2];
    for (int i = 0; i < names.length; i++) {
      nodes[i * 2] = names[i];
      nodes[i * 2 + 1] = BeeUtils.trim(Global.getVarValue(names[i]));
    }

    return transformDocument(createDoc(rootName, nodes));
  }

  public static Boolean getAttributeBoolean(Element element, String name) {
    Assert.notNull(element);
    Assert.notEmpty(name);
    return BeeUtils.toBooleanOrNull(element.getAttribute(name));
  }

  public static Double getAttributeDouble(Element element, String name) {
    Assert.notNull(element);
    Assert.notEmpty(name);
    return BeeUtils.toDoubleOrNull(element.getAttribute(name));
  }
  
  public static Integer getAttributeInteger(Element element, String name) {
    Assert.notNull(element);
    Assert.notEmpty(name);
    return BeeUtils.toIntOrNull(element.getAttribute(name));
  }

  public static Map<String, String> getAttributes(Element element) {
    Assert.notNull(element);
    Map<String, String> result = Maps.newHashMap();

    NamedNodeMap attributes = element.getAttributes();
    if (attributes == null || attributes.getLength() <= 0) {
      return result;
    }

    Attr attr;
    for (int i = 0; i < attributes.getLength(); i++) {
      attr = (Attr) attributes.item(i);
      result.put(attr.getName(), attr.getValue());
    }
    return result;
  }

  public static ScrollBars getAttributeScrollBars(Element element, String name, ScrollBars def) {
    Assert.notNull(element);
    Assert.notEmpty(name);
    return StyleUtils.parseScrollBars(element.getAttribute(name), def);
  }
  
  public static Unit getAttributeUnit(Element element, String name) {
    return getAttributeUnit(element, name, null);
  }
  
  public static Unit getAttributeUnit(Element element, String name, Unit defUnit) {
    Assert.notNull(element);
    Assert.notEmpty(name);
    return StyleUtils.parseUnit(element.getAttribute(name), defUnit);
  }

  public static List<Property> getAttrInfo(Attr attr) {
    Assert.notNull(attr);
    return PropertyUtils.createProperties("Name", attr.getName(), "Value", attr.getValue(),
        "Specified", attr.getSpecified());
  }

  public static Calculation getCalculation(Element element) {
    Assert.notNull(element);
    
    String expr = null;
    String func = null;
    String lamb = null;
    
    for (Element child : getChildrenElements(element)) {
      String tag = child.getTagName();
      String text = getText(child);
      if (BeeUtils.isEmpty(text)) {
        continue;
      }

      if (BeeUtils.same(tag, Calculation.TAG_EXPRESSION)) {
        expr = text;
      } else if (BeeUtils.same(tag, Calculation.TAG_FUNCTION)) {
        func = text;
      } else if (BeeUtils.same(tag, Calculation.TAG_LAMBDA)) {
        lamb = text;
      }
    }
    return new Calculation(expr, func, lamb);
  }

  public static Calculation getCalculation(Element parent, String tagName) {
    Assert.notNull(parent);
    Assert.notEmpty(tagName);

    Element element = getFirstChildElement(parent, tagName);
    if (element == null) {
      return null;
    }
    return getCalculation(element);
  }

  public static List<Property> getCDATAInfo(CDATASection cdata) {
    Assert.notNull(cdata);
    return PropertyUtils.createProperties("Length", cdata.getLength(), "Data", cdata.getData());
  }

  public static List<Element> getChildrenElements(Element parent) {
    Assert.notNull(parent);
    List<Element> result = Lists.newArrayList();

    NodeList nodes = parent.getChildNodes();
    if (nodes == null || nodes.getLength() <= 0) {
      return result;
    }

    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (isElement(node)) {
        result.add((Element) node);
      }
    }
    return result;
  }

  public static List<String> getChildrenText(Element parent, String tagName) {
    Assert.notNull(parent);
    Assert.notEmpty(tagName);
    List<String> result = Lists.newArrayList();

    NodeList children = parent.getElementsByTagName(tagName.trim());
    if (children == null || children.getLength() <= 0) {
      return null;
    }

    for (int i = 0; i < children.getLength(); i++) {
      String text = getText((Element) children.item(i));
      if (!BeeUtils.isEmpty(text)) {
        result.add(text);
      }
    }
    return result;
  }
  
  public static List<Property> getCommentInfo(Comment comm) {
    Assert.notNull(comm);
    return PropertyUtils.createProperties("Length", comm.getLength(), "Data", comm.getData());
  }

  public static ConditionalStyleDeclaration getConditionalStyle(Element element) {
    Assert.notNull(element);

    StyleDeclaration style = getStyle(element, ConditionalStyleDeclaration.TAG_STYLE);
    Calculation condition = getCalculation(element, ConditionalStyleDeclaration.TAG_CONDITION);

    if (style == null && condition == null) {
      return null;
    }
    return new ConditionalStyleDeclaration(style, condition);
  }

  public static ConditionalStyleDeclaration getConditionalStyle(Element parent, String tagName) {
    Assert.notNull(parent);
    Assert.notEmpty(tagName);

    Element element = getFirstChildElement(parent, tagName);
    if (element == null) {
      return null;
    }
    return getConditionalStyle(element);
  }

  public static Dimensions getDimensions(Element element) {
    Assert.notNull(element);
    return new Dimensions(getAttributeDouble(element, HasDimensions.ATTR_WIDTH),
        getAttributeUnit(element, HasDimensions.ATTR_WIDTH_UNIT),
        getAttributeDouble(element, HasDimensions.ATTR_HEIGHT),
        getAttributeUnit(element, HasDimensions.ATTR_HEIGHT_UNIT));
  }
  
  public static List<Property> getDocumentInfo(Document doc) {
    Assert.notNull(doc);
    List<Property> lst = Lists.newArrayList();

    Element el = doc.getDocumentElement();
    if (el != null) {
      PropertyUtils.addProperty(lst, "Document Element", el.getTagName());
    }
    return lst;
  }

  public static List<Property> getElementInfo(Element el) {
    Assert.notNull(el);
    List<Property> lst = PropertyUtils.createProperties("Tag Name", el.getTagName());
    
    Map<String, String> attributes = getAttributes(el);
    int c = (attributes == null) ? 0 : attributes.size();
    if (c > 0) {
      PropertyUtils.addProperty(lst, "Attributes", BeeUtils.bracket(c));
      int i = 0;
      for (Map.Entry<String, String> attr : attributes.entrySet()) {
        PropertyUtils.addProperty(lst,
            BeeUtils.concat(1, "Attribute", BeeUtils.progress(++i, c), attr.getKey()), 
            attr.getValue());
      }
    }
    return lst;
  }

  public static Element getFirstChildElement(Element parent, String tagName) {
    Assert.notNull(parent);
    Assert.notEmpty(tagName);

    NodeList children = parent.getElementsByTagName(tagName.trim());
    if (children == null || children.getLength() <= 0) {
      return null;
    }
    return (Element) children.item(0);
  }

  public static List<ExtendedProperty> getInfo(String xml) {
    Document doc = parse(xml);
    if (doc == null) {
      return null;
    }
    return getTreeInfo(doc, BeeConst.STRING_EMPTY);
  }

  public static List<Property> getNodeInfo(Node nd) {
    Assert.notNull(nd);
    return PropertyUtils.createProperties("Node Type", nd.getNodeType(),
        "Node Name", nd.getNodeName(),
        "Node Value", nd.getNodeValue(),
        "Namespace URI", nd.getNamespaceURI(),
        "Prefix", nd.getPrefix());
  }

  public static List<Property> getProcessingInstructionInfo(ProcessingInstruction pin) {
    Assert.notNull(pin);
    return PropertyUtils.createProperties("Data", pin.getData(), "Target", pin.getTarget());
  }

  public static RendererDescription getRendererDescription(Element element) {
    Assert.notNull(element);
    String typeCode = element.getAttribute(RendererDescription.ATTR_TYPE);
    if (BeeUtils.isEmpty(typeCode)) {
      return null;
    }

    RendererType type = RendererType.getByTypeCode(typeCode);
    if (type == null) {
      return null;
    }

    RendererDescription rendererDescription = new RendererDescription(type);
    rendererDescription.setAttributes(XmlUtils.getAttributes(element));

    List<String> items = getChildrenText(element, HasItems.TAG_ITEM);
    if (!items.isEmpty()) {
      rendererDescription.setItems(items);
    }
    
    return rendererDescription;
  }
  
  public static StyleDeclaration getStyle(Element element) {
    Assert.notNull(element);

    String className = getText(getFirstChildElement(element, StyleDeclaration.TAG_CLASS));
    String inline = getText(getFirstChildElement(element, StyleDeclaration.TAG_INLINE));
    String font = getText(getFirstChildElement(element, StyleDeclaration.TAG_FONT));

    if (BeeUtils.allEmpty(className, inline, font)) {
      return null;
    }
    return new StyleDeclaration(className, inline, font);
  }

  public static StyleDeclaration getStyle(Element parent, String tagName) {
    Assert.notNull(parent);
    Assert.notEmpty(tagName);

    Element element = getFirstChildElement(parent, tagName);
    if (element == null) {
      return null;
    }
    return getStyle(element);
  }

  public static String getText(Element element) {
    if (element == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();

    NodeList children = element.getChildNodes();
    if (children != null && children.getLength() > 0) {
      for (int i = 0; i < children.getLength(); i++) {
        Node node = children.item(i);
        short type = node.getNodeType();
        if (type == Node.CDATA_SECTION_NODE || type == Node.TEXT_NODE) {
          sb.append(BeeUtils.trim(node.getNodeValue()));
        }
      }
    }
    return sb.toString();
  }

  public static List<Property> getTextInfo(Text txt) {
    Assert.notNull(txt);
    return PropertyUtils.createProperties(BeeUtils.concat(1, "Length", txt.getLength()),
        txt.getData());
  }

  public static List<ExtendedProperty> getTreeInfo(Node nd, String root) {
    Assert.notNull(nd);
    List<ExtendedProperty> lst = Lists.newArrayList();

    List<Property> tpInf = null;
    short tp = nd.getNodeType();

    switch (tp) {
      case Node.ELEMENT_NODE:
        tpInf = getElementInfo((Element) nd);
        break;
      case Node.ATTRIBUTE_NODE:
        tpInf = getAttrInfo((Attr) nd);
        break;
      case Node.TEXT_NODE:
        if (!BeeUtils.isEmpty(((Text) nd).getData())) {
          tpInf = getTextInfo((Text) nd);
        }
        break;
      case Node.CDATA_SECTION_NODE:
        tpInf = getCDATAInfo((CDATASection) nd);
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        tpInf = getProcessingInstructionInfo((ProcessingInstruction) nd);
        break;
      case Node.COMMENT_NODE:
        tpInf = getCommentInfo((Comment) nd);
        break;
      case Node.DOCUMENT_NODE:
        tpInf = getDocumentInfo((Document) nd);
        break;
      default:
        tpInf = PropertyUtils.createProperties(nd.toString(),
            BeeUtils.concat(1, "unknown node type", tp));
    }

    if (!BeeUtils.isEmpty(tpInf)) {
      PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, root, getNodeName(tp)), tpInf);
    }

    if (nd.hasChildNodes()) {
      NodeList children = nd.getChildNodes();
      int c = (children == null) ? 0 : children.getLength();
      PropertyUtils.addExtended(lst, BeeUtils.ifString(root, getNodeName(tp)), "Children",
          BeeUtils.bracket(c));
      for (int i = 0; i < c; i++) {
        lst.addAll(getTreeInfo(children.item(i), BeeUtils.concat(".", root, i)));
      }
    }
    return lst;
  }

  public static Document parse(String xml) {
    Assert.notEmpty(xml);
    Document doc;

    try {
      doc = XMLParser.parse(xml);
    } catch (DOMParseException ex) {
      BeeKeeper.getLog().severe(ex.getMessage());
      doc = null;
    }
    return doc;
  }

  private static void appendElementWithText(Document doc, Element root, String tag, String txt) {
    Element el = doc.createElement(tag);
    Text x = doc.createTextNode(txt);

    el.appendChild(x);
    root.appendChild(el);
  }

  private static Document createDoc(String rootName, Object... nodes) {
    Document doc = XMLParser.createDocument();
    Element root = doc.createElement(rootName);

    String tag, txt;

    for (int i = 0; i < nodes.length - 1; i += 2) {
      if (!(nodes[i] instanceof String)) {
        continue;
      }
      tag = ((String) nodes[i]).trim();
      if (tag.length() <= 0) {
        continue;
      }

      txt = transformText(nodes[i + 1]);
      if (BeeUtils.isEmpty(txt)) {
        continue;
      }
      appendElementWithText(doc, root, tag, txt);
    }

    doc.appendChild(root);
    return doc;
  }

  private static String getNodeName(short type) {
    return NODE_TYPES.get(type);
  }

  private static boolean isElement(Node nd) {
    return nd.getNodeType() == Node.ELEMENT_NODE;
  }

  private static String transformDocument(Document doc) {
    return transformDocument(doc, BeeConst.XML_DEFAULT_PROLOG);
  }
  
  private static String transformDocument(Document doc, String prolog) {
    String xml = doc.toString();

    if (BeeUtils.isEmpty(prolog) || xml.matches("^<[?][xX][mM][lL].*")) {
      return xml;
    } else {
      return prolog + xml;
    }
  }

  private static String transformText(Object obj) {
    if (obj == null) {
      return BeeConst.STRING_EMPTY;
    } else if (obj instanceof String) {
      return (String) obj;
    } else {
      return BeeUtils.transform(obj);
    }
  }

  private XmlUtils() {
  }
}
