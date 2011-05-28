package com.butent.bee.client.grid;

import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.shared.data.IsColumn;

public class CurrencyColumn extends DecimalColumn {
  
  public CurrencyColumn(int index, IsColumn dataColumn) {
    this(Format.getDefaultCurrencyFormat(), index, dataColumn);
  }

  public CurrencyColumn(NumberFormat format, int index, IsColumn dataColumn) {
    super(format, index, dataColumn);
  }
}
