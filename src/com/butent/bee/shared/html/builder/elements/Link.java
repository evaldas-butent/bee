package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Link extends FertileElement {

  public Link() {
    super("link");
  }

  public Link insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Link append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Link append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Link text(String text) {
    super.appendText(text);
    return this;
  }

  public Link remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Link setCharset(String value) {
    setAttribute("charset", value);
    return this;
  }

  public String getCharset() {
    return getAttribute("charset");
  }

  public boolean removeCharset() {
    return removeAttribute("charset");
  }

  public Link setHref(String value) {
    setAttribute("href", value);
    return this;
  }

  public String getHref() {
    return getAttribute("href");
  }

  public boolean removeHref() {
    return removeAttribute("href");
  }

  public Link setHreflang(String value) {
    setAttribute("hreflang", value);
    return this;
  }

  public String getHreflang() {
    return getAttribute("hreflang");
  }

  public boolean removeHreflang() {
    return removeAttribute("hreflang");
  }

  public Link setMedia(String value) {
    setAttribute("media", value);
    return this;
  }

  public String getMedia() {
    return getAttribute("media");
  }

  public boolean removeMedia() {
    return removeAttribute("media");
  }

  public Link setRel(String value) {
    setAttribute("rel", value);
    return this;
  }

  public String getRel() {
    return getAttribute("rel");
  }

  public boolean removeRel() {
    return removeAttribute("rel");
  }

  public Link setRev(String value) {
    setAttribute("rev", value);
    return this;
  }

  public String getRev() {
    return getAttribute("rev");
  }

  public boolean removeRev() {
    return removeAttribute("rev");
  }

  public Link setTarget(String value) {
    setAttribute("target", value);
    return this;
  }

  public String getTarget() {
    return getAttribute("target");
  }

  public boolean removeTarget() {
    return removeAttribute("target");
  }

  public Link setType(String value) {
    setAttribute("type", value);
    return this;
  }

  public String getType() {
    return getAttribute("type");
  }

  public boolean removeType() {
    return removeAttribute("type");
  }

  public Link id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Link addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Link title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Link style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Link dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Link lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Link setXMLLang(String value) {
    setAttribute("xml:lang", value);
    return this;
  }

  public String getXMLLang() {
    return getAttribute("xml:lang");
  }

  public boolean removeXMLLang() {
    return removeAttribute("xml:lang");
  }

}
