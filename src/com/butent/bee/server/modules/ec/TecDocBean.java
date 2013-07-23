package com.butent.bee.server.modules.ec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.SqlConstants;
import com.butent.bee.shared.data.SqlConstants.SqlFunction;
import com.butent.bee.shared.data.SqlConstants.SqlKeyword;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.ArrayUtils;
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

  private class TcdData {
    private final SqlCreate base;
    private final SqlSelect baseSource;
    private String[] baseIndexes;
    private final List<Pair<SqlCreate, String>> preparations = Lists.newArrayList();

    public TcdData(SqlCreate base, SqlSelect baseSource) {
      this.base = base;
      this.baseSource = baseSource;
    }

    private void addPreparation(Pair<SqlCreate, String> preparation) {
      this.preparations.add(preparation);
    }

    private void setBaseIndexes(String... fldList) {
      this.baseIndexes = fldList;
    }
  }

  private static BeeLogger logger = LogUtils.getLogger(TecDocBean.class);
  private static BeeLogger messyLogger = LogUtils.getLogger(QueryServiceBean.class);

  private static final String TCD_SCHEMA = "TecDoc";

  private static ResponseObject getSQLData(String address, String login, String password,
      String query) {
    ButentWS butentWS = ButentWS.create(address);
    ButentWebServiceSoapPort port = butentWS.getButentWebServiceSoapPort();

    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
        butentWS.getWSDLDocumentLocation().toString());

    String response = port.login(login, password);
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
      return ResponseObject.error(error);
    }
    response = port.process("GetSQLData", "<query>" + query + "</query>");

    Node node = XmlUtils.fromString(response).getFirstChild();

    if (BeeUtils.same(node.getNodeName(), "Error")) {
      return ResponseObject.error(node.getTextContent());
    }
    return ResponseObject.response(node);
  }

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

    // ---------------- TcdArticleCriteria
    String articleCriteria = "TcdArticleCriteria";
    String tcdArticleCriteria = SqlUtils.table(TCD_SCHEMA, articleCriteria);
    String tcdCriteria = SqlUtils.table(TCD_SCHEMA, "TcdCriteria");

    if (names.contains(articleCriteria)) {
      dropTable(articleCriteria);
    }
    qs.updateData(new SqlCreate(articleCriteria, false)
        .setDataSource(new SqlSelect()
            .addFields(tcdArticleCriteria, "ArticleID", "Value")
            .addFields(tcdCriteria, "Name")
            .addFrom(tcdArticleCriteria)
            .addFromInner(TBL_TCD_ARTICLES,
                sys.joinTables(TBL_TCD_ARTICLES, tcdArticleCriteria, "ArticleID"))
            .addFromInner(tcdCriteria,
                SqlUtils.joinUsing(tcdArticleCriteria, tcdCriteria, "CriteriaID"))));

    qs.updateData(SqlUtils.createIndex(articleCriteria, "IK_" + articleCriteria + "ArticleID",
        Lists.newArrayList("ArticleID"), false));

    qs.updateData(SqlUtils.createForeignKey(articleCriteria,
        "FK_" + articleCriteria + TBL_TCD_ARTICLES, Lists.newArrayList("ArticleID"),
        TBL_TCD_ARTICLES, Lists.newArrayList(COL_TCD_ARTICLE_ID), SqlKeyword.DELETE));

    // ---------------- TcdAnalogs
    String analogs = "TcdAnalogs";
    String tcdAnalogs = SqlUtils.table(TCD_SCHEMA, analogs);

    if (names.contains(analogs)) {
      dropTable(analogs);
    }
    qs.updateData(new SqlCreate(analogs, false)
        .setDataSource(new SqlSelect()
            .addAllFields(tcdAnalogs)
            .addFrom(tcdAnalogs)
            .addFromInner(TBL_TCD_ARTICLES,
                sys.joinTables(TBL_TCD_ARTICLES, tcdAnalogs, "ArticleID"))));

    qs.updateData(SqlUtils.createIndex(analogs, "IK_" + analogs + "SearchNr",
        Lists.newArrayList("SearchNr"), false));

    qs.updateData(SqlUtils.createIndex(analogs, "IK_" + analogs + "ArticleID",
        Lists.newArrayList("ArticleID"), false));

    qs.updateData(SqlUtils.createForeignKey(analogs, "FK_" + analogs + TBL_TCD_ARTICLES,
        Lists.newArrayList("ArticleID"), TBL_TCD_ARTICLES, Lists.newArrayList(COL_TCD_ARTICLE_ID),
        SqlKeyword.DELETE));

    // ---------------- TcdCategories...
    String tcdArticlesToCategories = SqlUtils.table(TCD_SCHEMA, "TcdArticlesToCategories");
    String articleCategories = "ArticleCategories";
    String categories = "TcdCategories";
    String tcdCategories = SqlUtils.table(TCD_SCHEMA, categories);

    if (names.contains(articleCategories)) {
      dropTable(articleCategories);
    }
    if (names.contains(categories)) {
      dropTable(categories);
    }
    qs.updateData(new SqlCreate(articleCategories, false)
        .setDataSource(new SqlSelect()
            .addAllFields(tcdArticlesToCategories)
            .addFrom(tcdArticlesToCategories)
            .addFromInner(TBL_TCD_ARTICLES,
                sys.joinTables(TBL_TCD_ARTICLES, tcdArticlesToCategories, "ArticleID"))));

    qs.sqlIndex(articleCategories, "CategoryID");

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
        "FK_" + articleCategories + TBL_TCD_ARTICLES, Lists.newArrayList("ArticleID"),
        TBL_TCD_ARTICLES, Lists.newArrayList(COL_TCD_ARTICLE_ID), SqlKeyword.DELETE));

    qs.updateData(SqlUtils.createForeignKey(articleCategories,
        "FK_" + articleCategories + categories,
        Lists.newArrayList("CategoryID"), categories, Lists.newArrayList("CategoryID"),
        SqlKeyword.DELETE));

    qs.updateData(SqlUtils.dropTable(tmp));

    // ---------------- TcdManufacturers
    String models = "TcdModels";
    String tcdModels = SqlUtils.table(TCD_SCHEMA, models);

    String[] newMfs = qs.getColumn(new SqlSelect().setDistinctMode(true)
        .addFields(tcdModels, COL_TCD_MANUFACTURER_NAME)
        .addFrom(tcdModels)
        .setWhere(SqlUtils.not(SqlUtils.in(tcdModels, COL_TCD_MANUFACTURER_NAME,
            TBL_TCD_MANUFACTURERS, COL_TCD_MANUFACTURER_NAME, null))));

    for (String mf : newMfs) {
      qs.insertData(new SqlInsert(TBL_TCD_MANUFACTURERS)
          .addConstant(COL_TCD_MANUFACTURER_NAME, mf)
          .addConstant(COL_TCD_MF_VISIBLE, true));
    }

    // ---------------- TcdModels
    if (names.contains(models)) {
      dropTable(models);
    }
    qs.updateData(new SqlCreate(models, false)
        .setDataSource(new SqlSelect()
            .addFields(tcdModels, COL_TCD_MODEL_ID, COL_TCD_MODEL_NAME)
            .addField(TBL_TCD_MANUFACTURERS, sys.getIdName(TBL_TCD_MANUFACTURERS),
                COL_TCD_MANUFACTURER)
            .addFrom(tcdModels)
            .addFromInner(TBL_TCD_MANUFACTURERS,
                SqlUtils.joinUsing(tcdModels, TBL_TCD_MANUFACTURERS, COL_TCD_MANUFACTURER_NAME))
            .setWhere(SqlUtils.notNull(TBL_TCD_MANUFACTURERS, COL_TCD_MF_VISIBLE))));

    qs.updateData(SqlUtils.createPrimaryKey(models, "PK_" + models,
        Lists.newArrayList(COL_TCD_MODEL_ID)));

    qs.updateData(SqlUtils.createForeignKey(models, "FK_" + models + TBL_TCD_MANUFACTURERS,
        Lists.newArrayList(COL_TCD_MANUFACTURER), TBL_TCD_MANUFACTURERS,
        Lists.newArrayList(sys.getIdName(TBL_TCD_MANUFACTURERS)), SqlKeyword.DELETE));

    // ---------------- TcdTypes
    String types = "TcdTypes";
    String tcdTypes = SqlUtils.table(TCD_SCHEMA, types);

    if (names.contains(types)) {
      dropTable(types);
    }
    qs.updateData(new SqlCreate(types, false)
        .setDataSource(new SqlSelect()
            .addAllFields(tcdTypes)
            .addFrom(tcdTypes)
            .addFromInner(models, SqlUtils.joinUsing(tcdTypes, models, COL_TCD_MODEL_ID))));

    qs.updateData(SqlUtils.createPrimaryKey(types, "PK_" + types,
        Lists.newArrayList("TypeID")));

    qs.updateData(SqlUtils.createIndex(types,
        "IK_" + types + COL_TCD_MODEL_ID, Lists.newArrayList(COL_TCD_MODEL_ID), false));

    qs.updateData(SqlUtils.createForeignKey(types, "FK_" + types + models,
        Lists.newArrayList(COL_TCD_MODEL_ID), models, Lists.newArrayList(COL_TCD_MODEL_ID),
        SqlKeyword.DELETE));

    // ---------------- TcdTypeArticles
    String tcdArticlesToTypes = SqlUtils.table(TCD_SCHEMA, "TcdArticlesToTypes");
    String typeArticles = "TcdTypeArticles";

    if (names.contains(typeArticles)) {
      dropTable(typeArticles);
    }
    qs.updateData(new SqlCreate(typeArticles, false)
        .setDataSource(new SqlSelect()
            .addAllFields(tcdArticlesToTypes)
            .addFrom(tcdArticlesToTypes)
            .addFromInner(TBL_TCD_ARTICLES,
                sys.joinTables(TBL_TCD_ARTICLES, tcdArticlesToTypes, "ArticleID"))
            .addFromInner(types,
                SqlUtils.joinUsing(tcdArticlesToTypes, types, "TypeID"))));

    qs.updateData(SqlUtils.createIndex(typeArticles,
        "IK_" + typeArticles + "ArticleID", Lists.newArrayList("ArticleID"), false));

    qs.updateData(SqlUtils.createIndex(typeArticles,
        "IK_" + typeArticles + "TypeID", Lists.newArrayList("TypeID"), false));

    qs.updateData(SqlUtils.createForeignKey(typeArticles, "FK_" + typeArticles + TBL_TCD_ARTICLES,
        Lists.newArrayList("ArticleID"), TBL_TCD_ARTICLES, Lists.newArrayList(COL_TCD_ARTICLE_ID),
        SqlKeyword.DELETE));

    qs.updateData(SqlUtils.createForeignKey(typeArticles, "FK_" + typeArticles + types,
        Lists.newArrayList("TypeID"), types, Lists.newArrayList("TypeID"), SqlKeyword.DELETE));
  }

  @Asynchronous
  public void suckButent() {
    String supplier = "EOLTAS";
    String address = "http://82.135.245.222:8081/ButentWS/ButentWS.WSDL";
    String login = "admin";
    String password = "gruntas";

    logger.info(supplier, "Waiting for items...");

    ResponseObject response = getSQLData(address, login, password,
        "SELECT preke AS pr, savikaina AS kn, gam_art AS ga, gamintojas AS gam"
            + " FROM prekes"
            + " WHERE gamintojas IS NOT NULL AND gam_art IS NOT NULL");

    if (response.hasErrors()) {
      logger.severe(supplier, response.getErrors());
      return;
    }
    Node node = (Node) response.getResponse();

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

    response = getSQLData(address, login, password,
        "SELECT likuciai.sandelis AS sn, likuciai.preke AS pr, sum(likuciai.kiekis) AS lk"
            + " FROM likuciai INNER JOIN prekes ON likuciai.preke = prekes.preke"
            + " AND prekes.gam_art IS NOT NULL AND prekes.gamintojas IS NOT NULL"
            + " GROUP by likuciai.sandelis, likuciai.preke HAVING lk > 0");

    if (response.hasErrors()) {
      logger.severe(supplier, response.getErrors());
      return;
    }
    node = (Node) response.getResponse();

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
          .addFields(TBL_TCD_BRANDS_MAPPING, COL_TCD_SUPPLIER_BRAND)
          .addFields(TBL_TCD_BRANDS, COL_TCD_BRAND_NAME)
          .addFrom(TBL_TCD_BRANDS_MAPPING)
          .addFromInner(TBL_TCD_BRANDS,
              sys.joinTables(TBL_TCD_BRANDS, TBL_TCD_BRANDS_MAPPING, COL_TCD_BRAND)));

      for (String item : info.getString()) {
        String[] values = item.split("[|]", 8);

        if (values.length == 8) {
          String brand = rs.getValueByKey(COL_TCD_SUPPLIER_BRAND, values[0], COL_TCD_BRAND_NAME);
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
  public void suckTecdoc() {
    List<Pair<SqlCreate, String>> init = Lists.newArrayList();

    init.add(Pair.of(new SqlCreate("_country_designations", false)
        .setDataSource(new SqlSelect()
            .addFields("tof_country_designations", "cds_id")
            .addMax("tof_des_texts", "tex_text")
            .addFrom("tof_country_designations")
            .addFromInner("tof_des_texts", SqlUtils.join("tof_country_designations",
                "cds_tex_id", "tof_des_texts", "tex_id"))
            .setWhere(SqlUtils.equals("tof_country_designations", "cds_lng_id", 34))
            .addGroup("tof_country_designations", "cds_id")),
        "cds_id"));

    init.add(Pair.of(new SqlCreate("_designations", false)
        .setDataSource(new SqlSelect().setUnionAllMode(true)
            .addFields("tof_designations", "des_id")
            .addFields("tof_des_texts", "tex_text")
            .addFrom("tof_designations")
            .addFromInner("tof_des_texts",
                SqlUtils.join("tof_designations", "des_tex_id", "tof_des_texts", "tex_id"))
            .setWhere(SqlUtils.equals("tof_designations", "des_lng_id", 34))
            .addUnion(new SqlSelect()
                .addFields("tof_designations", "des_id")
                .addFields("tof_des_texts", "tex_text")
                .addFrom("tof_designations")
                .addFromInner("tof_des_texts",
                    SqlUtils.join("tof_designations", "des_tex_id", "tof_des_texts", "tex_id"))
                .setWhere(SqlUtils.and(SqlUtils.equals("tof_designations", "des_lng_id", 255),
                    SqlUtils.not(SqlUtils.in("tof_designations", "des_id", "tof_designations",
                        "des_id", SqlUtils.equals("tof_designations", "des_lng_id", 34))))))),
        "des_id"));

    List<TcdData> builds = Lists.newArrayList();

    TcdData data = new TcdData(new SqlCreate("TcdModels", false)
        .addInteger("ModelID", true)
        .addString("ModelName", 50, true)
        .addString("Manufacturer", 50, true),
        new SqlSelect()
            .addField("tof_models", "mod_id", "ModelID")
            .addField("_country_designations", "tex_text", "ModelName")
            .addField("tof_manufacturers", "mfa_brand", "ManufacturerName")
            .addFrom("tof_models")
            .addFromInner("tof_manufacturers",
                SqlUtils.join("tof_models", "mod_mfa_id", "tof_manufacturers", "mfa_id"))
            .addFromInner("_country_designations",
                SqlUtils.join("tof_models", "mod_cds_id", "_country_designations", "cds_id")));

    builds.add(data);

    data = new TcdData(new SqlCreate("TcdTypes", false)
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
            .addField("_country_designations", "tex_text", "TypeName")
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
            .addFromInner("_country_designations",
                SqlUtils.join("tof_types", "typ_cds_id", "_country_designations", "cds_id"))
            .addFromLeft("_designations", "_engines",
                SqlUtils.join("tof_types", "typ_kv_engine_des_id", "_engines", "des_id"))
            .addFromLeft("_designations", "_fuels",
                SqlUtils.join("tof_types", "typ_kv_fuel_des_id", "_fuels", "des_id"))
            .addFromLeft("_designations", "_bodies",
                SqlUtils.join("tof_types", "typ_kv_body_des_id", "_bodies", "des_id"))
            .addFromLeft("_designations", "_models",
                SqlUtils.join("tof_types", "typ_kv_model_des_id", "_models", "des_id"))
            .addFromLeft("_designations", "_axles",
                SqlUtils.join("tof_types", "typ_kv_axle_des_id", "_axles", "des_id")));

    builds.add(data);

    data = new TcdData(new SqlCreate("TcdArticles", false)
        .addInteger("ArticleID", true)
        .addString("ArticleNr", 50, true)
        .addString("ArticleName", 50, true)
        .addString("Supplier", 50, true),
        new SqlSelect().setLimit(100000)
            .addField("tof_articles", "art_id", "ArticleID")
            .addField("tof_articles", "art_article_nr", "ArticleNr")
            .addField("_designations", "tex_text", "ArticleName")
            .addField("tof_suppliers", "sup_brand", "Supplier")
            .addFrom("tof_articles")
            .addFromInner("tof_suppliers",
                SqlUtils.join("tof_articles", "art_sup_id", "tof_suppliers", "sup_id"))
            .addFromInner("_designations",
                SqlUtils.join("tof_articles", "art_complete_des_id", "_designations", "des_id")));

    builds.add(data);

    data = new TcdData(new SqlCreate("TcdCriteria", false)
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
                SqlUtils.join("tof_criteria", "cri_unit_des_id", "unit", "des_id")));

    data.setBaseIndexes("CriteriaID");
    builds.add(data);

    data = new TcdData(new SqlCreate("TcdArticleCriteria", false)
        .addInteger("ArticleID", true)
        .addInteger("CriteriaID", true)
        .addString("Value", 50, false),
        new SqlSelect().setLimit(1000000)
            .addField("_article_criteria", "acr_art_id", "ArticleID")
            .addField("_article_criteria", "acr_cri_id", "CriteriaID")
            .addFields("_article_criteria", "Value")
            .addFrom("_article_criteria"));

    data.setBaseIndexes("ArticleID", "CriteriaID");

    data.addPreparation(Pair.of(new SqlCreate("_article_criteria", false)
        .setDataSource(new SqlSelect().setDistinctMode(true)
            .addFields("tof_article_criteria", "acr_art_id", "acr_cri_id")
            .addExpr(SqlUtils.sqlCase(SqlUtils.name("acr_value"),
                SqlUtils.constant("NULL"), SqlUtils.field("_designations", "tex_text"),
                "acr_value"), "Value")
            .addFrom("tof_article_criteria")
            .addFromLeft("_designations", SqlUtils.join("tof_article_criteria", "acr_kv_des_id",
                "_designations", "des_id"))),
        (String) null));

    builds.add(data);

    data = new TcdData(new SqlCreate("TcdAnalogs", false)
        .addInteger("ArticleID", true)
        .addString("SearchNr", 50, true)
        .addString("AnalogNr", 50, true)
        .addDecimal("Kind", 1, 0, true)
        .addString("Brand", 50, false),
        new SqlSelect().setLimit(500000)
            .addField("_analogs", "arl_art_id", "ArticleID")
            .addField("_analogs", "arl_search_number", "SearchNr")
            .addField("_analogs", "arl_display_nr", "AnalogNr")
            .addField("_analogs", "arl_kind", "Kind")
            .addField("_analogs", "bra_brand", "Brand")
            .addFrom("_analogs"));

    data.setBaseIndexes("ArticleID", "SearchNr", "Brand");

    data.addPreparation(Pair.of(new SqlCreate("_analogs", false)
        .setDataSource(new SqlSelect().setDistinctMode(true)
            .addExpr(SqlUtils.expression("CAST(arl_art_id AS UNSIGNED)"), "arl_art_id")
            .addFields("tof_art_lookup", "arl_search_number", "arl_display_nr")
            .addExpr(SqlUtils.expression("CAST(arl_kind AS DECIMAL(1))"), "arl_kind")
            .addFields("tof_brands", "bra_brand")
            .addFrom("tof_art_lookup")
            .addFromLeft("tof_brands",
                SqlUtils.join("tof_art_lookup", "arl_bra_id", "tof_brands", "bra_id"))),
        (String) null));

    builds.add(data);

    data = new TcdData(new SqlCreate("TcdArticlesToTypes", false)
        .addInteger("ArticleID", true)
        .addInteger("TypeID", true),
        new SqlSelect().setLimit(1000000)
            .addField("_articles_to_types", "la_art_id", "ArticleID")
            .addField("_articles_to_types", "lat_typ_id", "TypeID")
            .addFrom("_articles_to_types"));

    data.setBaseIndexes("ArticleID");

    data.addPreparation(Pair.of(new SqlCreate("_art", false)
        .setDataSource(new SqlSelect()
            .addExpr(SqlUtils.expression("CAST(la_id AS UNSIGNED)"), "la_id")
            .addExpr(SqlUtils.expression("CAST(la_art_id AS UNSIGNED)"), "la_art_id")
            .addFrom("tof_link_art")),
        "la_id"));

    data.addPreparation(Pair.of(new SqlCreate("_la_typ", false)
        .setDataSource(new SqlSelect()
            .addExpr(SqlUtils.expression("CAST(lat_la_id AS UNSIGNED)"), "lat_la_id")
            .addExpr(SqlUtils.expression("CAST(lat_typ_id AS UNSIGNED)"), "lat_typ_id")
            .addFrom("tof_link_la_typ")),
        "lat_la_id"));

    data.addPreparation(Pair.of(new SqlCreate("_articles_to_types", false)
        .setDataSource(new SqlSelect().setDistinctMode(true)
            .addFields("_art", "la_art_id")
            .addFields("_la_typ", "lat_typ_id")
            .addFrom("_art")
            .addFromInner("_la_typ",
                SqlUtils.join("_art", "la_id", "_la_typ", "lat_la_id"))),
        (String) null));

    builds.add(data);

    data = new TcdData(new SqlCreate("TcdCategories", false)
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
                SqlUtils.join("tof_search_tree", "str_des_id", "_designations", "des_id")));

    builds.add(data);

    data = new TcdData(new SqlCreate("TcdArticlesToCategories", false)
        .addInteger("ArticleID", true)
        .addInteger("CategoryID", true),
        new SqlSelect().setLimit(1000000)
            .addField("_articles_to_categories", "lag_art_id", "ArticleID")
            .addField("_articles_to_categories", "lgs_str_id", "CategoryID")
            .addFrom("_articles_to_categories"));

    data.setBaseIndexes("ArticleID");

    data.addPreparation(Pair.of(new SqlCreate("_art_ga", false)
        .setDataSource(new SqlSelect()
            .addExpr(SqlUtils.expression("CAST(lag_ga_id AS UNSIGNED)"), "lag_ga_id")
            .addExpr(SqlUtils.expression("CAST(lag_art_id AS UNSIGNED)"), "lag_art_id")
            .addFrom("tof_link_art_ga")),
        "lag_ga_id"));

    data.addPreparation(Pair.of(new SqlCreate("_ga_str", false)
        .setDataSource(new SqlSelect()
            .addExpr(SqlUtils.expression("CAST(lgs_ga_id AS UNSIGNED)"), "lgs_ga_id")
            .addExpr(SqlUtils.expression("CAST(lgs_str_id AS UNSIGNED)"), "lgs_str_id")
            .addFrom("tof_link_ga_str")),
        "lgs_ga_id"));

    data.addPreparation(Pair.of(new SqlCreate("_articles_to_categories", false)
        .setDataSource(new SqlSelect().setDistinctMode(true)
            .addFields("_art_ga", "lag_art_id")
            .addFields("_ga_str", "lgs_str_id")
            .addFrom("_art_ga")
            .addFromInner("_ga_str",
                SqlUtils.join("_art_ga", "lag_ga_id", "_ga_str", "lgs_ga_id"))),
        (String) null));

    builds.add(data);

    data = new TcdData(new SqlCreate("TcdGraphics", false)
        .addLong("GraphicsID", true)
        .addString("Type", 3, true)
        .addInteger("ResourceID", true)
        .addString("ResourceNo", 2, true),
        new SqlSelect().setLimit(1000000)
            .addField("tof_graphics", "gra_id", "GraphicsID")
            .addField("tof_doc_types", "doc_extension", "Type")
            .addField("tof_graphics", "gra_grd_id", "ResourceID")
            .addField("tof_graphics", "gra_tab_nr", "ResourceNo")
            .addFrom("tof_graphics")
            .addFromLeft("tof_doc_types",
                SqlUtils.join("tof_graphics", "gra_doc_type", "tof_doc_types", "doc_type"))
            .setWhere(SqlUtils.and(SqlUtils.equals("tof_graphics", "gra_lng_id", "255"),
                SqlUtils.notEqual("tof_graphics", "gra_grd_id", "NULL"))));

    data.setBaseIndexes("GraphicsID");

    builds.add(data);

    data = new TcdData(new SqlCreate("TcdArticleGraphics", false)
        .addInteger("ArticleID", true)
        .addLong("GraphicsID", true)
        .addDecimal("Sort", 2, 0, true),
        new SqlSelect().setLimit(1000000)
            .addField("tof_link_gra_art", "lga_art_id", "ArticleID")
            .addField("tof_link_gra_art", "lga_gra_id", "GraphicsID")
            .addField("tof_link_gra_art", "lga_sort", "Sort")
            .addFrom("tof_link_gra_art"));

    data.setBaseIndexes("ArticleID");

    builds.add(data);

    if (!qs.dbSchemaExists(sys.getDbName(), TCD_SCHEMA)) {
      qs.updateData(SqlUtils.createSchema(TCD_SCHEMA));
    }
    tcd.init(init);

    for (TcdData entry : builds) {
      SqlCreate create = entry.base;
      SqlSelect query = entry.baseSource;

      String target = create.getTarget();

      if (qs.dbTableExists(sys.getDbName(), TCD_SCHEMA, target)) {
        continue; // qs.updateData(SqlUtils.dropTable(SqlUtils.table(tcdSchema, target)));
      }
      tcd.init(entry.preparations);

      target = SqlUtils.table(TCD_SCHEMA, target);
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

      if (!ArrayUtils.isEmpty(entry.baseIndexes)) {
        for (String index : entry.baseIndexes) {
          qs.sqlIndex(target, index);
        }
      }
      tcd.cleanup(entry.preparations);
    }
    tcd.cleanup(init);

    builds.clear();

    for (String resource : qs.getColumn(new SqlSelect().setDistinctMode(true)
        .addFields(SqlUtils.table(TCD_SCHEMA, "TcdGraphics"), "ResourceNo")
        .addFrom(SqlUtils.table(TCD_SCHEMA, "TcdGraphics")))) {

      String table = "tof_gra_data_" + resource;

      data = new TcdData(new SqlCreate("TcdResources" + resource, false)
          .addInteger("ResourceID", true)
          .addText("Resource", true),
          new SqlSelect().setLimit(500)
              .addField(table, "grd_id", "ResourceID")
              .addField(table, "grd_graphic", "Resource")
              .addFrom(table));

      data.setBaseIndexes("ResourceID");

      builds.add(data);
    }
    for (TcdData entry : builds) {
      SqlCreate create = entry.base;
      SqlSelect query = entry.baseSource;

      String target = create.getTarget();

      if (qs.dbTableExists(sys.getDbName(), TCD_SCHEMA, target)) {
        continue; // qs.updateData(SqlUtils.dropTable(SqlUtils.table(tcdSchema, target)));
      }
      tcd.init(entry.preparations);

      target = SqlUtils.table(TCD_SCHEMA, target);
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

      if (!ArrayUtils.isEmpty(entry.baseIndexes)) {
        for (String index : entry.baseIndexes) {
          qs.sqlIndex(target, index);
        }
      }
      tcd.cleanup(entry.preparations);
    }
  }

  private void dropTable(String tblName) {
    Assert.state(!sys.isTable(tblName));

    for (SimpleRow fKeys : qs
        .dbForeignKeys(sys.getDbName(), sys.getDbSchema(), null, tblName)) {
      String fk = fKeys.getValue(SqlConstants.KEY_NAME);
      String tbl = fKeys.getValue(SqlConstants.TBL_NAME);
      qs.updateData(SqlUtils.dropForeignKey(tbl, fk));
    }
    qs.updateData(SqlUtils.dropTable(tblName));
  }

  private void importItems(String supplier, List<RemoteItems> data) {
    String log = supplier + " " + TBL_TCD_ARTICLE_BRANDS + ":";

    boolean isDebugEnabled = messyLogger.isDebugEnabled();

    if (isDebugEnabled) {
      messyLogger.setLevel(LogLevel.INFO);
    }
    String tcdAnalogs = SqlUtils.table(TCD_SCHEMA, TBL_TCD_ANALOGS);
    String tcdArticles = SqlUtils.table(TCD_SCHEMA, TBL_TCD_ARTICLES);
    String idName = sys.getIdName(TBL_TCD_ARTICLE_BRANDS);
    int upd = 0;
    int tot = 0;
    String tmp = null;
    Map<String, Long> brands = null;

    for (RemoteItems info : data) {
      int c = qs.updateData(new SqlUpdate(TBL_TCD_ARTICLE_BRANDS)
          .addConstant(COL_TCD_UPDATED_PRICE, info.price)
          .setWhere(SqlUtils.equals(TBL_TCD_ARTICLE_BRANDS,
              COL_TCD_SUPPLIER, supplier, COL_TCD_SUPPLIER_ID, info.supplierId)));
      upd += c;

      if (c == 0) {
        if (tmp == null) {
          tmp = qs.sqlCreateTemp(new SqlSelect()
              .addFields(TBL_TCD_ARTICLE_BRANDS, COL_TCD_ARTICLE, COL_TCD_SUPPLIER_ID,
                  COL_TCD_ANALOG_NR, COL_TCD_BRAND, COL_TCD_PRICE, idName)
              .addField(TBL_TCD_ARTICLE_BRANDS, COL_TCD_ANALOG_NR, COL_TCD_SEARCH_NR)
              .addFields(TBL_TCD_BRANDS, COL_TCD_BRAND_NAME)
              .addFrom(TBL_TCD_ARTICLE_BRANDS)
              .addFrom(TBL_TCD_BRANDS)
              .setWhere(SqlUtils.sqlFalse()));
        }
        if (brands == null) {
          brands = Maps.newHashMap();

          for (SimpleRow remoteItems : qs.getData(new SqlSelect()
              .addFields(TBL_TCD_BRANDS, COL_TCD_BRAND_NAME)
              .addField(TBL_TCD_BRANDS, sys.getIdName(TBL_TCD_BRANDS), COL_TCD_BRAND)
              .addFrom(TBL_TCD_BRANDS))) {

            brands.put(remoteItems.getValue(COL_TCD_BRAND_NAME),
                remoteItems.getLong(COL_TCD_BRAND));
          }
        }
        if (!brands.containsKey(info.brand)) {
          brands.put(info.brand, qs.insertData(new SqlInsert(TBL_TCD_BRANDS)
              .addConstant(COL_TCD_BRAND_NAME, info.brand)));
        }
        qs.insertData(new SqlInsert(tmp)
            .addConstant(idName, ig.getId(TBL_TCD_ARTICLE_BRANDS))
            .addConstant(COL_TCD_SEARCH_NR, EcModuleBean.normalizeCode(info.articleNr))
            .addConstant(COL_TCD_BRAND_NAME, info.brand)
            .addConstant(COL_TCD_BRAND, brands.get(info.brand))
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
      qs.sqlIndex(tmp, COL_TCD_SEARCH_NR);

      upd = qs.updateData(new SqlUpdate(tmp)
          .addExpression(COL_TCD_ARTICLE, SqlUtils.field(tcdAnalogs, COL_TCD_ARTICLE_ID))
          .setFrom(tcdAnalogs, SqlUtils.and(SqlUtils.joinUsing(tmp, tcdAnalogs, COL_TCD_SEARCH_NR),
              SqlUtils.join(tmp, COL_TCD_BRAND_NAME, tcdAnalogs, COL_TCD_BRAND))));

      String subq = qs.sqlCreateTemp(new SqlSelect()
          .addAllFields(tmp)
          .addFrom(tmp)
          .setWhere(SqlUtils.notNull(tmp, COL_TCD_ARTICLE)));

      qs.sqlDropTemp(tmp);
      tmp = subq;

      qs.sqlIndex(tmp, COL_TCD_ARTICLE);

      String artIdName = sys.getIdName(TBL_TCD_ARTICLES);
      String artVerName = sys.getVersionName(TBL_TCD_ARTICLES);
      subq = SqlUtils.uniqueName();

      qs.insertData(new SqlInsert(TBL_TCD_ARTICLES)
          .addFields(COL_TCD_ARTICLE_NAME, artIdName, artVerName)
          .setDataSource(new SqlSelect()
              .addFields(tcdArticles, COL_TCD_ARTICLE_NAME)
              .addField(tcdArticles, COL_TCD_ARTICLE_ID, artIdName)
              .addConstant(System.currentTimeMillis(), artVerName)
              .addFrom(tcdArticles)
              .addFromInner(new SqlSelect().setDistinctMode(true)
                  .addField(tmp, COL_TCD_ARTICLE, COL_TCD_ARTICLE_ID)
                  .addFrom(tmp)
                  .addFromLeft(TBL_TCD_ARTICLES,
                      sys.joinTables(TBL_TCD_ARTICLES, tmp, COL_TCD_ARTICLE))
                  .setWhere(SqlUtils.isNull(TBL_TCD_ARTICLES, artIdName)), subq,
                  SqlUtils.joinUsing(tcdArticles, subq, COL_TCD_ARTICLE_ID))));

      qs.insertData(new SqlInsert(TBL_TCD_ARTICLE_BRANDS)
          .addFields(COL_TCD_ARTICLE, COL_TCD_BRAND, COL_TCD_ANALOG_NR, COL_TCD_PRICE,
              COL_TCD_SUPPLIER_ID, COL_TCD_SUPPLIER, COL_TCD_UPDATED_PRICE, idName,
              sys.getVersionName(TBL_TCD_ARTICLE_BRANDS))
          .setDataSource(new SqlSelect()
              .addFields(tmp, COL_TCD_ARTICLE, COL_TCD_BRAND, COL_TCD_ANALOG_NR, COL_TCD_PRICE,
                  COL_TCD_SUPPLIER_ID)
              .addConstant(supplier, COL_TCD_SUPPLIER)
              .addField(tmp, COL_TCD_PRICE, COL_TCD_UPDATED_PRICE)
              .addFields(tmp, idName)
              .addConstant(System.currentTimeMillis(), sys.getVersionName(TBL_TCD_ARTICLE_BRANDS))
              .addFrom(tmp)));

      qs.sqlDropTemp(tmp);
      logger.info(log, "Inserted", upd, "rows");
    }
  }

  private void importRemainders(String supplier, List<RemoteRemainders> data) {
    String log = supplier + " " + TBL_TCD_REMAINDERS + ":";

    String brands = qs.sqlCreateTemp(new SqlSelect()
        .addField(TBL_TCD_ARTICLE_BRANDS, sys.getIdName(TBL_TCD_ARTICLE_BRANDS),
            COL_TCD_ARTICLE_BRAND)
        .addFields(TBL_TCD_ARTICLE_BRANDS, COL_TCD_SUPPLIER_ID)
        .addFrom(TBL_TCD_ARTICLE_BRANDS)
        .setWhere(SqlUtils.equals(TBL_TCD_ARTICLE_BRANDS, COL_TCD_SUPPLIER, supplier)));

    qs.sqlIndex(brands, COL_TCD_ARTICLE_BRAND);
    qs.sqlIndex(brands, COL_TCD_SUPPLIER_ID);

    qs.updateData(new SqlUpdate(TBL_TCD_REMAINDERS)
        .addConstant(COL_TCD_REMAINDER, null)
        .setFrom(brands, SqlUtils.joinUsing(TBL_TCD_REMAINDERS, brands, COL_TCD_ARTICLE_BRAND)));

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
          .setFrom(brands,
              SqlUtils.and(SqlUtils.joinUsing(TBL_TCD_REMAINDERS, brands, COL_TCD_ARTICLE_BRAND),
                  SqlUtils.equals(brands, COL_TCD_SUPPLIER_ID, info.supplierId)))
          .setWhere(SqlUtils.equals(TBL_TCD_REMAINDERS, COL_TCD_WAREHOUSE, info.warehouse)));
      upd += c;

      if (c == 0) {
        if (tmp == null) {
          tmp = qs.sqlCreateTemp(new SqlSelect()
              .addFields(TBL_TCD_ARTICLE_BRANDS, COL_TCD_SUPPLIER_ID)
              .addFields(TBL_TCD_REMAINDERS, COL_TCD_WAREHOUSE, COL_TCD_REMAINDER, idName)
              .addFrom(TBL_TCD_ARTICLE_BRANDS)
              .addFrom(TBL_TCD_REMAINDERS)
              .setWhere(SqlUtils.sqlFalse()));
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
      qs.sqlIndex(tmp, COL_TCD_SUPPLIER_ID);

      upd = qs.sqlCount(TBL_TCD_REMAINDERS, null);

      qs.insertData(new SqlInsert(TBL_TCD_REMAINDERS)
          .addFields(COL_TCD_ARTICLE_BRAND, COL_TCD_WAREHOUSE, COL_TCD_REMAINDER, idName,
              sys.getVersionName(TBL_TCD_REMAINDERS))
          .setDataSource(new SqlSelect()
              .addFields(brands, COL_TCD_ARTICLE_BRAND)
              .addFields(tmp, COL_TCD_WAREHOUSE, COL_TCD_REMAINDER, idName)
              .addConstant(System.currentTimeMillis(), sys.getVersionName(TBL_TCD_REMAINDERS))
              .addFrom(tmp)
              .addFromInner(brands, SqlUtils.joinUsing(tmp, brands, COL_TCD_SUPPLIER_ID))));

      qs.sqlDropTemp(tmp);
      logger.info(log, "Inserted", qs.sqlCount(TBL_TCD_REMAINDERS, null) - upd, "rows");
    }
    qs.sqlDropTemp(brands);
  }
}
