package com.butent.bee.client.richtext;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.RichTextArea;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.AdjustmentListener;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.State;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables usage of formatted text editor user interface component.
 */

public class RichTextEditor extends Flow implements Editor, AdjustmentListener,
    NativePreviewHandler {

  private final RichTextToolbar toolbar;
  private final RichTextArea area;

  private boolean nullable = true;

  private boolean editing = false;

  private HandlerRegistration previewRegistration = null;

  public RichTextEditor() {
    super();
    this.area = new RichTextArea();
    this.area.setStyleName("bee-RichTextArea");
    DomUtils.createId(this.area, "rt-area");

    this.toolbar = new RichTextToolbar(this, this.area);
    this.toolbar.setStyleName("bee-RichTextToolbar");

    add(this.toolbar);
    add(this.area);
    setStyleName("bee-RichTextEditor");
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return addDomHandler(handler, FocusEvent.getType());
  }
  
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return getArea().addKeyDownHandler(handler);
  }
  
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public void adjust(Element source) {
    if (source != null) {
      StyleUtils.copyProperties(source, getElement(), StyleUtils.STYLE_LEFT, StyleUtils.STYLE_TOP);
      StyleUtils.setWidth(this, source.getOffsetWidth());
      StyleUtils.setHeight(this, source.getOffsetHeight());
    }
  }

  public EditorAction getDefaultFocusAction() {
    return null;
  }
  
  @Override
  public String getIdPrefix() {
    return "rt-editor";
  }

  public String getNormalizedValue() {
    if (getValue() == null) {
      return null;
    }
    return BeeUtils.trimRight(getValue());
  }

  public int getTabIndex() {
    return getArea().getTabIndex();
  }

  public String getValue() {
    return getArea().getHTML();
  }

  public boolean handlesKey(int keyCode) {
    return !BeeUtils.inList(keyCode, KeyCodes.KEY_ESCAPE, KeyCodes.KEY_TAB);
  }

  public boolean isEditing() {
    return editing;
  }

  public boolean isEnabled() {
    return getArea().isEnabled();
  }

  public boolean isNullable() {
    return nullable;
  }

  @Override
  public boolean isOrHasPartner(Node node) {
    return node != null && getElement().isOrHasChild(node);
  }
  
  public void onPreviewNativeEvent(NativePreviewEvent event) {
    if (isEditing() && EventUtils.isMouseDown(event.getNativeEvent().getType())
        && !EventUtils.equalsOrIsChild(getElement(), event.getNativeEvent().getEventTarget())) {
      fireEvent(new EditStopEvent(State.CANCELED));
    }
  }

  public void setAccessKey(char key) {
    getArea().setAccessKey(key);
  }

  public void setEditing(boolean editing) {
    this.editing = editing;

    if (editing) {
      if (getPreviewRegistration() == null) {
        setPreviewRegistration(Event.addNativePreviewHandler(this));
      }
    } else {
      closePreview();
      setFocus(false);
    }
  }

  public void setEnabled(boolean enabled) {
    DomUtils.enableChildren(this, enabled);
  }

  public void setFocus(boolean focused) {
    getArea().setFocus(focused);
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setTabIndex(int index) {
    getArea().setTabIndex(index);
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
    getArea().setHTML(value);
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
    EditorAction action = (onEntry == null) ? EditorAction.ADD_LAST : onEntry;
    UiHelper.doEditorAction(this, oldValue, charCode, action);

    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        StyleUtils.setHeight(getArea(), getElement().getClientHeight()
            - getArea().getElement().getOffsetTop());
        getToolbar().updateStatus();
      }
    });
  }

  public String validate() {
    return null;
  }

  @Override
  protected void onUnload() {
    if (!BeeKeeper.getScreen().isTemporaryDetach()) {
      closePreview();
    }
    super.onUnload();
  }

  private void closePreview() {
    if (getPreviewRegistration() != null) {
      getPreviewRegistration().removeHandler();
      setPreviewRegistration(null);
    }
  }

  private RichTextArea getArea() {
    return area;
  }

  private HandlerRegistration getPreviewRegistration() {
    return previewRegistration;
  }

  private RichTextToolbar getToolbar() {
    return toolbar;
  }

  private void setPreviewRegistration(HandlerRegistration previewRegistration) {
    this.previewRegistration = previewRegistration;
  }
}
