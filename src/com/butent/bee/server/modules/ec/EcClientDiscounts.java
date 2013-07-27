package com.butent.bee.server.modules.ec;

import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class EcClientDiscounts {
  
  private final Double defPercent;

  public EcClientDiscounts(Double defPercent, List<SimpleRowSet> discounts) {
    super();
    
    this.defPercent = defPercent;
  }

  public void applyTo(EcItem ecItem) {
    if (defPercent == null) {
      ecItem.setPrice(ecItem.getListPrice());
    } else {
      ecItem.setPrice(BeeUtils.minusPercent(ecItem.getRealListPrice(), defPercent));
    }
  }
}
