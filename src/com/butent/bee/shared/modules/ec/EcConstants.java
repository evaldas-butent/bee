package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.i18n.Localized;
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
  
  public static final String EC_MODULE = "Ec";
  public static final String EC_METHOD = EC_MODULE + "Method";
  
  public static final String SVC_FEATURED_AND_NOVELTY = "featuredAndNovelty";

  public static final String SVC_FINANCIAL_INFORMATION = "financialInformation";
  public static final String SVC_TERMS_OF_DELIVERY = "termsOfDelivery";
  public static final String SVC_CONTACTS = "contacts";

  public static final String SVC_GLOBAL_SEARCH = "globalSearch";

  public static final String SVC_SEARCH_BY_ITEM_CODE = "searchByItemCode";
  public static final String SVC_SEARCH_BY_OE_NUMBER = "searchByOeNumber";
  public static final String SVC_SEARCH_BY_CAR = "searchByCar";
  public static final String SVC_SEARCH_BY_MANUFACTURER = "searchByManufacturer";

  public static final String SVC_GENERAL_ITEMS = "generalItems";
  public static final String SVC_BIKE_ITEMS = "bikeItems";

  public static final String SVC_GET_CAR_MANUFACTURERS = "getCarManufacturers";
  public static final String SVC_GET_CAR_MODELS = "getCarModels";
  public static final String SVC_GET_CAR_TYPES = "getCarTypes";

  public static final String VAR_PREFIX = Service.RPC_VAR_PREFIX + "ec_";

  public static final String VAR_QUERY = VAR_PREFIX + "query";
  public static final String VAR_OFFSET = VAR_PREFIX + "offset";
  public static final String VAR_LIMIT = VAR_PREFIX + "limit";

  public static final String VAR_MANUFACTURER = VAR_PREFIX + "manufacturer";
  public static final String VAR_MODEL = VAR_PREFIX + "model";

  public static final String TBL_TCD_MODELS = "TcdModels";
  public static final String TBL_TCD_TYPES = "TcdTypes";

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
  
  private EcConstants() {
  }
}
