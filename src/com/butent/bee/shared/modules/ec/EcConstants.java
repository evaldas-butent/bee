package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskStatus;
import com.butent.bee.shared.ui.HasCaption;

public class EcConstants {
  
  public enum CartType implements HasCaption {
    MAIN(Localized.constants.ecShoppingCartMain(),
        Localized.constants.ecShoppingCartMainShort(), "shoppingCartMain"),
    ALTERNATIVE(Localized.constants.ecShoppingCartAlternative(),
        Localized.constants.ecShoppingCartAlternativeShort(), "shoppingCartAlternative");
    
    private final String caption;
    private final String label;
    private final String service;

    private CartType(String caption, String label, String service) {
      this.caption = caption;
      this.label = label;
      this.service = service;
    }
    
    @Override
    public String getCaption() {
      return caption;
    }
    
    public String getLabel() {
      return label;
    }

    public String getService() {
      return service;
    }
  }

  public enum ClientType implements HasCaption {
    COMPANY(Localized.constants.ecClientTypeCompany()),
    PERSON(Localized.constants.ecClientTypePerson());
    
    private final String caption;

    private ClientType(String caption) {
      this.caption = caption;
    }
    
    @Override
    public String getCaption() {
      return caption;
    }
  }
  
  public enum EcOrderStatus implements HasCaption {
    NEW(Localized.constants.ecOrderStatusNew()),
    ACTIVE(Localized.constants.ecOrderStatusActive()),
    REJECTED(Localized.constants.ecOrderStatusRejected());

    public static boolean in(int status, TaskStatus... statuses) {
      for (TaskStatus ts : statuses) {
        if (ts.ordinal() == status) {
          return true;
        }
      }
      return false;
    }

    private final String caption;

    private EcOrderStatus(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
  }
  
  public static final String EC_MODULE = "Ec";
  public static final String EC_METHOD = EC_MODULE + "Method";
  
  public static final String SVC_FEATURED_AND_NOVELTY = "featuredAndNovelty";

  public static final String SVC_FINANCIAL_INFORMATION = "financialInformation";
  public static final String SVC_TERMS_OF_DELIVERY = "termsOfDelivery";
  public static final String SVC_CONTACTS = "contacts";

  public static final String SVC_GLOBAL_SEARCH = "globalSearch";

  public static final String SVC_GET_CATEGORIES = "getCategories";

  public static final String SVC_SEARCH_BY_ITEM_CODE = "searchByItemCode";
  public static final String SVC_SEARCH_BY_OE_NUMBER = "searchByOeNumber";
  public static final String SVC_SEARCH_BY_CAR = "searchByCar";
  public static final String SVC_SEARCH_BY_MANUFACTURER = "searchByManufacturer";

  public static final String SVC_GENERAL_ITEMS = "generalItems";
  public static final String SVC_BIKE_ITEMS = "bikeItems";

  public static final String SVC_GET_CAR_MANUFACTURERS = "getCarManufacturers";
  public static final String SVC_GET_CAR_MODELS = "getCarModels";
  public static final String SVC_GET_CAR_TYPES = "getCarTypes";

  public static final String SVC_GET_ITEMS_BY_CAR_TYPE = "getItemsByCarType";

  public static final String SVC_GET_ITEM_MANUFACTURERS = "getItemManufacturers";
  public static final String SVC_GET_ITEMS_BY_MANUFACTURER = "getItemsByManufacturer";
  
  public static final String VAR_PREFIX = Service.RPC_VAR_PREFIX + "ec_";

  public static final String VAR_QUERY = VAR_PREFIX + "query";
  public static final String VAR_OFFSET = VAR_PREFIX + "offset";
  public static final String VAR_LIMIT = VAR_PREFIX + "limit";

  public static final String VAR_MANUFACTURER = VAR_PREFIX + "manufacturer";
  public static final String VAR_MODEL = VAR_PREFIX + "model";
  public static final String VAR_TYPE = VAR_PREFIX + "type";

  public static final String TBL_TCD_ARTICLES = "TcdArticles";
  public static final String TBL_TCD_ANALOGS = "TcdAnalogs";

  public static final String TBL_TCD_CATEGORIES = "TcdCategories";
  public static final String TBL_TCD_ARTICLE_CATEGORIES = "TcdArticleCategories";

  public static final String TBL_TCD_MODELS = "TcdModels";
  public static final String TBL_TCD_TYPES = "TcdTypes";
  public static final String TBL_TCD_TYPE_ARTICLES = "TcdTypeArticles";

  public static final String TBL_TCD_MOTONET = "TcdMotonet";

  public static final String COL_TCD_ARTICLE_ID = "ArticleID";
  public static final String COL_TCD_ARTICLE_NR = "ArticleNr";
  public static final String COL_TCD_ARTICLE_NAME = "ArticleName";
  public static final String COL_TCD_SUPPLIER = "Supplier"; 
  
  public static final String COL_TCD_SEARCH_NR = "SearchNr";
  public static final String COL_TCD_KIND = "Kind";
  public static final String COL_TCD_ANALOG_NR = "AnalogNr";
  
  public static final String COL_TCD_CATEGORY_ID = "CategoryID";
  public static final String COL_TCD_PARENT_ID = "ParentID";
  public static final String COL_TCD_CATEGORY_NAME = "CategoryName";
  
  public static final String COL_TCD_MODEL_ID = "ModelID";
  public static final String COL_TCD_MODEL_NAME = "ModelName";
  public static final String COL_TCD_MANUFACTURER = "Manufacturer";

  public static final String COL_TCD_TYPE_ID = "TypeID";
  public static final String COL_TCD_TYPE_NAME = "TypeName";
  public static final String COL_TCD_PRODUCED_FROM = "ProducedFrom";
  public static final String COL_TCD_PRODUCED_TO = "ProducedTo";
  public static final String COL_TCD_CCM = "Ccm";
  public static final String COL_TCD_KW_FROM = "KwFrom";
  public static final String COL_TCD_KW_TO = "KwTo";
  public static final String COL_TCD_CYLINDERS = "Cylinders";
  public static final String COL_TCD_MAX_WEIGHT = "MaxWeight";
  public static final String COL_TCD_ENGINE = "Engine";
  public static final String COL_TCD_FUEL = "Fuel";
  public static final String COL_TCD_BODY = "Body";
  public static final String COL_TCD_AXLE = "Axle";

  public static final String COL_TCD_PREFIX = "Prefix";
  public static final String COL_TCD_INDEX = "Index";
  public static final String COL_TCD_REMAINDER = "Remainder";
  public static final String COL_TCD_PRICE = "Price";

  public static final String ALS_TCD_ANALOG_SUPPLIER = "AnalogSupplier"; 

  public static final String CATEGORY_SEPARATOR = ","; 

  public static final String CURRENCY = "Lt"; 
  
  private EcConstants() {
  }
}
