package com.butent.bee.shared.modules.ec;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.modules.ec.EcConstants.EcDisplayedPrice;
import com.butent.bee.shared.modules.ec.EcConstants.EcSupplier;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;

public class EcItem implements BeeSerializable {

  private enum Serial {
    ARTICLE_ID, BRAND, CODE, NAME, SUPPLIERS, CATEGORIES, PRICE, LIST_PRICE,
    DESCRIPTION, NOVELTY, FEATURED, UNIT
  }

  public static final Splitter CATEGORY_SPLITTER =
      Splitter.on(EcConstants.CATEGORY_ID_SEPARATOR).trimResults().omitEmptyStrings();

  public static EcItem restore(String s) {
    EcItem item = new EcItem();
    item.deserialize(s);
    return item;
  }

  private long articleId;

  private Long brand;
  private String code;
  private String name;

  private String categories;
  private final Collection<ArticleSupplier> suppliers = Lists.newArrayList();

  private int price;
  private int listPrice;

  private String description;

  private boolean novelty;
  private boolean featured;

  private String unit;

  public EcItem(long articleId) {
    this.articleId = articleId;
  }

  private EcItem() {
  }

  public void clearListPrice() {
    this.listPrice = 0;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];
      if (value == null) {
        continue;
      }

      switch (member) {
        case ARTICLE_ID:
          this.articleId = BeeUtils.toLong(value);
          break;

        case BRAND:
          setBrand(BeeUtils.toLongOrNull(value));
          break;

        case CODE:
          setCode(value);
          break;

        case NAME:
          setName(value);
          break;

        case SUPPLIERS:
          Collection<ArticleSupplier> sups = Lists.newArrayList();

          for (String supplier : Codec.beeDeserializeCollection(value)) {
            sups.add(ArticleSupplier.restore(supplier));
          }
          setSuppliers(sups);
          break;

        case CATEGORIES:
          setCategories(value);
          break;

        case PRICE:
          setPrice(BeeUtils.toInt(value));
          break;

        case LIST_PRICE:
          setListPrice(BeeUtils.toInt(value));
          break;

        case DESCRIPTION:
          setDescription(value);
          break;

        case NOVELTY:
          setNovelty(Codec.unpack(value));
          break;

        case FEATURED:
          setFeatured(Codec.unpack(value));
          break;

        case UNIT:
          setUnit(value);
          break;
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof EcItem) && articleId == ((EcItem) obj).articleId;
  }

  public long getArticleId() {
    return articleId;
  }

  public Long getBrand() {
    return brand;
  }

  public String getCategories() {
    return categories;
  }

  public List<Long> getCategoryList() {
    List<Long> result = Lists.newArrayList();

    if (getCategories() != null) {
      for (String s : CATEGORY_SPLITTER.split(getCategories())) {
        result.add(BeeUtils.toLong(s));
      }
    }
    return result;
  }

  public String getCode() {
    return code;
  }

  public double getCost(EcDisplayedPrice displayedPrice) {
    EcSupplier costSupplier = EcDisplayedPrice.getSupplier(displayedPrice);

    double cost = BeeConst.DOUBLE_ZERO;

    for (ArticleSupplier articleSupplier : getSuppliers()) {
      double supplierCost = articleSupplier.getRealPrice();

      if (BeeUtils.isPositive(supplierCost)) {
        if (costSupplier != null && articleSupplier.getSupplier() == costSupplier) {
          cost = supplierCost;
          break;
        }

        if (displayedPrice == EcDisplayedPrice.MIN) {
          if (articleSupplier.totalStock() > 0) {
            if (BeeUtils.isPositive(cost)) {
              cost = Math.min(cost, supplierCost);
            } else {
              cost = supplierCost;
            }
          }

        } else if (displayedPrice == EcDisplayedPrice.MAX) {
          if (articleSupplier.totalStock() > 0) {
            cost = Math.max(cost, supplierCost);
          }

        } else {
          cost = Math.max(cost, supplierCost);
        }
      }
    }

    if (!BeeUtils.isPositive(cost)
        && (displayedPrice == EcDisplayedPrice.MIN || displayedPrice == EcDisplayedPrice.MAX)) {
      for (ArticleSupplier articleSupplier : getSuppliers()) {
        double supplierCost = articleSupplier.getRealPrice();

        if (BeeUtils.isPositive(supplierCost)) {
          if (displayedPrice == EcDisplayedPrice.MIN) {
            if (BeeUtils.isPositive(cost)) {
              cost = Math.min(cost, supplierCost);
            } else {
              cost = supplierCost;
            }

          } else if (displayedPrice == EcDisplayedPrice.MAX) {
            cost = Math.max(cost, supplierCost);
          }
        }
      }
    }

    return cost;
  }

  public String getDescription() {
    return description;
  }

  public int getListPrice() {
    return listPrice;
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

  public int getStock(Collection<String> warehouses) {
    int stock = 0;
    for (ArticleSupplier supplier : getSuppliers()) {
      stock += supplier.getStock(warehouses);
    }
    return stock;
  }

  public Collection<ArticleSupplier> getSuppliers() {
    return suppliers;
  }

  public String getUnit() {
    return unit;
  }

  public boolean hasAnalogs() {
    return true;
  }

  public boolean hasCategory(long category) {
    return categories != null
        && categories.contains(EcConstants.CATEGORY_ID_SEPARATOR + category
            + EcConstants.CATEGORY_ID_SEPARATOR);
  }

  @Override
  public int hashCode() {
    return Longs.hashCode(articleId);
  }

  public boolean isFeatured() {
    return featured;
  }

  public boolean isNovelty() {
    return novelty;
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

        case BRAND:
          arr[i++] = brand;
          break;

        case CODE:
          arr[i++] = code;
          break;

        case NAME:
          arr[i++] = name;
          break;

        case SUPPLIERS:
          arr[i++] = suppliers;
          break;

        case CATEGORIES:
          arr[i++] = categories;
          break;

        case PRICE:
          arr[i++] = price;
          break;

        case LIST_PRICE:
          arr[i++] = listPrice;
          break;

        case DESCRIPTION:
          arr[i++] = description;
          break;

        case NOVELTY:
          arr[i++] = Codec.pack(novelty);
          break;

        case FEATURED:
          arr[i++] = Codec.pack(featured);
          break;

        case UNIT:
          arr[i++] = unit;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setBrand(Long brand) {
    this.brand = brand;
  }

  public void setCategories(String categories) {
    this.categories = categories;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setFeatured(boolean featured) {
    this.featured = featured;
  }

  public void setListPrice(Double listPrice) {
    this.listPrice = BeeUtils.isDouble(listPrice) ? BeeUtils.round(listPrice * 100) : 0;
  }

  public void setListPrice(int listPrice) {
    this.listPrice = listPrice;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNovelty(boolean novelty) {
    this.novelty = novelty;
  }

  public void setPrice(Double price) {
    this.price = BeeUtils.isDouble(price) ? BeeUtils.round(price * 100) : 0;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public void setSuppliers(Collection<ArticleSupplier> suppliers) {
    BeeUtils.overwrite(this.suppliers, suppliers);
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public int totalStock() {
    int stock = 0;
    for (ArticleSupplier supplier : getSuppliers()) {
      stock += supplier.totalStock();
    }
    return stock;
  }
}
