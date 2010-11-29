package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.egg.server.data.BeeTable.BeeKey;
import com.butent.bee.egg.server.data.BeeTable.BeeStructure;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
@Lock(LockType.READ)
public class SystemBean {

  private static Logger logger = Logger.getLogger(SystemBean.class.getName());

  @EJB
  QueryServiceBean qs;

  private Map<String, BeeTable> dataCache = new LinkedHashMap<String, BeeTable>();

  @Lock(LockType.WRITE)
  public void createAll(ResponseBuffer buff) {
    rebuildTables(buff);
    rebuildKeys(buff);
    createData(buff);
  }

  @Lock(LockType.WRITE)
  public void createData(ResponseBuffer buff) {
    BeeTable tables = dataCache.get("bee_Tables");

    for (BeeTable tbl : dataCache.values()) {
      SqlInsert si = new SqlInsert(tables.getName());
      Iterator<BeeStructure> it = tables.getFields().iterator();

      si.addConstant(it.next().getName(), tbl.getName())
        .addConstant(it.next().getName(), tbl.getLockName())
        .addConstant(it.next().getName(), tbl.getIdName());

      long tableId = qs.insertData(si);

      BeeTable propert = dataCache.get("bee_Fields");

      for (BeeStructure fld : tbl.getFields()) {
        si = new SqlInsert(propert.getName());
        it = propert.getFields().iterator();

        si.addConstant(it.next().getName(), fld.getName())
          .addConstant(it.next().getName(), fld.getType().name())
          .addConstant(it.next().getName(), fld.getPrecision())
          .addConstant(it.next().getName(), fld.getScale())
          .addConstant(it.next().getName(), BeeUtils.toInt(fld.isNotNull()))
          .addConstant(it.next().getName(), BeeUtils.toInt(fld.isUnique()));

        si.addConstant("Table", tableId);

        qs.insertData(si);
      }
    }
    buff.add("Recreate data OK");
  }

