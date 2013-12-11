package com.butent.bee.server.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeTable.BeeRelation;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.commons.FileStorageBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.SqlConstants.SqlDataType;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.ImportType;
import com.butent.bee.shared.modules.transport.TransportConstants.ImportType.ImportProperty;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

import java.io.File;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import lt.locator.Report;
import lt.locator.ReportProviderInterface;
import lt.locator.ReportSummarizedPeriod;
import lt.locator.TripSumRepData;

@Stateless
@LocalBean
public class TransportImports {

  private static class ImportObject {
    private final int prpValue = 0;
    private final int prpMapingId = 1;
    private final int prpUserData = 2;

    private final ImportType type;
    private final Map<String, Object[]> props = Maps.newHashMap();

    public ImportObject(SimpleRowSet rs) {
      Assert.notNull(rs);
      Assert.state(!rs.isEmpty());

      this.type = EnumUtils.getEnumByIndex(ImportType.class, rs.getInt(0, COL_IMPORT_TYPE));
      Assert.notNull(type);

      for (SimpleRow row : rs) {
        ImportProperty prop = type.getProperty(row.getValue(COL_IMPORT_PROPERTY));

        if (prop != null) {
          Object[] data = new Object[Ints.max(prpValue, prpMapingId, prpUserData) + 1];
          data[prpValue] = row.getValue(COL_IMPORT_VALUE);
          data[prpMapingId] = row.getLong(COL_IMPORT_MAPPING);

          props.put(prop.getName(), data);
        }
      }
    }

    public Long getMappingId(String name) {
      if (props.containsKey(name)) {
        return (Long) props.get(name)[prpMapingId];
      }
      return null;
    }

    public Collection<ImportProperty> getProperties() {
      return type.getProperties();
    }

    public ImportProperty getProperty(String name) {
      return type.getProperty(name);
    }

    public Object getUserData(String name) {
      if (props.containsKey(name)) {
        return props.get(name)[prpUserData];
      }
      return null;
    }

    public String getValue(String name) {
      if (props.containsKey(name)) {
        return (String) props.get(name)[prpValue];
      }
      return null;
    }

    public void setUserData(String name, Object data) {
      if (props.containsKey(name)) {
        props.get(name)[prpUserData] = data;
      }
    }
  }

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  FileStorageBean fs;

  public ResponseObject doImport(RequestInfo reqInfo) {
    ResponseObject response = null;

    ImportType type = EnumUtils.getEnumByIndex(ImportType.class,
        BeeUtils.toIntOrNull(reqInfo.getParameter(COL_IMPORT_TYPE)));

    if (type != null) {
      switch (type) {
        case COSTS:
          response = importCosts(BeeUtils.toLong(reqInfo.getParameter(COL_IMPORT_OPTION)),
              reqInfo.getParameter(VAR_IMPORT_FILE),
              BeeUtils.toBoolean(reqInfo.getParameter("test")));
          break;

        case TRACKING:
          response = importTracking(BeeUtils.toLong(reqInfo.getParameter(COL_IMPORT_OPTION)),
              BeeUtils.toIntOrNull(reqInfo.getParameter(CommonsConstants.VAR_DATE_LOW)),
              BeeUtils.toIntOrNull(reqInfo.getParameter(CommonsConstants.VAR_DATE_HIGH)),
              BeeUtils.toBoolean(reqInfo.getParameter("test")));
          break;
      }
    } else {
      response = ResponseObject.error("Import type not recognized");
    }
    return response;
  }

