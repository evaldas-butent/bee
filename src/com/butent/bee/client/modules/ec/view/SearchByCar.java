package com.butent.bee.client.modules.ec.view;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasEnabled;

import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.modules.ec.widget.IndexSelector;
import com.butent.bee.client.modules.ec.widget.ItemPanel;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcCarModel;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class SearchByCar extends EcView {
  
  private static class CarAttributeWidget extends CustomDiv implements HasEnabled {
    
    private boolean enabled = true;
    
    private CarAttributeWidget(String styleName) {
      super(styleName);
    }

    @Override
    public boolean isEnabled() {
      return enabled;
    }

    @Override
    public void onBrowserEvent(Event event) {
      if (isEnabled() || !EventUtils.isClick(event)) {
        super.onBrowserEvent(event);
      }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      setStyleName(STYLE_ATTRIBUTE_DISABLED, !enabled);
    }
    
    private void setHasValue(boolean hasValue) {
      setStyleName(STYLE_ATTRIBUTE_EMPTY, !hasValue);
    }
  }
  
  private static final String STYLE_PREFIX = EcStyles.name("searchByCar-");

  private static final String STYLE_ATTRIBUTE = STYLE_PREFIX + "attribute-";
  private static final String STYLE_SELECTOR = STYLE_PREFIX + "selector-";
  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog-";
  private static final String STYLE_TYPE = STYLE_PREFIX + "type-";

  private static final String STYLE_MANUFACTURER = "manufacturer";
  private static final String STYLE_MODEL = "model";
  private static final String STYLE_YEAR = "year";
  private static final String STYLE_ENGINE = "engine";

  private static final String STYLE_ATTRIBUTE_DISABLED = STYLE_ATTRIBUTE + "disabled";
  private static final String STYLE_ATTRIBUTE_EMPTY = STYLE_ATTRIBUTE + "empty";
  
  private final CarAttributeWidget manufacturerWidget;
  private final IndexSelector manufacturerSelector;

  private final CarAttributeWidget modelWidget;
  private final IndexSelector modelSelector;

  private final CarAttributeWidget yearWidget;
  private final IndexSelector yearSelector;

  private final CarAttributeWidget engineWidget;
  private final IndexSelector engineSelector;
  
  private final Flow typePanel;
  private final ItemPanel itemPanel;
  
  private final List<String> manufacturers = Lists.newArrayList();
  private String manufacturer = null;
  
  private final List<EcCarModel> models = Lists.newArrayList();
  private Integer modelIndex = null;

  private final List<EcCarType> types = Lists.newArrayList();

  private Integer year = null;
  private String engine = null;
  
  SearchByCar() {
    super();

    this.manufacturerWidget = new CarAttributeWidget(STYLE_ATTRIBUTE + STYLE_MANUFACTURER);
    this.manufacturerSelector = new IndexSelector(STYLE_SELECTOR + STYLE_MANUFACTURER);

    this.modelWidget = new CarAttributeWidget(STYLE_ATTRIBUTE + STYLE_MODEL);
    this.modelSelector = new IndexSelector(STYLE_SELECTOR + STYLE_MODEL);

    this.yearWidget = new CarAttributeWidget(STYLE_ATTRIBUTE + STYLE_YEAR);
    this.yearSelector = new IndexSelector(STYLE_SELECTOR + STYLE_YEAR);

    this.engineWidget = new CarAttributeWidget(STYLE_ATTRIBUTE + STYLE_ENGINE);
    this.engineSelector = new IndexSelector(STYLE_SELECTOR + STYLE_ENGINE);
    
    this.typePanel = new Flow(STYLE_TYPE + "panel");
    this.itemPanel = new ItemPanel();
  }

  @Override
  protected void createUi() {
    Flow carPanel = new Flow(STYLE_PREFIX + "carPanel");
    
    carPanel.add(manufacturerWidget);
    carPanel.add(modelWidget);
    carPanel.add(yearWidget);
    carPanel.add(engineWidget);

    add(carPanel);
    add(typePanel);
    add(itemPanel);

    refreshAttributeWidgets();
    
    manufacturerWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openManufacturers();
      }
    });

    modelWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        openModels();
      }
    });
  }

  @Override
  protected String getPrimaryStyle() {
    return "searchByCar";
  }

  private String getEngine() {
    return engine;
  }

  private String getManufacturer() {
    return manufacturer;
  }

  private Integer getModelId() {
    return (getModelIndex() == null) ? null : models.get(getModelIndex()).getModelId();
  }

  private Integer getModelIndex() {
    return modelIndex;
  }

  private String getModelName() {
    return (getModelIndex() == null) ? null : models.get(getModelIndex()).getModelName();
  }

  private Integer getYear() {
    return year;
  }

  private void onSelectManufacturer(int index) {
    UiHelper.closeDialog(manufacturerSelector);

    if (!BeeUtils.isIndex(manufacturers, index)) {
      return;
    }
    
    String selectedManufacturer = manufacturers.get(index);
    if (!selectedManufacturer.equals(getManufacturer())) {
      setManufacturer(selectedManufacturer);
      resetModel();
    
      refreshAttributeWidgets();
    }
  }
  
  private void onSelectModel(int index) {
    UiHelper.closeDialog(modelSelector);
    if (!BeeUtils.isIndex(models, index)) {
      return;
    }

    setModelIndex(index);
    resetTypes();
    
    refreshAttributeWidgets();
    
    EcKeeper.getCarTypes(getModelId(), new Consumer<List<EcCarType>>() {
      @Override
      public void accept(List<EcCarType> input) {
        types.clear();
        types.addAll(input);

        renderTypes();
      }
    });
  }
  
  private void openManufacturers() {
    EcKeeper.getCarManufacturers(new Consumer<List<String>>() {
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
        
        Popup popup = new Popup(OutsideClick.CLOSE, STYLE_DIALOG + STYLE_MANUFACTURER);
        popup.setWidget(manufacturerSelector);
        popup.showRelativeTo(manufacturerWidget.getElement());
        
        manufacturerSelector.focus();
      }
    });
  }

  private void openModels() {
    if (BeeUtils.isEmpty(getManufacturer())) {
      return;
    }

    EcKeeper.getCarModels(getManufacturer(), new Consumer<List<EcCarModel>>() {
      @Override
      public void accept(List<EcCarModel> input) {
        models.clear();
        models.addAll(input);

        if (!modelSelector.hasSelectionHandler()) {
          modelSelector.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
              onSelectModel(event.getSelectedItem());
            }
          });
        }

        modelSelector.render(renderModels());
        
        Popup popup = new Popup(OutsideClick.CLOSE, STYLE_DIALOG + STYLE_MODEL);
        popup.setWidget(modelSelector);
        popup.showRelativeTo(modelWidget.getElement());
        
        modelSelector.focus();
      }
    });
  }
  
  private void refreshAttributeWidgets() {
    boolean hasManufacturer = !BeeUtils.isEmpty(getManufacturer());
    
    manufacturerWidget.setText(hasManufacturer 
        ? getManufacturer() : Localized.constants.ecCarManufacturer());
    manufacturerWidget.setHasValue(hasManufacturer);
    
    boolean modelEnabled = hasManufacturer;
    boolean hasModel = modelEnabled && getModelIndex() != null;
    
    modelWidget.setText(hasModel ? getModelName() : Localized.constants.ecCarModel());
    modelWidget.setHasValue(hasModel);
    modelWidget.setEnabled(modelEnabled);

    boolean yearEnabled = hasModel && !types.isEmpty();
    boolean hasYear = yearEnabled && getYear() != null;
    
    yearWidget.setText(hasYear ? getYear().toString() : Localized.constants.ecCarYear());
    yearWidget.setHasValue(hasYear);
    yearWidget.setEnabled(yearEnabled);

    boolean engineEnabled = hasModel && !types.isEmpty();
    boolean hasEngine = engineEnabled && !BeeUtils.isEmpty(getEngine());
    
    engineWidget.setText(hasEngine ? getEngine() : Localized.constants.ecCarEngine());
    engineWidget.setHasValue(hasEngine);
    engineWidget.setEnabled(engineEnabled);
  }

  private List<String> renderModels() {
    List<String> items = Lists.newArrayList();
    
    for (EcCarModel model : models) {
      items.add(BeeUtils.joinWords(model.getModelName(), model.getProducedFrom(), model.getProducedTo()));
    }

    return items;
  }
  
  private void renderTypes() {
    HtmlTable table = new HtmlTable(STYLE_TYPE + "table");

    int row = 0;
    for (EcCarType type : types) {
      int col = 0;
      
      table.setText(row, col++, type.getTypeName());

      table.setText(row, col++, EcUtils.string(type.getProducedFrom()));
      table.setText(row, col++, EcUtils.string(type.getProducedTo()));

      table.setText(row, col++, EcUtils.string(type.getCcm()));

      table.setText(row, col++, EcUtils.string(type.getKwFrom()));
      table.setText(row, col++, EcUtils.string(type.getKwTo()));

      table.setText(row, col++, EcUtils.string(type.getCylinders()));
      table.setText(row, col++, EcUtils.string(type.getMaxWeight()));

      table.setText(row, col++, type.getEngine());
      table.setText(row, col++, type.getFuel());
      table.setText(row, col++, type.getBody());
      table.setText(row, col++, type.getAxle());

      row++;
    }
    
    typePanel.clear();
    typePanel.add(table);
  }

  private void resetEngine() {
    setEngine(null);
  }
  
  private void resetModel() {
    models.clear();
    setModelIndex(null);
    
    resetTypes();
  }
  
  private void resetTypes() {
    types.clear();
    typePanel.clear();

    resetYear();
    resetEngine();
  }
  
  private void resetYear() {
    setYear(null);
  }

  private void setEngine(String engine) {
    this.engine = engine;
  }

  private void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  private void setModelIndex(Integer modelIndex) {
    this.modelIndex = modelIndex;
  }

  private void setYear(Integer year) {
    this.year = year;
  }
}
