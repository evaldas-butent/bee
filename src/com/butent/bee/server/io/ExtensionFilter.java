package com.butent.bee.server.io;

import com.google.common.collect.Sets;

import com.butent.bee.shared.io.FileNameUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.File;
import java.util.Collection;

/**
 * Enables to filter files by their extensions.
 */

public class ExtensionFilter extends AbstractNameFilter {
  private Collection<String> extensions;
  private boolean sensitive;

  public ExtensionFilter(String ext) {
    this(ext, false);
  }

  public ExtensionFilter(String ext, boolean sensitive) {
    this(Sets.newHashSet(ext), sensitive);
  }

  public ExtensionFilter(Collection<String> values) {
    this(values, false);
  }

  public ExtensionFilter(Collection<String> values, boolean sensitive) {
    this.extensions = values;
    this.sensitive = sensitive;
  }

  @Override
  public boolean accept(File dir, String name) {
    boolean ok = false;
    String x = FileNameUtils.getExtension(name);
    if (BeeUtils.isEmpty(x)) {
      return ok;
    }

    for (String ext : extensions) {
      if (sensitive) {
        ok = x.equals(ext);
      } else {
        ok = BeeUtils.same(x, ext);
      }
      if (ok) {
        break;
      }
    }
    return ok;
  }
}
