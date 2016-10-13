package com.butent.bee.shared.modules.orders.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class OrdEcInvoice implements BeeSerializable {

  private enum Serial {
    INVOICE, DATE, SERIES, NUMBER, AMOUNT, CURRENCY, TERM, PAYMENT_TIME, PAID, DEBT, ITEMS, MANAGER
  }

  public static OrdEcInvoice restore(String s) {
    OrdEcInvoice invoice = new OrdEcInvoice();
    invoice.deserialize(s);
    return invoice;
  }

  private Long invoiceId;

  private String series;
  private String number;
  private String currency;
  private String manager;

  private DateTime date;
  private JustDate term;
  private DateTime paymentTime;

  private Double amount;
  private Double paid;
  private Double debt;

  private final List<OrdEcInvoiceItem> saleItems = new ArrayList<>();

  public OrdEcInvoice() {
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
        case SERIES:
          setSeries(value);
          break;

        case NUMBER:
          setNumber(value);
          break;

        case CURRENCY:
          setCurrency(value);
          break;

        case DATE:
          setDate(DateTime.restore(value));
          break;

        case TERM:
          setTerm((JustDate) JustDate.restore(value, ValueType.DATE));
          break;

        case PAYMENT_TIME:
          setPaymentTime(DateTime.restore(value));
          break;

        case AMOUNT:
          setAmount(BeeUtils.toDoubleOrNull(value));
          break;

        case PAID:
          setPaid(BeeUtils.toDoubleOrNull(value));
          break;

        case DEBT:
          setDebt(BeeUtils.toDoubleOrNull(value));
          break;

        case INVOICE:
          setInvoiceId(BeeUtils.toLongOrNull(value));
          break;

        case ITEMS:
          saleItems.clear();
          String[] itemArr = Codec.beeDeserializeCollection(value);
          if (itemArr != null) {
            for (String it : itemArr) {
              saleItems.add(OrdEcInvoiceItem.restore(it));
            }
          }
          break;

        case MANAGER:
          setManager(value);
          break;
      }
    }
  }

  public Double getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public DateTime getDate() {
    return date;
  }

  public Double getDebt() {
    return debt;
  }

  public Long getInvoiceId() {
    return invoiceId;
  }

  public List<OrdEcInvoiceItem> getItems() {
    return saleItems;
  }

  public Double getPaid() {
    return paid;
  }

  public DateTime getPaymentTime() {
    return paymentTime;
  }

  public String getManager() {
    return manager;
  }

  public String getNumber() {
    return number;
  }

  public String getSeries() {
    return series;
  }

  public JustDate getTerm() {
    return term;
  }

  public double getTotalAmount() {
    double total = 0;
    for (OrdEcInvoiceItem item : saleItems) {
      total += item.getAmount();
    }
    return total;
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

        case INVOICE:
          arr[i++] = getInvoiceId();
          break;

        case SERIES:
          arr[i++] = getSeries();
          break;

        case PAID:
          arr[i++] = getPaid();
          break;

        case PAYMENT_TIME:
          arr[i++] = getPaymentTime();
          break;

        case CURRENCY:
          arr[i++] = getCurrency();
          break;

        case ITEMS:
          arr[i++] = getItems();
          break;

        case MANAGER:
          arr[i++] = getManager();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public void setDate(DateTime date) {
    this.date = date;
  }

  public void setDebt(Double debt) {
    this.debt = debt;
  }

  public void setInvoiceId(long invoiceId) {
    this.invoiceId = invoiceId;
  }

  public void setPaid(Double paid) {
    this.paid = paid;
  }

  public void setPaymentTime(DateTime paymentTime) {
    this.paymentTime = paymentTime;
  }

  public void setManager(String manager) {
    this.manager = manager;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public void setSeries(String series) {
    this.series = series;
  }

  public void setTerm(JustDate term) {
    this.term = term;
  }
}