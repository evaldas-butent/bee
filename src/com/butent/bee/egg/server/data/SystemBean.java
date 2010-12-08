package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.egg.server.data.BeeTable.BeeKey;
import com.butent.bee.egg.server.data.BeeTable.BeeStructure;
import com.butent.bee.egg.server.utils.FileUtils;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
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

  public BeeStructure getExtField(String table, String extField) {
    BeeTable extTable = getTable(table).getExtTable();

    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getField(extField);
    }
    return null;
  }

  public Collection<BeeStructure> getExtFields(String table) {
    BeeTable extTable = getTable(table).getExtTable();

    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getFields();
    }
    return null;
  }

  public BeeTable getExtTable(String table) {
    return getTable(table).getExtTable();
  }

  public BeeStructure getField(String table, String field) {
    return getTable(table).getField(field);
  }

  public Set<String> getFieldNames(String table) {
    Set<String> names = new HashSet<String>();

    for (BeeStructure field : getFields(table)) {
      names.add(field.getName());
    }
    return names;
  }

  public Collection<BeeStructure> getFields(String table) {
    return getTable(table).getFields();
  }

  public String getIdName(String table) {
    return getTable(table).getIdName();
  }

  public String getLockName(String table) {
    return getTable(table).getLockName();
  }

  public BeeTable getTable(String table) {
    Assert.state(isTable(table), "Not a base table: " + table);
    return dataCache.get(table);
  }

  public Set<String> getTableNames() {
    return Collections.unmodifiableSet(dataCache.keySet());
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

    for (BeeTable extension : extensions) {
      String extName = extension.getName();
      BeeTable table = getTable(extName);

      if (BeeUtils.isEmpty(table) || table.isCustom()) {
        if (extension.isEmpty()) {
          LogUtils.warning(logger, resource, "Table", extName, "has no fields defined");
          continue;
        }
        extension.setCustom(true);
        dataCache.put(extName, extension);
        cTbl++;
      } else {
        String error = null;

        for (BeeStructure fld : extension.getFields()) {
          if (table.isField(fld.getName())) {
            error = "Dublicate field name: " + table.getName() + " " + fld.getName();
            break;
          }
        }
        if (!BeeUtils.isEmpty(error)) {
          LogUtils.severe(logger, resource, error);
          continue;
        }
        table.setExtTable(extension);
        cFld += extension.getFields().size();
      }
      upd.add(extension);
    }
    for (BeeTable extension : upd) {
      initConstraints(extension);
    }
    LogUtils.infoNow(logger, "Loaded", cTbl, "new tables and", cFld,
        "new fields descriptions from", resource);
  }

  public boolean isField(String table, String field) {
    return getTable(table).isField(field);
  }

  public boolean isTable(String source) {
    return !BeeUtils.isEmpty(source) && getTableNames().contains(source);
  }

  public void rebuildTable(BeeTable table, boolean rebuildForeign) {
    if (!BeeUtils.isEmpty(table)) {
      String tblName = table.getName();
      List<BeeForeignKey> fk = new ArrayList<BeeForeignKey>();
      List<BeeTable> tables = new ArrayList<BeeTable>();

      for (String tbl : getTableNames()) {
        BeeTable base = getTable(tbl);

        if (BeeUtils.same(base.getName(), tblName)) {
          rebuildTable(table.getExtTable(), false);
        } else {
          tables.add(base);
          if (!BeeUtils.isEmpty(base.getExtTable())) {
            tables.add(base.getExtTable());
          }
        }
      }
      for (BeeTable tbl : tables) {
        for (BeeForeignKey key : tbl.getForeignKeys()) {
          if (BeeUtils.same(key.getRefTable(), tblName)) {
            if (qs.dbForeignKeys(tbl.getName()).contains(key.getName())) {
              qs.updateData(SqlUtils.dropForeignKey(tbl.getName(), key.getName()));
            }
            fk.add(key);
          }
        }
      }
      String tmp = null;

      if (qs.isDbTable(tblName)) {
        tmp = backupTable(tblName);
        qs.updateData(SqlUtils.dropTable(tblName));
      }
      createTable(table);
      restoreTable(table, tmp);
      createKeys(table);

      if (rebuildForeign) {
        createForeignKeys(table);

        for (BeeForeignKey key : fk) {
          qs.updateData(SqlUtils.createForeignKey(key.getTable(), key.getName(),
              key.getKeyField(), key.getRefTable(), key.getRefField(), key.getAction()));
        }
      }
    }
  }

  @Lock(LockType.WRITE)
  public void rebuildTables(ResponseBuffer buff) {
    for (BeeTable tbl : dataCache.values()) {
      rebuildTable(tbl, false);
    }
    for (BeeTable tbl : dataCache.values()) {
      createForeignKeys(tbl);
    }
    buff.add("Recreate structure OK");
  }

  public int restoreTable(BeeTable table, String tmp) {
    int rc = 0;

    if (!BeeUtils.isEmpty(tmp)) {
      Collection<String> tmpFields = qs.dbFields(tmp);
      List<String> fldList = new ArrayList<String>();

      for (BeeStructure fld : table.getFields()) {
        if (tmpFields.contains(fld.getName())) {
          fldList.add(fld.getName());
        }
      }
      if (tmpFields.contains(table.getLockName())) {
        fldList.add(table.getLockName());
      }
      if (tmpFields.contains(table.getIdName())) {
        fldList.add(table.getIdName());
      }
      if (!BeeUtils.isEmpty(fldList)) {
        String[] flds = fldList.toArray(new String[0]);

        rc = qs.updateData(new SqlInsert(table.getName())
          .addFields(flds)
          .setSource(new SqlSelect().addFields(tmp, flds).addFrom(tmp)));
      }
      qs.updateData(SqlUtils.dropTable(tmp));
    }
    return rc;
  }

  private void createForeignKeys(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      for (BeeForeignKey key : table.getForeignKeys()) {
        qs.updateData(SqlUtils.createForeignKey(key.getTable(), key.getName(),
            key.getKeyField(), key.getRefTable(), key.getRefField(), key.getAction()));
      }
      createForeignKeys(table.getExtTable());
    }
  }

  private void createKeys(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      String tbl = table.getName();
      IsQuery index;

      for (BeeKey key : table.getKeys()) {
        if (key.isPrimary()) {
          index = SqlUtils.createPrimaryKey(tbl, key.getName(), key.getKeyFields());
        } else if (key.isUnique()) {
          index = SqlUtils.createUniqueIndex(tbl, key.getName(), key.getKeyFields());
        } else {
          index = SqlUtils.createIndex(tbl, key.getName(), key.getKeyFields());
        }
        qs.updateData(index);
      }
    }
  }

  private void createTable(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      SqlCreate sc = new SqlCreate(table.getName(), false);

      for (BeeStructure field : table.getFields()) {
        sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
            field.isNotNull() ? Keywords.NOT_NULL : null);
      }
      sc.addLong(table.getLockName(), Keywords.NOT_NULL);
      sc.addLong(table.getIdName(), Keywords.NOT_NULL);
      qs.updateData(sc);

      if (!BeeUtils.isEmpty(table.getStateTable())) {
        sc = new SqlCreate(table.getStateTable())
          .addString("State", 10, Keywords.NOT_NULL)
          .addLong(table.getIdName(), Keywords.NOT_NULL);
      }
    }
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    initTables();
    initExtensions();
  }

  private void initConstraints(BeeTable table) {
    table.addPrimaryKey(table.getIdName());

    if (!BeeUtils.isEmpty(table.getOwner())) {
      BeeTable owner = table.getOwner();
      table.addForeignKey(table.getIdName(), owner.getName(), owner.getIdName(), Keywords.CASCADE);
    }
    for (BeeStructure fld : table.getFields()) {
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
  }

  @Lock(LockType.WRITE)
  private void initTables() {
    String resource = RESOURCE_PATH + "structure.xml";

    List<BeeTable> tables = loadTables(resource);

    if (BeeUtils.isEmpty(tables)) {
      LogUtils.warning(logger, resource, "Nothing to load");
      return;
    }
    List<BeeTable> upd = new ArrayList<BeeTable>();
    dataCache.clear();

    for (BeeTable table : tables) {
      String name = table.getName();

      if (table.isEmpty()) {
        LogUtils.warning(logger, resource, "Table", name, "has no fields defined");
      } else if (isTable(name)) {
        LogUtils.warning(logger, resource, "Dublicate table name:", name);
      } else {
        dataCache.put(name, table);
        upd.add(table);
      }
    }
    for (BeeTable table : upd) {
      initConstraints(table);
    }
    LogUtils.infoNow(logger, "Loaded", dataCache.size(), "main tables descriptions from", resource);
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

      String name = table.getAttribute("name");
      String idName = table.getAttribute("idName");
      String lockName = table.getAttribute("lockName");

      BeeTable tbl = new BeeTable(name, idName, lockName);

      NodeList nodeRoot = table.getElementsByTagName("BeeFields");
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
          tbl.addUniqueKey(fldName);
        }
      }
      nodeRoot = table.getElementsByTagName("BeeKeys");

      if (nodeRoot.getLength() > 0) {
        NodeList keys = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeKey");

        for (int j = 0; j < keys.getLength(); j++) {
          Element key = (Element) keys.item(j);

          boolean unique = BeeUtils.toBoolean(key.getAttribute("unique"));
          String[] flds = BeeUtils.split(key.getAttribute("fields"), ",");

          if (unique) {
            tbl.addUniqueKey(key.getAttribute("name"), flds);
          } else {
            tbl.addKey(key.getAttribute("name"), flds);
          }
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
}
