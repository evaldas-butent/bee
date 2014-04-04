package com.butent.bee.server.modules.classifiers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.DataEvent.ViewDeleteEvent;
import com.butent.bee.server.data.DataEvent.ViewInsertEvent;
import com.butent.bee.server.data.DataEvent.ViewModifyEvent;
import com.butent.bee.server.data.DataEvent.ViewUpdateEvent;
import com.butent.bee.server.data.BeeTable;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.i18n.I18nUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.news.NewsBean;
import com.butent.bee.server.news.NewsHelper;
import com.butent.bee.server.news.UsageQueryProvider;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@Stateless
@LocalBean
public class ClassifiersModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(ClassifiersModuleBean.class);

  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @EJB
  DataEditorBean deb;
  @EJB
  NewsBean news;

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> search = Lists.newArrayList();

    if (usr.isModuleVisible(ModuleAndSub.of(getModule(), SubModule.CONTACTS))) {
      List<SearchResult> companiesSr = qs.getSearchResults(VIEW_COMPANIES,
          Filter.anyContains(Sets.newHashSet(COL_COMPANY_NAME, COL_COMPANY_CODE, COL_PHONE,
              COL_EMAIL_ADDRESS, COL_ADDRESS, ALS_CITY_NAME, ALS_COUNTRY_NAME), query));
      search.addAll(companiesSr);

      List<SearchResult> personsSr = qs.getSearchResults(VIEW_PERSONS,
          Filter.anyContains(Sets.newHashSet(COL_FIRST_NAME, COL_LAST_NAME, COL_PHONE,
              COL_EMAIL_ADDRESS, COL_ADDRESS, ALS_CITY_NAME, ALS_COUNTRY_NAME), query));
      search.addAll(personsSr);
    }

    if (usr.isModuleVisible(Module.TRADE.getName())) {
      List<SearchResult> itemsSr = qs.getSearchResults(VIEW_ITEMS,
          Filter.anyContains(Sets.newHashSet(COL_ITEM_NAME, COL_ITEM_ARTICLE, COL_ITEM_BARCODE),
              query));
      search.addAll(itemsSr);
    }
    return search;
  }

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {

    ResponseObject response = null;

    if (BeeUtils.same(svc, SVC_COMPANY_INFO)) {
      response = getCompanyInfo(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_COMPANY)),
          reqInfo.getParameter("locale"));

    } else if (BeeUtils.same(svc, SVC_CREATE_COMPANY)) {
      response = createCompany(reqInfo);

    } else if (BeeUtils.same(svc, SVC_GET_COMPANY_TYPE_REPORT)) {
      response = getCompanyTypeReport(reqInfo);

    } else {
      String msg = BeeUtils.joinWords("Commons service not recognized:", svc);
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
  public Module getModule() {
    return Module.CLASSIFIERS;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public void init() {
    sys.registerDataEventHandler(new DataEventHandler() {
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

    news.registerUsageQueryProvider(Feed.COMPANIES_MY, new UsageQueryProvider() {
      @Override
      public SqlSelect getQueryForAccess(Feed feed, String relationColumn, long userId,
          DateTime startDate) {

        String usageTable = NewsConstants.getUsageTable(TBL_COMPANIES);
        String urc = news.getUsageRelationColumn(TBL_COMPANIES);

        List<Pair<String, IsCondition>> joins = NewsHelper.buildJoin(TBL_COMPANY_USERS,
            SqlUtils.join(TBL_COMPANY_USERS, COL_COMPANY_USER_COMPANY, usageTable, urc));

        return NewsHelper.getAccessQuery(usageTable, urc, joins, getUserCompanyCondition(userId),
            userId);
      }

      @Override
      public SqlSelect getQueryForUpdates(Feed feed, String relationColumn, long userId,
          DateTime startDate) {

        String usageTable = feed.getUsageTable();

        List<Pair<String, IsCondition>> joins = NewsHelper.buildJoin(usageTable,
            sys.joinTables(TBL_COMPANY_USERS, usageTable, relationColumn));

        return NewsHelper.getUpdatesQuery(TBL_COMPANY_USERS, COL_COMPANY_USER_COMPANY, usageTable,
            joins, getUserCompanyCondition(userId), userId, startDate);
      }

      private List<IsCondition> getUserCompanyCondition(long userId) {
        return Lists.newArrayList(SqlUtils.equals(TBL_COMPANY_USERS, COL_COMPANY_USER_USER,
            userId));
      }
    });
  }

  private ResponseObject createCompany(RequestInfo reqInfo) {
    String companyName = reqInfo.getParameter(COL_COMPANY_NAME);
    if (BeeUtils.isEmpty(companyName)) {
      return ResponseObject.parameterNotFound(SVC_CREATE_COMPANY, COL_COMPANY_NAME);
    }

    Long company = qs.getId(TBL_COMPANIES, COL_COMPANY_NAME, companyName);
    if (company != null) {
      return ResponseObject.response(company);
    }

    String companyCode = reqInfo.getParameter(COL_COMPANY_CODE);
    if (!BeeUtils.isEmpty(companyCode)) {
      company = qs.getId(TBL_COMPANIES, COL_COMPANY_CODE, companyCode);
      if (company != null) {
        logger.warning(SVC_CREATE_COMPANY, COL_COMPANY_NAME, companyName, "not found",
            COL_COMPANY_CODE, companyCode, "found id", company);
        return ResponseObject.response(company);
      }
    }

    String vatCode = reqInfo.getParameter(COL_COMPANY_VAT_CODE);
    String exchangeCode = reqInfo.getParameter(COL_COMPANY_EXCHANGE_CODE);

    String email = reqInfo.getParameter(COL_EMAIL);
    if (!BeeUtils.isEmpty(email) && qs.sqlExists(TBL_EMAILS, COL_EMAIL_ADDRESS, email)) {
      logger.warning(usr.getLocalizableMesssages()
          .valueExists(BeeUtils.joinWords(usr.getLocalizableConstants().email(), email)),
          "ignored");
      email = null;
    }

    String address = reqInfo.getParameter(COL_ADDRESS);
    String cityName = reqInfo.getParameter(COL_CITY);
    String countryName = reqInfo.getParameter(COL_COUNTRY);

    String phone = reqInfo.getParameter(COL_PHONE);
    String mobile = reqInfo.getParameter(COL_MOBILE);
    String fax = reqInfo.getParameter(COL_FAX);

    List<BeeColumn> columns = sys.getView(VIEW_COMPANIES).getRowSetColumns();
    BeeRow row = DataUtils.createEmptyRow(columns.size());

    row.setValue(DataUtils.getColumnIndex(COL_COMPANY_NAME, columns), companyName);

    if (!BeeUtils.isEmpty(companyCode)) {
      row.setValue(DataUtils.getColumnIndex(COL_COMPANY_CODE, columns), companyCode);
    }
    if (!BeeUtils.isEmpty(vatCode)) {
      row.setValue(DataUtils.getColumnIndex(COL_COMPANY_VAT_CODE, columns), vatCode);
    }
    if (!BeeUtils.isEmpty(exchangeCode)) {
      row.setValue(DataUtils.getColumnIndex(COL_COMPANY_EXCHANGE_CODE, columns), exchangeCode);
    }

    ResponseObject response;

    if (!BeeUtils.isEmpty(email)) {
      row.setValue(DataUtils.getColumnIndex(ALS_EMAIL_ID, columns),
          qs.insertData(new SqlInsert(TBL_EMAILS)
              .addConstant(COL_EMAIL_ADDRESS, address)
              .addNotEmpty(COL_EMAIL_LABEL, companyName)));
    }
    Long country = null;

    if (!BeeUtils.isEmpty(countryName)) {
      country = qs.getId(TBL_COUNTRIES, COL_COUNTRY_NAME, countryName);

      if (!DataUtils.isId(country)) {
        country = qs.insertData(new SqlInsert(TBL_COUNTRIES)
            .addConstant(COL_COUNTRY_NAME, countryName));
      }
      row.setValue(DataUtils.getColumnIndex(COL_COUNTRY, columns), country);
    }

    if (!BeeUtils.isEmpty(cityName)) {
      Long city;

      if (DataUtils.isId(country)) {
        city = qs.getId(TBL_CITIES, COL_CITY_NAME, cityName, COL_COUNTRY, country);

        if (!DataUtils.isId(city)) {
          city = qs.insertData(new SqlInsert(TBL_CITIES)
              .addConstant(COL_CITY_NAME, cityName)
              .addConstant(COL_COUNTRY, country));
        }
      } else {
        city = qs.getId(TBL_CITIES, COL_CITY_NAME, cityName);
      }
      if (DataUtils.isId(city)) {
        row.setValue(DataUtils.getColumnIndex(COL_CITY, columns), city);
      }
    }

    if (!BeeUtils.isEmpty(address)) {
      row.setValue(DataUtils.getColumnIndex(COL_ADDRESS, columns), address);
    }

    if (!BeeUtils.isEmpty(phone)) {
      row.setValue(DataUtils.getColumnIndex(COL_PHONE, columns), phone);
    }
    if (!BeeUtils.isEmpty(mobile)) {
      row.setValue(DataUtils.getColumnIndex(COL_MOBILE, columns), mobile);
    }
    if (!BeeUtils.isEmpty(fax)) {
      row.setValue(DataUtils.getColumnIndex(COL_FAX, columns), fax);
    }

    BeeRowSet rowSet = DataUtils.createRowSetForInsert(VIEW_COMPANIES, columns, row);
    response = deb.commitRow(rowSet);
    if (response.hasErrors()) {
      return response;
    }

    company = ((BeeRow) response.getResponse()).getId();
    return ResponseObject.response(company);
  }

  private ResponseObject getCompanyInfo(Long companyId, String locale) {
    if (!DataUtils.isId(companyId)) {
      return ResponseObject.error("Wrong company ID");
    }
    SimpleRow row = qs.getRow(new SqlSelect()
        .addFields(TBL_COMPANIES, COL_COMPANY_NAME, COL_COMPANY_CODE, COL_COMPANY_VAT_CODE)
        .addFields(TBL_CONTACTS, COL_ADDRESS, COL_POST_INDEX, COL_PHONE, COL_MOBILE, COL_FAX)
        .addFields(TBL_EMAILS, COL_EMAIL_ADDRESS)
        .addField(TBL_CITIES, COL_CITY_NAME, COL_CITY)
        .addField(TBL_COUNTRIES, COL_COUNTRY_NAME, COL_COUNTRY)
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
    translations.put(COL_COMPANY_NAME, constants.company());
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
  
  private ResponseObject getCompanyTypeReport(RequestInfo reqInfo) {
    Long startDate = BeeUtils.toLongOrNull(reqInfo.getParameter(Service.VAR_FROM));
    Long endDate = BeeUtils.toLongOrNull(reqInfo.getParameter(Service.VAR_TO));

    Set<Long> filterTypes = DataUtils.parseIdSet(reqInfo.getParameter(COL_RELATION_TYPE));
    
    BeeTable table = sys.getTable(TBL_COMPANIES);
    if (!table.isAuditable()) {
      return ResponseObject.warning(TBL_COMPANIES, "is not auditable");
    }
    
    String auditSource = sys.getAuditSource(table.getName());
    
    SqlSelect auditQuery = new SqlSelect();
    auditQuery.addFields(auditSource, AUDIT_FLD_ID);
    auditQuery.addMin(auditSource, AUDIT_FLD_TIME);
    auditQuery.addFrom(auditSource);
    auditQuery.addGroup(auditSource, AUDIT_FLD_ID);
    
    if (startDate != null || endDate != null) {
      HasConditions auditWhere = SqlUtils.and();
      if (startDate != null) {
        auditWhere.add(SqlUtils.moreEqual(auditSource, AUDIT_FLD_TIME, startDate));
      }
      if (endDate != null) {
        auditWhere.add(SqlUtils.less(auditSource, AUDIT_FLD_TIME, endDate));
      }
      
      auditQuery.setWhere(auditWhere);
    }
    
    String auditAlias = SqlUtils.uniqueName();
    
    String idName = table.getIdName();
    
    SqlSelect companyQuery = new SqlSelect();
    companyQuery.addFields(TBL_COMPANIES, idName);
    companyQuery.addFields(auditAlias, AUDIT_FLD_TIME);
    companyQuery.addEmptyNumeric(BeeConst.YEAR, 4, 0);
    companyQuery.addEmptyNumeric(BeeConst.MONTH, 2, 0);

    companyQuery.addFrom(TBL_COMPANIES);
    companyQuery.addFromInner(auditQuery, auditAlias,
        SqlUtils.join(auditAlias, AUDIT_FLD_ID, TBL_COMPANIES, idName));
    
    if (!filterTypes.isEmpty()) {
      companyQuery.addFromInner(TBL_COMPANY_RELATION_TYPE_STORE,
          sys.joinTables(TBL_COMPANIES, TBL_COMPANY_RELATION_TYPE_STORE, COL_COMPANY));
      companyQuery.setWhere(SqlUtils.inList(TBL_COMPANY_RELATION_TYPE_STORE,
          COL_RELATION_TYPE, filterTypes));
    }
    
    String tmp = qs.sqlCreateTemp(companyQuery);
    
    long count = qs.setYearMonth(tmp, AUDIT_FLD_TIME, BeeConst.YEAR, BeeConst.MONTH);
    if (count <= 0) {
      qs.sqlDropTemp(tmp);
      return ResponseObject.emptyResponse();
    }
    
    String countName = "Cnt" + SqlUtils.uniqueName();
    
    SqlSelect query = new SqlSelect();
    query.addFields(tmp, BeeConst.YEAR, BeeConst.MONTH);
    query.addFields(TBL_COMPANY_RELATION_TYPE_STORE, COL_RELATION_TYPE);
    query.addFields(TBL_COMPANY_RELATION_TYPES, COL_RELATION_TYPE_NAME);
    query.addCount(countName);
    
    query.addFrom(tmp);
    query.addFromLeft(TBL_COMPANY_RELATION_TYPE_STORE,
        SqlUtils.join(TBL_COMPANY_RELATION_TYPE_STORE, COL_COMPANY, tmp, idName));
    query.addFromLeft(TBL_COMPANY_RELATION_TYPES,
        sys.joinTables(TBL_COMPANY_RELATION_TYPES, TBL_COMPANY_RELATION_TYPE_STORE,
            COL_RELATION_TYPE));

    query.addGroup(tmp, BeeConst.YEAR, BeeConst.MONTH);
    query.addGroup(TBL_COMPANY_RELATION_TYPE_STORE, COL_RELATION_TYPE);
    query.addGroup(TBL_COMPANY_RELATION_TYPES, COL_RELATION_TYPE_NAME);
    
    query.setWhere(SqlUtils.notNull(TBL_COMPANY_RELATION_TYPE_STORE, COL_RELATION_TYPE));
    
    SimpleRowSet result = qs.getData(query);
    
    if (filterTypes.isEmpty()) {
      query.setWhere(SqlUtils.isNull(TBL_COMPANY_RELATION_TYPE_STORE, COL_RELATION_TYPE));
      
      SimpleRowSet other = qs.getData(query);
      if (!DataUtils.isEmpty(other)) {
        result.append(other);
      }
    }
    
    qs.sqlDropTemp(tmp);

    if (DataUtils.isEmpty(result)) {
      return ResponseObject.emptyResponse();
    } else {
      return ResponseObject.response(result);
    }
  }
}
