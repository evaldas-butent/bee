package com.butent.bee.client.composite;

/**
 * Requires that any spinner classes would contains {@code onSpinning} method.
 */
public interface SpinnerListener {
  void onSpinning(long value);
}
