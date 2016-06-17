package com.butent.bee.client.modules.orders.ec;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.butent.bee.client.modules.ec.widget.ItemSelector;
import com.butent.bee.client.ui.AutocompleteProvider;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.orders.ec.OrdEcItem;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class SearchByArticle extends OrdEcView implements SelectionHandler<InputText> {

  private final String service;
  private final String selectorCaption;

  private final OrdEcItemPanel itemPanel;

  SearchByArticle(String service, String selectorCaption) {
    super();
    this.service = service;
    this.selectorCaption = selectorCaption;

    this.itemPanel = new OrdEcItemPanel();
  }

  @Override
  public void onSelection(SelectionEvent<InputText> event) {
    itemPanel.clear();

    final InputText editor = event.getSelectedItem();

    OrdEcKeeper.searchItems(false, service, BeeUtils.trim(editor.getValue()),
        new Consumer<List<OrdEcItem>>() {
          @Override
          public void accept(List<OrdEcItem> input) {
            AutocompleteProvider.retainValue(editor);
            OrdEcKeeper.renderItems(itemPanel, input);
          }
        });
  }

  @Override
  protected void createUi() {
    ItemSelector selector = new ItemSelector(selectorCaption, EcConstants.NAME_PREFIX + service);
    selector.addSelectionHandler(this);

    add(selector);
    add(itemPanel);
  }

  @Override
  protected String getPrimaryStyle() {
    return "searchByItem";
  }
}