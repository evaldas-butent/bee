package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.data.BeeTable.BeeField;
import com.butent.bee.egg.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.egg.server.data.BeeTable.BeeKey;
import com.butent.bee.egg.server.utils.FileUtils;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.BeeColumn;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeRowSet.BeeRow;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.IsCondition;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlDelete;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUpdate;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

@Singleton
@Startup
@Lock(LockType.READ)
public class SystemBean {
  private static final Class<?> CLASS = SystemBean.class;
  public static final String RESOURCE_PATH = CLASS.getResource(CLASS.getSimpleName() + ".class").getPath()
    .replace(CLASS.getName().replace('.', '/') + ".class", "../config/");
  public static final String RESOURCE_SCHEMA = RESOURCE_PATH + "structure.xsd";

  private static Logger logger = Logger.getLogger(SystemBean.class.getName());

  @EJB
  QueryServiceBean qs;

  private String dbName;
  private String dbSchema;
  private Map<String, BeeTable> dataCache = new HashMap<String, BeeTable>();
  private Map<String, BeeView> viewCache = new HashMap<String, BeeView>();

  public String backupTable(String name) {
    String tmp = null;
    int rc = qs.getSingleRow(new SqlSelect().addCount(name).addFrom(name)).getInt(0);

    if (rc > 0) {
      tmp = SqlUtils.temporaryName();

      qs.updateData(new SqlCreate(tmp)
        .setSource(new SqlSelect().addAllFields(name).addFrom(name)));
    }
    return tmp;
  }

