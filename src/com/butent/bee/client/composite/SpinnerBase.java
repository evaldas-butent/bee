package com.butent.bee.client.composite;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.widget.BeeImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains core behaviors of spinner type user interface components.
 */
public class SpinnerBase {

  public static final int INITIAL_SPEED = 7;

  private final BeeImage decrementArrow;
  private final BeeImage incrementArrow;

  private List<SpinnerListener> spinnerListeners = new ArrayList<SpinnerListener>();

  private int step, minStep, maxStep;
  private int initialSpeed;

  private long value, min, max;
  private boolean constrained;

  private boolean enabled = true;
  private boolean increment;

  private final Timer timer = new Timer() {
    private int counter = 0;
    private int speed = initialSpeed;

    @Override
    public void cancel() {
      super.cancel();
      speed = initialSpeed;
      counter = 0;
    }

    @Override
    public void run() {
      counter++;
      if (speed <= 0 || counter % speed == 0) {
        speed--;
        counter = 0;
        if (increment) {
          increase();
        } else {
          decrease();
        }
      }
      if (speed < 0 && step < maxStep) {
        step++;
      }
    }
  };

  private MouseDownHandler mouseDownHandler = new MouseDownHandler() {
    public void onMouseDown(MouseDownEvent event) {
      if (enabled) {
        BeeImage sender = (BeeImage) event.getSource();
        if (sender == incrementArrow) {
          sender.setResource(arrowUpPressed());
          increment = true;
          increase();
        } else {
          sender.setResource(arrowDownPressed());
          increment = false;
          decrease();
        }
        timer.scheduleRepeating(30);
      }
    }
  };

  private MouseOverHandler mouseOverHandler = new MouseOverHandler() {
    public void onMouseOver(MouseOverEvent event) {
      if (enabled) {
        BeeImage sender = (BeeImage) event.getSource();
        if (sender == incrementArrow) {
          sender.setResource(arrowUpHover());
        } else {
          sender.setResource(arrowDownHover());
        }
      }
    }
  };

  private MouseOutHandler mouseOutHandler = new MouseOutHandler() {
    public void onMouseOut(MouseOutEvent event) {
      if (enabled) {
        cancelTimer((Widget) event.getSource());
      }
    }
  };

  private MouseUpHandler mouseUpHandler = new MouseUpHandler() {
    public void onMouseUp(MouseUpEvent event) {
      if (enabled) {
        cancelTimer((Widget) event.getSource());
      }
    }
  };

  public SpinnerBase(SpinnerListener spinner, long value) {
    this(spinner, value, 0, 0, 1, 99, false);
  }

  public SpinnerBase(SpinnerListener spinner, long value, long min, long max) {
    this(spinner, value, min, max, 1, 99, true);
  }

  public SpinnerBase(SpinnerListener spinner, long value, long min, long max, int step) {
    this(spinner, value, min, max, step, step, true);
  }

  public SpinnerBase(SpinnerListener spinner, long value, long min, long max,
        int minStep, int maxStep) {
    this(spinner, value, min, max, minStep, maxStep, true);
  }

  public SpinnerBase(SpinnerListener spinner, long value, long min, long max,
        int minStep, int maxStep, boolean constrained) {
    super();
    spinnerListeners.add(spinner);

    this.value = value;
    this.min = min;
    this.max = max;

    this.step = minStep;
    this.minStep = minStep;
    this.maxStep = maxStep;
    this.constrained = constrained;

    this.initialSpeed = INITIAL_SPEED;

    incrementArrow = new BeeImage(arrowUp());
    incrementArrow.addMouseUpHandler(mouseUpHandler);
    incrementArrow.addMouseDownHandler(mouseDownHandler);
    incrementArrow.addMouseOverHandler(mouseOverHandler);
    incrementArrow.addMouseOutHandler(mouseOutHandler);

    decrementArrow = new BeeImage(arrowDown());
    decrementArrow.addMouseUpHandler(mouseUpHandler);
    decrementArrow.addMouseDownHandler(mouseDownHandler);
    decrementArrow.addMouseOverHandler(mouseOverHandler);
    decrementArrow.addMouseOutHandler(mouseOutHandler);

    fireOnValueChanged();
  }

