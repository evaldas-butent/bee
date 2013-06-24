package com.butent.bee.shared.modules.ec;

import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;

public class EcItem implements BeeSerializable {

  private enum Serial {
    ID, CODE, NAME, SUPPLIER, STOCK, LIST_PRICE, PRICE
  }

  public static EcItem restore(String s) {
    EcItem item = new EcItem();
    item.deserialize(s);
    return item;
  }

  private long id;

  private String code;
  private String name;

  private final Collection<String> groups;

  private final String manufacturer;
  private String supplier;

  private int stock1;
  private final int stock2;
  
  private int listPrice;
  private int price;

  private int quantity = 1;

  public EcItem(long id) {
    this();
    this.id = id;
  }

  private EcItem() {
    this.groups = Sets.newHashSet();
    int cnt = BeeUtils.randomInt(1, 6);
    for (int i = 0; i < cnt; i++) {
      char ch = BeeUtils.randomChar('a', 'z');
      String group = String.valueOf(ch).toUpperCase() + BeeUtils.replicate(ch, 5);
      groups.add(group);
    }

    this.manufacturer = "Gamintojas " + BeeUtils.randomInt(1, 10);
    this.stock2 = BeeUtils.randomInt(0, 2) * BeeUtils.randomInt(1, 100);
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
          this.id = BeeUtils.toLong(value);
          break;

        case CODE:
          setCode(value);
          break;

        case NAME:
          setName(value);
          break;

        case STOCK:
          setStock(BeeUtils.toInt(value));
          break;

        case SUPPLIER:
          setSupplier(value);
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

  public String getCode() {
    return code;
  }

  public Collection<String> getGroups() {
    return groups;
  }

  public long getId() {
    return id;
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

  public int getQuantity() {
    return quantity;
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

  public boolean hasGroup(String group) {
    return groups.contains(group);
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

        case STOCK:
          arr[i++] = stock1;
          break;

        case SUPPLIER:
          arr[i++] = supplier;
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

  public EcItem setQuantity(int quantity) {
    this.quantity = quantity;
    return this;
  }

  public EcItem setStock(int stock) {
    this.stock1 = stock;
    return this;
  }

  public EcItem setSupplier(String supplier) {
    this.supplier = supplier;
    return this;
  }
}
