package com.butent.bee.client.modules.trade;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.Storage;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.trade.OperationType;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.modules.trade.TradeItemSearch;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class TradeItemPicker extends Flow {

  private static BeeLogger logger = LogUtils.getLogger(TradeItemPicker.class);

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
  private static final String SEARCH_BY_STORAGE_PREFIX =
      NameUtils.getClassName(TradeItemPicker.class) + "-by";

  private TradeDocumentPhase documentPhase;
  private OperationType operationType;
  private Long warehouse;

  TradeItemPicker(IsRow documentRow) {
    super(STYLE_NAME);

    add(createSearch());

    setDocumentRow(documentRow);
  }

  void setDocumentRow(IsRow row) {
    setDocumentPhase(TradeUtils.getDocumentPhase(row));
    setOperationType(TradeUtils.getDocumentOperationType(row));
    setWarehouse(TradeUtils.getDocumentRelation(row, COL_TRADE_WAREHOUSE_FROM));
  }

  private static String searchByStorageKey(int index) {
    return Storage.getUserKey(SEARCH_BY_STORAGE_PREFIX, BeeUtils.toString(index));
  }

  private static Widget createSearchBySelector(int index) {
    ListBox selector = new ListBox();
    selector.addStyleName(STYLE_SEARCH_BY_SELECTOR);

    selector.addItem(BeeConst.STRING_EMPTY, BeeConst.STRING_ASTERISK);
    for (TradeItemSearch tis : TradeItemSearch.values()) {
      selector.addItem(tis.getCaption(), tis.name());
    }

    String value = BeeKeeper.getStorage().get(searchByStorageKey(index));
    if (!BeeUtils.isEmpty(value)) {
      int selected = selector.getIndex(value);
      if (!BeeConst.isUndef(selected)) {
        selector.setSelectedIndex(selected);
      }
    }

    return selector;
  }

  private Widget createSearch() {
    Flow panel = new Flow(STYLE_SEARCH_PANEL);

    final List<Widget> searchBySelectors = new ArrayList<>();
    for (int i = 0; i < SEARCH_BY_SIZE; i++) {
      searchBySelectors.add(createSearchBySelector(i));
    }

    final InputText searchBox = new InputText();
    DomUtils.setSearch(searchBox);
    searchBox.setMaxLength(20);
    searchBox.addStyleName(STYLE_SEARCH_BOX);

    searchBox.addKeyDownHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        String input = searchBox.getValue();
        if (!BeeUtils.isEmpty(input)) {
          doSearch(input, searchBySelectors);
        }
      }
    });
    panel.add(searchBox);

    FaLabel searchCommand = new FaLabel(FontAwesome.SEARCH, STYLE_SEARCH_COMMAND);
    searchCommand.addClickHandler(event -> doSearch(searchBox.getValue(), searchBySelectors));
    panel.add(searchCommand);

    FaLabel spinner = new FaLabel(FontAwesome.SPINNER, STYLE_SEARCH_SPINNER);
    panel.add(spinner);

    Flow searchByContainer = new Flow(STYLE_SEARCH_BY_CONTAINER);
    searchBySelectors.forEach(searchByContainer::add);

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

  private void doSearch(String input, List<Widget> selectors) {
    if (getDocumentPhase() == null) {
      BeeKeeper.getScreen().notifyWarning(
          Localized.dictionary().fieldRequired(Localized.dictionary().trdDocumentPhase()));
      return;
    }

    if (getOperationType() == null) {
      BeeKeeper.getScreen().notifyWarning(
          Localized.dictionary().fieldRequired(Localized.dictionary().trdOperationType()));
      return;
    }

    List<TradeItemSearch> by = new ArrayList<>();

    for (int i = 0; i < selectors.size(); i++) {
      TradeItemSearch tis = parseSearchBySelector(selectors.get(i));

      if (tis == null) {
        BeeKeeper.getStorage().remove(searchByStorageKey(i));
      } else {
        BeeKeeper.getStorage().set(searchByStorageKey(i), tis.name());
      }

      if (tis != null && !by.contains(tis)) {
        by.add(tis);
      }
    }

    String query;
    List<TradeItemSearch> searchBy = new ArrayList<>();

    if (BeeUtils.isEmpty(input) || Operator.CHAR_ANY.equals(BeeUtils.trim(input))) {
      query = null;

      if (!BeeUtils.isEmpty(by)) {
        searchBy.addAll(by);
      }

    } else {
      query = BeeUtils.trim(input);

      if (!BeeUtils.isEmpty(by)) {
        List<String> errorMessages = new ArrayList<>();

        for (TradeItemSearch tis : by) {
          String message = tis.validate(query, Localized.dictionary());

          if (BeeUtils.isEmpty(message)) {
            searchBy.add(tis);
          } else {
            errorMessages.add(message);
          }
        }

        if (searchBy.isEmpty()) {
          BeeKeeper.getScreen().notifySevere(ArrayUtils.toArray(errorMessages));
          return;
        }
      }
    }

    if (searchBy.isEmpty() && !BeeUtils.isEmpty(query)) {
      searchBy.addAll(getDefaultSearchBy(query));
    }

    Filter filter = Filter.and(buildParentFilter(), buildSearchFilter(query, searchBy));

    addStyleName(STYLE_SEARCH_RUNNING);

    Queries.getRowSet(VIEW_ITEM_SELECTION,
        Arrays.asList(COL_ITEM_NAME, COL_ITEM_ARTICLE, COL_ITEM_IS_SERVICE, ALS_ITEM_STOCK),
        filter, new Queries.RowSetCallback() {
      @Override
      public void onFailure(String... reason) {
        removeStyleName(STYLE_SEARCH_RUNNING);
      }

      @Override
      public void onSuccess(BeeRowSet result) {
        if (DataUtils.isEmpty(result)) {
          logger.warning(filter, "not found");

        } else {
          logger.debug(filter, result.getNumberOfRows());
          Global.showModalGrid(query, result);
        }

        removeStyleName(STYLE_SEARCH_RUNNING);
      }
    });
  }

  private static Filter buildSearchFilter(String query, List<TradeItemSearch> searchBy) {
    if (BeeUtils.isEmpty(query) || BeeUtils.isEmpty(searchBy)) {
      return null;

    } else if (searchBy.size() == 1) {
      return searchBy.get(0).getItemFilter(query);

    } else {
      CompoundFilter filter = Filter.or();

      for (TradeItemSearch by : searchBy) {
        filter.add(by.getItemFilter(query));
      }
      return filter;
    }
  }

  private Filter buildParentFilter() {
    if (getDocumentPhase().modifyStock() && getOperationType().consumesStock()) {
      return Filter.or(Filter.notNull(COL_ITEM_IS_SERVICE), Filter.isPositive(ALS_ITEM_STOCK));
    } else {
      return null;
    }
  }

  private static List<TradeItemSearch> getDefaultSearchBy(String query) {
    List<TradeItemSearch> searchBy = new ArrayList<>();

    if (DataUtils.isId(query)) {
      searchBy.add(TradeItemSearch.ID);
      searchBy.add(TradeItemSearch.NAME);
      searchBy.add(TradeItemSearch.ARTICLE);
      searchBy.add(TradeItemSearch.BARCODE);

    } else if (!BeeUtils.isEmpty(query)) {
      searchBy.add(TradeItemSearch.NAME);
      searchBy.add(TradeItemSearch.ARTICLE);
    }

    return searchBy;
  }

  private TradeDocumentPhase getDocumentPhase() {
    return documentPhase;
  }

  private void setDocumentPhase(TradeDocumentPhase documentPhase) {
    this.documentPhase = documentPhase;
  }

  private OperationType getOperationType() {
    return operationType;
  }

  private void setOperationType(OperationType operationType) {
    this.operationType = operationType;
  }

  private Long getWarehouse() {
    return warehouse;
  }

  private void setWarehouse(Long warehouse) {
    this.warehouse = warehouse;
  }
}
