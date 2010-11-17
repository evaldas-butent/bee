package com.butent.bee.egg.server.data;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.egg.server.data.BeeTable.BeeKey;
import com.butent.bee.egg.server.data.BeeTable.BeeStructure;
import com.butent.bee.egg.server.utils.FileUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Singleton
@Startup
@Lock(LockType.READ)
public class SystemBean {

  @EJB
  QueryServiceBean qs;

  private Map<String, BeeTable> dataCache = new LinkedHashMap<String, BeeTable>();

  public boolean beeTable(String source) {
    Assert.notEmpty(source);
    return dataCache.containsKey(source);
  }

  @Lock(LockType.WRITE)
  public void createAll(ResponseBuffer buff) {
    createStructure(buff);
    createKeys(buff);
    createData(buff);
    createXml(buff);
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

        si.addConstant("TableID", tableId);

        qs.insertData(si);
      }
    }
    buff.add("Recreate data OK");
  }

  @Lock(LockType.WRITE)
  public void createKeys(ResponseBuffer buff) {
    for (BeeTable tbl : dataCache.values()) {
      String table = tbl.getName();

      IsQuery index = SqlUtils.createPrimaryKey(table, "PK_" + tbl.getIdName(), tbl.getIdName());
      qs.updateData(index);

      for (BeeStructure field : tbl.getFields()) {
        if (field.isUnique()) {
          index = SqlUtils.createUniqueIndex(table, field.getName());
          qs.updateData(index);
        }
      }
      for (BeeKey key : tbl.getKeys()) {
        if (key.isUnique()) {
          index = SqlUtils.createUniqueIndex(table, key.getName(), key.getKeyFields());
        } else {
          index = SqlUtils.createIndex(table, key.getName(), key.getKeyFields());
        }
        qs.updateData(index);
      }

      for (BeeForeignKey key : tbl.getForeignKeys()) {
        index = SqlUtils.createForeignKey(table, key.getName(), key.getKeyField(),
              key.getRefTable(), key.getRefField(), key.getAction());
        qs.updateData(index);
      }
    }
    buff.add("Recreate keys OK");
  }

  @Lock(LockType.WRITE)
  public void createStructure(ResponseBuffer buff) {
    dropStructure();

    for (BeeTable tbl : dataCache.values()) {
      SqlCreate sc = new SqlCreate(tbl.getName());

      for (BeeStructure field : tbl.getFields()) {
        sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
            field.isNotNull() ? Keywords.NOT_NULL : null);
      }
      for (BeeForeignKey key : tbl.getForeignKeys()) {
        sc.addLong(key.getKeyField(),
            (key.getAction() == Keywords.CASCADE) ? Keywords.NOT_NULL : null);
      }
      sc.addLong(tbl.getLockName(), Keywords.NOT_NULL);
      sc.addLong(tbl.getIdName(), Keywords.NOT_NULL);
      qs.updateData(sc);
    }
    buff.add("Recreate structure OK");
  }

  @Lock(LockType.WRITE)
  public void createXml(ResponseBuffer buff) {
    DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder;
    try {
      docBuilder = dbfac.newDocumentBuilder();
      Document doc = docBuilder.newDocument();

      Element root = doc.createElement("BeeTables");
      doc.appendChild(root);

      for (BeeTable table : dataCache.values()) {
        Element tbl = doc.createElement("BeeTable");
        tbl.setAttribute("name", table.getName());
        tbl.setAttribute("idName", table.getIdName());
        tbl.setAttribute("lockName", table.getLockName());
        root.appendChild(tbl);

        Element fldRoot = doc.createElement("BeeFields");
        tbl.appendChild(fldRoot);

        for (BeeStructure field : table.getFields()) {
          Element fld = doc.createElement("BeeField");
          fld.setAttribute("name", field.getName());
          fld.setAttribute("type", field.getType().toString());
          fld.setAttribute("precision", BeeUtils.transform(field.getPrecision()));
          fld.setAttribute("scale", BeeUtils.transform(field.getScale()));
          fld.setAttribute("notNull", BeeUtils.transform(field.isNotNull()));
          fld.setAttribute("unique", BeeUtils.transform(field.isUnique()));
          fldRoot.appendChild(fld);
        }
        if (!BeeUtils.isEmpty(table.getForeignKeys())) {
          Element foreignRoot = doc.createElement("BeeForeignKeys");
          tbl.appendChild(foreignRoot);

          for (BeeForeignKey key : table.getForeignKeys()) {
            Element foreign = doc.createElement("BeeForeignKey");
            foreign.setAttribute("name", key.getName());
            foreign.setAttribute("keyField", key.getKeyField());
            foreign.setAttribute("refTable", key.getRefTable());
            foreign.setAttribute("refField", key.getRefField());

            if (!BeeUtils.isEmpty(key.getAction())) {
              foreign.setAttribute("actionOnDelete", key.getAction().toString());
            }
            foreignRoot.appendChild(foreign);
          }
        }
        if (!BeeUtils.isEmpty(table.getForeignKeys())) {
          Element keyRoot = doc.createElement("BeeKeys");
          tbl.appendChild(keyRoot);

          for (BeeKey key : table.getKeys()) {
            Element index = doc.createElement("BeeKey");
            index.setAttribute("name", key.getName());
            index.setAttribute("fields", BeeUtils.transformArray(key.getKeyFields()));
            index.setAttribute("unique", Boolean.toString(key.isUnique()));
            keyRoot.appendChild(index);
          }
        }
      }
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();
      trans.setOutputProperty(OutputKeys.INDENT, "yes");

      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      DOMSource source = new DOMSource(doc);
      trans.transform(source, result);
      FileUtils.toFile(sw.toString(), "bee.xml");

    } catch (Exception e) {
      e.printStackTrace();
    }
    buff.add("Recreate XML OK");
  }

  public BeeStructure getField(String table, String field) {
    return dataCache.get(table).getField(field);
  }

  public Collection<BeeStructure> getFields(String table) {
    return dataCache.get(table).getFields();
  }

  public Collection<BeeForeignKey> getForeignKeys(String table) {
    return dataCache.get(table).getForeignKeys();
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

  public Set<String> getTables() {
    return Collections.unmodifiableSet(dataCache.keySet());
  }

  private void dropStructure() {
    List<BeeTable> cache = new ArrayList<BeeTable>();
    for (BeeTable tbl : dataCache.values()) {
      cache.add(tbl);
    }
    for (int i = cache.size() - 1; i >= 0; i--) {
      String table = cache.get(i).getName();

      if (qs.tableExists(table)) {
        IsQuery drop = SqlUtils.dropTable(table);
        qs.updateData(drop);
      }
    }
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    initTables();
    initStructure();
  }

  private void initStructure() {
    dataCache.get("bee_Tables")
      .addField("TableName", DataTypes.STRING, 30, 0, true, true)
      .addField("LockColumn", DataTypes.STRING, 30, 0, false, false)
      .addField("IdColumn", DataTypes.STRING, 30, 0, false, false);
    dataCache.get("bee_Fields")
      .addField("FieldName", DataTypes.STRING, 30, 0, true, false)
      .addField("FieldType", DataTypes.STRING, 30, 0, false, false)
      .addField("Precision", DataTypes.INTEGER, 0, 0, false, false)
      .addField("Scale", DataTypes.INTEGER, 0, 0, false, false)
      .addField("NotNull", DataTypes.BOOLEAN, 0, 0, false, false)
      .addField("Unique", DataTypes.BOOLEAN, 0, 0, false, false);
    dataCache.get("Countries")
      .addField("Country", DataTypes.STRING, 100, 0, true, true);
    dataCache.get("Cities")
      .addField("City", DataTypes.STRING, 100, 0, true, true);
  }

  private void initTables() {
    dataCache.put("bee_Tables", new BeeTable("bee_Tables", "TableID", "Locked"));
    dataCache.put("bee_Fields", new BeeTable("bee_Fields", "FieldID", null)
      .addKey("FieldName")
      .addUniqueKey("TableField", "TableID", "FieldName")
      .addForeignKey("TableID", "bee_Tables", "TableID", Keywords.CASCADE));
    dataCache.put("Countries", new BeeTable("Countries", "CountryID", null));
    dataCache.put("Cities", new BeeTable("Cities", "CityID", null)
      .addForeignKey("CountryID", "Countries", "CountryID", null));
  }
}
