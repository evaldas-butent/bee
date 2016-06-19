package com.butent.bee.shared.modules.orders.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class OrdEcFinInfo implements BeeSerializable {
  private enum Serial {
    CREDIT_LIMIT, DEBT, OVERDUE, ORDERS, INVOICES
  }

  public static OrdEcFinInfo restore(String s) {
    OrdEcFinInfo efi = new OrdEcFinInfo();
    efi.deserialize(s);
    return efi;
  }

  private double creditLimit;
  private double debt;
  private double overdue;

  private final List<OrdEcOrder> orders = new ArrayList<>();
  private final List<OrdEcInvoice> invoices = new ArrayList<>();

  public OrdEcFinInfo() {
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
        case CREDIT_LIMIT:
          setCreditLimit(BeeUtils.toDouble(value));
          break;

        case DEBT:
          setDebt(BeeUtils.toDouble(value));
          break;

        case OVERDUE:
          setOverdue(BeeUtils.toDouble(value));
          break;

        case ORDERS:
          orders.clear();
          String[] ordArr = Codec.beeDeserializeCollection(value);
          if (ordArr != null) {
            for (String ord : ordArr) {
              orders.add(OrdEcOrder.restore(ord));
            }
          }
          break;

        case INVOICES:
          invoices.clear();
          String[] invArr = Codec.beeDeserializeCollection(value);
          if (invArr != null) {
            for (String inv : invArr) {
              invoices.add(OrdEcInvoice.restore(inv));
            }
          }
          break;
      }
    }
  }

  public double getCreditLimit() {
    return creditLimit;
  }

  public double getDebt() {
    return debt;
  }

  public double getOverdue() {
    return overdue;
  }

  public List<OrdEcOrder> getOrders() {
    return orders;
  }

  public List<OrdEcInvoice> getInvoices() {
    return invoices;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case CREDIT_LIMIT:
          arr[i++] = getCreditLimit();
          break;

        case DEBT:
          arr[i++] = getDebt();
          break;

        case OVERDUE:
          arr[i++] = getOverdue();
          break;

        case ORDERS:
          arr[i++] = getOrders();
          break;

        case INVOICES:
          arr[i++] = getInvoices();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setCreditLimit(double creditLimit) {
    this.creditLimit = creditLimit;
  }

  public void setDebt(double debt) {
    this.debt = debt;
  }

  public void setOverdue(double overdue) {
    this.overdue = overdue;
  }
}