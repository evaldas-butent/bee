package com.butent.bee.server.modules.transport;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.rest.CrudWorker;
import com.butent.bee.server.rest.RestResponse;
import com.butent.bee.server.rest.annotations.Trusted;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("transport")
@Produces(RestResponse.JSON_TYPE)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class ShipmentRequestsWorker {

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  DataEditorBean deb;
  @EJB
  UserServiceBean usr;

  @POST
  @Path("request")
  @Trusted
  public RestResponse request(JsonObject data) {
    if (!usr.validateHost(CrudWorker.getValue(data, COL_QUERY_HOST))) {
      return RestResponse.error(Localized.getConstants().ipBlocked());
    }
    try {
      BeeView view = sys.getView(VIEW_SHIPMENT_REQUESTS);
      List<JsonObject> places = new ArrayList<>();
      BeeRowSet rs = buildRowSet(view, data);
      JsonArray handlings = data.getJsonArray(COL_CARGO_HANDLING);

      if (Objects.nonNull(handlings)) {
        places.addAll(handlings.getValuesAs(JsonObject.class));

        if (!BeeUtils.isEmpty(places)) {
          BeeRowSet handlingRs = buildRowSet(view, places.get(0));
          rs.getColumns().addAll(handlingRs.getColumns());
          rs.getRow(0).getValues().addAll(handlingRs.getRow(0).getValues());
        }
      }
      ResponseObject response = deb.commitRow(rs);

      if (response.hasErrors()) {
        return RestResponse.error(response.getErrors());
      }
      String cargo = DataUtils.getString(view.getRowSetColumns(), (BeeRow) response.getResponse(),
          COL_CARGO);
      view = sys.getView(VIEW_CARGO_HANDLING);

      for (int i = 1; i < places.size(); i++) {
        BeeRowSet handlingRs = buildRowSet(view, places.get(i));
        handlingRs.getColumns().add(view.getBeeColumn(COL_CARGO));
        handlingRs.getRow(0).getValues().add(cargo);

        response = deb.commitRow(handlingRs);
      }
    } catch (BeeException e) {
      return RestResponse.error(e);
    }
    return RestResponse.ok(data);
  }

  private BeeRowSet buildRowSet(BeeView view, JsonObject json) throws BeeException {
    Map<String, Pair<String, String>> relations = new HashMap<>();
    relations.put(COL_EXPEDITION, Pair.of(TBL_EXPEDITION_TYPES, COL_EXPEDITION_TYPE_NAME));
    relations.put(COL_CARGO_SHIPPING_TERM, Pair.of(TBL_SHIPPING_TERMS, COL_SHIPPING_TERM_NAME));
    relations.put(COL_CARGO_VALUE_CURRENCY, Pair.of(TBL_CURRENCIES, COL_CURRENCY_NAME));

    List<BeeColumn> columns = new ArrayList<>();
    List<String> values = new ArrayList<>();
    JsonObjectBuilder loading = null;
    JsonObjectBuilder unloading = null;

    for (String col : json.keySet()) {
      if (view.hasColumn(col)) {
        BeeColumn column = view.getBeeColumn(col);
        String value = CrudWorker.getValue(json, col);

        if (BeeUtils.isEmpty(value) || column.isReadOnly() || column.isForeign()) {
          continue;
        }
        Object val = null;

        if (BeeUtils.inList(col, VAR_LOADING + COL_PLACE_COUNTRY, VAR_LOADING + COL_PLACE_CITY)) {
          if (Objects.isNull(loading)) {
            loading = Json.createObjectBuilder();
          }
          loading.add(col, value);

        } else if (BeeUtils.inList(col, VAR_UNLOADING + COL_PLACE_COUNTRY,
            VAR_UNLOADING + COL_PLACE_CITY)) {
          if (Objects.isNull(unloading)) {
            unloading = Json.createObjectBuilder();
          }
          unloading.add(col, value);

        } else if (Objects.equals(col, COL_USER_LOCALE)) {
          val = SupportedLocale.getByLanguage(SupportedLocale.normalizeLanguage(value)).ordinal();

        } else if (relations.containsKey(col)) {
          String tbl = relations.get(col).getA();
          String fld = relations.get(col).getB();

          val = qs.getLong(new SqlSelect()
              .addFields(tbl, sys.getIdName(tbl))
              .addFrom(tbl)
              .setWhere(SqlUtils.and(SqlUtils.startsWith(tbl, fld, value),
                  SqlUtils.endsWith(tbl, fld, value))));

          if (Objects.isNull(val)) {
            throw new BeeException(BeeUtils.joinWords(col, value));
          }
          if (Objects.equals(col, COL_EXPEDITION)) {
            columns.add(view.getBeeColumn(COL_EXPEDITION_LOGISTICS));
            values.add(qs.getValue(new SqlSelect()
                .addFields(tbl, COL_EXPEDITION_LOGISTICS)
                .addFrom(tbl)
                .setWhere(SqlUtils.equals(tbl, sys.getIdName(tbl), val))));
          }
        } else {
          switch (column.getType()) {
            case BOOLEAN:
              val = !BeeUtils.isEmpty(value);
              break;
            case DATE:
              JustDate date = TimeUtils.parseDate(value);

              if (Objects.nonNull(date)) {
                val = date.serialize();
              }
              break;
            case DATE_TIME:
              DateTime datetime = TimeUtils.parseDateTime(value);

              if (Objects.nonNull(datetime)) {
                val = datetime.serialize();
              }
              break;
            case DECIMAL:
            case NUMBER:
            case INTEGER:
            case LONG:
              val = BeeUtils.toDecimalOrNull(value);
              break;
            default:
              val = value;
              break;
          }
        }
        if (Objects.nonNull(val)) {
          columns.add(column);
          values.add(val.toString());
        }
      }
    }
    if (Objects.nonNull(loading)) {
      columns.add(view.getBeeColumn(VAR_LOADING + COL_PLACE_NOTE));
      values.add(loading.build().toString());
    }
    if (Objects.nonNull(unloading)) {
      columns.add(view.getBeeColumn(VAR_UNLOADING + COL_PLACE_NOTE));
      values.add(unloading.build().toString());
    }
    return DataUtils.createRowSetForInsert(view.getName(), columns, values);
  }
}
