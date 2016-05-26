package com.butent.bee.client.js;

import jsinterop.annotations.JsFunction;

@JsFunction
@FunctionalInterface
public interface JsConsumer<T> {
  void accept(T t);
}
