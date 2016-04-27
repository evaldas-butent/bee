package com.butent.bee.shared.data.event;

import com.butent.bee.shared.Locality;

@FunctionalInterface
public interface FiresModificationEvents {
  void fireModificationEvent(ModificationEvent<?> event, Locality locality);
}
