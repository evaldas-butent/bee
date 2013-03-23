package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.InputLong;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class IdFilterSupplier extends AbstractFilterSupplier {
  
  private static final int MIN_EDITOR_WIDTH = 60;
  private static final int MAX_EDITOR_WIDTH = 100;
  
  private final Editor editor;
  private int lastWidth = BeeConst.UNDEF;

  public IdFilterSupplier(String viewName, final BeeColumn column, String options) {
    super(viewName, column, options);
    
    this.editor = new InputLong();

    editor.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          IdFilterSupplier.this.onSave();
        }
      }
    });
  }

  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList();
  }
  
  @Override
  public String getDisplayHtml() {
    return editor.getValue();
  }

  @Override
  public void onRequest(Element target, NotificationListener notificationListener,
      final Callback<Boolean> callback) {
    int width = BeeUtils.clamp(target.getOffsetWidth(), MIN_EDITOR_WIDTH, MAX_EDITOR_WIDTH);
    if (width != getLastWidth()) {
      StyleUtils.setWidth(editor.asWidget(), width);
      setLastWidth(width);
    }
    
    openDialog(target, editor.asWidget(), callback);
    editor.setFocus(true);
  }

  @Override
  public boolean reset() {
    editor.clearValue();
    return super.reset();
  }
  
  private int getLastWidth() {
    return lastWidth;
  }

  private void onSave() {
    String value = BeeUtils.trim(editor.getValue());
    if (BeeUtils.isEmpty(value)) {
      update(null);
      return;
    }
    
    if (!BeeUtils.isLong(value)) {
      Global.showError("Neteisinga ID reikšmė");
      return;
    }
    update(ComparisonFilter.compareId(BeeUtils.toLong(value)));
  }

  private void setLastWidth(int lastWidth) {
    this.lastWidth = lastWidth;
  }
}
