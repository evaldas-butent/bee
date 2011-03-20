package com.butent.bee.server.io;

import com.google.common.collect.Sets;

import com.butent.bee.server.io.NameUtils.Component;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Wildcards;
import com.butent.bee.shared.utils.Wildcards.Pattern;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class WildcardFilter extends AbstractFileFilter {
  private Pattern[] patterns;
  private Component component;

  public WildcardFilter(String expr) {
    this(expr, null, null);
  }

  public WildcardFilter(String expr, Boolean sensitive) {
    this(expr, null, sensitive);
  }
  
  public WildcardFilter(Collection<String> like) {
    this(like, null, null);
  }
  
  public WildcardFilter(Collection<String> like, Boolean sensitive) {
    this(like, null, sensitive);
  }
  
  public WildcardFilter(String expr, Component component) {
    this(expr, component, null);
  }
  
  public WildcardFilter(String expr, Component component, Boolean sensitive) {
    this(Sets.newHashSet(expr), component, sensitive);
  }
  
  public WildcardFilter(Collection<String> like, Component component) {
    this(like, component, null);
  }
  
  public WildcardFilter(Collection<String> like, Component component, Boolean sensitive) {
    patterns = new Pattern[like.size()];
    int i = 0;
    Pattern p;
    for (String expr : like) {
      if (sensitive == null) {
        p = Wildcards.getFsPattern(expr);
      } else {
        p = Wildcards.getFsPattern(expr, sensitive);
      }
      patterns[i++] = p;
    }
    this.component = component;
  }

  @Override
  public boolean accept(File pathname) {
    boolean ok = false;
    if (pathname == null) {
      return ok;
    }

    String z;
    if (component == null) {
      z = pathname.getPath();
    } else {
      z = NameUtils.getComponent(pathname.getPath(), component);
    }
    if (BeeUtils.isEmpty(z)) {
      return ok;
    }
    
    for (Pattern pattern : patterns) {
      if (Wildcards.isLike(z, pattern)) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  @Override
  public String toString() {
    return "pattern = " + Arrays.toString(patterns) + "; component = " + component;
  }
}
