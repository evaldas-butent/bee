package com.butent.bee.server.modules.ec;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.data.ViewEvent.ViewQueryEvent;
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.ec.Cart;
import com.butent.bee.shared.modules.ec.CartItem;
import com.butent.bee.shared.modules.ec.DeliveryMethod;
import com.butent.bee.shared.modules.ec.EcCarModel;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.modules.ec.EcConstants.EcOrderStatus;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class EcModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(EcModuleBean.class);

  public static String normalizeCode(String code) {
    return code.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
  }

  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;

  @Override
  public Collection<String> dependsOn() {
    return Lists.newArrayList(CommonsConstants.COMMONS_MODULE);
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    long startMillis = System.currentTimeMillis();

    ResponseObject response = null;
    String svc = reqInfo.getParameter(EC_METHOD);

    String query = null;
    boolean log = false;

    if (BeeUtils.same(svc, SVC_FEATURED_AND_NOVELTY)) {
      response = getFeaturedAndNoveltyItems();

    } else if (BeeUtils.same(svc, SVC_GET_CATEGORIES)) {
      response = getCategories();
      log = true;

    } else if (BeeUtils.same(svc, SVC_GLOBAL_SEARCH)) {
      query = reqInfo.getParameter(VAR_QUERY);
      response = doGlobalSearch(query);
      log = true;

    } else if (BeeUtils.same(svc, SVC_SEARCH_BY_ITEM_CODE)) {
      query = reqInfo.getParameter(VAR_QUERY);
      response = searchByItemCode(query, null);
      log = true;

    } else if (BeeUtils.same(svc, SVC_SEARCH_BY_OE_NUMBER)) {
      query = reqInfo.getParameter(VAR_QUERY);
      response = searchByOeNumber(query);
      log = true;

    } else if (BeeUtils.same(svc, SVC_GET_CAR_MANUFACTURERS)) {
      response = getCarManufacturers();
      log = true;

    } else if (BeeUtils.same(svc, SVC_GET_CAR_MODELS)) {
      query = reqInfo.getParameter(VAR_MANUFACTURER);
      response = getCarModels(query);
      log = true;

    } else if (BeeUtils.same(svc, SVC_GET_CAR_TYPES)) {
      query = reqInfo.getParameter(VAR_MODEL);
      response = getCarTypes(query);
      log = true;

    } else if (BeeUtils.same(svc, SVC_GET_ITEMS_BY_CAR_TYPE)) {
      response = getItemsByCarType(reqInfo);
      log = true;

    } else if (BeeUtils.same(svc, SVC_GET_ITEM_MANUFACTURERS)) {
      response = getItemManufacturers();
      log = true;

    } else if (BeeUtils.same(svc, SVC_GET_ITEMS_BY_MANUFACTURER)) {
      response = getItemsByManufacturer(reqInfo);
      log = true;

    } else if (BeeUtils.same(svc, SVC_GET_DELIVERY_METHODS)) {
      response = getDeliveryMethods();

    } else if (BeeUtils.same(svc, SVC_SUBMIT_ORDER)) {
      response = submitOrder(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_CONFIGURATION)) {
      response = getConfiguration();
    } else if (BeeUtils.same(svc, SVC_SAVE_CONFIGURATION)) {
      response = saveConfiguration(reqInfo);
    } else if (BeeUtils.same(svc, SVC_CLEAR_CONFIGURATION)) {
      response = clearConfiguration(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_ITEM_ANALOGS)) {
      response = getItemAnalogs(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("e-commerce service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }

    if (log && response != null && !response.hasErrors()) {
      logHistory(svc, query, response.getSize(), System.currentTimeMillis() - startMillis);
    }

    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  @Override
  public String getName() {
    return EC_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
    sys.registerViewEventHandler(new ViewEventHandler() {
      @Subscribe
      public void setItemProperties(ViewQueryEvent event) {
        if (event.isAfter() && BeeUtils.same(event.getViewName(), VIEW_ORDER_ITEMS)) {
          BeeRowSet orderItems = event.getRowset();
          if (DataUtils.isEmpty(orderItems)) {
            return;
          }

          int itemIndex = orderItems.getColumnIndex(COL_ORDER_ITEM_ID);
          if (itemIndex <= 0) {
            return;
          }

          Multimap<Integer, Integer> itemIdToRowIndex = HashMultimap.create();

          for (int i = 0; i < orderItems.getNumberOfRows(); i++) {
            Integer itemId = orderItems.getInteger(i, itemIndex);
            if (BeeUtils.isPositive(itemId)) {
              itemIdToRowIndex.put(itemId, i);
            }
          }
          if (itemIdToRowIndex.isEmpty()) {
            return;
          }

          SqlSelect ss = new SqlSelect();
          ss.addFrom(TBL_TCD_ARTICLES);
          ss.addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID, COL_TCD_ARTICLE_NAME,
              COL_TCD_ARTICLE_NR);

          ss.setWhere(SqlUtils.inList(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID,
              itemIdToRowIndex.keySet()));

          SimpleRowSet articleData = qs.getData(ss);
          if (DataUtils.isEmpty(articleData)) {
            return;
          }

          int idIndex = articleData.getColumnIndex(COL_TCD_ARTICLE_ID);
          int nameIndex = articleData.getColumnIndex(COL_TCD_ARTICLE_NAME);
          int nrIndex = articleData.getColumnIndex(COL_TCD_ARTICLE_NR);

          for (SimpleRow articleRow : articleData) {
            Integer itemId = articleRow.getInt(idIndex);
            String name = articleRow.getValue(nameIndex);
            String nr = articleRow.getValue(nrIndex);

            for (Integer rowIndex : itemIdToRowIndex.get(itemId)) {
              BeeRow row = orderItems.getRow(rowIndex);

              row.setProperty(COL_TCD_ARTICLE_NAME, name);
              row.setProperty(COL_TCD_ARTICLE_NR, nr);
            }
          }
        }
      }
    });
  }

  private ResponseObject clearConfiguration(RequestInfo reqInfo) {
    String column = reqInfo.getParameter(Service.VAR_COLUMN);
    if (BeeUtils.isEmpty(column)) {
      return ResponseObject.parameterNotFound(SVC_CLEAR_CONFIGURATION, Service.VAR_COLUMN);
    }

    if (updateConfiguration(column, null)) {
      return ResponseObject.response(column);
    } else {
      String message = BeeUtils.joinWords(SVC_CLEAR_CONFIGURATION, column,
          "cannot clear configuration");
      logger.severe(message);
      return ResponseObject.error(message);
    }
  }

  private String createTempArticleIds(SqlSelect query) {
    String tmp = qs.sqlCreateTemp(query);
    qs.sqlIndex(tmp, COL_TCD_ARTICLE_ID);
    return tmp;
  }

  private ResponseObject doGlobalSearch(String query) {
    if (BeeUtils.isEmpty(query)) {
      return ResponseObject.parameterNotFound(SVC_GLOBAL_SEARCH, VAR_QUERY);
    }

    SqlSelect articleIdQuery = new SqlSelect()
        .addFrom(TBL_TCD_ARTICLES)
        .addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID)
        .setWhere(SqlUtils.contains(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_NAME, query));

    List<EcItem> items = getItems(articleIdQuery);
    if (items.isEmpty()) {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(query));
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private Map<Integer, String> getArticleCategories(String tempArticleIds) {
    Map<Integer, String> result = Maps.newHashMap();

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TCD_ARTICLE_CATEGORIES, COL_TCD_ARTICLE_ID, COL_TCD_CATEGORY_ID)
        .addFrom(TBL_TCD_ARTICLE_CATEGORIES)
        .addFromInner(tempArticleIds, SqlUtils.joinUsing(tempArticleIds,
            TBL_TCD_ARTICLE_CATEGORIES, COL_TCD_ARTICLE_ID))
        .addOrder(TBL_TCD_ARTICLE_CATEGORIES, COL_TCD_ARTICLE_ID, COL_TCD_CATEGORY_ID);

    SimpleRowSet data = qs.getData(query);
    if (!DataUtils.isEmpty(data)) {
      int lastArt = 0;
      StringBuilder sb = new StringBuilder();

      for (SimpleRow row : data) {
        int art = row.getInt(COL_TCD_ARTICLE_ID);
        int cat = row.getInt(COL_TCD_CATEGORY_ID);

        if (art != lastArt) {
          if (sb.length() > 0) {
            result.put(lastArt, sb.toString());
            lastArt = art;
            sb = new StringBuilder();
          }
        }
        sb.append(CATEGORY_SEPARATOR).append(cat);
      }

      if (sb.length() > 0) {
        result.put(lastArt, sb.toString());
      }
    }

    return result;
  }

  private ResponseObject getCarManufacturers() {
    SqlSelect query = new SqlSelect().setDistinctMode(true)
        .addFrom(TBL_TCD_MODELS)
        .addFields(TBL_TCD_MODELS, COL_TCD_MANUFACTURER)
        .addOrder(TBL_TCD_MODELS, COL_TCD_MANUFACTURER);

    String[] manufacturers = qs.getColumn(query);
    return ResponseObject.response(manufacturers).setSize(ArrayUtils.length(manufacturers));
  }

  private ResponseObject getCarModels(String manufacturer) {
    if (BeeUtils.isEmpty(manufacturer)) {
      return ResponseObject.parameterNotFound(SVC_GET_CAR_MODELS, VAR_MANUFACTURER);
    }

    SqlSelect query = new SqlSelect()
        .addFrom(TBL_TCD_MODELS)
        .addFromInner(TBL_TCD_TYPES,
            SqlUtils.joinUsing(TBL_TCD_MODELS, TBL_TCD_TYPES, COL_TCD_MODEL_ID))
        .addFields(TBL_TCD_MODELS, COL_TCD_MODEL_ID, COL_TCD_MODEL_NAME, COL_TCD_MANUFACTURER)
        .addMin(TBL_TCD_TYPES, COL_TCD_PRODUCED_FROM)
        .addMax(TBL_TCD_TYPES, COL_TCD_PRODUCED_TO)
        .setWhere(SqlUtils.equals(TBL_TCD_MODELS, COL_TCD_MANUFACTURER, manufacturer))
        .addGroup(TBL_TCD_MODELS, COL_TCD_MODEL_ID, COL_TCD_MODEL_NAME, COL_TCD_MANUFACTURER)
        .addOrder(TBL_TCD_MODELS, COL_TCD_MODEL_NAME);

    SimpleRowSet rowSet = qs.getData(query);
    if (DataUtils.isEmpty(rowSet)) {
      return ResponseObject
          .warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(manufacturer));
    }

    List<EcCarModel> carModels = Lists.newArrayList();
    for (SimpleRow row : rowSet) {
      carModels.add(new EcCarModel(row));
    }

    return ResponseObject.response(carModels).setSize(carModels.size());
  }

  private ResponseObject getCarTypes(String modelId) {
    if (!BeeUtils.isPositiveInt(modelId)) {
      return ResponseObject.parameterNotFound(SVC_GET_CAR_TYPES, VAR_MODEL);
    }

    SqlSelect query = new SqlSelect()
        .addFrom(TBL_TCD_MODELS)
        .addFromInner(TBL_TCD_TYPES,
            SqlUtils.joinUsing(TBL_TCD_MODELS, TBL_TCD_TYPES, COL_TCD_MODEL_ID))
        .addFields(TBL_TCD_MODELS, COL_TCD_MODEL_ID, COL_TCD_MODEL_NAME, COL_TCD_MANUFACTURER)
        .addFields(TBL_TCD_TYPES, COL_TCD_TYPE_ID, COL_TCD_TYPE_NAME,
            COL_TCD_PRODUCED_FROM, COL_TCD_PRODUCED_TO, COL_TCD_CCM,
            COL_TCD_KW_FROM, COL_TCD_KW_TO, COL_TCD_CYLINDERS, COL_TCD_MAX_WEIGHT,
            COL_TCD_ENGINE, COL_TCD_FUEL, COL_TCD_BODY, COL_TCD_AXLE)
        .setWhere(SqlUtils.equals(TBL_TCD_MODELS, COL_TCD_MODEL_ID, modelId))
        .addOrder(TBL_TCD_MODELS, COL_TCD_MODEL_NAME);

    SimpleRowSet rowSet = qs.getData(query);
    if (DataUtils.isEmpty(rowSet)) {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(modelId));
    }

    List<EcCarType> carTypes = Lists.newArrayList();
    for (SimpleRow row : rowSet) {
      carTypes.add(new EcCarType(row));
    }

    return ResponseObject.response(carTypes).setSize(carTypes.size());
  }

  private ResponseObject getCategories() {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_TCD_CATEGORIES, COL_TCD_CATEGORY_ID, COL_TCD_PARENT_ID,
            COL_TCD_CATEGORY_NAME)
        .addFrom(TBL_TCD_CATEGORIES)
        .addOrder(TBL_TCD_CATEGORIES, COL_TCD_CATEGORY_ID);

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      String msg = TBL_TCD_CATEGORIES + ": data not available";
      logger.warning(msg);
      return ResponseObject.error(msg);
    }

    int rc = data.getNumberOfRows();
    int cc = data.getNumberOfColumns();

    String[] arr = new String[rc * cc];
    int i = 0;

    for (String[] row : data.getRows()) {
      for (int j = 0; j < cc; j++) {
        arr[i * cc + j] = row[j];
      }
      i++;
    }

    return ResponseObject.response(arr).setSize(rc);
  }

  private ResponseObject getConfiguration() {
    BeeRowSet rowSet = qs.getViewData(VIEW_CONFIGURATION);
    if (rowSet == null) {
      return ResponseObject.error("cannot read", VIEW_CONFIGURATION);
    }

    Map<String, String> result = Maps.newHashMap();
    if (rowSet.isEmpty()) {
      for (BeeColumn column : rowSet.getColumns()) {
        result.put(column.getId(), null);
      }
    } else {
      BeeRow row = rowSet.getRow(0);
      for (int i = 0; i < rowSet.getNumberOfColumns(); i++) {
        result.put(rowSet.getColumnId(i), row.getString(i));
      }
    }

    return ResponseObject.response(result);
  }

  private SimpleRowSet getCurrentClientInfo(String... fields) {
    return qs.getData(new SqlSelect().addFrom(TBL_CLIENTS).addFields(TBL_CLIENTS, fields)
        .setWhere(SqlUtils.equals(TBL_CLIENTS, COL_CLIENT_USER, usr.getCurrentUserId())));
  }

  private ResponseObject getDeliveryMethods() {
    SqlSelect query = new SqlSelect()
        .addFrom(TBL_DELIVERY_METHODS)
        .addFields(TBL_DELIVERY_METHODS, COL_DELIVERY_METHOD_ID, COL_DELIVERY_METHOD_NAME,
            COL_DELIVERY_METHOD_NOTES)
        .addOrder(TBL_DELIVERY_METHODS, COL_DELIVERY_METHOD_NAME);

    SimpleRowSet rowSet = qs.getData(query);
    if (DataUtils.isEmpty(rowSet)) {
      return ResponseObject.warning(usr.getLocalizableMesssages().dataNotAvailable(
          usr.getLocalizableConstants().ecDeliveryMethods()));
    }

    List<DeliveryMethod> deliveryMethods = Lists.newArrayList();
    for (SimpleRow row : rowSet) {
      deliveryMethods.add(new DeliveryMethod(row.getLong(COL_DELIVERY_METHOD_ID),
          row.getValue(COL_DELIVERY_METHOD_NAME), row.getValue(COL_DELIVERY_METHOD_NOTES)));
    }

    return ResponseObject.response(deliveryMethods);
  }

  private ResponseObject getFeaturedAndNoveltyItems() {
    int offset = BeeUtils.randomInt(0, 100) * 100;
    int limit = BeeUtils.randomInt(5, 30);

    SqlSelect articleIdQuery = new SqlSelect()
        .addFrom(TBL_TCD_ARTICLES)
        .addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID)
        .setOffset(offset)
        .setLimit(limit);

    List<EcItem> items = getItems(articleIdQuery);
    return ResponseObject.response(items);
  }

  private ResponseObject getItemAnalogs(RequestInfo reqInfo) {
    Long id = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_TCD_ARTICLE_ID));
    String code = normalizeCode(reqInfo.getParameter(COL_TCD_ARTICLE_NR));
    String brand = reqInfo.getParameter(COL_TCD_BRAND);

    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_TCD_ANALOGS, COL_TCD_ARTICLE_ID)
        .addFrom(TBL_TCD_ANALOGS)
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_TCD_ANALOGS, COL_TCD_SEARCH_NR, code,
            COL_TCD_BRAND, brand), SqlUtils.notEqual(TBL_TCD_ANALOGS, COL_TCD_ARTICLE_ID, id)));

    return ResponseObject.response(getItems(articleIdQuery));
  }

  private ResponseObject getItemManufacturers() {
    String[] manufacturers = qs.getColumn(new SqlSelect().setDistinctMode(true)
        .addFields(TBL_TCD_ARTICLE_BRANDS, COL_TCD_BRAND)
        .addFrom(TBL_TCD_ARTICLE_BRANDS)
        .addOrder(TBL_TCD_ARTICLE_BRANDS, COL_TCD_BRAND));

    return ResponseObject.response(manufacturers).setSize(ArrayUtils.length(manufacturers));
  }

  private List<EcItem> getItems(SqlSelect query) {
    List<EcItem> items = Lists.newArrayList();

    String tempArticleIds = createTempArticleIds(query);

    SqlSelect articleQuery = new SqlSelect()
        .addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID, COL_TCD_ARTICLE_NAME)
        .addFields(TBL_TCD_ARTICLE_BRANDS, COL_TCD_BRAND, COL_TCD_ANALOG_NR, COL_TCD_PRICE,
            COL_TCD_SUPPLIER, COL_TCD_SUPPLIER_ID)
        .addSum(TBL_TCD_REMAINDERS, COL_TCD_REMAINDER)
        .addFrom(tempArticleIds)
        .addFromInner(TBL_TCD_ARTICLES, SqlUtils.joinUsing(tempArticleIds, TBL_TCD_ARTICLES,
            COL_TCD_ARTICLE_ID))
        .addFromInner(TBL_TCD_ARTICLE_BRANDS,
            SqlUtils.joinUsing(TBL_TCD_ARTICLES, TBL_TCD_ARTICLE_BRANDS, COL_TCD_ARTICLE_ID))
        .addFromLeft(TBL_TCD_REMAINDERS,
            sys.joinTables(TBL_TCD_ARTICLE_BRANDS, TBL_TCD_REMAINDERS, COL_TCD_ARTICLE_BRAND))
        .addGroup(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID, COL_TCD_ARTICLE_NAME)
        .addGroup(TBL_TCD_ARTICLE_BRANDS, COL_TCD_BRAND, COL_TCD_ANALOG_NR, COL_TCD_PRICE,
            COL_TCD_SUPPLIER, COL_TCD_SUPPLIER_ID)
        .addOrder(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_NAME, COL_TCD_ARTICLE_ID);

    SimpleRowSet articleData = qs.getData(articleQuery);
    if (!DataUtils.isEmpty(articleData)) {
      for (SimpleRow row : articleData) {
        EcItem item = new EcItem(row.getInt(COL_TCD_ARTICLE_ID));

        item.setManufacturer(row.getValue(COL_TCD_BRAND));
        item.setCode(row.getValue(COL_TCD_ANALOG_NR));
        item.setName(row.getValue(COL_TCD_ARTICLE_NAME));

        item.setSupplier(row.getValue(COL_TCD_SUPPLIER));
        item.setSupplierCode(row.getValue(COL_TCD_SUPPLIER_ID));

        item.setStock1(BeeUtils.unbox(row.getInt(COL_TCD_REMAINDER)));
        item.setStock2(BeeUtils.randomInt(0, 20) * BeeUtils.randomInt(0, 2));

        Double price = row.getDouble(COL_TCD_PRICE);
        if (BeeUtils.isPositive(price)) {
          item.setListPrice(price * (1 + Math.random() * 0.5));
          item.setPrice(price);
        }
        items.add(item);
      }
    }

    if (!items.isEmpty()) {
      Map<Integer, String> articleCategories = getArticleCategories(tempArticleIds);
      for (EcItem item : items) {
        item.setCategories(articleCategories.get(item.getId()));
      }
    }

    qs.sqlDropTemp(tempArticleIds);

    return items;
  }

  private ResponseObject getItemsByCarType(RequestInfo reqInfo) {
    String typeId = reqInfo.getParameter(VAR_TYPE);

    if (!BeeUtils.isPositiveInt(typeId)) {
      return ResponseObject.parameterNotFound(SVC_GET_ITEMS_BY_CAR_TYPE, VAR_TYPE);
    }
    SqlSelect articleIdQuery = new SqlSelect()
        .addFields(TBL_TCD_TYPE_ARTICLES, COL_TCD_ARTICLE_ID)
        .addFrom(TBL_TCD_TYPE_ARTICLES)
        .setWhere(SqlUtils.equals(TBL_TCD_TYPE_ARTICLES, COL_TCD_TYPE_ID, typeId));

    List<EcItem> items = getItems(articleIdQuery);

    if (items.isEmpty()) {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(typeId));
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private ResponseObject getItemsByManufacturer(RequestInfo reqInfo) {
    String brand = reqInfo.getParameter(VAR_MANUFACTURER);

    if (BeeUtils.isEmpty(brand)) {
      return ResponseObject.parameterNotFound(SVC_GET_ITEMS_BY_MANUFACTURER, VAR_MANUFACTURER);
    }
    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_TCD_ARTICLE_BRANDS, COL_TCD_ARTICLE_ID)
        .addFrom(TBL_TCD_ARTICLE_BRANDS)
        .setWhere(SqlUtils.equals(TBL_TCD_ARTICLE_BRANDS, COL_TCD_BRAND, brand));

    List<EcItem> items = getItems(articleIdQuery);

    if (items.isEmpty()) {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(brand));
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private void logHistory(String service, String query, int count, long duration) {
    SqlInsert ins = new SqlInsert(TBL_HISTORY);
    ins.addConstant(COL_HISTORY_DATE, System.currentTimeMillis());
    ins.addConstant(COL_HISTORY_USER, usr.getCurrentUserId());

    ins.addConstant(COL_HISTORY_SERVICE, service);
    if (!BeeUtils.isEmpty(query)) {
      ins.addConstant(COL_HISTORY_QUERY, query);
    }

    ins.addConstant(COL_HISTORY_COUNT, count);
    ins.addConstant(COL_HISTORY_DURATION, duration);

    qs.insertData(ins);
  }

  private ResponseObject saveConfiguration(RequestInfo reqInfo) {
    String column = reqInfo.getParameter(Service.VAR_COLUMN);
    if (BeeUtils.isEmpty(column)) {
      return ResponseObject.parameterNotFound(SVC_SAVE_CONFIGURATION, Service.VAR_COLUMN);
    }

    String value = reqInfo.getParameter(Service.VAR_VALUE);
    if (BeeUtils.isEmpty(value)) {
      return ResponseObject.parameterNotFound(SVC_SAVE_CONFIGURATION, Service.VAR_VALUE);
    }

    if (updateConfiguration(column, value)) {
      return ResponseObject.response(column);
    } else {
      String message = BeeUtils.joinWords(SVC_SAVE_CONFIGURATION, column,
          "cannot save configuration");
      logger.severe(message);
      return ResponseObject.error(message);
    }
  }

  private ResponseObject searchByItemCode(String code, IsCondition clause) {
    if (BeeUtils.isEmpty(code)) {
      return ResponseObject.parameterNotFound(SVC_SEARCH_BY_ITEM_CODE, VAR_QUERY);
    }
    String search = normalizeCode(code);
    if (BeeUtils.length(search) < 3) {
      return ResponseObject.error("Search code must be at least 3 characters length:", search);
    }
    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_TCD_ANALOGS, COL_TCD_ARTICLE_ID)
        .addFrom(TBL_TCD_ANALOGS)
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_TCD_ANALOGS, COL_TCD_SEARCH_NR, search),
            clause));

    List<EcItem> items = getItems(articleIdQuery);
    if (items.isEmpty()) {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(code));
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private ResponseObject searchByOeNumber(String code) {
    if (BeeUtils.isEmpty(code)) {
      return ResponseObject.parameterNotFound(SVC_SEARCH_BY_OE_NUMBER, VAR_QUERY);
    }
    return searchByItemCode(code, SqlUtils.equals(TBL_TCD_ANALOGS, COL_TCD_KIND, "3"));
  }

  private ResponseObject submitOrder(RequestInfo reqInfo) {
    String serializedCart = reqInfo.getParameter(VAR_CART);
    if (BeeUtils.isEmpty(serializedCart)) {
      return ResponseObject.parameterNotFound(SVC_SUBMIT_ORDER, VAR_CART);
    }

    Cart cart = Cart.restore(serializedCart);
    if (cart == null || cart.isEmpty()) {
      String message = BeeUtils.joinWords(SVC_SUBMIT_ORDER, "cart deserialization failed");
      logger.severe(message);
      return ResponseObject.error(message);
    }

    SimpleRowSet clientInfo = getCurrentClientInfo(COL_CLIENT_ID, COL_CLIENT_MANAGER);
    if (DataUtils.isEmpty(clientInfo)) {
      String message = BeeUtils.joinWords(SVC_SUBMIT_ORDER, "client not found for user",
          usr.getCurrentUserId());
      logger.severe(message);
      return ResponseObject.error(message);
    }

    SqlInsert insOrder = new SqlInsert(VIEW_ORDERS);

    insOrder.addConstant(COL_ORDER_DATE, TimeUtils.nowMinutes().getTime());
    insOrder.addConstant(COL_ORDER_STATUS, EcOrderStatus.NEW.ordinal());

    insOrder.addConstant(COL_ORDER_CLIENT, clientInfo.getLong(0, COL_CLIENT_ID));
    Long manager = clientInfo.getLong(0, COL_CLIENT_MANAGER);
    if (manager != null) {
      insOrder.addConstant(COL_ORDER_MANAGER, manager);
    }

    insOrder.addConstant(COL_ORDER_DELIVERY_METHOD, cart.getDeliveryMethod());
    if (!BeeUtils.isEmpty(cart.getDeliveryAddress())) {
      insOrder.addConstant(COL_ORDER_DELIVERY_ADDRESS, cart.getDeliveryAddress());
    }

    if (!BeeUtils.isTrue(cart.getCopyByMail())) {
      insOrder.addConstant(COL_ORDER_COPY_BY_MAIL, cart.getCopyByMail());
    }

    if (!BeeUtils.isEmpty(cart.getComment())) {
      insOrder.addConstant(COL_ORDER_CLIENT_COMMENT, cart.getComment());
    }

    ResponseObject response = qs.insertDataWithResponse(insOrder);
    if (response.hasErrors() || !response.hasResponse(Long.class)) {
      return response;
    }

    Long orderId = (Long) response.getResponse();

    for (CartItem cartItem : cart.getItems()) {
      SqlInsert insItem = new SqlInsert(VIEW_ORDER_ITEMS);

      insItem.addConstant(COL_ORDER_ITEM_ORDER_ID, orderId);
      insItem.addConstant(COL_ORDER_ITEM_ID, cartItem.getEcItem().getId());

      insItem.addConstant(COL_ORDER_ITEM_QUANTITY_ORDERED, cartItem.getQuantity());
      insItem.addConstant(COL_ORDER_ITEM_QUANTITY_SUBMIT, cartItem.getQuantity());

      insItem.addConstant(COL_ORDER_ITEM_PRICE, cartItem.getEcItem().getRealPrice());

      ResponseObject itemResponse = qs.insertDataWithResponse(insItem);
      if (itemResponse.hasErrors()) {
        return itemResponse;
      }
    }

    return response;
  }

  private boolean updateConfiguration(String column, String value) {
    BeeRowSet rowSet = qs.getViewData(VIEW_CONFIGURATION);

    if (DataUtils.isEmpty(rowSet)) {
      if (BeeUtils.isEmpty(value)) {
        return true;
      } else {
        SqlInsert ins = new SqlInsert(TBL_CONFIGURATION).addConstant(column, value);

        ResponseObject response = qs.insertDataWithResponse(ins);
        return !response.hasErrors();
      }

    } else {
      String oldValue = rowSet.getString(0, column);
      if (BeeUtils.equalsTrimRight(value, oldValue)) {
        return true;
      } else {
        SqlUpdate upd = new SqlUpdate(TBL_CONFIGURATION).addConstant(column, value);
        upd.setWhere(SqlUtils.equals(TBL_CONFIGURATION, COL_CONFIG_ID, rowSet.getRow(0).getId()));

        ResponseObject response = qs.updateDataWithResponse(upd);
        return !response.hasErrors();
      }
    }
  }
}