  public BeeStructure getExtField(String table, String extField) {
    BeeTable extTable = dataCache.get(table).getExtTable();

    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getField(extField);
    }
    return null;
  }

  public Collection<BeeStructure> getExtFields(String table) {
    BeeTable extTable = dataCache.get(table).getExtTable();

    if (!BeeUtils.isEmpty(extTable)) {
      return extTable.getFields();
    }
    return null;
  }

  public BeeTable getExtTable(String table) {
    return dataCache.get(table).getExtTable();
  }

  public BeeStructure getField(String table, String field) {
    return dataCache.get(table).getField(field);
  }

  public Set<String> getFieldNames(String table) {
    Set<String> names = new HashSet<String>();

    for (BeeStructure field : getFields(table)) {
      names.add(field.getName());
    }
    return names;
  }

  public Collection<BeeStructure> getFields(String table) {
    return dataCache.get(table).getFields();
  }

  public String getIdName(String table) {
    return dataCache.get(table).getIdName();
  }

  public Collection<BeeKey> getKeys(String table) {
    return dataCache.get(table).getKeys();
  }

  public String getLockName(String table) {
    return dataCache.get(table).getLockName();
  }

  public BeeTable getTable(String tbl) {
    return dataCache.get(tbl);
  }

  public Set<String> getTableNames() {
    return Collections.unmodifiableSet(dataCache.keySet());
  }

  public boolean isField(String table, String field) {
    return dataCache.get(table).isField(field);
  }

  public boolean isTable(String source) {
    return !BeeUtils.isEmpty(source) && getTableNames().contains(source);
  }

  @Lock(LockType.WRITE)
  public void rebuildKeys(ResponseBuffer buff) {
    for (BeeTable tbl : dataCache.values()) {
      createKeys(tbl);
    }
    for (BeeTable tbl : dataCache.values()) {
      createForeignKeys(tbl);
    }
    buff.add("Recreate keys OK");
  }

  @Lock(LockType.WRITE)
  public void rebuildTables(ResponseBuffer buff) {
    dropTables();

    for (BeeTable tbl : dataCache.values()) {
      createTable(tbl);
    }
    buff.add("Recreate structure OK");
  }

  private void createForeignKeys(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      for (BeeForeignKey key : table.getForeignKeys()) {
        IsQuery index = SqlUtils.createForeignKey(key.getTable(), key.getName(),
            key.getKeyField(), key.getRefTable(), key.getRefField(), key.getAction());
        qs.updateData(index);
      }
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
      SqlCreate sc = new SqlCreate(table.getName());

      for (BeeStructure field : table.getFields()) {
        sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
            field.isNotNull() ? Keywords.NOT_NULL : null);
      }
      sc.addLong(table.getLockName(), Keywords.NOT_NULL);
      sc.addLong(table.getIdName(), Keywords.NOT_NULL);
      qs.updateData(sc);
    }
  }

  private void dropTables() {
    for (String tbl : getTableNames()) {
      Set<String> dbForeignKeys = qs.dbForeignKeys(tbl);

      if (!BeeUtils.isEmpty(dbForeignKeys)) {
        for (String key : dbForeignKeys) {
          IsQuery drop = SqlUtils.dropForeignKey(tbl, key);
          qs.updateData(drop);
        }
      }
    }
    for (String tbl : getTableNames()) {
      if (qs.isDbTable(tbl)) {
        IsQuery drop = SqlUtils.dropTable(tbl);
        qs.updateData(drop);
      }
    }
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    initData();
  }

  @Lock(LockType.WRITE)
  private void initData() {
    String path = getClass().getResource("/../classes").toString().replaceFirst("classes/$", "");
    String resource = "config/structure.xml";

    URL xmlUrl = getClass().getResource("/../" + resource);

    if (xmlUrl == null) {
      LogUtils.warning(logger, "Resource", path + resource, "not found");
      return;
    }
    Document xml = XmlUtils.fromFileName(xmlUrl.getPath());
    if (BeeUtils.isEmpty(xml)) {
      return;
    }
    int c = 0;
    NodeList root = xml.getElementsByTagName("BeeTables");

    if (!BeeUtils.isEmpty(root.getLength())) {
      NodeList tables = ((Element) root.item(0)).getElementsByTagName("BeeTable");

      for (int i = 0; i < tables.getLength(); i++) {
        Element table = (Element) tables.item(i);

        String name = table.getAttribute("name");
        String idName = table.getAttribute("idName");
        String lockName = table.getAttribute("lockName");

        BeeTable tbl = new BeeTable(name, idName, lockName);
        initStructure(tbl, table.getElementsByTagName("BeeStructure"));

        if (tbl.isEmpty()) {
          LogUtils.warning(logger, "Table", name, "has no fields defined");
        } else {
          c++;
          dataCache.put(name, tbl);

          BeeTable extTbl = new BeeTable(name + BeeTable.EXT_TABLE_SUFFIX,
              name + tbl.getIdName(), name + tbl.getLockName());
          initStructure(extTbl, table.getElementsByTagName("BeeExtended"));

          if (!extTbl.isEmpty()) {
            extTbl.addForeignKey(extTbl.getIdName(), name, tbl.getIdName(), Keywords.CASCADE);
            tbl.setExtTable(extTbl);
            c++;
            dataCache.put(extTbl.getName(), extTbl);
          }
        }
      }
      initRelations();
    } else {
      LogUtils.warning(logger, "Node <BeeTables> is not found");
    }
    LogUtils.infoNow(logger, "Loaded", c, "tables descriptions from", path + resource);
  }

  private void initRelations() {
    for (BeeTable tbl : dataCache.values()) {
      for (BeeStructure fld : tbl.getFields()) {
        String relTable = fld.getRelation();

        if (!BeeUtils.isEmpty(relTable)) {
          tbl.addForeignKey(fld.getName(), relTable, getIdName(relTable),
              fld.isCascade() ? (fld.isNotNull() ? Keywords.CASCADE : Keywords.SET_NULL) : null);
        }
      }
    }
  }

  private int initStructure(BeeTable table, NodeList node) {
    int c = 0;

    if (!BeeUtils.isEmpty(node.getLength())) {
      NodeList fields = ((Element) node.item(0)).getElementsByTagName("BeeField");
      c = fields.getLength();

      for (int i = 0; i < c; i++) {
        Element field = (Element) fields.item(i);

        String name = field.getAttribute("name");
        boolean unique = BeeUtils.toBoolean(field.getAttribute("unique"));

        table.addField(name,
            DataTypes.valueOf(field.getAttribute("type")),
            BeeUtils.toInt(field.getAttribute("precision")),
            BeeUtils.toInt(field.getAttribute("scale")),
            BeeUtils.toBoolean(field.getAttribute("notNull")),
            unique, field.getAttribute("relation"),
            BeeUtils.toBoolean(field.getAttribute("cascade")));

        if (unique) {
          table.addUniqueKey(name);
        }
      }
      if (!table.isEmpty()) {
        NodeList keys = ((Element) node.item(0)).getElementsByTagName("BeeKey");

        for (int i = 0; i < keys.getLength(); i++) {
          Element key = (Element) keys.item(i);

          boolean unique = BeeUtils.toBoolean(key.getAttribute("unique"));
          String[] flds = BeeUtils.split(key.getAttribute("fields"), ",");

          if (unique) {
            table.addUniqueKey(key.getAttribute("name"), flds);
          } else {
            table.addKey(key.getAttribute("name"), flds);
          }
        }
        table.addPrimaryKey(table.getIdName());
      }
    }
    return c;
  }
}
