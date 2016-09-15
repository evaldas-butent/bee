package com.butent.bee.client.modules.payroll;

import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IdPair;

final class Partition {

  private final BeeRow row;
  private final Long substituteFor;

  Partition(BeeRow row) {
    this(row, null);
  }

  Partition(BeeRow row, Long substituteFor) {
    this.row = row;
    this.substituteFor = substituteFor;
  }

  long getId() {
    return getRow().getId();
  }

  IdPair getIds() {
    return IdPair.of(row.getId(), substituteFor);
  }

  BeeRow getRow() {
    return row;
  }

  Long getSubstituteFor() {
    return substituteFor;
  }

  boolean hasSubstituteFor() {
    return DataUtils.isId(substituteFor);
  }
}
