package com.butent.bee.client;

@FunctionalInterface
public interface Callback<T> {

  default void onFailure(String... reason) {
    BeeKeeper.getScreen().notifySevere(reason);
  }

  void onSuccess(T result);
}
