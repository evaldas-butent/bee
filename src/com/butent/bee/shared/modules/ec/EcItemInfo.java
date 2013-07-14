package com.butent.bee.shared.modules.ec;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class EcItemInfo implements BeeSerializable {

  private enum Serial {
    CRITERIA, REMAINDERS, BRANDS, CAR_TYPES
  }

  public static EcItemInfo restore(String s) {
    EcItemInfo ecItemInfo = new EcItemInfo();
    ecItemInfo.deserialize(s);
    return ecItemInfo;
  }

  private final List<ArticleCriteria> criteria = Lists.newArrayList();

  private final List<ArticleRemainder> remainders = Lists.newArrayList();

  private final List<ArticleBrand> brands = Lists.newArrayList();

  private final List<EcCarType> carTypes = Lists.newArrayList();

  public EcItemInfo() {
    super();
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      String[] values = Codec.beeDeserializeCollection(arr[i]);
      if (values == null) {
        continue;
      }

      switch (members[i]) {
        case CRITERIA:
          criteria.clear();
          for (String v : values) {
            criteria.add(ArticleCriteria.restore(v));
          }
          break;

        case REMAINDERS:
          remainders.clear();
          for (String v : values) {
            remainders.add(ArticleRemainder.restore(v));
          }
          break;

        case BRANDS:
          brands.clear();
          for (String v : values) {
            brands.add(ArticleBrand.restore(v));
          }
          break;

        case CAR_TYPES:
          carTypes.clear();
          for (String v : values) {
            carTypes.add(EcCarType.restore(v));
          }
          break;
      }
    }
  }

  public List<ArticleBrand> getBrands() {
    return brands;
  }

  public List<EcCarType> getCarTypes() {
    return carTypes;
  }

  public List<ArticleCriteria> getCriteria() {
    return criteria;
  }

  public List<ArticleRemainder> getRemainders() {
    return remainders;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case CRITERIA:
          arr[i++] = getCriteria();
          break;

        case REMAINDERS:
          arr[i++] = getRemainders();
          break;

        case BRANDS:
          arr[i++] = getBrands();
          break;

        case CAR_TYPES:
          arr[i++] = getCarTypes();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setBrands(List<ArticleBrand> brands) {
    BeeUtils.overwrite(this.brands, brands);
  }

  public void setCarTypes(List<EcCarType> carTypes) {
    BeeUtils.overwrite(this.carTypes, carTypes);
  }

  public void setCriteria(List<ArticleCriteria> criteria) {
    BeeUtils.overwrite(this.criteria, criteria);
  }

  public void setRemainders(List<ArticleRemainder> remainders) {
    BeeUtils.overwrite(this.remainders, remainders);
  }
}
