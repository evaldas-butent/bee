package com.butent.bee.shared.modules.cars;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class Bundle implements BeeSerializable {
  private Set<Option> options = new LinkedHashSet<>();

  public Bundle(Collection<Option> options) {
    for (Option option : Assert.notEmpty(options)) {
      this.options.add(Assert.notNull(option));
    }
  }

  private Bundle() {
  }

  @Override
  public void deserialize(String s) {
    options.clear();
    String[] optData = Codec.beeDeserializeCollection(s);

    if (!ArrayUtils.isEmpty(optData)) {
      for (String o : optData) {
        options.add(Option.restore(o));
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return Objects.equals(options, ((Bundle) o).options);
  }

  public String getKey() {
    Set<Long> opts = new TreeSet<>();

    for (Option option : options) {
      opts.add(option.getId());
    }
    return Codec.md5(BeeUtils.join("-", opts));
  }

  public Set<Option> getOptions() {
    return options;
  }

  @Override
  public int hashCode() {
    return Objects.hash(options);
  }

  public static Bundle of(Bundle... bundles) {
    List<Option> options = new ArrayList<>();

    if (!ArrayUtils.isEmpty(bundles)) {
      for (Bundle bundle : bundles) {
        if (bundle != null) {
          options.addAll(bundle.getOptions());
        }
      }
    }
    if (options.isEmpty()) {
      return null;
    }
    return new Bundle(options);
  }

  public static Bundle restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    Bundle bundle = new Bundle();
    bundle.deserialize(s);
    return bundle;
  }

  @Override
  public String serialize() {
    return Codec.beeSerialize(options);
  }

  @Override
  public String toString() {
    return options.toString();
  }
}
