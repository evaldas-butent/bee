package com.butent.bee.shared.menu;

import com.google.common.base.Function;

import java.util.List;

@FunctionalInterface
public interface MenuTransformer extends Function<Menu, List<Menu>> {
}
