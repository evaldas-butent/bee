package com.butent.bee.client.modules.trade.acts;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.i18n.Money;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

  private static void applyActTemplate(final IsRow templRow, final FormView form) {
    IsRow targetRow = form.getActiveRow();
    List<BeeColumn> templColumns = Data.getColumns(VIEW_TRADE_ACT_TEMPLATES);

    if (targetRow != null && !BeeUtils.isEmpty(templColumns)) {
      Set<String> updatedColumns = new HashSet<>();

      BeeRow actRow = DataUtils.cloneRow(targetRow);
      TradeActKind actKind = TradeActKeeper.getKind(VIEW_TRADE_ACTS, actRow);

      for (int i = 0; i < templColumns.size(); i++) {
        String colName = templColumns.get(i).getId();
        String newValue = templRow.getString(i);

        int targetIndex = form.getDataIndex(colName);
        boolean upd;

        if (BeeConst.isUndef(targetIndex)) {
          upd = false;

        } else if (COL_TA_TEMPLATE_NAME.equals(colName)) {
          upd = false;

        } else if (COL_TA_UNTIL.equals(colName)) {
          upd = !templRow.isNull(i) && BeeUtils.isMeq(templRow.getDateTime(i),
              TimeUtils.startOfNextMonth(TimeUtils.today()).getDateTime());

        } else if (colName.contains(COL_TA_SERIES)) {
          upd = isTemplatable(actRow, templRow, COL_TA_SERIES)
              && TradeActKeeper.isUserSeries(getTemplateLong(templRow, COL_TA_SERIES));

        } else if (colName.contains(COL_TA_OPERATION)) {
          upd = isTemplatable(actRow, templRow, COL_TA_OPERATION);
          if (upd) {
            TradeActKind templKind =
                TradeActKeeper.getOperationKind(getTemplateLong(templRow, COL_TA_OPERATION));
            upd = actKind != null && actKind == templKind;
          }

        } else if (colName.contains(COL_TA_STATUS)) {
          upd = isTemplatable(actRow, templRow, COL_TA_STATUS);

        } else if (colName.contains(COL_TA_COMPANY)) {
          upd = isTemplatable(actRow, templRow, COL_TA_COMPANY);

        } else if (colName.contains(COL_TA_OBJECT)) {
          upd = isTemplatable(actRow, templRow, COL_TA_OBJECT);

        } else if (colName.contains(COL_TA_MANAGER)) {
          upd = isTemplatable(actRow, templRow, COL_TA_MANAGER);

        } else if (colName.contains(COL_TA_CURRENCY)) {
          upd = isTemplatable(actRow, templRow, COL_TA_CURRENCY);

        } else if (colName.contains(COL_TA_VEHICLE)) {
          upd = isTemplatable(actRow, templRow, COL_TA_VEHICLE);

        } else if (colName.contains(COL_TA_DRIVER)) {
          upd = isTemplatable(actRow, templRow, COL_TA_DRIVER);

        } else {
          upd = !BeeUtils.isEmpty(newValue) && targetRow.isNull(targetIndex);
        }

        if (upd) {
          targetRow.setValue(targetIndex, newValue);
          if (templColumns.get(i).isEditable()) {
            updatedColumns.add(colName);
          }
        }
      }

      for (String colName : updatedColumns) {
        form.refreshBySource(colName);
      }

      ParameterList params = TradeActKeeper.createArgs(SVC_GET_TEMPLATE_ITEMS_AND_SERVICES);

      params.addQueryItem(COL_TRADE_ACT_TEMPLATE, templRow.getId());
      if (DataUtils.hasId(targetRow)) {
        params.addQueryItem(COL_TRADE_ACT, actRow.getId());
      }
      if (actKind != null) {
        params.addQueryItem(COL_TA_KIND, actKind.ordinal());
      }

      BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          final List<BeeRowSet> data = new ArrayList<>();

          if (response.getSize() > 0) {
            String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());
            if (arr != null) {
              for (String s : arr) {
                BeeRowSet rowSet = BeeRowSet.restore(s);
                if (!DataUtils.isEmpty(rowSet)) {
                  data.add(rowSet);
                }
              }
            }
          }

          if (!data.isEmpty()) {
            GridView gridView = UiHelper.getChildGrid(form, GRID_TRADE_ACT_ITEMS);

            if (gridView != null) {
              gridView.ensureRelId(new IdCallback() {
                @Override
                public void onSuccess(Long result) {
                  if (DataUtils.isId(result) && DataUtils.idEquals(form.getActiveRow(), result)) {
                    BeeRowSet templateItems = null;
                    BeeRowSet templateServices = null;
                    BeeRowSet items = null;

                    for (BeeRowSet rowSet : data) {
                      switch (rowSet.getViewName()) {
                        case VIEW_TRADE_ACT_TMPL_ITEMS:
                          templateItems = rowSet;
                          break;
                        case VIEW_TRADE_ACT_TMPL_SERVICES:
                          templateServices = rowSet;
                          break;
                        case VIEW_ITEMS:
                          items = rowSet;
                          break;
                      }
                    }

                    if (!DataUtils.isEmpty(templateItems)) {
                      addTemplateItems(templRow, form.getActiveRow(), templateItems, items);
                    }
                    if (!DataUtils.isEmpty(templateServices)) {
                      addTemplateServices(templRow, form.getActiveRow(), templateServices, items);
                    }
                  }
                }
              });
            }
          }
        }
      });
    }
  }

  private static void addTemplateItems(IsRow templRow, IsRow actRow, BeeRowSet templItems,
      BeeRowSet items) {

    ItemPrice itemPrice = TradeActKeeper.getItemPrice(VIEW_TRADE_ACTS, actRow);

    DateTime date = Data.getDateTime(VIEW_TRADE_ACTS, actRow, COL_TA_DATE);

    Long templCurrency = Data.getLong(VIEW_TRADE_ACT_TEMPLATES, templRow, COL_TA_CURRENCY);
    Long actCurrency = Data.getLong(VIEW_TRADE_ACTS, actRow, COL_TA_CURRENCY);

    List<String> colNames = Lists.newArrayList(COL_TRADE_ACT, COL_TA_ITEM,
        COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
        COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC,
        COL_TRADE_DISCOUNT, COL_TRADE_ITEM_NOTE);

    BeeRowSet actItems = new BeeRowSet(VIEW_TRADE_ACT_ITEMS,
        Data.getColumns(VIEW_TRADE_ACT_ITEMS, colNames));

    Map<Integer, Integer> indexes = new HashMap<>();
    for (String colName : colNames) {
      if (!COL_TRADE_ACT.equals(colName)) {
        int sourceIndex = templItems.getColumnIndex(colName);
        int destinationIndex = actItems.getColumnIndex(colName);

        if (!BeeConst.isUndef(sourceIndex) && !BeeConst.isUndef(destinationIndex)) {
          indexes.put(sourceIndex, destinationIndex);
        }
      }
    }

    int itemIndex = actItems.getColumnIndex(COL_TA_ITEM);
    int actIndex = actItems.getColumnIndex(COL_TRADE_ACT);
    int qtyIndex = actItems.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    int priceIndex = actItems.getColumnIndex(COL_TRADE_ITEM_PRICE);

    for (BeeRow templItem : templItems) {
      BeeRow actItem = DataUtils.createEmptyRow(actItems.getNumberOfColumns());
      actItem.setValue(actIndex, templItem.getId());

      for (Map.Entry<Integer, Integer> indexEntry : indexes.entrySet()) {
        if (!templItem.isNull(indexEntry.getKey())) {
          actItem.setValue(indexEntry.getValue(), templItem.getString(indexEntry.getKey()));
        }
      }

      if (actItem.isNull(qtyIndex)) {
        actItem.setValue(qtyIndex, 0);
      }

      Double price = actItem.getDouble(priceIndex);

      if (BeeUtils.isPositive(price)) {
        if (DataUtils.isId(templCurrency) && DataUtils.isId(actCurrency)
            && !templCurrency.equals(actCurrency)) {
          actItem.setValue(priceIndex, Money.exchange(templCurrency, actCurrency, price, date));
        }

      } else if (itemPrice != null && items != null) {
        BeeRow item = items.getRowById(actItem.getLong(itemIndex));

        if (item != null) {
          price = item.getDouble(items.getColumnIndex(itemPrice.getPriceColumn()));

          if (BeeUtils.isDouble(price)) {
            if (DataUtils.isId(actCurrency)) {
              Long ic = item.getLong(items.getColumnIndex(itemPrice.getCurrencyColumn()));
              if (DataUtils.isId(ic) && !actCurrency.equals(ic)) {
                price = Money.exchange(ic, actCurrency, price, date);
              }
            }

            actItem.setValue(priceIndex,
                Data.round(actItems.getViewName(), COL_TRADE_ITEM_PRICE, price));
          }
        }
      }

      actItems.addRow(actItem);
    }

    if (!actItems.isEmpty()) {
      Queries.insertRows(actItems);
    }
  }

  @SuppressWarnings("unused")
  private static void addTemplateServices(IsRow templRow, IsRow actRow, BeeRowSet templServices,
      BeeRowSet items) {
  }

  private static boolean isTemplatable(IsRow actRow, IsRow templRow, String colName) {
    int actIndex = Data.getColumnIndex(VIEW_TRADE_ACTS, colName);
    int templIndex = Data.getColumnIndex(VIEW_TRADE_ACT_TEMPLATES, colName);

    return !BeeConst.isUndef(actIndex) && !BeeConst.isUndef(templIndex)
        && actRow.isNull(actIndex) && !templRow.isNull(templIndex);
  }

  private static Long getTemplateLong(IsRow row, String colName) {
    int index = Data.getColumnIndex(VIEW_TRADE_ACT_TEMPLATES, colName);
    return BeeConst.isUndef(index) ? null : row.getLong(index);
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
