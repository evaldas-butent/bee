package com.butent.bee.shared.modules.ec;

import com.google.common.collect.Lists;

import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;

import java.util.List;

public class EcConstants {
  
  public enum CartType implements HasCaption {
    MAIN(Localized.constants.ecShoppingCartMain(), 0, "shoppingCartMain"),
    ALTERNATIVE(Localized.constants.ecShoppingCartAlternative(), 1, "shoppingCartAlternative");
    
    private final String caption;
    private final int index;
    private final String service;

    private CartType(String caption, int index, String service) {
      this.caption = caption;
      this.index = index;
      this.service = service;
    }
    
    @Override
    public String getCaption() {
      return caption;
    }

    public int getIndex() {
      return index;
    }

    public String getService() {
      return service;
    }
  }
  
  public static final List<CartType> cartTypesOrderedByIndex = 
      Lists.newArrayList(CartType.MAIN, CartType.ALTERNATIVE);

  public static final String SVC_FINANCIAL_INFORMATION = "financialInformation";
  public static final String SVC_TERMS_OF_DELIVERY = "termsOfDelivery";
  public static final String SVC_CONTACTS = "contacts";

  public static final String SVC_SEARCH_BY_ITEM_CODE = "searchByItemCode";
  public static final String SVC_SEARCH_BY_OE_NUMBER = "searchByOeNumber";
  public static final String SVC_SEARCH_BY_CAR = "searchByCar";
  public static final String SVC_SEARCH_BY_MANUFACTURER = "searchByManufacturer";

  public static final String SVC_GENERAL_ITEMS = "generalItems";
  public static final String SVC_BIKE_ITEMS = "bikeItems";

  private EcConstants() {
  }
}
