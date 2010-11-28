package com.butent.bee.egg.client.composite;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.TextBox;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.layout.Absolute;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.ValueUtils;

public class ValueSpinner extends Absolute implements RequiresResize {
  private static final String STYLENAME_DEFAULT = "bee-ValueSpinner";

  private SpinnerBase spinner;
  private TextBox valueBox;
  private Object source;

  private SpinnerListener spinnerListener = new SpinnerListener() {
    public void onSpinning(long value) {
      valueBox.setText(formatValue(value));
      setSourceValue(value);
    }
  };

  private KeyDownHandler keyDownHandler = new KeyDownHandler() {
    public void onKeyDown(KeyDownEvent event) {
      if (!isEnabled()) {
        return;
      }
      
      switch (event.getNativeKeyCode()) {
        case KeyCodes.KEY_UP:
          valueBox.cancelKey();
          doStep(true);
          break;
        case KeyCodes.KEY_DOWN:
          valueBox.cancelKey();
          doStep(false);
          break;
        case KeyCodes.KEY_DELETE:
        case KeyCodes.KEY_BACKSPACE:
          valueBox.cancelKey();
          
          String oldText = valueBox.getText();
          int pos = valueBox.getCursorPos();
          int sel = valueBox.getSelectionLength();
          int len = BeeUtils.length(oldText);
          
          String newText;
          if (sel > 0) {
            newText = BeeUtils.delete(oldText, pos, pos + sel);
          } else if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
            newText = (pos > 0) ? BeeUtils.delete(oldText, pos - 1, pos) : oldText;
          } else if (len > 0 && pos == len) {
            newText = BeeUtils.left(oldText, len - 1);
          } else if (len > 0) {
            newText = BeeUtils.delete(oldText, pos, pos + 1);
          } else {
            newText = oldText;
          }
          
          if (!BeeUtils.same(oldText, newText)) {
            long value = BeeUtils.isEmpty(newText) ? spinner.getMin() : BeeUtils.toLong(newText);
            spinner.updateValue(value);
          }
          break;
      }
    }
  };
  
  private KeyPressHandler keyPressHandler = new KeyPressHandler() {
    public void onKeyPress(KeyPressEvent event) {
      char charCode = event.getCharCode();
      if (charCode <= BeeConst.CHAR_SPACE) {
        return;
      }

      valueBox.cancelKey();
      if (!isEnabled()) {
        return;
      }
      
      if (!BeeUtils.isDigit(charCode)) {
        switch (charCode) {
          case BeeConst.CHAR_PLUS:
            doStep(true);
            break;
          case BeeConst.CHAR_MINUS:  
            doStep(false);
            break;
        }
        return;
      }

      String oldText = valueBox.getText();
      int pos = valueBox.getCursorPos();
      int sel = valueBox.getSelectionLength();

      int len = BeeUtils.length(oldText);
      int maxLen = valueBox.getMaxLength();
      
      String newText;
      if (sel > 0) {
        newText = BeeUtils.replace(oldText, pos, pos + sel, charCode);
      } else if (maxLen > 0 && maxLen <= len) {
        int z = BeeUtils.min(pos, len - 1);
        newText = BeeUtils.replace(oldText, z, z + 1, charCode);
      } else {
        newText = BeeUtils.insert(oldText, pos, charCode);
      }

      if (!BeeUtils.same(oldText, newText)) {
        long value = BeeUtils.toLong(newText);
        spinner.updateValue(value);
      }
    }
  };

  private MouseWheelHandler mouseWheelHandler = new MouseWheelHandler() {
    public void onMouseWheel(MouseWheelEvent event) {
      int z = event.getNativeEvent().getMouseWheelVelocityY();
      if (isEnabled() && z != 0) {
        doStep(z < 0);
      }
    }
  };
  
  public ValueSpinner(Object source) {
    this(source, 0, 0, 1, 99, false);
  }

  public ValueSpinner(Object source, int min, int max) {
    this(source, min, max, 1, 99, true);
  }

  public ValueSpinner(Object source, int min, int max, int step) {
    this(source, min, max, step, step, true);
  }

  public ValueSpinner(Object source, int min, int max, int minStep, int maxStep) {
    this(source, min, max, minStep, maxStep, true);
  }

  public ValueSpinner(Object source, int min, int max, int minStep, int maxStep, boolean constr) {
    super();
    setStylePrimaryName(STYLENAME_DEFAULT);

    this.source = source;

    valueBox = new TextBox();
    DomUtils.createId(valueBox, "spin");
    valueBox.setStyleName("valueBox");
    valueBox.addKeyDownHandler(keyDownHandler);
    valueBox.addKeyPressHandler(keyPressHandler);
    valueBox.addMouseWheelHandler(mouseWheelHandler);
    if (min >= 0 && max > min) {
      valueBox.setMaxLength(BeeUtils.toString(max).length());
    }
    add(valueBox);

    spinner = new SpinnerBase(spinnerListener, getSourceValue(), 
        min, max, minStep, maxStep, constr);
    add(spinner.getIncrementArrow());
    add(spinner.getDecrementArrow());
  }

  public void addSpinnerListener(SpinnerListener listener) {
    spinner.addSpinnerListener(listener);
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

  public void removeSpinnerListener(SpinnerListener listener) {
    spinner.removeSpinnerListener(listener);
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
  
  private void doStep(boolean incr) {
    spinner.doStep(parseValue(), incr);
  }

  private long getSourceValue() {
    return ValueUtils.getLong(source);
  }

  private long parseValue() {
    return BeeUtils.toLong(valueBox.getText());
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
    source = ValueUtils.setLong(source, value);
  }
}
