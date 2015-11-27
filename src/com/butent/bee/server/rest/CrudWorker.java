package com.butent.bee.server.rest;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
public abstract class CrudWorker {

  static final String LAST_SYNC_TIME = "LastSyncTime";

  static final String ID = "ID";
  static final String VERSION = "VERSION";

  private static final String SERVER_ERROR = "ServerError";

  private static final String CONTACT_VERSION = "ContactVersion";
  private static final String OLD_VALUES = "OLD_VALUES";

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  DataEditorBean deb;
  @EJB
  UserServiceBean usr;
  @Resource
  UserTransaction utx;

  @DELETE
  @Path("{" + ID + ":\\d+}/{" + VERSION + ":\\d+}")
  public Response delete(@PathParam(ID) Long id, @PathParam(VERSION) Long version) {
    if (!usr.canDeleteData(getViewName())) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    try {
      commit(new Runnable() {
        @Override
        public void run() {
          ResponseObject response = deb.deleteRows(getViewName(),
              new RowInfo[] {new RowInfo(id, version)});

          if (response.hasErrors()) {
            throw new BeeRuntimeException(ArrayUtils.joinWords(response.getErrors()));
          }
        }
      });
    } catch (BeeException e) {
      return serverError(e);
    }
    return Response.ok().build();
  }

  @GET
  @Path("{" + ID + ":\\d+}")
  public Response get(@PathParam(ID) Long id) {
    return get(Filter.compareId(id));
  }

