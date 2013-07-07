package com.butent.bee.server.modules.ec;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlCreate.SqlField;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.SqlConstants;
import com.butent.bee.shared.data.SqlConstants.SqlFunction;
import com.butent.bee.shared.data.SqlConstants.SqlKeyword;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.webservice.ButentWS;
import com.butent.webservice.ButentWebServiceSoapPort;

import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.ws.BindingProvider;

@Stateless
@LocalBean
public class TecDocBean {

  private static BeeLogger logger = LogUtils.getLogger(TecDocBean.class);
  private static BeeLogger messyLogger = LogUtils.getLogger(QueryServiceBean.class);

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  TecDocRemote tcd;

  @Asynchronous
  public void justDoIt() {
    Set<String> names = Sets.newHashSet(qs.dbTables(sys.getDbName(), sys.getDbSchema(), null)
        .getColumn(SqlConstants.TBL_NAME));

    String articleBrands = "TcdArticleBrands";
    String tcdButent = SqlUtils.table("TecDoc", "TcdButent");
    String analogs = "TcdAnalogs";
    String tcdAnalogs = SqlUtils.table("TecDoc", analogs);

    if (!names.contains(articleBrands)) {
      qs.updateData(new SqlCreate(articleBrands, false)
          .setDataSource(new SqlSelect()
              .addFields(tcdAnalogs, "ArticleID", "Brand", "AnalogNr")
              .addFields(tcdButent, "Price", "ButentID")
              .addFrom(tcdButent)
              .addFromInner(tcdAnalogs,
                  SqlUtils.joinUsing(tcdButent, tcdAnalogs, "SearchNr", "Brand"))));

      qs.updateData(SqlUtils.createIndex(articleBrands, "IK_" + articleBrands + "ButentID",
          Lists.newArrayList("ButentID"), false));

      qs.updateData(SqlUtils.createIndex(articleBrands, "IK_" + articleBrands + "ArticleID",
          Lists.newArrayList("ArticleID"), false));
    }
    String stocks = "TcdStocks";
    String tcdButentStocks = SqlUtils.table("TecDoc", "TcdButentStocks");

    if (!names.contains(stocks)) {
      qs.updateData(new SqlCreate(stocks, false)
          .setDataSource(new SqlSelect()
              .addAllFields(tcdButentStocks)
              .addFrom(tcdButentStocks)
              .setWhere(SqlUtils.in(tcdButentStocks, "ButentID", articleBrands, "ButentID",
                  null))));

      qs.updateData(SqlUtils.createIndex(stocks, "IK_" + stocks + "ButentID",
          Lists.newArrayList("ButentID"), false));
    }
    String articles = "TcdArticles";
    String tcdArticles = SqlUtils.table("TecDoc", articles);

    if (!names.contains(articles)) {
      qs.updateData(new SqlCreate(articles, false)
          .setDataSource(new SqlSelect()
              .addAllFields(tcdArticles)
              .addFrom(tcdArticles)
              .setWhere(SqlUtils.in(tcdArticles, "ArticleID", articleBrands, "ArticleID", null))));

      qs.updateData(SqlUtils.createPrimaryKey(articles, "PK_" + articles,
          Lists.newArrayList("ArticleID")));

      qs.updateData(SqlUtils.createForeignKey(articleBrands, "FK_" + articleBrands + articles,
          Lists.newArrayList("ArticleID"), articles, Lists.newArrayList("ArticleID"),
          SqlKeyword.DELETE));
    }
    String articleCriteria = "TcdArticleCriteria";
    String tcdArticleCriteria = SqlUtils.table("TecDoc", articleCriteria);
    String tcdCriteria = SqlUtils.table("TecDoc", "TcdCriteria");

    if (!names.contains(articleCriteria)) {
      qs.updateData(new SqlCreate(articleCriteria, false)
          .setDataSource(new SqlSelect()
              .addFields(tcdArticleCriteria, "ArticleID", "Value")
              .addFields(tcdCriteria, "Name")
              .addFrom(tcdArticleCriteria)
              .addFromInner(articles, SqlUtils.joinUsing(tcdArticleCriteria, articles, "ArticleID"))
              .addFromInner(tcdCriteria,
                  SqlUtils.joinUsing(tcdArticleCriteria, tcdCriteria, "CriteriaID"))));

      qs.updateData(SqlUtils.createForeignKey(articleCriteria, "FK_" + articleCriteria + articles,
          Lists.newArrayList("ArticleID"), articles, Lists.newArrayList("ArticleID"),
          SqlKeyword.DELETE));
    }
    if (!names.contains(analogs)) {
      qs.updateData(new SqlCreate(analogs, false)
          .setDataSource(new SqlSelect()
              .addAllFields(tcdAnalogs)
              .addFrom(tcdAnalogs)
              .addFromInner(articles, SqlUtils.joinUsing(tcdAnalogs, articles, "ArticleID"))));

      qs.updateData(SqlUtils.createForeignKey(analogs, "FK_" + analogs + articles,
          Lists.newArrayList("ArticleID"), articles, Lists.newArrayList("ArticleID"),
          SqlKeyword.DELETE));

      qs.updateData(SqlUtils.createIndex(analogs, "IK_" + analogs + "SearchNr",
          Lists.newArrayList("SearchNr"), false));
    }
    String articleCategories = "TcdArticleCategories";
    String tcdArticlesToGeneric = SqlUtils.table("TecDoc", "TcdArticlesToGeneric");
    String tcdGenericToCategories = SqlUtils.table("TecDoc", "TcdGenericToCategories");
    String categories = "TcdCategories";
    String tcdCategories = SqlUtils.table("TecDoc", categories);

    if (!names.containsAll(Lists.newArrayList(articleCategories, categories))) {
      qs.updateData(new SqlCreate(articleCategories, false)
          .setDataSource(new SqlSelect().setDistinctMode(true)
              .addFields(articles, "ArticleID")
              .addFields(tcdCategories, "CategoryID")
              .addFrom(articles)
              .addFromInner(tcdArticlesToGeneric,
                  SqlUtils.joinUsing(articles, tcdArticlesToGeneric, "ArticleID"))
              .addFromInner(tcdGenericToCategories,
                  SqlUtils.joinUsing(tcdArticlesToGeneric, tcdGenericToCategories, "GenericID"))
              .addFromInner(tcdCategories,
                  SqlUtils.joinUsing(tcdGenericToCategories, tcdCategories, "CategoryID"))));

      qs.updateData(new SqlCreate(categories, false)
          .setDataSource(new SqlSelect()
              .addAllFields(tcdCategories)
              .addFrom(tcdCategories)
              .setWhere(SqlUtils.in(tcdCategories, "CategoryID", articleCategories, "CategoryID",
                  null))));

      qs.updateData(SqlUtils.createPrimaryKey(categories, "PK_" + categories,
          Lists.newArrayList("CategoryID")));

      String tmpId = qs.sqlCreateTemp(new SqlSelect()
          .addFields(categories, "CategoryID")
          .addFrom(categories)
          .setWhere(SqlUtils.isNull(categories, "ParentID")));

      int c = 0;
      String als = SqlUtils.uniqueName();

      do {
        String nextLevel = qs.sqlCreateTemp(new SqlSelect()
            .addFields(categories, "CategoryID")
            .addFrom(categories)
            .addFromInner(tmpId, SqlUtils.join(categories, "ParentID", tmpId, "CategoryID")));

        SqlSelect query = new SqlSelect()
            .addFields(categories, "CategoryName")
            .addFrom(categories)
            .addFromInner(tmpId, SqlUtils.joinUsing(categories, tmpId, "CategoryID"))
            .addGroup(categories, "CategoryName");

        if (c > 0) {
          query.addMax(categories, "CategoryID")
              .setHaving(SqlUtils.more(SqlUtils.aggregate(SqlFunction.COUNT, null), 1));
        } else {
          query.addEmptyInt("CategoryID");
        }
        String tmp = qs.sqlCreateTemp(new SqlSelect()
            .addFields(categories, "CategoryID")
            .addField(als, "CategoryID", "TargetID")
            .addFrom(categories)
            .addFromInner(tmpId, SqlUtils.joinUsing(categories, tmpId, "CategoryID"))
            .addFromInner(query, als, SqlUtils.joinUsing(categories, als, "CategoryName"))
            .setWhere(SqlUtils.or(SqlUtils.isNull(als, "CategoryID"),
                SqlUtils.joinNotEqual(als, "CategoryID", categories, "CategoryID"))));

        qs.sqlIndex(tmp, "CategoryID");

        qs.updateData(new SqlUpdate(articleCategories)
            .addExpression("CategoryID", SqlUtils.field(tmp, "TargetID"))
            .setFrom(tmp, SqlUtils.joinUsing(articleCategories, tmp, "CategoryID")));

        c = qs.updateData(new SqlUpdate(categories)
            .addExpression("ParentID", SqlUtils.field(tmp, "TargetID"))
            .setFrom(tmp, SqlUtils.join(categories, "ParentID", tmp, "CategoryID")));

        qs.updateData(new SqlDelete(categories)
            .setWhere(SqlUtils.in(categories, "CategoryID", tmp, "CategoryID", null)));

        qs.sqlDropTemp(tmp);
        qs.sqlDropTemp(tmpId);
        tmpId = nextLevel;
      } while (c > 0);

      qs.sqlDropTemp(tmpId);

      qs.updateData(SqlUtils.createForeignKey(categories,
          "FK_" + categories + categories,
          Lists.newArrayList("ParentID"), categories, Lists.newArrayList("CategoryID"),
          SqlKeyword.DELETE));

      String tmp = SqlUtils.uniqueName();

      qs.updateData(SqlUtils.renameTable(articleCategories, tmp));

      qs.updateData(new SqlCreate(articleCategories, false)
          .setDataSource(new SqlSelect().setDistinctMode(true)
              .addFields(tmp, "ArticleID", "CategoryID")
              .addFrom(tmp)
              .addFromInner(categories, SqlUtils.joinUsing(tmp, categories, "CategoryID"))));

      qs.updateData(SqlUtils.createForeignKey(articleCategories,
          "FK_" + articleCategories + articles,
          Lists.newArrayList("ArticleID"), articles, Lists.newArrayList("ArticleID"),
          SqlKeyword.DELETE));

      qs.updateData(SqlUtils.createForeignKey(articleCategories,
          "FK_" + articleCategories + categories,
          Lists.newArrayList("CategoryID"), categories, Lists.newArrayList("CategoryID"),
          SqlKeyword.DELETE));

      qs.updateData(SqlUtils.dropTable(tmp));
    }
    String tcdArticlesWithGeneric = SqlUtils.table("TecDoc", "TcdArticlesWithGeneric");
    String tcdArticlesWithGenericToTypes =
        SqlUtils.table("TecDoc", "TcdArticlesWithGenericToTypes");
    String typeArticles = "TcdTypeArticles";
    String types = "TcdTypes";
    String tcdTypes = SqlUtils.table("TecDoc", types);

    if (!names.contains(typeArticles)) {
      qs.updateData(new SqlCreate(typeArticles, false)
          .setDataSource(new SqlSelect().setDistinctMode(true)
              .addFields(tcdTypes, "TypeID")
              .addFields(articles, "ArticleID")
              .addFrom(articles)
              .addFromInner(tcdArticlesWithGeneric,
                  SqlUtils.joinUsing(articles, tcdArticlesWithGeneric, "ArticleID"))
              .addFromInner(tcdArticlesWithGenericToTypes,
                  SqlUtils.joinUsing(tcdArticlesWithGeneric,
                      tcdArticlesWithGenericToTypes, "GenericArticleID"))
              .addFromInner(tcdTypes,
                  SqlUtils.joinUsing(tcdArticlesWithGenericToTypes, tcdTypes, "TypeID"))));

      qs.updateData(SqlUtils.createForeignKey(typeArticles, "FK_" + typeArticles + articles,
          Lists.newArrayList("ArticleID"), articles, Lists.newArrayList("ArticleID"),
          SqlKeyword.DELETE));
    }
    if (!names.contains(types)) {
      qs.updateData(new SqlCreate(types, false)
          .setDataSource(new SqlSelect()
              .addAllFields(tcdTypes)
              .addFrom(tcdTypes)
              .setWhere(SqlUtils.in(tcdTypes, "TypeID", typeArticles, "TypeID", null))));

      qs.updateData(SqlUtils.createPrimaryKey(types, "PK_" + types,
          Lists.newArrayList("TypeID")));

      qs.updateData(SqlUtils.createForeignKey(typeArticles, "FK_" + typeArticles + types,
          Lists.newArrayList("TypeID"), types, Lists.newArrayList("TypeID"), SqlKeyword.DELETE));
    }
    String models = "TcdModels";
    String tcdModels = SqlUtils.table("TecDoc", models);

    if (!names.contains(models)) {
      qs.updateData(new SqlCreate(models, false)
          .setDataSource(new SqlSelect()
              .addAllFields(tcdModels)
              .addFrom(tcdModels)
              .setWhere(SqlUtils.in(tcdModels, "ModelID", types, "ModelID", null))));

      qs.updateData(SqlUtils.createPrimaryKey(models, "PK_" + models,
          Lists.newArrayList("ModelID")));

      qs.updateData(SqlUtils.createForeignKey(types, "FK_" + types + models,
          Lists.newArrayList("ModelID"), models, Lists.newArrayList("ModelID"), SqlKeyword.DELETE));
    }
  }

