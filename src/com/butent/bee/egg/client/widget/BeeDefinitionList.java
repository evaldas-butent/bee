package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class BeeDefinitionList extends Widget implements HasId {
  private static final int INSERT_AT_END = -1;
  private List<Element> items = new ArrayList<Element>();

  public BeeDefinitionList() {
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
    Element child;

    if (definition) {
      child = DomUtils.createDtElement().cast();
    } else {
      child = DomUtils.createDdElement().cast();
    }
    
    if (!BeeUtils.isEmpty(item)) {
      if (asHtml) {
        child.setInnerHTML(item);
      } else {
        child.setInnerText(item);
      }
    }
    
    String tag = child.getTagName();
    child.setClassName("bee-Definition-" + tag);
    child.setId(DomUtils.createUniqueId(tag));
    
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
