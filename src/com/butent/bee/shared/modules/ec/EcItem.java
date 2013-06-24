package com.butent.bee.shared.modules.ec;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class EcItem implements BeeSerializable {

  private enum Serial {
    ID, CODE, NAME, SUPPLIER, CATEGORIES, STOCK_1, STOCK_2, LIST_PRICE, PRICE
  }

  public static final Splitter CATEGORY_SPLITTER =
      Splitter.on(EcConstants.CATEGORY_SEPARATOR).trimResults().omitEmptyStrings();
  
  public static EcItem restore(String s) {
    EcItem item = new EcItem();
    item.deserialize(s);
    return item;
  }

  private int id;

  private String code;
  private String name;

  private String supplier;

  private String categories;

  private int stock1;
  private int stock2;

  private int listPrice;
  private int price;

  public EcItem(int id) {
    this.id = id;
  }

  private EcItem() {
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
        case ID:
          this.id = BeeUtils.toInt(value);
          break;

        case CODE:
          setCode(value);
          break;

        case NAME:
          setName(value);
          break;

        case SUPPLIER:
          setSupplier(value);
          break;

        case CATEGORIES:
          setCategories(value);
          break;

        case STOCK_1:
          setStock1(BeeUtils.toInt(value));
          break;

        case STOCK_2:
          setStock2(BeeUtils.toInt(value));
          break;

        case LIST_PRICE:
          this.listPrice = BeeUtils.toInt(value);
          break;

        case PRICE:
          this.price = BeeUtils.toInt(value);
          break;
      }
    }
  }

  public String getCategories() {
    return categories;
  }
  
  public List<Integer> getCategoryList() {
    List<Integer> result = Lists.newArrayList();

    if (getCategories() != null) {
      for (String s : CATEGORY_SPLITTER.split(getCategories())) {
        result.add(BeeUtils.toInt(s));
      }
    }
    return result;
  }

  public String getCode() {
    return code;
  }

  public int getId() {
    return id;
  }

  public int getListPrice() {
    return listPrice;
  }

  public String getManufacturer() {
    return supplier;
  }

  public String getName() {
    return name;
  }

  public int getPrice() {
    return price;
  }

  public int getStock1() {
    return stock1;
  }

  public int getStock2() {
    return stock2;
  }

  public String getSupplier() {
    return supplier;
  }

  public boolean hasAnalogs() {
    return true;
  }

  public boolean hasCategory(int category) {
    return categories != null
        && categories.contains(EcConstants.CATEGORY_SEPARATOR + category
            + EcConstants.CATEGORY_SEPARATOR);
  }

  public boolean isFeatured() {
    return true;
  }

  public boolean isNovelty() {
    return true;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case ID:
          arr[i++] = id;
          break;

        case CODE:
          arr[i++] = code;
          break;

        case NAME:
          arr[i++] = name;
          break;

        case SUPPLIER:
          arr[i++] = supplier;
          break;

        case CATEGORIES:
          arr[i++] = categories;
          break;

        case STOCK_1:
          arr[i++] = stock1;
          break;

        case STOCK_2:
          arr[i++] = stock2;
          break;

        case LIST_PRICE:
          arr[i++] = listPrice;
          break;

        case PRICE:
          arr[i++] = price;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public EcItem setCategories(String categories) {
    this.categories = categories;
    return this;
  }

  public EcItem setCode(String code) {
    this.code = code;
    return this;
  }

  public EcItem setListPrice(Double listPrice) {
    this.listPrice = BeeUtils.isDouble(listPrice) ? BeeUtils.round(listPrice * 100) : 0;
    return this;
  }

  public void setListPrice(int listPrice) {
    this.listPrice = listPrice;
  }

  public EcItem setName(String name) {
    this.name = name;
    return this;
  }

  public EcItem setPrice(Double price) {
    this.price = BeeUtils.isDouble(price) ? BeeUtils.round(price * 100) : 0;
    return this;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public EcItem setStock1(int stock1) {
    this.stock1 = stock1;
    return this;
  }

  public EcItem setStock2(int stock2) {
    this.stock2 = stock2;
    return this;
  }

  public EcItem setSupplier(String supplier) {
    this.supplier = supplier;
    return this;
  }
}
