package com.butent.bee.client.modules.classifiers;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.classifiers.ClassifiersConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClassifierSelector implements SelectorEvent.Handler {

  private final Map<String, Long> companyPersonSelectors = Maps.newHashMap();

  ClassifierSelector() {
    super();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (BeeUtils.same(event.getRelatedViewName(), TBL_EMAILS)) {
      handleEmails(event);

    } else if (BeeUtils.same(event.getRelatedViewName(), TBL_CITIES)) {
      handleCities(event);

    } else if (event.isNewRow()
        && BeeUtils.inListSame(event.getRelatedViewName(), VIEW_PERSONS, VIEW_COMPANY_PERSONS)) {
      handleNewPersons(event);

    } else if (BeeUtils.same(event.getRelatedViewName(), VIEW_COMPANY_PERSONS)) {
      if (event.isOpened() || event.isDataLoaded() || event.isUnloading()) {
        handleCompanyPersons(event);
      }
    }
  }

  private static void handleCities(SelectorEvent event) {
    if (!event.isChanged()) {
      return;
    }

    DataInfo sourceInfo = Data.getDataInfo(event.getRelatedViewName());
    IsRow source = event.getRelatedRow();
    if (source == null) {
      return;
    }

    DataView dataView = UiHelper.getDataView(event.getSelector());
    if (dataView == null || BeeUtils.isEmpty(dataView.getViewName())) {
      return;
    }

    DataInfo targetInfo = Data.getDataInfo(dataView.getViewName());
    IsRow target = dataView.getActiveRow();
    if (target == null) {
      return;
    }

    String targetColumn = BeeUtils.notEmpty(event.getSelector().getOptions(), COL_COUNTRY);
    int targetIndex = targetInfo.getColumnIndex(targetColumn);
    if (BeeConst.isUndef(targetIndex)) {
      return;
    }

    Long country = source.getLong(sourceInfo.getColumnIndex(COL_COUNTRY));
    if (country == null || country.equals(target.getLong(targetIndex))) {
      return;
    }

    if (dataView.isFlushable()) {
      target.setValue(targetIndex, country);
    } else {
      target.preliminaryUpdate(targetIndex, country.toString());
    }

    Collection<String> updatedColumns =
        RelationUtils.updateRow(targetInfo, targetColumn, target, sourceInfo, source, false);
    updatedColumns.add(targetColumn);

    for (String colName : updatedColumns) {
      dataView.refreshBySource(colName);
    }
  }

  private void handleCompanyPersons(SelectorEvent event) {
    String targetCompanyColumnName = event.getSelector().getOptions();
    if (BeeUtils.isEmpty(targetCompanyColumnName)) {
      return;
    }

    String selectorId = event.getSelector().getId();
    if (BeeUtils.isEmpty(selectorId)) {
      return;
    }

    if (event.isUnloading()) {
      removeCompanyPersonSelector(selectorId);
      return;
    }

    DataView dataView = UiHelper.getDataView(event.getSelector());
    if (dataView == null) {
      removeCompanyPersonSelector(selectorId);
      return;
    }

    IsRow targetRow = dataView.getActiveRow();
    if (targetRow == null) {
      removeCompanyPersonSelector(selectorId);
      return;
    }

    int targetCompanyIndex = Data.getColumnIndex(dataView.getViewName(), targetCompanyColumnName);
    if (targetCompanyIndex < 0) {
      removeCompanyPersonSelector(selectorId);
      return;
    }

    Long company = targetRow.getLong(targetCompanyIndex);
    if (Objects.equal(company, companyPersonSelectors.get(selectorId))) {
      return;
    }

    if (event.isOpened()) {
      event.getSelector().getOracle().clearData();
      return;
    }

    if (event.isDataLoaded()) {
      if (company == null) {
        removeCompanyPersonSelector(selectorId);
        return;
      }

      companyPersonSelectors.put(selectorId, company);

      BeeRowSet rowSet = event.getSelector().getOracle().getViewData();
      if (rowSet == null || rowSet.getNumberOfRows() <= 1) {
        return;
      }

      int sourceCompanyIndex = Data.getColumnIndex(rowSet.getViewName(), COL_COMPANY);
      if (targetCompanyIndex < 0) {
        return;
      }

      List<BeeRow> companyRows = Lists.newArrayList();

      for (Iterator<BeeRow> it = rowSet.getRows().iterator(); it.hasNext();) {
        BeeRow row = it.next();

        if (company.equals(row.getLong(sourceCompanyIndex))) {
          companyRows.add(row);
          it.remove();
        }
      }

      if (!companyRows.isEmpty()) {
        if (rowSet.isEmpty()) {
          rowSet.addRows(companyRows);
        } else {
          companyRows.addAll(rowSet.getRows());
          rowSet.setRows(companyRows);
        }
      }
    }
  }

  private static void handleEmails(SelectorEvent event) {
    if (!event.isNewRow()) {
      return;
    }
    Data.setValue(TBL_EMAILS, event.getNewRow(), COL_EMAIL_ADDRESS,
        event.getSelector().getDisplayValue());
    event.consume();
  }

  private static void handleNewPersons(SelectorEvent event) {
    if (!event.isNewRow()) {
      return;
    }
    String value = event.getSelector().getDisplayValue();

    if (!BeeUtils.isEmpty(value)) {
      String firstName = null;
      String lastName = null;

      for (String val : Splitter.on(BeeConst.CHAR_SPACE).trimResults().omitEmptyStrings().limit(2)
          .split(value)) {
        if (BeeUtils.isEmpty(firstName)) {
          firstName = val;
        } else {
          lastName = val;
        }
      }
      Data.setValue(event.getRelatedViewName(), event.getNewRow(), COL_FIRST_NAME, firstName);
      Data.setValue(event.getRelatedViewName(), event.getNewRow(), COL_LAST_NAME, lastName);
    }
    event.consume();
  }

  private void removeCompanyPersonSelector(String id) {
    if (companyPersonSelectors.containsKey(id)) {
      companyPersonSelectors.remove(id);
    }
  }
}