  public boolean commitChanges(BeeRowSet upd, ResponseBuffer buff) {
    int c = 0;
    String err = "";

    BeeTable tbl = null;
    BeeTable extTbl = null;
    int idIndex = -1;
    int lockIndex = -1;
    int extLockIndex = -1;
    BeeView view = getView(upd.getViewName());
    String src = upd.getViewName();

    if (isTable(src)) {
      tbl = getTable(src);
      extTbl = getExtTable(src);

      for (int i = 0; i < upd.getColumns().length; i++) {
        BeeColumn column = upd.getColumns()[i];

        if (BeeUtils.same(column.getFieldSource(), tbl.getName())) {
          String name = column.getFieldName();

          if (BeeUtils.same(name, tbl.getIdName())) {
            idIndex = i;
            continue;
          }
          if (BeeUtils.same(name, tbl.getLockName())) {
            lockIndex = i;
            continue;
          }
          if (!BeeUtils.isEmpty(extTbl) && BeeUtils.same(name, extTbl.getLockName())) {
            extLockIndex = i;
            continue;
          }
        }
      }
      if (idIndex < 0) {
        err = "Cannot update table " + tbl.getName() + " (Unknown ID index).";
      }
    } else {
      err = "Cannot update table (Not a base table " + src + ").";
    }
    for (BeeRow row : upd.getRows()) {
      if (!BeeUtils.isEmpty(err)) {
        break;
      }
      List<Object[]> baseList = new ArrayList<Object[]>();
      List<Object[]> extList = new ArrayList<Object[]>();

      if (!BeeUtils.isEmpty(row.getShadow())) {
        for (Integer col : row.getShadow().keySet()) {
          BeeColumn column = upd.getColumn(col);
          String fld = column.getFieldName();

          if (BeeUtils.isEmpty(fld)) {
            err = "Cannot update column " + upd.getColumnName(col) + " (Unknown source).";
            break;
          }
          if (!BeeUtils.same(column.getFieldSource(), src)) {
            err = "Cannot update column (Wrong source " + column.getFieldSource() + ").";
            break;
          }
          Object[] entry = new Object[]{fld, row.getOriginal(col)};

          if (tbl.hasField(fld)) {
            baseList.add(entry);
          } else if (!BeeUtils.isEmpty(extTbl) && extTbl.hasField(fld)) {
            extList.add(entry);
          } else {
            err = "Cannot update column " + upd.getColumnName(col) + " (Unknown field: " + fld
                + ").";
            break;
          }
        }
        if (!BeeUtils.isEmpty(err)) {
          break;
        }
      }
      long id = row.getLong(idIndex);

      if (row.markedForDelete()) { // DELETE
        SqlDelete sd = new SqlDelete(tbl.getName());
        IsCondition wh = SqlUtils.equal(tbl.getName(), tbl.getIdName(), id);

        if (lockIndex >= 0) {
          wh = SqlUtils.and(wh,
              SqlUtils.equal(tbl.getName(), tbl.getLockName(), row.getLong(lockIndex)));
        }
        int res = qs.updateData(sd.setWhere(wh));

        if (res < 0) {
          err = "Error deleting data";
          break;
        } else if (res == 0) {
          err = "Optimistic lock exception";
          break;
        }
        c += res;

      } else if (row.markedForInsert()) { // INSERT
        SqlInsert si = new SqlInsert(tbl.getName());

        for (Object[] entry : baseList) {
          si.addConstant((String) entry[0], entry[1]);
        }
        if (lockIndex >= 0) {
          long lock = System.currentTimeMillis();
          si.addConstant(tbl.getLockName(), lock);
          row.setValue(lockIndex, BeeUtils.transform(lock));
        }
        id = qs.insertData(si);

        if (id < 0) {
          err = "Error inserting data";
          break;
        }
        c++;
        row.setValue(idIndex, BeeUtils.transform(id));

        if (!BeeUtils.isEmpty(extList)) {
          si = new SqlInsert(extTbl.getName());

          for (Object[] entry : extList) {
            si.addConstant((String) entry[0], entry[1]);
          }
          if (extLockIndex >= 0) {
            long lock = System.currentTimeMillis();
            si.addConstant(extTbl.getLockName(), lock);
            row.setValue(extLockIndex, BeeUtils.transform(lock));
          }
          si.addConstant(extTbl.getIdName(), id);

          if (qs.insertData(si) < 0) {
            err = "Error inserting data";
            break;
          }
          c++;
        }

      } else { // UPDATE
        if (!BeeUtils.isEmpty(baseList)) {
          SqlUpdate su = new SqlUpdate(tbl.getName());

          for (Object[] entry : baseList) {
            su.addConstant((String) entry[0], entry[1]);
          }
          IsCondition wh = SqlUtils.equal(tbl.getName(), tbl.getIdName(), id);

          if (lockIndex >= 0) {
            wh = SqlUtils.and(wh,
                SqlUtils.equal(tbl.getName(), tbl.getLockName(), row.getLong(lockIndex)));

            long lock = System.currentTimeMillis();
            su.addConstant(tbl.getLockName(), lock);
            row.setValue(lockIndex, BeeUtils.transform(lock));
          }
          int res = qs.updateData(su.setWhere(wh));

          if (res < 0) {
            err = "Error updating data";
            break;
          } else if (res == 0) {
            err = "Optimistic lock exception";
            break;
          }
          c += res;
        }
        if (!BeeUtils.isEmpty(extList)) {
          if (extLockIndex < 0 || !BeeUtils.isEmpty(row.getLong(extLockIndex))) {
            SqlUpdate su = new SqlUpdate(extTbl.getName());

            for (Object[] entry : extList) {
              su.addConstant((String) entry[0], entry[1]);
            }
            IsCondition wh = SqlUtils.equal(extTbl.getName(), extTbl.getIdName(), id);

            if (extLockIndex >= 0) {
              wh = SqlUtils.and(wh,
                  SqlUtils.equal(extTbl.getName(), extTbl.getLockName(), row.getLong(extLockIndex)));

              long lock = System.currentTimeMillis();
              su.addConstant(extTbl.getLockName(), lock);
              row.setValue(extLockIndex, BeeUtils.transform(lock));
            }
            int res = qs.updateData(su.setWhere(wh));

            if (res < 0) {
              err = "Error updating data";
              break;
            } else if (res == 0 && extLockIndex >= 0) {
              err = "Optimistic lock exception";
              break;
            } else if (res > 0) {
              c += res;
              continue;
            }
          }
          SqlInsert si = new SqlInsert(extTbl.getName());

          for (Object[] entry : extList) {
            si.addConstant((String) entry[0], entry[1]);
          }
          if (extLockIndex >= 0) {
            long lock = System.currentTimeMillis();
            si.addConstant(extTbl.getLockName(), lock);
            row.setValue(extLockIndex, BeeUtils.transform(lock));
          }
          si.addConstant(extTbl.getIdName(), id);

          if (qs.insertData(si) < 0) {
            err = "Error inserting data";
            break;
          }
          c++;
        }
      }
    }
    if (BeeUtils.isEmpty(err)) {
      buff.add(c);
      buff.add(upd.serialize());
    } else {
      buff.add(-1);
      buff.add(err);
      return false;
    }
    return true;
  }