  public void addSpinnerListener(SpinnerListener listener) {
    spinnerListeners.add(listener);
  }

  public ImageResource arrowDown() {
    return Global.getImages().arrowDown();
  }

  public ImageResource arrowDownDisabled() {
    return Global.getImages().arrowDownDisabled();
  }

  public ImageResource arrowDownHover() {
    return Global.getImages().arrowDownHover();
  }

  public ImageResource arrowDownPressed() {
    return Global.getImages().arrowDownPressed();
  }

  public ImageResource arrowUp() {
    return Global.getImages().arrowUp();
  }

  public ImageResource arrowUpDisabled() {
    return Global.getImages().arrowUpDisabled();
  }

  public ImageResource arrowUpHover() {
    return Global.getImages().arrowUpHover();
  }

  public ImageResource arrowUpPressed() {
    return Global.getImages().arrowUpPressed();
  }

  public void doStep(long v, boolean incr) {
    if (incr) {
      if (v < getMin() || v >= getMax()) {
        v = getMin();
      } else {
        v += getMinStep();
        if (v > getMax()) {
          v = getMax();
        }
      }
    } else {
      if (v <= getMin() || v > getMax()) {
        v = getMax();
      } else {
        v -= getMinStep();
        if (v < getMin()) {
          v = getMin();
        }
      }
    }
    updateValue(v);
  }

  public BeeImage getDecrementArrow() {
    return decrementArrow;
  }

  public BeeImage getIncrementArrow() {
    return incrementArrow;
  }

  public long getMax() {
    return max;
  }

  public int getMaxStep() {
    return maxStep;
  }

  public long getMin() {
    return min;
  }

  public int getMinStep() {
    return minStep;
  }

  public long getValue() {
    return value;
  }

  public boolean isConstrained() {
    return constrained;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isValid(long v) {
    if (isConstrained()) {
      return v >= getMin() && v <= getMax();
    } else {
      return true;
    }
  }

  public void removeSpinnerListener(SpinnerListener listener) {
    spinnerListeners.remove(listener);
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      incrementArrow.setResource(arrowUp());
      decrementArrow.setResource(arrowDown());
    } else {
      incrementArrow.setResource(arrowUpDisabled());
      decrementArrow.setResource(arrowDownDisabled());
    }
    if (!enabled) {
      timer.cancel();
    }
  }

  public void setInitialSpeed(int initialSpeed) {
    this.initialSpeed = initialSpeed;
  }

  public void setMax(long max) {
    this.max = max;
  }

  public void setMaxStep(int maxStep) {
    this.maxStep = maxStep;
  }

  public void setMin(long min) {
    this.min = min;
  }

  public void setMinStep(int minStep) {
    this.minStep = minStep;
  }

  public void setValue(long value, boolean fireEvent) {
    this.value = value;
    if (fireEvent) {
      fireOnValueChanged();
    }
  }

  public void updateValue(long v) {
    if (isValid(v)) {
      setValue(v, true);
    }
  }

  protected void decrease() {
    value -= step;
    if (constrained && value < min) {
      value = min;
      timer.cancel();
    }
    fireOnValueChanged();
  }

  protected void increase() {
    value += step;
    if (constrained && value > max) {
      value = max;
      timer.cancel();
    }
    fireOnValueChanged();
  }

  private void cancelTimer(Widget sender) {
    step = minStep;
    if (sender == incrementArrow) {
      ((BeeImage) sender).setResource(arrowUp());
    } else {
      ((BeeImage) sender).setResource(arrowDown());
    }
    timer.cancel();
  }

  private void fireOnValueChanged() {
    for (SpinnerListener listener : spinnerListeners) {
      listener.onSpinning(value);
    }
  }
}
