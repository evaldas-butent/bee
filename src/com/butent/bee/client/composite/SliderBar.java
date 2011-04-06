package com.butent.bee.client.composite;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RequiresResize;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Focus;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ValueUtils;

import java.util.ArrayList;
import java.util.List;

public class SliderBar extends Focus implements RequiresResize {
  public static interface LabelFormatter {
    String formatLabel(double value);
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
  
  private Object source;

  private double curValue;
  private double maxValue;
  private double minValue;
  private double stepSize;

  private BeeImage knobImage;

  private int numLabels = 0;
  private List<Element> labelElements = new ArrayList<Element>();
  private LabelFormatter labelFormatter;

  private int numTicks = 0;
  private List<Element> tickElements = new ArrayList<Element>();

  private Element lineElement;
  private int lineLeftOffset = 0;

  private boolean slidingKeyboard = false;
  private boolean slidingMouse = false;

  private boolean enabled = true;

  private KeyTimer keyTimer = new KeyTimer();

  public SliderBar(Object src, double min, double max, double step) {
    this(src, min, max, step, 0, 0);
  }

  public SliderBar(Object src, double min, double max, double step, int labels, int ticks) {
    super();
    this.source = src;
    this.curValue = ValueUtils.getDouble(src);

    this.minValue = min;
    this.maxValue = max;
    this.stepSize = step;
    
    this.numLabels = labels;
    this.numTicks = ticks;

    setStyleName(styleNameShell);

    lineElement = DOM.createDiv();
    getElement().appendChild(lineElement);
    lineElement.setClassName(styleNameLine);
    DomUtils.createId(lineElement, "line");
    lineElement.getStyle().setPosition(Position.ABSOLUTE);

    knobImage = new BeeImage(knobDefault());
    Element knobElement = knobImage.getElement();
    getElement().appendChild(knobElement);
    knobElement.setClassName(styleNameKnob);
    DomUtils.createId(knobElement, "knob");
    knobElement.getStyle().setPosition(Position.ABSOLUTE);

    sinkEvents(Event.MOUSEEVENTS | Event.ONMOUSEWHEEL | Event.KEYEVENTS | Event.FOCUSEVENTS);
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "slider");
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

  public boolean isEnabled() {
    return enabled;
  }

  public ImageResource knobDefault() {
    return Global.getImages().slider();
  }

  public ImageResource knobDisabled() {
    return Global.getImages().sliderDisabled();
  }
  
  public ImageResource knobSliding() {
    return Global.getImages().sliderSliding();
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
    redraw();
  }

  public void redraw() {
    if (isAttached()) {
      int width = getElement().getClientWidth();
      int lineWidth = lineElement.getOffsetWidth();

      lineLeftOffset = (width / 2) - (lineWidth / 2);
      StyleUtils.setLeft(lineElement, lineLeftOffset);

      drawLabels();
      drawTicks();
      drawKnob();
    }
  }

  public void setCurrentValue(double curValue) {
    this.curValue = Math.max(minValue, Math.min(maxValue, curValue));
    double remainder = (this.curValue - minValue) % stepSize;
    this.curValue -= remainder;

    if ((remainder > (stepSize / 2)) && ((this.curValue + stepSize) <= maxValue)) {
      this.curValue += stepSize;
    }

    drawKnob();
    
    source = ValueUtils.setDouble(source, this.curValue);
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      knobImage.setResource(knobDefault());
      StyleUtils.removeStyleDependentName(lineElement, StyleUtils.NAME_DISABLED);
    } else {
      knobImage.setResource(knobDisabled());
      StyleUtils.addStyleDependentName(lineElement, StyleUtils.NAME_DISABLED);
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

  public void shiftLeft(int numSteps) {
    setCurrentValue(getCurrentValue() - numSteps * stepSize);
  }

  public void shiftRight(int numSteps) {
    setCurrentValue(getCurrentValue() + numSteps * stepSize);
  }
  
  protected String formatLabel(double value) {
    if (labelFormatter != null) {
      return labelFormatter.formatLabel(value);
    } else {
      return BeeUtils.toString(BeeUtils.round(value, 1));
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
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        redraw();
      }
    });
    super.onLoad();
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
    StyleUtils.setLeft(knobElement, knobLeftOffset);
  }

  private void drawLabels() {
    if (!isAttached()) {
      return;
    }
    if (numLabels > 0) {
      int shellWidth = getElement().getClientWidth();
      int lineWidth = lineElement.getOffsetWidth();
      Element label;

      for (int i = 0; i <= numLabels; i++) {
        if (i < labelElements.size()) {
          label = labelElements.get(i);
        } else {
          label = DOM.createDiv();
          DomUtils.createId(label, "label");
          label.getStyle().setPosition(Position.ABSOLUTE);
          label.getStyle().setDisplay(Display.NONE);
          label.setClassName(styleNameLabel);
          if (!enabled) {
            StyleUtils.addStyleDependentName(label, StyleUtils.NAME_DISABLED);
          }
          getElement().appendChild(label);
          labelElements.add(label);
        }

        double value = minValue + (getTotalRange() * i / numLabels);
        label.getStyle().setVisibility(Visibility.HIDDEN);
        label.getStyle().clearDisplay();
        label.setInnerHTML(formatLabel(value));

        StyleUtils.zeroLeft(label);
        int labelWidth = label.getOffsetWidth();
        int labelLeftOffset = lineLeftOffset + (lineWidth * i / numLabels) - (labelWidth / 2);
        labelLeftOffset = Math.min(labelLeftOffset, shellWidth - labelWidth - 1);
        labelLeftOffset = Math.max(labelLeftOffset, 1);
        StyleUtils.setLeft(label, labelLeftOffset);
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
      Element tick;
      for (int i = 0; i <= numTicks; i++) {
        if (i < tickElements.size()) {
          tick = tickElements.get(i);
        } else {
          tick = DOM.createDiv();
          DomUtils.createId(tick, "tick");
          tick.getStyle().setPosition(Position.ABSOLUTE);
          tick.getStyle().setDisplay(Display.NONE);
          tick.setClassName(styleNameTick);

          getElement().appendChild(tick);
          tickElements.add(tick);
        }
        if (!enabled) {
          StyleUtils.addStyleDependentName(tick, StyleUtils.NAME_DISABLED);
        }

        tick.getStyle().setVisibility(Visibility.HIDDEN);
        tick.getStyle().clearDisplay();
        int tickWidth = tick.getOffsetWidth();
        int tickLeftOffset = lineLeftOffset + (lineWidth * i / numTicks) - (tickWidth / 2);
        tickLeftOffset = Math.min(tickLeftOffset, lineLeftOffset + lineWidth - tickWidth);
        StyleUtils.setLeft(tick, tickLeftOffset);
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
    addStyleDependentName(StyleUtils.NAME_FOCUSED);
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
      setCurrentValue(getTotalRange() * percent + minValue);
    }
  }

  private void startSliding(boolean highlight) {
    if (highlight) {
      StyleUtils.addStyleDependentName(lineElement, styleNameSliding);
      StyleUtils.addStyleDependentName(knobImage.getElement(), styleNameSliding);
      knobImage.setResource(knobSliding());
    }
  }

  private void stopSliding(boolean unhighlight) {
    if (unhighlight) {
      StyleUtils.removeStyleDependentName(lineElement, styleNameSliding);
      StyleUtils.removeStyleDependentName(knobImage.getElement(), styleNameSliding);
      knobImage.setResource(knobDefault());
    }
  }

  private void unhighlight() {
    removeStyleDependentName(StyleUtils.NAME_FOCUSED);
  }
}