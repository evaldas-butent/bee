package com.butent.bee.server.modules.commons;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.server.data.BeeTable;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.DataEvent.TableModifyEvent;
import com.butent.bee.server.data.DataEvent.ViewDeleteEvent;
import com.butent.bee.server.data.DataEvent.ViewInsertEvent;
import com.butent.bee.server.data.DataEvent.ViewModifyEvent;
import com.butent.bee.server.data.DataEvent.ViewUpdateEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.i18n.I18nUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeConst.SqlEngine;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import lt.lb.webservices.exchangerates.ExchangeRatesWS;

@Stateless
@LocalBean
public class CommonsModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(CommonsModuleBean.class);

  private static final Splitter ID_SPLITTER =
      Splitter.on(BeeConst.CHAR_COMMA).omitEmptyStrings().trimResults();

  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  DataEditorBean deb;
  @EJB
  QueryServiceBean qs;
  @EJB
  ParamHolderBean prm;
  @Resource
  EJBContext ctx;

  @Override
  public Collection<String> dependsOn() {
    return null;
  }

  @Override
  public List<SearchResult> doSearch(String query) {

    List<SearchResult> companiesSr =
        qs.getSearchResults(VIEW_COMPANIES,
            Filter.anyContains(Sets.newHashSet(COL_NAME, COL_COMPANY_CODE, COL_PHONE,
                COL_EMAIL_ADDRESS, COL_ADDRESS, ALS_CITY_NAME, ALS_COUNTRY_NAME), query));

    List<SearchResult> personsSr = qs.getSearchResults(VIEW_PERSONS,
        Filter.anyContains(Sets.newHashSet(COL_FIRST_NAME, COL_LAST_NAME, COL_PHONE,
            COL_EMAIL_ADDRESS, COL_ADDRESS, ALS_CITY_NAME, ALS_COUNTRY_NAME), query));

    List<SearchResult> usersSr = qs.getSearchResults(VIEW_USERS,
        Filter.anyContains(Sets.newHashSet(COL_LOGIN, COL_FIRST_NAME, COL_LAST_NAME), query));

    List<SearchResult> itemsSr = qs.getSearchResults(VIEW_ITEMS,
        Filter.anyContains(Sets.newHashSet(COL_NAME, COL_ITEM_ARTICLE, COL_ITEM_BARCODE), query));

    List<SearchResult> commonsSr = Lists.newArrayList();
    commonsSr.addAll(companiesSr);
    commonsSr.addAll(personsSr);
    commonsSr.addAll(usersSr);
    commonsSr.addAll(itemsSr);

    return commonsSr;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(COMMONS_METHOD);

    if (BeeUtils.isPrefix(svc, COMMONS_ITEM_PREFIX)) {
      response = doItemEvent(svc, reqInfo);

    } else if (BeeUtils.isPrefix(svc, COMMONS_PARAMETERS_PREFIX)) {
      response = doParameterEvent(svc, reqInfo);

    } else if (BeeUtils.same(svc, SVC_COMPANY_INFO)) {
      response = getCompanyInfo(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_COMPANY)),
          reqInfo.getParameter("locale"));

    } else if (BeeUtils.same(svc, SVC_GET_HISTORY)) {
      response = getHistory(reqInfo.getParameter(VAR_HISTORY_VIEW),
          DataUtils.parseIdSet(reqInfo.getParameter(VAR_HISTORY_IDS)));
    } else if (BeeUtils.same(svc, SVC_GET_LIST_OF_CURRENCIES)) {
      response = getListOfCurrencies();
    } else if (BeeUtils.same(svc, SVC_GET_CURRENT_EXCHANGE_RATE)) {
      response = getCurrentExchangeRate(reqInfo.getParameter(COL_CURRENCY_NAME));
    } else if (BeeUtils.same(svc, SVC_GET_EXCHANGE_RATE)) {
      response =
          getExchangeRate(reqInfo.getParameter(COL_CURRENCY_NAME), reqInfo
              .getParameter(COL_EXCHANGE_RATE_DATE));
    } else if (BeeUtils.same(svc, SVC_GET_EXCHANGE_RATES_BY_CURRENCIES)) {
        response =
          getExchangeRatesByCurrency(reqInfo.getParameter(COL_CURRENCY_NAME), reqInfo
              .getParameter(VAR_DATE_LOW), reqInfo.getParameter(VAR_DATE_HIGH));

    } else if (BeeUtils.same(svc, SVC_BLOCK_HOST)) {
      response = blockHost(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("Commons service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    List<BeeParameter> params = Lists.newArrayList(
        new BeeParameter(COMMONS_MODULE,
            "ProgramTitle", ParameterType.TEXT, null, false, "BEE"),
        new BeeParameter(COMMONS_MODULE, PRM_VAT_PERCENT, ParameterType.NUMBER,
            "Default VAT percent", false, 21),
        new BeeParameter(COMMONS_MODULE,
            PRM_AUDIT_OFF, ParameterType.BOOLEAN, "Disable database level auditing", false, false),
        new BeeParameter(COMMONS_MODULE, PRM_ERP_ADDRESS, ParameterType.TEXT,
            "Address of ERP system WebService", false, null),
        new BeeParameter(COMMONS_MODULE, PRM_ERP_LOGIN, ParameterType.TEXT,
            "Login name of ERP system WebService", false, null),
        new BeeParameter(COMMONS_MODULE, PRM_ERP_PASSWORD, ParameterType.TEXT,
            "Password of ERP system WebService", false, null),
        new BeeParameter(COMMONS_MODULE, "ERPOperation", ParameterType.TEXT,
            "Document operation name in ERP system", false, null),
        new BeeParameter(COMMONS_MODULE, "ERPWarehouse", ParameterType.TEXT,
            "Document warehouse name in ERP system", false, null));

    params.addAll(getSqlEngineParameters());
    return params;
  }

  @Override
  public String getName() {
    return COMMONS_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      public void refreshRightsCache(TableModifyEvent event) {
        if (usr.isRightsTable(event.getTargetName()) && event.isAfter()) {
          usr.initRights();
        }
      }

      @Subscribe
      public void refreshUsersCache(TableModifyEvent event) {
        if ((usr.isRoleTable(event.getTargetName()) || usr.isUserTable(event.getTargetName()))
            && event.isAfter()) {
          usr.initUsers();
        }
      }

      @Subscribe
      public void refreshIpFilterCache(TableModifyEvent event) {
        if (BeeUtils.same(event.getTargetName(), TBL_IP_FILTERS) && event.isAfter()) {
          sys.initIpFilters();
        }
      }

      @Subscribe
      public void storeEmail(ViewModifyEvent event) {
        if (BeeUtils.same(event.getTargetName(), TBL_EMAILS) && event.isBefore()
            && !(event instanceof ViewDeleteEvent)) {

          List<BeeColumn> cols;
          BeeRow row;

          if (event instanceof ViewInsertEvent) {
            cols = ((ViewInsertEvent) event).getColumns();
            row = ((ViewInsertEvent) event).getRow();
          } else {
            cols = ((ViewUpdateEvent) event).getColumns();
            row = ((ViewUpdateEvent) event).getRow();
          }
          int idx = DataUtils.getColumnIndex(COL_EMAIL_ADDRESS, cols);

          if (idx != BeeConst.UNDEF) {
            String email = BeeUtils.normalize(row.getString(idx));

            try {
              new InternetAddress(email, true).validate();
              row.setValue(idx, email);
            } catch (AddressException ex) {
              event.addErrorMessage(BeeUtils.joinWords("Wrong address:", ex.getMessage()));
            }
          }
        }
      }
    });
  }

  private ResponseObject blockHost(RequestInfo reqInfo) {
    String host = BeeUtils.trim(reqInfo.getParameter(COL_IP_FILTER_HOST));
    if (BeeUtils.isEmpty(host)) {
      return ResponseObject.parameterNotFound(SVC_BLOCK_HOST, COL_IP_FILTER_HOST);
    }

    if (qs.sqlExists(TBL_IP_FILTERS,
        SqlUtils.and(SqlUtils.equals(TBL_IP_FILTERS, COL_IP_FILTER_HOST, host),
            SqlUtils.isNull(TBL_IP_FILTERS, COL_IP_FILTER_BLOCK_AFTER),
            SqlUtils.isNull(TBL_IP_FILTERS, COL_IP_FILTER_BLOCK_BEFORE)))) {
      return ResponseObject.response(host);
    }

    SqlInsert insert = new SqlInsert(TBL_IP_FILTERS).addConstant(COL_IP_FILTER_HOST, host);
    ResponseObject response = qs.insertDataWithResponse(insert);

    if (response.hasErrors()) {
      return response;
    } else {
      return ResponseObject.response(host);
    }
  }

  private ResponseObject doItemEvent(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    if (BeeUtils.same(svc, SVC_ADD_CATEGORIES)) {
      int cnt = 0;
      long itemId = BeeUtils.toLong(reqInfo.getParameter(VAR_ITEM_ID));
      String categories = reqInfo.getParameter(VAR_ITEM_CATEGORIES);

      for (String catId : ID_SPLITTER.split(categories)) {
        response = qs.insertDataWithResponse(new SqlInsert(TBL_ITEM_CATEGORIES)
            .addConstant(COL_ITEM, itemId)
            .addConstant(COL_CATEGORY, BeeUtils.toLong(catId)));

        if (response.hasErrors()) {
          break;
        }
        cnt++;
      }
      if (!response.hasErrors()) {
        response = ResponseObject.response(cnt);
      }
    } else if (BeeUtils.same(svc, SVC_REMOVE_CATEGORIES)) {
      long itemId = BeeUtils.toLong(reqInfo.getParameter(VAR_ITEM_ID));
      String categories = reqInfo.getParameter(VAR_ITEM_CATEGORIES);

      String tbl = TBL_ITEM_CATEGORIES;
      HasConditions catClause = SqlUtils.or();
      IsCondition cond = SqlUtils.and(SqlUtils.equals(tbl, COL_ITEM, itemId),
          catClause);

      for (String catId : ID_SPLITTER.split(categories)) {
        catClause.add(SqlUtils.equals(tbl, COL_CATEGORY, BeeUtils.toLong(catId)));
      }
      response = qs.updateDataWithResponse(new SqlDelete(tbl).setWhere(cond));

    } else if (BeeUtils.same(svc, SVC_ITEM_CREATE)) {
      BeeRowSet rs = BeeRowSet.restore(reqInfo.getParameter(VAR_ITEM_DATA));
      String categories = reqInfo.getParameter(VAR_ITEM_CATEGORIES);
      response = deb.commitRow(rs, false);

      if (!response.hasErrors()) {
        long itemId = ((BeeRow) response.getResponse()).getId();

        if (!BeeUtils.isEmpty(categories)) {
          for (String catId : ID_SPLITTER.split(categories)) {
            response =
                qs.insertDataWithResponse(new SqlInsert(TBL_ITEM_CATEGORIES)
                    .addConstant(COL_ITEM, itemId)
                    .addConstant(COL_CATEGORY, BeeUtils.toLong(catId)));

            if (response.hasErrors()) {
              break;
            }
          }
        }
        if (!response.hasErrors()) {
          BeeView view = sys.getView(rs.getViewName());
          rs = qs.getViewData(view.getName(), ComparisonFilter.compareId(itemId));

          if (rs.isEmpty()) {
            String msg = "Optimistic lock exception";
            logger.warning(msg);
            response = ResponseObject.error(msg);
          } else {
            response.setResponse(rs.getRow(0));
          }
        }
      }
    }
    if (response == null) {
      String msg = BeeUtils.joinWords("Items service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);

    } else if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
  }

  private ResponseObject doParameterEvent(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    if (BeeUtils.same(svc, SVC_GET_PARAMETERS)) {
      List<BeeParameter> params = Lists.newArrayList();

      for (BeeParameter p : prm
          .getParameters(reqInfo.getParameter(VAR_PARAMETERS_MODULE)).values()) {
        BeeParameter param = new BeeParameter(p.getModule(), p.getName(), p.getType(),
            p.getDescription(), p.supportsUsers(), p.getValue());

        if (param.supportsUsers()) {
          param.setUserValue(usr.getCurrentUserId(), p.getUserValue(usr.getCurrentUserId()));
        }
        params.add(param);
      }
      response = ResponseObject.response(params);

    } else if (BeeUtils.same(svc, SVC_GET_PARAMETER)) {
      response = ResponseObject.response(prm.getValue(reqInfo.getParameter(VAR_PARAMETERS_MODULE),
          reqInfo.getParameter(VAR_PARAMETERS)));

    } else if (BeeUtils.same(svc, SVC_CREATE_PARAMETER)) {
      prm.createParameter(BeeParameter.restore(
          reqInfo.getParameter(VAR_PARAMETERS)));
      response = ResponseObject.response(true);

    } else if (BeeUtils.same(svc, SVC_SET_PARAMETER)) {
      prm.setParameter(reqInfo.getParameter(VAR_PARAMETERS_MODULE),
          reqInfo.getParameter(VAR_PARAMETERS),
          reqInfo.getParameter(VAR_PARAMETER_VALUE));
      response = ResponseObject.response(true);

    } else if (BeeUtils.same(svc, SVC_REMOVE_PARAMETERS)) {
      prm.removeParameters(reqInfo.getParameter(VAR_PARAMETERS_MODULE),
          Codec.beeDeserializeCollection(reqInfo.getParameter(VAR_PARAMETERS)));

      response = ResponseObject.response(true);
    }
    if (response == null) {
      String msg = BeeUtils.joinWords("Parameters service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);

    } else if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
  }

  private ResponseObject getCompanyInfo(Long companyId, String locale) {
    if (!DataUtils.isId(companyId)) {
      return ResponseObject.error("Wrong company ID");
    }
    SimpleRow row = qs.getRow(new SqlSelect()
        .addFields(TBL_COMPANIES, COL_NAME, COL_COMPANY_CODE, COL_COMPANY_VAT_CODE)
        .addFields(TBL_CONTACTS, COL_ADDRESS, COL_POST_INDEX, COL_PHONE, COL_MOBILE, COL_FAX)
        .addFields(TBL_EMAILS, COL_EMAIL_ADDRESS)
        .addField(TBL_CITIES, COL_NAME, COL_CITY)
        .addField(TBL_COUNTRIES, COL_NAME, COL_COUNTRY)
        .addFrom(TBL_COMPANIES)
        .addFromLeft(TBL_CONTACTS, sys.joinTables(TBL_CONTACTS, TBL_COMPANIES, COL_CONTACT))
        .addFromLeft(TBL_EMAILS, sys.joinTables(TBL_EMAILS, TBL_CONTACTS, COL_EMAIL))
        .addFromLeft(TBL_CITIES, sys.joinTables(TBL_CITIES, TBL_CONTACTS, COL_CITY))
        .addFromLeft(TBL_COUNTRIES, sys.joinTables(TBL_COUNTRIES, TBL_CONTACTS, COL_COUNTRY))
        .setWhere(sys.idEquals(TBL_COMPANIES, companyId)));

    Locale loc = I18nUtils.toLocale(locale);
    LocalizableConstants constants = (loc == null)
        ? Localized.getConstants() : Localizations.getConstants(loc);

    Map<String, String> translations = Maps.newHashMap();
    translations.put(COL_NAME, constants.company());
    translations.put(COL_COMPANY_CODE, constants.companyCode());
    translations.put(COL_COMPANY_VAT_CODE, constants.companyVATCode());
    translations.put(COL_ADDRESS, constants.address());
    translations.put(COL_POST_INDEX, constants.postIndex());
    translations.put(COL_PHONE, constants.phone());
    translations.put(COL_MOBILE, constants.mobile());
    translations.put(COL_FAX, constants.fax());
    translations.put(COL_EMAIL_ADDRESS, constants.address());
    translations.put(COL_CITY, constants.city());
    translations.put(COL_COUNTRY, constants.country());

    Map<String, Pair<String, String>> info = Maps.newHashMap();

    for (String col : translations.keySet()) {
      info.put(col, Pair.of(translations.get(col), row.getValue(col)));
    }
    return ResponseObject.response(info);
  }

  private ResponseObject getCurrentExchangeRate(String currency) {

    String remoteWSDL = prm.getText(COMMONS_MODULE, PRM_WS_LB_EXCHANGE_RATES_ADDRESS);
    ResponseObject response;
    if (BeeUtils.isEmpty(remoteWSDL)) {
      response = ExchangeRatesWS.getCurrentExchangeRate(currency);
      return ResponseObject.response(response.getResponse());
    }

    return ResponseObject.response(ExchangeRatesWS.getCurrentExchangeRate(remoteWSDL,
        currency)
        .getResponse());

  }

  private ResponseObject getExchangeRate(String currency, String date) {

    String remoteWSDL = prm.getText(COMMONS_MODULE, PRM_WS_LB_EXCHANGE_RATES_ADDRESS);
    ResponseObject response;
    if (BeeUtils.isEmpty(remoteWSDL)) {
      response = ExchangeRatesWS.getExchangeRate(currency, TimeUtils.parseDate(date));
      return ResponseObject.response(response.getResponse());
    }

    return ResponseObject.response(ExchangeRatesWS.getExchangeRate(remoteWSDL, currency,
        TimeUtils.parseDate(date))
        .getResponse());

  }

  private ResponseObject getExchangeRatesByCurrency(String currency, String dateLow,
      String dateHigh) {
    String remoteWSDL = prm.getText(COMMONS_MODULE, PRM_WS_LB_EXCHANGE_RATES_ADDRESS);
    ResponseObject response;

    if (BeeUtils.isEmpty(remoteWSDL)) {
      response =
          ExchangeRatesWS.getExchangeRatesByCurrency(currency, TimeUtils.parseDate(dateLow),
              TimeUtils.parseDate(dateHigh));
      return ResponseObject.response(response.getResponse());
    }

    return ResponseObject.response(ExchangeRatesWS.getExchangeRatesByCurrency(remoteWSDL, currency,
        TimeUtils.parseDate(dateLow),
        TimeUtils.parseDate(dateHigh)).getResponse());
  }

  private ResponseObject getHistory(String viewName, Collection<Long> idList) {
    LocalizableConstants loc = usr.getLocalizableConstants();

    if (BeeUtils.isEmpty(idList)) {
      return ResponseObject.error(loc.selectAtLeastOneRow());
    }
    BeeView view = sys.getView(viewName);

    SqlSelect query = view.getQuery(ComparisonFilter.idIn(idList), null)
        .resetFields().resetOrder();

    Multimap<String, ViewColumn> columnMap = HashMultimap.create();
    Map<String, Pair<String, String>> idMap = Maps.newHashMap();

    for (ViewColumn col : view.getViewColumns()) {
      if (!col.isHidden() && !col.isReadOnly()
          && (col.getLevel() == 0 || BeeUtils.unbox(col.getEditable()))) {

        String als = view.getColumnSource(col.getName());
        columnMap.put(als, col);

        if (!idMap.containsKey(als)) {
          String parent = col.getParent();

          if (!BeeUtils.isEmpty(parent)) {
            String src = view.getColumnSource(parent);
            String fld = view.getColumnField(parent);
            query.addField(src, fld, parent);

            if (!BeeUtils.isEmpty(query.getGroupBy())) {
              query.addGroup(src, fld);
            }
          } else {
            parent = view.getSourceIdName();
            query.addFields(view.getSourceAlias(), parent);
          }
          idMap.put(als, Pair.of(col.getTable(), parent));
        }
      }
    }
    SimpleRowSet ids = qs.getData(query);
    query = null;

    for (String als : columnMap.keySet()) {
      BeeTable table = sys.getTable(idMap.get(als).getA());

      Set<Long> auditIds = Sets.newHashSet(ids.getLongColumn(idMap.get(als).getB()));
      auditIds.remove(null);

      if (BeeUtils.isEmpty(auditIds) || !table.isAuditable()) {
        continue;
      }
      String src = sys.getAuditSource(table.getName());
      SqlSelect subq = new SqlSelect();

      List<String> fields = Lists.newArrayList();
      List<Object> pairs = Lists.newArrayList();

      for (ViewColumn col : columnMap.get(als)) {
        fields.add(col.getField());

        if (!BeeUtils.same(col.getField(), col.getName())) {
          pairs.add(col.getField());
          pairs.add(col.getName());
        }
      }
      if (!BeeUtils.isEmpty(pairs)) {
        pairs.add(SqlUtils.field(src, AUDIT_FLD_FIELD));

        subq.addExpr(SqlUtils.sqlCase(SqlUtils.field(src, AUDIT_FLD_FIELD),
            pairs.toArray()), AUDIT_FLD_FIELD);
      } else {
        subq.addFields(src, AUDIT_FLD_FIELD);
      }
      subq.addFields(src, AUDIT_FLD_TIME, AUDIT_FLD_TX, AUDIT_FLD_MODE, AUDIT_FLD_ID,
          AUDIT_FLD_VALUE)
          .addConstant(table.getName(), COL_OBJECT)
          .addField(TBL_USERS, COL_LOGIN, COL_USER)
          .addFrom(src)
          .addFromLeft(TBL_USERS, sys.joinTables(TBL_USERS, src, AUDIT_FLD_USER))
          .setWhere(SqlUtils.and(SqlUtils.inList(src, AUDIT_FLD_ID, auditIds),
              SqlUtils.or(SqlUtils.inList(src, AUDIT_FLD_FIELD, fields),
                  SqlUtils.isNull(src, AUDIT_FLD_FIELD))));

      if (query == null) {
        query = subq.addOrder(src, AUDIT_FLD_TIME, AUDIT_FLD_ID).addOrder(null, COL_OBJECT);
      } else {
        query.setUnionAllMode(true).addUnion(subq);
      }
    }
    BeeRowSet rs = new BeeRowSet(HISTORY_COLUMNS);

    if (query != null) {
      int fldIdx = rs.getColumnIndex(AUDIT_FLD_FIELD);
      int valIdx = rs.getColumnIndex(AUDIT_FLD_VALUE);
      int relIdx = rs.getColumnIndex(COL_RELATION);
      Map<String, String> dict = usr.getLocalizableDictionary();

      for (SimpleRow row : qs.getData(query)) {
        String[] values = new String[rs.getNumberOfColumns()];
        String fld = row.getValue(AUDIT_FLD_FIELD);

        for (int i = 0; i < values.length; i++) {
          String value;

          if (i == relIdx) {
            value = BeeUtils.isEmpty(fld) ? null : view.getColumnRelation(fld);
          } else {
            value = row.getValue(rs.getColumnId(i));
          }
          if (value != null) {
            if (i == fldIdx) {
              value = BeeUtils.notEmpty(Localized.maybeTranslate(view.getColumnLabel(fld), dict),
                  value);

            } else if (i == valIdx) {
              switch (view.getColumnType(fld)) {
                case BOOLEAN:
                  value = BeeUtils.toBoolean(value) ? loc.yes() : loc.no();
                  break;
                case DATE:
                  value = TimeUtils.toDateTimeOrNull(BeeUtils.toLong(value)).toDateString();
                  break;
                case DATETIME:
                  value = TimeUtils.toDateTimeOrNull(BeeUtils.toLong(value)).toCompactString();
                  break;
                case DECIMAL:
                  String enumKey = view.getColumnEnumKey(fld);

                  if (!BeeUtils.isEmpty(enumKey)) {
                    value = BeeUtils.notEmpty(EnumUtils.getLocalizedCaption(enumKey,
                        BeeUtils.toInt(value), loc), value);
                  }
                  break;
                default:
                  break;
              }
            }
          }
          values[i] = value;
        }
        rs.addRow(new BeeRow(0L, values));
      }
    }
    return ResponseObject.response(rs);
  }

  private static Collection<? extends BeeParameter> getSqlEngineParameters() {
    List<BeeParameter> params = Lists.newArrayList();

    for (SqlEngine engine : SqlEngine.values()) {
      BeeParameter param = new BeeParameter(COMMONS_MODULE,
          BeeUtils.join(BeeConst.STRING_EMPTY, PRM_SQL_MESSAGES, engine), ParameterType.MAP,
          BeeUtils.joinWords("Duomenų bazės", engine, "klaidų pranešimai"), false, null);

      switch (engine) {
        case GENERIC:
        case MSSQL:
        case ORACLE:
          break;
        case POSTGRESQL:
          param.setValue(ImmutableMap
              .of(".+duplicate key value violates unique constraint.+(\\(.+=.+\\)).+",
                  "Tokia reikšmė jau egzistuoja: $1",
                  ".+violates foreign key constraint.+from table \"(.+)\"\\.",
                  "Įrašas naudojamas lentelėje \"$1\""));
          break;
      }
      params.add(param);
    }
    return params;
  }

  private ResponseObject getListOfCurrencies() {

    String remoteWSDL = prm.getText(COMMONS_MODULE, PRM_WS_LB_EXCHANGE_RATES_ADDRESS);
    ResponseObject response;
    if (BeeUtils.isEmpty(remoteWSDL)) {
      response = ExchangeRatesWS.getListOfCurrencies();
      return ResponseObject.response(response.getResponse());
    }

    return ResponseObject.response(ExchangeRatesWS.getListOfCurrencies(remoteWSDL)
        .getResponse());

  }
}