  private List<ExtendedProperty> checkMappings(ImportObject imp, String prfx, String tmp) {
    List<ExtendedProperty> info = Lists.newArrayList();
    PropertyUtils.addExtended(info, "NEŽINOMIEJI", null, ":");

    for (ImportProperty prop : imp.getProperties()) {
      String relTable = prop.getRelTable();

      if (BeeUtils.isEmpty(relTable)) {
        continue;
      }
      String name = prop.getName();
      Long mappingId = imp.getMappingId(name);

      if (DataUtils.isId(mappingId)) {
        SqlSelect query = new SqlSelect()
            .addField(TBL_IMPORT_MAPPINGS, COL_IMPORT_VALUE, name)
            .addFields(TBL_IMPORT_MAPPINGS, COL_IMPORT_MAPPING)
            .addFrom(TBL_IMPORT_MAPPINGS)
            .addFromInner(relTable,
                sys.joinTables(relTable, TBL_IMPORT_MAPPINGS, COL_IMPORT_MAPPING))
            .setWhere(SqlUtils.equals(TBL_IMPORT_MAPPINGS, COL_IMPORT_PROPERTY, mappingId));
        String subq = SqlUtils.uniqueName();

        qs.updateData(new SqlUpdate(tmp)
            .addExpression(prfx + name, SqlUtils.field(subq, COL_IMPORT_MAPPING))
            .setFrom(query, subq, SqlUtils.joinUsing(tmp, subq, name)));
      }
      qs.updateData(new SqlUpdate(tmp)
          .addExpression(prfx + name, SqlUtils.field(relTable, sys.getIdName(relTable)))
          .setFrom(relTable, SqlUtils.join(tmp, name, relTable, prop.getRelField()))
          .setWhere(SqlUtils.isNull(tmp, prfx + name)));

      for (SimpleRow row : qs.getData(new SqlSelect()
          .addFields(tmp, name)
          .addCount("cnt")
          .addFrom(tmp)
          .setWhere(SqlUtils.isNull(tmp, prfx + name))
          .addGroup(tmp, name)
          .addOrderDesc(null, "cnt"))) {

        PropertyUtils.addExtended(info, prop.getCaption(), row.getValue(name), row.getValue("cnt"));
      }
    }
    return info;
  }

  private ResponseObject importCosts(Long optionId, String fileName, boolean test) {
    File file = new File(fileName);

    if (!file.isFile() || !file.canRead()) {
      return ResponseObject.error(Localized.getMessages().fileNotFound(fileName));
    }
    ImportObject imp = initImport(optionId);
    Sheet shit;

    try {
      Workbook wb = WorkbookFactory.create(file);
      wb.setMissingCellPolicy(Row.RETURN_BLANK_AS_NULL);
      shit = wb.getSheetAt(0);
    } catch (Exception e) {
      file.delete();
      return ResponseObject.error(e);
    }
    int startRow = BeeUtils.max(BeeUtils.toInt(imp.getValue(VAR_IMPORT_START_ROW)) - 1,
        shit.getFirstRowNum());

    DateFormat dtf = null;
    if (!BeeUtils.isEmpty(imp.getValue(VAR_IMPORT_DATE_FORMAT))) {
      dtf = new SimpleDateFormat(imp.getValue(VAR_IMPORT_DATE_FORMAT));
    }
    String tmp = SqlUtils.temporaryName();
    String prfx = "_";
    SqlCreate create = new SqlCreate(tmp)
        .addBoolean(prfx, false)
        .addBoolean(COL_FUEL, false)
        .addLong(COL_TRIP, false);

    for (ImportProperty prop : imp.getProperties()) {
      String name = prop.getName();

      if (BeeUtils.inList(name, VAR_IMPORT_START_ROW, VAR_IMPORT_DATE_FORMAT)) {
        continue;
      }
      if (!BeeUtils.isEmpty(prop.getRelTable())) {
        create.addLong(prfx + name, false);
      }
      String colValue = imp.getValue(name);

      if (BeeUtils.isPrefix(colValue, '=')) {
        imp.setUserData(name, CellReference.convertColStringToIndex(colValue.substring(1)));
      }
      create.addString(name, 50, false);
    }
    qs.updateData(create);

    for (int i = startRow; i <= shit.getLastRowNum(); i++) {
      Row row = shit.getRow(i);
      Map<String, String> values = Maps.newHashMap();

      for (ImportProperty prop : imp.getProperties()) {
        String name = prop.getName();

        if (BeeUtils.inList(name, VAR_IMPORT_START_ROW, VAR_IMPORT_DATE_FORMAT)) {
          continue;
        }
        String value = imp.getValue(name);
        Integer col = (Integer) imp.getUserData(name);

        if (BeeUtils.isNonNegative(col)) {
          Cell cell = row.getCell(col);

          if (cell != null) {
            switch (cell.getCellType()) {
              case Cell.CELL_TYPE_STRING:
                value = cell.getRichStringCellValue().getString();
                break;
              case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                  Date date = cell.getDateCellValue();

                  if (date != null) {
                    value = new DateTime(date).toDateString();
                  } else {
                    value = null;
                  }
                } else {
                  value = BeeUtils.toString(cell.getNumericCellValue());
                }
                break;
              case Cell.CELL_TYPE_BOOLEAN:
                value = BeeUtils.toString(cell.getBooleanCellValue());
                break;
              case Cell.CELL_TYPE_FORMULA:
                value = cell.getCellFormula();
                break;
              default:
                value = null;
            }
          }
        }
        values.put(name, value);
      }
      double qty = BeeUtils.toDouble(values.get(COL_COSTS_QUANTITY));
      double prc = BeeUtils.toDouble(values.get(COL_COSTS_PRICE));

      if (!BeeUtils.isPositive(qty)) {
        continue;
      }
      if (!BeeUtils.isPositive(prc)) {
        prc = BeeUtils.round(BeeUtils.toDouble(values.get(COL_AMOUNT)) / qty, 5);
      }
      if (BeeUtils.isPositive(prc)) {
        values.put(COL_COSTS_PRICE, BeeUtils.toString(prc));
      } else {
        continue;
      }
      String value = values.get(COL_COSTS_DATE);

      if (!BeeUtils.isEmpty(value)) {
        DateTime date;

        if (dtf != null) {
          try {
            date = new DateTime(dtf.parse(value));
          } catch (ParseException e) {
            date = null;
          }
        } else {
          date = TimeUtils.parseDateTime(value);
        }
        values.put(COL_COSTS_DATE, date != null
            ? BeeUtils.toString(date.getDate().getTime()) : null);
      }
      value = values.get(COL_COSTS_EXTERNAL_ID);

      if (BeeUtils.isEmpty(value)) {
        values.put(COL_COSTS_EXTERNAL_ID, Codec.md5(BeeUtils.joinItems(values.get(COL_VEHICLE),
            values.get(COL_COSTS_DATE), values.get(COL_COSTS_ITEM),
            values.get(COL_COSTS_SUPPLIER), values.get(COL_NUMBER), values.get(COL_COSTS_NOTE))));
      }
      SqlInsert insert = new SqlInsert(tmp);

      for (String name : values.keySet()) {
        insert.addNotEmpty(name, values.get(name));
      }
      qs.updateData(insert);
    }
    file.delete();

