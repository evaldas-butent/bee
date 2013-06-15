package com.butent.bee.shared.modules.ec;

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

  public static final String SVC_SEARCH_GEBERAL = "searchGeneral";

  public static final String SVC_SEARCH_BY_ITEM_CODE = "searchByItemCode";
  public static final String SVC_SEARCH_BY_OE_NUMBER = "searchByOeNumber";
  public static final String SVC_SEARCH_BY_CAR = "searchByCar";
  public static final String SVC_SEARCH_BY_MANUFACTURER = "searchByManufacturer";

  public static final String SVC_GENERAL_ITEMS = "generalItems";
  public static final String SVC_BIKE_ITEMS = "bikeItems";

  private EcConstants() {
  }
}
