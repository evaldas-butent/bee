package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements a user interface component that can contain a list of HTML code items.
 */

public class HtmlList extends Widget implements HasId {

  private static final int INSERT_AT_END = -1;
  private boolean ordered = false;

  private List<LIElement> items = new ArrayList<LIElement>();

  public HtmlList() {
    this(false);
  }

  public HtmlList(boolean ordered) {
    if (ordered) {
      setElement(Document.get().createOLElement());
    } else {
      setElement(Document.get().createULElement());
    }

    setStyleName("bee-HtmlList");
    DomUtils.createId(this, getIdPrefix());

    this.ordered = ordered;
  }

  public void addItem(String item) {
    addItem(item, false);
  }

  public void addItem(String item, boolean asHtml) {
    insertItem(item, asHtml, INSERT_AT_END);
  }

  public void clear() {
    for (LIElement item : items) {
      getElement().removeChild(item);
    }
    items.clear();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "html-list";
  }

  public LIElement getItem(int index) {
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

  public void insertItem(String item, boolean asHtml, int index) {
    LIElement child = DomUtils.createListItem(item, asHtml).cast();
    child.setClassName("bee-HtmlListItem");

    if ((index < 0) || (index >= getItemCount())) {
      getElement().appendChild(child);
      items.add(child);
    } else {
      getElement().insertBefore(getItem(index), child);
      items.add(index, child);
    }
  }

  public void insertItem(String item, int index) {
    insertItem(item, false, index);
  }

  public boolean isOrdered() {
    return ordered;
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
