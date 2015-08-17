package com.butent.bee.shared.modules.classifiers;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

public final class PriceInfo implements BeeSerializable {

  private enum Serial {
    COMPANY, CATEGORY, ITEM, PRICE_NAME, DISCOUNT_PERCENT, PRICE, CURRENCY
  }

  public static PriceInfo fromCompany(Long company, SimpleRow row) {
    Assert.notNull(row);
    PriceInfo result = new PriceInfo();

    result.setCompany(company);

    result.setPriceName(EnumUtils.getEnumByIndex(ItemPrice.class,
        row.getInt(COL_COMPANY_PRICE_NAME)));
    result.setDiscountPercent(row.getDouble(COL_COMPANY_DISCOUNT_PERCENT));

    return result;
  }

  public static PriceInfo fromDiscount(SimpleRow row) {
    Assert.notNull(row);
    PriceInfo result = new PriceInfo();

    result.setCompany(row.getLong(COL_DISCOUNT_COMPANY));
    result.setCategory(row.getLong(COL_DISCOUNT_CATEGORY));
    result.setItem(row.getLong(COL_DISCOUNT_ITEM));

    result.setPriceName(EnumUtils.getEnumByIndex(ItemPrice.class,
        row.getInt(COL_DISCOUNT_PRICE_NAME)));
    result.setDiscountPercent(row.getDouble(COL_DISCOUNT_PERCENT));
    result.setPrice(row.getDouble(COL_DISCOUNT_PRICE));
    result.setCurrency(row.getLong(COL_DISCOUNT_CURRENCY));

    return result;
  }

  public static PriceInfo restore(String s) {
    PriceInfo result = new PriceInfo();
    result.deserialize(s);
    return result;
  }

  private Long company;
  private Long category;
  private Long item;

  private ItemPrice priceName;
  private Double discountPercent;
  private Double price;
  private Long currency;

  private PriceInfo() {
  }

  public Long getCategory() {
    return category;
  }

  public Long getCompany() {
    return company;
  }

  public Long getCurrency() {
    return currency;
  }

  public Double getDiscountPercent() {
    return discountPercent;
  }

  public Long getItem() {
    return item;
  }

  public Double getPrice() {
    return price;
  }

  public ItemPrice getPriceName() {
    return priceName;
  }

  public void setCategory(Long category) {
    this.category = category;
  }

  public void setCompany(Long company) {
    this.company = company;
  }

  public void setCurrency(Long currency) {
    this.currency = currency;
  }

  public void setDiscountPercent(Double discountPercent) {
    this.discountPercent = discountPercent;
  }

  public void setItem(Long item) {
    this.item = item;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public void setPriceName(ItemPrice priceName) {
    this.priceName = priceName;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      String value = arr[i];
      if (!BeeUtils.isEmpty(value)) {
        switch (members[i]) {
          case COMPANY:
            setCompany(BeeUtils.toLongOrNull(value));
            break;

          case CATEGORY:
            setCategory(BeeUtils.toLongOrNull(value));
            break;

          case ITEM:
            setItem(BeeUtils.toLongOrNull(value));
            break;

          case PRICE_NAME:
            setPriceName(Codec.unpack(ItemPrice.class, value));
            break;

          case DISCOUNT_PERCENT:
            setDiscountPercent(BeeUtils.toDoubleOrNull(value));
            break;

          case PRICE:
            setPrice(BeeUtils.toDoubleOrNull(value));
            break;

          case CURRENCY:
            setCurrency(BeeUtils.toLongOrNull(value));
            break;
        }
      }
    }
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case COMPANY:
          arr[i++] = getCompany();
          break;

        case CATEGORY:
          arr[i++] = getCategory();
          break;

        case ITEM:
          arr[i++] = getItem();
          break;

        case PRICE_NAME:
          arr[i++] = Codec.pack(getPriceName());
          break;

        case DISCOUNT_PERCENT:
          arr[i++] = getDiscountPercent();
          break;

        case PRICE:
          arr[i++] = getPrice();
          break;

        case CURRENCY:
          arr[i++] = getCurrency();
          break;
      }
    }

    return Codec.beeSerialize(arr);
  }

  @Override
  public String toString() {
    return BeeUtils.joinOptions("company", company, "category", category, "item", item,
        "priceName", priceName, "percent", discountPercent, "price", price, "currency", currency);
  }
}
