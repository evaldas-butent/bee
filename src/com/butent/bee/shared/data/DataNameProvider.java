package com.butent.bee.shared.data;

import java.util.Set;
import java.util.function.Function;

@FunctionalInterface
public interface DataNameProvider extends Function<String, Set<String>> {
}
