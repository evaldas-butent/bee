package com.butent.bee.egg.client.composite;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RequiresResize;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.EventUtils;
import com.butent.bee.egg.client.layout.Absolute;
import com.butent.bee.egg.shared.HasIntValue;
import com.butent.bee.egg.shared.HasLongValue;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class VolumeSlider extends Absolute implements RequiresResize {
  private class VolumeSpinner extends SpinnerBase {
    private VolumeSpinner(SpinnerListener spinner, long value, long min, long max, int minStep,
        int maxStep, boolean constrained) {
      super(spinner, value, min, max, minStep, maxStep, constrained);
    }

    @Override
    public ImageResource arrowDown() {
      return BeeGlobal.getImages().arrowLeft();
    }

    @Override
    public ImageResource arrowDownDisabled() {
      return BeeGlobal.getImages().arrowLeftDisabled();
    }

    @Override
    public ImageResource arrowDownHover() {
      return BeeGlobal.getImages().arrowLeftHover();
    }

    @Override
    public ImageResource arrowDownPressed() {
      return BeeGlobal.getImages().arrowLeftPressed();
    }

    @Override
    public ImageResource arrowUp() {
      return BeeGlobal.getImages().arrowRight();
    }

    @Override
    public ImageResource arrowUpDisabled() {
      return BeeGlobal.getImages().arrowRightDisabled();
    }

    @Override
    public ImageResource arrowUpHover() {
      return BeeGlobal.getImages().arrowRightHover();
    }

    @Override
    public ImageResource arrowUpPressed() {
      return BeeGlobal.getImages().arrowRightPressed();
    }
  }

  private VolumeSpinner spinner;
  private ProgressBar progressBar;
  private HasIntValue source;
  
  private int spacing = 5;
  private int padding = 1;

  private SpinnerListener listener = new SpinnerListener() {
    public void onSpinning(long value) {
      progressBar.setProgress(value);
      if (source instanceof HasLongValue) {
        ((HasLongValue) source).setValue(value);
      } else if (source != null) {
        source.setValue((int) value);
      }
    }
  };

  public VolumeSlider(HasIntValue source, long min, long max) {
    this(source, min, max, 1, 5);
  }

  public VolumeSlider(HasIntValue source, long min, long max, int step) {
    this(source, min, max, step, step);
  }

  public VolumeSlider(HasIntValue source, long min, long max, int minStep, int maxStep) {
    setStyleName("bee-VolumeSlider");

    long value;
    if (source == null) {
      value = 0;
    } else if (source instanceof HasLongValue) {
      value = ((HasLongValue) source).getLong();
    } else {
      value = source.getInt();
    }

    progressBar = new ProgressBar(min, max, value);
    spinner = new VolumeSpinner(listener, value, min, max, minStep, maxStep, true);

    add(spinner.getDecrementArrow());
    spinner.getDecrementArrow().setStyleName("decreaseArrow");

    add(progressBar);

    add(spinner.getIncrementArrow());
    spinner.getIncrementArrow().setStyleName("increaseArrow");
    
    sinkEvents(Event.ONMOUSEWHEEL);
  }

  public void createId() {
    DomUtils.createId(this, "volume-slider");
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

  public boolean isEnabled() {
    return spinner.isEnabled();
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (event.getTypeInt() == Event.ONMOUSEWHEEL && isEnabled()) {
      int z = event.getMouseWheelVelocityY();
      if (z != 0) {
        spinner.doStep(spinner.getValue(), z < 0);
        EventUtils.eatEvent(event);
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
      
      int h = BeeUtils.max(panelHeight, barHeight, leftHeight, rightHeight);
      if (panelHeight < h) {
        DomUtils.setHeight(this, h);
      }
      if (barHeight < h) {
        DomUtils.setHeight(progressBar, h);
      }
      
      int w = leftWidth + rightWidth + spacing * 2 + padding * 2;
      if (barWidth <= 0 || panelWidth - w != barWidth) {
        barWidth = (panelWidth > w) ? panelWidth - w : w;
        DomUtils.setWidth(progressBar, barWidth);
        progressBar.redraw();
      }
      if (panelWidth - w != barWidth) {
        panelWidth = w + barWidth;
        DomUtils.setWidth(this, panelWidth);
      }
      
      setWidgetPosition(spinner.getDecrementArrow(), padding, (h - leftHeight) / 2);
      setWidgetPosition(progressBar, padding + leftWidth + spacing, 0);
      setWidgetPosition(spinner.getIncrementArrow(), panelWidth - padding - rightWidth,
          (h - rightHeight) / 2);
    }
  }
}
