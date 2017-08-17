package com.butent.bee.server.modules.trade;

import com.butent.bee.shared.modules.trade.TradeCostBasis;

import java.util.Objects;

class TradeExpenditure {

  private final TradeCostBasis costBasis;

  private final Long expenditureType;
  private final Long supplier;

  TradeExpenditure(TradeCostBasis costBasis, Long expenditureType, Long supplier) {
    this.costBasis = costBasis;

    this.expenditureType = expenditureType;
    this.supplier = supplier;
  }

  TradeCostBasis getCostBasis() {
    return costBasis;
  }

  Long getExpenditureType() {
    return expenditureType;
  }

  Long getSupplier() {
    return supplier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TradeExpenditure)) {
      return false;
    }
    TradeExpenditure that = (TradeExpenditure) o;
    return costBasis == that.costBasis
        && Objects.equals(expenditureType, that.expenditureType)
        && Objects.equals(supplier, that.supplier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(costBasis, expenditureType, supplier);
  }
}
