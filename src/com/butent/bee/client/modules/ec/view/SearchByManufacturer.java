package com.butent.bee.client.modules.ec.view;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.widget.IndexSelector;
import com.butent.bee.client.modules.ec.widget.ItemPanel;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcItem;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class SearchByManufacturer extends EcView {

  private static final String STYLE_PREFIX = EcStyles.name("searchByManufacturer-");
  private static final String STYLE_MANUFACTURER = STYLE_PREFIX + "manufacturer-";

  private final Button manufacturerWidget;
  private final IndexSelector manufacturerSelector;
  
  private final ItemPanel itemPanel;
  
  private final List<String> manufacturers = Lists.newArrayList();
  private String manufacturer;
  
  SearchByManufacturer() {
    super();
    
    this.manufacturerWidget = new Button(Localized.getConstants().ecItemManufacturer());
    manufacturerWidget.addStyleName(STYLE_MANUFACTURER + "widget");
    
    this.manufacturerSelector = new IndexSelector(STYLE_MANUFACTURER + "selector");

    this.itemPanel = new ItemPanel();
  }

  @Override
  protected void createUi() {
    add(manufacturerWidget);
    add(itemPanel);

    manufacturerWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openManufacturers();
      }
    });
  }

  @Override
  protected String getPrimaryStyle() {
    return "searchByManufacturer";
  }

  @Override
  protected void onLoad() {
    super.onLoad();

    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        openManufacturers();
      }
    });
  }

  private String getManufacturer() {
    return manufacturer;
  }

  private void onSelectManufacturer(int index) {
    UiHelper.closeDialog(manufacturerSelector);
    if (!BeeUtils.isIndex(manufacturers, index)) {
      return;
    }
    
    String selectedManufacturer = manufacturers.get(index);
    if (!selectedManufacturer.equals(getManufacturer())) {
      setManufacturer(selectedManufacturer);
      
      manufacturerWidget.setText(manufacturer);
      manufacturerWidget.addStyleName(STYLE_MANUFACTURER + "selected");
      
      itemPanel.clear();
      
      ParameterList params = EcKeeper.createArgs(SVC_GET_ITEMS_BY_MANUFACTURER);
      params.addDataItem(VAR_MANUFACTURER, selectedManufacturer);

      EcKeeper.requestItems(SVC_GET_ITEMS_BY_MANUFACTURER, selectedManufacturer, params,
          new Consumer<List<EcItem>>() {
            @Override
            public void accept(List<EcItem> items) {
              EcKeeper.renderItems(itemPanel, items);
            }
          });
    }
  }
  
  private void openManufacturers() {
    EcKeeper.getItemManufacturers(new Consumer<List<String>>() {
      @Override
      public void accept(List<String> input) {
        manufacturers.clear();
        manufacturers.addAll(input);
        
        if (!manufacturerSelector.hasSelectionHandler()) {
          manufacturerSelector.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
              onSelectManufacturer(event.getSelectedItem());
            }
          });
        }

        manufacturerSelector.render(input);
        
        Popup popup = new Popup(OutsideClick.CLOSE, STYLE_MANUFACTURER + "dialog");
        popup.setWidget(manufacturerSelector);

        popup.setHideOnEscape(true);
        popup.showRelativeTo(manufacturerWidget.getElement());
        
        manufacturerSelector.focus();
      }
    });
  }
  
  private void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }
}
