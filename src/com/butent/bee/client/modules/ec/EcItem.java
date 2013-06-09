package com.butent.bee.client.modules.ec;

import com.butent.bee.shared.utils.BeeUtils;

public class EcItem {
  
  private final long id = 0;

  private final String code;
  private final String name;
  private final String supplier;
  
  private final int stock1; 
  private final int stock2;
  
  private int quantity = 1;

  public EcItem() {
    super();
    
    this.code = BeeUtils.randomString(10, 10, '0', '9');
    this.name = BeeUtils.randomString(6, 30, 'a', 'z');
    this.supplier = BeeUtils.randomString(6, 10, 'A', 'Z');
    
    this.stock1 = BeeUtils.randomInt(0, 10);
    this.stock2 = BeeUtils.randomInt(0, 2) * BeeUtils.randomInt(1, 100);
  }

  public String getCode() {
    return code;
  }

  public long getId() {
    return id;
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

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}
