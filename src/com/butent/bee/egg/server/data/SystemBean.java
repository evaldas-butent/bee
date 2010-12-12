package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.data.BeeTable.BeeField;
import com.butent.bee.egg.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.egg.server.data.BeeTable.BeeKey;
import com.butent.bee.egg.server.data.BeeTable.BeeState;
import com.butent.bee.egg.server.utils.FileUtils;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.BeeColumn;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeRowSet.BeeRow;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.HasFrom;
import com.butent.bee.egg.shared.sql.IsExpression;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  private Map<String, BeeTable> dataCache = new HashMap<String, BeeTable>();

  public String addExtJoin(HasFrom<?> query, String table, String alias) {
    String extTable = getExtName(table);

    if (BeeUtils.isEmpty(extTable)) {
      return null;
    }
    String extAlias = extTable;

    if (BeeUtils.isEmpty(alias) || BeeUtils.same(alias, table)) {
      query.addFromLeft(extTable,
          SqlUtils.join(table, getIdName(table), extTable, getExtIdName(table)));
    } else {
      extAlias = SqlUtils.uniqueName();
      query.addFromLeft(extTable, extAlias,
          SqlUtils.join(alias, getIdName(table), extAlias, getExtIdName(table)));
    }
    return extAlias;
  }

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

  public BeeField getExtField(String table, String extField) {
    BeeTable extTable = getExtTable(table);

    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getField(extField);
    }
    return null;
  }

  public Collection<BeeField> getExtFields(String table) {
    BeeTable extTable = getExtTable(table);

    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getFields();
    }
    return new ArrayList<BeeField>();
  }

  public String getExtIdName(String table) {
    BeeTable extTable = getExtTable(table);

    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getIdName();
    }
    return null;
  }

  public String getExtLockName(String table) {
    BeeTable extTable = getExtTable(table);

    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getLockName();
    }
    return null;
  }

  public String getExtName(String table) {
    BeeTable extTable = getExtTable(table);

    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getName();
    }
    return null;
  }

  public BeeField getField(String tbl, String fld) {
    BeeField field = getTable(tbl).getField(fld);

    if (BeeUtils.isEmpty(field)) {
      field = getExtField(tbl, fld);
    }
    return field;
  }

  public Collection<String> getFieldNames(String table) {
    Set<String> names = new HashSet<String>();

    for (BeeField field : getFields(table)) {
      names.add(field.getName());
    }
    return names;
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

  public BeeRowSet getView(String viewSource) {
    Assert.notEmpty(viewSource);
    List<String> viewFields = null;

    BeeRowSet rs = qs.getData(new SqlSelect()
      .addFields("Views", "Field")
      .addFrom("Views")
      .setWhere(SqlUtils.equal("Views", "View", viewSource)));

    if (rs.isEmpty()) {
      viewFields = getViewFields(viewSource, null);
    } else {
      viewFields = new ArrayList<String>(rs.getRowCount());

      for (BeeRow row : rs.getRows()) {
        viewFields.add(row.getString("Field"));
      }
    }
    return getViewData(viewSource, viewFields);
  }

  public boolean hasField(String table, String field) {
    return getTable(table).hasField(field);
  }

  @Lock(LockType.WRITE)
  public void initExtensions() {
    String resource = RESOURCE_PATH + "extensions.xml";

    List<BeeTable> extensions = loadTables(resource);

    if (BeeUtils.isEmpty(extensions)) {
      return;
    }
    List<BeeTable> upd = new ArrayList<BeeTable>();
    int cTbl = 0;
    int cFld = 0;
    int cKey = 0;
    int cStt = 0;

    for (BeeTable extension : extensions) {
      String extName = extension.getName();

      if (!isTable(extName) || getTable(extName).isCustom()) {
        if (extension.isEmpty()) {
          LogUtils.warning(logger, resource, "Table", extName, "has no fields defined");
          continue;
        }
        extension.makeCustom();
        dataCache.put(extName, extension);
        cTbl++;
        upd.add(extension);
      } else {
        BeeTable table = getTable(extName);

        if (extension.hasFields()) {
          LogUtils.warning(logger, resource, "Table", extName, "can't define base fields");
        }
        for (BeeKey key : extension.getKeys()) {
          table.addKey(key.isUnique(), key.getKeyFields());
          cKey++;
        }
        for (BeeField fld : extension.getExtFields()) {
          table.addExtField(fld.getName(), fld.getType(), fld.getPrecision(), fld.getScale(),
              fld.isNotNull(), fld.isUnique(), fld.getRelation(), fld.isCascade());
          cFld++;
        }
        for (BeeKey key : extension.getExtKeys()) {
          table.addExtKey(key.isUnique(), key.getKeyFields());
          cKey++;
        }
        for (BeeState state : extension.getStates()) {
          table.addState(state.getName(), state.hasUserMode(), state.hasRoleMode(),
              state.isForced());
          cStt++;
        }
        upd.add(table.getExtTable());
        upd.add(table.getStateTable());
      }
    }
    for (BeeTable extension : upd) {
      initConstraints(extension);
    }
    LogUtils.infoNow(logger, "Loaded", cTbl, "new tables,", cFld,
        "new fields,", cKey, "new keys", cStt, "new states descriptions from", resource);
  }

  public boolean isTable(String source) {
    return !BeeUtils.isEmpty(source) && getTableNames().contains(source);
  }

  public void rebuildTable(String table, boolean rebuildForeign) {
    rebuildTable(getTable(table), rebuildForeign);
  }

  @Lock(LockType.WRITE)
  public void rebuildTables(ResponseBuffer buff) {
    for (BeeTable tbl : getTables()) {
      rebuildTable(tbl, false);
    }
    for (BeeTable tbl : getTables()) {
      createForeignKeys(tbl.getForeignKeys());
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

  BeeTable getExtTable(String table) {
    return getTable(table).getExtTable();
  }

  BeeTable getTable(String table) {
    Assert.state(isTable(table), "Not a base table: " + table);
    return dataCache.get(table);
  }

  Collection<BeeTable> getTables() {
    return Collections.unmodifiableCollection(dataCache.values());
  }

  private void createForeignKeys(Collection<BeeForeignKey> fKeys) {
    for (BeeForeignKey key : fKeys) {
      qs.updateData(SqlUtils.createForeignKey(key.getTable(), key.getName(),
            key.getKeyField(), key.getRefTable(), key.getRefField(), key.getAction()));
    }
  }

  private void createKeys(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      IsQuery index;

      for (BeeKey key : table.getKeys()) {
        if (key.isPrimary()) {
          index = SqlUtils.createPrimaryKey(key.getTable(), key.getName(), key.getKeyFields());
        } else if (key.isUnique()) {
          index = SqlUtils.createUniqueIndex(key.getTable(), key.getName(), key.getKeyFields());
        } else {
          index = SqlUtils.createIndex(key.getTable(), key.getName(), key.getKeyFields());
        }
        qs.updateData(index);
      }
    }
  }

  private void createTable(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      SqlCreate sc = new SqlCreate(table.getName(), false);

      for (BeeField field : table.getFields()) {
        sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
            field.isNotNull() ? Keywords.NOT_NULL : null);
      }
      sc.addLong(table.getLockName(), Keywords.NOT_NULL);
      sc.addLong(table.getIdName(), Keywords.NOT_NULL);
      qs.updateData(sc);
    }
  }

  private BeeRowSet getViewData(String viewSource, List<String> viewFields) {
    Assert.notEmpty(viewFields);
    LogUtils.warning(logger, viewFields);

    Map<String, String[]> sources = new HashMap<String, String[]>();
    sources.put(null, new String[]{viewSource, viewSource, null});

    SqlSelect ss = new SqlSelect().addFrom(viewSource);

    for (String fldExpr : viewFields) {
      String xpr = null;
      String fld = null;
      String dst = null;
      String src = sources.get(xpr)[0];
      String als = sources.get(xpr)[1];
      boolean isError = false;

      for (String ff : fldExpr.split(":")) {
        if (!BeeUtils.isEmpty(dst)) {
          xpr = BeeUtils.concat(":", xpr, fld);

          if (!sources.containsKey(xpr)) {
            String tmpAls = SqlUtils.uniqueName();
            sources.put(xpr, new String[]{dst, tmpAls, null});
            LogUtils.warning(logger, xpr, (Object[]) sources.get(xpr));
            ss.addFromLeft(dst, tmpAls, SqlUtils.join(als, fld, tmpAls, getIdName(dst)));
          }
          src = sources.get(xpr)[0];
          als = sources.get(xpr)[1];
        }
        fld = ff;
        BeeField field = getField(src, fld);
        if (BeeUtils.isEmpty(field)) {
          isError = true;
          break;
        }
        if (!BeeUtils.same(field.getTable(), src)) {
          if (BeeUtils.isEmpty(sources.get(xpr)[2])) {
            sources.get(xpr)[2] = addExtJoin(ss, src, als);
            LogUtils.warning(logger, xpr, (Object[]) sources.get(xpr));

            if (BeeUtils.isEmpty(xpr)) {
              ss.addFields(sources.get(xpr)[2], getExtLockName(viewSource));
            }
          }
          als = sources.get(xpr)[2];
        }
        dst = field.getRelation();
      }
      if (isError) {
        LogUtils.warning(logger, "Unknown field name:", fld, src, xpr);
        continue;
      }
      if (BeeUtils.isEmpty(xpr)) {
        ss.addFields(als, fld);
      } else {
        ss.addField(als, fld, xpr.replace(":", "") + fld);
      }
    }
    ss.addFields(viewSource, getLockName(viewSource), getIdName(viewSource));
    BeeRowSet res = qs.getData(ss);

    for (IsExpression[] pair : ss.getFields()) {
      String xpr = pair[0].getValue();
      IsExpression als = pair[1];

      int idx = xpr.lastIndexOf(".");
      if (idx <= 0) {
        continue;
      }
      String tbl = BeeUtils.left(xpr, idx);
      String fld = xpr.substring(idx + 1);

      if (BeeUtils.inListSame(tbl, viewSource, getExtName(viewSource))) {
        BeeColumn col = res.getColumn(BeeUtils.isEmpty(als) ? fld : als.getValue());
        col.setFieldSource(viewSource);
        col.setFieldName(fld);
      }
    }
    res.setSource(viewSource);

    return res;
  }

  private List<String> getViewFields(String table, String field) {
    List<String> fieldList = new ArrayList<String>();

    if (!isTable(table)) {
      return fieldList;
    }
    for (BeeField fld : getTable(table).getAllFields()) {
      if (BeeUtils.isEmpty(field) || fld.isUnique()) {
        String fldName = BeeUtils.concat(":", field, fld.getName());
        fieldList.add(fldName);

        if (!BeeUtils.isEmpty(fld.getRelation()) && !fldName.contains(fld.getName() + ":")) {
          fieldList.addAll(getViewFields(fld.getRelation(), fldName));
        }
      }
    }
    return fieldList;
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    initTables();
    initExtensions();
  }

  private void initConstraints(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      table.addPrimaryKey(table.getIdName());

      for (BeeField fld : table.getAllFields()) {
        String relTable = fld.getRelation();

        if (!BeeUtils.isEmpty(relTable)) {
          if (isTable(relTable)) {
            table.addForeignKey(fld.getName(), relTable, getIdName(relTable),
                fld.isCascade() ? (fld.isNotNull() ? Keywords.CASCADE : Keywords.SET_NULL) : null);
          } else {
            LogUtils.warning(logger, "Unknown relation:", relTable);
          }
        }
      }
      initConstraints(table.getExtTable());
      initConstraints(table.getStateTable());
    }
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
      } else if (isTable(name)) {
        LogUtils.warning(logger, resource, "Dublicate table name:", name);
      } else {
        dataCache.put(name, table);
      }
    }
    for (BeeTable table : getTables()) {
      initConstraints(table);
    }
    LogUtils.infoNow(logger,
        "Loaded", getTables().size(), "main tables descriptions from", resource);
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

      BeeTable tbl = new BeeTable(table.getAttribute("name")
          , table.getAttribute("idName")
          , table.getAttribute("lockName"));

      NodeList nodeRoot = table.getElementsByTagName("BeeFields");

      if (nodeRoot.getLength() > 0) {
        NodeList fields = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeField");

        for (int j = 0; j < fields.getLength(); j++) {
          Element field = (Element) fields.item(j);

          String fldName = field.getAttribute("name");
          boolean unique = BeeUtils.toBoolean(field.getAttribute("unique"));

          tbl.addField(fldName
              , DataTypes.valueOf(field.getAttribute("type"))
              , BeeUtils.toInt(field.getAttribute("precision"))
              , BeeUtils.toInt(field.getAttribute("scale"))
              , BeeUtils.toBoolean(field.getAttribute("notNull"))
              , unique, field.getAttribute("relation")
              , BeeUtils.toBoolean(field.getAttribute("cascade")));

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
          boolean unique = BeeUtils.toBoolean(field.getAttribute("unique"));

          tbl.addExtField(fldName
              , DataTypes.valueOf(field.getAttribute("type"))
              , BeeUtils.toInt(field.getAttribute("precision"))
              , BeeUtils.toInt(field.getAttribute("scale"))
              , BeeUtils.toBoolean(field.getAttribute("notNull"))
              , unique, field.getAttribute("relation")
              , BeeUtils.toBoolean(field.getAttribute("cascade")));

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

  private void rebuildTable(BeeTable table, boolean rebuildForeign) {
    if (!BeeUtils.isEmpty(table)) {
      String tblName = table.getName();
      List<BeeForeignKey> fKeys = new ArrayList<BeeForeignKey>();

      for (BeeTable tbl : getTables()) {
        if (BeeUtils.same(tbl.getName(), tblName)) {
          rebuildTable(table.getExtTable(), false);
          rebuildTable(table.getStateTable(), false);
        } else {
          for (BeeForeignKey key : tbl.getForeignKeys()) {
            if (BeeUtils.same(key.getRefTable(), tblName)) {
              if (qs.dbForeignKeys(key.getTable()).contains(key.getName())) {
                qs.updateData(SqlUtils.dropForeignKey(key.getTable(), key.getName()));
              }
              fKeys.add(key);
            }
          }
        }
      }
      String tmp = null;

      if (qs.isDbTable(tblName)) {
        tmp = backupTable(tblName);
        qs.updateData(SqlUtils.dropTable(tblName));
      }
      createTable(table);
      restoreTable(tblName, tmp);
      createKeys(table);

      if (rebuildForeign) {
        createForeignKeys(table.getForeignKeys());
        createForeignKeys(fKeys);
      }
    }
  }
}
