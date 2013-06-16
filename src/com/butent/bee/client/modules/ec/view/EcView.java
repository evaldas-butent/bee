package com.butent.bee.client.modules.ec.view;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcConstants.CartType;

public abstract class EcView extends Flow {

  public static EcView create(String service) {
    EcView ecView = null;

    if (EcConstants.SVC_FINANCIAL_INFORMATION.equals(service)) {
      ecView = new FinancialInformation();

    } else if (EcConstants.SVC_TERMS_OF_DELIVERY.equals(service)) {
      ecView = new TermsOfDelivery();
    } else if (EcConstants.SVC_CONTACTS.equals(service)) {
      ecView = new Contacts();

    } else if (EcConstants.SVC_SEARCH_BY_ITEM_CODE.equals(service)) {
      ecView = new SearchByItem(service, Localized.constants.ecItemCode());
    } else if (EcConstants.SVC_SEARCH_BY_OE_NUMBER.equals(service)) {
      ecView = new SearchByItem(service, Localized.constants.ecItemOeNumber());

    } else if (EcConstants.SVC_SEARCH_BY_CAR.equals(service)) {
      ecView = new SearchByCar();
    } else if (EcConstants.SVC_SEARCH_BY_MANUFACTURER.equals(service)) {
      ecView = new SearchByManufacturer();

    } else if (EcConstants.SVC_GENERAL_ITEMS.equals(service)) {
      ecView = new GeneralItems();
    } else if (EcConstants.SVC_BIKE_ITEMS.equals(service)) {
      ecView = new BikeItems();

    } else {
      for (CartType cartType : CartType.values()) {
        if (cartType.getService().equals(service)) {
          ecView = new ShoppingCart(cartType);
          break;
        }
      }
    }
    
    if (ecView != null) {
      ecView.createUi();
    }
    return ecView;
  }

  protected EcView() {
    super(EcStyles.name("View"));
    EcStyles.add(this, getPrimaryStyle());
  }

  protected abstract void createUi();

  protected abstract String getPrimaryStyle();
  
  @Override
  protected void onLoad() {
    super.onLoad();
    UiHelper.focus(this);
  }
}
