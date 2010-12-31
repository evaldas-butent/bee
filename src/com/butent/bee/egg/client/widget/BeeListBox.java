package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ListBox;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.HasBeeChangeHandler;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.HasStringValue;
import com.butent.bee.egg.shared.Variable;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public class BeeListBox extends ListBox implements HasId, HasBeeChangeHandler {
  private HasStringValue source = null;

  public BeeListBox() {
    super();
    init();
  }

  public BeeListBox(boolean isMultipleSelect) {
    super(isMultipleSelect);
    init();
  }

  public BeeListBox(Element element) {
    super(element);
    init();
  }

  public BeeListBox(HasStringValue source) {
    this();
    this.source = source;
    addDefaultHandlers();
    
    if (source instanceof Variable) {
      initVar((Variable) source);
    }
  }

  public BeeListBox(HasStringValue source, boolean allVisible) {
    this(source);
    if (allVisible) {
      setAllVisible();
    }
  }

  public BeeListBox(HasStringValue source, int cnt) {
    this(source);
    if (cnt > 0) {
      setVisibleItemCount(cnt);
    }
  }

  public void addItems(List<String> items) {
    Assert.notNull(items);

    for (String it : items) {
      addItem(it);
    }
  }

  public void createId() {
    DomUtils.createId(this, "list");
  }

  public String getId() {
    return DomUtils.getId(this);
  }
  
  public int getIndex(String text) {
    Assert.notNull(text);
    int index = -1;
    
    for (int i = 0; i < getItemCount(); i++) {
      if (BeeUtils.same(getValue(i), text)) {
        index = i;
        break;
      }
    }
    return index;
  }

  public HasStringValue getSource() {
    return source;
  }

  public boolean onChange() {
    if (getSource() != null) {
      getSource().setValue(getValue(getSelectedIndex()));
    }
    return true;
  }

  public void setAllVisible() {
    int cnt = getItemCount();
    if (cnt > 0) {
      setVisibleItemCount(cnt);
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setSource(HasStringValue source) {
    this.source = source;
  }

  private void addDefaultHandlers() {
    BeeKeeper.getBus().addVch(this);
  }

  private void init() {
    createId();
    setStyleName("bee-ListBox");
  }
  
  private void initVar(Variable var) {
    addItems(var.getItems());

    String v = var.getValue();
    if (!BeeUtils.isEmpty(v)) {
      setSelectedIndex(getIndex(v));
    }
  }
}
