package com.butent.bee.shared.modules.ec;

import com.google.common.collect.Sets;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

public class EcItem {
  
  private final long id = 0;

  private final String code;
  private final String name;
  
  private final Collection<String> groups;

  private final String manufacturer;
  private final String supplier;
  
  private final int stock1; 
  private final int stock2;
  
  private int quantity = 1;

  public EcItem() {
    super();
    
    this.code = BeeUtils.randomString(10, 10, '0', '9');
    this.name = BeeUtils.randomString(6, 30, 'a', 'z');
    
    this.groups = Sets.newHashSet();
    int cnt = BeeUtils.randomInt(1, 6);
    for (int i = 0; i < cnt; i++) {
      char ch = BeeUtils.randomChar('a', 'z');
      String group = String.valueOf(ch).toUpperCase() + BeeUtils.replicate(ch, 5);
      groups.add(group);
    }
    
    this.manufacturer = "Gamintojas " + BeeUtils.randomInt(1, 10);
    this.supplier = BeeUtils.randomString(6, 10, 'A', 'Z');
    
    this.stock1 = BeeUtils.randomInt(0, 10);
    this.stock2 = BeeUtils.randomInt(0, 2) * BeeUtils.randomInt(1, 100);
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

  public String getManufacturer() {
    return manufacturer;
  }

  public String getName() {
    return name;
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
  
  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}
