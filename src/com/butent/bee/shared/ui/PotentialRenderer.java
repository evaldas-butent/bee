package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public abstract class PotentialRenderer implements BeeSerializable, HasInfo {

  public static PotentialRenderer restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    String[] arr = Codec.beeDeserializeCollection(s);
    
    PotentialRenderer result = null;
    if (Calculation.canRestore(arr)) {
      result = new Calculation();
    } else if (RendererDescription.canRestore(arr)) {
      result = new RendererDescription();
    }
    Assert.notNull(result, "cannot deserialize PotentialRenderer");
    
    result.deserializeMembers(arr);
    return result;
  }
  
  protected abstract void deserializeMembers(String[] arr);
}
