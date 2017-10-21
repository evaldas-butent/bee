package com.butent.bee.client.modules.cars;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.DataChangeCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.modules.trade.TradeItemPicker;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.view.grid.interceptor.ParentRowRefreshGrid;
import com.butent.bee.shared.Latch;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.data.view.RowInfoList;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.OperationType;
import com.butent.bee.shared.modules.trade.TradeDiscountMode;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.modules.trade.TradeVatMode;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class CarServiceItemsGrid extends ParentRowRefreshGrid {

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    HeaderView header = presenter.getHeader();

    if (Objects.nonNull(header) && BeeKeeper.getUser().canEditData(getViewName())) {
      header.addCommandItem(new PriceRecalculator(this));
    }
    super.afterCreatePresenter(presenter);
  }

  @Override
  public void afterDeleteRow(long rowId) {
    previewModify(null, true);
    super.afterDeleteRow(rowId);
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    addSomething();
    return false;
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    if (Objects.equals(action, Action.DELETE)) {
      Long bundle = getLongValue(COL_BUNDLE);

      if (DataUtils.isId(bundle) && presenter.getGridView().getRowData().stream().noneMatch(row ->
          Objects.equals(Data.getLong(presenter.getViewName(), row, COL_BUNDLE), bundle)
              && !presenter.getGridView().isRowEditable(row, null))) {

        Global.confirmDelete(Localized.dictionary().deleteQuestion(), Icon.WARNING,
            Arrays.asList(Localized.dictionary().bundle(),
                getStringValue(COL_BUNDLE_NAME)), () -> Queries.delete(presenter.getViewName(),
                Filter.and(Filter.equals(COL_SERVICE_ORDER, getLongValue(COL_SERVICE_ORDER)),
                    Filter.equals(COL_BUNDLE, bundle)),
                result -> previewModify(null, true)));
        return false;
      }
    }
    return super.beforeAction(action, presenter);
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {
    return DeleteMode.SINGLE;
  }

  @Override
  public GridInterceptor getInstance() {
    return new CarServiceItemsGrid();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (Objects.equals(event.getColumnId(), COL_RESERVE)) {
      event.consume();
      IsRow row = event.getRowValue();
      String value = Data.getString(getViewName(), row, COL_RESERVE);

      Queries.updateCellAndFire(getViewName(), row.getId(), row.getVersion(), COL_RESERVE,
          value, BeeUtils.toString(!BeeUtils.toBoolean(value)));
    } else {
      super.onEditStart(event);
    }
  }

  private void addBundle(Long orderId) {
    FormView parentForm = ViewHelper.getForm(getGridView());
    CompoundFilter filter = Filter.and();

    filter.add(Filter.or(Filter.isNull(COL_VALID_UNTIL),
        Filter.isMore(COL_VALID_UNTIL, Value.getValue(TimeUtils.today()))));

    Filter sub = Filter.isNull(COL_MODEL);

    if (DataUtils.isId(parentForm.getLongValue(COL_MODEL))) {
      sub = Filter.or(sub, Filter.equals(COL_MODEL, parentForm.getLongValue(COL_MODEL)));
    }
    filter.add(sub);

    Relation relation = Relation.create(TBL_CAR_BUNDLES, Arrays.asList(COL_CODE, COL_BUNDLE_NAME));
    relation.disableNewRow();
    relation.disableEdit();
    relation.setCaching(Relation.Caching.NONE);
    relation.setFilter(filter);
    UnboundSelector selector = UnboundSelector.create(relation);

    selector.addSelectorHandler(event -> {
      Long id = event.getValue();

      if (event.isChanged() && DataUtils.isId(id)) {
        UiHelper.getParentPopup(selector).close();
        DateTime validUntil = Data.getDateTime(event.getRelatedViewName(), event.getRelatedRow(),
            COL_VALID_UNTIL);

        Queries.getRowSet(TBL_CAR_BUNDLE_ITEMS, null, Filter.equals(COL_BUNDLE, id),
            rs -> {
              if (rs.isEmpty()) {
                getGridView().notifyWarning(Localized.dictionary().noData());
                return;
              }
              DataInfo info = Data.getDataInfo(getViewName());

              List<BeeColumn> cols = new ArrayList<>();
              cols.add(info.getColumn(COL_SERVICE_ORDER));
              cols.add(info.getColumn(COL_TRADE_DOCUMENT_ITEM_DISCOUNT));
              cols.add(info.getColumn(COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT));

              rs.getColumns().stream()
                  .filter(BeeColumn::isEditable)
                  .map(BeeColumn::getId)
                  .filter(info::containsColumn)
                  .forEach(col -> cols.add(info.getColumn(col)));

              BeeRowSet childRs = new BeeRowSet(info.getViewName(), cols);
              BeeRowSet parentRs = new BeeRowSet(info.getViewName(), cols);

              PriceRecalculator.getPriceAndDiscount(getGridView(), rs.getNumberOfRows(),
                  (idx, data) -> {
                    IsRow row = rs.getRow(idx);

                    switch (data) {
                      case ITEM:
                        return Objects.nonNull(validUntil) ? null
                            : row.getString(rs.getColumnIndex(COL_ITEM));
                      case QUANTITY:
                        return row.getString(rs.getColumnIndex(COL_TRADE_ITEM_QUANTITY));
                      case PRICE:
                        return row.getString(rs.getColumnIndex(COL_PRICE));
                      case CURRENCY:
                        return row.getString(rs.getColumnIndex(COL_CURRENCY));
                      case DISCOUNT:
                        return row.getString(rs.getColumnIndex(COL_DISCOUNT_PERCENT));
                    }
                    return null;
                  },
                  (idx, price, discount) -> {
                    BeeRow beeRow = rs.getRow(idx);

                    BeeRow newRow = beeRow.isNull(rs.getColumnIndex(COL_PARENT))
                        ? parentRs.addEmptyRow() : childRs.addEmptyRow();

                    newRow.setProperty("oldID", beeRow.getId());

                    for (int i = 0; i < cols.size(); i++) {
                      String col = cols.get(i).getId();

                      switch (col) {
                        case COL_SERVICE_ORDER:
                          newRow.setValue(i, orderId);
                          break;

                        case COL_PRICE:
                          newRow.setValue(i, price);
                          break;

                        case COL_TRADE_DOCUMENT_ITEM_DISCOUNT:
                          newRow.setValue(i, discount);
                          break;

                        case COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT:
                          newRow.setValue(i, BeeUtils.isPositive(discount));
                          break;

                        default:
                          newRow.setValue(i, beeRow.getString(rs.getColumnIndex(col)));
                          break;
                      }
                    }
                  },
                  () -> {
                    Latch parents = new Latch(parentRs.getNumberOfRows());

                    Runnable requery = () -> previewModify(null, true);

                    parentRs.forEach(parentRow -> Queries.insert(parentRs.getViewName(),
                        parentRs.getColumns(), parentRow, result -> {
                          parentRow.setId(result.getId());
                          parents.decrement();

                          if (parents.isOpen()) {
                            if (childRs.isEmpty()) {
                              requery.run();
                            } else {
                              int parentIdx = childRs.getColumnIndex(COL_PARENT);
                              childRs.forEach(childRow -> childRow.setValue(parentIdx,
                                  parentRs.findRow((columns, row) ->
                                      Objects.equals(row.getPropertyLong("oldID"),
                                          childRow.getLong(parentIdx))).getId()));

                              Queries.insertRows(childRs, res -> requery.run());
                            }
                          }
                        }));
                  });
            });
      }
    });
    Global.showModalWidget(Localized.dictionary().bundle(), selector);
  }

  private void addItems(Long orderId, Long parent) {
    FormView parentForm = ViewHelper.getForm(getGridView());

    OperationType operationType = OperationType.SALE;
    TradeVatMode vatMode = EnumUtils.getEnumByIndex(TradeVatMode.class,
        parentForm.getIntegerValue(COL_TRADE_DOCUMENT_VAT_MODE));

    Map<String, String> options = new HashMap<>();
    options.put(COL_DISCOUNT_COMPANY, parentForm.getStringValue(COL_CUSTOMER));
    options.put(Service.VAR_TIME, parentForm.getStringValue(COL_DATE));
    options.put(COL_DISCOUNT_CURRENCY, parentForm.getStringValue(COL_CURRENCY));
    options.put(COL_MODEL, parentForm.getStringValue(COL_MODEL));
    options.put(COL_PRODUCTION_DATE, parentForm.getStringValue(COL_PRODUCTION_DATE));

    getVatPercent(vatMode, vatPercent -> {
      TradeItemPicker picker = new TradeItemPicker(TradeDocumentPhase.PENDING, operationType,
          parentForm.getLongValue(COL_WAREHOUSE), operationType.getDefaultPrice(),
          parentForm.getDateTimeValue(COL_DATE), parentForm.getLongValue(COL_CURRENCY),
          parentForm.getStringValue(ALS_CURRENCY_NAME), TradeDiscountMode.FROM_AMOUNT, null,
          vatMode, options, vatPercent);

      picker.open((selectedItems, tds) -> {
        List<BeeColumn> columns = DataUtils.getColumns(getDataColumns(),
            Arrays.asList(COL_SERVICE_ORDER, COL_PARENT,
                COL_ITEM, COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE,
                COL_TRADE_DOCUMENT_ITEM_DISCOUNT, COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT,
                COL_TRADE_DOCUMENT_ITEM_VAT, COL_TRADE_DOCUMENT_ITEM_VAT_IS_PERCENT));

        BeeRowSet rowSet = new BeeRowSet(getViewName(), columns);

        for (BeeRow row : selectedItems) {
          long id = row.getId();

          double quantity = tds.getQuantity(id);

          Double price = tds.getPrice(id);
          if (BeeUtils.isZero(price)) {
            price = null;
          }
          Pair<Double, Boolean> discountInfo =
              TradeUtils.normalizeDiscountOrVatInfo(tds.getDiscountInfo(id));
          Pair<Double, Boolean> vatInfo =
              TradeUtils.normalizeDiscountOrVatInfo(tds.getVatInfo(id));

          rowSet.addRow(DataUtils.NEW_ROW_ID, DataUtils.NEW_ROW_VERSION,
              Queries.asList(orderId, parent, id, quantity, price, discountInfo.getA(),
                  discountInfo.getB(), vatInfo.getA(), vatInfo.getB()));
        }
        Queries.insertRows(rowSet, new DataChangeCallback(rowSet.getViewName()) {
          @Override
          public void onSuccess(RowInfoList result) {
            previewModify(null);
            super.onSuccess(result);
          }
        });
      });
    });
  }

  private void addJob(Long orderId) {
    FormView parentForm = ViewHelper.getForm(getGridView());
    Filter filter = Filter.isNull(COL_MODEL);

    if (DataUtils.isId(parentForm.getLongValue(COL_MODEL))) {
      filter = Filter.or(filter, Filter.equals(COL_MODEL, parentForm.getLongValue(COL_MODEL)));
    }
    Relation relation = Relation.create(TBL_CAR_JOBS,
        Arrays.asList(COL_ITEM_ARTICLE, ALS_ITEM_NAME));
    relation.disableNewRow();
    relation.disableEdit();
    relation.setCaching(Relation.Caching.NONE);
    relation.setFilter(filter);
    UnboundSelector selector = UnboundSelector.create(relation);

    selector.addSelectorHandler(event -> {
      if (event.isChanged()) {
        UiHelper.getParentPopup(selector).close();

        String srcView = event.getRelatedViewName();
        IsRow srcRow = event.getRelatedRow();
        if (srcRow == null) {
          return;
        }
        DataInfo info = Data.getDataInfo(getViewName());

        List<BeeColumn> cols = new ArrayList<>();
        cols.add(info.getColumn(COL_SERVICE_ORDER));
        cols.add(info.getColumn(COL_ITEM));
        cols.add(info.getColumn(COL_DURATION));
        cols.add(info.getColumn(COL_TRADE_ITEM_QUANTITY));
        cols.add(info.getColumn(COL_PRICE));
        cols.add(info.getColumn(COL_TRADE_DOCUMENT_ITEM_DISCOUNT));
        cols.add(info.getColumn(COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT));

        BeeRowSet rs = new BeeRowSet(info.getViewName(), cols);

        PriceRecalculator.getPriceAndDiscount(getGridView(), 1, (idx, data) -> {
              switch (data) {
                case ITEM:
                  return Data.getString(srcView, srcRow, COL_ITEM);
                case PRICE:
                  return BeeUtils.nvl(Data.getString(srcView, srcRow, COL_MODEL + COL_PRICE),
                      Data.getString(srcView, srcRow, COL_PRICE));
                case CURRENCY:
                  return BeeUtils.nvl(Data.getString(srcView, srcRow, COL_MODEL + COL_CURRENCY),
                      Data.getString(srcView, srcRow, COL_CURRENCY));
                default:
                  return null;
              }
            },
            (idx, price, discount) -> {
              BeeRow newRow = rs.addEmptyRow();

              for (int i = 0; i < cols.size(); i++) {
                String col = cols.get(i).getId();

                switch (col) {
                  case COL_SERVICE_ORDER:
                    newRow.setValue(i, orderId);
                    break;

                  case COL_ITEM:
                    newRow.setValue(i, Data.getString(srcView, srcRow, COL_ITEM));
                    break;

                  case COL_DURATION:
                    newRow.setValue(i, BeeUtils.nvl(Data.getString(srcView, srcRow,
                        COL_MODEL + COL_DURATION), Data.getString(srcView, srcRow, COL_DURATION)));
                    break;

                  case COL_TRADE_ITEM_QUANTITY:
                    newRow.setValue(i, 1);
                    break;

                  case COL_PRICE:
                    newRow.setValue(i, price);
                    break;

                  case COL_TRADE_DOCUMENT_ITEM_DISCOUNT:
                    newRow.setValue(i, discount);
                    break;

                  case COL_TRADE_DOCUMENT_ITEM_DISCOUNT_IS_PERCENT:
                    newRow.setValue(i, BeeUtils.isPositive(discount));
                    break;
                }
              }
            },
            () -> Queries.insertRows(rs, new DataChangeCallback(rs.getViewName()) {
              @Override
              public void onSuccess(RowInfoList result) {
                previewModify(null);
                super.onSuccess(result);
              }
            }));
      }
    });
    Global.showModalWidget(Localized.dictionary().serviceJob(), selector);
  }

  private void addSomething() {
    List<String> choices = new ArrayList<>(Arrays.asList(Localized.dictionary().bundle(),
        Localized.dictionary().serviceJob(), Localized.dictionary().itemOrService()));

    if (!DataUtils.isId(getLongValue(COL_BUNDLE)) && !DataUtils.isId(getLongValue(COL_PARENT))
        && DataUtils.isId(getLongValue(COL_JOB))) {

      choices.add(BeeUtils.joinWords(Localized.dictionary().itemOrService(),
          BeeUtils.parenthesize(BeeUtils.join(": ", Localized.dictionary().parent(),
              getStringValue(ClassifierConstants.ALS_ITEM_NAME)))));
    }

    Global.choice(Localized.dictionary().newRow(), null, choices,
        choice -> getGridView().ensureRelId(orderId -> {
          switch (choice) {
            case 0:
              addBundle(orderId);
              break;

            case 1:
              addJob(orderId);
              break;

            case 2:
            case 3:
              addItems(orderId, choice == 3 ? getActiveRowId() : null);
              break;
          }
        }));
  }

  private static void getVatPercent(TradeVatMode vatMode, Consumer<Double> vatConsumer) {
    Long operation = Global.getParameterRelation(PRM_SERVICE_TRADE_OPERATION);

    if (Objects.nonNull(vatMode) && DataUtils.isId(operation)) {
      Queries.getValue(VIEW_TRADE_OPERATIONS, operation, COL_OPERATION_VAT_PERCENT,
          result -> {
            Double vatPercent = BeeUtils.toDoubleOrNull(result);

            if (vatPercent == null) {
              Number p = Global.getParameterNumber(PRM_VAT_PERCENT);

              if (p != null) {
                vatPercent = p.doubleValue();
              }
            }
            vatConsumer.accept(vatPercent);
          });
    } else {
      vatConsumer.accept(null);
    }
  }
}
