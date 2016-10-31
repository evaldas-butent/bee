package com.butent.bee.shared.modules.finance;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public final class TradeAccounts {

  private static final String COL_COST_ACCOUNT = "CostAccount";

  private static final String COL_TRADE_PAYABLES = "TradePayables";
  private static final String COL_TRADE_RECEIVABLES = "TradeReceivables";

  private static final String COL_VAT_PAYABLE = "VatPayable";
  private static final String COL_VAT_RECEIVABLE = "VatReceivable";

  private static final String COL_SALES_REVENUE = "SalesRevenue";
  private static final String COL_SALES_DISCOUNTS = "SalesDiscounts";

  private static final String COL_COST_OF_GOODS_SOLD = "CostOfGoodsSold";
  private static final String COL_WRITE_OFF_ACCOUNT = "WriteOffAccount";

  public static TradeAccounts create(BeeRowSet rowSet, IsRow row) {
    Assert.notNull(rowSet);
    return create(rowSet.getColumns(), row);
  }

  public static TradeAccounts createAvailable(BeeRowSet rowSet, IsRow row) {
    return (rowSet == null) ? null : createAvailable(rowSet.getColumns(), row);
  }

  public static TradeAccounts create(List<? extends IsColumn> columns, IsRow row) {
    Assert.notEmpty(columns);
    Assert.notNull(row);

    Long costAccount = DataUtils.getLong(columns, row, COL_COST_ACCOUNT);
    Long tradePayables = DataUtils.getLong(columns, row, COL_TRADE_PAYABLES);
    Long tradeReceivables = DataUtils.getLong(columns, row, COL_TRADE_RECEIVABLES);
    Long vatPayable = DataUtils.getLong(columns, row, COL_VAT_PAYABLE);
    Long vatReceivable = DataUtils.getLong(columns, row, COL_VAT_RECEIVABLE);
    Long salesRevenue = DataUtils.getLong(columns, row, COL_SALES_REVENUE);
    Long salesDiscounts = DataUtils.getLong(columns, row, COL_SALES_DISCOUNTS);
    Long costOfGoodsSold = DataUtils.getLong(columns, row, COL_COST_OF_GOODS_SOLD);
    Long writeOffAccount = DataUtils.getLong(columns, row, COL_WRITE_OFF_ACCOUNT);

    return new TradeAccounts(costAccount, tradePayables, tradeReceivables,
        vatPayable, vatReceivable, salesRevenue, salesDiscounts, costOfGoodsSold, writeOffAccount);
  }

  public static TradeAccounts createAvailable(List<? extends IsColumn> columns, IsRow row) {
    Long costAccount = DataUtils.getLongQuietly(columns, row, COL_COST_ACCOUNT);
    if (costAccount == null) {
      costAccount = DataUtils.getLongQuietly(columns, row,
          FinanceConstants.COL_COST_OF_MERCHANDISE);
    }

    Long tradePayables = DataUtils.getLongQuietly(columns, row, COL_TRADE_PAYABLES);
    Long tradeReceivables = DataUtils.getLongQuietly(columns, row, COL_TRADE_RECEIVABLES);
    Long vatPayable = DataUtils.getLongQuietly(columns, row, COL_VAT_PAYABLE);
    Long vatReceivable = DataUtils.getLongQuietly(columns, row, COL_VAT_RECEIVABLE);
    Long salesRevenue = DataUtils.getLongQuietly(columns, row, COL_SALES_REVENUE);
    Long salesDiscounts = DataUtils.getLongQuietly(columns, row, COL_SALES_DISCOUNTS);
    Long costOfGoodsSold = DataUtils.getLongQuietly(columns, row, COL_COST_OF_GOODS_SOLD);
    Long writeOffAccount = DataUtils.getLongQuietly(columns, row, COL_WRITE_OFF_ACCOUNT);

    return new TradeAccounts(costAccount, tradePayables, tradeReceivables,
        vatPayable, vatReceivable, salesRevenue, salesDiscounts, costOfGoodsSold, writeOffAccount);
  }

  public static TradeAccounts merge(List<TradeAccounts> list) {
    Assert.notEmpty(list);

    Long costAccount = null;
    Long tradePayables = null;
    Long tradeReceivables = null;
    Long vatPayable = null;
    Long vatReceivable = null;
    Long salesRevenue = null;
    Long salesDiscounts = null;
    Long costOfGoodsSold = null;
    Long writeOffAccount = null;

    for (TradeAccounts ta : list) {
      if (ta != null && !ta.isEmpty()) {
        if (costAccount == null) {
          costAccount = ta.getCostAccount();
        }
        if (tradePayables == null) {
          tradePayables = ta.getTradePayables();
        }
        if (tradeReceivables == null) {
          tradeReceivables = ta.getTradeReceivables();
        }
        if (vatPayable == null) {
          vatPayable = ta.getVatPayable();
        }
        if (vatReceivable == null) {
          vatReceivable = ta.getVatReceivable();
        }
        if (salesRevenue == null) {
          salesRevenue = ta.getSalesRevenue();
        }
        if (salesDiscounts == null) {
          salesDiscounts = ta.getSalesDiscounts();
        }
        if (costOfGoodsSold == null) {
          costOfGoodsSold = ta.getCostOfGoodsSold();
        }
        if (writeOffAccount == null) {
          writeOffAccount = ta.getWriteOffAccount();
        }

        if (costAccount != null
            && tradePayables != null && tradeReceivables != null
            && vatPayable != null && vatReceivable != null
            && salesRevenue != null && salesDiscounts != null
            && costOfGoodsSold != null && writeOffAccount != null) {

          break;
        }
      }
    }

    return new TradeAccounts(costAccount, tradePayables, tradeReceivables,
        vatPayable, vatReceivable, salesRevenue, salesDiscounts, costOfGoodsSold, writeOffAccount);
  }

  private final Long costAccount;

  private final Long tradePayables;
  private final Long tradeReceivables;

  private final Long vatPayable;
  private final Long vatReceivable;

  private final Long salesRevenue;
  private final Long salesDiscounts;

  private final Long costOfGoodsSold;
  private final Long writeOffAccount;

  public TradeAccounts(Long costAccount, Long tradePayables, Long tradeReceivables,
      Long vatPayable, Long vatReceivable, Long salesRevenue, Long salesDiscounts,
      Long costOfGoodsSold, Long writeOffAccount) {

    this.costAccount = costAccount;
    this.tradePayables = tradePayables;
    this.tradeReceivables = tradeReceivables;
    this.vatPayable = vatPayable;
    this.vatReceivable = vatReceivable;
    this.salesRevenue = salesRevenue;
    this.salesDiscounts = salesDiscounts;
    this.costOfGoodsSold = costOfGoodsSold;
    this.writeOffAccount = writeOffAccount;
  }

  public Long getCostAccount() {
    return costAccount;
  }

  public Long getTradePayables() {
    return tradePayables;
  }

  public Long getTradeReceivables() {
    return tradeReceivables;
  }

  public Long getVatPayable() {
    return vatPayable;
  }

  public Long getVatReceivable() {
    return vatReceivable;
  }

  public Long getSalesRevenue() {
    return salesRevenue;
  }

  public Long getSalesDiscounts() {
    return salesDiscounts;
  }

  public Long getCostOfGoodsSold() {
    return costOfGoodsSold;
  }

  public Long getWriteOffAccount() {
    return writeOffAccount;
  }

  public boolean isEmpty() {
    return costAccount == null
        && tradePayables == null && tradeReceivables == null
        && vatPayable == null && vatReceivable == null
        && salesRevenue == null && salesDiscounts == null
        && costOfGoodsSold == null && writeOffAccount == null;
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return BeeConst.EMPTY;
    } else {
      return BeeUtils.joinOptions(COL_COST_ACCOUNT, costAccount,
          COL_TRADE_PAYABLES, tradePayables, COL_TRADE_RECEIVABLES, tradeReceivables,
          COL_VAT_PAYABLE, vatPayable, COL_VAT_RECEIVABLE, vatReceivable,
          COL_SALES_REVENUE, salesRevenue, COL_SALES_DISCOUNTS, salesDiscounts,
          COL_COST_OF_GOODS_SOLD, costOfGoodsSold, COL_WRITE_OFF_ACCOUNT, writeOffAccount);
    }
  }
}
