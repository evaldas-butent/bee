package com.butent.bee.client.modules.trade;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.modules.trade.TradeItemSearch;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class TradeItemPicker extends Flow {

  private static final String STYLE_NAME = TradeKeeper.STYLE_PREFIX + "item-picker";
  private static final String STYLE_PREFIX = STYLE_NAME + "-";

  private static final String STYLE_SEARCH_PREFIX = STYLE_PREFIX + "search-";
  private static final String STYLE_SEARCH_PANEL = STYLE_SEARCH_PREFIX + "panel";
  private static final String STYLE_SEARCH_BOX = STYLE_SEARCH_PREFIX + "box";
  private static final String STYLE_SEARCH_COMMAND = STYLE_SEARCH_PREFIX + "command";
  private static final String STYLE_SEARCH_SPINNER = STYLE_SEARCH_PREFIX + "spinner";
  private static final String STYLE_SEARCH_RUNNING = STYLE_SEARCH_PREFIX + "running";
  private static final String STYLE_SEARCH_BY_CONTAINER = STYLE_SEARCH_PREFIX + "by-container";
  private static final String STYLE_SEARCH_BY_SELECTOR = STYLE_SEARCH_PREFIX + "by-selector";

  private static final int SEARCH_BY_SIZE = 3;

  TradeItemPicker() {
    super(STYLE_NAME);

    add(createSearch());
  }

  private static Widget createSearchBySelector() {
    ListBox selector = new ListBox();
    selector.addStyleName(STYLE_SEARCH_BY_SELECTOR);

    selector.addItem(BeeConst.STRING_EMPTY, BeeConst.STRING_ASTERISK);
    for (TradeItemSearch tis : TradeItemSearch.values()) {
      selector.addItem(tis.getCaption(), tis.name());
    }

    return selector;
  }

  private Widget createSearch() {
    Flow panel = new Flow(STYLE_SEARCH_PANEL);

    final List<Widget> searchBySelectors = new ArrayList<>();
    for (int i = 0; i < SEARCH_BY_SIZE; i++) {
      searchBySelectors.add(createSearchBySelector());
    }

    final InputText searchBox = new InputText();
    DomUtils.setSearch(searchBox);
    searchBox.setMaxLength(20);
    searchBox.addStyleName(STYLE_SEARCH_BOX);

    searchBox.addKeyDownHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        String query = BeeUtils.trim(searchBox.getValue());
        if (!BeeUtils.isEmpty(query)) {
          doSearch(parseSearchBySelectors(searchBySelectors), query);
        }
      }
    });
    panel.add(searchBox);

    FaLabel searchCommand = new FaLabel(FontAwesome.SEARCH, STYLE_SEARCH_COMMAND);
    searchCommand.addClickHandler(event ->
        doSearch(parseSearchBySelectors(searchBySelectors), BeeUtils.trim(searchBox.getValue())));
    panel.add(searchCommand);

    FaLabel spinner = new FaLabel(FontAwesome.SPINNER, STYLE_SEARCH_SPINNER);
    panel.add(spinner);

    Flow searchByContainer = new Flow(STYLE_SEARCH_BY_CONTAINER);
    searchBySelectors.forEach(searchBySelectors::add);

    panel.add(searchByContainer);

    return panel;
  }

  private static TradeItemSearch parseSearchBySelector(Widget selector) {
    if (selector instanceof ListBox) {
      String value = ((ListBox) selector).getValue();

      if (!BeeUtils.isEmpty(value) && !BeeUtils.equalsTrim(value, BeeConst.STRING_ASTERISK)) {
        return EnumUtils.getEnumByName(TradeItemSearch.class, value);
      }
    }
    return null;
  }

  private static Collection<TradeItemSearch> parseSearchBySelectors(Collection<Widget> selectors) {
    List<TradeItemSearch> result = new ArrayList<>();

    for (Widget selector : selectors) {
      TradeItemSearch tis = parseSearchBySelector(selector);

      if (tis != null && !result.contains(tis)) {
        result.add(tis);
      }
    }

    return result;
  }

  private void doSearch(Collection<TradeItemSearch> by, String query) {
  }
}
