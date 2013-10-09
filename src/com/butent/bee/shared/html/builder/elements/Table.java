package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Table extends FertileElement {

  public Table() {
    super("table");
  }

  public Table insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Table append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Table append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Table text(String text) {
    super.appendText(text);
    return this;
  }

  public Table remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Table setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public Table setBgcolor(String value) {
    setAttribute("bgcolor", value);
    return this;
  }

  public String getBgcolor() {
    return getAttribute("bgcolor");
  }

  public boolean removeBgcolor() {
    return removeAttribute("bgcolor");
  }

  public Table setBorder(String value) {
    setAttribute("border", value);
    return this;
  }

  public String getBorder() {
    return getAttribute("border");
  }

  public boolean removeBorder() {
    return removeAttribute("border");
  }

  public Table setCellpadding(String value) {
    setAttribute("cellpadding", value);
    return this;
  }

  public String getCellpadding() {
    return getAttribute("cellpadding");
  }

  public boolean removeCellpadding() {
    return removeAttribute("cellpadding");
  }

  public Table setCellspacing(String value) {
    setAttribute("cellspacing", value);
    return this;
  }

  public String getCellspacing() {
    return getAttribute("cellspacing");
  }

  public boolean removeCellspacing() {
    return removeAttribute("cellspacing");
  }

  public Table setFrame(String value) {
    setAttribute("frame", value);
    return this;
  }

  public String getFrame() {
    return getAttribute("frame");
  }

  public boolean removeFrame() {
    return removeAttribute("frame");
  }

  public Table setRules(String value) {
    setAttribute("rules", value);
    return this;
  }

  public String getRules() {
    return getAttribute("rules");
  }

  public boolean removeRules() {
    return removeAttribute("rules");
  }

  public Table setSummary(String value) {
    setAttribute("summary", value);
    return this;
  }

  public String getSummary() {
    return getAttribute("summary");
  }

  public boolean removeSummary() {
    return removeAttribute("summary");
  }

  public Table setWidth(String value) {
    setAttribute("width", value);
    return this;
  }

  public String getWidth() {
    return getAttribute("width");
  }

  public boolean removeWidth() {
    return removeAttribute("width");
  }

  public Table id(String value) {
    setId(value);
    return this;
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Table addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Table title(String value) {
    setTitle(value);
    return this;
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Table style(String value) {
    setStyle(value);
    return this;
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Table dir(String value) {
    setDir(value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Table lang(String value) {
    setLang(value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Table setXMLLang(String value) {
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