  public String getDbName() {
    return dbName;
  }

  public String getDbSchema() {
    return dbSchema;
  }

  public Collection<BeeField> getExtFields(String tbl) {
    return getTable(tbl).getExtFields();
  }

  public BeeField getField(String tbl, String fld) {
    BeeTable table = getTable(tbl);
    BeeField field = table.getField(fld);

    if (BeeUtils.isEmpty(field)) {
      field = table.getExtField(fld);
    }
    return field;
  }

  public Collection<BeeField> getFields(String table) {
    return getTable(table).getFields();
  }

  public String getIdName(String table) {
    return getTable(table).getIdName();
  }

  public String getLockName(String table) {
    return getTable(table).getLockName();
  }

  public Collection<String> getTableNames() {
    return Collections.unmodifiableCollection(dataCache.keySet());
  }

  public BeeRowSet getViewData(String viewName) {
    return getViewData(getView(viewName));
  }

  public boolean hasField(String table, String field) {
    return getTable(table).hasField(field);
  }

  @PostConstruct
  public void init() {
    dbName = qs.dbName();
    dbSchema = qs.dbSchema();
    initTables();
    initViews();
    initExtensions();
  }

  @Lock(LockType.WRITE)
  public void initExtensions() {
    String resource = RESOURCE_PATH + "extensions.xml";

    List<BeeTable> extensions = loadTables(resource);

    if (BeeUtils.isEmpty(extensions)) {
      return;
    }
    int cNew = 0;
    int cUpd = 0;

    for (BeeTable extension : extensions) {
      String extName = extension.getName();

      if (!isRegisteredTable(extName) || getRegisteredTable(extName).isCustom()) {
        if (extension.isEmpty()) {
          LogUtils.warning(logger, resource, "Table", extName, "has no fields defined");
          continue;
        }
        extension.setCustom();
        registerTable(extension);
        cNew++;
      } else {
        BeeTable table = getRegisteredTable(extName);

        if (table.applyChanges(extension) > 0) {
          cUpd++;
        }
      }
    }
    LogUtils.infoNow(logger, "Loaded", cNew, "new tables, updated", cUpd,
        "existing tables descriptions from", resource);
  }

  public boolean isTable(String tableName) {
    boolean exists = isRegisteredTable(tableName);

    if (exists) {
      BeeTable table = getRegisteredTable(tableName);

      if (!table.isActive()) {
        if (!qs.isDbTable(getDbName(), getDbSchema(), tableName)) {
          rebuildTable(table, false);
        } else {
          table.activate();
        }
      }
    }
    return exists;
  }

  public String joinExtField(HasFrom<?> query, String fld, String tbl, String tblAlias) {
    BeeTable table = getTable(tbl);
    return table.appendExtJoin(query, tblAlias, table.getField(fld));
  }

  public void rebuildTable(String table) {
    rebuildTable(getTable(table), true);
  }

  @Lock(LockType.WRITE)
  public void rebuildTables(ResponseBuffer buff) {
    for (BeeTable tbl : getTables()) {
      rebuildTable(tbl, qs.isDbTable(getDbName(), getDbSchema(), tbl.getName()));
    }
    buff.add("Recreate structure OK");
  }

  public int restoreTable(String tbl, String tmp) {
    int rc = 0;

    if (!BeeUtils.isEmpty(tmp)) {
      Collection<String> tmpFields = qs.dbFields(tmp);
      List<String> fldList = new ArrayList<String>();

      for (String fld : qs.dbFields(tbl)) {
        if (tmpFields.contains(fld)) {
          fldList.add(fld);
        }
      }
      if (!BeeUtils.isEmpty(fldList)) {
        String[] flds = fldList.toArray(new String[0]);

        rc = qs.updateData(new SqlInsert(tbl)
          .addFields(flds)
          .setSource(new SqlSelect().addFields(tmp, flds).addFrom(tmp)));
      }
      qs.updateData(SqlUtils.dropTable(tmp));
    }
    return rc;
  }