    List<ExtendedProperty> info = checkMappings(imp, prfx, tmp);
    int c;

    if (!test) {
      c = qs.updateData(new SqlDelete(tmp)
          .setWhere(SqlUtils.or(SqlUtils.isNull(tmp, prfx + COL_COSTS_ITEM),
              SqlUtils.isNull(tmp, prfx + COL_COSTS_CURRENCY),
              SqlUtils.and(SqlUtils.notNull(tmp, COL_VEHICLE),
                  SqlUtils.isNull(tmp, prfx + COL_VEHICLE)))));

      PropertyUtils.addExtended(info, "PAŠALINTA", null, c);
    }
    qs.updateData(new SqlUpdate(tmp)
        .addExpression(COL_FUEL, SqlUtils.constant(true))
        .setFrom(TBL_FUEL_TYPES,
            SqlUtils.join(tmp, prfx + COL_COSTS_ITEM, TBL_FUEL_TYPES, COL_ITEM)));

    for (String tt : new String[] {TBL_TRIP_COSTS, TBL_TRIP_FUEL_COSTS}) {
      HasConditions wh = SqlUtils.and(BeeUtils.same(tt, TBL_TRIP_FUEL_COSTS)
          ? SqlUtils.notNull(tmp, COL_FUEL) : SqlUtils.isNull(tmp, COL_FUEL),
          SqlUtils.notNull(tt, COL_COSTS_EXTERNAL_ID));

      c = qs.updateData(new SqlUpdate(tmp)
          .addExpression(prfx, SqlUtils.constant(true))
          .setFrom(tt, SqlUtils.and(wh, SqlUtils.joinUsing(tmp, tt, COL_COSTS_EXTERNAL_ID))));

      PropertyUtils.addExtended(info, "BUVO", tt, c);

      if (!BeeUtils.isEmpty(imp.getValue(COL_NUMBER)) && !test) {
        SqlSelect query = new SqlSelect()
            .addEmptyBoolean(prfx)
            .addField(tt, sys.getIdName(tt), "ID")
            .addFields(tmp, COL_NUMBER)
            .addFields(TBL_TRIPS, COL_TRIP_NO)
            .addFrom(tmp)
            .addFromInner(tt, SqlUtils.joinUsing(tmp, tt, COL_COSTS_EXTERNAL_ID))
            .addFromInner(TBL_TRIPS, sys.joinTables(TBL_TRIPS, tt, COL_TRIP))
            .setWhere(wh);

        wh = SqlUtils.and();
        String[] flds = new String[] {COL_COSTS_QUANTITY, COL_COSTS_PRICE, COL_COSTS_VAT};

        for (String fld : flds) {
          query.addExpr(SqlUtils.nvl(SqlUtils.field(tt, fld), 0), fld)
              .addExpr(SqlUtils.nvl(SqlUtils.cast(SqlUtils.field(tmp, fld),
                  SqlDataType.DECIMAL, 15, 5), 0), prfx + fld);

          wh.add(SqlUtils.equals(SqlUtils.name(fld), SqlUtils.name(prfx + fld)));
        }
        String diff = qs.sqlCreateTemp(query);

        qs.updateData(new SqlUpdate(diff)
            .addExpression(prfx, SqlUtils.constant(true))
            .setWhere(wh));

        qs.updateData(new SqlUpdate(tt)
            .addExpression(COL_NUMBER, SqlUtils.field(diff, COL_NUMBER))
            .setFrom(diff, sys.joinTables(tt, diff, "ID"))
            .setWhere(SqlUtils.notNull(diff, prfx)));

        for (String fld : flds) {
          SimpleRowSet rs = qs.getData(new SqlSelect()
              .addFields(diff, COL_TRIP_NO, fld, prfx + fld)
              .addFrom(diff)
              .setWhere(SqlUtils.notEqual(SqlUtils.name(fld), SqlUtils.name(prfx + fld))));

          c = rs.getNumberOfRows();

          if (c > 0) {
            PropertyUtils.addExtended(info, "SKIRTUMAI", imp.getProperty(fld).getCaption(), c);

            for (SimpleRow row : rs) {
              PropertyUtils.addExtended(info, row.getValue(COL_TRIP_NO), row.getValue(fld),
                  row.getValue(prfx + fld));
            }
          }
        }
        qs.sqlDropTemp(diff);
      }
    }
    IsExpression dt = SqlUtils.cast(SqlUtils.field(tmp, COL_COSTS_DATE), SqlDataType.DATE, 0, 0);

