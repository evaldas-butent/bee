package com.butent.bee.client.animation;

import com.google.gwt.user.client.Timer;

import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;

public interface HasAnimatableActivity extends IdentifiableWidget {

  default void enableAnimation(int duration) {
    if (duration > 0) {
      setAnimationDuration(duration);
      addStyleName(StyleUtils.NAME_ANIMATE_ACTIVE);
    }
  }

  int getAnimationDuration();

  Timer getAnimationTimer();

  double getSensitivityRatio();

  default boolean isAnimationEnabled() {
    return getElement().hasClassName(StyleUtils.NAME_ANIMATE_ACTIVE);
  }

  default void maybeAnimate() {
    if (isAnimationEnabled()) {
      startAnimation();
      scheduleAnimationEnd();
    }
  }

  default void scheduleAnimationEnd() {
    if (getAnimationDuration() > 0) {
      if (getAnimationTimer() == null) {
        setAnimationTimer(new Timer() {
          @Override
          public void run() {
            removeStyleName(StyleUtils.NAME_ACTIVE);
          }
        });
      }

      getAnimationTimer().schedule(getAnimationDuration());
    }
  }

  void setAnimationDuration(int animationDuration);

  void setAnimationTimer(Timer animationTimer);

  default void startAnimation() {
    StyleUtils.restartAnimation(getElement(), StyleUtils.NAME_ACTIVE);
  }
}
