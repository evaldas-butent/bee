package com.butent.bee.egg.client.composite;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RequiresResize;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.BeeStyle;
import com.butent.bee.egg.client.widget.BeeImage;

import java.util.ArrayList;
import java.util.List;

public class SliderBar extends FocusPanel implements RequiresResize, HasValue<Double> {
  public static interface LabelFormatter {
    String formatLabel(SliderBar slider, double value);
  }

  private class KeyTimer extends Timer {
    private boolean firstRun = true;
    private int repeatDelay = 30;
    private boolean shiftRight = false;
    private int multiplier = 1;

    @Override
    public void run() {
      if (firstRun) {
        firstRun = false;
        startSliding(true);
      }

      if (shiftRight) {
        setCurrentValue(curValue + multiplier * stepSize);
      } else {
        setCurrentValue(curValue - multiplier * stepSize);
      }

      schedule(repeatDelay);
    }

    public void schedule(int delayMillis, boolean right, int multi) {
      firstRun = true;
      this.shiftRight = right;
      this.multiplier = multi;
      super.schedule(delayMillis);
    }
  }
  
  private String styleNameShell = "bee-SliderBar-shell"; 
  private String styleNameLine = "bee-SliderBar-line";
  private String styleNameKnob = "bee-SliderBar-knob";
  private String styleNameLabel = "bee-SliderBar-label";
  private String styleNameTick = "bee-SliderBar-tick";

  private String styleNameSliding = "sliding";  

  private double curValue;

  private BeeImage knobImage;

  private KeyTimer keyTimer = new KeyTimer();

  private List<Element> labelElements = new ArrayList<Element>();

  private LabelFormatter labelFormatter;

  private Element lineElement;

  private int lineLeftOffset = 0;

  private double maxValue;
  private double minValue;

  private int numLabels = 0;
  private int numTicks = 0;

  private boolean slidingKeyboard = false;
  private boolean slidingMouse = false;

  private boolean enabled = true;

  private double stepSize;

  private List<Element> tickElements = new ArrayList<Element>();

  public SliderBar(double minValue, double maxValue) {
    this(minValue, maxValue, null);
  }

