package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Textarea extends FertileNode {

  public Textarea(String cols, String rows) {
    super("textarea");
    setCols(cols);
    setRows(rows);
  }

  public Textarea insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Textarea append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Textarea append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Textarea text(String text) {
    super.appendText(text);
    return this;
  }

  public Textarea remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Textarea setCols(String value) {
    setAttribute("cols", value);
    return this;
  }

  public String getCols() {
    return getAttribute("cols");
  }

  public boolean removeCols() {
    return removeAttribute("cols");
  }

  public Textarea setRows(String value) {
    setAttribute("rows", value);
    return this;
  }

  public String getRows() {
    return getAttribute("rows");
  }

  public boolean removeRows() {
    return removeAttribute("rows");
  }

  public Textarea setDisabled(String value) {
    setAttribute("disabled", value);
    return this;
  }

  public String getDisabled() {
    return getAttribute("disabled");
  }

  public boolean removeDisabled() {
    return removeAttribute("disabled");
  }

  public Textarea setName(String value) {
    setAttribute("name", value);
    return this;
  }

  public String getName() {
    return getAttribute("name");
  }

  public boolean removeName() {
    return removeAttribute("name");
  }

  public Textarea setReadonly(String value) {
    setAttribute("readonly", value);
    return this;
  }

  public String getReadonly() {
    return getAttribute("readonly");
  }

  public boolean removeReadonly() {
    return removeAttribute("readonly");
  }

  public Textarea setId(String value) {
    setAttribute("id", value);
    return this;
  }

  public String getId() {
    return getAttribute("id");
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Textarea setCSSClass(String value) {
    setAttribute("class", value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Textarea setTitle(String value) {
    setAttribute("title", value);
    return this;
  }

  public String getTitle() {
    return getAttribute("title");
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Textarea setStyle(String value) {
    setAttribute("style", value);
    return this;
  }

  public String getStyle() {
    return getAttribute("style");
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Textarea setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Textarea setLang(String value) {
    setAttribute("lang", value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Textarea setXMLLang(String value) {
    setAttribute("xml:lang", value);
    return this;
  }

  public String getXMLLang() {
    return getAttribute("xml:lang");
  }

  public boolean removeXMLLang() {
    return removeAttribute("xml:lang");
  }

  public Textarea setTabindex(String value) {
    setAttribute("tabindex", value);
    return this;
  }

  public String getTabindex() {
    return getAttribute("tabindex");
  }

  public boolean removeTabindex() {
    return removeAttribute("tabindex");
  }

  public Textarea setAccesskey(String value) {
    setAttribute("accesskey", value);
    return this;
  }

  public String getAccesskey() {
    return getAttribute("accesskey");
  }

  public boolean removeAccesskey() {
    return removeAttribute("accesskey");
  }

}
