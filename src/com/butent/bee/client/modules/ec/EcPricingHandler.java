package com.butent.bee.client.modules.ec;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

class EcPricingHandler extends AbstractGridInterceptor {
  
  EcPricingHandler() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new EcPricingHandler();
  }

  @Override
  public void onShow(final GridPresenter presenter) {
    EcKeeper.getConfiguration(new Consumer<Map<String, String>>() {
      @Override
      public void accept(Map<String, String> input) {
        String value = input.get(EcConstants.COL_CONFIG_MARGIN_DEFAULT_PERCENT);
        
        String stylePrefix = EcStyles.name("Margins-defPercent-");
        
        presenter.getHeader().clearCommandPanel();
        
        Label label = new Label(Localized.getConstants().ecMarginDefaultPercent());
        label.addStyleName(stylePrefix + "label");
        presenter.getHeader().addCommandItem(label);
        
        InputNumber dmpInput = new InputNumber();
        dmpInput.addStyleName(stylePrefix + "input");

        if (!BeeUtils.isEmpty(value)) {
          dmpInput.setValue(value);
        }
        
        dmpInput.addValueChangeHandler(new ValueChangeHandler<String>() {
          @Override
          public void onValueChange(ValueChangeEvent<String> event) {
            EcKeeper.saveConfiguration(EcConstants.COL_CONFIG_MARGIN_DEFAULT_PERCENT,
                event.getValue());
          }
        });
        
        presenter.getHeader().addCommandItem(dmpInput);
      }
    });
  }
}
