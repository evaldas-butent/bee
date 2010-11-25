package com.butent.bee.egg.server.utils;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XmlUtils {
  public static String defaultXmlExtension = "xml";
  public static String defaultXslExtension = "xsl";

  private static final String ALL_TAGS = "*";
  private static Logger logger = Logger.getLogger(XmlUtils.class.getName());

  private static DocumentBuilderFactory domFactory;
  private static DocumentBuilder domBuilder;

  private static TransformerFactory xsltFactory;

  private static Map<Short, String> nodeTypes = new HashMap<Short, String>();

  static {
    nodeTypes.put(Node.ELEMENT_NODE, "Element");
    nodeTypes.put(Node.ATTRIBUTE_NODE, "Attribute");
    nodeTypes.put(Node.TEXT_NODE, "Text");
    nodeTypes.put(Node.CDATA_SECTION_NODE, "CDATA Section");
    nodeTypes.put(Node.ENTITY_REFERENCE_NODE, "Entity Reference");
    nodeTypes.put(Node.ENTITY_NODE, "Entity");
    nodeTypes.put(Node.PROCESSING_INSTRUCTION_NODE, "Processing Instruction");
    nodeTypes.put(Node.COMMENT_NODE, "Comment");
    nodeTypes.put(Node.DOCUMENT_NODE, "Document");
    nodeTypes.put(Node.DOCUMENT_TYPE_NODE, "Document Type");
    nodeTypes.put(Node.DOCUMENT_FRAGMENT_NODE, "Document Fragment");
    nodeTypes.put(Node.NOTATION_NODE, "Notation");

    DocumentBuilderFactory dbf = null;
    DocumentBuilder bld;

    try {
      dbf = DocumentBuilderFactory.newInstance();
      bld = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException ex) {
      LogUtils.error(logger, ex);
      bld = null;
    }

    domFactory = dbf;
    domBuilder = bld;

    TransformerFactory tf = null;

    try {
      tf = TransformerFactory.newInstance();
    } catch (TransformerFactoryConfigurationError ex) {
      LogUtils.error(logger, ex);
    }
    xsltFactory = tf;
  }

  public static Document fromFileName(String fileName) {
    File fl = new File(fileName);
    if (!FileUtils.isInputFile(fl)) {
      LogUtils.severe(logger, fileName, "not an input file");
      return null;
    }

    Document doc = createDocument(fl);
    return doc;
  }

  public static StringProp[][] getAttributesFromFile(String src, String tag) {
    return getAttributesFromFile(src, null, tag);
  }

  public static StringProp[][] getAttributesFromFile(String src, String xsl,
      String tag) {
    Assert.notEmpty(src);
    Assert.notEmpty(tag);

    Document doc = null;
    if (BeeUtils.isEmpty(xsl)) {
      doc = fromFileName(src);
    } else {
      doc = xsltToDom(src, xsl);
    }

    if (doc == null) {
      LogUtils.warning(logger, src, xsl, "cannot parse xml");
      return null;
    }

    NodeList lst = doc.getElementsByTagName(tag);
    int r = (lst == null) ? 0 : lst.getLength();
    if (r <= 0) {
      LogUtils.warning(logger, "tag", tag, "not found in", src, xsl);
      return null;
    }

    StringProp[][] arr = new StringProp[r][];

    NamedNodeMap attributes;
    Attr attr;
    int c;

    for (int i = 0; i < r; i++) {
      attributes = lst.item(i).getAttributes();
      c = (attributes == null) ? 0 : attributes.getLength();

      if (c <= 0) {
        arr[i] = null;
      } else {
        arr[i] = new StringProp[c];
        for (int j = 0; j < c; j++) {
          attr = (Attr) attributes.item(j);
          arr[i][j] = new StringProp(attr.getName(), attr.getValue());
        }
      }
    }

    return arr;
  }

  public static List<StringProp> getAttrInfo(Attr attr) {
    Assert.notNull(attr);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Name", attr.getName(), "Value", attr.getValue(),
        "Owner Element", transformElement(attr.getOwnerElement()),
        "Schema Type Info", transformTypeInfo(attr.getSchemaTypeInfo()),
        "Specified", attr.getSpecified(), "Is Id", attr.isId(), "To String",
        attr.toString());

    return lst;
  }

  public static List<StringProp> getCDATAInfo(CDATASection cdata) {
    Assert.notNull(cdata);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Length", cdata.getLength(), "Data",
        cdata.getData(), "Is Element Content Whitespace",
        cdata.isElementContentWhitespace());

    return lst;
  }

  public static List<StringProp> getCommentInfo(Comment comm) {
    Assert.notNull(comm);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Length", comm.getLength(), "Data",
        comm.getData(), "To String", comm.toString());

    return lst;
  }

  public static List<StringProp> getDocumentFragmentInfo(DocumentFragment df) {
    Assert.notNull(df);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "To String", df.toString());

    return lst;
  }

  public static List<StringProp> getDocumentInfo(Document doc) {
    Assert.notNull(doc);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Document URI", doc.getDocumentURI(),
        "Implementation", transformDOMImplementation(doc.getImplementation()),
        "Input Encoding", doc.getInputEncoding(), "Strict Error Checking",
        doc.getStrictErrorChecking(), "Xml Encoding", doc.getXmlEncoding(),
        "Xml Standalone", doc.getXmlStandalone(), "Xml Version",
        doc.getXmlVersion(), "To String", doc.toString());

    PropUtils.appendStringProp(lst, "Dom Config",
        getDOMConfigurationInfo(doc.getDomConfig()));

    DocumentType dtp = doc.getDoctype();
    if (dtp != null) {
      PropUtils.appendStringProp(lst, "Doctype", getDocumentTypeInfo(dtp));
    }

    Element el = doc.getDocumentElement();
    if (el != null) {
      PropUtils.appendStringProp(lst, "Document Element", getElementInfo(el));
    }

    return lst;
  }

  public static List<StringProp> getDocumentTypeInfo(DocumentType dtp) {
    Assert.notNull(dtp);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Name", dtp.getName(), "Internal Subset",
        dtp.getInternalSubset(), "Public Id", dtp.getPublicId(), "System Id",
        dtp.getSystemId(), "To String", dtp.toString());

    lst.addAll(getNamedNodeMapInfo(dtp.getEntities(), "Entities"));
    lst.addAll(getNamedNodeMapInfo(dtp.getNotations(), "Notations"));

    return lst;
  }

  public static List<StringProp> getDomBuilderInfo() {
    List<StringProp> lst = new ArrayList<StringProp>();

    if (domBuilder == null) {
      PropUtils.addString(lst, "Error creating builder",
          DocumentBuilder.class.getName());
    } else {
      PropUtils.addString(lst, "Schema", domBuilder.getSchema(),
          "Is Namespace Aware", domBuilder.isNamespaceAware(), "Is Validating",
          domBuilder.isValidating(), "Is XInclude Aware",
          domBuilder.isXIncludeAware(), "To String", domBuilder.toString());
    }

    return lst;
  }

  public static List<StringProp> getDomFactoryInfo() {
    List<StringProp> lst = new ArrayList<StringProp>();

    if (domFactory == null) {
      PropUtils.addString(lst, "Error instantiating factory",
          DocumentBuilderFactory.class.getName());
    } else {
      PropUtils.addString(lst, "Is Coalescing", domFactory.isCoalescing(),
          "Is Expand Entity References", domFactory.isExpandEntityReferences(),
          "Is Ignoring Comments", domFactory.isIgnoringComments(),
          "Is Ignoring Element Content Whitespace",
          domFactory.isIgnoringElementContentWhitespace(),
          "Is Namespace Aware", domFactory.isNamespaceAware(), "Is Validating",
          domFactory.isValidating(), "Is XInclude Aware",
          domFactory.isXIncludeAware(), "To String", domFactory.toString());
    }

    return lst;
  }

  public static List<StringProp> getElementInfo(Element el) {
    Assert.notNull(el);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Tag Name", el.getTagName(), "Schema Type Info",
        transformTypeInfo(el.getSchemaTypeInfo()), "To String", el.toString());

    return lst;
  }

  public static Map<String, String> getElements(String xml, String... ignore) {
    Assert.notEmpty(xml);
    Map<String, String> ret = new HashMap<String, String>();

    Document doc = fromString(xml);
    if (doc == null) {
      return ret;
    }

    NodeList nodes = doc.getElementsByTagName(ALL_TAGS);
    if (nodes == null) {
      return ret;
    }

    Node nd;
    Element el;
    String tg, txt;

    int n = ignore.length;

    for (int i = 0; i < nodes.getLength(); i++) {
      nd = nodes.item(i);
      if (!isElement(nd)) {
        continue;
      }
      el = (Element) nd;

      tg = el.getTagName();
      if (n > 0 && BeeUtils.inListSame(tg, ignore)) {
        continue;
      }

      txt = el.getTextContent();
      if (!BeeUtils.isEmpty(tg) && !BeeUtils.isEmpty(txt)) {
        ret.put(tg, txt);
      }
    }

    return ret;
  }

  public static List<StringProp> getEntityInfo(Entity ent) {
    Assert.notNull(ent);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Input Encoding", ent.getInputEncoding(),
        "Notation Name", ent.getNotationName(), "Public Id", ent.getPublicId(),
        "System Id", ent.getSystemId(), "Xml Encoding", ent.getXmlEncoding(),
        "XmlVersion", ent.getXmlVersion(), "To String", ent.toString());

    return lst;
  }

  public static List<StringProp> getEntityReferenceInfo(EntityReference er) {
    Assert.notNull(er);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "To String", er.toString());

    return lst;
  }

  public static List<SubProp> getFileInfo(String fileName) {
    Assert.notEmpty(fileName);

    Document doc = fromFileName(fileName);
    if (doc == null) {
      LogUtils.warning(logger, fileName, "cannot parse xml");
      return PropUtils.EMPTY_PROP_SUB_LIST;
    }

    return getTreeInfo(doc, "0");
  }

  public static List<StringProp> getNodeInfo(Node nd) {
    Assert.notNull(nd);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Node Type", nd.getNodeType(), "Node Name",
        nd.getNodeName(), "Local Name", nd.getLocalName(), "Node Value",
        nd.getNodeValue(), "Text Content", nd.getTextContent(), "Parent Node",
        nd.getParentNode(), "First Child", nd.getFirstChild(), "Last Child",
        nd.getLastChild(), "Previous Sibling", nd.getPreviousSibling(),
        "Next Sibling", nd.getNextSibling(), "Base URI", nd.getBaseURI(),
        "Namespace URI", nd.getNamespaceURI(), "Prefix", nd.getPrefix());

    if (nd.hasAttributes()) {
      NamedNodeMap attributes = nd.getAttributes();
      int c = (attributes == null) ? 0 : attributes.getLength();
      PropUtils.addString(lst, "Attributes", BeeUtils.bracket(c));

      for (int i = 0; i < c; i++) {
        Node attr = attributes.item(i);
        if (!isAttribute(attr)) {
          continue;
        }

        PropUtils.addString(lst, "Attribute", BeeUtils.progress(i + 1, c));
        lst.addAll(getAttrInfo((Attr) attr));
      }
    }

    return lst;
  }

  public static List<StringProp> getNotationInfo(Notation nt) {
    Assert.notNull(nt);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Public Id", nt.getPublicId(), "System Id",
        nt.getSystemId(), "To String", nt.toString());

    return lst;
  }

  public static List<StringProp> getOutputKeysInfo() {
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "CDATA SECTION ELEMENTS",
        OutputKeys.CDATA_SECTION_ELEMENTS, "DOCTYPE PUBLIC",
        OutputKeys.DOCTYPE_PUBLIC, "DOCTYPE SYSTEM", OutputKeys.DOCTYPE_SYSTEM,
        "ENCODING", OutputKeys.ENCODING, "INDENT", OutputKeys.INDENT,
        "MEDIA TYPE", OutputKeys.MEDIA_TYPE, "METHOD", OutputKeys.METHOD,
        "OMIT XML DECLARATION", OutputKeys.OMIT_XML_DECLARATION, "STANDALONE",
        OutputKeys.STANDALONE, "VERSION", OutputKeys.VERSION);

    return lst;
  }

  public static List<StringProp> getProcessingInstructionInfo(
      ProcessingInstruction pin) {
    Assert.notNull(pin);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Data", pin.getData(), "Target", pin.getTarget(),
        "To String", pin.toString());

    return lst;
  }

  public static List<SubProp> getRootInfo(Document doc) {
    Assert.notNull(doc);
    List<SubProp> lst = new ArrayList<SubProp>();

    String root = getNodeName(Node.DOCUMENT_NODE);

    PropUtils.addRoot(lst, root, "Document URI", doc.getDocumentURI(),
        "Implementation", transformDOMImplementation(doc.getImplementation()),
        "Input Encoding", doc.getInputEncoding(), "Strict Error Checking",
        doc.getStrictErrorChecking(), "Xml Encoding", doc.getXmlEncoding(),
        "Xml Standalone", doc.getXmlStandalone(), "Xml Version",
        doc.getXmlVersion(), "To String", doc.toString());

    PropUtils.appendString(lst, root + " Dom Config",
        getDOMConfigurationInfo(doc.getDomConfig()));

    DocumentType dtp = doc.getDoctype();
    if (dtp != null) {
      PropUtils.appendString(lst, root + " Doctype", getDocumentTypeInfo(dtp));
    }

    PropUtils.appendString(lst, "Document Node", getNodeInfo(doc));

    NodeList nodes = doc.getElementsByTagName(ALL_TAGS);
    int c = (nodes == null ? 0 : nodes.getLength());
    PropUtils.addSub(lst, root, "ElementsByTagName " + ALL_TAGS,
        BeeUtils.bracket(c));

    if (c > 0) {
      for (int i = 0; i < c; i++) {
        PropUtils.addSub(lst, root, "Node " + BeeUtils.progress(i + 1, c),
            transformNode(nodes.item(i)));
      }
    }

    Element el = doc.getDocumentElement();
    PropUtils.appendString(lst, "Document Element", getElementInfo(el));
    PropUtils.appendString(lst, "Document Element Node", getNodeInfo(el));

    return lst;
  }

  public static String getText(String xml, String tag) {
    Assert.notEmpty(xml);
    Assert.notEmpty(tag);

    Document doc = fromString(xml);
    if (doc == null) {
      return null;
    }

    NodeList nodes = doc.getElementsByTagName(tag.trim());
    if (nodes == null) {
      return null;
    }

    Node nd;
    String txt;

    for (int i = 0; i < nodes.getLength(); i++) {
      nd = nodes.item(i);
      if (!isElement(nd)) {
        continue;
      }

      txt = ((Element) nd).getTextContent();
      if (!BeeUtils.isEmpty(txt)) {
        return txt;
      }
    }

    return BeeConst.STRING_EMPTY;
  }

  public static List<StringProp> getTextInfo(Text txt) {
    Assert.notNull(txt);
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Length", txt.getLength(), "Data", txt.getData(),
        "Whole Text", txt.getWholeText(), "Is Element Content Whitespace",
        txt.isElementContentWhitespace(), "To String", txt.toString());

    return lst;
  }

  public static List<SubProp> getTreeInfo(Node nd, String root) {
    Assert.notNull(nd);
    Assert.notEmpty(root);
    List<SubProp> lst = new ArrayList<SubProp>();

    List<StringProp> tpInf = null;
    short tp = nd.getNodeType();

    switch (tp) {
      case Node.ELEMENT_NODE:
        tpInf = getElementInfo((Element) nd);
        break;
      case Node.ATTRIBUTE_NODE:
        tpInf = getAttrInfo((Attr) nd);
        break;
      case Node.TEXT_NODE:
        tpInf = getTextInfo((Text) nd);
        break;
      case Node.CDATA_SECTION_NODE:
        tpInf = getCDATAInfo((CDATASection) nd);
        break;
      case Node.ENTITY_REFERENCE_NODE:
        tpInf = getEntityReferenceInfo((EntityReference) nd);
        break;
      case Node.ENTITY_NODE:
        tpInf = getEntityInfo((Entity) nd);
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
      case Node.DOCUMENT_TYPE_NODE:
        tpInf = getDocumentTypeInfo((DocumentType) nd);
        break;
      case Node.DOCUMENT_FRAGMENT_NODE:
        tpInf = getDocumentFragmentInfo((DocumentFragment) nd);
        break;
      case Node.NOTATION_NODE:
        tpInf = getNotationInfo((Notation) nd);
        break;
      default:
        LogUtils.warning(logger, "unknown node type", tp);
        tpInf = PropUtils.createStringProp(nd.toString(),
            BeeUtils.concat(1, "unknown node type", tp));
    }

    if (!BeeUtils.isEmpty(tpInf)) {
      PropUtils.appendString(lst, BeeUtils.concat(1, root, getNodeName(tp)),
          tpInf);
    }

    PropUtils.appendString(lst, BeeUtils.concat(1, root, "Node"),
        getNodeInfo(nd));

    if (nd.hasChildNodes()) {
      NodeList children = nd.getChildNodes();
      int c = (children == null) ? 0 : children.getLength();
      PropUtils.addSub(lst, root, "Children", BeeUtils.bracket(c));

      for (int i = 0; i < c; i++) {
        lst.addAll(getTreeInfo(children.item(i),
            BeeUtils.concat(".", root, i + 1)));
      }
    }

    return lst;
  }

  public static List<StringProp> getXsltFactoryInfo() {
    List<StringProp> lst = new ArrayList<StringProp>();

    if (xsltFactory == null) {
      PropUtils.addString(lst, "Error instantiating factory",
          TransformerFactory.class.getName());
    } else {
      PropUtils.addString(lst, "Class", BeeUtils.transformClass(xsltFactory),
          "Error Listener",
          BeeUtils.transformClass(xsltFactory.getErrorListener()),
          "URI Resolver", BeeUtils.transformClass(xsltFactory.getURIResolver()));
    }

    return lst;
  }

  public static boolean saveNode(Node node, String dst) {
    boolean ok = true;

    try {
      Transformer transformer = xsltFactory.newTransformer();

      DOMSource source = new DOMSource(node);
      StreamResult result = new StreamResult(dst);

      transformer.transform(source, result);

    } catch (TransformerException ex) {
      LogUtils.severe(logger, ex);
      ok = false;
    }

    return ok;
  }

  public static Document xsltToDom(String src, String xsl) {
    Assert.notEmpty(src);
    Assert.notEmpty(xsl);

    StreamSource in = new StreamSource(src);
    StreamSource tr = new StreamSource(xsl);

    Document doc = domBuilder.newDocument();
    Element nd = doc.createElement(BeeUtils.createUniqueName("x2d"));

    DOMResult out = new DOMResult(nd);

    if (doXslt(in, tr, out)) {
      doc.appendChild(nd);
      return doc;
    } else {
      return null;
    }
  }

  public static boolean xsltToFile(String src, String xsl, String dst) {
    Assert.notEmpty(src);
    Assert.notEmpty(xsl);
    Assert.notEmpty(dst);

    StreamSource in = new StreamSource(src);
    StreamSource tr = new StreamSource(xsl);
    StreamResult out = new StreamResult(dst);

    return doXslt(in, tr, out);
  }

  public static List<SubProp> xsltToInfo(String src, String xsl) {
    Assert.notEmpty(src);
    Assert.notEmpty(xsl);

    Document doc = xsltToDom(src, xsl);
    if (doc == null) {
      return PropUtils.EMPTY_PROP_SUB_LIST;
    } else {
      return getTreeInfo(doc, "0");
    }
  }

  public static String xsltToString(String src, String xsl) {
    Assert.notEmpty(src);
    Assert.notEmpty(xsl);

    StreamSource in = new StreamSource(src);
    StreamSource tr = new StreamSource(xsl);

    StringWriter wrt = new StringWriter();
    StreamResult out = new StreamResult(wrt);

    if (doXslt(in, tr, out)) {
      return wrt.getBuffer().toString();
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  private static boolean checkBuilder() {
    if (domBuilder == null) {
      LogUtils.severe(logger, "Document Builder not available");
      return false;
    } else {
      return true;
    }
  }

  private static synchronized Document createDocument(File fl) {
    Document ret = null;
    if (!checkBuilder()) {
      return ret;
    }

    try {
      ret = domBuilder.parse(fl);
    } catch (SAXException ex) {
      LogUtils.error(logger, ex);
    } catch (IOException ex) {
      LogUtils.error(logger, ex);
    }

    return ret;
  }

  private static synchronized Document createDocument(Reader rdr) {
    Document ret = null;
    if (!checkBuilder()) {
      return ret;
    }

    try {
      ret = domBuilder.parse(new InputSource(rdr));
    } catch (SAXException ex) {
      LogUtils.error(logger, ex);
    } catch (IOException ex) {
      LogUtils.error(logger, ex);
    }

    return ret;
  }

  private static boolean doXslt(Source src, Source xsl, Result dst) {
    boolean ok = true;

    try {
      Transformer transf = xsltFactory.newTransformer(xsl);
      transf.transform(src, dst);
    } catch (TransformerException ex) {
      LogUtils.severe(logger, ex);
      ok = false;
    }

    return ok;
  }

  private static Document fromString(String xml) {
    Document doc = createDocument(new StringReader(xml));
    return doc;
  }

  private static List<StringProp> getDOMConfigurationInfo(DOMConfiguration cfg) {
    List<StringProp> lst = new ArrayList<StringProp>();
    if (cfg == null) {
      return lst;
    }

    DOMStringList names = cfg.getParameterNames();
    if (names == null) {
      return lst;
    }

    String key;

    for (int i = 0; i < names.getLength(); i++) {
      key = names.item(i);
      PropUtils.addString(lst, key, cfg.getParameter(key));
    }

    return lst;
  }

  private static List<StringProp> getNamedNodeMapInfo(NamedNodeMap nodes,
      String msg) {
    List<StringProp> lst = new ArrayList<StringProp>();
    if (nodes == null) {
      return lst;
    }

    int c = nodes.getLength();
    PropUtils.addString(lst, BeeUtils.ifString(msg, "Named Nodes"),
        BeeUtils.bracket(c));
    if (c > 0) {
      for (int i = 0; i < c; i++) {
        PropUtils.addString(lst, "Node " + BeeUtils.progress(i + 1, c),
            transformNode(nodes.item(i)));
      }
    }

    return lst;
  }

  private static String getNodeName(short type) {
    return nodeTypes.get(type);
  }

  private static boolean isAttribute(Node nd) {
    return nd.getNodeType() == Node.ATTRIBUTE_NODE;
  }

  private static boolean isElement(Node nd) {
    return nd.getNodeType() == Node.ELEMENT_NODE;
  }

  private static String transformDOMImplementation(DOMImplementation imp) {
    if (imp == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return imp.toString();
    }
  }

  private static String transformElement(Element el) {
    if (el == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.concat(1, el.getNodeName(), el.getTagName());
    }
  }

  private static String transformNode(Node nd) {
    if (nd == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.concat(1, nd.getNodeName(), nd.getNodeValue());
    }
  }

  private static String transformTypeInfo(TypeInfo ti) {
    if (ti == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.concat(1, ti.getTypeName(), ti.getTypeNamespace());
    }
  }
}
