package com.butent.bee.egg.client.utils;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

import com.butent.bee.egg.client.Global;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeXml {
  public static String createSimple(String rootName, Object... nodes) {
    Assert.notEmpty(rootName);
    Assert.parameterCount(nodes.length + 1, 3);

    return transformDocument(createDoc(rootName, nodes), null);
  }

  public static String createString(String rootName, Object... nodes) {
    Assert.notEmpty(rootName);
    Assert.parameterCount(nodes.length + 1, 3);

    return transformDocument(createDoc(rootName, nodes));
  }

  public static String fromVars(String rootName, String... names) {
    Assert.notEmpty(rootName);
    Assert.parameterCount(names.length + 1, 2);

    Object[] nodes = new Object[names.length * 2];
    for (int i = 0; i < names.length; i++) {
      nodes[i * 2] = names[i];
      nodes[i * 2 + 1] = Global.getVarValue(names[i]);
    }

    return transformDocument(createDoc(rootName, nodes));
  }

  private static void appendElementWithText(Document doc, Element root,
      String tag, String txt) {
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
      return ((String) obj).trim();
    } else {
      return BeeUtils.transform(obj);
    }
  }
}
