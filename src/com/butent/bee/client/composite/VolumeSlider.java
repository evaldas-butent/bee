package com.butent.bee.client.composite;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RequiresResize;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ValueUtils;

/**
 * Implements a volume slider user interface component (note:similar to sound level slider in MS
 * Windows).
 */

public class VolumeSlider extends Absolute implements RequiresResize {

  private class VolumeSpinner extends SpinnerBase {
    private VolumeSpinner(SpinnerListener spinnerListener, long value, long min, long max,
        int minStep, int maxStep, boolean constrained) {
      super(spinnerListener, value, min, max, minStep, maxStep, constrained);
    }

    @Override
    public ImageResource arrowDown() {
      return Global.getImages().arrowLeft();
    }

    @Override
    public ImageResource arrowDownDisabled() {
      return Global.getImages().arrowLeftDisabled();
    }

    @Override
    public ImageResource arrowDownHover() {
      return Global.getImages().arrowLeftHover();
    }

    @Override
    public ImageResource arrowDownPressed() {
      return Global.getImages().arrowLeftPressed();
    }

    @Override
    public ImageResource arrowUp() {
      return Global.getImages().arrowRight();
    }

    @Override
    public ImageResource arrowUpDisabled() {
      return Global.getImages().arrowRightDisabled();
    }

    @Override
    public ImageResource arrowUpHover() {
      return Global.getImages().arrowRightHover();
    }

    @Override
    public ImageResource arrowUpPressed() {
      return Global.getImages().arrowRightPressed();
    }
  }

  private VolumeSpinner spinner;
  private ProgressBar progressBar;
  private Object source;

  private int spacing = 5;
  private int padding = 1;

  private SpinnerListener listener = new SpinnerListener() {
    public void onSpinning(long value) {
      progressBar.setProgress(value);
      source = ValueUtils.setLong(source, value);
    }
  };

  public VolumeSlider(Object source, long min, long max) {
    this(source, min, max, 1, 5);
  }

  public VolumeSlider(Object source, long min, long max, int step) {
    this(source, min, max, step, step);
  }

  public VolumeSlider(Object source, long min, long max, int minStep, int maxStep) {
    super(Position.RELATIVE, Overflow.HIDDEN);
    setStyleName("bee-VolumeSlider");

    this.source = source;
    long value = ValueUtils.getLong(source);
    progressBar = new ProgressBar(min, max, value);
    spinner = new VolumeSpinner(listener, value, min, max, minStep, maxStep, true);

    add(spinner.getDecrementArrow());
    spinner.getDecrementArrow().setStyleName("decreaseArrow");

    add(progressBar);

    add(spinner.getIncrementArrow());
    spinner.getIncrementArrow().setStyleName("increaseArrow");

    sinkEvents(Event.ONMOUSEWHEEL);
  }
  
  @Override
  public String getIdPrefix() {
    return "volume-slider";
  }

  public int getPadding() {
    return padding;
  }

  public ProgressBar getProgressBar() {
    return progressBar;
  }

  public int getSpacing() {
    return spacing;
  }

  public VolumeSpinner getSpinner() {
    return spinner;
  }
  
  public long getValue() {
    return spinner.getValue();
  }

  public boolean isEnabled() {
    return spinner.isEnabled();
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (event.getTypeInt() == Event.ONMOUSEWHEEL && isEnabled()) {
      int z = event.getMouseWheelVelocityY();
      if (z != 0) {
        spinner.doStep(spinner.getValue(), z > 0);
        event.preventDefault();
      }
    }

    super.onBrowserEvent(event);
  }

  @Override
  public void onResize() {
    progressBar.onResize();
    redraw();
  }

  public void setEnabled(boolean enabled) {
    spinner.setEnabled(enabled);
  }

  public void setPadding(int padding) {
    this.padding = padding;
  }

  public void setSpacing(int spacing) {
    this.spacing = spacing;
  }

  @Override
  protected void onLoad() {
    redraw();
    super.onLoad();
  }

  private void redraw() {
    if (isAttached()) {
      int panelWidth = getElement().getClientWidth();
      int panelHeight = getElement().getClientHeight();

      int barWidth = progressBar.getElement().getOffsetWidth();
      int barHeight = progressBar.getElement().getOffsetHeight();

      int leftWidth = spinner.getDecrementArrow().getWidth();
      int leftHeight = spinner.getDecrementArrow().getHeight();
      int rightWidth = spinner.getIncrementArrow().getWidth();
      int rightHeight = spinner.getIncrementArrow().getHeight();

      int h = BeeUtils.max(panelHeight, barHeight, leftHeight, rightHeight,
          DomUtils.getTextBoxClientHeight());
      if (panelHeight < h) {
        StyleUtils.setHeight(this, h);
      }
      if (barHeight < h) {
        StyleUtils.setHeight(progressBar, h);
      }

      int w = leftWidth + rightWidth + spacing * 2 + padding * 2;
      if (barWidth <= 0 || panelWidth - w != barWidth) {
        barWidth = (panelWidth > w) ? panelWidth - w : w;
        StyleUtils.setWidth(progressBar, barWidth);
        progressBar.redraw();
      }
      if (panelWidth - w != barWidth) {
        panelWidth = w + barWidth;
        StyleUtils.setWidth(this, panelWidth);
      }

      setWidgetPosition(spinner.getDecrementArrow(), padding, (h - leftHeight) / 2);
      setWidgetPosition(progressBar, padding + leftWidth + spacing, 0);
      setWidgetPosition(spinner.getIncrementArrow(), panelWidth - padding - rightWidth,
          (h - rightHeight) / 2);
    }
  }
}
