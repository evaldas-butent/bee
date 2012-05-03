package com.butent.bee.shared.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import com.butent.bee.shared.Assert;

import java.util.Collection;

public class XmlHelper {

  public static final String DEFAULT_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  public static final String ATTR_XMLNS = "xmlns";

  public static final char LIST_SEPARATOR = ' ';
  public static final Joiner LIST_JOINER = Joiner.on(LIST_SEPARATOR);

  public static String getList(Collection<String> items) {
    Assert.notNull(items);
    return LIST_JOINER.join(Iterables.filter(items, StringPredicate.NOT_EMPTY));
  }
  
  private XmlHelper() {
  }
}
