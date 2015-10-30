package com.butent.bee.server.modules.documents;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.butent.bee.shared.modules.documents.DocumentConstants.*;

import com.butent.bee.server.data.BeeTable;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.DataEvent;
import com.butent.bee.server.data.DataEvent.ViewInsertEvent;
import com.butent.bee.server.data.DataEvent.ViewQueryEvent;
import com.butent.bee.server.data.DataEventHandler;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.ExtensionIcons;
import com.butent.bee.server.modules.administration.FileStorageBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.IsFrom;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.RegulatedWidget;
import com.butent.bee.shared.rights.RightsState;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class DocumentsModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(DocumentsModuleBean.class);

  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @EJB
  DataEditorBean deb;
  @EJB
  FileStorageBean fs;
  @EJB
  ParamHolderBean prm;

  @Override
  public List<SearchResult> doSearch(String query) {
    List<SearchResult> docsSr = qs.getSearchResults(VIEW_DOCUMENTS,
        Filter.anyContains(Sets.newHashSet(COL_DOCUMENT_NUMBER, COL_REGISTRATION_NUMBER,
            COL_DOCUMENT_NAME, ALS_CATEGORY_NAME, ALS_TYPE_NAME,
            ALS_PLACE_NAME, ALS_STATUS_NAME, ALS_DOCUMENT_COMPANY_NAME), query));

    return docsSr;
  }

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    ResponseObject response = null;

    if (BeeUtils.same(svc, SVC_COPY_DOCUMENT_DATA)) {
      response = copyDocumentData(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_DOCUMENT_DATA)));

    } else if (BeeUtils.same(svc, SVC_CREATE_PDF_DOCUMENT)) {
      response = createPdf(reqInfo.getParameter(COL_DOCUMENT_CONTENT));

    } else if (BeeUtils.same(svc, SVC_SET_CATEGORY_STATE)) {
      response = setCategoryState(BeeUtils.toLongOrNull(reqInfo.getParameter("id")),
          BeeUtils.toLongOrNull(reqInfo.getParameter(AdministrationConstants.COL_ROLE)),
          EnumUtils.getEnumByIndex(RightsState.class,
              reqInfo.getParameter(AdministrationConstants.COL_STATE)),
          Codec.unpack(reqInfo.getParameter("on")));

    } else {
      String msg = BeeUtils.joinWords(getModule().getName(), "service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    String module = getModule().getName();

    return Arrays.asList(BeeParameter.createBoolean(module, PRM_PRINT_AS_PDF, true, null),
        BeeParameter.createRelation(module, PRM_PRINT_HEADER, true, TBL_EDITOR_TEMPLATES,
            COL_EDITOR_TEMPLATE_NAME),
        BeeParameter.createRelation(module, PRM_PRINT_FOOTER, true, TBL_EDITOR_TEMPLATES,
            COL_EDITOR_TEMPLATE_NAME),
        BeeParameter.createText(module, PRM_PRINT_MARGINS, true, null),
        BeeParameter.createText(module, PRM_DOCUMENT_RECEIVED_PREFIX, false, null),
        BeeParameter.createText(module, PRM_DOCUMENT_SENT_PREFIX, false, null));
  }

  @Override
  public Module getModule() {
    return Module.DOCUMENTS;
  }

  @Override
  public String getResourcePath() {
    return getModule().getName();
  }

  @Override
  public void init() {
    sys.registerDataEventHandler(new DataEventHandler() {
      @Subscribe
      @AllowConcurrentEvents
      public void applyDocumentRights(ViewQueryEvent event) {
        if (event.isTarget(TBL_DOCUMENTS, VIEW_RELATED_DOCUMENTS) && !usr.isAdministrator()) {
          if (event.isBefore()) {
            SqlSelect query = event.getQuery();
            String tableAlias = null;

            for (IsFrom from : query.getFrom()) {
              if (from.getSource() instanceof String
                  && BeeUtils.same((String) from.getSource(), TBL_DOCUMENT_TREE)) {
                tableAlias = BeeUtils.notEmpty(from.getAlias(), TBL_DOCUMENT_TREE);
                break;
              }
            }
            if (!BeeUtils.isEmpty(tableAlias)) {
              sys.filterVisibleState(query, TBL_DOCUMENT_TREE, tableAlias);
            }
          } else {
            BeeRowSet rs = event.getRowset();
            int categoryIdx = rs.getColumnIndex(COL_DOCUMENT_CATEGORY);
            List<Long> categories = new ArrayList<>();

            if (BeeUtils.isNonNegative(categoryIdx)) {
              for (Long category : rs.getDistinctLongs(categoryIdx)) {
                categories.add(category);
              }
            }
            if (!BeeUtils.isEmpty(categories)) {
              BeeRowSet catRs = qs.getViewData(TBL_DOCUMENT_TREE, Filter.idIn(categories), null,
                  Lists.newArrayList(COL_DOCUMENT_CATEGORY + COL_CATEGORY_NAME));

              for (BeeRow row : rs) {
                IsRow catRow = catRs.getRowById(row.getLong(categoryIdx));
                row.setEditable(catRow.isEditable());
                row.setRemovable(catRow.isRemovable());
              }
            }
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void fillDocumentNumber(ViewInsertEvent event) {
        if (event.isBefore(TBL_DOCUMENTS)) {
          List<BeeColumn> cols = event.getColumns();

          if (DataUtils.contains(cols, COL_DOCUMENT_NUMBER)
              || !DataUtils.contains(cols, COL_DOCUMENT_CATEGORY)) {
            return;
          }
          IsRow row = event.getRow();
          HasConditions or = SqlUtils.or(SqlUtils.isNull(TBL_TREE_PREFIXES, COL_DOCUMENT_TYPE));

          SqlSelect query = new SqlSelect()
              .addFields(TBL_TREE_PREFIXES, COL_NUMBER_PREFIX)
              .addFrom(TBL_TREE_PREFIXES)
              .setWhere(SqlUtils.and(SqlUtils.equals(TBL_TREE_PREFIXES, COL_DOCUMENT_CATEGORY,
                  row.getLong(DataUtils.getColumnIndex(COL_DOCUMENT_CATEGORY, cols))), or))
              .addOrder(TBL_TREE_PREFIXES, COL_DOCUMENT_TYPE);

          Long type = null;
          int typeIdx = DataUtils.getColumnIndex(COL_DOCUMENT_TYPE, cols);

          if (!BeeConst.isUndef(typeIdx)) {
            type = row.getLong(typeIdx);

            if (DataUtils.isId(type)) {
              or.add(SqlUtils.equals(TBL_TREE_PREFIXES, COL_DOCUMENT_TYPE, type));
            }
          }
          SimpleRowSet rs = qs.getData(query);
          String prefix = DataUtils.isEmpty(rs) ? null : rs.getValue(0, COL_NUMBER_PREFIX);

          if (!BeeUtils.isEmpty(prefix)) {
            JustDate date = TimeUtils.today();
            cols.add(new BeeColumn(COL_DOCUMENT_NUMBER));
            row.addValue(Value.getValue(qs.getNextNumber(event.getTargetName(),
                COL_DOCUMENT_NUMBER,
                prefix.replace("{year}", TimeUtils.yearToString(date.getYear()))
                    .replace("{month}", TimeUtils.monthToString(date.getMonth()))
                    .replace("{day}", TimeUtils.dayOfMonthToString(date.getDom())), null)));
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void fillDocumentSentNumber(ViewInsertEvent event) {
        if (BeeUtils.same(event.getTargetName(), TBL_DOCUMENTS) && event.isBefore()) {

          List<BeeColumn> cols = event.getColumns();

          if (DataUtils.contains(cols, COL_DOCUMENT_SENT_NUMBER)
              && !DataUtils.contains(cols, COL_DOCUMENT_SENT)) {
            return;
          }

          Boolean sent = null;

          for (int i = 0; i < event.getColumns().size(); i++) {
            switch (event.getColumns().get(i).getId()) {
              case COL_DOCUMENT_SENT:
                sent = true;
                break;
            }
          }

          if (sent == null) {
            return;
          }

          String prefix = prm.getText(PRM_DOCUMENT_SENT_PREFIX);
          prefix = BeeUtils.isEmpty(prefix) ? BeeConst.STRING_EMPTY : prefix;

          BeeColumn column = sys.getView(VIEW_DOCUMENTS).getBeeColumn(COL_DOCUMENT_SENT_NUMBER);
          String number = getNextRegNumber(COL_DOCUMENT_SENT,
              column.getPrecision(), COL_DOCUMENT_SENT_NUMBER, prefix);

          if (!BeeUtils.isEmpty(number)) {
            event.addValue(column, new TextValue(prefix + number));
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void updateDocumentSentNumber(DataEvent.ViewUpdateEvent event) {
        if (BeeUtils.same(event.getTargetName(), TBL_DOCUMENTS) && event.isBefore()) {

          List<BeeColumn> cols = event.getColumns();

          if (DataUtils.contains(cols, COL_DOCUMENT_SENT_NUMBER)
              && !DataUtils.contains(cols, COL_DOCUMENT_SENT)) {
            return;
          }

          Boolean sent = null;
          String value = null;

          for (int i = 0; i < event.getColumns().size(); i++) {
            switch (event.getColumns().get(i).getId()) {
              case COL_DOCUMENT_SENT:
                sent = true;
                value = event.getRow().getString(i);
                break;
            }
          }

          if (sent == null || BeeUtils.isEmpty(value)) {
            return;
          }

          String prefix = prm.getText(PRM_DOCUMENT_SENT_PREFIX);
          prefix = BeeUtils.isEmpty(prefix) ? BeeConst.STRING_EMPTY : prefix;

          BeeColumn column = sys.getView(VIEW_DOCUMENTS).getBeeColumn(COL_DOCUMENT_SENT_NUMBER);
          String number = getNextRegNumber(COL_DOCUMENT_SENT, column.getPrecision(),
              COL_DOCUMENT_SENT_NUMBER, prefix);

          if (!BeeUtils.isEmpty(number)) {
            event.getColumns().add(column);
            event.getRow().addValue(new TextValue(prefix + number));
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void fillDocumentReceivedNumber(ViewInsertEvent event) {
        if (BeeUtils.same(event.getTargetName(), TBL_DOCUMENTS) && event.isBefore()) {

          List<BeeColumn> cols = event.getColumns();

          if (DataUtils.contains(cols, COL_DOCUMENT_RECEIVED_NUMBER)
              && !DataUtils.contains(cols, COL_DOCUMENT_RECEIVED)) {
            return;
          }

          Boolean received = null;

          for (int i = 0; i < event.getColumns().size(); i++) {
            switch (event.getColumns().get(i).getId()) {
              case COL_DOCUMENT_RECEIVED:
                received = true;
                break;
            }
          }

          if (received == null) {
            return;
          }

          String prefix = prm.getText(PRM_DOCUMENT_RECEIVED_PREFIX);
          prefix = BeeUtils.isEmpty(prefix) ? BeeConst.STRING_EMPTY : prefix;

          BeeColumn column = sys.getView(VIEW_DOCUMENTS).getBeeColumn(COL_DOCUMENT_RECEIVED_NUMBER);
          String number = getNextRegNumber(COL_DOCUMENT_RECEIVED,
              column.getPrecision(), COL_DOCUMENT_RECEIVED_NUMBER, prefix);

          if (!BeeUtils.isEmpty(number)) {
            event.addValue(column, new TextValue(prefix + number));
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void updateDocumentReceivedNumber(DataEvent.ViewUpdateEvent event) {
        if (BeeUtils.same(event.getTargetName(), TBL_DOCUMENTS) && event.isBefore()) {

          List<BeeColumn> cols = event.getColumns();

          if (DataUtils.contains(cols, COL_DOCUMENT_RECEIVED_NUMBER)
              && !DataUtils.contains(cols, COL_DOCUMENT_RECEIVED)) {
            return;
          }

          Boolean received = null;
          String value = null;

          for (int i = 0; i < event.getColumns().size(); i++) {
            switch (event.getColumns().get(i).getId()) {
              case COL_DOCUMENT_RECEIVED:
                received = true;
                value = event.getRow().getString(i);
                break;
            }
          }

          if (received == null || BeeUtils.isEmpty(value)) {
            return;
          }

          String prefix = prm.getText(PRM_DOCUMENT_RECEIVED_PREFIX);
          prefix = BeeUtils.isEmpty(prefix) ? BeeConst.STRING_EMPTY : prefix;

          BeeColumn column = sys.getView(VIEW_DOCUMENTS).getBeeColumn(COL_DOCUMENT_RECEIVED_NUMBER);
          String number = getNextRegNumber(COL_DOCUMENT_RECEIVED, column.getPrecision(),
              COL_DOCUMENT_RECEIVED_NUMBER, prefix);

          if (!BeeUtils.isEmpty(number)) {
            event.getColumns().add(column);
            event.getRow().addValue(new TextValue(prefix + number));
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void setRowProperties(ViewQueryEvent event) {
        if (event.isAfter(VIEW_DOCUMENT_FILES)) {
          ExtensionIcons.setIcons(event.getRowset(), AdministrationConstants.ALS_FILE_NAME,
              AdministrationConstants.PROP_ICON);

        } else if (event.isAfter(VIEW_DOCUMENT_TEMPLATES)) {
          Map<Long, IsRow> indexedRows = new HashMap<>();
          BeeRowSet rowSet = event.getRowset();
          int idx = rowSet.getColumnIndex(COL_DOCUMENT_DATA);

          for (BeeRow row : rowSet.getRows()) {
            Long id = row.getLong(idx);

            if (DataUtils.isId(id)) {
              indexedRows.put(id, row);
            }
          }
          if (!indexedRows.isEmpty()) {
            BeeView view = sys.getView(VIEW_DATA_CRITERIA);
            SqlSelect query = view.getQuery(usr.getCurrentUserId());

            query.setWhere(SqlUtils.and(query.getWhere(),
                SqlUtils.isNull(view.getSourceAlias(), COL_CRITERIA_GROUP_NAME),
                SqlUtils.inList(view.getSourceAlias(), COL_DOCUMENT_DATA, indexedRows.keySet())));

            for (SimpleRow row : qs.getData(query)) {
              IsRow r = indexedRows.get(row.getLong(COL_DOCUMENT_DATA));

              if (r != null) {
                r.setProperty(COL_CRITERION_NAME + row.getValue(COL_CRITERION_NAME),
                    row.getValue(COL_CRITERION_VALUE));
              }
            }
          }
        }
      }

      @Subscribe
      @AllowConcurrentEvents
      public void setRightsProperties(ViewQueryEvent event) {
        if (event.isAfter(TBL_DOCUMENT_TREE)
            && usr.isWidgetVisible(RegulatedWidget.DOCUMENT_TREE)) {

          String tableName = event.getTargetName();
          String idName = sys.getIdName(tableName);

          SqlSelect query = event.getQuery().resetFields().addFields(tableName, idName);

          BeeTable table = sys.getTable(tableName);
          Map<RightsState, String> states = new LinkedHashMap<>();
          boolean stateExists = false;
          Map<String, Long> roles = new TreeMap<>();
          roles.put("", 0L);

          for (Long role : usr.getRoles()) {
            roles.put(usr.getRoleName(role), role);
          }
          for (RightsState state : table.getStates()) {
            states.put(state, table.joinState(query, tableName, state));

            if (!BeeUtils.isEmpty(states.get(state))) {
              for (Long role : roles.values()) {
                IsExpression xpr = SqlUtils.sqlIf(table.checkState(states.get(state), state, role),
                    true, false);

                if (!BeeUtils.isEmpty(query.getGroupBy())) {
                  query.addMax(xpr, state.name() + role);
                } else {
                  query.addExpr(xpr, state.name() + role);
                }
              }
              stateExists = true;
            }
          }
          SimpleRowSet rs = null;

          if (stateExists) {
            rs = qs.getData(query);
          }
          for (BeeRow row : event.getRowset()) {
            for (RightsState state : states.keySet()) {
              for (Long role : roles.values()) {
                String value;

                if (!BeeUtils.isEmpty(states.get(state))) {
                  value = rs.getValueByKey(idName, BeeUtils.toString(row.getId()),
                      state.name() + role);
                } else {
                  value = Codec.pack(state.isChecked());
                }
                row.setProperty(BeeUtils.join("_", role, state.ordinal()), value);
              }
            }
          }
          event.getRowset().setTableProperty(AdministrationConstants.TBL_ROLES,
              Codec.beeSerialize(roles));
          event.getRowset().setTableProperty(AdministrationConstants.TBL_RIGHTS,
              Codec.beeSerialize(states.keySet()));
        }
      }
    });
  }

  private ResponseObject createPdf(String content, String... styleSheets) {
    if (!BeeUtils.unbox(prm.getBoolean(PRM_PRINT_AS_PDF))) {
      return ResponseObject.emptyResponse();
    }
    return ResponseObject.response(fs.createPdf(content, styleSheets));
  }

  private ResponseObject copyDocumentData(Long data) {
    Assert.state(DataUtils.isId(data));

    Long dataId = qs.insertData(new SqlInsert(TBL_DOCUMENT_DATA)
        .addConstant(COL_DOCUMENT_CONTENT, qs.getValue(new SqlSelect()
            .addFields(TBL_DOCUMENT_DATA, COL_DOCUMENT_CONTENT)
            .addFrom(TBL_DOCUMENT_DATA)
            .setWhere(sys.idEquals(TBL_DOCUMENT_DATA, data)))));

    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addField(TBL_CRITERIA_GROUPS, sys.getIdName(TBL_CRITERIA_GROUPS), COL_CRITERIA_GROUP)
        .addField(TBL_CRITERIA_GROUPS, COL_CRITERIA_ORDINAL,
            COL_CRITERIA_GROUP + COL_CRITERIA_ORDINAL)
        .addFields(TBL_CRITERIA_GROUPS, COL_CRITERIA_GROUP_NAME)
        .addFields(TBL_CRITERIA, COL_CRITERIA_ORDINAL, COL_CRITERION_NAME, COL_CRITERION_VALUE)
        .addFrom(TBL_CRITERIA_GROUPS)
        .addFromLeft(TBL_CRITERIA,
            sys.joinTables(TBL_CRITERIA_GROUPS, TBL_CRITERIA, COL_CRITERIA_GROUP))
        .setWhere(SqlUtils.equals(TBL_CRITERIA_GROUPS, COL_DOCUMENT_DATA, data)));

    Map<Long, Long> groups = new HashMap<>();

    for (SimpleRow row : rs) {
      long groupId = row.getLong(COL_CRITERIA_GROUP);
      String criterion = row.getValue(COL_CRITERION_NAME);

      if (!groups.containsKey(groupId)) {
        groups.put(groupId, qs.insertData(new SqlInsert(TBL_CRITERIA_GROUPS)
            .addConstant(COL_DOCUMENT_DATA, dataId)
            .addConstant(COL_CRITERIA_ORDINAL,
                row.getValue(COL_CRITERIA_GROUP + COL_CRITERIA_ORDINAL))
            .addConstant(COL_CRITERIA_GROUP_NAME, row.getValue(COL_CRITERIA_GROUP_NAME))));
      }
      if (!BeeUtils.isEmpty(criterion)) {
        qs.insertData(new SqlInsert(TBL_CRITERIA)
            .addConstant(COL_CRITERIA_GROUP, groups.get(groupId))
            .addConstant(COL_CRITERIA_ORDINAL, row.getValue(COL_CRITERIA_ORDINAL))
            .addConstant(COL_CRITERION_NAME, criterion)
            .addConstant(COL_CRITERION_VALUE, row.getValue(COL_CRITERION_VALUE)));
      }
    }
    return ResponseObject.response(dataId);
  }

  private String getNextRegNumber(String columnFilter,
      int maxLength, String column, String prefix) {
    IsCondition where = SqlUtils.notNull(TBL_DOCUMENTS, columnFilter);

    if (!BeeUtils.isEmpty(prefix)) {
      SqlUtils.and(where, SqlUtils.startsWith(TBL_DOCUMENTS, column, prefix));
    }

    SqlSelect query = new SqlSelect()
        .addFields(TBL_DOCUMENTS, column)
        .addFrom(TBL_DOCUMENTS)
        .setWhere(where);

    String[] values = qs.getColumn(query);

    long max = 0;
    BigInteger bigMax = null;
    int paternSize = 0;

    if (!ArrayUtils.isEmpty(values)) {
      for (String value : values) {
        if (!BeeUtils.isEmpty(prefix)) {
          value = BeeUtils.getSuffix(value, prefix);
        }
        if (BeeUtils.isDigit(value)) {
          if (BeeUtils.isLong(value)) {
            long oldMax = max;
            max = Math.max(max, BeeUtils.toLong(value));
            if (max != oldMax) {
              paternSize = Math.max(paternSize, value.length());
            }
          } else {
            BigInteger big = new BigInteger(value);

            if (bigMax == null || BeeUtils.isLess(bigMax, big)) {
              bigMax = big;
            }
          }
        }
      }
    }

    BigInteger big = new BigInteger(BeeUtils.toString(max));
    if (bigMax != null) {
      big = big.max(bigMax);
    }

    String number = big.add(BigInteger.ONE).toString();
    number = BeeUtils.padLeft(number, paternSize, BeeConst.CHAR_ZERO);

    if (maxLength > 0 && number.length() > maxLength) {
      number = number.substring(number.length() - maxLength);
    }

    return number;
  }

  private ResponseObject setCategoryState(Long id, Long roleId, RightsState state, boolean on) {
    Assert.noNulls(id, roleId, state);

    deb.setState(TBL_DOCUMENT_TREE, state, id, roleId, on);
    return ResponseObject.emptyResponse();
  }
}
