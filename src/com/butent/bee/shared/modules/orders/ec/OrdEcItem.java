package com.butent.bee.shared.modules.orders.ec;

import com.google.common.base.Splitter;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;

public class OrdEcItem implements BeeSerializable, HasCaption {

  private enum Serial {
    ID, ARTICLE, NAME, PRICE, DESCRIPTION, UNIT
  }

  public static final Splitter CATEGORY_SPLITTER =
      Splitter.on(CATEGORY_ID_SEPARATOR).trimResults().omitEmptyStrings();

  public static String joinCategories(Collection<Long> categories) {
    StringBuilder sb = new StringBuilder();
    for (Long category : categories) {
      sb.append(category).append(CATEGORY_ID_SEPARATOR);
    }
    return sb.toString();
  }

  public static OrdEcItem restore(String s) {
    OrdEcItem item = new OrdEcItem();
    item.deserialize(s);
    return item;
  }

  private long id;
  private String article;
  private String name;
  private double price;
  private String description;
  private String unit;

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
        case ID:
          setId(BeeUtils.toLong(value));
          break;

        case ARTICLE:
          setArticle(value);
          break;

        case NAME:
          setName(value);
          break;

        case PRICE:
          setPrice(BeeUtils.toDouble(value));
          break;

        case DESCRIPTION:
          setDescription(value);
          break;

        case UNIT:
          setUnit(value);
          break;
      }
    }
  }

  public long getId() {
    return id;
  }

  public String getArticle() {
    return article;
  }

  @Override
  public String getCaption() {
    return getName();
  }

  public double getPrice() {
    return price;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
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
        case ID:
          arr[i++] = id;
          break;

        case ARTICLE:
          arr[i++] = article;
          break;

        case NAME:
          arr[i++] = name;
          break;

        case PRICE:
          arr[i++] = price;
          break;

        case DESCRIPTION:
          arr[i++] = description;
          break;

        case UNIT:
          arr[i++] = unit;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setArticle(String article) {
    this.article = article;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }
}