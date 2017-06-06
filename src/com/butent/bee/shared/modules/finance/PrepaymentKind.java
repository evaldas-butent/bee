package com.butent.bee.shared.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.modules.trade.DebtKind;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum PrepaymentKind implements HasLocalizedCaption {
  CUSTOMERS {
    @Override
    public String defaultAccountColumn() {
      return COL_ADVANCE_PAYMENTS_RECEIVED;
    }

    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.prepaymentCustomersShort();
    }

    @Override
    public DebtKind getDebtKInd() {
      return DebtKind.RECEIVABLE;
    }

    @Override
    public String getFullCaption(Dictionary dictionary) {
      return dictionary.prepaymentCustomers();
    }

    @Override
    public NormalBalance normalBalance() {
      return NormalBalance.CREDIT;
    }

    @Override
    public String tradePaymentsGrid() {
      return GRID_OUTSTANDING_PREPAYMENT_RECEIVED;
    }
  },

  SUPPLIERS {
    @Override
    public String defaultAccountColumn() {
      return COL_ADVANCE_PAYMENTS_GIVEN;
    }

    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.prepaymentSuppliersShort();
    }

    @Override
    public DebtKind getDebtKInd() {
      return DebtKind.PAYABLE;
    }

    @Override
    public String getFullCaption(Dictionary dictionary) {
      return dictionary.prepaymentSuppliers();
    }

    @Override
    public NormalBalance normalBalance() {
      return NormalBalance.DEBIT;
    }

    @Override
    public String tradePaymentsGrid() {
      return GRID_OUTSTANDING_PREPAYMENT_GIVEN;
    }
  };

  public abstract String defaultAccountColumn();

  public abstract DebtKind getDebtKInd();

  public abstract String getFullCaption(Dictionary dictionary);

  public abstract NormalBalance normalBalance();

  public String styleSuffix() {
    return name().toLowerCase();
  }

  public abstract String tradePaymentsGrid();
}
