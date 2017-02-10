package com.butent.bee.shared.utils;

import com.google.common.base.Joiner;

import com.butent.bee.shared.Assert;

import java.util.Collection;
import java.util.Map;

public final class XmlHelper {

  public static final String DEFAULT_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  public static final String ATTR_XMLNS = "xmlns";

  public static final char LIST_SEPARATOR = ' ';
  public static final Joiner LIST_JOINER = Joiner.on(LIST_SEPARATOR);

  public static String getAttribute(Map<String, String> attributes, String name) {
    return (attributes == null) ? null : attributes.get(name);
  }

  public static String getList(Collection<String> items) {
    Assert.notNull(items);
    return LIST_JOINER.join(items.stream().filter(StringPredicate.NOT_EMPTY).iterator());
  }

  private XmlHelper() {
  }
}
