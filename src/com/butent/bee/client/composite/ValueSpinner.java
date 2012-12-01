package com.butent.bee.client.composite;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.HandlesAllFocusEvents;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.ui.TextBox;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ValueUtils;

/**
 * Enables to use value spinner user interface component, incrementally increasing and decreasing
 * input value.
 */

public class ValueSpinner extends Absolute {

  private static final String STYLENAME_DEFAULT = "bee-ValueSpinner";

  private SpinnerBase spinner;
  private TextBox valueBox;
  private Object source;
  private boolean focus = false;

  private SpinnerListener spinnerListener = new SpinnerListener() {
    @Override
    public void onSpinning(long value) {
      valueBox.setText(formatValue(value));
      setSourceValue(value);
    }
  };

  private KeyDownHandler keyDownHandler = new KeyDownHandler() {
    @Override
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
    @Override
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
        int z = Math.min(pos, len - 1);
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
    @Override
    public void onMouseWheel(MouseWheelEvent event) {
      int z = event.getNativeEvent().getMouseWheelVelocityY();
      if (focus && isEnabled() && z != 0) {
        doStep(z > 0);
      }
    }
  };

  private HandlesAllFocusEvents focusHandler = new HandlesAllFocusEvents() {
    @Override
    public void onBlur(BlurEvent event) {
      focus = false;
    }

    @Override
    public void onFocus(FocusEvent event) {
      focus = true;
    }
  };

  private ScheduledCommand layoutCommand = new ScheduledCommand() {
    @Override
    public void execute() {
      setPositions();
    }
  };

  public ValueSpinner(Object source, long min, long max) {
    this(source, min, max, true);
  }

  public ValueSpinner(Object source, long min, long max, boolean constrained) {
    this(source, min, max, 1, 99, constrained);
  }
  
  public ValueSpinner(Object source, long min, long max, int step) {
    this(source, min, max, step, step, true);
  }

  public ValueSpinner(Object source, long min, long max, int minStep, int maxStep) {
    this(source, min, max, minStep, maxStep, true);
  }

  public ValueSpinner(Object source, long min, long max, int minStep, int maxStep,
      boolean constrained) {
    super(Position.RELATIVE, Overflow.HIDDEN);
    setStylePrimaryName(STYLENAME_DEFAULT);

    this.source = source;

    valueBox = new TextBox();
    DomUtils.createId(valueBox, "spin");
    valueBox.setStyleName("valueBox");
    valueBox.addKeyDownHandler(keyDownHandler);
    valueBox.addKeyPressHandler(keyPressHandler);
    valueBox.addMouseWheelHandler(mouseWheelHandler);
    valueBox.addFocusHandler(focusHandler);
    valueBox.addBlurHandler(focusHandler);
    if (min >= 0 && max > min) {
      valueBox.setMaxLength(BeeUtils.toString(max).length());
    }
    add(valueBox);

    spinner = new SpinnerBase(spinnerListener, getSourceValue(),
        min, max, minStep, maxStep, constrained);
    add(spinner.getIncrementArrow());
    add(spinner.getDecrementArrow());
  }

  public void addSpinnerListener(SpinnerListener listener) {
    spinner.addSpinnerListener(listener);
  }

  @Override
  public String getIdPrefix() {
    return "spin-container";
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

  @Override
  public void onResize() {
    Scheduler.get().scheduleDeferred(layoutCommand);
  }

  public void removeSpinnerListener(SpinnerListener listener) {
    spinner.removeSpinnerListener(listener);
  }

  public void setEnabled(boolean enabled) {
    spinner.setEnabled(enabled);
    valueBox.setEnabled(enabled);
  }

  public void updateValue(long value) {
    spinner.updateValue(value);
  }

  protected String formatValue(long value) {
    return String.valueOf(value);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    Scheduler.get().scheduleDeferred(layoutCommand);
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

    int w = Math.max(incrWidth, decrWidth);
    if (panelWidth < w * 2) {
      panelWidth = Math.max(w * 2, 60);
      StyleUtils.setWidth(this, panelWidth);
    }
    int h = Math.max(boxHeight, incrHeight + decrHeight + 2);
    if (panelHeight != h) {
      StyleUtils.setHeight(this, h);
    }

    setWidgetPosition(valueBox, 0, 0);
    StyleUtils.setWidth(valueBox, panelWidth - w - 5);

    setWidgetPosition(spinner.getIncrementArrow(), panelWidth - w, 0);
    setWidgetPosition(spinner.getDecrementArrow(), panelWidth - w, h - decrHeight);
  }

  private void setSourceValue(long value) {
    source = ValueUtils.setLong(source, value);
  }
}
