package com.butent.bee.client.modules.finance;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfoList;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.finance.analysis.IndicatorBalance;
import com.butent.bee.shared.modules.finance.analysis.IndicatorKind;
import com.butent.bee.shared.modules.finance.analysis.IndicatorSource;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ChartOfAccountsGrid extends AbstractGridInterceptor {

  private enum IndicatorType implements HasCaption {
    TURNOVER {
      @Override
      public String getCaption() {
        return Localized.dictionary().finTurnover();
      }

      @Override
      String getName(String accountName) {
        return Localized.dictionary().finIndicatorNameTurnover(accountName);
      }

      @Override
      String getAbbreviation(String accountCode) {
        return Localized.dictionary().finIndicatorAbbreviationTurnover(accountCode);
      }

      @Override
      IndicatorBalance getIndicatorBalance() {
        return IndicatorBalance.TURNOVER;
      }

      @Override
      int getRowCount() {
        return 2;
      }
    },

    DEBIT {
      @Override
      public String getCaption() {
        return Localized.dictionary().finDebitOnly();
      }

      @Override
      String getName(String accountName) {
        return Localized.dictionary().finIndicatorNameDebit(accountName);
      }

      @Override
      String getAbbreviation(String accountCode) {
        return Localized.dictionary().finIndicatorAbbreviationDebit(accountCode);
      }

      @Override
      IndicatorBalance getIndicatorBalance() {
        return IndicatorBalance.TURNOVER;
      }

      @Override
      int getRowCount() {
        return 1;
      }

      @Override
      Long getRowDebit(int rowIndex, long accountId, boolean normalBalanceIsCredit) {
        if (rowIndex == 0) {
          return accountId;
        } else {
          return null;
        }
      }

      @Override
      Long getRowCredit(int rowIndex, long accountId, boolean normalBalanceIsCredit) {
        return null;
      }
    },

    CREDIT {
      @Override
      public String getCaption() {
        return Localized.dictionary().finCreditOnly();
      }

      @Override
      String getName(String accountName) {
        return Localized.dictionary().finIndicatorNameCredit(accountName);
      }

      @Override
      String getAbbreviation(String accountCode) {
        return Localized.dictionary().finIndicatorAbbreviationCredit(accountCode);
      }

      @Override
      IndicatorBalance getIndicatorBalance() {
        return IndicatorBalance.TURNOVER;
      }

      @Override
      int getRowCount() {
        return 1;
      }

      @Override
      Long getRowDebit(int rowIndex, long accountId, boolean normalBalanceIsCredit) {
        return null;
      }

      @Override
      Long getRowCredit(int rowIndex, long accountId, boolean normalBalanceIsCredit) {
        if (rowIndex == 0) {
          return accountId;
        } else {
          return null;
        }
      }
    },

    OPENING_BALANCE {
      @Override
      public String getCaption() {
        return Localized.dictionary().finOpeningBalance();
      }

      @Override
      String getName(String accountName) {
        return Localized.dictionary().finIndicatorNameOpeningBalance(accountName);
      }

      @Override
      String getAbbreviation(String accountCode) {
        return Localized.dictionary().finIndicatorAbbreviationOpeningBalance(accountCode);
      }

      @Override
      IndicatorBalance getIndicatorBalance() {
        return IndicatorBalance.OPENING_BALANCE;
      }

      @Override
      int getRowCount() {
        return 2;
      }
    },

    CLOSING_BALANCE {
      @Override
      public String getCaption() {
        return Localized.dictionary().finClosingBalance();
      }

      @Override
      String getName(String accountName) {
        return Localized.dictionary().finIndicatorNameClosingBalance(accountName);
      }

      @Override
      String getAbbreviation(String accountCode) {
        return Localized.dictionary().finIndicatorAbbreviationClosingBalance(accountCode);
      }

      @Override
      IndicatorBalance getIndicatorBalance() {
        return IndicatorBalance.CLOSING_BALANCE;
      }

      @Override
      int getRowCount() {
        return 2;
      }
    };

    abstract String getName(String accountName);

    abstract String getAbbreviation(String accountCode);

    abstract IndicatorBalance getIndicatorBalance();

    abstract int getRowCount();

    Long getRowDebit(int rowIndex, long accountId, boolean normalBalanceIsCredit) {
      if (rowIndex == 0 && !normalBalanceIsCredit || rowIndex == 1 && normalBalanceIsCredit) {
        return accountId;
      } else {
        return null;
      }
    }

    Long getRowCredit(int rowIndex, long accountId, boolean normalBalanceIsCredit) {
      if (rowIndex == 0 && normalBalanceIsCredit || rowIndex == 1 && !normalBalanceIsCredit) {
        return accountId;
      } else {
        return null;
      }
    }
  }

  private static final int NAME_PRECISION =
      Data.getColumnPrecision(VIEW_FINANCIAL_INDICATORS, COL_FIN_INDICATOR_NAME);
  private static final int ABBREVIATION_PRECISION =
      Data.getColumnPrecision(VIEW_FINANCIAL_INDICATORS, COL_FIN_INDICATOR_ABBREVIATION);

  ChartOfAccountsGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new ChartOfAccountsGrid();
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (presenter != null && presenter.getHeader() != null
        && BeeKeeper.getUser().canCreateData(VIEW_FINANCIAL_INDICATORS)) {

      Button create = new Button(Localized.dictionary().finIndicatorCreate(), event -> {
        if (getActiveRow() != null) {
          String accountCode = BeeUtils.trim(getStringValue(COL_ACCOUNT_CODE));
          String accountName = BeeUtils.trim(getStringValue(COL_ACCOUNT_NAME));

          Boolean accountNormalBalance = getBooleanValue(COL_ACCOUNT_NORMAL_BALANCE);

          if (BeeUtils.allNotEmpty(accountCode, accountName)) {
            onCreateIndicator(getActiveRowId(), sanitizeAccountCode(accountCode), accountName,
                normalBalanceIsCredit(accountNormalBalance),
                getStringValue(COL_BACKGROUND), getStringValue(COL_FOREGROUND));
          }
        }
      });
      presenter.getHeader().addCommandItem(create);
    }

    super.afterCreatePresenter(presenter);
  }

  @Override
  public ColumnDescription beforeCreateColumn(GridView gridView,
      ColumnDescription columnDescription) {

    if (columnDescription != null && columnDescription.is(COL_ACCOUNT_NORMAL_BALANCE)) {
      columnDescription.setFormat(BeeUtils.joinWords(Localized.dictionary().finCreditShort(),
          Localized.dictionary().finDebitShort()));
    }

    return super.beforeCreateColumn(gridView, columnDescription);
  }

  private static void onCreateIndicator(final long accountId, final String accountCode,
      final String accountName, final boolean normalBalanceIsCredit,
      final String background, final String foreground) {

    final EnumMap<IndicatorType, String> names = new EnumMap<>(IndicatorType.class);
    final EnumMap<IndicatorType, String> abbreviations = new EnumMap<>(IndicatorType.class);

    for (IndicatorType type : IndicatorType.values()) {
      names.put(type, normalizeIndicatorName(type.getName(accountName)));
      abbreviations.put(type, normalizeIndicatorAbbreviation(type.getAbbreviation(accountCode)));
    }

    List<String> columns = Arrays.asList(COL_FIN_INDICATOR_NAME, COL_FIN_INDICATOR_ABBREVIATION);

    Filter filter = Filter.or(
        Filter.anyString(COL_FIN_INDICATOR_NAME, names.values()),
        Filter.anyString(COL_FIN_INDICATOR_ABBREVIATION, abbreviations.values()));

    Queries.getRowSet(VIEW_FINANCIAL_INDICATORS, columns, filter, new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            Set<String> existingNames = new HashSet<>();
            Set<String> existingAbbreviations = new HashSet<>();

            if (!DataUtils.isEmpty(result)) {
              int nameIndex = result.getColumnIndex(COL_FIN_INDICATOR_NAME);
              int abbreviationIndex = result.getColumnIndex(COL_FIN_INDICATOR_ABBREVIATION);

              for (BeeRow row : result) {
                String name = BeeUtils.trimRight(row.getString(nameIndex));
                String abbreviation = BeeUtils.trimRight(row.getString(abbreviationIndex));

                if (!name.isEmpty()) {
                  existingNames.add(name);
                }
                if (!abbreviation.isEmpty()) {
                  existingAbbreviations.add(abbreviation);
                }
              }
            }

            final List<IndicatorType> types = new ArrayList<>();
            List<String> captions = new ArrayList<>();

            for (IndicatorType type : IndicatorType.values()) {
              if (!existingNames.contains(names.get(type))
                  && !existingAbbreviations.contains(abbreviations.get(type))) {

                types.add(type);
                captions.add(type.getCaption());
              }
            }

            if (types.isEmpty()) {
              Global.sayHuh("OMG", "ENOUGH", "ALREADY");

            } else {
              Global.choiceWithCancel(Localized.dictionary().finIndicatorCreate(),
                  BeeUtils.joinWords(accountCode, accountName), captions, index -> {
                    if (BeeUtils.isIndex(types, index)) {
                      IndicatorType type = types.get(index);

                      createIndicator(type, accountId, names.get(type), abbreviations.get(type),
                          normalBalanceIsCredit, background, foreground);
                    }
                  });
            }
          }
        }
    );
  }

  private static void createIndicator(final IndicatorType type, final long accountId,
      String name, String abbreviation, final boolean normalBalanceIsCredit,
      String background, String foreground) {

    final DataInfo dataInfo = Data.getDataInfo(VIEW_FINANCIAL_INDICATORS);
    BeeRow row = RowFactory.createEmptyRow(dataInfo);

    row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_KIND), IndicatorKind.PRIMARY);

    row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_NAME), name);
    row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_ABBREVIATION), abbreviation);

    row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_SOURCE), IndicatorSource.AMOUNT);

    row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_BALANCE), type.getIndicatorBalance());

    if (!BeeUtils.isEmpty(background)) {
      row.setValue(dataInfo.getColumnIndex(COL_BACKGROUND), background);
    }
    if (!BeeUtils.isEmpty(foreground)) {
      row.setValue(dataInfo.getColumnIndex(COL_FOREGROUND), foreground);
    }

    Queries.insert(dataInfo.getViewName(), dataInfo.getColumns(), row, new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        if (DataUtils.hasId(result)) {
          RowInsertEvent.fire(BeeKeeper.getBus(), dataInfo.getViewName(), result, null);
          addIndicatorAccounts(type, result.getId(), accountId, normalBalanceIsCredit);
        }
      }
    });
  }

  private static void addIndicatorAccounts(IndicatorType type, final long indicatorId,
      long accountId, boolean normalBalanceIsCredit) {

    List<String> colNames = Arrays.asList(COL_FIN_INDICATOR,
        COL_INDICATOR_ACCOUNT_DEBIT, COL_INDICATOR_ACCOUNT_CREDIT, COL_INDICATOR_ACCOUNT_PLUS);

    BeeRowSet rowSet = new BeeRowSet(VIEW_INDICATOR_ACCOUNTS,
        Data.getColumns(VIEW_INDICATOR_ACCOUNTS, colNames));

    int indicatorIndex = rowSet.getColumnIndex(COL_FIN_INDICATOR);
    int debitIndex = rowSet.getColumnIndex(COL_INDICATOR_ACCOUNT_DEBIT);
    int creditIndex = rowSet.getColumnIndex(COL_INDICATOR_ACCOUNT_CREDIT);
    int plusIndex = rowSet.getColumnIndex(COL_INDICATOR_ACCOUNT_PLUS);

    int rowCount = type.getRowCount();

    for (int i = 0; i < rowCount; i++) {
      BeeRow row = rowSet.addEmptyRow();
      row.setValue(indicatorIndex, indicatorId);

      row.setValue(debitIndex, type.getRowDebit(i, accountId, normalBalanceIsCredit));
      row.setValue(creditIndex, type.getRowCredit(i, accountId, normalBalanceIsCredit));

      if (i == 0) {
        row.setValue(plusIndex, true);
      }
    }

    Queries.insertRows(rowSet, new RpcCallback<RowInfoList>() {
      @Override
      public void onSuccess(RowInfoList result) {
        RowEditor.open(VIEW_FINANCIAL_INDICATORS, indicatorId, Opener.MODAL);
      }
    });
  }

  private static String sanitizeAccountCode(String input) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      if (NameUtils.isIdentifierPart(c)) {
        sb.append(c);
      } else {
        sb.append(BeeConst.CHAR_UNDER);
      }
    }

    return sb.toString();
  }

  private static String normalizeIndicatorName(String input) {
    return BeeUtils.left(input, NAME_PRECISION);
  }

  private static String normalizeIndicatorAbbreviation(String input) {
    return BeeUtils.left(input, ABBREVIATION_PRECISION);
  }
}