  public SliderBar(double minValue, double maxValue, LabelFormatter labelFormatter) {
    super();
    this.minValue = minValue;
    this.maxValue = maxValue;
    setLabelFormatter(labelFormatter);

    setStyleName(styleNameShell);

    lineElement = DOM.createDiv();
    getElement().appendChild(lineElement);
    lineElement.setClassName(styleNameLine);

    knobImage = new BeeImage(knobDefault());
    Element knobElement = knobImage.getElement();
    getElement().appendChild(knobElement);
    knobElement.setClassName(styleNameKnob);

    sinkEvents(Event.MOUSEEVENTS | Event.KEYEVENTS | Event.FOCUSEVENTS);
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Double> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public double getCurrentValue() {
    return curValue;
  }

  public LabelFormatter getLabelFormatter() {
    return labelFormatter;
  }

  public double getMaxValue() {
    return maxValue;
  }

  public double getMinValue() {
    return minValue;
  }

  public int getNumLabels() {
    return numLabels;
  }

  public int getNumTicks() {
    return numTicks;
  }

  public double getStepSize() {
    return stepSize;
  }

  public double getTotalRange() {
    if (minValue > maxValue) {
      return 0;
    } else {
      return maxValue - minValue;
    }
  }

  public Double getValue() {
    return curValue;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public ImageResource knobDefault() {
    return BeeGlobal.getImages().slider();
  }

  public ImageResource knobDisabled() {
    return BeeGlobal.getImages().sliderDisabled();
  }
  
  public ImageResource knobSliding() {
    return BeeGlobal.getImages().sliderSliding();
  }
  
  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    if (enabled) {
      switch (event.getTypeInt()) {
        case Event.ONBLUR:
          keyTimer.cancel();
          if (slidingMouse) {
            DOM.releaseCapture(getElement());
            slidingMouse = false;
            slideKnob(event);
            stopSliding(true);
          } else if (slidingKeyboard) {
            slidingKeyboard = false;
            stopSliding(true);
          }
          unhighlight();
          break;

        case Event.ONFOCUS:
          highlight();
          break;

        case Event.ONMOUSEWHEEL:
          int velocityY = event.getMouseWheelVelocityY();
          event.preventDefault();
          if (velocityY > 0) {
            shiftRight(1);
          } else {
            shiftLeft(1);
          }
          break;

        case Event.ONKEYDOWN:
          if (!slidingKeyboard) {
            int multiplier = 1;
            if (event.getCtrlKey()) {
              multiplier = (int) (getTotalRange() / stepSize / 10);
            }

            switch (event.getKeyCode()) {
              case KeyCodes.KEY_HOME:
                event.preventDefault();
                setCurrentValue(minValue);
                break;
              case KeyCodes.KEY_END:
                event.preventDefault();
                setCurrentValue(maxValue);
                break;
              case KeyCodes.KEY_LEFT:
                event.preventDefault();
                slidingKeyboard = true;
                startSliding(false);
                shiftLeft(multiplier);
                keyTimer.schedule(400, false, multiplier);
                break;
              case KeyCodes.KEY_RIGHT:
                event.preventDefault();
                slidingKeyboard = true;
                startSliding(false);
                shiftRight(multiplier);
                keyTimer.schedule(400, true, multiplier);
                break;
              case 32:
                event.preventDefault();
                setCurrentValue(minValue + getTotalRange() / 2);
                break;
            }
          }
          break;

        case Event.ONKEYUP:
          keyTimer.cancel();
          if (slidingKeyboard) {
            slidingKeyboard = false;
            stopSliding(true);
          }
          break;

        case Event.ONMOUSEDOWN:
          setFocus(true);
          slidingMouse = true;
          DOM.setCapture(getElement());
          startSliding(true);
          event.preventDefault();
          slideKnob(event);
          break;

        case Event.ONMOUSEUP:
          if (slidingMouse) {
            DOM.releaseCapture(getElement());
            slidingMouse = false;
            slideKnob(event);
            stopSliding(true);
          }
          break;

        case Event.ONMOUSEMOVE:
          if (slidingMouse) {
            slideKnob(event);
          }
          break;
      }
    }
  }

  public void onResize() {
    int width = getElement().getClientWidth();
    int lineWidth = lineElement.getOffsetWidth();
    lineLeftOffset = (width / 2) - (lineWidth / 2);
    BeeKeeper.getStyle().setLeft(lineElement, lineLeftOffset);

    drawLabels();
    drawTicks();
    drawKnob();
  }

  public void redraw() {
    if (isAttached()) {
      onResize();
    }
  }

  public void setCurrentValue(double curValue) {
    setCurrentValue(curValue, true);
  }

  public void setCurrentValue(double curValue, boolean fireEvent) {
    this.curValue = Math.max(minValue, Math.min(maxValue, curValue));
    double remainder = (this.curValue - minValue) % stepSize;
    this.curValue -= remainder;

    if ((remainder > (stepSize / 2)) && ((this.curValue + stepSize) <= maxValue)) {
      this.curValue += stepSize;
    }

    drawKnob();
    
    if (fireEvent) {
      ValueChangeEvent.fire(this, this.curValue);
    }
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      knobImage.setResource(knobDefault());
      BeeKeeper.getStyle().removeStyleDependentName(lineElement, BeeStyle.NAME_DISABLED);
    } else {
      knobImage.setResource(knobDisabled());
      BeeKeeper.getStyle().addStyleDependentName(lineElement, BeeStyle.NAME_DISABLED);
    }
    redraw();
  }

  public void setLabelFormatter(LabelFormatter labelFormatter) {
    this.labelFormatter = labelFormatter;
  }

  public void setMaxValue(double maxValue) {
    this.maxValue = maxValue;
    drawLabels();
    resetCurrentValue();
  }

  public void setMinValue(double minValue) {
    this.minValue = minValue;
    drawLabels();
    resetCurrentValue();
  }

  public void setNumLabels(int numLabels) {
    this.numLabels = numLabels;
    drawLabels();
  }

  public void setNumTicks(int numTicks) {
    this.numTicks = numTicks;
    drawTicks();
  }

  public void setStepSize(double stepSize) {
    this.stepSize = stepSize;
    resetCurrentValue();
  }

  public void setValue(Double value) {
    setCurrentValue(value, false);
  }

  public void setValue(Double value, boolean fireEvent) {
    setCurrentValue(value, fireEvent);
  }

  public void shiftLeft(int numSteps) {
    setCurrentValue(getCurrentValue() - numSteps * stepSize);
  }

  public void shiftRight(int numSteps) {
    setCurrentValue(getCurrentValue() + numSteps * stepSize);
  }
  
  protected String formatLabel(double value) {
    if (labelFormatter != null) {
      return labelFormatter.formatLabel(this, value);
    } else {
      return (int) (10 * value) / 10.0 + "";
    }
  }

  protected double getKnobPercent() {
    if (maxValue <= minValue) {
      return 0;
    }

    double percent = (curValue - minValue) / (maxValue - minValue);
    return Math.max(0.0, Math.min(1.0, percent));
  }

  @Override
  protected void onLoad() {
    redraw();
  }

  private void drawKnob() {
    if (!isAttached()) {
      return;
    }

    Element knobElement = knobImage.getElement();
    int lineWidth = lineElement.getOffsetWidth();
    int knobWidth = knobElement.getOffsetWidth();
    int knobLeftOffset = (int) (lineLeftOffset + (getKnobPercent() * lineWidth) - (knobWidth / 2));
    knobLeftOffset = Math.min(knobLeftOffset, lineLeftOffset + lineWidth - (knobWidth / 2) - 1);
    BeeKeeper.getStyle().setLeft(knobElement, knobLeftOffset);
  }

