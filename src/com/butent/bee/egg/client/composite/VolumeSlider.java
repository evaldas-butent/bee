package com.butent.bee.egg.client.composite;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.layout.Horizontal;
import com.butent.bee.egg.client.layout.Simple;
import com.butent.bee.egg.client.widget.ProgressBar;
import com.butent.bee.egg.shared.HasIntValue;
import com.butent.bee.egg.shared.HasLongValue;

public class VolumeSlider extends Composite {
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
    Horizontal panel = new Horizontal();
    panel.setStyleName("bee-VolumeSlider");
    panel.setVerticalAlignment(Horizontal.ALIGN_MIDDLE);
    
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
    
    Simple leftPanel = new Simple();
    leftPanel.add(spinner.getDecrementArrow());
    leftPanel.setStyleName("decreaseArrow");
    
    panel.add(leftPanel);
    panel.add(progressBar);
    BeeKeeper.getStyle().fullWidth(progressBar);
    panel.setCellWidth(progressBar, "100%");
    
    Simple rightPanel = new Simple();
    rightPanel.add(spinner.getIncrementArrow());
    rightPanel.setStyleName("increaseArrow");
    panel.add(rightPanel);
    
    initWidget(panel);
  }

  public ProgressBar getProgressBar() {
    return progressBar;
  }

  public VolumeSpinner getSpinner() {
    return spinner;
  }

  public boolean isEnabled() {
    return spinner.isEnabled();
  }

  public void setEnabled(boolean enabled) {
    spinner.setEnabled(enabled);
  }
}
