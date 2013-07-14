package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class ArticleRemainder implements BeeSerializable {

  public static ArticleRemainder restore(String s) {
    ArticleRemainder ar = new ArticleRemainder();
    ar.deserialize(s);
    return ar;
  }
  
  private String warehouse;
  private Double remainder;

  public ArticleRemainder(String warehouse, Double remainder) {
    this.warehouse = warehouse;
    this.remainder = remainder;
  }

  private ArticleRemainder() {
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 2);

    setWarehouse(arr[0]);
    setRemainder(BeeUtils.toDoubleOrNull(arr[1]));
  }

  public Double getRemainder() {
    return remainder;
  }

  public String getWarehouse() {
    return warehouse;
  }

  @Override
  public String serialize() {
    Object[] arr = new Object[] {getWarehouse(), getRemainder()};
    return Codec.beeSerialize(arr);
  }

  private void setRemainder(Double remainder) {
    this.remainder = remainder;
  }

  private void setWarehouse(String warehouse) {
    this.warehouse = warehouse;
  }
}
