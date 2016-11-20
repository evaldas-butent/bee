package com.butent.bee.client.modules.finance;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
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
    };

    abstract String getName(String accountName);

    abstract String getAbbreviation(String accountCode);
  }

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
                normalBalanceIsCredit(accountNormalBalance));
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

  private void onCreateIndicator(final long accountId, final String accountCode,
      final String accountName, final boolean normalBalanceIsCredit) {

    final EnumMap<IndicatorType, String> names = new EnumMap<>(IndicatorType.class);
    final EnumMap<IndicatorType, String> abbreviations = new EnumMap<>(IndicatorType.class);

    for (IndicatorType type : IndicatorType.values()) {
      names.put(type, type.getName(accountName));
      abbreviations.put(type, type.getAbbreviation(accountCode));
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

            final List<IndicatorType> keys = new ArrayList<>();
            final List<String> values = new ArrayList<>();

            abbreviations.forEach((type, abbreviation) -> {
              if (!existingAbbreviations.contains(abbreviation)) {
                keys.add(type);
                values.add(abbreviation);
              }
            });

            if (keys.isEmpty()) {
              Global.sayHuh("OMG", "ENOUGH", "ALREADY");

            } else {
              Global.choiceWithCancel(Localized.dictionary().finIndicatorCreate(),
                  BeeUtils.joinWords(accountCode, accountName), values, index -> {

                  });
            }
          }
        }
    );
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
}
