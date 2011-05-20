package com.butent.bee.client.utils;

import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Timer;

public abstract class Animation {

  public static final int DEFAULT_FRAME_DELAY = 25;

  private Timer timer = new Timer() {
    @Override
    public void run() {
      if (!update(Duration.currentTimeMillis())) {
        schedule(DEFAULT_FRAME_DELAY);
      }
    }
  };

  private boolean running = false;
  private boolean started = false;

  private double startTime = -1;
  private int duration = -1;

  public void cancel() {
    if (!running) {
      return;
    }

    onCancel();
    started = false;
    running = false;
  }

  public void run(int durationMillis) {
    run(durationMillis, Duration.currentTimeMillis());
  }

  public void run(int durationMillis, double startMillis) {
    cancel();

    this.running = true;
    this.duration = durationMillis;
    this.startTime = startMillis;

    if (!update(Duration.currentTimeMillis())) {
      timer.schedule(DEFAULT_FRAME_DELAY);
    }
  }

  protected double interpolate(double progress) {
    return (1 + Math.cos(Math.PI + progress * Math.PI)) / 2;
  }

  protected void onCancel() {
  }

  protected void onComplete() {
    onUpdate(interpolate(1.0));
  }

  protected void onStart() {
    onUpdate(interpolate(0.0));
  }

  protected abstract void onUpdate(double progress);

  private boolean update(double curTime) {
    boolean finished = curTime >= startTime + duration;

    if (started && !finished) {
      double progress = (curTime - startTime) / duration;
      onUpdate(interpolate(progress));
      return false;
    }

    if (!started && curTime >= startTime) {
      started = true;
      onStart();
    }

    if (finished) {
      onComplete();
      started = false;
      running = false;
      return true;
    }
    return false;
  }
}
