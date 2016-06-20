package com.butent.bee.shared.modules.orders.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class OrdEcInvoiceItem implements BeeSerializable, HasCaption {
  private enum Serial {
    ITEM_ID, NAME, ARTICLE, QUANTITY, PRICE, UNIT
  }

  public static OrdEcInvoiceItem restore(String s) {
    OrdEcInvoiceItem item = new OrdEcInvoiceItem();
    item.deserialize(s);
    return item;
  }

  private long itemId;

  private String name;
  private String article;
  private Integer quantity;
  private Double price;
  private String unit;

  public OrdEcInvoiceItem() {
    super();
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
        case ITEM_ID:
          setItemId(BeeUtils.toLong(value));
          break;

        case NAME:
          setName(value);
          break;

        case ARTICLE:
          setArticle(value);
          break;

        case QUANTITY:
          setQuantity(BeeUtils.toIntOrNull(value));
          break;

        case PRICE:
          setPrice(BeeUtils.toDoubleOrNull(value));
          break;

        case UNIT:
          setUnit(value);
          break;
      }
    }
  }

  public long getItemId() {
    return itemId;
  }

  public double getAmount() {
    return BeeUtils.unbox(getQuantity()) * BeeUtils.unbox(getPrice());
  }

  @Override
  public String getCaption() {
    return BeeUtils.joinWords(getName(), getArticle());
  }

  public String getArticle() {
    return article;
  }

  public String getName() {
    return name;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public Double getPrice() {
    return price;
  }

  public String getUnit() {
    return unit;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case ITEM_ID:
          arr[i++] = getItemId();
          break;

        case NAME:
          arr[i++] = getName();
          break;

        case ARTICLE:
          arr[i++] = getArticle();
          break;

        case QUANTITY:
          arr[i++] = getQuantity();
          break;

        case PRICE:
          arr[i++] = getPrice();
          break;

        case UNIT:
          arr[i++] = getUnit();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setItemId(long itemId) {
    this.itemId = itemId;
  }

  public void setArticle(String article) {
    this.article = article;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }
}