  @Asynchronous
  public void suckButent() {
    String butent = "TcdButent";
    String tcdSchema = "TecDoc";
    String tcdButent = SqlUtils.table(tcdSchema, butent);
    String tcdButentStocks = SqlUtils.table(tcdSchema, butent + "Stocks");

    if (!qs.dbSchemaExists(sys.getDbName(), tcdSchema)) {
      qs.updateData(SqlUtils.createSchema(tcdSchema));
    }
    if (qs.dbTableExists(sys.getDbName(), tcdSchema, butent)) {
      qs.updateData(SqlUtils.dropTable(tcdButent));
    }
    qs.updateData(new SqlCreate(tcdButent, false)
        .addLong("ButentID", true)
        .addString("Brand", 50, false)
        .addString("SearchNr", 50, true)
        .addDecimal("Price", 10, 2, false));

    if (qs.dbTableExists(sys.getDbName(), tcdSchema, butent + "Stocks")) {
      qs.updateData(SqlUtils.dropTable(tcdButentStocks));
    }
    qs.updateData(new SqlCreate(tcdButentStocks, false)
        .addLong("ButentID", true)
        .addString("Warehouse", 10, true)
        .addDecimal("Stock", 1, 0, false));

    String address = "http://82.135.245.222:8081/ButentWS/ButentWS.WSDL";
    ButentWS butentWS = ButentWS.create(address);
    ButentWebServiceSoapPort port = butentWS.getButentWebServiceSoapPort();

    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
        butentWS.getWSDLDocumentLocation().toString());

