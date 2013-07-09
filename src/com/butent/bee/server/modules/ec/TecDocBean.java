package com.butent.bee.server.modules.ec;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.server.data.IdGeneratorBean;
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
import com.butent.bee.shared.data.SimpleRowSet;
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
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;

import pl.motonet.ArrayOfString;
import pl.motonet.WSMotoOferta;
import pl.motonet.WSMotoOfertaSoap;

@Stateless
@LocalBean
public class TecDocBean {

  static {
    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
      private final HostnameVerifier verifier = HttpsURLConnection.getDefaultHostnameVerifier();
      private final String host = WSMotoOferta.WSMOTOOFERTA_WSDL_LOCATION.getHost();

      @Override
      public boolean verify(String hostname, SSLSession session) {
        if (BeeUtils.same(hostname, host)) {
          return true;
        }
        return verifier.verify(hostname, session);
      }
    });
  }

  private class RemoteItems {
    private final String supplierId;
    private final Double price;
    private final String brand;
    private final String articleNr;

    public RemoteItems(String supplierId, String brand, String articleNr, Double price) {
      this.supplierId = supplierId;
      this.brand = brand;
      this.articleNr = articleNr;
      this.price = price;
    }
  }

  private class RemoteRemainders {
    private final String supplierId;
    private final String warehouse;
    private final Double remainder;

    public RemoteRemainders(String supplierId, String warehouse, Double remainder) {
      this.supplierId = supplierId;
      this.warehouse = warehouse;
      this.remainder = remainder;
    }
  }

  private static BeeLogger logger = LogUtils.getLogger(TecDocBean.class);
  private static BeeLogger messyLogger = LogUtils.getLogger(QueryServiceBean.class);

  private static final String TCD_SCHEMA = "TecDoc";

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  TecDocRemote tcd;
  @EJB
  IdGeneratorBean ig;

  @Asynchronous
  public void justDoIt() {
    Set<String> names = Sets.newHashSet(qs.dbTables(sys.getDbName(), sys.getDbSchema(), null)
        .getColumn(SqlConstants.TBL_NAME));

    String articleBrands = "TcdArticleBrands";
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

      qs.updateData(SqlUtils.createIndex(articleCriteria, "IK_" + articleCriteria + "ArticleID",
          Lists.newArrayList("ArticleID"), false));

      qs.updateData(SqlUtils.createForeignKey(articleCriteria, "FK_" + articleCriteria + articles,
          Lists.newArrayList("ArticleID"), articles, Lists.newArrayList("ArticleID"),
          SqlKeyword.DELETE));
    }
    String analogs = "TcdAnalogs";
    String tcdAnalogs = SqlUtils.table("TecDoc", analogs);

    if (!names.contains(analogs)) {
      qs.updateData(new SqlCreate(analogs, false)
          .setDataSource(new SqlSelect()
              .addAllFields(tcdAnalogs)
              .addFrom(tcdAnalogs)
              .addFromInner(articles, SqlUtils.joinUsing(tcdAnalogs, articles, "ArticleID"))));

      qs.updateData(SqlUtils.createIndex(analogs, "IK_" + analogs + "SearchNr",
          Lists.newArrayList("SearchNr"), false));

      qs.updateData(SqlUtils.createIndex(analogs, "IK_" + analogs + "ArticleID",
          Lists.newArrayList("ArticleID"), false));

      qs.updateData(SqlUtils.createForeignKey(analogs, "FK_" + analogs + articles,
          Lists.newArrayList("ArticleID"), articles, Lists.newArrayList("ArticleID"),
          SqlKeyword.DELETE));
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

      qs.updateData(SqlUtils.createIndex(categories, "IK_" + categories + "CategoryID",
          Lists.newArrayList("CategoryID"), false));

      qs.updateData(SqlUtils.createForeignKey(categories, "FK_" + categories + categories,
          Lists.newArrayList("ParentID"), categories, Lists.newArrayList("CategoryID"),
          SqlKeyword.DELETE));

      String tmp = SqlUtils.uniqueName();

      qs.updateData(SqlUtils.renameTable(articleCategories, tmp));

      qs.updateData(new SqlCreate(articleCategories, false)
          .setDataSource(new SqlSelect().setDistinctMode(true)
              .addFields(tmp, "ArticleID", "CategoryID")
              .addFrom(tmp)
              .addFromInner(categories, SqlUtils.joinUsing(tmp, categories, "CategoryID"))));

      qs.updateData(SqlUtils.createIndex(articleCategories,
          "IK_" + articleCategories + "CategoryID", Lists.newArrayList("CategoryID"), false));

      qs.updateData(SqlUtils.createIndex(articleCategories,
          "IK_" + articleCategories + "ArticleID", Lists.newArrayList("ArticleID"), false));

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

      qs.updateData(SqlUtils.createIndex(typeArticles,
          "IK_" + typeArticles + "ArticleID", Lists.newArrayList("ArticleID"), false));

      qs.updateData(SqlUtils.createIndex(typeArticles,
          "IK_" + typeArticles + "TypeID", Lists.newArrayList("TypeID"), false));

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

      qs.updateData(SqlUtils.createIndex(types,
          "IK_" + types + "ModelID", Lists.newArrayList("ModelID"), false));

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
    String supplier = "EOLTAS";

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
      logger.severe(supplier, error);
      return;
    }
    logger.info(supplier, "Waiting for items...");

    response = port.process("GetSQLData", "<query>SELECT preke AS pr, savikaina AS kn,"
        + " gam_art AS ga, gamintojas AS gam FROM prekes"
        + " WHERE gamintojas IS NOT NULL AND gam_art IS NOT NULL</query>");

    Node node = XmlUtils.fromString(response).getFirstChild();

    if (BeeUtils.same(node.getNodeName(), "Error")) {
      logger.severe(supplier, node.getTextContent());
      return;
    }
    if (node.hasChildNodes()) {
      int size = node.getChildNodes().getLength();
      List<RemoteItems> data = Lists.newArrayListWithExpectedSize(size);

      logger.info(supplier, "Received", size, "records. Updating data...");

      for (int i = 0; i < size; i++) {
        Node row = node.getChildNodes().item(i);

        if (row.hasChildNodes()) {
          Map<String, String> info = XmlUtils.getElements(row.getChildNodes(), null);

          data.add(new RemoteItems(info.get("pr"), info.get("gam"), info.get("ga"),
              BeeUtils.toDoubleOrNull(info.get("kn"))));
        }
      }
      importItems(supplier, data);
    }
    logger.info(supplier, "Waiting for remainders...");

    response = port.process("GetSQLData", "<query>SELECT likuciai.sandelis AS sn,"
        + " likuciai.preke AS pr, sum(likuciai.kiekis) AS lk"
        + " FROM likuciai INNER JOIN prekes ON likuciai.preke = prekes.preke"
        + " AND prekes.gam_art IS NOT NULL AND prekes.gamintojas IS NOT NULL"
        + " GROUP by likuciai.sandelis, likuciai.preke HAVING lk > 0</query>");

    node = XmlUtils.fromString(response).getFirstChild();

    if (BeeUtils.same(node.getNodeName(), "Error")) {
      logger.severe(supplier, node.getTextContent());
      return;
    }
    if (node.hasChildNodes()) {
      int size = node.getChildNodes().getLength();
      List<RemoteRemainders> data = Lists.newArrayListWithExpectedSize(size);

      logger.info(supplier, "Received", size, "records. Updating data...");

      for (int i = 0; i < size; i++) {
        Node row = node.getChildNodes().item(i);

        if (row.hasChildNodes()) {
          Map<String, String> info = XmlUtils.getElements(row.getChildNodes(), null);

          data.add(new RemoteRemainders(info.get("pr"), info.get("sn"),
              BeeUtils.toDoubleOrNull(info.get("lk"))));
        }
      }
      importRemainders(supplier, data);

    } else {
      logger.info(supplier, "webService returned no remainders");
    }
  }

  @Asynchronous
  public void suckMotonet() {
    // keytool -importcert -keystore
    // /Applications/JavaEE/JavaEE7AS/glassfish4/glassfish/domains/domain1/config/cacerts.jks
    // -storepass changeit -alias motonet -file motonet.cer

    String supplier = "MOTOPROFIL";
    logger.info(supplier, "Waiting for data...");

    WSMotoOfertaSoap port = new WSMotoOferta().getWSMotoOfertaSoap();
    ArrayOfString info = port.zwrocCennikDetalOffline("10431", "6492", "BEE");

    if (info != null && info.getString().size() > 1) {
      int size = info.getString().size();
      List<RemoteItems> items = Lists.newArrayListWithExpectedSize(size);
      List<RemoteRemainders> remainders = Lists.newArrayListWithExpectedSize(size);

      logger.info(supplier, "Received", size, "records. Updating data...");

      SimpleRowSet rs = qs.getData(new SqlSelect()
          .addFields(TBL_TCD_BRANDS_MAPPING, COL_TCD_SUPPLIER_BRAND, COL_TCD_TECDOC_BRAND)
          .addFrom(TBL_TCD_BRANDS_MAPPING));

      for (String item : info.getString()) {
        String[] values = item.split("[|]", 8);

        if (values.length == 8) {
          String brand = rs.getValueByKey(COL_TCD_SUPPLIER_BRAND, values[0], COL_TCD_TECDOC_BRAND);
          String supplierId = values[0] + values[1];

          if (!BeeUtils.isEmpty(brand)) {
            items.add(new RemoteItems(supplierId, brand, values[1],
                BeeUtils.toDoubleOrNull(values[7].replace(',', '.'))));

            remainders.add(new RemoteRemainders(supplierId, "MotoNet",
                BeeUtils.toDoubleOrNull(values[3])));
          }
        }
      }
      importItems(supplier, items);
      importRemainders(supplier, remainders);

    } else {
      logger.info(supplier, "webService returned no data");
    }
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

  private void importItems(String supplier, List<RemoteItems> data) {
    String log = supplier + " " + TBL_TCD_ARTICLE_BRANDS + ":";

    boolean isDebugEnabled = messyLogger.isDebugEnabled();

    if (isDebugEnabled) {
      messyLogger.setLevel(LogLevel.INFO);
    }
    String idName = sys.getIdName(TBL_TCD_ARTICLE_BRANDS);
    int tot = 0;
    int upd = 0;
    String tmp = null;

    for (RemoteItems info : data) {
      int c = qs.updateData(new SqlUpdate(TBL_TCD_ARTICLE_BRANDS)
          .addConstant(COL_TCD_PRICE, info.price)
          .setWhere(SqlUtils.equals(TBL_TCD_ARTICLE_BRANDS,
              COL_TCD_SUPPLIER, supplier, COL_TCD_SUPPLIER_ID, info.supplierId)));
      upd += c;

      if (c == 0) {
        if (tmp == null) {
          tmp = SqlUtils.temporaryName();

          qs.updateData(new SqlCreate(tmp)
              .addLong(idName, true)
              .addString(COL_TCD_SUPPLIER_ID, 30, true)
              .addString(COL_TCD_ANALOG_NR, 50, true)
              .addString(COL_TCD_BRAND, 50, true)
              .addString(COL_TCD_SEARCH_NR, 50, true)
              .addDecimal(COL_TCD_PRICE, 10, 2, false));
        }
        qs.insertData(new SqlInsert(tmp)
            .addConstant(idName, ig.getId(TBL_TCD_ARTICLE_BRANDS))
            .addConstant(COL_TCD_SEARCH_NR, EcModuleBean.normalizeCode(info.articleNr))
            .addConstant(COL_TCD_BRAND, info.brand)
            .addConstant(COL_TCD_ANALOG_NR, info.articleNr)
            .addConstant(COL_TCD_PRICE, info.price)
            .addConstant(COL_TCD_SUPPLIER_ID, info.supplierId));
      }
      if (++tot % 1000 == 0) {
        logger.info(log, "Processed", tot, "records");
      }
    }
    if (isDebugEnabled) {
      messyLogger.setLevel(LogLevel.DEBUG);
    }
    if (tot % 1000 > 0) {
      logger.info(log, "Processed", tot, "records");
    }
    logger.info(log, "Updated", upd, "rows");

    if (tmp != null) {
      qs.updateData(SqlUtils.createIndex(tmp, "IK_" + tmp + COL_TCD_SEARCH_NR,
          Lists.newArrayList(COL_TCD_SEARCH_NR), false));
      qs.updateData(SqlUtils.createIndex(tmp, "IK_" + tmp + COL_TCD_BRAND,
          Lists.newArrayList(COL_TCD_BRAND), false));

      String tcdAnalogs = SqlUtils.table(TCD_SCHEMA, TBL_TCD_ANALOGS);

      upd = qs.sqlCount(TBL_TCD_ARTICLE_BRANDS, null);

      qs.insertData(new SqlInsert(TBL_TCD_ARTICLE_BRANDS)
          .addFields(COL_TCD_ARTICLE_ID, COL_TCD_BRAND, COL_TCD_ANALOG_NR, COL_TCD_PRICE, idName,
              COL_TCD_SUPPLIER_ID, COL_TCD_SUPPLIER, sys.getVersionName(TBL_TCD_ARTICLE_BRANDS))
          .setDataSource(new SqlSelect()
              .addMax(tcdAnalogs, COL_TCD_ARTICLE_ID)
              .addFields(tmp, COL_TCD_BRAND, COL_TCD_ANALOG_NR, COL_TCD_PRICE, idName,
                  COL_TCD_SUPPLIER_ID)
              .addConstant(supplier, COL_TCD_SUPPLIER)
              .addConstant(System.currentTimeMillis(),
                  sys.getVersionName(TBL_TCD_ARTICLE_BRANDS))
              .addFrom(tmp)
              .addFromInner(tcdAnalogs,
                  SqlUtils.joinUsing(tmp, tcdAnalogs, COL_TCD_SEARCH_NR, COL_TCD_BRAND))
              .addGroup(tmp, COL_TCD_BRAND, COL_TCD_ANALOG_NR, COL_TCD_PRICE,
                  COL_TCD_SUPPLIER_ID, idName)));

      qs.sqlDropTemp(tmp);
      logger.info(log, "Inserted", qs.sqlCount(TBL_TCD_ARTICLE_BRANDS, null) - upd, "rows");
    }
  }

  private void importRemainders(String supplier, List<RemoteRemainders> data) {
    String log = supplier + " " + TBL_TCD_REMAINDERS + ":";

    qs.updateData(new SqlUpdate(TBL_TCD_REMAINDERS)
        .addConstant(COL_TCD_REMAINDER, null)
        .setFrom(TBL_TCD_ARTICLE_BRANDS,
            sys.joinTables(TBL_TCD_ARTICLE_BRANDS, TBL_TCD_REMAINDERS, COL_TCD_ARTICLE_BRAND))
        .setWhere(SqlUtils.equals(TBL_TCD_ARTICLE_BRANDS, COL_TCD_SUPPLIER, supplier)));

    boolean isDebugEnabled = messyLogger.isDebugEnabled();

    if (isDebugEnabled) {
      messyLogger.setLevel(LogLevel.INFO);
    }
    String idName = sys.getIdName(TBL_TCD_REMAINDERS);
    int tot = 0;
    int upd = 0;
    String tmp = null;

    for (RemoteRemainders info : data) {
      int c = qs.updateData(new SqlUpdate(TBL_TCD_REMAINDERS)
          .addConstant(COL_TCD_REMAINDER, info.remainder)
          .setFrom(TBL_TCD_ARTICLE_BRANDS, sys.joinTables(TBL_TCD_ARTICLE_BRANDS,
              TBL_TCD_REMAINDERS, COL_TCD_ARTICLE_BRAND))
          .setWhere(SqlUtils.and(SqlUtils.equals(TBL_TCD_REMAINDERS,
              COL_TCD_WAREHOUSE, info.warehouse),
              SqlUtils.equals(TBL_TCD_ARTICLE_BRANDS,
                  COL_TCD_SUPPLIER, supplier, COL_TCD_SUPPLIER_ID, info.supplierId))));
      upd += c;

      if (c == 0) {
        if (tmp == null) {
          tmp = SqlUtils.temporaryName();

          qs.updateData(new SqlCreate(tmp, false)
              .addLong(idName, true)
              .addString(COL_TCD_SUPPLIER_ID, 30, true)
              .addString(COL_TCD_WAREHOUSE, 10, true)
              .addDecimal(COL_TCD_REMAINDER, 12, 3, true));
        }
        qs.insertData(new SqlInsert(tmp)
            .addConstant(idName, ig.getId(TBL_TCD_REMAINDERS))
            .addConstant(COL_TCD_WAREHOUSE, info.warehouse)
            .addConstant(COL_TCD_REMAINDER, info.remainder)
            .addConstant(COL_TCD_SUPPLIER_ID, info.supplierId));
      }
      if (++tot % 1000 == 0) {
        logger.info(log, "Processed", tot, "records");
      }
    }
    if (isDebugEnabled) {
      messyLogger.setLevel(LogLevel.DEBUG);
    }
    if (tot % 1000 > 0) {
      logger.info(log, "Processed", tot, "records");
    }
    logger.info(log, "Updated", upd, "rows");

    if (tmp != null) {
      qs.updateData(SqlUtils.createIndex(tmp, "IK_" + tmp + COL_TCD_SUPPLIER_ID,
          Lists.newArrayList(COL_TCD_SUPPLIER_ID), false));

      upd = qs.sqlCount(TBL_TCD_REMAINDERS, null);

      qs.insertData(new SqlInsert(TBL_TCD_REMAINDERS)
          .addFields(COL_TCD_ARTICLE_BRAND, COL_TCD_WAREHOUSE, COL_TCD_REMAINDER, idName,
              sys.getVersionName(TBL_TCD_REMAINDERS))
          .setDataSource(new SqlSelect()
              .addFields(TBL_TCD_ARTICLE_BRANDS, sys.getIdName(TBL_TCD_ARTICLE_BRANDS))
              .addFields(tmp, COL_TCD_WAREHOUSE, COL_TCD_REMAINDER, idName)
              .addConstant(System.currentTimeMillis(), sys.getVersionName(TBL_TCD_REMAINDERS))
              .addFrom(tmp)
              .addFromInner(TBL_TCD_ARTICLE_BRANDS,
                  SqlUtils.joinUsing(tmp, TBL_TCD_ARTICLE_BRANDS, COL_TCD_SUPPLIER_ID))
              .setWhere(SqlUtils.equals(TBL_TCD_ARTICLE_BRANDS, COL_TCD_SUPPLIER, supplier))));

      qs.sqlDropTemp(tmp);
      logger.info(log, "Inserted", qs.sqlCount(TBL_TCD_REMAINDERS, null) - upd, "rows");
    }
  }
}
