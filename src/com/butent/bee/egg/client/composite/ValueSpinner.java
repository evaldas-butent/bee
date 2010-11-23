package com.butent.bee.egg.client.composite;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.TextBox;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.layout.Absolute;
import com.butent.bee.egg.shared.HasIntValue;
import com.butent.bee.egg.shared.HasLongValue;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class ValueSpinner extends Absolute implements RequiresResize {
  private static final String STYLENAME_DEFAULT = "bee-ValueSpinner";

  private SpinnerBase spinner;
  private TextBox valueBox;
  private HasIntValue source;

  private SpinnerListener spinnerListener = new SpinnerListener() {
    public void onSpinning(long value) {
      if (getSpinner() != null) {
        getSpinner().setValue(value, false);
      }
      valueBox.setText(formatValue(value));
      setSourceValue(value);
    }
  };

  private KeyPressHandler keyPressHandler = new KeyPressHandler() {
    public void onKeyPress(KeyPressEvent event) {
      int index = valueBox.getCursorPos();
      String previousText = valueBox.getText();

      String newText;
      if (valueBox.getSelectionLength() > 0) {
        newText = previousText.substring(0, valueBox.getCursorPos()) + event.getCharCode()
            + previousText.substring(valueBox.getCursorPos()
                + valueBox.getSelectionLength(), previousText.length());
      } else {
        newText = previousText.substring(0, index) + event.getCharCode()
            + previousText.substring(index, previousText.length());
      }

      valueBox.cancelKey();
      try {
        long newValue = parseValue(newText);
        if (spinner.isConstrained()
            && (newValue > spinner.getMax() || newValue < spinner.getMin())) {
          return;
        }
        spinner.setValue(newValue, true);
      } catch (Exception ex) {
        BeeKeeper.getLog().warning(newText, ex);
      }
    }
  };

  public ValueSpinner(HasIntValue source) {
    this(source, 0, 0, 1, 99, false);
  }

  public ValueSpinner(HasIntValue source, int min, int max) {
    this(source, min, max, 1, 99, true);
  }

  public ValueSpinner(HasIntValue source, int min, int max, int step) {
    this(source, min, max, step, step, true);
  }

  public ValueSpinner(HasIntValue source, int min, int max, int minStep, int maxStep) {
    this(source, min, max, minStep, maxStep, true);
  }

  public ValueSpinner(HasIntValue source, int min, int max, int minStep, int maxStep,
      boolean constrained) {
    super();
    setStylePrimaryName(STYLENAME_DEFAULT);

    this.source = source;

    valueBox = new TextBox();
    DomUtils.createId(valueBox, "spin");
    valueBox.setStyleName("valueBox");
    valueBox.addKeyPressHandler(keyPressHandler);
    add(valueBox);

    spinner = new SpinnerBase(spinnerListener, getSourceValue(), min, max, minStep, maxStep,
        constrained);
    add(spinner.getIncrementArrow());
    add(spinner.getDecrementArrow());
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "spin-container");
  }

  public SpinnerBase getSpinner() {
    return spinner;
  }

  public SpinnerListener getSpinnerListener() {
    return spinnerListener;
  }

  public TextBox getTextBox() {
    return valueBox;
  }

  public long getValue() {
    return spinner.getValue();
  }

  public boolean isEnabled() {
    return spinner.isEnabled();
  }

  public void onResize() {
    setPositions();
  }

  public void setEnabled(boolean enabled) {
    spinner.setEnabled(enabled);
    valueBox.setEnabled(enabled);
  }

  protected String formatValue(long value) {
    return String.valueOf(value);
  }

  @Override
  protected void onLoad() {
    setPositions();
    super.onLoad();
  }

  protected long parseValue(String value) {
    return Long.valueOf(value);
  }

  private long getSourceValue() {
    if (source == null) {
      return 0;
    } else if (source instanceof HasLongValue) {
      return ((HasLongValue) source).getLong();
    } else {
      return source.getInt();
    }
  }
  
  private void setPositions() {
    int panelWidth = getElement().getClientWidth();
    int panelHeight = getElement().getClientHeight();

    int boxHeight = valueBox.getOffsetHeight();

    int incrWidth = spinner.getIncrementArrow().getWidth();
    int incrHeight = spinner.getIncrementArrow().getHeight();
    int decrWidth = spinner.getDecrementArrow().getWidth();
    int decrHeight = spinner.getDecrementArrow().getHeight();
    
    int w = BeeUtils.max(incrWidth, decrWidth);
    if (panelWidth < w * 2) {
      panelWidth = BeeUtils.max(w * 2, 60);
      DomUtils.setWidth(this, panelWidth);
    }
    int h = BeeUtils.max(boxHeight, incrHeight + decrHeight + 2);
    if (panelHeight != h) {
      DomUtils.setHeight(this, h);
    }
    
    setWidgetPosition(valueBox, 0, 0);
    DomUtils.setWidth(valueBox, panelWidth - w - 5);
    
    setWidgetPosition(spinner.getIncrementArrow(), panelWidth - w, 0);
    setWidgetPosition(spinner.getDecrementArrow(), panelWidth - w, h - decrHeight);
  }

  private void setSourceValue(long value) {
    if (source instanceof HasLongValue) {
      ((HasLongValue) source).setValue(value);
    } else if (source != null) {
      source.setValue((int) value);
    }
  }
}