    qs.updateData(new SqlUpdate(tmp)
        .addExpression(COL_TRIP, SqlUtils.field(TBL_TRIPS, sys.getIdName(TBL_TRIPS)))
        .setFrom(TBL_TRIPS,
            SqlUtils.and(SqlUtils.join(tmp, prfx + COL_VEHICLE, TBL_TRIPS, COL_VEHICLE),
                SqlUtils.moreEqual(dt, SqlUtils.nvl(SqlUtils.field(TBL_TRIPS, COL_TRIP_DATE_FROM),
                    SqlUtils.field(TBL_TRIPS, COL_TRIP_DATE))),
                SqlUtils.less(dt, SqlUtils.nvl(SqlUtils.field(TBL_TRIPS, COL_TRIP_DATE_TO),
                    SqlUtils.field(TBL_TRIPS, COL_TRIP_PLANNED_END_DATE), Long.MAX_VALUE)),
                SqlUtils.isNull(tmp, prfx), SqlUtils.notNull(tmp, prfx + COL_VEHICLE))));

    qs.updateData(new SqlUpdate(tmp)
        .addExpression(COL_COSTS_NOTE, SqlUtils.field(tmp, COL_VEHICLE))
        .setWhere(SqlUtils.and(SqlUtils.isNull(tmp, COL_TRIP),
            SqlUtils.notNull(tmp, prfx + COL_VEHICLE))));

