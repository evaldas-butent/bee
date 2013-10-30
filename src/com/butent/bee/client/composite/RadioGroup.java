package com.butent.bee.client.composite;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Span;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AcceptsCaptions;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.RadioButton;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collections;
import java.util.List;

/**
 * Enables to use a user interface component, consisting of a group of radio buttons.
 */

public class RadioGroup extends Span implements Editor, ValueChangeHandler<Boolean>,
    HasValueStartIndex, AcceptsCaptions {

  public static int getValue(String name) {
    int v = BeeConst.UNDEF;
    if (BeeUtils.isEmpty(name)) {
      return v;
    }

    NodeList<Element> lst = DomUtils.getElementsByName(name);
    if (lst.getLength() <= 0) {
      return v;
    }

    Element el;
    InputElement inp;

    for (int i = 0; i < lst.getLength(); i++) {
      el = lst.getItem(i);
      if (!DomUtils.isInputElement(el)) {
        continue;
      }
      inp = InputElement.as(el);

      if (inp.isChecked()) {
        v = BeeUtils.toInt(inp.getValue());
        break;
      }
    }
    return v;
  }

  private final String name;
  private final boolean vertical;
  private int optionCount;

  private int valueStartIndex;

  private String options;

  private boolean handlesTabulation;
  
  public RadioGroup(boolean vertical) {
    this(NameUtils.createUniqueName("optiongroup"), vertical);
  }

  public RadioGroup(boolean vertical, int value, List<String> opt) {
    this(vertical);
    addButtons(opt, value);
  }
  
  public RadioGroup(String name, boolean vertical) {
    super();
    Assert.notEmpty(name);
    this.name = name;
    this.vertical = vertical;
  }

  public RadioGroup(String name, boolean vertical, Enum<?> value, Class<? extends Enum<?>> clazz) {
    this(name, vertical);

    List<String> opt = Captions.getCaptions(clazz);
    int z = (value == null) ? BeeConst.UNDEF : value.ordinal();
    addButtons(opt, z);
  }
  
  public RadioGroup(String name, boolean vertical, int value, List<String> opt) {
    this(name, vertical);
    addButtons(opt, value);
  }

  public RadioGroup(String name, boolean vertical, List<String> opt) {
    this(name, vertical, BeeConst.UNDEF, opt);
  }

  public RadioGroup(String name, Enum<?> value, Class<? extends Enum<?>> clazz) {
    this(name, false, value, clazz);
  }

  public RadioGroup(String name, int value, List<String> opt) {
    this(name, false, value, opt);
  }

  public RadioGroup(String name, List<String> opt) {
    this(name, false, opt);
  }

  @Override
  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  @Override
  public void addCaptions(Class<? extends Enum<?>> clazz) {
    addButtons(Captions.getCaptions(clazz));
  }

  @Override
  public void addCaptions(String captionKey) {
    addButtons(Captions.getCaptions(captionKey));
  }

  @Override
  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  @Override
  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return addDomHandler(handler, FocusEvent.getType());
  }

  @Override
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  public void addOption(String label) {
    addOption(label, false);
  }

  public void addOption(String label, boolean selected) {
    Assert.notEmpty(label);
    int index = getOptionCount();

    RadioButton rb = new RadioButton(getName(), label);
    add(rb);

    rb.setFormValue(BeeUtils.toString(index));
    rb.addStyleDependentName(isVertical() ? StyleUtils.NAME_VERTICAL : StyleUtils.NAME_HORIZONTAL);
    if (selected) {
      rb.setValue(true);
    }
    rb.addValueChangeHandler(this);

    setOptionCount(index + 1);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public void clear() {
    super.clear();
    setOptionCount(0);
  }

  @Override
  public void clearValue() {
    setValue(null);
  }

  @Override
  public EditorAction getDefaultFocusAction() {
    return null;
  }

  @Override
  public String getIdPrefix() {
    return "rg";
  }

  public String getName() {
    return name;
  }

  @Override
  public String getNormalizedValue() {
    return getValue();
  }

  @Override
  public String getOptions() {
    return options;
  }
  
  public int getSelectedIndex() {
    for (int i = 0; i < getWidgetCount(); i++) {
      Widget widget = getWidget(i);
      if (widget instanceof RadioButton && ((RadioButton) widget).getValue()) {
        return BeeUtils.toInt(((RadioButton) widget).getFormValue());
      }
    }
    return BeeConst.UNDEF;
  }

  @Override
  public int getTabIndex() {
    return 0;
  }

  @Override
  public String getValue() {
    int index = getSelectedIndex();
    return (index >= 0) ? BeeUtils.toString(index + getValueStartIndex()) : null;
  }

  @Override
  public int getValueStartIndex() {
    return valueStartIndex;
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.RADIO;
  }

  @Override
  public boolean handlesKey(int keyCode) {
    return false;
  }

  @Override
  public boolean handlesTabulation() {
    return handlesTabulation;
  }

  @Override
  public boolean isEditing() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    for (int i = 0; i < getWidgetCount(); i++) {
      Widget widget = getWidget(i);
      if (widget instanceof RadioButton) {
        return ((RadioButton) widget).isEnabled();
      }
    }
    return false;
  }

  @Override
  public boolean isNullable() {
    return false;
  }

  @Override
  public boolean isOrHasPartner(Node node) {
    return node != null && getElement().isOrHasChild(node);
  }

  public boolean isVertical() {
    return vertical;
  }

  @Override
  public void normalizeDisplay(String normalizedValue) {
  }

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    Object source = event.getSource();

    if (source instanceof RadioButton && BeeUtils.isTrue(event.getValue())) {
      RadioButton rb = (RadioButton) source;
      int index = BeeUtils.toInt(rb.getFormValue()) + getValueStartIndex();

      ValueChangeEvent.fire(this, BeeUtils.toString(index));
    }
  }
  
  @Override
  public void setAccessKey(char key) {
  }
  
  @Override
  public void setEditing(boolean editing) {
  }

  @Override
  public void setEnabled(boolean enabled) {
    DomUtils.enableChildren(this, enabled);
  }

  @Override
  public void setFocus(boolean focused) {
  }

  @Override
  public void setHandlesTabulation(boolean handlesTabulation) {
    this.handlesTabulation = handlesTabulation;
  }

  @Override
  public void setNullable(boolean nullable) {
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  public void setSelectedIndex(int newIndex) {
    int oldIndex = getSelectedIndex();

    if (newIndex != oldIndex) {
      if (isIndex(newIndex)) {
        getOption(newIndex).setValue(true);
      } else if (isIndex(oldIndex)) {
        getOption(oldIndex).setValue(false);
      }
    }
  }

  @Override
  public void setTabIndex(int index) {
  }

  @Override
  public void setValue(String value) {
    int oldIndex = getSelectedIndex();
    int newIndex = BeeConst.UNDEF;

    if (BeeUtils.isDigit(BeeUtils.trim(value))) {
      int z = BeeUtils.toInt(value) - getValueStartIndex();
      if (isIndex(z)) {
        newIndex = z;
      }
    }

    if (newIndex != oldIndex) {
      if (isIndex(newIndex)) {
        getOption(newIndex).setValue(true);
      } else if (isIndex(oldIndex)) {
        getOption(oldIndex).setValue(false);
      }
    }
  }

  @Override
  public void setValueStartIndex(int valueStartIndex) {
    this.valueStartIndex = valueStartIndex;
  }

  @Override
  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
  }

  @Override
  public List<String> validate(boolean checkForNull) {
    return Collections.emptyList();
  }

  @Override
  public List<String> validate(String normalizedValue, boolean checkForNull) {
    return Collections.emptyList();
  }
  
  @Override
  protected String getDefaultStyleName() {
    return "bee-RadioGroup";
  }

  private void addButtons(List<String> opt) {
    Assert.notNull(opt);

    for (String s : opt) {
      addOption(s, false);
    }
  }

  private void addButtons(List<String> opt, int value) {
    Assert.notNull(opt);

    int idx = 0;
    for (String s : opt) {
      addOption(s, idx++ == value);
    }
  }

  private RadioButton getOption(int index) {
    if (isIndex(index)) {
      for (int i = 0; i < getWidgetCount(); i++) {
        Widget widget = getWidget(i);
        if (widget instanceof RadioButton
            && BeeUtils.equalsTrim(BeeUtils.toString(index),
                ((RadioButton) widget).getFormValue())) {
          return (RadioButton) widget;
        }
      }
    }
    return null;
  }

  private int getOptionCount() {
    return optionCount;
  }

  private boolean isIndex(int index) {
    return index >= 0 && index < getOptionCount();
  }

  private void setOptionCount(int optionCount) {
    this.optionCount = optionCount;
  }
}
