package com.butent.webservice;

import com.google.common.collect.Lists;

import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class WSDocument {

  public static final class WSDocumentItem {
    private final String itemId;
    private final String quantity;
    private String price;
    private String vatMode;
    private String vat;
    private String vatPercent;
    private String note;

    private WSDocumentItem(String itemId, String quantity) {
      this.itemId = itemId;
      this.quantity = quantity;
    }

    public void setNote(String note) {
      this.note = note;
    }

    public void setPrice(String price) {
      this.price = price;
    }

    public void setVat(String vatAmount, Boolean isPercent, Boolean isIncluded) {
      this.vatMode = BeeUtils.unbox(isIncluded) ? "T" : "S";
      this.vat = vatAmount;
      this.vatPercent = BeeUtils.unbox(isPercent) ? "%" : null;
    }
  }

  private final String documentId;
  private final DateTime date;
  private final String operation;
  private final String warehouse;

  private final String company;
  private String supplier;
  private String payer;

  private final List<WSDocumentItem> items = Lists.newArrayList();

  public WSDocument(String documentId, DateTime date, String operation, String company,
      String warehouse) {
    this.documentId = documentId;
    this.date = date;
    this.operation = operation;
    this.company = company;
    this.warehouse = warehouse;
  }

  public WSDocumentItem addItem(String itemId, String quantity) {
    WSDocumentItem item = new WSDocumentItem(itemId, quantity);
    items.add(item);
    return item;
  }

  public String getXml() {
    StringBuilder sb = new StringBuilder("<VFPData>");

    for (WSDocumentItem item : items) {
      sb.append("<row>")
          .append(ButentWS.tag("apyv_id", documentId))
          .append(ButentWS.tag("data", date))
          .append(ButentWS.tag("operacija", operation))
          .append(ButentWS.tag("tiekejas", supplier))
          .append(ButentWS.tag("klientas", company))
          .append(ButentWS.tag("moketojas", payer))
          .append(ButentWS.tag("sandelis", warehouse))
          .append(ButentWS.tag("preke", item.itemId))
          .append(ButentWS.tag("kiekis", item.quantity))
          .append(ButentWS.tag("pastaba", item.note));

      if (!BeeUtils.isEmpty(item.price)) {
        sb.append(ButentWS.tag("kaina", item.price));

        if (!BeeUtils.isEmpty(item.vat)) {
          sb.append(ButentWS.tag("pvm_stat", item.vatMode))
              .append(ButentWS.tag("pvm", item.vat))
              .append(ButentWS.tag("pvm_p_md", item.vatPercent));
        }
      }
      sb.append("</row>");
    }
    return sb.append("</VFPData>").toString();
  }

  public void setPayer(String payer) {
    this.payer = payer;
  }

  public void setSupplier(String supplier) {
    this.supplier = supplier;
  }
}