    String response = port.login("admin", "gruntas");
    String error = "Unknown login response";

    if (BeeUtils.same(response, "OK")) {
      error = null;
    }
    if (!BeeUtils.isEmpty(error)) {
      Map<String, String> messages = XmlUtils.getElements(response, null);

      if (BeeUtils.containsKey(messages, "Message")) {
        error = messages.get("Message");

        if (BeeUtils.same(error, "Already logged in")) {
          error = null;
        }
      }
    }
    if (!BeeUtils.isEmpty(error)) {
      logger.severe(error);
      return;
    }
    logger.info(butent, "Waiting for webService data...");

    response = port.process("GetSQLData", "<query>SELECT preke AS pr, pard_kaina AS kn, "
        + "artikulas AS art, tiek_art AS ta, gam_art AS ga, gamintojas AS gam FROM prekes</query>");

    Node data = XmlUtils.fromString(response).getFirstChild();

    if (BeeUtils.same(data.getNodeName(), "Error")) {
      logger.severe(data.getTextContent());
      return;
    }
    if (data.hasChildNodes()) {
      logger.info(butent, "webService returned", data.getChildNodes().getLength(),
          "rows. Inserting data...");

      boolean isDebugEnabled = messyLogger.isDebugEnabled();

      if (isDebugEnabled) {
        messyLogger.setLevel(LogLevel.INFO);
      }
      for (int i = 0; i < data.getChildNodes().getLength(); i++) {
        Node row = data.getChildNodes().item(i);

        if (row.hasChildNodes()) {
          Map<String, String> info = XmlUtils.getElements(row.getChildNodes(), null);
          String code = BeeUtils.notEmpty(info.get("ga"), info.get("ta"), info.get("art"));

          if (!BeeUtils.isEmpty(code)) {
            qs.insertData(new SqlInsert(tcdButent)
                .addConstant("ButentID", BeeUtils.toLong(info.get("pr")))
                .addConstant("Brand", info.get("gam"))
                .addConstant("SearchNr", code.replaceAll("[^A-Za-z0-9]", "").toUpperCase())
                .addConstant("Price", BeeUtils.toDoubleOrNull(info.get("kn"))));
          }
        }
      }
      if (isDebugEnabled) {
        messyLogger.setLevel(LogLevel.DEBUG);
      }
      logger.info(butent, "insert finished. Indexing data...");
    }
    qs.updateData(SqlUtils.createIndex(tcdButent, "IK_" + butent + "SearchNr",
        Lists.newArrayList("SearchNr"), false));