    for (String tt : new String[] {TBL_TRIP_COSTS, TBL_TRIP_FUEL_COSTS}) {
      SqlSelect query = new SqlSelect()
          .addFields(tmp, COL_COSTS_DATE, COL_COSTS_QUANTITY, COL_COSTS_PRICE,
              COL_TRADE_VAT_PLUS, COL_COSTS_VAT, COL_TRADE_VAT_PERC,
              COL_NUMBER, COL_COSTS_NOTE, COL_TRIP, COL_COSTS_EXTERNAL_ID)
          .addField(tmp, prfx + COL_COSTS_CURRENCY, COL_COSTS_CURRENCY)
          .addField(tmp, prfx + COL_COSTS_COUNTRY, COL_COSTS_COUNTRY)
          .addField(tmp, prfx + COL_COSTS_SUPPLIER, COL_COSTS_SUPPLIER)
          .addFrom(tmp)
          .setWhere(SqlUtils.isNull(tmp, prfx));

      if (BeeUtils.same(tt, TBL_TRIP_COSTS)) {
        query.addField(tmp, prfx + COL_COSTS_ITEM, COL_COSTS_ITEM)
            .setWhere(SqlUtils.and(query.getWhere(), SqlUtils.isNull(tmp, COL_FUEL)));
      } else {
        query.setWhere(SqlUtils.and(query.getWhere(), SqlUtils.notNull(tmp, COL_FUEL)));
      }
      SimpleRowSet rs = qs.getData(query);

      if (!test) {
        for (int i = 0; i < rs.getNumberOfRows(); i++) {
          qs.insertData(new SqlInsert(tt)
              .addFields(rs.getColumnNames())
              .addValues((Object[]) rs.getValues(i)));
        }
      }
      PropertyUtils.addExtended(info, "IMPORTUOTA", tt, rs.getNumberOfRows());
    }
    Object res;

