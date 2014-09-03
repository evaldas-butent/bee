package com.butent.bee.client.modules.trade.acts;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TradeActSelectorHandler implements SelectorEvent.Handler {

  private static void addNotes(String notes, FormView form) {
    IsRow row = form.getActiveRow();
    int index = form.getDataIndex(COL_TA_NOTES);

    if (row != null && !BeeConst.isUndef(index)) {
      String oldValue = row.getString(index);

      if (!BeeUtils.containsSame(oldValue, notes)) {
        row.setValue(index, BeeUtils.buildLines(oldValue, notes));
        form.refreshBySource(COL_TA_NOTES);
      }
    }
  }

  private static void applyActTemplate(IsRow sourceRow, FormView form) {
    IsRow targetRow = form.getActiveRow();
    List<BeeColumn> sourceColumns = Data.getColumns(VIEW_TRADE_ACT_TEMPLATES);

    if (targetRow != null && !BeeUtils.isEmpty(sourceColumns)) {
      Set<String> updatedColumns = new HashSet<>();

      for (int i = 0; i < sourceColumns.size(); i++) {
        String colName = sourceColumns.get(i).getId();
        String newValue = sourceRow.getString(i);

        boolean upd;

        switch (colName) {
          case COL_TA_TEMPLATE_NAME:
            upd = false;
            break;
          case COL_TA_UNTIL:
            upd = !sourceRow.isNull(i) && BeeUtils.isMeq(sourceRow.getDateTime(i),
                TimeUtils.startOfNextMonth(TimeUtils.today()).getDateTime());
            break;
          default:
            upd = !BeeUtils.isEmpty(newValue);
        }

        if (upd) {
          int index = form.getDataIndex(colName);

          if (index >= 0 && targetRow.isNull(index)) {
            targetRow.setValue(index, newValue);
            if (sourceColumns.get(i).isEditable()) {
              updatedColumns.add(colName);
            }
          }
        }
      }

      for (String colName : updatedColumns) {
        form.refreshBySource(colName);
      }
    }
  }

  private static TradeActKind getKind(SelectorEvent event) {
    DataView dataView = UiHelper.getDataView(event.getSelector());

    if (dataView != null && VIEW_TRADE_ACTS.equals(dataView.getViewName())
        && dataView.getActiveRow() != null) {

      return EnumUtils.getEnumByIndex(TradeActKind.class,
          dataView.getActiveRow().getInteger(dataView.getDataIndex(COL_TA_KIND)));
    } else {
      return null;
    }
  }

  private static Long getSeries(SelectorEvent event) {
    DataView dataView = UiHelper.getDataView(event.getSelector());

    if (dataView != null && VIEW_TRADE_ACTS.equals(dataView.getViewName())
        && dataView.getActiveRow() != null) {

      return dataView.getActiveRow().getLong(dataView.getDataIndex(COL_TA_SERIES));
    } else {
      return null;
    }
  }

  TradeActSelectorHandler() {
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    String relatedViewName = event.getRelatedViewName();
    if (BeeUtils.isEmpty(relatedViewName)) {
      return;
    }

    switch (relatedViewName) {
      case VIEW_TRADE_OPERATIONS:
        if (event.isOpened()) {
          TradeActKind kind = getKind(event);

          if (kind != null) {
            Filter filter;
            if (kind == TradeActKind.SUPPLEMENT) {
              filter = Filter.or(Filter.equals(COL_TA_KIND, TradeActKind.SALE),
                  Filter.equals(COL_TA_KIND, kind));
            } else {
              filter = Filter.equals(COL_TA_KIND, kind);
            }

            event.getSelector().setAdditionalFilter(filter);
          }
        }
        break;

      case VIEW_TRADE_SERIES:
        if (event.isOpened()) {
          DataView dataView = UiHelper.getDataView(event.getSelector());

          if (dataView != null && BeeUtils.inList(dataView.getViewName(),
              VIEW_TRADE_ACTS, VIEW_TRADE_ACT_TEMPLATES)) {

            BeeRowSet series = TradeActKeeper.getUserSeries();
            Filter filter;

            if (DataUtils.isEmpty(series)) {
              filter = null;
            } else {
              filter = Filter.idIn(series.getRowIds());
            }

            event.getSelector().setAdditionalFilter(filter);
          }
        }
        break;

      case VIEW_TRADE_NOTES:
        if (event.isOpened()) {
          Long series = getSeries(event);

          Filter filter;
          if (DataUtils.isId(series)) {
            filter = Filter.or(Filter.equals(COL_SERIES, series), Filter.isNull(COL_SERIES));
          } else {
            filter = null;
          }

          event.getSelector().setAdditionalFilter(filter);

        } else if (event.isChanged()) {
          IsRow relatedRow = event.getRelatedRow();
          String notes = (relatedRow == null)
              ? null : Data.getString(relatedViewName, relatedRow, COL_TRADE_NOTES);

          FormView form = UiHelper.getForm(event.getSelector());

          if (!BeeUtils.isEmpty(notes) && form != null) {
            addNotes(notes, form);
          }
        }
        break;

      case VIEW_TRADE_ACT_TEMPLATES:
        if (event.isOpened()) {
          CompoundFilter filter = Filter.and();

          TradeActKind kind = getKind(event);

          Collection<Long> operations = TradeActKeeper.filterOperations(kind);
          if (operations.isEmpty() && kind == TradeActKind.SUPPLEMENT) {
            operations.addAll(TradeActKeeper.filterOperations(TradeActKind.SALE));
          }

          if (operations.isEmpty()) {
            filter.add(Filter.isNull(COL_TA_OPERATION));
          } else {
            filter.add(Filter.or(Filter.any(COL_TA_OPERATION, operations),
                Filter.isNull(COL_TA_OPERATION)));
          }

          BeeRowSet series = TradeActKeeper.getUserSeries();
          if (!DataUtils.isEmpty(series)) {
            filter.add(Filter.or(Filter.any(COL_TA_SERIES, series.getRowIds()),
                Filter.isNull(COL_TA_SERIES)));
          }

          if (filter.isEmpty()) {
            event.getSelector().setAdditionalFilter(null);
          } else {
            event.getSelector().setAdditionalFilter(filter);
          }

        } else if (event.isChanged()) {
          IsRow relatedRow = event.getRelatedRow();
          FormView form = UiHelper.getForm(event.getSelector());

          if (relatedRow != null && form != null) {
            applyActTemplate(relatedRow, form);
          }
        }
        break;
    }
  }
}
