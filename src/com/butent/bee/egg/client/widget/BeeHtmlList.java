package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.user.client.ui.FocusWidget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class BeeHtmlList extends FocusWidget implements HasId {
  private static final int INSERT_AT_END = -1;
  private boolean ordered = false;
  
  private List<LIElement> items = new ArrayList<LIElement>();

  public BeeHtmlList() {
    this(false);
  }

  public BeeHtmlList(boolean ordered) {
    if (ordered) {
      setElement(Document.get().createOLElement());
    } else {
      setElement(Document.get().createULElement());
    }

    setStyleName("bee-HtmlList");
    createId();

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

  public void createId() {
    DomUtils.createId(this, "list");
  }

  public String getId() {
    return DomUtils.getId(this);
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

  public void insertItem(String item, int index) {
    insertItem(item, false, index);
  }

  public void insertItem(String item, boolean asHtml, int index) {
    LIElement child = Document.get().createLIElement();
    
    if (!BeeUtils.isEmpty(item)) {
      if (asHtml) {
        child.setInnerHTML(item);
      } else {
        child.setInnerText(item);
      }
    }

    child.setClassName("bee-HtmlListItem");
    child.setId(DomUtils.createUniqueId("li"));
    
    if ((index < 0) || (index >= getItemCount())) {
      getElement().appendChild(child);
      items.add(child);
    } else {
      getElement().insertBefore(getItem(index), child);
      items.add(index, child);
    }
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
