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
import com.butent.bee.egg.shared.sql.SqlCommand;
import com.butent.bee.egg.shared.sql.SqlCreate;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

  private Map<String, BeeTable> dataCache = new HashMap<String, BeeTable>();

  public boolean beeTable(String source) {
    Assert.notEmpty(source);
    return dataCache.containsKey(source);
  }

  public BeeStructure getField(String table, String field) {
    return dataCache.get(table).getField(field);
  }

  public String getIdName(String table) {
    return dataCache.get(table).getIdName();
  }

  public String getLockName(String table) {
    return dataCache.get(table).getLockName();
  }

  @Lock(LockType.WRITE)
  public void recreate(ResponseBuffer buff) {
    for (BeeTable tbl : dataCache.values()) {
      String table = tbl.getName();

      if (qs.tableExists(table)) {
        IsQuery drop = SqlUtils.dropTable(table);
        qs.updateData(drop);
      }
      SqlCreate sc = new SqlCreate(table);

      for (BeeStructure field : tbl.getFields()) {
        sc.addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
            field.isNotNull() ? new SqlCommand(Keywords.NOT_NULL) : null,
            field.isUnique() ? new SqlCommand(Keywords.UNIQUE) : null);
      }
      for (BeeForeignKey key : tbl.getForeignKeys()) {
        sc.addLong(key.getKeyField(),
            (key.getAction() == Keywords.CASCADE) ? new SqlCommand(Keywords.NOT_NULL) : null,
            new SqlCommand(Keywords.REFERENCES, SqlUtils.field(key.getRefTable()),
                SqlUtils.field(key.getRefField()), key.getAction()));
      }
      sc.addLong(tbl.getLockName(), new SqlCommand(Keywords.NOT_NULL));
      sc.addLong(tbl.getIdName(), new SqlCommand(Keywords.PRIMARY));

      buff.add("Create table " + table + ": " + qs.updateData(sc));

      for (BeeKey key : tbl.getKeys()) {
        IsQuery index;

        if (key.isUnique()) {
          index = SqlUtils.createUniqueIndex(table, key.getName(), key.getKeyFields());
        } else {
          index = SqlUtils.createIndex(table, key.getName(), key.getKeyFields());
        }
        qs.updateData(index);
      }
    }
    createData();
    buildXml("pypas.xml");
  }

  private void buildXml(String dst) {
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
      FileUtils.toFile(sw.toString(), dst);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void createData() {
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
      .addField("IdColumn", DataTypes.STRING, 30, 0, false, false)
      .addField("LockColumn", DataTypes.STRING, 30, 0, false, false);
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
    dataCache.put("Cities", new BeeTable("Cities", "CityID", null)
      .addForeignKey("CountryID", "Countries", "CountryID", null));
    dataCache.put("Countries", new BeeTable("Countries", "CountryID", null));
  }
}