    logger.info(butent, "indexing finished");

    logger.info(tcdButentStocks, "Waiting for webService data...");

    response = port.process("GetSQLData", "<query>SELECT sandelis AS sn, preke AS pr,"
        + " sum(kiekis) AS lk FROM likuciai GROUP by sandelis, preke HAVING lk > 0</query>");

    data = XmlUtils.fromString(response).getFirstChild();

    if (BeeUtils.same(data.getNodeName(), "Error")) {
      logger.severe(data.getTextContent());
      return;
    }
    if (data.hasChildNodes()) {
      logger.info(tcdButentStocks, "webService returned", data.getChildNodes().getLength(),
          "rows. Inserting data...");

      boolean isDebugEnabled = messyLogger.isDebugEnabled();

      if (isDebugEnabled) {
        messyLogger.setLevel(LogLevel.INFO);
      }
      for (int i = 0; i < data.getChildNodes().getLength(); i++) {
        Node row = data.getChildNodes().item(i);

        if (row.hasChildNodes()) {
          Map<String, String> info = XmlUtils.getElements(row.getChildNodes(), null);
          Double stock = BeeUtils.toDoubleOrNull(info.get("lk"));

          qs.insertData(new SqlInsert(tcdButentStocks)
              .addConstant("ButentID", BeeUtils.toLong(info.get("pr")))
              .addConstant("Warehouse", info.get("sn"))
              .addConstant("Stock", stock > 5 ? 6 : BeeUtils.round(stock)));
        }
      }
      if (isDebugEnabled) {
        messyLogger.setLevel(LogLevel.DEBUG);
      }
      logger.info(tcdButentStocks, "insert finished. Indexing data...");

      qs.updateData(SqlUtils.createIndex(tcdButentStocks, "IK_" + butent + "StocksButentID",
          Lists.newArrayList("ButentID"), false));

      logger.info(tcdButentStocks, "indexing finished");
    } else {
      logger.info(butent, "webService returned no data");
    }
  }

  @Asynchronous
  public void suckMotonet() {
    // String motonet = "TcdMotonet";
    // String tcdSchema = "TecDoc";
    // String tcdMotonet = SqlUtils.table(tcdSchema, motonet);
    // logger.info(motonet, "Waiting for webService data...");
    //
    // WSMotoOfertaSoap port = new WSMotoOferta().getWSMotoOfertaSoap();
    // ArrayOfString data = port.zwrocCennikDetalOffline("10431", "6492", "BEE");
    //
    // if (data == null || data.getString().size() == 1) {
    // logger.info(motonet, "webService returned no data");
    // } else {
    // logger.info(motonet, "webService returned", data.getString().size(),
    // "rows. Inserting data...");
    //
    // if (!qs.dbSchemaExists(sys.getDbName(), tcdSchema)) {
    // qs.updateData(SqlUtils.createSchema(tcdSchema));
    // }
    // if (qs.dbTableExists(sys.getDbName(), tcdSchema, motonet)) {
    // qs.updateData(SqlUtils.dropTable(tcdMotonet));
    // }
    // qs.updateData(new SqlCreate(tcdMotonet, false)
    // .addString("Prefix", 10, true)
    // .addString("Index", 50, true)
    // .addString("SearchNr", 50, true)
    // .addDecimal("Stock", 1, 0, false)
    // .addDecimal("Price", 10, 2, false));
    //
    // boolean isDebugEnabled = messyLogger.isDebugEnabled();
    //
    // if (isDebugEnabled) {
    // messyLogger.setLevel(LogLevel.INFO);
    // }
    // for (String item : data.getString()) {
    // String[] values = item.split("[|]", 8);
    //
    // if (values.length == 8) {
    // qs.insertData(new SqlInsert(tcdMotonet)
    // .addConstant("Prefix", values[0])
    // .addConstant("Index", values[1])
    // .addConstant("SearchNr", values[1].replaceAll("[^A-Za-z0-9]", "").toUpperCase())
    // .addConstant("Stock", BeeUtils.toNonNegativeInt(BeeUtils.toInt(values[3])))
    // .addConstant("Price", BeeUtils.toDoubleOrNull(values[7].replace(',', '.'))));
    // }
    // }
    //
    // if (isDebugEnabled) {
    // messyLogger.setLevel(LogLevel.DEBUG);
    // }
    // logger.info(motonet, "insert finished. Indexing data...");
    //
    // qs.updateData(SqlUtils.createIndex(tcdMotonet, "IK_" + motonet + "SearchNr",
    // Lists.newArrayList("SearchNr"), false));
    //
    // logger.info(motonet, "indexing finished");
    // }
  }

  @Asynchronous
  @TransactionAttribute(TransactionAttributeType.NEVER)
  public void suckTecdoc() {
    List<Pair<SqlCreate, SqlSelect>> queries = Lists.newArrayList();
    queries.add(Pair.of(
        new SqlCreate("TcdModels", false)
            .addInteger("ModelID", true)
            .addString("ModelName", 50, true)
            .addString("Manufacturer", 50, true),
        new SqlSelect()
            .addField("tof_models", "mod_id", "ModelID")
            .addField("_designations", "tex_text", "ModelName")
            .addField("tof_manufacturers", "mfa_brand", "Manufacturer")
            .addFrom("tof_models")
            .addFromInner("tof_manufacturers",
                SqlUtils.join("tof_models", "mod_mfa_id", "tof_manufacturers", "mfa_id"))
            .addFromInner("_designations",
                SqlUtils.join("tof_models", "mod_cds_id", "_designations", "cds_id"))));
    queries.add(Pair.of(
        new SqlCreate("TcdTypes", false)
            .addInteger("TypeID", true)
            .addInteger("ModelID", true)
            .addString("TypeName", 50, true)
            .addInteger("ProducedFrom", false)
            .addInteger("ProducedTo", false)
            .addInteger("Ccm", false)
            .addDecimal("KwFrom", 3, 0, false)
            .addDecimal("KwTo", 3, 0, false)
            .addDecimal("Cylinders", 2, 0, false)
            .addDecimal("MaxWeight", 6, 2, false)
            .addString("Engine", 50, false)
            .addString("Fuel", 50, false)
            .addString("Body", 50, false)
            .addString("Axle", 50, false),
        new SqlSelect()
            .addField("tof_types", "typ_id", "TypeID")
            .addField("tof_types", "typ_mod_id", "ModelID")
            .addField("_designations", "tex_text", "TypeName")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("typ_pcon_start"),
                SqlUtils.constant("NULL"), null, "typ_pcon_start"), "ProducedFrom")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("typ_pcon_end"),
                SqlUtils.constant("NULL"), null, "typ_pcon_end"), "ProducedTo")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("typ_ccm"),
                SqlUtils.constant("NULL"), null, "typ_ccm"), "Ccm")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("typ_kw_from"),
                SqlUtils.constant("NULL"), null, "typ_kw_from"), "KwFrom")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("typ_kw_upto"),
                SqlUtils.constant("NULL"), null, "typ_kw_upto"), "KwTo")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("typ_cylinders"),
                SqlUtils.constant("NULL"), null, "typ_cylinders"), "Cylinders")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("typ_max_weight"),
                SqlUtils.constant("NULL"), null, "typ_max_weight"), "MaxWeight")
            .addField("_engines", "tex_text", "Engine")
            .addField("_fuels", "tex_text", "Fuel")
            .addExpr(SqlUtils.nvl(SqlUtils.field("_bodies", "tex_text"),
                SqlUtils.field("_models", "tex_text")), "Body")
            .addField("_bodies", "tex_text", "Body")
            .addField("_axles", "tex_text", "Axle")
            .addFrom("tof_types")
            .addFromInner("_designations",
                SqlUtils.join("tof_types", "typ_cds_id", "_designations", "cds_id"))
            .addFromLeft("_designations", "_engines",
                SqlUtils.join("tof_types", "typ_kv_engine_des_id", "_engines", "des_id"))
            .addFromLeft("_designations", "_fuels",
                SqlUtils.join("tof_types", "typ_kv_fuel_des_id", "_fuels", "des_id"))
            .addFromLeft("_designations", "_bodies",
                SqlUtils.join("tof_types", "typ_kv_body_des_id", "_bodies", "des_id"))
            .addFromLeft("_designations", "_models",
                SqlUtils.join("tof_types", "typ_kv_model_des_id", "_models", "des_id"))
            .addFromLeft("_designations", "_axles",
                SqlUtils.join("tof_types", "typ_kv_axle_des_id", "_axles", "des_id"))));
    queries.add(Pair.of(
        new SqlCreate("TcdArticles", false)
            .addInteger("ArticleID", true)
            .addString("ArticleNr", 50, true)
            .addString("ArticleName", 50, true)
            .addString("Supplier", 50, true),
        new SqlSelect().setLimit(1000000)
            .addField("tof_articles", "art_id", "ArticleID")
            .addField("tof_articles", "art_article_nr", "ArticleNr")
            .addField("_designations", "tex_text", "ArticleName")
            .addField("tof_suppliers", "sup_brand", "Supplier")
            .addFrom("tof_articles")
            .addFromInner("tof_suppliers",
                SqlUtils.join("tof_articles", "art_sup_id", "tof_suppliers", "sup_id"))
            .addFromInner("_designations",
                SqlUtils.join("tof_articles", "art_complete_des_id", "_designations", "des_id"))));
    queries.add(Pair.of(
        new SqlCreate("TcdCriteria", false)
            .addInteger("CriteriaID", true)
            .addString("Name", 50, true)
            .addString("ShortName", 50, false)
            .addString("UnitName", 50, false),
        new SqlSelect()
            .addField("tof_criteria", "cri_id", "CriteriaID")
            .addField("_designations", "tex_text", "Name")
            .addField("short", "tex_text", "ShortName")
            .addField("unit", "tex_text", "UnitName")
            .addFrom("tof_criteria")
            .addFromInner("_designations",
                SqlUtils.join("tof_criteria", "cri_des_id", "_designations", "des_id"))
            .addFromLeft("_designations", "short",
                SqlUtils.join("tof_criteria", "cri_short_des_id", "short", "des_id"))
            .addFromLeft("_designations", "unit",
                SqlUtils.join("tof_criteria", "cri_unit_des_id", "unit", "des_id"))));
    queries.add(Pair.of(
        new SqlCreate("TcdArticleCriteria", false)
            .addInteger("ArticleID", true)
            .addInteger("CriteriaID", true)
            .addString("Value", 50, false),
        new SqlSelect().setLimit(1000000)
            .addField("tof_article_criteria", "acr_art_id", "ArticleID")
            .addField("tof_article_criteria", "acr_cri_id", "CriteriaID")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("acr_value"),
                SqlUtils.constant("NULL"), SqlUtils.field("_designations", "tex_text"),
                "acr_value"), "Value")
            .addFrom("tof_article_criteria")
            .addFromLeft("_designations", SqlUtils.join("tof_article_criteria", "acr_kv_des_id",
                "_designations", "des_id"))));
    queries.add(Pair.of(
        new SqlCreate("TcdAnalogs", false)
            .addInteger("ArticleID", true)
            .addString("SearchNr", 50, true)
            .addDecimal("Kind", 1, 0, true)
            .addString("AnalogNr", 50, false)
            .addString("Supplier", 50, false),
        new SqlSelect().setLimit(1000000)
            .addField("tof_art_lookup", "arl_art_id", "ArticleID")
            .addField("tof_art_lookup", "arl_search_number", "SearchNr")
            .addField("tof_art_lookup", "arl_kind", "Kind")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("arl_display_nr"),
                SqlUtils.constant("NULL"), null, "arl_display_nr"), "AnalogNr")
            .addField("tof_brands", "bra_brand", "Supplier")
            .addFrom("tof_art_lookup")
            .addFromLeft("tof_brands",
                SqlUtils.join("tof_art_lookup", "arl_bra_id", "tof_brands", "bra_id"))));
    queries.add(Pair.of(
        new SqlCreate("TcdGenericArticles", false)
            .addInteger("GenericID", true)
            .addString("GenericName", 50, true)
            .addString("Standard", 50, false)
            .addString("Assembly", 50, false)
            .addString("Intended", 50, false),
        new SqlSelect()
            .addField("tof_generic_articles", "ga_id", "GenericID")
            .addField("_designations", "tex_text", "GenericName")
            .addField("_standard", "tex_text", "Standard")
            .addField("_assembly", "tex_text", "Assembly")
            .addField("_intend", "tex_text", "Intended")
            .addFrom("tof_generic_articles")
            .addFromInner("_designations",
                SqlUtils.join("tof_generic_articles", "ga_des_id", "_designations", "des_id"))
            .addFromLeft("_designations", "_standard",
                SqlUtils.join("tof_generic_articles", "ga_des_id_standard", "_standard", "des_id"))
            .addFromLeft("_designations", "_assembly",
                SqlUtils.join("tof_generic_articles", "ga_des_id_assembly", "_assembly", "des_id"))
            .addFromLeft("_designations", "_intend",
                SqlUtils.join("tof_generic_articles", "ga_des_id_intended", "_intend", "des_id"))));
    queries.add(Pair.of(
        new SqlCreate("TcdArticlesToGeneric", false)
            .addInteger("ArticleID", true)
            .addInteger("GenericID", true),
        new SqlSelect().setLimit(1000000)
            .addField("tof_link_art_ga", "lag_art_id", "ArticleID")
            .addField("tof_link_art_ga", "lag_ga_id", "GenericID")
            .addFrom("tof_link_art_ga")));
    queries.add(Pair.of(
        new SqlCreate("TcdArticlesWithGeneric", false)
            .addInteger("GenericArticleID", true)
            .addInteger("ArticleID", true)
            .addInteger("GenericID", true),
        new SqlSelect().setLimit(1000000)
            .addField("tof_link_art", "la_id", "GenericArticleID")
            .addField("tof_link_art", "la_art_id", "ArticleID")
            .addField("tof_link_art", "la_ga_id", "GenericID")
            .addFrom("tof_link_art")));
    queries.add(Pair.of(
        new SqlCreate("TcdArticlesWithGenericToTypes", false)
            .addInteger("GenericArticleID", true)
            .addInteger("TypeID", true),
        new SqlSelect().setLimit(1000000)
            .addField("tof_link_la_typ", "lat_typ_id", "TypeID")
            .addField("tof_link_la_typ", "lat_la_id", "GenericArticleID")
            .addFrom("tof_link_la_typ")));
    queries.add(Pair.of(
        new SqlCreate("TcdCategories", false)
            .addInteger("CategoryID", true)
            .addInteger("ParentID", false)
            .addString("CategoryName", 50, true),
        new SqlSelect()
            .addField("tof_search_tree", "str_id", "CategoryID")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("str_id_parent"),
                SqlUtils.constant("NULL"), null, "str_id_parent"), "ParentID")
            .addField("_designations", "tex_text", "CategoryName")
            .addFrom("tof_search_tree")
            .addFromInner("_designations",
                SqlUtils.join("tof_search_tree", "str_des_id", "_designations", "des_id"))));
    queries.add(Pair.of(
        new SqlCreate("TcdGenericToCategories", false)
            .addInteger("GenericID", true)
            .addInteger("CategoryID", true),
        new SqlSelect()
            .addField("tof_link_ga_str", "lgs_ga_id", "GenericID")
            .addField("tof_link_ga_str", "lgs_str_id", "CategoryID")
            .addFrom("tof_link_ga_str")));

    String tcdSchema = "TecDoc";

    if (!qs.dbSchemaExists(sys.getDbName(), tcdSchema)) {
      qs.updateData(SqlUtils.createSchema(tcdSchema));
    }
    for (Pair<SqlCreate, SqlSelect> entry : queries) {
      SqlCreate create = entry.getA();
      SqlSelect query = entry.getB();

      String target = create.getTarget();

      if (qs.dbTableExists(sys.getDbName(), tcdSchema, target)) {
        continue; // qs.updateData(SqlUtils.dropTable(SqlUtils.table(tcdSchema, target)));
      }
      target = SqlUtils.table(tcdSchema, target);
      create.setTarget(target);

      qs.updateData(create);

      List<SqlField> fields = create.getFields();

      int total = 0;
      int chunkTotal = 0;
      int chunk = query.getLimit();
      int offset = 0;

      boolean isDebugEnabled = messyLogger.isDebugEnabled();

      do {
        if (chunk > 0) {
          chunkTotal = 0;
          query.setOffset(offset);
        }
        SqlInsert insert = new SqlInsert(target);

        for (SqlField field : fields) {
          insert.addFields(field.getName());
        }
        List<StringBuilder> inserts = tcd.getRemoteData(query, insert);

        if (isDebugEnabled) {
          messyLogger.setLevel(LogLevel.INFO);
        }
        for (StringBuilder sql : inserts) {
          int cnt = (Integer) qs.doSql(sql.toString());
          total += cnt;
          chunkTotal += cnt;
          logger.info(target, "inserted rows:", total);
        }
        if (isDebugEnabled) {
          messyLogger.setLevel(LogLevel.DEBUG);
        }
        if (chunk > 0) {
          offset += chunk;
        }
      } while (chunk > 0 && chunkTotal == chunk);
    }
  }
}
