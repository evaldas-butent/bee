package com.butent.bee.server.modules.ec;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.ec.EcCarModel;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class EcModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(EcModuleBean.class);

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

    } else if (BeeUtils.same(svc, SVC_GET_ITEM_MANUFACTURERS)) {
      response = getItemManufacturers();

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
  }

  private ResponseObject doGlobalSearch(RequestInfo reqInfo) {
    String query = reqInfo.getParameter(VAR_QUERY);
    if (BeeUtils.isEmpty(query)) {
      return ResponseObject.parameterNotFound(SVC_GLOBAL_SEARCH, VAR_QUERY);
    }

    int count = BeeUtils.randomInt(0, Math.max((10 - query.length()) * 3, 2));
    if (count > 0) {
      return ResponseObject.response(generateItems(count));
    } else {
      return ResponseObject.warning(usr.getLocalizableMesssages().ecSearchDidNotMatch(query));
    }
  }

  private List<EcItem> generateItems(int count) {
    List<EcItem> items = Lists.newArrayList();
    for (int i = 0; i < count; i++) {
      items.add(new EcItem(0));
    }
    return items;
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
      return ResponseObject.parameterNotFound(VAR_MANUFACTURER);
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
      return ResponseObject.parameterNotFound(VAR_MODEL);
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

  private ResponseObject getFeaturedAndNoveltyItems() {
    return ResponseObject.response(generateItems(BeeUtils.randomInt(1, 30)));
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

  private ResponseObject searchByItemCode(RequestInfo reqInfo) {
    String code = reqInfo.getParameter(VAR_QUERY);
    if (BeeUtils.isEmpty(code)) {
      return ResponseObject.error("No search criteria defined");
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
      EcItem item = new EcItem(row.getLong("ArticleID"));
      item.setName(row.getValue("ArticleName"));
      item.setCode(row.getValue("ArticleNr"));
      item.setSupplier(BeeUtils.notEmpty(row.getValue("AnalogSupplier"), row.getValue("Supplier")));
      item.setStock(row.getInt("Remainder"));

      Double price = row.getDouble("Price");
      if (BeeUtils.isPositive(price)) {
        item.setListPrice(price * (1 + Math.random() * 0.5));
        item.setPrice(price);
      }

      items.add(item);
    }
    return ResponseObject.response(items);
  }

  private ResponseObject searchByOeNumber(RequestInfo reqInfo) {
    return doGlobalSearch(reqInfo);
  }
}
