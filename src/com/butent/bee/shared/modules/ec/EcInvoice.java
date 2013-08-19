package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class EcInvoice implements BeeSerializable {

  private enum Serial {
    NUMBER, DATE, TERM, AMOUNT, DEBT
  }
  
  public static EcInvoice restore(String s) {
    EcInvoice invoice = new EcInvoice();
    invoice.deserialize(s);
    return invoice;
  }

  private String number;
  private DateTime date;

  private DateTime term;

  private Double amount;
  private Double debt;

  public EcInvoice() {
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
        case NUMBER:
          setNumber(value);
          break;

        case DATE:
          setDate(DateTime.restore(value));
          break;

        case TERM:
          setTerm(DateTime.restore(value));
          break;

        case AMOUNT:
          setAmount(BeeUtils.toDoubleOrNull(value));
          break;

        case DEBT:
          setDebt(BeeUtils.toDoubleOrNull(value));
          break;
      }
    }
  }

  public Double getAmount() {
    return amount;
  }

  public DateTime getDate() {
    return date;
  }

  public Double getDebt() {
    return debt;
  }

  public String getNumber() {
    return number;
  }

  public DateTime getTerm() {
    return term;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case NUMBER:
          arr[i++] = getNumber();
          break;

        case DATE:
          arr[i++] = getDate();
          break;

        case TERM:
          arr[i++] = getTerm();
          break;

        case AMOUNT:
          arr[i++] = getAmount();
          break;

        case DEBT:
          arr[i++] = getDebt();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public void setDate(DateTime date) {
    this.date = date;
  }

  public void setDebt(Double debt) {
    this.debt = debt;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public void setTerm(DateTime term) {
    this.term = term;
  }
}
