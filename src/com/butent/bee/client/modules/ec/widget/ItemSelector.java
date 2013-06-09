package com.butent.bee.client.modules.ec.widget;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcItem;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ItemSelector extends Flow implements HasSelectionHandlers<List<EcItem>> {
  
  private static final String STYLE_PRIMARY = "ItemSelector";
  
  private final DataSelector selector;

  public ItemSelector() {
    super(EcStyles.name(STYLE_PRIMARY));
    
    Label label = new Label(Localized.constants.ecItemCode());
    EcStyles.add(label, STYLE_PRIMARY, "label");
    add(label);
    
    this.selector = createSelector();
    EcStyles.add(selector, STYLE_PRIMARY, "input");
    add(selector);
    
    Button button = new Button(Localized.constants.ecDoSearch());
    EcStyles.add(button, STYLE_PRIMARY, "submit");
    add(button);
    
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        List<EcItem> items = Lists.newArrayList();
        int count = BeeUtils.randomInt(1, 10);
        for (int i = 0; i < count; i++) {
          items.add(new EcItem());
        }

        SelectionEvent.fire(ItemSelector.this, items);
      }
    });
  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<List<EcItem>> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  public DataSelector getSelector() {
    return selector;
  }

  private DataSelector createSelector() {
    Relation relation = Relation.create(CommonsConstants.VIEW_ITEMS,
        Lists.newArrayList(CommonsConstants.COL_NAME, CommonsConstants.COL_ARTICLE));
    relation.disableEdit();
    relation.disableNewRow();

    DataSelector dataSelector = new DataSelector(relation, true);
    dataSelector.setEditing(true);

    return dataSelector;
  }
}
