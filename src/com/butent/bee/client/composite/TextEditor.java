package com.butent.bee.client.composite;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBoxBase;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.client.view.edit.HasTextBox;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.State;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.HasTextDimensions;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a component used for making changes to multiple lines of text.
 */

public class TextEditor extends Absolute implements Editor, HasTextDimensions, HasTextBox {

  private final InputArea area;
  private final String acceptId;
  private final String noesId;

  public TextEditor() {
    super();
    this.area = new InputArea();
    area.addStyleName("bee-TextEditor-area");

    BeeImage accept = new BeeImage(Global.getImages().accept(), new EditorFactory.Accept(area));
    accept.addStyleName("bee-TextEditor-accept");
    this.acceptId = accept.getId();

    BeeImage noes = new BeeImage(Global.getImages().noes(), new EditorFactory.Cancel(area));
    noes.addStyleName("bee-TextEditor-noes");
    this.noesId = noes.getId();

    add(area);
    add(accept);
    add(noes);

    addStyleName("bee-TextEditor");
    sinkEvents(Event.ONMOUSEDOWN);
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return getArea().addDomHandler(handler, BlurEvent.getType());
  }

  public HandlerRegistration addEditStopHandler(Handler handler) {
    return getArea().addEditStopHandler(handler);
  }

  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return getArea().addDomHandler(handler, FocusEvent.getType());
  }
  
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return getArea().addKeyDownHandler(handler);
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return getArea().addValueChangeHandler(handler);
  }
  
  public int getCharacterWidth() {
    return getArea().getCharacterWidth();
  }

  public EditorAction getDefaultFocusAction() {
    return getArea().getDefaultFocusAction();
  }
  
  @Override
  public String getIdPrefix() {
    return "text-editor";
  }
  
  public String getNormalizedValue() {
    return getArea().getNormalizedValue();
  }

  public int getTabIndex() {
    return getArea().getTabIndex();
  }

  public TextBoxBase getTextBox() {
    return getArea();
  }
  
  public String getValue() {
    return getArea().getValue();
  }

  public int getVisibleLines() {
    return getArea().getVisibleLines();
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.INPUT_AREA;
  }
  
  public boolean handlesKey(int keyCode) {
    return getArea().handlesKey(keyCode);
  }

  public boolean isEditing() {
    return getArea().isEditing();
  }

  public boolean isEnabled() {
    return getArea().isEnabled();
  }

  public boolean isNullable() {
    return getArea().isNullable();
  }

  @Override
  public boolean isOrHasPartner(Node node) {
    return node != null && getElement().isOrHasChild(node);
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (EventUtils.isMouseDown(event.getType())) {
      String id = EventUtils.getTargetId(event.getEventTarget());
      if (BeeUtils.same(id, getId())) {
        event.preventDefault();
        return;
      }
      if (BeeUtils.inListSame(id, getAcceptId(), getNoesId())) {
        event.preventDefault();
        State state = BeeUtils.same(id, getAcceptId()) ? State.CHANGED : State.CANCELED;
        getArea().fireEvent(new EditStopEvent(state));
        return;
      }
    }
    super.onBrowserEvent(event);
  }

  public void setAccessKey(char key) {
    getArea().setAccessKey(key);
  }

  public void setCharacterWidth(int width) {
    getArea().setCharacterWidth(width);
  }

  public void setEditing(boolean editing) {
    getArea().setEditing(editing);
  }

  public void setEnabled(boolean enabled) {
    DomUtils.enableChildren(this, enabled);
  }

  public void setFocus(boolean focused) {
    getArea().setFocus(focused);
  }

  public void setNullable(boolean nullable) {
    getArea().setNullable(nullable);
  }

  public void setTabIndex(int index) {
    getArea().setTabIndex(index);
  }

  public void setValue(String value) {
    getArea().setValue(value);
  }

  public void setValue(String value, boolean fireEvents) {
    getArea().setValue(value, fireEvents);
  }

  public void setVisibleLines(int lines) {
    getArea().setVisibleLines(lines);
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
    getArea().startEdit(oldValue, charCode, onEntry, sourceElement);
  }

  public String validate() {
    return getArea().validate();
  }

  private String getAcceptId() {
    return acceptId;
  }

  private InputArea getArea() {
    return area;
  }

  private String getNoesId() {
    return noesId;
  }
}
