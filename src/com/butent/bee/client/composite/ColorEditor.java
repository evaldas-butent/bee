package com.butent.bee.client.composite;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBoxBase;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.InputEvent;
import com.butent.bee.client.event.InputHandler;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.Color;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.HasTextBox;
import com.butent.bee.client.widget.InputColor;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ColorEditor extends Composite implements Editor, HasTextBox {

  private static final String STYLE_PREFIX = "bee-ColorEditor-";

  private static final String STYLE_CONTAINER = STYLE_PREFIX + "container";
  private static final String STYLE_TEXT_BOX = STYLE_PREFIX + "textBox";
  private static final String STYLE_PICKER = STYLE_PREFIX + "picker";

  private final InputText textBox;
  private final InputColor picker;

  private final List<BlurHandler> blurHandlers = Lists.newArrayList();

  private State pickerState = State.CLOSED;

  public ColorEditor() {
    super();

    this.textBox = new InputText();
    textBox.addStyleName(STYLE_TEXT_BOX);

    this.picker = new InputColor();
    picker.addStyleName(STYLE_PICKER);

    textBox.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        EventUtils.logEvent(event.getNativeEvent());
        LogUtils.getRootLogger().debug("blur", getPickerState());
        if (isPickerPending()) {
          setPickerState(State.ACTIVATED);
        } else {
          setPickerState(State.CLOSED);
          for (BlurHandler handler : blurHandlers) {
            handler.onBlur(event);
          }
        }
      }
    });

    textBox.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        LogUtils.getRootLogger().debug("press", getPickerState());
        if (event.getCharCode() == BeeConst.CHAR_ALL) {
          event.preventDefault();
          event.stopPropagation();

          openPicker();
        }
      }
    });

    picker.addInputHandler(new InputHandler() {
      @Override
      public void onInput(InputEvent event) {
        LogUtils.getRootLogger().debug("input", getPickerState());
        setPickerState(State.CLOSED);
        textBox.setValue(picker.getValue());
        fireEvent(new EditStopEvent(State.CHANGED));
      }
    });

    picker.addMouseDownHandler(new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent event) {
        LogUtils.getRootLogger().debug("mouse", getPickerState());
        setPickerState(State.PENDING);
      }
    });

    Flow container = new Flow();
    container.addStyleName(STYLE_CONTAINER);

    container.add(textBox);
    container.add(picker);

    initWidget(container);
  }

  @Override
  public HandlerRegistration addBlurHandler(final BlurHandler handler) {
    Assert.notNull(handler);
    blurHandlers.add(handler);

    return new HandlerRegistration() {
      @Override
      public void removeHandler() {
        blurHandlers.remove(handler);
      }
    };
  }

  @Override
  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  @Override
  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return getTextBox().addDomHandler(handler, FocusEvent.getType());
  }

  @Override
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return getTextBox().addDomHandler(handler, KeyDownEvent.getType());
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public void clearValue() {
    setValue(null);
  }

  @Override
  public EditorAction getDefaultFocusAction() {
    return EditorAction.SELECT;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "color-edit";
  }

  @Override
  public String getNormalizedValue() {
    return textBox.getNormalizedValue();
  }

  @Override
  public String getOptions() {
    return textBox.getOptions();
  }

  @Override
  public int getTabIndex() {
    return getTextBox().getTabIndex();
  }

  @Override
  public TextBoxBase getTextBox() {
    return textBox;
  }

  @Override
  public String getValue() {
    return getTextBox().getValue();
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.COLOR_EDITOR;
  }

  @Override
  public boolean handlesKey(int keyCode) {
    return textBox.handlesKey(keyCode);
  }

  @Override
  public boolean handlesTabulation() {
    return textBox.handlesTabulation();
  }

  @Override
  public boolean isEditing() {
    return textBox.isEditing();
  }

  @Override
  public boolean isEnabled() {
    return textBox.isEnabled();
  }

  @Override
  public boolean isNullable() {
    return textBox.isNullable();
  }

  @Override
  public boolean isOrHasPartner(Node node) {
    return textBox.isOrHasPartner(node) || picker.getElement().isOrHasChild(node);
  }

  @Override
  public void normalizeDisplay(String normalizedValue) {
    picker.setColor(normalizedValue);
  }

  @Override
  public void setAccessKey(char key) {
    getTextBox().setAccessKey(key);
  }

  @Override
  public void setEditing(boolean editing) {
    textBox.setEditing(editing);
  }

  @Override
  public void setEnabled(boolean enabled) {
    getTextBox().setEnabled(enabled);
    picker.setEnabled(enabled);
  }

  @Override
  public void setFocus(boolean focused) {
    getTextBox().setFocus(focused);
  }

  @Override
  public void setHandlesTabulation(boolean handlesTabulation) {
    textBox.setHandlesTabulation(handlesTabulation);
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  public void setNullable(boolean nullable) {
    textBox.setNullable(nullable);
  }

  @Override
  public void setOptions(String options) {
    textBox.setOptions(options);
  }

  @Override
  public void setTabIndex(int index) {
    getTextBox().setTabIndex(index);
  }

  @Override
  public void setValue(String newValue) {
    setValue(newValue, false);
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    getTextBox().setValue(value, fireEvents);
    picker.setColor(value);
  }

  @Override
  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
    
    if (charCode == BeeConst.CHAR_ALL) {
      setValue(oldValue);
      openPicker();
    } else {
      textBox.startEdit(oldValue, charCode, onEntry, sourceElement);
    }
  }

  @Override
  public List<String> validate(boolean checkForNull) {
    return validate(getValue(), checkForNull);
  }

  @Override
  public List<String> validate(String normalizedValue, boolean checkForNull) {
    List<String> messages = Lists.newArrayList();

    if (BeeUtils.isEmpty(normalizedValue)) {
      if (checkForNull && !isNullable()) {
        messages.add("Įveskite spalvą");
      }
      return messages;
    }

    if (!Color.validate(normalizedValue)) {
      messages.add(BeeUtils.joinWords("Neteisinga spalva:", normalizedValue));
    }
    return messages;
  }

  private State getPickerState() {
    return pickerState;
  }

  private boolean isPickerPending() {
    return State.PENDING.equals(getPickerState());
  }

  private void openPicker() {
    if (!isPickerPending()) {
      setPickerState(State.PENDING);
      picker.click();
    }
  }

  private void setPickerState(State pickerState) {
    this.pickerState = pickerState;
  }
}