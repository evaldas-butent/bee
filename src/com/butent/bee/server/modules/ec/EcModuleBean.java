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
import com.butent.bee.server.data.ViewEventHandler;
import com.butent.bee.server.data.ViewEvent.ViewQueryEvent;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.communication.ResponseObject;
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
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.time.TimeUtils;
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
    ResponseObject response = null;
    String svc = reqInfo.getParameter(EC_METHOD);

    if (BeeUtils.same(svc, SVC_FEATURED_AND_NOVELTY)) {
      response = getFeaturedAndNoveltyItems();

    } else if (BeeUtils.same(svc, SVC_GET_CATEGORIES)) {
      response = getCategories();

    } else if (BeeUtils.same(svc, SVC_GLOBAL_SEARCH)) {
      response = doGlobalSearch(reqInfo);

    } else if (BeeUtils.same(svc, SVC_SEARCH_BY_ITEM_CODE)) {
      response = searchByItemCode(reqInfo);

    } else if (BeeUtils.same(svc, SVC_SEARCH_BY_OE_NUMBER)) {
      response = searchByOeNumber(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_CAR_MANUFACTURERS)) {
      response = getCarManufacturers();
    } else if (BeeUtils.same(svc, SVC_GET_CAR_MODELS)) {
      response = getCarModels(reqInfo);
    } else if (BeeUtils.same(svc, SVC_GET_CAR_TYPES)) {
      response = getCarTypes(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_ITEMS_BY_CAR_TYPE)) {
      response = getItemsByCarType(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_ITEM_MANUFACTURERS)) {
      response = getItemManufacturers();
    } else if (BeeUtils.same(svc, SVC_GET_ITEMS_BY_MANUFACTURER)) {
      response = getItemsByManufacturer(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_DELIVERY_METHODS)) {
      response = getDeliveryMethods();

    } else if (BeeUtils.same(svc, SVC_SUBMIT_ORDER)) {
      response = submitOrder(reqInfo);

    } else if (BeeUtils.same(svc, "ITEM_INFO")) {
      response = getItemInfo(BeeUtils.toLong(reqInfo.getParameter("ID")));

    } else {
      String msg = BeeUtils.joinWords("e-commerce service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
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

  private String createTempArticleIds(SqlSelect query) {
    String tmp = qs.sqlCreateTemp(query);
    qs.sqlIndex(tmp, COL_TCD_ARTICLE_ID);
    return tmp;
  }

  private ResponseObject doGlobalSearch(RequestInfo reqInfo) {
    String query = reqInfo.getParameter(VAR_QUERY);
    if (BeeUtils.isEmpty(query)) {
      return ResponseObject.parameterNotFound(SVC_GLOBAL_SEARCH, VAR_QUERY);
    }

    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFrom(TBL_TCD_ARTICLES)
        .addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID)
        .setWhere(SqlUtils.contains(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_NAME, query))
        .setLimit(getLimit(reqInfo));

    List<EcItem> items = getItems(articleIdQuery);
    if (items.isEmpty()) {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(query));
    } else {
      return ResponseObject.response(items);
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

    return ResponseObject.response(qs.getColumn(query));
  }

  private ResponseObject getCarModels(RequestInfo reqInfo) {
    String manufacturer = reqInfo.getParameter(VAR_MANUFACTURER);
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

    return ResponseObject.response(carModels);
  }

  private ResponseObject getCarTypes(RequestInfo reqInfo) {
    String modelId = reqInfo.getParameter(VAR_MODEL);
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

    return ResponseObject.response(carTypes);
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

    return ResponseObject.response(arr);
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
    int limit = BeeUtils.randomInt(1, 30);

    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFrom(TBL_TCD_ARTICLES)
        .addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID)
        .setOffset(offset)
        .setLimit(limit);

    List<EcItem> items = getItems(articleIdQuery);
    return ResponseObject.response(items);
  }

  private ResponseObject getItemInfo(long id) {
    SqlSelect query =
        new SqlSelect()
            .addFields(TBL_TCD_CATEGORIES, COL_TCD_CATEGORY_NAME, COL_TCD_CATEGORY_ID,
                COL_TCD_PARENT_ID)
            .addFrom(TBL_TCD_ARTICLE_CATEGORIES)
            .addFromInner(TBL_TCD_CATEGORIES, SqlUtils.joinUsing(TBL_TCD_ARTICLE_CATEGORIES,
                TBL_TCD_CATEGORIES, COL_TCD_CATEGORY_ID))
            .setWhere(SqlUtils.equals(TBL_TCD_ARTICLE_CATEGORIES, COL_TCD_ARTICLE_ID, id));

    return ResponseObject.response(qs.getData(query));
  }

  private ResponseObject getItemManufacturers() {
    SqlSelect query = new SqlSelect().setDistinctMode(true)
        .addFrom(TBL_TCD_ARTICLES)
        .addFields(TBL_TCD_ARTICLES, COL_TCD_SUPPLIER)
        .addOrder(TBL_TCD_ARTICLES, COL_TCD_SUPPLIER);

    return ResponseObject.response(qs.getColumn(query));
  }

  private List<EcItem> getItems(SqlSelect query) {
    List<EcItem> items = Lists.newArrayList();

    String tempArticleIds = createTempArticleIds(query);

    SqlSelect articleQuery = new SqlSelect()
        .addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID, COL_TCD_ARTICLE_NR, COL_TCD_ARTICLE_NAME,
            COL_TCD_SUPPLIER)
        .addSum(TBL_TCD_MOTONET, COL_TCD_REMAINDER)
        .addMax(TBL_TCD_MOTONET, COL_TCD_PRICE)
        .addFrom(tempArticleIds)
        .addFromInner(TBL_TCD_ARTICLES, SqlUtils.joinUsing(tempArticleIds, TBL_TCD_ARTICLES,
            COL_TCD_ARTICLE_ID))
        .addFromLeft(TBL_TCD_MOTONET, SqlUtils.joinUsing(TBL_TCD_ARTICLES, TBL_TCD_MOTONET,
            COL_TCD_ARTICLE_ID))
        .addGroup(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID, COL_TCD_ARTICLE_NR, COL_TCD_ARTICLE_NAME,
            COL_TCD_SUPPLIER)
        .addOrder(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_NAME, COL_TCD_ARTICLE_ID);

    SimpleRowSet articleData = qs.getData(articleQuery);
    if (!DataUtils.isEmpty(articleData)) {
      for (SimpleRow row : articleData) {
        EcItem item = new EcItem(row.getInt(COL_TCD_ARTICLE_ID));

        item.setCode(row.getValue(COL_TCD_ARTICLE_NR));
        item.setName(row.getValue(COL_TCD_ARTICLE_NAME));
        item.setSupplier(row.getValue(COL_TCD_SUPPLIER));

        item.setStock1(row.getInt(COL_TCD_REMAINDER));
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

    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFrom(TBL_TCD_TYPE_ARTICLES)
        .addFields(TBL_TCD_TYPE_ARTICLES, COL_TCD_ARTICLE_ID)
        .setWhere(SqlUtils.equals(TBL_TCD_TYPE_ARTICLES, COL_TCD_TYPE_ID, typeId));

    List<EcItem> items = getItems(articleIdQuery);
    if (items.isEmpty()) {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(typeId));
    } else {
      return ResponseObject.response(items);
    }
  }

  private ResponseObject getItemsByManufacturer(RequestInfo reqInfo) {
    String query = reqInfo.getParameter(VAR_MANUFACTURER);
    if (BeeUtils.isEmpty(query)) {
      return ResponseObject.parameterNotFound(SVC_GET_ITEMS_BY_MANUFACTURER, VAR_MANUFACTURER);
    }

    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFrom(TBL_TCD_ARTICLES)
        .addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID)
        .setWhere(SqlUtils.equals(TBL_TCD_ARTICLES, COL_TCD_SUPPLIER, query));

    List<EcItem> items = getItems(articleIdQuery);
    if (items.isEmpty()) {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(query));
    } else {
      return ResponseObject.response(items);
    }
  }

  private int getLimit(RequestInfo reqInfo) {
    return BeeUtils.positive(BeeUtils.toInt(reqInfo.getParameter(VAR_LIMIT)), 100);
  }

  private ResponseObject searchByItemCode(RequestInfo reqInfo) {
    String code = reqInfo.getParameter(VAR_QUERY);
    if (BeeUtils.isEmpty(code)) {
      return ResponseObject.parameterNotFound(SVC_SEARCH_BY_ITEM_CODE, VAR_QUERY);
    }

    int offset = BeeUtils.toInt(reqInfo.getParameter(VAR_OFFSET));
    int limit = BeeUtils.toInt(reqInfo.getParameter(VAR_LIMIT));

    String searchCode = code.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    if (BeeUtils.length(searchCode) < 3) {
      return ResponseObject.error("Search code must be at least 3 characters length:", searchCode);
    }

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID, COL_TCD_ARTICLE_NR, COL_TCD_ARTICLE_NAME,
            COL_TCD_SUPPLIER)
        .addField(TBL_TCD_ANALOGS, COL_TCD_SUPPLIER, ALS_TCD_ANALOG_SUPPLIER)
        .addSum(TBL_TCD_MOTONET, COL_TCD_REMAINDER)
        .addMax(TBL_TCD_MOTONET, COL_TCD_PRICE)
        .addFrom(TBL_TCD_ANALOGS)
        .addFromInner(TBL_TCD_ARTICLES, SqlUtils.joinUsing(TBL_TCD_ANALOGS, TBL_TCD_ARTICLES,
            COL_TCD_ARTICLE_ID))
        .addFromLeft(TBL_TCD_MOTONET, SqlUtils.joinUsing(TBL_TCD_ARTICLES, TBL_TCD_MOTONET,
            COL_TCD_ARTICLE_ID))
        .setWhere(SqlUtils.equals(TBL_TCD_ANALOGS, COL_TCD_SEARCH_NR, searchCode))
        .addGroup(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID, COL_TCD_ARTICLE_NR, COL_TCD_ARTICLE_NAME,
            COL_TCD_SUPPLIER)
        .addGroup(TBL_TCD_ANALOGS, COL_TCD_SUPPLIER)
        .addOrder(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID);

    if (limit > 0) {
      query.setLimit(limit);
    }
    if (offset > 0) {
      query.setOffset(offset);
    }

    List<EcItem> items = Lists.newArrayList();

    for (SimpleRow row : qs.getData(query)) {
      EcItem item = new EcItem(row.getInt(COL_TCD_ARTICLE_ID));
      item.setName(row.getValue(COL_TCD_ARTICLE_NAME));
      item.setCode(row.getValue(COL_TCD_ARTICLE_NR));
      item.setSupplier(BeeUtils.notEmpty(row.getValue(ALS_TCD_ANALOG_SUPPLIER),
          row.getValue(COL_TCD_SUPPLIER)));
      item.setStock1(row.getInt(COL_TCD_REMAINDER));

      Double price = row.getDouble(COL_TCD_PRICE);
      if (BeeUtils.isPositive(price)) {
        item.setListPrice(price * (1 + Math.random() * 0.5));
        item.setPrice(price);
      }

      items.add(item);
    }
    return ResponseObject.response(items);
  }

  private ResponseObject searchByOeNumber(RequestInfo reqInfo) {
    String query = reqInfo.getParameter(VAR_QUERY);
    if (BeeUtils.isEmpty(query)) {
      return ResponseObject.parameterNotFound(SVC_SEARCH_BY_OE_NUMBER, VAR_QUERY);
    }

    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFrom(TBL_TCD_ARTICLES)
        .addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_ID)
        .setWhere(SqlUtils.startsWith(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_NR, query))
        .setLimit(getLimit(reqInfo));

    List<EcItem> items = getItems(articleIdQuery);
    if (items.isEmpty()) {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(query));
    } else {
      return ResponseObject.response(items);
    }
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
}
