package com.butent.bee.shared.menu;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface MenuTransformer extends Function<Menu, List<Menu>> {
}
