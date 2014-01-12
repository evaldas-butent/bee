package com.butent.bee.shared.data.event;

import com.butent.bee.shared.Locality;

public interface FiresModificationEvents {
  void fireModificationEvent(ModificationEvent<?> event, Locality locality);
}