  private void drawLabels() {
    if (!isAttached()) {
      return;
    }

    int lineWidth = lineElement.getOffsetWidth();
    if (numLabels > 0) {
      for (int i = 0; i <= numLabels; i++) {
        Element label = null;
        if (i < labelElements.size()) {
          label = labelElements.get(i);
        } else {
          label = DOM.createDiv();
          label.getStyle().setDisplay(Display.NONE);
          label.setClassName(styleNameLabel);
          if (!enabled) {
            BeeKeeper.getStyle().addStyleDependentName(label, BeeStyle.NAME_DISABLED);
          }
          getElement().appendChild(label);
          labelElements.add(label);
        }

        double value = minValue + (getTotalRange() * i / numLabels);
        label.getStyle().setVisibility(Visibility.HIDDEN);
        label.getStyle().clearDisplay();
        label.setInnerHTML(formatLabel(value));

        BeeKeeper.getStyle().zeroLeft(label);

        int labelWidth = label.getOffsetWidth();
        int labelLeftOffset = lineLeftOffset + (lineWidth * i / numLabels) - (labelWidth / 2);
        labelLeftOffset = Math.min(labelLeftOffset, lineLeftOffset + lineWidth - labelWidth);
        labelLeftOffset = Math.max(labelLeftOffset, lineLeftOffset);
        BeeKeeper.getStyle().setLeft(label, labelLeftOffset);
        label.getStyle().setVisibility(Visibility.VISIBLE);
      }

      for (int i = (numLabels + 1); i < labelElements.size(); i++) {
        labelElements.get(i).getStyle().setDisplay(Display.NONE);
      }
    } else {
      for (Element elem : labelElements) {
        elem.getStyle().setDisplay(Display.NONE);
      }
    }
  }

  private void drawTicks() {
    if (!isAttached()) {
      return;
    }

    int lineWidth = lineElement.getOffsetWidth();
    if (numTicks > 0) {
      for (int i = 0; i <= numTicks; i++) {
        Element tick = null;
        if (i < tickElements.size()) {
          tick = tickElements.get(i);
        } else {
          tick = DOM.createDiv();
          tick.getStyle().setDisplay(Display.NONE);
          getElement().appendChild(tick);
          tickElements.add(tick);
        }
        tick.setClassName(styleNameTick);
        if (!enabled) {
          BeeKeeper.getStyle().addStyleDependentName(tick, BeeStyle.NAME_DISABLED);
        }
        tick.getStyle().setVisibility(Visibility.HIDDEN);
        tick.getStyle().clearDisplay();
        int tickWidth = tick.getOffsetWidth();
        int tickLeftOffset = lineLeftOffset + (lineWidth * i / numTicks) - (tickWidth / 2);
        tickLeftOffset = Math.min(tickLeftOffset, lineLeftOffset + lineWidth - tickWidth);
        BeeKeeper.getStyle().setLeft(tick, tickLeftOffset);
        tick.getStyle().setVisibility(Visibility.VISIBLE);
      }

      for (int i = (numTicks + 1); i < tickElements.size(); i++) {
        tickElements.get(i).getStyle().setDisplay(Display.NONE);
      }
    } else {
      for (Element elem : tickElements) {
        elem.getStyle().setDisplay(Display.NONE);
      }
    }
  }

  private void highlight() {
    addStyleDependentName(BeeStyle.NAME_FOCUSED);
  }

  private void resetCurrentValue() {
    setCurrentValue(getCurrentValue());
  }

  private void slideKnob(Event event) {
    int x = event.getClientX();
    if (x > 0) {
      int lineWidth = lineElement.getOffsetWidth();
      int lineLeft = lineElement.getAbsoluteLeft();
      double percent = (double) (x - lineLeft) / lineWidth * 1.0;
      setCurrentValue(getTotalRange() * percent + minValue, true);
    }
  }

  private void startSliding(boolean highlight) {
    if (highlight) {
      BeeKeeper.getStyle().addStyleDependentName(lineElement, styleNameSliding);
      BeeKeeper.getStyle().addStyleDependentName(knobImage.getElement(), styleNameSliding);
      knobImage.setResource(knobSliding());
    }
  }

  private void stopSliding(boolean unhighlight) {
    if (unhighlight) {
      BeeKeeper.getStyle().removeStyleDependentName(lineElement, styleNameSliding);
      BeeKeeper.getStyle().removeStyleDependentName(knobImage.getElement(), styleNameSliding);
      knobImage.setResource(knobDefault());
    }
  }

  private void unhighlight() {
    removeStyleDependentName(BeeStyle.NAME_FOCUSED);
  }
}