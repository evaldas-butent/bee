package com.butent.bee.shared.data;

import java.util.function.Function;

@FunctionalInterface
public interface RowFunction<T> extends Function<IsRow, T> {
}
