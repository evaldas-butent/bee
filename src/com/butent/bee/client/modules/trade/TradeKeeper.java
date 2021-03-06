package com.butent.bee.client.modules.trade;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.modules.trade.acts.TradeActKeeper;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.style.ColorStyleProvider;
import com.butent.bee.client.style.ConditionalStyle;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.ViewFactory;
import com.butent.bee.client.view.grid.interceptor.FileGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.UniqueChildInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Triplet;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.menu.MenuItem;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.trade.DebtKind;
import com.butent.bee.shared.modules.trade.ItemQuantities;
import com.butent.bee.shared.modules.trade.TradeDocument;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.StringList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class TradeKeeper implements HandlesAllDataEvents {

  public static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "trade-";

  private static final TradeKeeper INSTANCE = new TradeKeeper();

  private static final BiMap<Long, String> warehouses = HashBiMap.create();

  public static ParameterList createArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.TRADE, method);
  }

  public static void createDocument(TradeDocument tradeDocument, IdCallback callback) {
    Assert.notNull(tradeDocument, SVC_CREATE_DOCUMENT + " document required");
    Assert.notNull(callback, SVC_CREATE_DOCUMENT + " callback required");

    ParameterList parameters = createArgs(SVC_CREATE_DOCUMENT);
    parameters.addDataItem(VAR_DOCUMENT, tradeDocument.serialize());

    BeeKeeper.getRpc().makeRequest(parameters, response -> {
      if (response.hasErrors()) {
        callback.onFailure(response.getErrors());
      } else {
        callback.onSuccess(response.getResponseAsLong());
      }
    });
  }

  public static void getItemStockByWarehouse(long item,
      final Consumer<List<Triplet<String, Double, Double>>> consumer) {

    Assert.isTrue(DataUtils.isId(item), SVC_GET_ITEM_STOCK_BY_WAREHOUSE + " item required");
    Assert.notNull(consumer, SVC_GET_ITEM_STOCK_BY_WAREHOUSE + " consumer required");

    ParameterList parameters = createArgs(SVC_GET_ITEM_STOCK_BY_WAREHOUSE);
    parameters.addQueryItem(COL_ITEM, item);

    BeeKeeper.getRpc().makeRequest(parameters, response -> {
      List<Triplet<String, Double, Double>> result = new ArrayList<>();

      if (response.hasResponse()) {
        String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());

        if (arr != null) {
          for (String s : arr) {
            Triplet<String, String, String> triplet = Triplet.restore(s);

            String warehouse = triplet.getA();
            Double stock = BeeUtils.toDoubleOrNull(triplet.getB());
            Double reserved = BeeUtils.toDoubleOrNull(triplet.getC());

            if (!BeeUtils.isEmpty(warehouse) && BeeUtils.isPositive(stock)) {
              result.add(Triplet.of(warehouse, stock, reserved));
            }
          }
        }
      }

      consumer.accept(result);
    });
  }

  public static void getReservationsInfo(Long warehouse, Long item, DateTime dateTo,
      Consumer<Map<ModuleAndSub, Map<String, Double>>> consumer) {

    Assert.notNull(consumer, SVC_GET_RESERVATIONS_INFO + " consumer required");
    ParameterList parameters = createArgs(SVC_GET_RESERVATIONS_INFO);

    if (DataUtils.isId(warehouse)) {
      parameters.addQueryItem(COL_STOCK_WAREHOUSE, warehouse);
    }
    parameters.addDataItem(COL_ITEM, item);

    if (Objects.nonNull(dateTo)) {
      parameters.addDataItem(COL_DATE_TO, dateTo.serialize());
    }
    BeeKeeper.getRpc().makeRequest(parameters, response -> {
      Map<ModuleAndSub, Map<String, Double>> reservationsInfo = new LinkedHashMap<>();

      Codec.deserializeLinkedHashMap(response.getResponseAsString())
          .forEach((k, v) -> {
            Map<String, Double> map = new LinkedHashMap<>();
            reservationsInfo.put(ModuleAndSub.parse(k), map);
            Codec.deserializeLinkedHashMap(v)
                .forEach((s, q) -> map.put(s, BeeUtils.toDouble(q)));
          });
      consumer.accept(reservationsInfo);
    });
  }

  public static void getStock(Long warehouse, Collection<Long> items, boolean includeReservations,
      Consumer<Multimap<Long, ItemQuantities>> consumer) {

    Assert.notNull(consumer, SVC_GET_STOCK + " consumer required");
    ParameterList parameters = createArgs(SVC_GET_STOCK);

    if (DataUtils.isId(warehouse)) {
      parameters.addQueryItem(COL_STOCK_WAREHOUSE, warehouse);
    }
    if (!BeeUtils.isEmpty(items)) {
      parameters.addDataItem(VAR_ITEMS, DataUtils.buildIdList(items));
    }
    parameters.addDataItem(VAR_RESERVATIONS, includeReservations);

    BeeKeeper.getRpc().makeRequest(parameters, response -> {
      Multimap<Long, ItemQuantities> result = ArrayListMultimap.create();

      if (response.hasResponse()) {
        Codec.deserializeMultiMap(response.getResponseAsString())
            .forEach((k, v) -> result.put(BeeUtils.toLong(k), ItemQuantities.restore(v)));
      }
      consumer.accept(result);
    });
  }

  public static void getWarehouseId(String code, Consumer<Long> callback) {
    getWarehouseIds(Collections.singleton(code), map -> callback.accept(map.get(code)));
  }

  public static void getWarehouseIds(Collection<String> codes,
      Consumer<Map<String, Long>> callback) {

    Assert.notNull(callback, "warehouse ids callback required");

    Map<String, Long> result = new HashMap<>();

    if (BeeUtils.isEmpty(codes)) {
      callback.accept(result);

    } else if (warehouses.values().containsAll(codes)) {
      codes.forEach(code -> result.put(code, warehouses.inverse().get(code)));
      callback.accept(result);

    } else {
      loadWarehouses(b -> {
        codes.forEach(code -> {
          Long id = warehouses.inverse().get(code);
          if (DataUtils.isId(id)) {
            result.put(code, id);
          }
        });
        callback.accept(result);
      });
    }
  }

  public static void register() {
    GridFactory.registerGridInterceptor(VIEW_PURCHASE_ITEMS, new TradeItemsGrid());
    GridFactory.registerGridInterceptor(VIEW_SALE_ITEMS, new TradeItemsGrid());

    GridFactory.registerGridInterceptor(GRID_SERIES_MANAGERS,
        UniqueChildInterceptor.forUsers(Localized.dictionary().managers(),
            COL_SERIES, COL_TRADE_MANAGER));
    GridFactory.registerGridInterceptor(GRID_TRADE_DOCUMENT_FILES,
        new FileGridInterceptor(COL_TRADE_DOCUMENT, COL_FILE, COL_FILE_CAPTION, ALS_FILE_NAME));

    GridFactory.registerGridInterceptor(VIEW_SALE_FILES,
        new FileGridInterceptor(COL_SALE, COL_FILE, COL_FILE_CAPTION, ALS_FILE_NAME));

    GridFactory.registerGridInterceptor(GRID_TRADE_STOCK, new TradeStockGrid());

    GridFactory.registerGridInterceptor(GRID_TRADE_EXPENDITURES, new TradeExpendituresGrid());
    GridFactory.registerGridInterceptor(GRID_TRADE_PAYMENT_TERMS, new TradePaymentTermsGrid());

    GridFactory.registerGridInterceptor(GRID_TRADE_PAYABLES,
        new TradeDebtsGrid(DebtKind.PAYABLE));
    GridFactory.registerGridInterceptor(GRID_TRADE_RECEIVABLES,
        new TradeDebtsGrid(DebtKind.RECEIVABLE));

    GridFactory.registerGridInterceptor(GRID_ANALOGS_OF_ITEM, new AnalogsOfItemGrid());

    FormFactory.registerFormInterceptor(FORM_SALES_INVOICE, new SalesInvoiceForm());
    FormFactory.registerFormInterceptor(FORM_TRADE_DOCUMENT, new TradeDocumentForm());

    FormFactory.registerFormInterceptor(FORM_PAYMENT_SUPPLIERS,
        new PaymentForm(DebtKind.PAYABLE));
    FormFactory.registerFormInterceptor(FORM_PAYMENT_CUSTOMERS,
        new PaymentForm(DebtKind.RECEIVABLE));

    ColorStyleProvider csp = ColorStyleProvider.createDefault(VIEW_TRADE_OPERATIONS);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_OPERATIONS, COL_BACKGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_OPERATIONS, COL_FOREGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_OPERATIONS, COL_OPERATION_NAME,
        csp);

    csp = ColorStyleProvider.createDefault(VIEW_TRADE_STATUSES);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_STATUSES, COL_BACKGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_STATUSES, COL_FOREGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_STATUSES, COL_STATUS_NAME, csp);

    csp = ColorStyleProvider.createDefault(VIEW_TRADE_TAGS);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_TAGS, COL_BACKGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_TAGS, COL_FOREGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_TAGS, COL_TAG_NAME, csp);

    csp = ColorStyleProvider.createDefault(VIEW_EXPENDITURE_TYPES);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_EXPENDITURE_TYPES, COL_BACKGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_EXPENDITURE_TYPES, COL_FOREGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_EXPENDITURE_TYPES,
        COL_EXPENDITURE_TYPE_NAME, csp);

    List<String> gridNames = StringList.of(GRID_TRADE_DOCUMENTS,
        GRID_TRADE_PAYABLES, GRID_TRADE_RECEIVABLES);

    ConditionalStyle.registerGridColumnColorProvider(gridNames,
        Collections.singleton(COL_TRADE_OPERATION),
        VIEW_TRADE_DOCUMENTS, ALS_OPERATION_BACKGROUND, ALS_OPERATION_FOREGROUND);
    ConditionalStyle.registerGridColumnColorProvider(gridNames,
        Collections.singleton(COL_TRADE_DOCUMENT_STATUS),
        VIEW_TRADE_DOCUMENTS, ALS_STATUS_BACKGROUND, ALS_STATUS_FOREGROUND);

    gridNames = StringList.of(GRID_ITEM_MOVEMENT, GRID_TRADE_RELATED_ITEMS);

    ConditionalStyle.registerGridColumnColorProvider(gridNames,
        Collections.singleton(COL_TRADE_OPERATION),
        VIEW_TRADE_MOVEMENT, ALS_OPERATION_BACKGROUND, ALS_OPERATION_FOREGROUND);
    ConditionalStyle.registerGridColumnColorProvider(gridNames,
        Collections.singleton(COL_TRADE_DOCUMENT_STATUS),
        VIEW_TRADE_MOVEMENT, ALS_STATUS_BACKGROUND, ALS_STATUS_FOREGROUND);

    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_EXPENDITURES,
        COL_EXPENDITURE_TYPE, ColorStyleProvider.createDefault(VIEW_TRADE_EXPENDITURES));

    registerDocumentViews();
    BeeKeeper.getBus().registerDataHandler(INSTANCE, false);

    MenuService.REBUILD_TRADE_STOCK.setHandler(p -> rebuildStock());

    BeeKeeper.getBus().registerRowTransformHandler(event -> {
      if (event.hasView(VIEW_TRADE_DOCUMENTS)) {
        DataInfo dataInfo = Data.getDataInfo(VIEW_TRADE_DOCUMENTS);

        event.setResult(DataUtils.join(dataInfo, event.getRow(),
            StringList.of(dataInfo.getIdColumn(), COL_TRADE_DATE,
                COL_TRADE_SERIES, COL_TRADE_NUMBER,
                COL_OPERATION_NAME, COL_TRADE_DOCUMENT_PHASE, COL_STATUS_NAME,
                ALS_SUPPLIER_NAME, ALS_CUSTOMER_NAME,
                ALS_WAREHOUSE_FROM_CODE, ALS_WAREHOUSE_TO_CODE),
            BeeConst.STRING_SPACE, Format.getDateRenderer(), Format.getDateTimeRenderer()));
      }
    });

    if (ModuleAndSub.of(Module.TRADE, SubModule.ACTS).isEnabled()) {
      TradeActKeeper.register();
    }
  }

  public static void registerCommons() {
    GridFactory.registerGridInterceptor(GRID_DEBTS, new DebtsGrid());
    GridFactory.registerGridInterceptor(GRID_DEBT_REPORTS, new DebtReportsGrid());
  }

  private static String getDocumentGridSupplierKey(long typeId) {
    return BeeUtils.join(BeeConst.STRING_UNDER, GRID_TRADE_DOCUMENTS, typeId);
  }

  private static void loadWarehouses(Consumer<Boolean> callback) {
    Queries.getRowSet(VIEW_WAREHOUSES, Collections.singletonList(COL_WAREHOUSE_CODE),
        new Queries.RowSetCallback() {
          @Override
          public void onFailure(String... reason) {
            callback.accept(false);
          }

          @Override
          public void onSuccess(BeeRowSet result) {
            warehouses.clear();

            if (!DataUtils.isEmpty(result)) {
              int index = result.getColumnIndex(COL_WAREHOUSE_CODE);
              result.forEach(row -> warehouses.put(row.getId(), row.getString(index)));
            }
            callback.accept(true);
          }
        });
  }

  private static void onDataEvent(DataEvent event) {
    if (event.hasView(VIEW_TRADE_DOCUMENT_TYPES)
        && BeeKeeper.getUser().isModuleVisible(ModuleAndSub.of(Module.TRADE))) {

      BeeKeeper.getMenu().loadMenu(TradeKeeper::registerDocumentViews);
    }
  }

  private static void openDocumentGrid(final long typeId, final PresenterCallback callback) {
    ParameterList params = createArgs(SVC_GET_DOCUMENT_TYPE_CAPTION_AND_FILTER);
    params.addDataItem(COL_DOCUMENT_TYPE, typeId);

    BeeKeeper.getRpc().makeRequest(params, response -> {
      if (response.hasResponse()) {
        Pair<String, String> pair = Pair.restore(response.getResponseAsString());

        String caption = pair.getA();
        Filter filter = BeeUtils.isEmpty(pair.getB()) ? null : Filter.restore(pair.getB());

        String supplierKey = getDocumentGridSupplierKey(typeId);

        GridFactory.createGrid(GRID_TRADE_DOCUMENTS, supplierKey, new TradeDocumentsGrid(),
            EnumSet.of(UiOption.GRID), GridOptions.forCaptionAndFilter(caption, filter),
            callback);
      }
    });
  }

  private static void rebuildStock() {
    Global.confirm(Localized.dictionary().rebuildTradeStockCaption(), Icon.WARNING,
        Collections.singletonList(Localized.dictionary().rebuildTradeStockQuestion()),
        Localized.dictionary().actionUpdate(), Localized.dictionary().cancel(),
        () -> {
          final long startTime = System.currentTimeMillis();

          BeeKeeper.getRpc().makeRequest(createArgs(SVC_REBUILD_STOCK), response -> {
            List<String> messages = new ArrayList<>();

            if (response.hasMessages()) {
              for (ResponseMessage responseMessage : response.getMessages()) {
                messages.add(responseMessage.getMessage());
              }
            }

            if (response.hasErrors()) {
              Global.showError(Localized.dictionary().rebuildTradeStockCaption(), messages);

            } else {
              if (!messages.isEmpty()) {
                messages.add(BeeConst.STRING_EMPTY);
              }
              messages.add(BeeUtils.joinWords(
                  Localized.dictionary().rebuildTradeStockNotification(),
                  TimeUtils.elapsedSeconds(startTime)));

              Global.showInfo(Localized.dictionary().rebuildTradeStockCaption(), messages);
            }
          });
        });
  }

  private static void registerDocumentViews() {
    Set<Long> typeIds = new HashSet<>();

    List<MenuItem> menuItems = BeeKeeper.getMenu().filter(MenuService.TRADE_DOCUMENTS);
    if (!BeeUtils.isEmpty(menuItems)) {
      for (MenuItem menuItem : menuItems) {
        String id = menuItem.getParameters();
        if (DataUtils.isId(id)) {
          typeIds.add(BeeUtils.toLong(id));
        }
      }
    }

    if (!typeIds.isEmpty()) {
      for (long typeId : typeIds) {
        String key = getDocumentGridSupplierKey(typeId);

        if (!ViewFactory.hasSupplier(key)) {
          ViewFactory.registerSupplier(key,
              callback -> openDocumentGrid(typeId, ViewFactory.getPresenterCallback(callback)));
        }
      }

      if (MenuService.TRADE_DOCUMENTS.getHandler() == null) {
        MenuService.TRADE_DOCUMENTS.setHandler(parameters -> {
          if (DataUtils.isId(parameters)) {
            long typeId = BeeUtils.toLong(parameters);
            ViewFactory.createAndShow(getDocumentGridSupplierKey(typeId));
          }
        });
      }
    }
  }

  private TradeKeeper() {
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    onDataEvent(event);
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
    onDataEvent(event);
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    onDataEvent(event);
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    onDataEvent(event);
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
    onDataEvent(event);
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    onDataEvent(event);
  }
}
