package com.butent.bee.client.modules.finance;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.finance.NormalBalance;
import com.butent.bee.shared.modules.finance.analysis.IndicatorKind;
import com.butent.bee.shared.modules.finance.analysis.IndicatorSource;
import com.butent.bee.shared.modules.finance.analysis.TurnoverOrBalance;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ChartOfAccountsGrid extends AbstractGridInterceptor {

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

          if (BeeUtils.allNotEmpty(accountCode, accountName)) {
            onCreateIndicator(getActiveRowId(), sanitizeAccountCode(accountCode), accountName,
                getEnumValue(COL_ACCOUNT_NORMAL_BALANCE, NormalBalance.class),
                getStringValue(COL_BACKGROUND), getStringValue(COL_FOREGROUND));
          }
        }
      });
      presenter.getHeader().addCommandItem(create);
    }

    super.afterCreatePresenter(presenter);
  }

  private static void onCreateIndicator(final long accountId, final String accountCode,
      final String accountName, final NormalBalance normalBalance,
      final String background, final String foreground) {

    final EnumMap<TurnoverOrBalance, String> names = new EnumMap<>(TurnoverOrBalance.class);
    final EnumMap<TurnoverOrBalance, String> abbreviations = new EnumMap<>(TurnoverOrBalance.class);

    for (TurnoverOrBalance tob : TurnoverOrBalance.values()) {
      names.put(tob, normalizeIndicatorName(tob.getIndicatorName(Localized.dictionary(),
          accountName)));
      abbreviations.put(tob, normalizeIndicatorAbbreviation(tob.getIndicatorAbbreviation(
          Localized.dictionary(), accountCode)));
    }

    List<String> columns = Arrays.asList(COL_FIN_INDICATOR_NAME, COL_FIN_INDICATOR_ABBREVIATION);

    Filter filter = Filter.or(
        Filter.anyString(COL_FIN_INDICATOR_NAME, names.values()),
        Filter.anyString(COL_FIN_INDICATOR_ABBREVIATION, abbreviations.values()));

    Queries.getRowSet(VIEW_FINANCIAL_INDICATORS, columns, filter, result -> {
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

      final List<TurnoverOrBalance> types = new ArrayList<>();
      List<String> captions = new ArrayList<>();

      for (TurnoverOrBalance type : TurnoverOrBalance.values()) {
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
                TurnoverOrBalance type = types.get(index);

                createIndicator(type, accountId, names.get(type), abbreviations.get(type),
                    normalBalance, background, foreground);
              }
            });
      }
    }
    );
  }

  private static void createIndicator(TurnoverOrBalance turnoverOrBalance,
      final long accountId, String name, String abbreviation,
      NormalBalance normalBalance, String background, String foreground) {

    final DataInfo dataInfo = Data.getDataInfo(VIEW_FINANCIAL_INDICATORS);
    BeeRow row = RowFactory.createEmptyRow(dataInfo);

    row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_KIND), IndicatorKind.PRIMARY);

    row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_NAME), name);
    row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_ABBREVIATION), abbreviation);

    row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_SOURCE), IndicatorSource.DEFAULT);

    row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_TURNOVER_OR_BALANCE), turnoverOrBalance);
    if (normalBalance != null) {
      row.setValue(dataInfo.getColumnIndex(COL_FIN_INDICATOR_NORMAL_BALANCE), normalBalance);
    }

    if (!BeeUtils.isEmpty(background)) {
      row.setValue(dataInfo.getColumnIndex(COL_BACKGROUND), background);
    }
    if (!BeeUtils.isEmpty(foreground)) {
      row.setValue(dataInfo.getColumnIndex(COL_FOREGROUND), foreground);
    }

    Queries.insert(dataInfo.getViewName(), dataInfo.getColumns(), row, result -> {
      if (DataUtils.hasId(result)) {
        RowInsertEvent.fire(BeeKeeper.getBus(), dataInfo.getViewName(), result, null);
        addIndicatorAccount(result.getId(), accountId);
      }
    });
  }

  private static void addIndicatorAccount(final long indicatorId, long accountId) {
    Queries.insert(VIEW_INDICATOR_ACCOUNTS,
        Data.getColumns(VIEW_INDICATOR_ACCOUNTS, COL_FIN_INDICATOR, COL_INDICATOR_ACCOUNT),
        Queries.asList(indicatorId, accountId), null,
        result -> RowEditor.open(VIEW_FINANCIAL_INDICATORS, indicatorId));
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
