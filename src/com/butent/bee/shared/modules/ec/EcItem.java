package com.butent.bee.shared.modules.ec;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class EcItem implements BeeSerializable {

  private enum Serial {
    ARTICLE_ID, ARTICLE_BRAND_ID, MANUFACTURER, CODE, NAME, SUPPLIER, SUPPLIER_CODE,
    CATEGORIES, STOCK_1, STOCK_2, LIST_PRICE, PRICE
  }

  public static final Splitter CATEGORY_SPLITTER =
      Splitter.on(EcConstants.CATEGORY_SEPARATOR).trimResults().omitEmptyStrings();

  public static EcItem restore(String s) {
    EcItem item = new EcItem();
    item.deserialize(s);
    return item;
  }

  private int articleId;
  private long articleBrandId;

  private String manufacturer;
  private String code;
  private String name;

  private String supplier;
  private String supplierCode;

  private String categories;

  private int stock1;
  private int stock2;

  private int listPrice;
  private int price;

  public EcItem(int articleId, long articleBrandId) {
    this.articleId = articleId;
    this.articleBrandId = articleBrandId;
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
        case ARTICLE_ID:
          this.articleId = BeeUtils.toInt(value);
          break;

        case ARTICLE_BRAND_ID:
          this.articleBrandId = BeeUtils.toLong(value);
          break;

        case MANUFACTURER:
          setManufacturer(value);
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

        case SUPPLIER_CODE:
          setSupplierCode(value);
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
          setListPrice(BeeUtils.toInt(value));
          break;

        case PRICE:
          setPrice(BeeUtils.toInt(value));
          break;
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof EcItem) && articleBrandId == ((EcItem) obj).articleBrandId;
  }

  public long getArticleBrandId() {
    return articleBrandId;
  }

  public int getArticleId() {
    return articleId;
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

  public int getListPrice() {
    return listPrice;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public String getName() {
    return name;
  }

  public int getPrice() {
    return price;
  }

  public double getRealListPrice() {
    return listPrice / 100d;
  }
  
  public double getRealPrice() {
    return price / 100d;
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

  public String getSupplierCode() {
    return supplierCode;
  }

  public boolean hasAnalogs() {
    return true;
  }

  public boolean hasCategory(int category) {
    return categories != null
        && categories.contains(EcConstants.CATEGORY_SEPARATOR + category
            + EcConstants.CATEGORY_SEPARATOR);
  }

  @Override
  public int hashCode() {
    return Longs.hashCode(articleBrandId);
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
        case ARTICLE_ID:
          arr[i++] = articleId;
          break;

        case ARTICLE_BRAND_ID:
          arr[i++] = articleBrandId;
          break;

        case MANUFACTURER:
          arr[i++] = manufacturer;
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

        case SUPPLIER_CODE:
          arr[i++] = supplierCode;
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

  public void setCategories(String categories) {
    this.categories = categories;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setListPrice(Double listPrice) {
    this.listPrice = BeeUtils.isDouble(listPrice) ? BeeUtils.round(listPrice * 100) : 0;
  }

  public void setListPrice(int listPrice) {
    this.listPrice = listPrice;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPrice(Double price) {
    this.price = BeeUtils.isDouble(price) ? BeeUtils.round(price * 100) : 0;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public void setStock1(int stock1) {
    this.stock1 = stock1;
  }

  public void setStock2(int stock2) {
    this.stock2 = stock2;
  }

  public void setSupplier(String supplier) {
    this.supplier = supplier;
  }

  public void setSupplierCode(String supplierCode) {
    this.supplierCode = supplierCode;
  }
}
