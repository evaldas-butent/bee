package com.butent.bee.client.animation;

import elemental.client.Browser;
import elemental.dom.RequestAnimationFrameCallback;

public abstract class RafCallback implements RequestAnimationFrameCallback {
  
  private static void request(RequestAnimationFrameCallback callback) {
    Browser.getWindow().webkitRequestAnimationFrame(callback);
  }

  protected double duration;
  
  private double startTime;

  public RafCallback(double duration) {
    this.duration = duration;
  }
  
  public double getDuration() {
    return duration;
  }

  public double getStartTime() {
    return startTime;
  }

  public void onComplete() {
  }
  
  @Override
  public boolean onRequestAnimationFrameCallback(double time) {
    double elapsed = time - startTime;

    if (elapsed <= duration && run(elapsed)) {
      request(this);
    } else {
      onComplete();
    }
    return true;
  }

  public abstract boolean run(double elapsed);

  public void setDuration(double duration) {
    this.duration = duration;
  }

  public void start() {
    request(new RequestAnimationFrameCallback() {
      @Override
      public boolean onRequestAnimationFrameCallback(double time) {
        RafCallback.this.setStartTime(time);
        request(RafCallback.this);
        return false;
      }
    });
  }

  private void setStartTime(double startTime) {
    this.startTime = startTime;
  }
}