  @GET
  public Response getAll(@HeaderParam(LAST_SYNC_TIME) Long lastSynced) {
    Filter filter = Filter.compareVersion(Operator.GT, lastSynced);

    if (sys.getView(getViewName()).hasColumn(CONTACT_VERSION)) {
      filter = Filter.or(filter, Filter.isMore(CONTACT_VERSION, Value.getValue(lastSynced)));
    }
    return get(filter);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response insert(JsonObject data) {
    Holder<Long> idHolder = Holder.of(BeeUtils.toLongOrNull(getValue(data, ID)));

    if (DataUtils.isId(idHolder.get())) {
      return update(idHolder.get(), BeeUtils.toLongOrNull(getValue(data, VERSION)), data);
    }
    if (!usr.canCreateData(getViewName())) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    try {
      commit(new Runnable() {
        @Override
        public void run() {
          idHolder.set(insert(getViewName(), data));
        }
      });
    } catch (BeeException e) {
      return serverError(e);
    }
    return Response.fromResponse(get(idHolder.get())).status(Response.Status.CREATED).build();
  }

  @PUT
  @Path("{" + ID + ":\\d+}/{" + VERSION + ":\\d+}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(@PathParam(ID) Long id, @PathParam(VERSION) Long version,
      JsonObject data) {

    if (!usr.canEditData(getViewName())) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    String error = null;

    try {
      commit(new Runnable() {
        @Override
        public void run() {
          update(getViewName(), id, version, data);
        }
      });
    } catch (BeeException e) {
      error = e.getLocalizedMessage();
    }
    Response response = get(id);

    if (!BeeUtils.isEmpty(error)) {
      response.getHeaders().add(SERVER_ERROR, error);
    }
    return response;
  }

  protected abstract String getViewName();

  static Response rowSetResponse(BeeRowSet rowSet) {
    List<Map<String, Object>> data = new ArrayList<>();

    for (int i = 0; i < rowSet.getNumberOfRows(); i++) {
      Map<String, Object> row = new LinkedHashMap<>(rowSet.getNumberOfColumns());
      BeeRow beeRow = rowSet.getRow(i);
      row.put(ID, beeRow.getId());
      row.put(VERSION, beeRow.getVersion());

      for (int j = 0; j < rowSet.getNumberOfColumns(); j++) {
        BeeColumn column = rowSet.getColumn(j);
        row.put(column.getId(),
            column.getType() == ValueType.LONG ? rowSet.getLong(i, j) : rowSet.getString(i, j));
      }
      data.add(row);
    }
    return Response.ok(data, MediaType.APPLICATION_JSON_TYPE.withCharset(BeeConst.CHARSET_UTF8))
        .build();
  }

  private void commit(Runnable executor) throws BeeException {
    try {
      utx.begin();
      executor.run();
      utx.commit();
    } catch (Throwable ex) {
      try {
        utx.rollback();
      } catch (Throwable ex2) {
      }
      throw BeeException.error(ex);
    }
  }

  private Response get(Filter filter) {
    long time = System.currentTimeMillis();

    BeeRowSet rs = qs.getViewData(getViewName(), filter);
    Response response = rowSetResponse(rs);
    response.getHeaders().add(LAST_SYNC_TIME, time);

    return response;
  }

  private static Map<String, Object> getFields(BeeView view, String parentCol, JsonObject data) {
    Map<String, Object> fields = new HashMap<>();

    for (ViewColumn viewColumn : view.getViewColumns()) {
      if (Objects.equals(viewColumn.getParent(), parentCol)
          && data.containsKey(viewColumn.getName())) {

        fields.put(viewColumn.getField(), getValue(data, viewColumn.getName()));
      }
    }
    return fields;
  }

  private static BeeColumn getParentColumn(BeeView view, BeeColumn column) {
    BeeColumn parent = null;
    String parentCol = view.getColumnParent(column.getId());

    for (ViewColumn viewColumn : view.getViewColumns()) {
      if (!Objects.equals(viewColumn.getName(), parentCol)
          && Objects.equals(viewColumn.getTable(), view.getColumnTable(parentCol))
          && Objects.equals(viewColumn.getField(), view.getColumnField(parentCol))
          && Objects.equals(viewColumn.getParent(), view.getColumnParent(parentCol))) {

        parent = view.getBeeColumn(viewColumn.getName());
        break;
      }
    }
    if (Objects.isNull(parent)) {
      parent = view.getBeeColumn(parentCol);
    }
    return parent;
  }

  private String getRelation(String table, Map<String, Object> fields) {
    boolean ok = false;

    for (Object o : fields.values()) {
      if (Objects.nonNull(o)) {
        ok = true;
        break;
      }
    }
    if (!ok) {
      return null;
    }
    String[] ids = qs.getColumn(new SqlSelect()
        .addFields(table, sys.getIdName(table))
        .addFrom(table)
        .setWhere(SqlUtils.equals(table, fields)));

    String id = (ids.length == 1) ? ids[0] : null;

    if (!DataUtils.isId(id)) {
      SqlInsert insert = new SqlInsert(table);

      for (Map.Entry<String, Object> entry : fields.entrySet()) {
        insert.addNotNull(entry.getKey(), entry.getValue());
      }
      ResponseObject response = qs.insertDataWithResponse(insert);

      if (response.hasErrors()) {
        throw new BeeRuntimeException(ArrayUtils.joinWords(response.getErrors()));
      }
      id = BeeUtils.toString(response.getResponseAsLong());
    }
    return id;
  }

  private static String getValue(JsonObject data, String field) {
    String value = null;
    JsonValue object = data.get(field);

    if (Objects.nonNull(object) && object != JsonValue.NULL) {
      value = object instanceof JsonString ? ((JsonString) object).getString() : object.toString();
    }
    return value;
  }

  private Long insert(String viewName, JsonObject data) {
    BeeView view = sys.getView(viewName);
    Map<String, BeeColumn> columns = new LinkedHashMap<>();
    List<String> values = new ArrayList<>();

    for (String col : data.keySet()) {
      if (view.hasColumn(col)) {
        BeeColumn column = view.getBeeColumn(col);
        String value = getValue(data, col);

        if (column.isReadOnly() || BeeUtils.isEmpty(value)) {
          continue;
        }
        if (column.isForeign()) {
          column = getParentColumn(view, column);

          if (columns.containsKey(column.getId())) {
            continue;
          }
          value = getValue(data, column.getId());

          if (BeeUtils.isEmpty(value)) {
            value = getRelation(view.getColumnTable(col),
                getFields(view, view.getColumnParent(col), data));
          }
        }
        if (usr.canEditColumn(getViewName(), column.getId())) {
          columns.put(column.getId(), column);
          values.add(value);
        }
      }
    }
    BeeRowSet rs = DataUtils.createRowSetForInsert(viewName, new ArrayList<>(columns.values()),
        values);

    if (Objects.isNull(rs)) {
      throw new BeeRuntimeException(Localized.getConstants().noData());
    }
    ResponseObject response = deb.commitRow(rs, RowInfo.class);

    if (response.hasErrors()) {
      throw new BeeRuntimeException(ArrayUtils.joinWords(response.getErrors()));
    }
    return ((RowInfo) response.getResponse()).getId();
  }

  private static Response serverError(BeeException e) {
    return Response.serverError()
        .type(MediaType.TEXT_PLAIN_TYPE.withCharset(BeeConst.CHARSET_UTF8))
        .entity(BeeUtils.notEmpty(e.getLocalizedMessage(), e.toString())).build();
  }

  private void update(String viewName, Long id, Long version, JsonObject data) {
    if (data.containsKey(OLD_VALUES) && data.get(OLD_VALUES) instanceof JsonObject) {
      JsonObject oldData = data.getJsonObject(OLD_VALUES);
      BeeView view = sys.getView(viewName);
      Map<String, BeeColumn> columns = new LinkedHashMap<>();
      List<String> oldValues = new ArrayList<>();
      List<String> newValues = new ArrayList<>();

      for (String col : oldData.keySet()) {
        if (view.hasColumn(col)) {
          BeeColumn column = view.getBeeColumn(col);

          if (column.isReadOnly()) {
            continue;
          }
          String oldValue = getValue(oldData, col);
          String value = getValue(data, col);

          if (column.isForeign()) {
            column = getParentColumn(view, column);

            if (columns.containsKey(column.getId())) {
              continue;
            }
            String key = getValue(data, column.getId());
            String table = view.getColumnTable(col);

            if (oldData.containsKey(column.getId())) {
              oldValue = getValue(oldData, column.getId());
              value = key;

            } else if (BeeUtils.isEmpty(key)) {
              oldValue = getRelation(table, getFields(view, view.getColumnParent(col), oldData));
              value = getRelation(table, getFields(view, view.getColumnParent(col), data));
            } else {
              ResponseObject response = qs.updateDataWithResponse(new SqlUpdate(table)
                  .addConstant(view.getColumnField(col), getValue(data, col))
                  .setWhere(sys.idEquals(table, BeeUtils.toLong(key))));

              if (response.hasErrors()) {
                throw new BeeRuntimeException(ArrayUtils.joinWords(response.getErrors()));
              }
              continue;
            }
          }
          if (usr.canEditColumn(getViewName(), column.getId())) {
            columns.put(column.getId(), column);
            oldValues.add(oldValue);
            newValues.add(value);
          }
        }
      }
      BeeRowSet rs = DataUtils.getUpdated(viewName, id, version,
          new ArrayList<>(columns.values()), oldValues, newValues, null);

      if (Objects.nonNull(rs)) {
        ResponseObject response = deb.commitRow(rs, RowInfo.class);

        if (response.hasErrors()) {
          throw new BeeRuntimeException(ArrayUtils.joinWords(response.getErrors()));
        }
      }
    }
  }
}
