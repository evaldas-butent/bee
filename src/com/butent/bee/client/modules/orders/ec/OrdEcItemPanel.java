package com.butent.bee.client.modules.orders.ec;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.shared.modules.orders.ec.OrdEcItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrdEcItemPanel extends Flow {

  private static final String STYLE_PRIMARY = "ItemPanel";
  private static final String STYLE_ITEMS = STYLE_PRIMARY + "-items";

  private static final String STYLE_WRAPPER = "wrapper";

  private final Set<Long> selectedCategories = new HashSet<>();

  private final List<OrdEcItem> items = new ArrayList<>();

  private boolean byCategory;
  private String service;
  private String query;

  public OrdEcItemPanel(boolean byCategory, String service) {
    super(EcStyles.name(STYLE_PRIMARY));
    this.byCategory = byCategory;
    this.service = service;
  }

  @Override
  public void clear() {
    super.clear();

    if (!items.isEmpty()) {
      items.clear();
      selectedCategories.clear();
    }
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public void render(List<OrdEcItem> ecItems) {
    if (!isEmpty()) {
      clear();
    }

    for (OrdEcItem ecItem : ecItems) {
      items.add(ecItem);
    }

    Flow itemsFlow = new Flow(EcStyles.name(STYLE_ITEMS, STYLE_WRAPPER));

    OrdEcItemList itemWrapper = new OrdEcItemList(items, byCategory, service, query);
    EcStyles.add(itemWrapper, STYLE_ITEMS, STYLE_WRAPPER);
    itemsFlow.add(itemWrapper);

    add(itemsFlow);
  }
}