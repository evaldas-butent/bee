package com.butent.bee.shared.modules.finance;

public final class FinanceConstants {

  public static final String TBL_FINANCIAL_RECORDS = "FinancialRecords";

  public static final String VIEW_FINANCIAL_RECORDS = "FinancialRecords";

  public static final String COL_FIN_JOURNAL = "Journal";
  public static final String COL_FIN_DATE = "Date";
  public static final String COL_FIN_COMPANY = "Company";
  public static final String COL_FIN_CONTENT = "Content";
  public static final String COL_FIN_DEBIT = "Debit";
  public static final String COL_FIN_DEBIT_SERIES = "DebitSeries";
  public static final String COL_FIN_DEBIT_DOCUMENT = "DebitDocument";
  public static final String COL_FIN_CREDIT = "Credit";
  public static final String COL_FIN_CREDIT_SERIES = "CreditSeries";
  public static final String COL_FIN_CREDIT_DOCUMENT = "CreditDocument";
  public static final String COL_FIN_AMOUNT = "Amount";
  public static final String COL_FIN_CURRENCY = "Currency";
  public static final String COL_FIN_TRADE_DOCUMENT = "TradeDocument";
  public static final String COL_FIN_TRADE_PAYMENT = "TradePayment";
  public static final String COL_FIN_EMPLOYEE = "Employee";

  public static final String COL_DEFAULT_JOURNAL = "DefaultJournal";
  public static final String COL_PETTY_CASH = "PettyCash";
  public static final String COL_CASH_IN_BANK = "CashInBank";
  public static final String COL_RECEIVABLES_FROM_EMPLOYEES = "ReceivablesFromEmployees";
  public static final String COL_LIABILITIES_TO_EMPLOYEES = "LiabilitiesToEmployees";
  public static final String COL_REVENUE_AND_EXPENSE_SUMMARY = "RevenueAndExpenseSummary";
  public static final String COL_TRANSITORY_ACCOUNT = "TransitoryAccount";
  public static final String COL_FOREIGN_EXCHANGE_GAIN = "ForeignExchangeGain";
  public static final String COL_FOREIGN_EXCHANGE_LOSS = "ForeignExchangeLoss";
  public static final String COL_VAT_PAYABLE = "VatPayable";
  public static final String COL_VAT_RECEIVABLE = "VatReceivable";
  public static final String COL_TRADE_PAYABLES = "TradePayables";
  public static final String COL_TRADE_RECEIVABLES = "TradeReceivables";
  public static final String COL_ADVANCE_PAYMENTS_GIVEN = "AdvancePaymentsGiven";
  public static final String COL_ADVANCE_PAYMENTS_RECEIVED = "AdvancePaymentsReceived";
  public static final String COL_SALES_REVENUE = "SalesRevenue";
  public static final String COL_COST_OF_GOODS_SOLD = "CostOfGoodsSold";
  public static final String COL_COST_OF_MERCHANDISE = "CostOfMerchandise";

  public static final String ALS_JOURNAL_BACKGROUND = "JournalBackground";
  public static final String ALS_JOURNAL_FOREGROUND = "JournalForeground";

  public static final String ALS_DEBIT_BACKGROUND = "DebitBackground";
  public static final String ALS_DEBIT_FOREGROUND = "DebitForeground";

  public static final String ALS_CREDIT_BACKGROUND = "CreditBackground";
  public static final String ALS_CREDIT_FOREGROUND = "CreditForeground";

  public static final String GRID_FINANCIAL_RECORDS = "FinancialRecords";

  public static final String FORM_FINANCE_CONFIGURATION = "FinanceConfiguration";

  private FinanceConstants() {
  }
}