  @Lock(LockType.WRITE)
  private void createForeignKeys(Collection<BeeForeignKey> fKeys) {
    for (BeeForeignKey fKey : fKeys) {
      String tblName = fKey.getTable();
      String refTblName = fKey.getRefTable();

      if (isActive(refTblName)) {
        qs.updateData(SqlUtils.createForeignKey(tblName, fKey.getName(),
            fKey.getKeyField(), refTblName, getIdName(refTblName), fKey.getAction()));
      }
    }
  }

  @Lock(LockType.WRITE)
  private void createKeys(Collection<BeeKey> keys) {
    for (BeeKey key : keys) {
      String tblName = key.getTable();

      IsQuery index;
      String keyName = key.getName();
      String[] keyFields = key.getKeyFields();

      if (key.isPrimary()) {
        index = SqlUtils.createPrimaryKey(tblName, keyName, keyFields);
      } else if (key.isUnique()) {
        index = SqlUtils.createUniqueIndex(tblName, keyName, keyFields);
      } else {
        index = SqlUtils.createIndex(tblName, keyName, keyFields);
      }
      qs.updateData(index);
    }
  }

  @Lock(LockType.WRITE)
  private void createTable(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      SqlCreate sc = new SqlCreate(table.getName(), false);

      for (BeeField field : table.getFields()) {
        if (BeeUtils.same(field.getTable(), table.getName())) {
          sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
              field.isNotNull() ? Keywords.NOT_NULL : null);
        }
      }
      sc.addLong(table.getLockName(), Keywords.NOT_NULL);
      sc.addLong(table.getIdName(), Keywords.NOT_NULL);
      qs.updateData(sc);
      table.activate();
    }
  }

  private BeeView getDefaultView(String tbl, boolean allFields) {
    BeeTable table = getTable(tbl);
    Collection<BeeField> fields = allFields ? table.getFields() : table.getMainFields();

    BeeView view = new BeeView(tbl, tbl);

    for (BeeField field : fields) {
      String fld = field.getName();
      view.addField(fld, null);
      String relTbl = field.getRelation();

      if (!BeeUtils.isEmpty(relTbl) && !BeeUtils.same(relTbl, tbl)) {
        BeeView vw = getDefaultView(relTbl, false);

        for (String xpr : vw.getFields().values()) {
          view.addField(fld + ">" + xpr, null);
        }
      }
    }
    return view;
  }

  private BeeTable getRegisteredTable(String tableName) {
    Assert.state(isRegisteredTable(tableName), "Not a base table: " + tableName);
    return dataCache.get(tableName);
  }

  private BeeView getRegisteredView(String viewName) {
    return viewCache.get(viewName);
  }

  private BeeTable getTable(String tableName) {
    Assert.state(isTable(tableName), "Not a base table: " + tableName);
    return getRegisteredTable(tableName);
  }

  private Collection<BeeTable> getTables() {
    return Collections.unmodifiableCollection(dataCache.values());
  }

  private BeeView getView(String viewName) {
    Assert.notEmpty(viewName);
    BeeView view = getRegisteredView(viewName);

    if (BeeUtils.isEmpty(view) && isTable(viewName)) {
      view = getDefaultView(viewName, true);
      registerView(view);
    }
    return view;
  }

  private BeeRowSet getViewData(BeeView view) {
    if (BeeUtils.isEmpty(view) || view.isEmpty()) {
      return null;
    }
    String viewSource = view.getSource();
    if (!isTable(viewSource)) {
      return null;
    }
    Map<String, Map<String, String>> sources = new HashMap<String, Map<String, String>>();
    sources.put("", new HashMap<String, String>());
    sources.get("").put(viewSource, viewSource);

    SqlSelect ss = new SqlSelect().addFrom(viewSource);

    for (Entry<String, String> fldExpr : view.getFields().entrySet()) {
      String xpr = "";
      String fld = null;
      String dst = null;
      String src = viewSource;
      String als = sources.get(xpr).get(viewSource);
      boolean isError = false;

      for (String ff : fldExpr.getValue().split(BeeView.JOIN_MASK)) {
        if (isTable(dst)) {
          xpr = xpr + fld;

          if (!sources.containsKey(xpr)) {
            String tmpAls = SqlUtils.uniqueName();
            Map<String, String> tmpMap = new HashMap<String, String>();
            tmpMap.put(dst, tmpAls);
            sources.put(xpr, tmpMap);
            ss.addFromLeft(dst, tmpAls, SqlUtils.join(als, fld, tmpAls, getIdName(dst)));
          }
          src = dst;
          als = sources.get(xpr).get(dst);
        }
        fld = ff;
        BeeField field = getField(src, fld);

        if (BeeUtils.isEmpty(field)) {
          isError = true;
          break;
        }
        if (field.isExtended()) {
          String extAls = sources.get(xpr).get(fld);

          if (BeeUtils.isEmpty(extAls)) {
            extAls = joinExtField(ss, fld, src, als);
            sources.get(xpr).put(fld, extAls);

            if (BeeUtils.isEmpty(xpr)) {
              // TODO getTable(viewSource).appendExtLockName(ss, extAls);
            }
          }
          als = sources.get(xpr).get(fld);
        }
        dst = field.getRelation();
      }
      if (isError) {
        LogUtils.warning(logger, "Unknown field name:", fld, src, xpr);
        continue;
      }
      ss.addField(als, fld, fldExpr.getKey());
    }
    ss.addFields(viewSource, getLockName(viewSource), getIdName(viewSource));

    BeeRowSet res = qs.getData(ss);
    res.setViewName(view.getName());

    return res;
  }

  @Lock(LockType.WRITE)
  private void initTables() {
    String resource = RESOURCE_PATH + "structure.xml";

    List<BeeTable> tables = loadTables(resource);

    if (BeeUtils.isEmpty(tables)) {
      LogUtils.warning(logger, resource, "Nothing to load");
      return;
    }
    dataCache.clear();

    for (BeeTable table : tables) {
      String name = table.getName();

      if (table.isEmpty()) {
        LogUtils.warning(logger, resource, "Table", name, "has no fields defined");
      } else if (isRegisteredTable(name)) {
        LogUtils.warning(logger, resource, "Dublicate table name:", name);
      } else {
        registerTable(table);
      }
    }

    LogUtils.infoNow(logger,
        "Loaded", getTables().size(), "main tables descriptions from", resource);
  }

  @Lock(LockType.WRITE)
  private void initViews() {
    if (!isTable("Views")) {
      return;
    }
    BeeRowSet rs = qs.getData(new SqlSelect()
      .addFields("Views", "Name", "Source", "Field", "Alias")
      .addFrom("Views"));

    if (rs.isEmpty()) {
      LogUtils.warning(logger, "No views defined");
      return;
    }
    viewCache.clear();

    for (BeeRow row : rs.getRows()) {
      String viewName = row.getString("name");
      BeeView view = getRegisteredView(viewName);

      if (BeeUtils.isEmpty(view)) {
        view = new BeeView(viewName, row.getString("source"));
        registerView(view);
      }
      view.addField(row.getString("Field"), row.getString("Alias"));
    }
    LogUtils.infoNow(logger, "Loaded", viewCache.size(), "views descriptions");
  }

  private boolean isActive(String tableName) {
    return getRegisteredTable(tableName).isActive();
  }

  private boolean isRegisteredTable(String tableName) {
    return dataCache.containsKey(tableName);
  }

  @Lock(LockType.WRITE)
  private List<BeeTable> loadTables(String resource) {
    if (!FileUtils.isInputFile(resource)) {
      return null;
    }
    String error = null;
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    try {
      Schema schema = factory.newSchema(new StreamSource(RESOURCE_SCHEMA));
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(resource));
    } catch (SAXException e) {
      error = e.getMessage();
    } catch (IOException e) {
      error = e.getMessage();
    }
    if (!BeeUtils.isEmpty(error)) {
      LogUtils.severe(logger, resource, error);
      return null;
    }
    Document xml = XmlUtils.fromFileName(resource);
    if (BeeUtils.isEmpty(xml)) {
      return null;
    }
    List<BeeTable> data = new ArrayList<BeeTable>();
    Element root = xml.getDocumentElement();
    NodeList tables = root.getElementsByTagName("BeeTable");

    for (int i = 0; i < tables.getLength(); i++) {
      Element table = (Element) tables.item(i);

      BeeTable tbl = new BeeExtTable(table.getAttribute("name")
          , table.getAttribute("idName")
          , table.getAttribute("lockName"));

      NodeList nodeRoot = table.getElementsByTagName("BeeFields");

      if (nodeRoot.getLength() > 0) {
        NodeList fields = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeField");

        for (int j = 0; j < fields.getLength(); j++) {
          Element field = (Element) fields.item(j);

          String fldName = field.getAttribute("name");
          boolean notNull = BeeUtils.toBoolean(field.getAttribute("notNull"));
          boolean unique = BeeUtils.toBoolean(field.getAttribute("unique"));
          String relation = field.getAttribute("relation");
          boolean cascade = BeeUtils.toBoolean(field.getAttribute("cascade"));

          tbl.addField(fldName
              , DataTypes.valueOf(field.getAttribute("type"))
              , BeeUtils.toInt(field.getAttribute("precision"))
              , BeeUtils.toInt(field.getAttribute("scale"))
              , notNull, unique, relation, cascade);

          if (unique) {
            tbl.addKey(true, fldName);
          }
        }
        NodeList keys = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeKey");

        for (int j = 0; j < keys.getLength(); j++) {
          Element key = (Element) keys.item(j);

          tbl.addKey(BeeUtils.toBoolean(key.getAttribute("unique")),
              key.getAttribute("fields").split(","));
        }
      }
      nodeRoot = table.getElementsByTagName("BeeExtended");

      if (nodeRoot.getLength() > 0) {
        NodeList extFields = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeField");

        for (int j = 0; j < extFields.getLength(); j++) {
          Element field = (Element) extFields.item(j);

          String fldName = field.getAttribute("name");
          boolean notNull = BeeUtils.toBoolean(field.getAttribute("notNull"));
          boolean unique = BeeUtils.toBoolean(field.getAttribute("unique"));
          String relation = field.getAttribute("relation");
          boolean cascade = BeeUtils.toBoolean(field.getAttribute("cascade"));

          tbl.addExtField(fldName
              , DataTypes.valueOf(field.getAttribute("type"))
              , BeeUtils.toInt(field.getAttribute("precision"))
              , BeeUtils.toInt(field.getAttribute("scale"))
              , notNull, unique, relation, cascade);

          if (unique) {
            tbl.addExtKey(true, fldName);
          }
        }
        NodeList keys = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeKey");

        for (int j = 0; j < keys.getLength(); j++) {
          Element key = (Element) keys.item(j);

          tbl.addExtKey(BeeUtils.toBoolean(key.getAttribute("unique")),
              key.getAttribute("fields").split(","));
        }
      }
      nodeRoot = table.getElementsByTagName("BeeStates");

      if (nodeRoot.getLength() > 0) {
        NodeList states = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeState");

        for (int j = 0; j < states.getLength(); j++) {
          Element state = (Element) states.item(j);

          tbl.addState(state.getAttribute("name")
              , BeeUtils.toBoolean(state.getAttribute("userMode"))
              , BeeUtils.toBoolean(state.getAttribute("roleMode"))
              , BeeUtils.toBoolean(state.getAttribute("forced")));
        }
      }
      data.add(tbl);
    }
    return data;
  }

  private void rebuildTable(BeeTable table, boolean exists) {
    if (!BeeUtils.isEmpty(table)) {
      String tblName = table.getName();
      List<BeeForeignKey> fKeys = new ArrayList<BeeForeignKey>();

      for (BeeTable tbl : getTables()) {
        for (BeeForeignKey key : tbl.getForeignKeys()) {
          if (BeeUtils.same(key.getRefTable(), tblName)) {
            String keyOwner = key.getTable();

            if (exists && qs.dbForeignKeys(keyOwner).contains(key.getName())) {
              qs.updateData(SqlUtils.dropForeignKey(keyOwner, key.getName()));
              fKeys.add(key);
            }
            if (!BeeUtils.same(tbl.getName(), tblName)) {
              fKeys.add(key);
            }
          }
        }
      }
      String tmp = null;
      if (exists) {
        tmp = backupTable(tblName);
        qs.updateData(SqlUtils.dropTable(tblName));
      }
      createTable(table);
      restoreTable(tblName, tmp);
      createKeys(table.getKeys());
      createForeignKeys(table.getForeignKeys());
      createForeignKeys(fKeys);
    }
  }

  private void registerTable(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      dataCache.put(table.getName(), table);
      registerTable(table.getStateTable());
    }
  }

  private void registerView(BeeView view) {
    if (!BeeUtils.isEmpty(view)) {
      viewCache.put(view.getName(), view);
    }
  }
}
