package com.butent.bee.shared.modules.ec;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class EcGroupFilters implements BeeSerializable {
  
  private final List<EcBrand> brands = Lists.newArrayList();

  private final List<EcCriterion> criteria = Lists.newArrayList();

  public EcGroupFilters() {
    super();
  }
  
  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 2);
    
    brands.clear();
    criteria.clear();
    
    String[] values = Codec.beeDeserializeCollection(arr[0]);
    if (values != null) {
      for (String v : values) {
        brands.add(EcBrand.restore(v));
      }
    }

    values = Codec.beeDeserializeCollection(arr[1]);
    if (values != null) {
      for (String v : values) {
        criteria.add(EcCriterion.restore(v));
      }
    }
  }

  public List<EcBrand> getBrands() {
    return brands;
  }

  public List<EcCriterion> getCriteria() {
    return criteria;
  }

  public int getSize() {
    int size = brands.size();
    for (EcCriterion criterion : criteria) {
      size += criterion.getSize();
    }
    return size;
  }
  
  public boolean isEmpty() {
    return brands.isEmpty() && criteria.isEmpty();
  }

  @Override
  public String serialize() {
    Object[] arr = new Object[] {getBrands(), getCriteria()};
    return Codec.beeSerialize(arr);
  }
}
