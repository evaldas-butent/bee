package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;

import java.util.ArrayList;
import java.util.List;

/**
 * Enables using definitions for user interface components.
 */

public class DefinitionList extends Widget implements HasId {
  private static final int INSERT_AT_END = -1;
  private List<Element> items = new ArrayList<Element>();

  public DefinitionList() {
    setElement(Document.get().createDLElement());
    setStyleName("bee-DefinitionList");

    createId();
  }

  public void addDefinition(String text) {
    addDefinition(text, false);
  }

  public void addDefinition(String text, boolean asHtml) {
    insertItem(text, true, asHtml, INSERT_AT_END);
  }

  public void addItem(String text) {
    addItem(text, false);
  }

  public void addItem(String text, boolean asHtml) {
    insertItem(text, false, asHtml, INSERT_AT_END);
  }

  public void clear() {
    for (Element item : items) {
      getElement().removeChild(item);
    }
    items.clear();
  }

  public void createId() {
    DomUtils.createId(this, "d-list");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public Element getItem(int index) {
    checkIndex(index);
    return items.get(index);
  }

  public int getItemCount() {
    return items.size();
  }

  public String getItemText(int index) {
    checkIndex(index);
    return getItem(index).getInnerText();
  }

  public void insertDefinition(String text, int index) {
    insertItem(text, true, false, index);
  }

  public void insertItem(String item, boolean definition, boolean asHtml, int index) {
    Element child = DomUtils.createDefinitionItem(definition, item, asHtml).cast();

    String tag = child.getTagName().toLowerCase();
    child.setClassName("bee-Definition-" + tag);

    if ((index < 0) || (index >= getItemCount())) {
      getElement().appendChild(child);
      items.add(child);
    } else {
      getElement().insertBefore(getItem(index), child);
      items.add(index, child);
    }
  }

  public void insertItem(String text, int index) {
    insertItem(text, false, false, index);
  }

  public void removeItem(int index) {
    checkIndex(index);

    getElement().removeChild(getItem(index));
    items.remove(index);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setItemHtml(int index, String html) {
    checkIndex(index);
    Assert.notNull(html);

    getItem(index).setInnerHTML(html);
  }

  public void setItemText(int index, String text) {
    checkIndex(index);
    Assert.notNull(text);

    getItem(index).setInnerText(text);
  }

  private void checkIndex(int index) {
    Assert.isIndex(items, index);
  }

}
