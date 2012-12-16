package com.butent.bee.client.layout;

import com.butent.bee.client.layout.Layout.AnimationCallback;

public interface AnimatedLayout {

  void animate(int duration);

  void animate(int duration, AnimationCallback callback);

  void forceLayout();
}
