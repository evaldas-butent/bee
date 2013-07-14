package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class ArticleBrand implements BeeSerializable {

  private enum Serial {
    BRAND, ANALOG_NR, PRICE, SUPPLIER, SUPPLIER_ID
  }
  
  public static ArticleBrand restore(String s) {
    ArticleBrand ab = new ArticleBrand();
    ab.deserialize(s);
    return ab;
  }
  
  private String brand;
  private String analogNr;

  private Double price;

  private String supplier;
  private String supplierId;

  public ArticleBrand(String brand, String analogNr, Double price, String supplier,
      String supplierId) {
    this.brand = brand;
    this.analogNr = analogNr;
    this.price = price;
    this.supplier = supplier;
    this.supplierId = supplierId;
  }

  private ArticleBrand() {
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
        case BRAND:
          setBrand(value);
          break;

        case ANALOG_NR:
          setAnalogNr(value);
          break;

        case PRICE:
          setPrice(BeeUtils.toDoubleOrNull(value));
          break;

        case SUPPLIER:
          setSupplier(value);
          break;

        case SUPPLIER_ID:
          setSupplierId(value);
          break;
      }
    }
  }
  
  public String getBrand() {
    return brand;
  }

  public String getAnalogNr() {
    return analogNr;
  }

  public Double getPrice() {
    return price;
  }

  public String getSupplier() {
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
        case BRAND:
          arr[i++] = getBrand();
          break;

        case ANALOG_NR:
          arr[i++] = getAnalogNr();
          break;

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
  
  private void setBrand(String brand) {
    this.brand = brand;
  }

  private void setAnalogNr(String analogNr) {
    this.analogNr = analogNr;
  }

  private void setPrice(Double price) {
    this.price = price;
  }

  private void setSupplier(String supplier) {
    this.supplier = supplier;
  }

  private void setSupplierId(String supplierId) {
    this.supplierId = supplierId;
  }
}