    if (test) {
      res = qs.doSql(new SqlSelect().addAllFields(tmp).addFrom(tmp).getQuery());
    } else {
      res = info;
    }
    qs.sqlDropTemp(tmp);
    return ResponseObject.response(res);
  }

  private ResponseObject importTracking(Long optionId, Integer from, Integer to, boolean test) {
    ImportObject imp = initImport(optionId);

    Long mappingId = imp.getMappingId(COL_VEHICLE);
    String[] objects = null;

    if (DataUtils.isId(mappingId)) {
      objects = qs.getColumn(new SqlSelect()
          .addFields(TBL_IMPORT_MAPPINGS, COL_IMPORT_VALUE)
          .addFrom(TBL_IMPORT_MAPPINGS)
          .setWhere(SqlUtils.equals(TBL_IMPORT_MAPPINGS, COL_IMPORT_PROPERTY, mappingId)));
    }
    if (ArrayUtils.isEmpty(objects)) {
      return ResponseObject.error(Localized.getConstants().noData());
    }
    String prfx = "_";

    List<String> cols = Lists.newArrayList();

    for (BeeField fld : sys.getTableFields(TBL_VEHICLE_TRACKING)) {
      if (!(fld instanceof BeeRelation)) {
        cols.add(fld.getName());
      }
    }
    String tmp = qs.sqlCreateTemp(new SqlSelect()
        .addEmptyBoolean(prfx)
        .addFields(TBL_VEHICLE_TRACKING, cols.toArray(new String[0]))
        .addField(TBL_VEHICLE_TRACKING, COL_VEHICLE, prfx + COL_VEHICLE)
        .addField(TBL_VEHICLE_TRACKING, COL_COUNTRY, prfx + COL_COUNTRY)
        .addEmptyString(COL_VEHICLE, 30)
        .addEmptyString(COL_COUNTRY, 30)
        .addFrom(TBL_VEHICLE_TRACKING)
        .setWhere(SqlUtils.sqlFalse()));

    String login = imp.getValue(VAR_IMPORT_LOGIN);
    String pwd = imp.getValue(VAR_IMPORT_PASSWORD);

    JustDate dateFrom = new JustDate(from);
    JustDate dateTo = new JustDate(to);

    ReportProviderInterface port = new Report().getReportProviderImplPort();

    for (String obj : objects) {
      boolean exists = true;

      for (int i = 0; i < TimeUtils.dayDiff(dateFrom, dateTo); i++) {
        DateTime dt = TimeUtils.nextDay(dateFrom, i).getDateTime();
        TimeUtils.addMinute(dt, TimeUtils.MINUTES_PER_DAY - 1);

        ReportSummarizedPeriod report = port.getSummarizedReport(login, pwd,
            TimeUtils.startOfDay(dt).toString(), dt.toString(), obj, false, true, null, null);

        switch (report.getErrorCode()) {
          case 0:
            break;

          case -6:
            exists = false;
            break;

          default:
            qs.sqlDropTemp(tmp);
            return ResponseObject.error(report.getErrorMessage(),
                BeeUtils.parenthesize(report.getErrorCode()));
        }
        if (!exists) {
          break;
        }
        List<TripSumRepData> data = report.getTripSummaryByCountriesGrouped();

        if (!BeeUtils.isEmpty(data)) {
          for (TripSumRepData tripData : data) {
            qs.insertData(new SqlInsert(tmp)
                .addConstant(COL_VEHICLE, obj)
                .addConstant(COL_COUNTRY, tripData.getName())
                .addConstant(COL_DATE, dt.getDate())
                .addConstant("DriveTimeInHours", tripData.getDriveTimeInHours())
                .addConstant("StopTimeInHours", tripData.getStopTimeInHours())
                .addConstant("GpsDistanceInKm", tripData.getGpsDistanceInKm())
                .addConstant("GpsAverageFuelUsage", tripData.getGpsAverageFuelUsage())
                .addConstant("CounterFuelUsedInLiters", tripData.getCounterFuelUsedInLiters())
                .addConstant("CanDistanceInKm", tripData.getCanDistanceInKm())
                .addConstant("CanFuelUsedInLiters", tripData.getCanFuelUsedInLiters())
                .addConstant("CanAverageFuelUsage", tripData.getCanAverageFuelUsage())
                .addConstant("CanOdometerValueStartInKm", tripData.getCanOdometerValueStartInKm())
                .addConstant("CanOdometerValueEndInKm", tripData.getCanOdometerValueEndInKm()));
          }
        }
      }
    }
    List<ExtendedProperty> info = checkMappings(imp, prfx, tmp);
    int c;

    if (!test) {
      c = qs.updateData(new SqlDelete(tmp)
          .setWhere(SqlUtils.or(SqlUtils.isNull(tmp, prfx + COL_VEHICLE),
              SqlUtils.and(SqlUtils.notNull(tmp, COL_COUNTRY),
                  SqlUtils.isNull(tmp, prfx + COL_COUNTRY)))));
      PropertyUtils.addExtended(info, "PAŠALINTA", null, c);
    }
    c = qs.updateData(new SqlUpdate(tmp)
        .addExpression(prfx, SqlUtils.constant(true))
        .setFrom(TBL_VEHICLE_TRACKING, SqlUtils.and(SqlUtils.join(tmp, prfx + COL_VEHICLE,
            TBL_VEHICLE_TRACKING, COL_VEHICLE), SqlUtils.joinUsing(tmp, TBL_VEHICLE_TRACKING,
            COL_DATE), SqlUtils.or(SqlUtils.isNull(tmp, prfx + COL_COUNTRY),
            SqlUtils.join(tmp, prfx + COL_COUNTRY, TBL_VEHICLE_TRACKING, COL_COUNTRY)))));

    PropertyUtils.addExtended(info, "BUVO", null, c);

    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(tmp, cols.toArray(new String[0]))
        .addField(tmp, prfx + COL_VEHICLE, COL_VEHICLE)
        .addField(tmp, prfx + COL_COUNTRY, COL_COUNTRY)
        .addFrom(tmp)
        .setWhere(SqlUtils.isNull(tmp, prfx)));

    if (!test) {
      for (int i = 0; i < rs.getNumberOfRows(); i++) {
        qs.insertData(new SqlInsert(TBL_VEHICLE_TRACKING)
            .addFields(rs.getColumnNames())
            .addValues((Object[]) rs.getValues(i)));
      }
    }
    PropertyUtils.addExtended(info, "IMPORTUOTA", null, rs.getNumberOfRows());
    Object res;

    if (test) {
      res = qs.doSql(new SqlSelect().addAllFields(tmp).addFrom(tmp).getQuery());
    } else {
      res = info;
    }
    qs.sqlDropTemp(tmp);
    return ResponseObject.response(res);
  }

  private ImportObject initImport(Long optId) {
    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_IMPORT_OPTIONS, COL_IMPORT_TYPE)
        .addFields(TBL_IMPORT_PROPERTIES, COL_IMPORT_PROPERTY, COL_IMPORT_VALUE)
        .addField(TBL_IMPORT_PROPERTIES, sys.getIdName(TBL_IMPORT_PROPERTIES), COL_IMPORT_MAPPING)
        .addFrom(TBL_IMPORT_OPTIONS)
        .addFromLeft(TBL_IMPORT_PROPERTIES,
            sys.joinTables(TBL_IMPORT_OPTIONS, TBL_IMPORT_PROPERTIES, COL_IMPORT_OPTION))
        .setWhere(SqlUtils.equals(TBL_IMPORT_OPTIONS, sys.getIdName(TBL_IMPORT_OPTIONS), optId)));

    return new ImportObject(rs);
  }
}
