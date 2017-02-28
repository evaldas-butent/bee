package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Objects;

public class ItemQuantities implements BeeSerializable {

  private String article;
  private double stock;
  private double reserved;

  public ItemQuantities(String article) {
    setArticle(article);
  }

  private ItemQuantities() {
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 3);

    setArticle(arr[0]);
    setStock(BeeUtils.toDouble(arr[1]));
    setReserved(BeeUtils.toDouble(arr[2]));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ItemQuantities)) {
      return false;
    }
    ItemQuantities itemQuantities = (ItemQuantities) o;

    return Double.compare(itemQuantities.stock, stock) == 0
        && Double.compare(itemQuantities.reserved, reserved) == 0
        && Objects.equals(article, itemQuantities.article);
  }

  public String getArticle() {
    return article;
  }

  public double getReserved() {
    return reserved;
  }

  public double getStock() {
    return stock;
  }

  @Override
  public int hashCode() {
    return Objects.hash(article, stock, reserved);
  }

  public static ItemQuantities reserved(String article, Double reserved) {
    ItemQuantities itemQuantities = new ItemQuantities(article);
    itemQuantities.setReserved(reserved);
    return itemQuantities;
  }

  public static ItemQuantities restore(String s) {
    ItemQuantities itemQuantities = new ItemQuantities();
    itemQuantities.deserialize(s);
    return itemQuantities;
  }

  public static ItemQuantities stock(String article, Double stock) {
    ItemQuantities itemQuantities = new ItemQuantities(article);
    itemQuantities.setStock(stock);
    return itemQuantities;
  }

  @Override
  public String serialize() {
    return Codec.beeSerialize(new Object[] {getArticle(), getStock(), getReserved()});
  }

  public void setReserved(Double reserved) {
    this.reserved = BeeUtils.unbox(reserved);
  }

  public void setStock(Double stock) {
    this.stock = BeeUtils.unbox(stock);
  }

  @Override
  public String toString() {
    return "a=" + article + " s=" + BeeUtils.toString(stock)
        + " r=" + BeeUtils.toString(reserved);
  }

  private void setArticle(String article) {
    this.article = article;
  }
}
