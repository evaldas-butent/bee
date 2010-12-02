package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.egg.server.data.BeeTable.BeeKey;
import com.butent.bee.egg.server.data.BeeTable.BeeStructure;
import com.butent.bee.egg.server.utils.FileUtils;
import com.butent.bee.egg.server.utils.XmlUtils;
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

@Singleton
@Startup
@Lock(LockType.READ)
public class SystemBean {
  private static final Class<?> CLASS = SystemBean.class;
  public static final String RESOURCE_PATH = CLASS.getResource(CLASS.getSimpleName() + ".class").getPath()
    .replace(CLASS.getName().replace('.', '/') + ".class", "../config/");

  private static Logger logger = Logger.getLogger(SystemBean.class.getName());

  @EJB
  QueryServiceBean qs;

  private Map<String, BeeTable> dataCache = new HashMap<String, BeeTable>();

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

  public String getLockName(String table) {
    return dataCache.get(table).getLockName();
  }

  public BeeTable getTable(String tbl) {
    return dataCache.get(tbl);
  }

  public Set<String> getTableNames() {
    return Collections.unmodifiableSet(dataCache.keySet());
  }

  @Lock(LockType.WRITE)
  public void initExtensions() {
    String resource = RESOURCE_PATH + "extensions.xml";

    List<BeeTable> extensions = loadTables(resource);

    if (BeeUtils.isEmpty(extensions)) {
      LogUtils.warning(logger, resource, "Nothing to load");
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
        } else {
          extension.setCustom(true);
          dataCache.put(extName, extension);
          upd.add(extension);
          cTbl++;
        }
      } else {
        if (!extension.isEmpty()) {
          LogUtils.warning(logger, resource, "Table", extName, "denies structure modifications");
        } else {
          BeeTable extExt = extension.getExtTable();

          if (!BeeUtils.isEmpty(extExt)) {
            table.setExtTable(extExt);
            upd.add(extExt);
            cFld += extExt.getFields().size();
          }
        }
      }
    }
    for (BeeTable extension : upd) {
      initConstraints(extension);
    }
    LogUtils.infoNow(logger, "Loaded", cTbl, "new tables and", cFld,
        "new fields descriptions from", resource);
  }

  public boolean isField(String table, String field) {
    return dataCache.get(table).isField(field);
  }

  public boolean isTable(String source) {
    return !BeeUtils.isEmpty(source) && getTableNames().contains(source);
  }

  public void rebuildTable(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      String name = table.getName();
      List<BeeForeignKey> fk = new ArrayList<BeeForeignKey>();
      List<BeeTable> tables = new ArrayList<BeeTable>();

      for (String tbl : getTableNames()) {
        BeeTable base = getTable(tbl);

        if (!BeeUtils.same(base.getName(), name)) {
          tables.add(base);
        }
        if (!BeeUtils.isEmpty(base.getExtTable())) {
          tables.add(base.getExtTable());
        }
      }
      for (BeeTable tbl : tables) {
        for (BeeForeignKey key : tbl.getForeignKeys()) {
          if (BeeUtils.same(key.getRefTable(), name)) {
            fk.add(key);

            if (qs.dbForeignKeys(tbl.getName()).contains(key.getName())) {
              qs.updateData(SqlUtils.dropForeignKey(tbl.getName(), key.getName()));
            }
          }
        }
      }
      String als = BeeUtils.randomString(3, 3, 'a', 'z');
      List<String> fldList = new ArrayList<String>();

      if (qs.isDbTable(name)) {
        Collection<String> fields = qs.dbFields(name);

        for (String fld : getFieldNames(name)) {
          if (fields.contains(fld)) {
            fldList.add(fld);
          }
        }
        if (fields.contains(getLockName(name))) {
          fldList.add(getLockName(name));
        }
        if (fields.contains(getIdName(name))) {
          fldList.add(getIdName(name));
        }
        if (!BeeUtils.isEmpty(fldList)) {
          qs.updateData(new SqlCreate(als).setSource(
              new SqlSelect().addFields(name, fldList.toArray(new String[0])).addFrom(name)));
        }
        qs.updateData(SqlUtils.dropTable(name));
      }
      createTable(table);

      if (!BeeUtils.isEmpty(fldList)) {
        qs.updateData(new SqlInsert(name)
          .addFields(fldList.toArray(new String[0]))
          .setSource(new SqlSelect().addFields(als, fldList.toArray(new String[0])).addFrom(als)));

        qs.updateData(SqlUtils.dropTable(als));
      }
      createKeys(table);
      createForeignKeys(table);

      for (BeeForeignKey key : fk) {
        qs.updateData(SqlUtils.createForeignKey(key.getTable(), key.getName(),
            key.getKeyField(), key.getRefTable(), key.getRefField(), key.getAction()));
      }
    }
  }

  @Lock(LockType.WRITE)
  public void rebuildTables(ResponseBuffer buff) {
    for (BeeTable tbl : dataCache.values()) {
      rebuildTable(tbl);
      rebuildTable(tbl.getExtTable());
    }
    buff.add("Recreate structure OK");
  }

  private void createForeignKeys(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      for (BeeForeignKey key : table.getForeignKeys()) {
        qs.updateData(SqlUtils.createForeignKey(key.getTable(), key.getName(),
            key.getKeyField(), key.getRefTable(), key.getRefField(), key.getAction()));
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
    }
    return c;
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

        if (!BeeUtils.isEmpty(table.getExtTable())) {
          upd.add(table.getExtTable());
        }
      }
    }
    for (BeeTable table : upd) {
      initConstraints(table);
    }
    LogUtils.infoNow(logger, "Loaded", dataCache.size(), "tables descriptions from", resource);
  }

  @Lock(LockType.WRITE)
  private List<BeeTable> loadTables(String resource) {
    if (!FileUtils.isInputFile(resource)) {
      return null;
    }
    Document xml = XmlUtils.fromFileName(resource);
    if (BeeUtils.isEmpty(xml)) {
      return null;
    }
    List<BeeTable> data = new ArrayList<BeeTable>();
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

        data.add(tbl);

        BeeTable extTbl = new BeeTable(name + BeeTable.EXT_TABLE_SUFFIX,
              name + tbl.getIdName(), name + tbl.getLockName());
        initStructure(extTbl, table.getElementsByTagName("BeeExtended"));

        if (!extTbl.isEmpty()) {
          tbl.setExtTable(extTbl);
        }
      }
    } else {
      LogUtils.warning(logger, resource, "Node <BeeTables> is not found");
    }
    return data;
  }
}
