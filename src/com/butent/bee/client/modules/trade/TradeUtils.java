package com.butent.bee.client.modules.trade;

import com.google.common.collect.Maps;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.server.modules.commons.ExchangeUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class TradeUtils {

  private static final String STYLE_ITEMS = "itemsInfo-";
  private static final String STYLE_ITEMS_TABLE = STYLE_ITEMS + "table";
  private static final String STYLE_ITEMS_LABEL = STYLE_ITEMS + "label";
  private static final String STYLE_ITEMS_DATA = STYLE_ITEMS + "data";

  public static void getSaleItems(Long saleId, final HasWidgets target, Long currencyTo) {
    Assert.notNull(target);
    target.clear();

    if (!DataUtils.isId(saleId)) {
      return;
    }
    ParameterList args = TradeKeeper.createArgs(SVC_ITEMS_INFO);
    args.addDataItem(COL_SALE, saleId);

    if (DataUtils.isId(currencyTo)) {
      args.addDataItem(ExchangeUtils.FLD_CURRENCY, currencyTo);
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (response.hasErrors()) {
          return;
        }
        SimpleRowSet rs = SimpleRowSet.restore((String) response.getResponse());

        Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put(COL_NAME, Global.CONSTANTS.item());
        cols.put(COL_ARTICLE, Global.CONSTANTS.article());
        cols.put(COL_QUANTITY, Global.CONSTANTS.quantity());
        cols.put(COL_UNIT, Global.CONSTANTS.unit());
        cols.put(COL_PRICE, Global.CONSTANTS.price());
        cols.put(COL_AMOUNT, Global.CONSTANTS.amount());
        cols.put(COL_VAT, Global.CONSTANTS.vat());

        HtmlTable table = new HtmlTable();
        table.setStyleName(STYLE_ITEMS_TABLE);
        table.getRowFormatter().addStyleName(0, STYLE_ITEMS_LABEL);
        int c = 0;
        int qtyIdx = BeeConst.UNDEF;
        int sumIdx = BeeConst.UNDEF;

        for (String col : cols.keySet()) {
          Widget cell = new CustomDiv(STYLE_ITEMS + col.toLowerCase());
          cell.getElement().setInnerText(cols.get(col));
          table.setWidget(0, c++, cell);

          if (BeeUtils.same(col, COL_QUANTITY)) {
            qtyIdx = c - 1;
          } else if (BeeUtils.same(col, COL_AMOUNT)) {
            sumIdx = c - 1;
          }
        }
        String currency = null;
        NumberFormat formater = NumberFormat.getFormat("0.00");
        double qtyTotal = 0;
        double vatTotal = 0;
        double sumTotal = 0;
        boolean vatExists = false;

        for (int i = 0; i < rs.getNumberOfRows(); i++) {
          table.getRowFormatter().addStyleName(i + 1, STYLE_ITEMS_DATA);
          c = 0;
          double qty = rs.getDouble(i, COL_QUANTITY);
          qtyTotal += qty;
          double sum = qty * BeeUtils.unbox(rs.getDouble(i, COL_PRICE));
          double vat = BeeUtils.unbox(rs.getDouble(i, COL_VAT));

          if (BeeUtils.isEmpty(currency)) {
            currency = " " + rs.getValue(i, ExchangeUtils.FLD_CURRENCY);
          }
          if (!vatExists) {
            vatExists = (rs.getDouble(i, COL_VAT) != null);
          }
          if (BeeUtils.unbox(rs.getBoolean(i, COL_VAT_INCL))) {
            if (BeeUtils.unbox(rs.getBoolean(i, COL_VAT_PERC))) {
              vat = sum - sum / (1 + vat / 100);
            }
            sum -= vat;
          } else {
            if (BeeUtils.unbox(rs.getBoolean(i, COL_VAT_PERC))) {
              vat = sum / 100 * vat;
            }
          }
          vatTotal += vat;
          sum = BeeUtils.round(sum, 2);
          sumTotal += sum;

          for (String col : cols.keySet()) {
            Widget cell = new CustomDiv(STYLE_ITEMS + col.toLowerCase());
            String value;

            if (BeeUtils.same(col, COL_QUANTITY)) {
              value = BeeUtils.toString(qty);

            } else if (BeeUtils.same(col, COL_PRICE)) {
              value = BeeUtils.toString(BeeUtils.round(sum / qty, 5));

            } else if (BeeUtils.same(col, COL_VAT)) {
              value = rs.getValue(i, col);

              if (value != null && BeeUtils.unbox(rs.getBoolean(i, COL_VAT_PERC))) {
                value = BeeUtils.removeTrailingZeros(value) + "%";
              }
            } else if (BeeUtils.same(col, COL_AMOUNT)) {
              value = formater.format(sum);

            } else {
              value = rs.getValue(i, col);
            }
            cell.getElement().setInnerText(value);
            table.setWidget(i + 1, c++, cell);
          }
        }
        vatTotal = BeeUtils.round(vatTotal, 2);
        c = table.getRowCount();

        if (qtyIdx != BeeConst.UNDEF) {
          Widget cell = new CustomDiv(STYLE_ITEMS + "totalQuantity");
          cell.getElement().setInnerText(BeeUtils.toString(qtyTotal));
          table.setWidget(c, qtyIdx, cell);
        }
        if (sumIdx != BeeConst.UNDEF) {
          Widget cell = new CustomDiv(STYLE_ITEMS + "totalAmount");
          cell.getElement().setInnerText(formater.format(sumTotal) + currency);
          table.setWidget(c, sumIdx, cell);

          if (sumIdx > 0) {
            cell = new CustomDiv(STYLE_ITEMS + "totalAmount-label");
            cell.getElement().setInnerText(Global.CONSTANTS.amount());
            table.setWidget(c, sumIdx - 1, cell);
          }
          if (vatExists) {
            cell = new CustomDiv(STYLE_ITEMS + "totalVat");
            cell.getElement().setInnerText(formater.format(vatTotal) + currency);
            table.setWidget(++c, sumIdx, cell);

            if (sumIdx > 0) {
              cell = new CustomDiv(STYLE_ITEMS + "totalVat-label");
              cell.getElement().setInnerText(Global.CONSTANTS.vatSum());
              table.setWidget(c, sumIdx - 1, cell);
            }
            cell = new CustomDiv(STYLE_ITEMS + "total");
            cell.getElement().setInnerText(formater.format(sumTotal + vatTotal) + currency);
            table.setWidget(++c, sumIdx, cell);

            if (sumIdx > 0) {
              cell = new CustomDiv(STYLE_ITEMS + "total-label");
              cell.getElement().setInnerText(Global.CONSTANTS.total());
              table.setWidget(c, sumIdx - 1, cell);
            }
          }
        }
        target.add(table);
      }
    });
  }

  public static void getTotalInWords(Double amount, final String currencyName,
      final String minorName, final HasWidgets target, String locale) {
    Assert.notNull(target);
    target.clear();

    if (amount == null || amount <= 0) {
      return;
    }
    long number = BeeUtils.toLong(Math.floor(amount));
    final int fraction = BeeUtils.toInt((amount - number) * 100);

    ParameterList args = TradeKeeper.createArgs(SVC_NUMBER_TO_WORDS);
    args.addDataItem(COL_AMOUNT, number);

    if (!BeeUtils.isEmpty(locale)) {
      args.addDataItem("Locale", locale);
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (response.hasErrors()) {
          return;
        }
        target.add(new BeeLabel(BeeUtils.joinWords(response.getResponse(), currencyName, fraction,
            minorName)));
      }
    });
  }
}
