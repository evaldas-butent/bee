package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.modules.ec.EcConstants.EcSupplier;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

public class ArticleSupplier implements BeeSerializable {

  private enum Serial {
    SUPPLIER, SUPPLIER_ID, PRICE
  }

  public static ArticleSupplier restore(String s) {
    ArticleSupplier as = new ArticleSupplier();
    as.deserialize(s);
    return as;
  }

  private EcSupplier supplier;
  private String supplierId;
  private int price;

  public ArticleSupplier(EcSupplier supplier, String supplierId, Double price) {
    this.supplier = supplier;
    this.supplierId = supplierId;
    setPrice(price);
  }

  private ArticleSupplier() {
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case PRICE:
          setPrice(BeeUtils.toInt(value));
          break;

        case SUPPLIER:
          setSupplier(NameUtils.getEnumByName(EcSupplier.class, value));
          break;

        case SUPPLIER_ID:
          setSupplierId(value);
          break;
      }
    }
  }

  public int getPrice() {
    return price;
  }

  public double getRealPrice() {
    return price / 100d;
  }

  public EcSupplier getSupplier() {
    return supplier;
  }

  public String getSupplierId() {
    return supplierId;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case PRICE:
          arr[i++] = getPrice();
          break;

        case SUPPLIER:
          arr[i++] = getSupplier();
          break;

        case SUPPLIER_ID:
          arr[i++] = getSupplierId();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setPrice(Double price) {
    this.price = BeeUtils.isDouble(price) ? BeeUtils.round(price * 100) : 0;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  private void setSupplier(EcSupplier supplier) {
    this.supplier = supplier;
  }

  private void setSupplierId(String supplierId) {
    this.supplierId = supplierId;
  }
}
