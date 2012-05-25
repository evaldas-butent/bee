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

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Span;
import com.butent.bee.client.ui.AcceptsCaptions;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.BeeRadioButton;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

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
  private int optionCount = 0;

  private Variable variable = null;

  private int valueStartIndex = 0;

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

    List<String> opt = UiHelper.getCaptions(clazz);
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

  public RadioGroup(Variable var) {
    this(var, false);
  }

  public RadioGroup(Variable var, boolean vertical) {
    this(vertical);
    setVariable(var);

    List<String> opt = var.getItems();

    String z = var.getValue();
    int value = BeeUtils.isEmpty(z) ? BeeConst.UNDEF : opt.indexOf(z);

    addButtons(opt, value);
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  public void addCaptions(Class<? extends Enum<?>> clazz) {
    addButtons(UiHelper.getCaptions(clazz));
  }

  public void addCaptions(String captionKey) {
    addButtons(Global.getCaptions(captionKey));
  }

  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return addDomHandler(handler, FocusEvent.getType());
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  public void addOption(String label) {
    addOption(label, false, false);
  }

  public void addOption(String label, boolean asHtml) {
    addOption(label, asHtml, false);
  }

  public void addOption(String label, boolean asHtml, boolean selected) {
    Assert.notEmpty(label);
    int index = getOptionCount();

    BeeRadioButton rb = new BeeRadioButton(getName(), label, asHtml);
    add(rb);

    rb.setFormValue(BeeUtils.toString(index));
    rb.addStyleDependentName(isVertical() ? StyleUtils.NAME_VERTICAL : StyleUtils.NAME_HORIZONTAL);
    if (selected) {
      rb.setValue(true);
    }
    rb.addValueChangeHandler(this);

    setOptionCount(index + 1);
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public void clear() {
    super.clear();
    setOptionCount(0);
  }

  public EditorAction getDefaultFocusAction() {
    return null;
  }
  
  @Override
  public String getDefaultStyleName() {
    return "bee-RadioGroup";
  }

  @Override
  public String getIdPrefix() {
    return "rg";
  }

  public String getName() {
    return name;
  }

  public String getNormalizedValue() {
    return getValue();
  }

  public int getSelectedIndex() {
    for (int i = 0; i < getWidgetCount(); i++) {
      Widget widget = getWidget(i);
      if (widget instanceof BeeRadioButton && ((BeeRadioButton) widget).getValue()) {
        return BeeUtils.toInt(((BeeRadioButton) widget).getFormValue());
      }
    }
    return BeeConst.UNDEF;
  }

  public int getTabIndex() {
    return 0;
  }

  public String getValue() {
    int index = getSelectedIndex();
    return (index >= 0) ? BeeUtils.toString(index + getValueStartIndex()) : null;
  }

  public int getValueStartIndex() {
    return valueStartIndex;
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.RADIO;
  }

  public boolean handlesKey(int keyCode) {
    return false;
  }

  public boolean isEditing() {
    return false;
  }

  public boolean isEnabled() {
    for (int i = 0; i < getWidgetCount(); i++) {
      Widget widget = getWidget(i);
      if (widget instanceof BeeRadioButton) {
        return ((BeeRadioButton) widget).isEnabled();
      }
    }
    return false;
  }

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

  public void onValueChange(ValueChangeEvent<Boolean> event) {
    Object source = event.getSource();

    if (source instanceof BeeRadioButton && BeeUtils.isTrue(event.getValue())) {
      BeeRadioButton rb = (BeeRadioButton) source;
      int index = BeeUtils.toInt(rb.getFormValue()) + getValueStartIndex();

      if (getVariable() != null) {
        getVariable().setValue(getVariable().getItems().get(index));
      }
      if (BeeKeeper.getStorage().hasItem(getName())) {
        BeeKeeper.getStorage().setItem(getName(), BeeUtils.toString(index));
      }

      ValueChangeEvent.fire(this, BeeUtils.toString(index));
    }
  }

  public void setAccessKey(char key) {
  }

  public void setEditing(boolean editing) {
  }

  public void setEnabled(boolean enabled) {
    DomUtils.enableChildren(this, enabled);
  }

  public void setFocus(boolean focused) {
  }

  public void setNullable(boolean nullable) {
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

  public void setTabIndex(int index) {
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
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
      if (fireEvents) {
        ValueChangeEvent.fire(this, isIndex(newIndex) ? BeeUtils.toString(newIndex) : null);
      }
    }
  }

  public void setValueStartIndex(int valueStartIndex) {
    this.valueStartIndex = valueStartIndex;
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
  }

  public String validate() {
    return null;
  }

  private void addButtons(List<String> opt) {
    Assert.notNull(opt);

    for (String s : opt) {
      addOption(s, false, false);
    }
  }

  private void addButtons(List<String> opt, int value) {
    Assert.notNull(opt);

    int idx = 0;
    for (String s : opt) {
      addOption(s, false, idx++ == value);
    }
  }

  private BeeRadioButton getOption(int index) {
    if (isIndex(index)) {
      for (int i = 0; i < getWidgetCount(); i++) {
        Widget widget = getWidget(i);
        if (widget instanceof BeeRadioButton
            && BeeUtils.equalsTrim(BeeUtils.toString(index),
                ((BeeRadioButton) widget).getFormValue())) {
          return (BeeRadioButton) widget;
        }
      }
    }
    return null;
  }

  private int getOptionCount() {
    return optionCount;
  }

  private Variable getVariable() {
    return variable;
  }

  private boolean isIndex(int index) {
    return index >= 0 && index < getOptionCount();
  }

  private void setOptionCount(int optionCount) {
    this.optionCount = optionCount;
  }

  private void setVariable(Variable variable) {
    this.variable = variable;
  }
}
