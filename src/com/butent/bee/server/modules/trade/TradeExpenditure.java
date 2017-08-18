package com.butent.bee.server.modules.trade;

import com.butent.bee.shared.modules.trade.TradeCostBasis;

import java.util.Objects;

class TradeExpenditure {

  private final TradeCostBasis costBasis;

  private final Long expenditureType;
  private final Long supplier;

  private final String series;
  private final String number;

  TradeExpenditure(TradeCostBasis costBasis, Long expenditureType, Long supplier,
      String series, String number) {

    this.costBasis = costBasis;

    this.expenditureType = expenditureType;
    this.supplier = supplier;

    this.series = series;
    this.number = number;
  }

  TradeCostBasis getCostBasis() {
    return costBasis;
  }

  Long getExpenditureType() {
    return expenditureType;
  }

  String getNumber() {
    return number;
  }

  String getSeries() {
    return series;
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
        && Objects.equals(supplier, that.supplier)
        && Objects.equals(series, that.series)
        && Objects.equals(number, that.number);
  }

  @Override
  public int hashCode() {
    return Objects.hash(costBasis, expenditureType, supplier, series, number);
  }
}
