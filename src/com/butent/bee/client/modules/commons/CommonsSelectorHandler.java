package com.butent.bee.client.modules.commons;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.HasDataProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommonsSelectorHandler implements SelectorEvent.Handler {

  private final Map<String, Long> companyPersonSelectors = Maps.newHashMap();

  CommonsSelectorHandler() {
    super();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (BeeUtils.same(event.getRelatedViewName(), TBL_ROLES)) {
      handleRoles(event);

    } else if (BeeUtils.same(event.getRelatedViewName(), TBL_EMAILS)) {
      handleEmails(event);

    } else if (BeeUtils.same(event.getRelatedViewName(), VIEW_PERSONS)) {
      handlePersons(event);

    } else if (BeeUtils.same(event.getRelatedViewName(), VIEW_COMPANY_PERSONS)) {
      if (event.isOpened() || event.isDataLoaded() || event.isUnloading()) {
        handleCompanyPersons(event);
      }
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
          companyRows.addAll(rowSet.getRows().getList());
          rowSet.setRows(companyRows);
        }
      }
    }
  }

  private void handleEmails(SelectorEvent event) {
    if (!event.isNewRow()) {
      return;
    }
    Data.setValue(TBL_EMAILS, event.getNewRow(), COL_EMAIL_ADDRESS,
        event.getSelector().getDisplayValue());
    event.consume();
  }

  private void handlePersons(SelectorEvent event) {
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
      Data.setValue(TBL_PERSONS, event.getNewRow(), COL_FIRST_NAME, firstName);
      Data.setValue(TBL_PERSONS, event.getNewRow(), COL_LAST_NAME, lastName);
    }
    event.consume();
  }

  private void handleRoles(SelectorEvent event) {
    if (!event.isOpened()) {
      return;
    }
    GridView gridView = UiHelper.getGrid(event.getSelector());
    if (gridView == null) {
      return;
    }
    IsRow row = gridView.getActiveRow();
    if (row == null) {
      return;
    }
    long id = row.getId();

    if (BeeUtils.same(gridView.getViewName(), TBL_USER_ROLES)) {
      Provider provider = ((HasDataProvider) gridView.getViewPresenter()).getDataProvider();

      if (provider != null) {
        int index = provider.getColumnIndex(COL_ROLE);
        Long exclude = DataUtils.isId(id) ? row.getLong(index) : null;
        List<Long> used = DataUtils.getDistinct(gridView.getRowData(), index, exclude);

        if (!BeeUtils.isEmpty(used)) {
          CompoundFilter filter = Filter.and();

          for (Long value : used) {
            filter.add(ComparisonFilter.compareId(Operator.NE, value));
          }
          event.getSelector().setAdditionalFilter(filter);
        }
      }
    }
  }

  private void removeCompanyPersonSelector(String id) {
    if (companyPersonSelectors.containsKey(id)) {
      companyPersonSelectors.remove(id);
    }
  }
}
