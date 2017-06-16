package com.butent.bee.client.animation;

import com.google.gwt.user.client.Timer;

import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;

import java.util.function.Consumer;

public interface HasAnimatableActivity extends IdentifiableWidget {

  default void disableAnimation() {
    stop();

    setAnimationDuration(BeeConst.UNDEF);
    removeStyleName(StyleUtils.NAME_ANIMATE_ACTIVE);
  }

  default void enableAnimation(int duration) {
    if (duration > 0) {
      setAnimationDuration(duration);
      addStyleName(StyleUtils.NAME_ANIMATE_ACTIVE);
    }
  }

  int getAnimationDuration();

  Timer getAnimationTimer();

  default RowCallback getRowCallback(Consumer<BeeRow> consumer) {
    return getRowCallback(consumer, false);
  }

  default RowCallback getRowCallback(Consumer<BeeRow> consumer, boolean stopOnSuccess) {
    return new RowCallback() {
      @Override
      public void onCancel() {
        RowCallback.super.onCancel();
        stop();
      }

      @Override
      public void onFailure(String... reason) {
        RowCallback.super.onFailure(reason);
        stop();
      }

      @Override
      public void onSuccess(BeeRow result) {
        if (consumer != null) {
          consumer.accept(result);
        }

        if (stopOnSuccess) {
          stop();
        }
      }
    };
  }

  double getSensitivityRatio();

  default boolean isAnimationEnabled() {
    return getAnimationDuration() > 0 && getElement().hasClassName(StyleUtils.NAME_ANIMATE_ACTIVE);
  }

  default boolean isAnimationRunning() {
    return getElement().hasClassName(StyleUtils.NAME_ACTIVE);
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
            cancelAnimation();
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

  default void stop() {
    if (isAnimationRunning()) {
      cancelAnimation();
    }

    if (getAnimationTimer() != null && getAnimationTimer().isRunning()) {
      getAnimationTimer().cancel();
    }
  }

  default void cancelAnimation() {
    removeStyleName(StyleUtils.NAME_ACTIVE);
  }
}
