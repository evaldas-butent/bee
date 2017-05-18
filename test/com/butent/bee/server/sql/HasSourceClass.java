package com.butent.bee.server.sql;

import com.butent.bee.server.sql.HasSource;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Object for testing {@link com.butent.bee.server.sql.HasSource}.
 */
public class HasSourceClass implements HasSource {

  Collection<String> a;

  public HasSourceClass() {
  }

  @Override
  public Collection<String> getSources() {
    a = new ArrayList<>();
    a.add("sql");
    a.add("test");
    return null;
  }

}
