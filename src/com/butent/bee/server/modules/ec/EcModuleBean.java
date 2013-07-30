package com.butent.bee.server.modules.ec;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Longs;

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
import com.butent.bee.shared.BeeConst;
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
import com.butent.bee.shared.modules.ec.ArticleBrand;
import com.butent.bee.shared.modules.ec.ArticleCriteria;
import com.butent.bee.shared.modules.ec.ArticleRemainder;
import com.butent.bee.shared.modules.ec.Cart;
import com.butent.bee.shared.modules.ec.CartItem;
import com.butent.bee.shared.modules.ec.DeliveryMethod;
import com.butent.bee.shared.modules.ec.EcBrand;
import com.butent.bee.shared.modules.ec.EcCarModel;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcConstants.EcOrderStatus;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.modules.ec.EcItemInfo;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class EcModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(EcModuleBean.class);

  private static IsCondition oeNumberCondition = SqlUtils.equals(TBL_TCD_ANALOGS, COL_TCD_KIND, 3);

  public static String normalizeCode(String code) {
    return code.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
  }

  private static Double getMarginPercent(Collection<Long> categories, Map<Long, Long> parents,
      Map<Long, Long> roots, Map<Long, Double> margins) {

    if (categories.isEmpty() || margins.isEmpty()) {
      return null;
    }

    Map<Long, Double> marginByRoot = Maps.newHashMap();
    Set<Long> traversed = Sets.newHashSet();

    for (Long category : categories) {
      Double margin = null;

      for (Long id = category; id != null && !traversed.contains(id); id = parents.get(id)) {
        traversed.add(id);
        if (margin == null) {
          margin = margins.get(id);
        }
      }

      if (margin != null) {
        marginByRoot.put(roots.get(category), margin);
      }
    }

    if (marginByRoot.isEmpty()) {
      return null;
    } else {
      Double result = null;
      for (double margin : marginByRoot.values()) {
        if (result == null || margin < result) {
          result = margin;
        }
      }
      return result;
    }
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
    Long articleBrand = null;

    boolean log = false;

    if (BeeUtils.same(svc, SVC_FEATURED_AND_NOVELTY)) {
      response = getFeaturedAndNoveltyItems();

    } else if (BeeUtils.same(svc, SVC_GET_CATEGORIES)) {
      response = getCategories();

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
      response = getCarTypes(BeeUtils.toLongOrNull(query));
      log = true;

    } else if (BeeUtils.same(svc, SVC_GET_ITEMS_BY_CAR_TYPE)) {
      response = getItemsByCarType(reqInfo);
      log = true;

    } else if (BeeUtils.same(svc, SVC_GET_ITEM_BRANDS)) {
      response = getItemBrands();

    } else if (BeeUtils.same(svc, SVC_GET_ITEMS_BY_BRAND)) {
      query = reqInfo.getParameter(COL_TCD_BRAND);
      response = getItemsByBrand(BeeUtils.toLongOrNull(query));
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

    } else if (BeeUtils.same(svc, SVC_GET_ITEM_INFO)) {
      query = reqInfo.getParameter(COL_TCD_ARTICLE);
      articleBrand = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_TCD_ARTICLE_BRAND));
      response = getItemInfo(BeeUtils.toLongOrNull(query), articleBrand);
      log = true;

    } else if (BeeUtils.same(svc, SVC_GET_PICTURES)) {
      Set<Long> articles = DataUtils.parseIdSet(reqInfo.getParameter(COL_TCD_ARTICLE));
      response = getPictures(articles);
      query = BeeUtils.toString(articles.size());
      log = true;

    } else {
      String msg = BeeUtils.joinWords("e-commerce service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }

    if (log && response != null && !response.hasErrors()) {
      logHistory(svc, query, articleBrand, response.getSize(),
          System.currentTimeMillis() - startMillis);
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
      public void orderCategories(ViewQueryEvent event) {
        if (event.isAfter() && BeeUtils.same(event.getViewName(), VIEW_CATEGORIES)) {
          BeeRowSet rowSet = event.getRowset();

          if (rowSet.getNumberOfRows() > 1) {
            final int nameIndex = rowSet.getColumnIndex(COL_TCD_CATEGORY_NAME);
            final int parentIndex = rowSet.getColumnIndex(COL_TCD_CATEGORY_PARENT);
            final int fullNameIndex = rowSet.getColumnIndex(COL_TCD_CATEGORY_FULL_NAME);

            if (BeeConst.isUndef(nameIndex) || BeeConst.isUndef(parentIndex)
                || BeeConst.isUndef(fullNameIndex)) {
              return;
            }

            Map<Long, Long> parents = Maps.newHashMap();
            Map<Long, String> names = Maps.newHashMap();

            for (BeeRow row : rowSet.getRows()) {
              Long parent = row.getLong(parentIndex);
              if (parent != null) {
                parents.put(row.getId(), parent);
              }

              names.put(row.getId(), row.getString(nameIndex));
            }

            for (BeeRow row : rowSet.getRows()) {
              Long parent = row.getLong(parentIndex);

              if (parent == null) {
                row.setValue(fullNameIndex, row.getString(nameIndex));

              } else {
                List<String> fullName = Lists.newArrayList(row.getString(nameIndex),
                    row.getString(fullNameIndex));

                while (parent != null && parents.containsKey(parent)) {
                  parent = parents.get(parent);
                  fullName.add(names.get(parent));
                }

                StringBuilder sb = new StringBuilder();
                for (int i = fullName.size() - 1; i >= 0; i--) {
                  sb.append(fullName.get(i));
                  if (i > 0) {
                    sb.append(EcConstants.CATEGORY_NAME_SEPARATOR);
                  }
                }
                row.setValue(fullNameIndex, sb.toString());
              }
            }

            final Collator collator = Collator.getInstance(usr.getLocale());
            collator.setStrength(Collator.IDENTICAL);

            Collections.sort(rowSet.getRows().getList(), new Comparator<BeeRow>() {
              @Override
              public int compare(BeeRow row1, BeeRow row2) {
                String name1 = row1.getString(fullNameIndex);
                String name2 = row2.getString(fullNameIndex);

                int result;
                if (name1 == null) {
                  result = (name2 == null) ? BeeConst.COMPARE_EQUAL : BeeConst.COMPARE_LESS;
                } else if (name2 == null) {
                  result = BeeConst.COMPARE_MORE;
                } else {
                  result = collator.compare(name1.toLowerCase(), name2.toLowerCase());
                }

                if (result == BeeConst.COMPARE_EQUAL) {
                  result = Longs.compare(row1.getId(), row2.getId());
                }

                return result;
              }
            });
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
    qs.sqlIndex(tmp, COL_TCD_ARTICLE);
    return tmp;
  }

  private ResponseObject didNotMatch(String query) {
    return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(query));
  }

  private ResponseObject doGlobalSearch(String query) {
    if (BeeUtils.isEmpty(query)) {
      return ResponseObject.parameterNotFound(SVC_GLOBAL_SEARCH, VAR_QUERY);
    }

    SqlSelect articleIdQuery;
    IsCondition condition;

    if (BeeUtils.isEmpty(BeeUtils.parseDigits(query))) {
      condition = null;
      articleIdQuery = new SqlSelect()
          .addField(TBL_TCD_ARTICLES, sys.getIdName(TBL_TCD_ARTICLES), COL_TCD_ARTICLE)
          .addFrom(TBL_TCD_ARTICLES)
          .setWhere(SqlUtils.contains(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_NAME, query));

    } else {
      condition = SqlUtils.contains(TBL_TCD_ARTICLE_BRANDS, COL_TCD_ANALOG_NR, query);
      articleIdQuery = new SqlSelect().setDistinctMode(true)
          .addFields(TBL_TCD_ARTICLE_BRANDS, COL_TCD_ARTICLE)
          .addFrom(TBL_TCD_ARTICLE_BRANDS)
          .setWhere(condition);
    }

    List<EcItem> items = getItems(articleIdQuery, condition);
    if (items.isEmpty()) {
      return didNotMatch(query);
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private Map<Long, String> getArticleCategories(String tempArticleIds) {
    Map<Long, String> result = Maps.newHashMap();

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TCD_ARTICLE_CATEGORIES, COL_TCD_ARTICLE, COL_TCD_CATEGORY)
        .addFrom(TBL_TCD_ARTICLE_CATEGORIES)
        .addFromInner(tempArticleIds, SqlUtils.joinUsing(tempArticleIds,
            TBL_TCD_ARTICLE_CATEGORIES, COL_TCD_ARTICLE))
        .addOrder(TBL_TCD_ARTICLE_CATEGORIES, COL_TCD_ARTICLE, COL_TCD_CATEGORY);

    SimpleRowSet data = qs.getData(query);
    if (!DataUtils.isEmpty(data)) {
      long lastArt = 0;
      StringBuilder sb = new StringBuilder();

      for (SimpleRow row : data) {
        long art = row.getLong(COL_TCD_ARTICLE);
        long cat = row.getLong(COL_TCD_CATEGORY);

        if (art != lastArt) {
          if (sb.length() > 0) {
            result.put(lastArt, sb.toString());
            lastArt = art;
            sb = new StringBuilder();
          }
        }
        sb.append(CATEGORY_ID_SEPARATOR).append(cat);
      }

      if (sb.length() > 0) {
        result.put(lastArt, sb.toString());
      }
    }

    return result;
  }

  private ResponseObject getCarManufacturers() {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_TCD_MANUFACTURERS, COL_TCD_MANUFACTURER_NAME)
        .addFrom(TBL_TCD_MANUFACTURERS)
        .setWhere(SqlUtils.notNull(TBL_TCD_MANUFACTURERS, COL_TCD_MF_VISIBLE))
        .addOrder(TBL_TCD_MANUFACTURERS, COL_TCD_MANUFACTURER_NAME);

    String[] manufacturers = qs.getColumn(query);
    return ResponseObject.response(manufacturers).setSize(ArrayUtils.length(manufacturers));
  }

  private ResponseObject getCarModels(String manufacturer) {
    if (BeeUtils.isEmpty(manufacturer)) {
      return ResponseObject.parameterNotFound(SVC_GET_CAR_MODELS, VAR_MANUFACTURER);
    }

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TCD_TYPES, COL_TCD_MODEL)
        .addFields(TBL_TCD_MODELS, COL_TCD_MODEL_NAME)
        .addFields(TBL_TCD_MANUFACTURERS, COL_TCD_MANUFACTURER_NAME)
        .addMin(TBL_TCD_TYPES, COL_TCD_PRODUCED_FROM)
        .addMax(TBL_TCD_TYPES, COL_TCD_PRODUCED_TO)
        .addFrom(TBL_TCD_MODELS)
        .addFromInner(TBL_TCD_MANUFACTURERS,
            sys.joinTables(TBL_TCD_MANUFACTURERS, TBL_TCD_MODELS, COL_TCD_MANUFACTURER))
        .addFromInner(TBL_TCD_TYPES, sys.joinTables(TBL_TCD_MODELS, TBL_TCD_TYPES, COL_TCD_MODEL))
        .setWhere(SqlUtils.equals(TBL_TCD_MANUFACTURERS, COL_TCD_MANUFACTURER_NAME, manufacturer))
        .addGroup(TBL_TCD_TYPES, COL_TCD_MODEL)
        .addGroup(TBL_TCD_MODELS, COL_TCD_MODEL_NAME)
        .addGroup(TBL_TCD_MANUFACTURERS, COL_TCD_MANUFACTURER_NAME)
        .addOrder(TBL_TCD_MODELS, COL_TCD_MODEL_NAME);

    SimpleRowSet rowSet = qs.getData(query);
    if (DataUtils.isEmpty(rowSet)) {
      return didNotMatch(manufacturer);
    }

    List<EcCarModel> carModels = Lists.newArrayList();
    for (SimpleRow row : rowSet) {
      carModels.add(new EcCarModel(row));
    }

    return ResponseObject.response(carModels).setSize(carModels.size());
  }

  private ResponseObject getCarTypes(Long modelId) {
    if (!DataUtils.isId(modelId)) {
      return ResponseObject.parameterNotFound(SVC_GET_CAR_TYPES, VAR_MODEL);
    }

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TCD_TYPES, COL_TCD_MODEL)
        .addFields(TBL_TCD_MODELS, COL_TCD_MODEL_NAME)
        .addFields(TBL_TCD_MANUFACTURERS, COL_TCD_MANUFACTURER_NAME)
        .addField(TBL_TCD_TYPES, sys.getIdName(TBL_TCD_TYPES), COL_TCD_TYPE)
        .addFields(TBL_TCD_TYPES, COL_TCD_TYPE_NAME,
            COL_TCD_PRODUCED_FROM, COL_TCD_PRODUCED_TO, COL_TCD_CCM,
            COL_TCD_KW_FROM, COL_TCD_KW_TO, COL_TCD_CYLINDERS, COL_TCD_MAX_WEIGHT,
            COL_TCD_ENGINE, COL_TCD_FUEL, COL_TCD_BODY, COL_TCD_AXLE)
        .addFrom(TBL_TCD_MODELS)
        .addFromInner(TBL_TCD_MANUFACTURERS,
            sys.joinTables(TBL_TCD_MANUFACTURERS, TBL_TCD_MODELS, COL_TCD_MANUFACTURER))
        .addFromInner(TBL_TCD_TYPES, sys.joinTables(TBL_TCD_MODELS, TBL_TCD_TYPES, COL_TCD_MODEL))
        .setWhere(SqlUtils.equals(TBL_TCD_TYPES, COL_TCD_MODEL, modelId))
        .addOrder(TBL_TCD_TYPES, COL_TCD_TYPE_NAME, COL_TCD_PRODUCED_FROM, COL_TCD_PRODUCED_TO,
            COL_TCD_KW_FROM, COL_TCD_KW_TO);

    SimpleRowSet rowSet = qs.getData(query);
    if (DataUtils.isEmpty(rowSet)) {
      return didNotMatch(BeeUtils.toString(modelId));
    }

    List<EcCarType> carTypes = Lists.newArrayList();
    for (SimpleRow row : rowSet) {
      carTypes.add(new EcCarType(row));
    }

    return ResponseObject.response(carTypes).setSize(carTypes.size());
  }

  private ResponseObject getCategories() {
    String idName = sys.getIdName(TBL_TCD_CATEGORIES);

    SqlSelect query = new SqlSelect()
        .addFields(TBL_TCD_CATEGORIES, idName, COL_TCD_CATEGORY_PARENT,
            COL_TCD_CATEGORY_NAME)
        .addFrom(TBL_TCD_CATEGORIES)
        .addOrder(TBL_TCD_CATEGORIES, idName);

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

  private Map<Long, Long> getCategoryParents() {
    Map<Long, Long> result = Maps.newHashMap();

    String colCategoryId = sys.getIdName(TBL_TCD_CATEGORIES);

    SqlSelect query = new SqlSelect();
    query.addFields(TBL_TCD_CATEGORIES, colCategoryId, COL_TCD_CATEGORY_PARENT);
    query.addFrom(TBL_TCD_CATEGORIES);
    query.setWhere(SqlUtils.notNull(TBL_TCD_CATEGORIES, COL_TCD_CATEGORY_PARENT));

    SimpleRowSet data = qs.getData(query);
    if (!DataUtils.isEmpty(data)) {
      for (SimpleRow row : data) {
        result.put(row.getLong(colCategoryId), row.getLong(COL_TCD_CATEGORY_PARENT));
      }
    }

    return result;
  }

  private EcClientDiscounts getClientDiscounts() {
    List<SimpleRowSet> discounts = Lists.newArrayList();

    String colClientId = sys.getIdName(TBL_CLIENTS);
    SimpleRow currentClientInfo = getCurrentClientInfo(colClientId, COL_CLIENT_DISCOUNT_PERCENT,
        COL_CLIENT_DISCOUNT_PARENT);
    if (currentClientInfo == null) {
      return new EcClientDiscounts(null, discounts);
    }

    Long client = currentClientInfo.getLong(colClientId);

    Double percent = currentClientInfo.getDouble(COL_CLIENT_DISCOUNT_PERCENT);
    Long parent = currentClientInfo.getLong(COL_CLIENT_DISCOUNT_PARENT);

    SqlSelect discountQuery = new SqlSelect();
    discountQuery.addFields(TBL_DISCOUNTS, COL_DISCOUNT_DATE_FROM, COL_DISCOUNT_DATE_TO,
        COL_DISCOUNT_CATEGORY, COL_DISCOUNT_BRAND, COL_DISCOUNT_ARTICLE_BRAND,
        COL_DISCOUNT_PERCENT, COL_DISCOUNT_PRICE);
    discountQuery.addFrom(TBL_DISCOUNTS);

    discountQuery.setWhere(SqlUtils.equals(TBL_DISCOUNTS, COL_DISCOUNT_CLIENT, client));

    SimpleRowSet discountData = qs.getData(discountQuery);
    if (!DataUtils.isEmpty(discountData)) {
      discounts.add(discountData);
    }

    if (DataUtils.isId(parent) && !Objects.equal(client, parent)) {
      Set<Long> traversed = Sets.newHashSet(client);

      SqlSelect clientQuery = new SqlSelect();
      clientQuery.addFields(TBL_CLIENTS, COL_CLIENT_DISCOUNT_PERCENT, COL_CLIENT_DISCOUNT_PARENT);
      clientQuery.addFrom(TBL_CLIENTS);

      while (DataUtils.isId(parent) && !traversed.contains(parent)) {
        discountQuery.setWhere(SqlUtils.equals(TBL_DISCOUNTS, COL_DISCOUNT_CLIENT, parent));

        discountData = qs.getData(discountQuery);
        if (!DataUtils.isEmpty(discountData)) {
          discounts.add(discountData);
        }

        clientQuery.setWhere(SqlUtils.equals(TBL_CLIENTS, colClientId, parent));

        SimpleRow clientRow = qs.getRow(clientQuery);
        if (percent == null) {
          percent = clientRow.getDouble(COL_CLIENT_DISCOUNT_PERCENT);
        }

        traversed.add(parent);
        parent = clientRow.getLong(COL_CLIENT_DISCOUNT_PARENT);
      }
    }

    return new EcClientDiscounts(percent, discounts);
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

  private SimpleRow getCurrentClientInfo(String... fields) {
    return qs.getRow(new SqlSelect().addFrom(TBL_CLIENTS).addFields(TBL_CLIENTS, fields)
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
    int offset = BeeUtils.randomInt(0, 30) * 100;
    int limit = BeeUtils.randomInt(5, 30);

    SqlSelect articleIdQuery = new SqlSelect()
        .addFields(TBL_TCD_ARTICLE_GRAPHICS, COL_TCD_ARTICLE)
        .addFrom(TBL_TCD_ARTICLE_GRAPHICS)
        .addFromInner(TBL_TCD_GRAPHICS,
            sys.joinTables(TBL_TCD_GRAPHICS, TBL_TCD_ARTICLE_GRAPHICS, COL_TCD_GRAPHICS))
        .setWhere(SqlUtils.equals(TBL_TCD_ARTICLE_GRAPHICS, COL_TCD_SORT, 1))
        .addOrder(TBL_TCD_ARTICLE_GRAPHICS, COL_TCD_ARTICLE)
        .setOffset(offset)
        .setLimit(limit);

    List<EcItem> items = getItems(articleIdQuery, null);
    return ResponseObject.response(items);
  }

  private ResponseObject getItemAnalogs(RequestInfo reqInfo) {
    Long id = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_TCD_ARTICLE));
    String code = normalizeCode(reqInfo.getParameter(COL_TCD_ANALOG_NR));
    String brand = reqInfo.getParameter(COL_TCD_BRAND_NAME);

    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_TCD_ANALOGS, COL_TCD_ARTICLE)
        .addFrom(TBL_TCD_ANALOGS)
        .setWhere(SqlUtils.and(SqlUtils.notEqual(TBL_TCD_ANALOGS, COL_TCD_ARTICLE, id),
            SqlUtils.equals(TBL_TCD_ANALOGS, COL_TCD_SEARCH_NR, code, COL_TCD_BRAND_NAME, brand)));

    return ResponseObject.response(getItems(articleIdQuery, null));
  }

  private ResponseObject getItemBrands() {
    String colBrandId = sys.getIdName(TBL_TCD_BRANDS);

    SimpleRowSet data = qs.getData(new SqlSelect().setDistinctMode(true)
        .addFields(TBL_TCD_BRANDS, colBrandId, COL_TCD_BRAND_NAME)
        .addFrom(TBL_TCD_ARTICLE_BRANDS)
        .addFromInner(TBL_TCD_BRANDS,
            sys.joinTables(TBL_TCD_BRANDS, TBL_TCD_ARTICLE_BRANDS, COL_TCD_BRAND))
        .addOrder(TBL_TCD_BRANDS, COL_TCD_BRAND_NAME, colBrandId));

    if (DataUtils.isEmpty(data)) {
      return ResponseObject.warning(usr.getLocalizableMesssages().dataNotAvailable(TBL_TCD_BRANDS));
    }

    List<EcBrand> brands = Lists.newArrayList();
    for (SimpleRow row : data) {
      brands.add(new EcBrand(row.getLong(colBrandId), row.getValue(COL_TCD_BRAND_NAME)));
    }

    return ResponseObject.response(brands);
  }

  private ResponseObject getItemInfo(Long articleId, Long articleBrandId) {
    if (!DataUtils.isId(articleId)) {
      return ResponseObject.parameterNotFound(SVC_GET_ITEM_INFO, COL_TCD_ARTICLE);
    }
    if (!DataUtils.isId(articleBrandId)) {
      return ResponseObject.parameterNotFound(SVC_GET_ITEM_INFO, COL_TCD_ARTICLE_BRAND);
    }

    EcItemInfo ecItemInfo = new EcItemInfo();

    SqlSelect criteriaQuery = new SqlSelect()
        .addFields(TBL_TCD_ARTICLE_CRITERIA, COL_TCD_CRITERIA_NAME, COL_TCD_CRITERIA_VALUE)
        .addFrom(TBL_TCD_ARTICLE_CRITERIA)
        .setWhere(SqlUtils.equals(TBL_TCD_ARTICLE_CRITERIA, COL_TCD_ARTICLE, articleId))
        .addOrder(TBL_TCD_ARTICLE_CRITERIA, COL_TCD_CRITERIA_NAME);

    SimpleRowSet criteriaData = qs.getData(criteriaQuery);
    if (!DataUtils.isEmpty(criteriaData)) {
      for (SimpleRow row : criteriaData) {
        ecItemInfo.addCriteria(new ArticleCriteria(row.getValue(COL_TCD_CRITERIA_NAME),
            row.getValue(COL_TCD_CRITERIA_VALUE)));
      }
    }

    SqlSelect brandQuery = new SqlSelect()
        .addFields(TBL_TCD_BRANDS, COL_TCD_BRAND_NAME)
        .addFields(TBL_TCD_ARTICLE_BRANDS, COL_TCD_ANALOG_NR, COL_TCD_SUPPLIER,
            COL_TCD_SUPPLIER_ID)
        .addFrom(TBL_TCD_ARTICLE_BRANDS)
        .addFromInner(TBL_TCD_BRANDS,
            sys.joinTables(TBL_TCD_BRANDS, TBL_TCD_ARTICLE_BRANDS, COL_TCD_BRAND))
        .setWhere(SqlUtils.equals(TBL_TCD_ARTICLE_BRANDS, COL_TCD_ARTICLE, articleId))
        .addOrder(TBL_TCD_BRANDS, COL_TCD_BRAND_NAME)
        .addOrder(TBL_TCD_ARTICLE_BRANDS, COL_TCD_ANALOG_NR, COL_TCD_SUPPLIER,
            COL_TCD_SUPPLIER_ID);

    SimpleRowSet brandData = qs.getData(brandQuery);
    if (!DataUtils.isEmpty(brandData)) {
      for (SimpleRow row : brandData) {
        ecItemInfo.addBrand(new ArticleBrand(row.getValue(COL_TCD_BRAND_NAME),
            row.getValue(COL_TCD_ANALOG_NR), row.getValue(COL_TCD_SUPPLIER),
            row.getValue(COL_TCD_SUPPLIER_ID)));
      }
    }

    SqlSelect remainderQuery = new SqlSelect()
        .addFields(TBL_TCD_REMAINDERS, COL_TCD_WAREHOUSE)
        .addSum(TBL_TCD_REMAINDERS, COL_TCD_REMAINDER)
        .addFrom(TBL_TCD_REMAINDERS)
        .setWhere(SqlUtils.equals(TBL_TCD_REMAINDERS, COL_TCD_ARTICLE_BRAND, articleBrandId))
        .addGroup(TBL_TCD_REMAINDERS, COL_TCD_WAREHOUSE)
        .addOrder(TBL_TCD_REMAINDERS, COL_TCD_WAREHOUSE);

    SimpleRowSet remainderData = qs.getData(remainderQuery);
    if (!DataUtils.isEmpty(remainderData)) {
      for (SimpleRow row : remainderData) {
        ecItemInfo.addRemainder(new ArticleRemainder(row.getValue(COL_TCD_WAREHOUSE),
            row.getDouble(COL_TCD_REMAINDER)));
      }
    }

    SqlSelect carTypeQuery = new SqlSelect()
        .addFields(TBL_TCD_TYPES, COL_TCD_MODEL)
        .addFields(TBL_TCD_MODELS, COL_TCD_MODEL_NAME)
        .addFields(TBL_TCD_MANUFACTURERS, COL_TCD_MANUFACTURER_NAME)
        .addFields(TBL_TCD_TYPES, COL_TCD_TYPE_NAME,
            COL_TCD_PRODUCED_FROM, COL_TCD_PRODUCED_TO, COL_TCD_CCM,
            COL_TCD_KW_FROM, COL_TCD_KW_TO, COL_TCD_CYLINDERS, COL_TCD_MAX_WEIGHT,
            COL_TCD_ENGINE, COL_TCD_FUEL, COL_TCD_BODY, COL_TCD_AXLE)
        .addFields(TBL_TCD_TYPE_ARTICLES, COL_TCD_TYPE)
        .addFrom(TBL_TCD_TYPE_ARTICLES)
        .addFromInner(TBL_TCD_TYPES,
            sys.joinTables(TBL_TCD_TYPES, TBL_TCD_TYPE_ARTICLES, COL_TCD_TYPE))
        .addFromInner(TBL_TCD_MODELS, sys.joinTables(TBL_TCD_MODELS, TBL_TCD_TYPES, COL_TCD_MODEL))
        .addFromInner(TBL_TCD_MANUFACTURERS,
            sys.joinTables(TBL_TCD_MANUFACTURERS, TBL_TCD_MODELS, COL_TCD_MANUFACTURER))
        .setWhere(SqlUtils.equals(TBL_TCD_TYPE_ARTICLES, COL_TCD_ARTICLE, articleId))
        .addOrder(TBL_TCD_MANUFACTURERS, COL_TCD_MANUFACTURER_NAME)
        .addOrder(TBL_TCD_MODELS, COL_TCD_MODEL_NAME)
        .addOrder(TBL_TCD_TYPES, COL_TCD_TYPE_NAME, COL_TCD_PRODUCED_FROM, COL_TCD_PRODUCED_TO,
            COL_TCD_KW_FROM, COL_TCD_KW_TO);

    SimpleRowSet carTypeData = qs.getData(carTypeQuery);
    if (!DataUtils.isEmpty(carTypeData)) {
      for (SimpleRow row : carTypeData) {
        ecItemInfo.addCarType(new EcCarType(row));
      }
    }

    SqlSelect oeNumberQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_TCD_ANALOGS, COL_TCD_ANALOG_NR)
        .addFrom(TBL_TCD_ANALOGS)
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_TCD_ANALOGS, COL_TCD_ARTICLE, articleId),
            oeNumberCondition))
        .addOrder(TBL_TCD_ANALOGS, COL_TCD_ANALOG_NR);

    SimpleRowSet oeNumberData = qs.getData(oeNumberQuery);
    if (!DataUtils.isEmpty(oeNumberData)) {
      for (SimpleRow row : oeNumberData) {
        ecItemInfo.addOeNumber(row.getValue(COL_TCD_ANALOG_NR));
      }
    }

    return ResponseObject.response(ecItemInfo);
  }

  private List<EcItem> getItems(SqlSelect query, IsCondition condition) {
    List<EcItem> items = Lists.newArrayList();

    String tempArticleIds = createTempArticleIds(query);
    String idName = sys.getIdName(TBL_TCD_ARTICLE_BRANDS);

    SqlSelect articleQuery = new SqlSelect()
        .addFields(tempArticleIds, COL_TCD_ARTICLE)
        .addFields(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_NAME)
        .addFields(TBL_TCD_ARTICLE_BRANDS, idName, COL_TCD_BRAND, COL_TCD_ANALOG_NR,
            COL_TCD_SUPPLIER, COL_TCD_SUPPLIER_ID, COL_TCD_COST)
        .addSum(TBL_TCD_REMAINDERS, COL_TCD_REMAINDER)
        .addFrom(tempArticleIds)
        .addFromInner(TBL_TCD_ARTICLES,
            sys.joinTables(TBL_TCD_ARTICLES, tempArticleIds, COL_TCD_ARTICLE))
        .addFromInner(TBL_TCD_ARTICLE_BRANDS,
            sys.joinTables(TBL_TCD_ARTICLES, TBL_TCD_ARTICLE_BRANDS, COL_TCD_ARTICLE))
        .addFromLeft(TBL_TCD_REMAINDERS,
            sys.joinTables(TBL_TCD_ARTICLE_BRANDS, TBL_TCD_REMAINDERS, COL_TCD_ARTICLE_BRAND))
        .addGroup(tempArticleIds, COL_TCD_ARTICLE)
        .addGroup(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_NAME)
        .addGroup(TBL_TCD_ARTICLE_BRANDS, idName, COL_TCD_BRAND, COL_TCD_ANALOG_NR,
            COL_TCD_UPDATED_COST, COL_TCD_SUPPLIER, COL_TCD_SUPPLIER_ID)
        .addOrder(TBL_TCD_ARTICLES, COL_TCD_ARTICLE_NAME)
        .addOrder(tempArticleIds, COL_TCD_ARTICLE);

    if (condition != null) {
      articleQuery.setWhere(condition);
    }

    SimpleRowSet articleData = qs.getData(articleQuery);
    if (!DataUtils.isEmpty(articleData)) {
      for (SimpleRow row : articleData) {
        EcItem item = new EcItem(row.getLong(COL_TCD_ARTICLE), row.getLong(idName));

        item.setBrand(row.getLong(COL_TCD_BRAND));
        item.setCode(row.getValue(COL_TCD_ANALOG_NR));
        item.setName(row.getValue(COL_TCD_ARTICLE_NAME));

        item.setSupplier(row.getValue(COL_TCD_SUPPLIER));
        item.setSupplierCode(row.getValue(COL_TCD_SUPPLIER_ID));

        item.setStock1(BeeUtils.unbox(row.getInt(COL_TCD_REMAINDER)));
        item.setStock2(BeeUtils.randomInt(0, 20) * BeeUtils.randomInt(0, 2));

        item.setPrice(row.getDouble(COL_TCD_COST));

        items.add(item);
      }
    }

    if (!items.isEmpty()) {
      Map<Long, String> articleCategories = getArticleCategories(tempArticleIds);
      for (EcItem item : items) {
        item.setCategories(articleCategories.get(item.getArticleId()));
      }

      setListPrice(items);
      setPrice(items);
    }

    qs.sqlDropTemp(tempArticleIds);

    return items;
  }

  private ResponseObject getItemsByBrand(Long brand) {
    if (!DataUtils.isId(brand)) {
      return ResponseObject.parameterNotFound(SVC_GET_ITEMS_BY_BRAND, COL_TCD_BRAND);
    }

    IsCondition condition = SqlUtils.equals(TBL_TCD_ARTICLE_BRANDS, COL_TCD_BRAND, brand);

    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_TCD_ARTICLE_BRANDS, COL_TCD_ARTICLE)
        .addFrom(TBL_TCD_ARTICLE_BRANDS)
        .setWhere(condition);

    List<EcItem> items = getItems(articleIdQuery, condition);

    if (items.isEmpty()) {
      return didNotMatch(BeeUtils.toString(brand));
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private ResponseObject getItemsByCarType(RequestInfo reqInfo) {
    Long typeId = BeeUtils.toLongOrNull(reqInfo.getParameter(VAR_TYPE));
    if (!DataUtils.isId(typeId)) {
      return ResponseObject.parameterNotFound(SVC_GET_ITEMS_BY_CAR_TYPE, VAR_TYPE);
    }

    SqlSelect articleIdQuery = new SqlSelect()
        .addFields(TBL_TCD_TYPE_ARTICLES, COL_TCD_ARTICLE)
        .addFrom(TBL_TCD_TYPE_ARTICLES)
        .setWhere(SqlUtils.equals(TBL_TCD_TYPE_ARTICLES, COL_TCD_TYPE, typeId));

    List<EcItem> items = getItems(articleIdQuery, null);

    if (items.isEmpty()) {
      return didNotMatch(BeeUtils.toString(typeId));
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private ResponseObject getPictures(Set<Long> articles) {
    if (BeeUtils.isEmpty(articles)) {
      return ResponseObject.parameterNotFound(SVC_GET_PICTURES, COL_TCD_ARTICLE);
    }

    SqlSelect graphicsQuery = new SqlSelect();
    graphicsQuery.addFields(TBL_TCD_ARTICLE_GRAPHICS, COL_TCD_ARTICLE);
    graphicsQuery.addFields(TBL_TCD_GRAPHICS, COL_TCD_GRAPHICS_TYPE, COL_TCD_GRAPHICS_RESOURCE_ID,
        COL_TCD_GRAPHICS_RESOURCE_NO);

    graphicsQuery.addFrom(TBL_TCD_ARTICLE_GRAPHICS);
    graphicsQuery.addFromInner(TBL_TCD_GRAPHICS,
        sys.joinTables(TBL_TCD_GRAPHICS, TBL_TCD_ARTICLE_GRAPHICS, COL_TCD_GRAPHICS));

    graphicsQuery.setWhere(SqlUtils.and(SqlUtils.equals(TBL_TCD_ARTICLE_GRAPHICS, COL_TCD_SORT, 1),
        SqlUtils.inList(TBL_TCD_ARTICLE_GRAPHICS, COL_TCD_ARTICLE, articles)));

    SimpleRowSet graphicsData = qs.getData(graphicsQuery);
    if (DataUtils.isEmpty(graphicsData)) {
      logger.warning("graphics not found for", articles);
      return ResponseObject.response(BeeConst.NULL);
    }

    Multimap<Long, Long> resourceArticles = HashMultimap.create();
    Map<Long, String> resourceTypes = Maps.newHashMap();

    Multimap<String, Long> sources = HashMultimap.create();

    for (SimpleRow row : graphicsData) {
      Long resource = row.getLong(COL_TCD_GRAPHICS_RESOURCE_ID);

      resourceArticles.put(resource, row.getLong(COL_TCD_ARTICLE));
      resourceTypes.put(resource, row.getValue(COL_TCD_GRAPHICS_TYPE));

      sources.put(row.getValue(COL_TCD_GRAPHICS_RESOURCE_NO), resource);
    }

    Map<Long, String> pictures = Maps.newHashMap();

    for (String source : sources.keySet()) {
      String table = TBL_TCD_GRAPHICS_RESOURCES + BeeUtils.trim(source);

      SqlSelect resourceQuery = new SqlSelect();
      resourceQuery.addFields(table, COL_TCD_GRAPHICS_RESOURCE_ID, COL_TCD_GRAPHICS_RESOURCE);
      resourceQuery.addFrom(table);
      resourceQuery.setWhere(SqlUtils.inList(table, COL_TCD_GRAPHICS_RESOURCE_ID,
          sources.get(source)));

      SimpleRowSet resourceData = qs.getData(resourceQuery);
      if (DataUtils.isEmpty(resourceData)) {
        logger.warning("resources not found", table, sources.get(source));

      } else {
        for (SimpleRow row : resourceData) {
          Long resource = row.getLong(COL_TCD_GRAPHICS_RESOURCE_ID);
          String text = row.getValue(COL_TCD_GRAPHICS_RESOURCE);

          String type = resourceTypes.get(resource);

          if (text != null && !BeeUtils.isEmpty(type) && resourceArticles.containsKey(resource)) {
            String picture = PICTURE_PREFIX + type.toLowerCase() + ";base64," + text;

            for (Long article : resourceArticles.get(resource)) {
              pictures.put(article, picture);
            }
          }
        }
      }
    }

    if (pictures.isEmpty()) {
      return ResponseObject.response(BeeConst.NULL);
    } else {
      return ResponseObject.response(pictures).setSize(pictures.size());
    }
  }

  private void logHistory(String service, String query, Long articeBrand, int count,
      long duration) {
    SqlInsert ins = new SqlInsert(TBL_HISTORY);
    ins.addConstant(COL_HISTORY_DATE, System.currentTimeMillis());
    ins.addConstant(COL_HISTORY_USER, usr.getCurrentUserId());

    ins.addConstant(COL_HISTORY_SERVICE, service);
    if (!BeeUtils.isEmpty(query)) {
      ins.addConstant(COL_HISTORY_QUERY, query);
    }
    if (articeBrand != null) {
      ins.addConstant(COL_HISTORY_ARTICLE_BRAND, articeBrand);
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
    if (BeeUtils.length(search) < MIN_SEARCH_QUERY_LENGTH) {
      return ResponseObject.error(search,
          usr.getLocalizableMesssages().minSearchQueryLength(MIN_SEARCH_QUERY_LENGTH));
    }

    SqlSelect articleIdQuery = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_TCD_ANALOGS, COL_TCD_ARTICLE)
        .addFrom(TBL_TCD_ANALOGS)
        .setWhere(SqlUtils.and(SqlUtils.equals(TBL_TCD_ANALOGS, COL_TCD_SEARCH_NR, search),
            clause));

    List<EcItem> items = getItems(articleIdQuery, null);
    if (items.isEmpty()) {
      return didNotMatch(code);
    } else {
      return ResponseObject.response(items).setSize(items.size());
    }
  }

  private ResponseObject searchByOeNumber(String code) {
    if (BeeUtils.isEmpty(code)) {
      return ResponseObject.parameterNotFound(SVC_SEARCH_BY_OE_NUMBER, VAR_QUERY);
    }
    return searchByItemCode(code, oeNumberCondition);
  }

  private void setListPrice(List<EcItem> items) {
    long start = System.currentTimeMillis();

    SqlSelect defQuery = new SqlSelect();
    defQuery.addFrom(TBL_CONFIGURATION);
    defQuery.addFields(TBL_CONFIGURATION, COL_CONFIG_MARGIN_DEFAULT_PERCENT);

    Double defMargin = qs.getDouble(defQuery);

    Map<Long, Long> catParents = Maps.newHashMap();
    Map<Long, Long> catRoots = Maps.newHashMap();
    Map<Long, Double> catMargins = Maps.newHashMap();

    String colCategoryId = sys.getIdName(TBL_TCD_CATEGORIES);

    SqlSelect catQuery = new SqlSelect();
    catQuery.addFrom(TBL_TCD_CATEGORIES);
    catQuery.addFields(TBL_TCD_CATEGORIES, colCategoryId, COL_TCD_CATEGORY_PARENT,
        COL_TCD_CATEGORY_MARGIN_PERCENT);

    SimpleRowSet catData = qs.getData(catQuery);
    if (!DataUtils.isEmpty(catData)) {
      for (SimpleRow row : catData) {
        Long id = row.getLong(colCategoryId);

        Long parent = row.getLong(COL_TCD_CATEGORY_PARENT);
        if (parent == null) {
          catRoots.put(id, id);
        } else {
          catParents.put(id, parent);
        }

        Double percent = row.getDouble(COL_TCD_CATEGORY_MARGIN_PERCENT);
        if (percent != null) {
          catMargins.put(id, percent);
        }
      }

      ImmutableSet<Long> roots = ImmutableSet.copyOf(catRoots.values());
      ImmutableSet<Long> categories = ImmutableSet.copyOf(catParents.keySet());

      for (Long category : categories) {
        Long parent = catParents.get(category);

        while (!roots.contains(parent)) {
          parent = catParents.get(parent);
        }

        catRoots.put(category, parent);
      }
    }

    for (EcItem item : items) {
      Double margin = getMarginPercent(item.getCategoryList(), catParents, catRoots, catMargins);

      double cost = item.getRealPrice();
      Double listPrice;

      if (margin != null) {
        listPrice = BeeUtils.plusPercent(cost, margin);
      } else if (defMargin == null) {
        listPrice = cost;
      } else {
        listPrice = BeeUtils.plusPercent(cost, defMargin);
      }

      item.setListPrice(listPrice);
    }

    logger.debug("list price", items.size(), catMargins.size(), TimeUtils.elapsedMillis(start));
  }

  private void setPrice(List<EcItem> items) {
    long start = System.currentTimeMillis();
    EcClientDiscounts clientDiscounts = getClientDiscounts();

    long watch = System.currentTimeMillis();

    if (clientDiscounts == null || clientDiscounts.isEmpry()) {
      for (EcItem item : items) {
        item.setPrice(item.getListPrice());
      }

    } else {
      Map<Long, Long> categoryParents;
      if (clientDiscounts.hasCategories()) {
        categoryParents = getCategoryParents();
      } else {
        categoryParents = Maps.newHashMap();
      }

      for (EcItem item : items) {
        clientDiscounts.applyTo(item, categoryParents);
      }
    }

    long end = System.currentTimeMillis();
    logger.debug("price", watch - start, "+", end - watch, "=", end - start);
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

    String colClientId = sys.getIdName(TBL_CLIENTS);
    SimpleRow clientInfo = getCurrentClientInfo(colClientId, COL_CLIENT_MANAGER);
    if (clientInfo == null) {
      String message = BeeUtils.joinWords(SVC_SUBMIT_ORDER, "client not found for user",
          usr.getCurrentUserId());
      logger.severe(message);
      return ResponseObject.error(message);
    }

    SqlInsert insOrder = new SqlInsert(VIEW_ORDERS);

    insOrder.addConstant(COL_ORDER_DATE, TimeUtils.nowMinutes().getTime());
    insOrder.addConstant(COL_ORDER_STATUS, EcOrderStatus.NEW.ordinal());

    insOrder.addConstant(COL_ORDER_CLIENT, clientInfo.getLong(colClientId));
    Long manager = clientInfo.getLong(COL_CLIENT_MANAGER);
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
      insItem.addConstant(COL_ORDER_ITEM_ARTICLE_BRAND, cartItem.getEcItem().getArticleBrandId());

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
