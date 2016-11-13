package com.butent.bee.shared.modules.finance;

import com.butent.bee.shared.modules.finance.analysis.IndicatorBalance;
import com.butent.bee.shared.modules.finance.analysis.IndicatorKind;
import com.butent.bee.shared.modules.finance.analysis.IndicatorSource;
import com.butent.bee.shared.utils.EnumUtils;

public final class FinanceConstants {

  public static final String SVC_POST_TRADE_DOCUMENT = "postTradeDocument";

  public static final String TBL_FINANCIAL_RECORDS = "FinancialRecords";

  public static final String TBL_FINANCE_CONFIGURATION = "FinanceConfiguration";
  public static final String TBL_FINANCE_CONTENTS = "FinanceContents";
  public static final String TBL_FINANCE_DISTRIBUTION = "FinanceDistribution";

  public static final String VIEW_FINANCIAL_RECORDS = "FinancialRecords";

  public static final String VIEW_FINANCE_CONFIGURATION = "FinanceConfiguration";
  public static final String VIEW_FINANCE_CONTENTS = "FinanceContents";

  public static final String VIEW_FINANCE_DISTRIBUTION = "FinanceDistribution";
  public static final String VIEW_FINANCE_DISTRIBUTION_OF_ITEMS = "FinanceDistributionOfItems";
  public static final String VIEW_FINANCE_DISTRIBUTION_OF_TRADE_OPERATIONS =
      "FinanceDistributionOfTradeOperations";
  public static final String VIEW_FINANCE_DISTRIBUTION_OF_TRADE_DOCUMENTS =
      "FinanceDistributionOfTradeDocuments";

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
  public static final String COL_FIN_QUANTITY = "Quantity";
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
  public static final String COL_ADVANCE_PAYMENTS_GIVEN = "AdvancePaymentsGiven";
  public static final String COL_ADVANCE_PAYMENTS_RECEIVED = "AdvancePaymentsReceived";
  public static final String COL_COST_OF_MERCHANDISE = "CostOfMerchandise";

  public static final String COL_TRADE_ACCOUNTS_PRECEDENCE = "TradeAccountsPrecedence";
  public static final String COL_TRADE_DIMENSIONS_PRECEDENCE = "TradeDimensionsPrecedence";

  public static final String COL_FIN_DISTR_DATE_FROM = "DateFrom";
  public static final String COL_FIN_DISTR_DATE_TO = "DateTo";
  public static final String COL_FIN_DISTR_DEBIT = "Debit";
  public static final String COL_FIN_DISTR_CREDIT = "Credit";
  public static final String COL_FIN_DISTR_PERCENT = "Percent";
  public static final String COL_FIN_DISTR_DEBIT_REPLACEMENT = "DebitReplacement";
  public static final String COL_FIN_DISTR_CREDIT_REPLACEMENT = "CreditReplacement";
  public static final String COL_FIN_DISTR_ITEM = "Item";
  public static final String COL_FIN_DISTR_TRADE_OPERATION = "Operation";
  public static final String COL_FIN_DISTR_TRADE_DOCUMENT = "TradeDocument";

  public static final String COL_FIN_INDICATOR_KIND = "IndicatorKind";
  public static final String COL_FIN_INDICATOR_NAME = "IndicatorName";
  public static final String COL_FIN_INDICATOR_ABBREVIATION = "IndicatorAbbreviation";
  public static final String COL_FIN_INDICATOR_SOURCE = "IndicatorSource";
  public static final String COL_FIN_INDICATOR_SCRIPT = "IndicatorScript";
  public static final String COL_FIN_INDICATOR_BALANCE = "IndicatorBalance";
  public static final String COL_FIN_INDICATOR_CLOSING_ENTRIES = "IndicatorClosingEntries";
  public static final String COL_FIN_INDICATOR_IS_PERCENT = "IndicatorIsPercent";
  public static final String COL_FIN_INDICATOR_SCALE = "IndicatorScale";

  public static final String ALS_JOURNAL_BACKGROUND = "JournalBackground";
  public static final String ALS_JOURNAL_FOREGROUND = "JournalForeground";

  public static final String ALS_DEBIT_CODE = "DebitCode";
  public static final String ALS_DEBIT_BACKGROUND = "DebitBackground";
  public static final String ALS_DEBIT_FOREGROUND = "DebitForeground";

  public static final String ALS_CREDIT_CODE = "CreditCode";
  public static final String ALS_CREDIT_BACKGROUND = "CreditBackground";
  public static final String ALS_CREDIT_FOREGROUND = "CreditForeground";

  public static final String ALS_DEBIT_REPLACEMENT_BACKGROUND = "DebitReplacementBackground";
  public static final String ALS_DEBIT_REPLACEMENT_FOREGROUND = "DebitReplacementForeground";

  public static final String ALS_CREDIT_REPLACEMENT_BACKGROUND = "CreditReplacementBackground";
  public static final String ALS_CREDIT_REPLACEMENT_FOREGROUND = "CreditReplacementForeground";

  public static final String GRID_FINANCIAL_RECORDS = "FinancialRecords";
  public static final String GRID_TRADE_DOCUMENT_FINANCIAL_RECORDS =
      "TradeDocumentFinancialRecords";

  public static final String GRID_ITEM_FINANCE_DISTRIBUTION = "ItemFinanceDistribution";
  public static final String GRID_TRADE_OPERATION_FINANCE_DISTRIBUTION =
      "OperationFinanceDistribution";
  public static final String GRID_TRADE_DOCUMENT_FINANCE_DISTRIBUTION =
      "TradeDocumentFinanceDistribution";

  public static final String GRID_FINANCE_DISTRIBUTION_OF_ITEMS = "FinanceDistributionOfItems";
  public static final String GRID_FINANCE_DISTRIBUTION_OF_TRADE_OPERATIONS =
      "FinanceDistributionOfTradeOperations";
  public static final String GRID_FINANCE_DISTRIBUTION_OF_TRADE_DOCUMENTS =
      "FinanceDistributionOfTradeDocuments";

  public static final String GRID_FINANCE_CONTENTS = "FinanceContents";

  public static final String GRID_FINANCIAL_INDICATORS_PRIMARY = "FinancialIndicatorsPrimary";
  public static final String GRID_FINANCIAL_INDICATORS_SECONDARY = "FinancialIndicatorsSecondary";

  public static final String FORM_FINANCE_DEFAULT_ACCOUNTS = "FinanceDefaultAccounts";
  public static final String FORM_FINANCE_POSTING_PRECEDENCE = "FinancePostingPrecedence";

  public static void register() {
    EnumUtils.register(IndicatorKind.class);
    EnumUtils.register(IndicatorSource.class);
    EnumUtils.register(IndicatorBalance.class);
  }

  private FinanceConstants() {
  }
}
