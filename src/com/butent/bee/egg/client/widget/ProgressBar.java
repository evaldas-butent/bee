package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class ProgressBar extends Widget implements HasId, RequiresResize {
  public abstract static class TextFormatter {
    protected abstract String getText(ProgressBar bar, double curProgress);
  }

  private Element barElement;
  private Element textElement;

  private double curProgress;
  private double maxProgress;
  private double minProgress;

  private boolean textVisible = true;
  private TextFormatter textFormatter;

  public ProgressBar() {
    this(0.0, 100.0, 0.0);
  }

  public ProgressBar(double curProgress) {
    this(0.0, 100.0, curProgress);
  }

  public ProgressBar(double minProgress, double maxProgress) {
    this(minProgress, maxProgress, 0.0);
  }

  public ProgressBar(double minProgress, double maxProgress, double curProgress) {
    this(minProgress, maxProgress, curProgress, null);
  }

  public ProgressBar(double minProgress, double maxProgress,
      double curProgress, TextFormatter textFormatter) {
    this.minProgress = minProgress;
    this.maxProgress = maxProgress;
    this.curProgress = curProgress;
    setTextFormatter(textFormatter);

    setElement(DOM.createDiv());
    getElement().getStyle().setPosition(Style.Position.RELATIVE);
    setStyleName("bee-ProgressBar-shell");

    barElement = DOM.createDiv();
    DOM.appendChild(getElement(), barElement);
    BeeKeeper.getStyle().fullWidth(barElement);
    barElement.setClassName("bee-ProgressBar-bar");

    textElement = DOM.createDiv();
    DOM.appendChild(getElement(), textElement);
    textElement.getStyle().setPosition(Style.Position.ABSOLUTE);
    BeeKeeper.getStyle().zeroTop(textElement);
    textElement.setClassName("bee-ProgressBar-text");

    setProgress(curProgress);
  }

  public void createId() {
    DomUtils.createId(this, "progress");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public double getMaxProgress() {
    return maxProgress;
  }

  public double getMinProgress() {
    return minProgress;
  }

  public double getPercent() {
    if (maxProgress <= minProgress) {
      return 0.0;
    }

    double percent = (curProgress - minProgress) / (maxProgress - minProgress);
    return Math.max(0.0, Math.min(1.0, percent));
  }

  public double getProgress() {
    return curProgress;
  }

  public TextFormatter getTextFormatter() {
    return textFormatter;
  }

  public boolean isTextVisible() {
    return textVisible;
  }

  public void onResize() {
    int width = getElement().getClientWidth();
    if (textVisible) {
      int textWidth = textElement.getOffsetWidth();
      int left = (width / 2) - (textWidth / 2);
      BeeKeeper.getStyle().setLeft(textElement, left);
    }
  }

  public void redraw() {
    if (isAttached()) {
      onResize();
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setMaxProgress(double maxProgress) {
    this.maxProgress = maxProgress;
    curProgress = Math.min(curProgress, maxProgress);
    resetProgress();
  }

  public void setMinProgress(double minProgress) {
    this.minProgress = minProgress;
    curProgress = Math.max(curProgress, minProgress);
    resetProgress();
  }

  public void setProgress(double curProgress) {
    this.curProgress = Math.max(minProgress, Math.min(maxProgress, curProgress));

    int percent = (int) (100 * getPercent());
    barElement.getStyle().setWidth(percent, Unit.PCT);
    textElement.setInnerHTML(generateText());

    if (percent < 50) {
      textElement.addClassName("firstHalf");
    } else {
      textElement.addClassName("secondHalf");
    }

    redraw();
  }

  public void setTextFormatter(TextFormatter textFormatter) {
    this.textFormatter = textFormatter;
  }

  public void setTextVisible(boolean isVisible) {
    this.textVisible = isVisible;
    if (this.textVisible) {
      textElement.getStyle().clearDisplay();
      redraw();
    } else {
      textElement.getStyle().setDisplay(Display.NONE);
    }
  }

  protected String generateText() {
    if (textFormatter != null) {
      return textFormatter.getText(this, curProgress);
    } else {
      return (int) (100 * getPercent()) + "%";
    }
  }

  protected Element getBarElement() {
    return barElement;
  }

  protected Element getTextElement() {
    return textElement;
  }

  @Override
  protected void onLoad() {
    getElement().getStyle().setPosition(Position.RELATIVE);
    redraw();
  }

  protected void resetProgress() {
    setProgress(getProgress());
  }
} 