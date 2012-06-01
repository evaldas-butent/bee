package com.butent.bee.client.screen;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DialogCallback;
import com.butent.bee.client.layout.Stack;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.HandlesDeleteEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

public class Favorites extends Stack implements HandlesDeleteEvents {

  public enum Group implements HasCaption, HasViewName {
    APPOINTMENTS("Vizitai", "Appointments"),
    CALENDARS("Kalendoriai", "Calendars"),
    CUSTOMERS("Klientai", "Companies");

    public static Group getByViewName(String viewName) {
      if (!BeeUtils.isEmpty(viewName)) {
        for (Group group : Group.values()) {
          if (BeeUtils.same(group.getViewName(), viewName)) {
            return group;
          }
        }
      }
      return null;
    }

    private final String caption;
    private final String viewName;

    private final List<Item> items = Lists.newArrayList();

    private int widgetIndex = BeeConst.UNDEF;

    private Group(String caption, String viewName) {
      this.caption = caption;
      this.viewName = viewName;
    }

    public String getCaption() {
      return caption;
    }

    public String getViewName() {
      return viewName;
    }

    private void add(Item item) {
      if (item != null) {
        items.add(item);
      }
    }

    private void clear() {
      if (!items.isEmpty()) {
        items.clear();
      }
    }

    private boolean contains(long id) {
      for (Item item : items) {
        if (item.getId() == id) {
          return true;
        }
      }
      return false;
    }

    private Widget createItemWidget(Item item) {
      return createItemWidget(item.getId(), item.getHtml());
    }

    private Widget createItemWidget(final long id, final String html) {
      InternalLink itemWidget = new InternalLink(html.trim());
      itemWidget.addStyleName(ITEM_STYLE);

      itemWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          BeeKeeper.getBus().fireEvent(new RowActionEvent(getViewName(), id,
              Service.OPEN_FAVORITE, html));
        }
      });

      return itemWidget;
    }

    private Item find(long id) {
      for (Item item : items) {
        if (item.getId() == id) {
          return item;
        }
      }
      return null;
    }

    private int getWidgetIndex() {
      return widgetIndex;
    }

    private int indexOf(Item item) {
      return items.indexOf(item);
    }

    private boolean isEmpty() {
      return items.isEmpty();
    }

    private int maxOrder() {
      int result = 0;
      for (Item item : items) {
        result = Math.max(result, item.getOrder());
      }
      return result;
    }

    private boolean remove(Item item) {
      if (item == null) {
        return false;
      } else {
        return items.remove(item);
      }
    }

    private void setWidgetIndex(int widgetIndex) {
      this.widgetIndex = widgetIndex;
    }
  }

  public static class Item {
    private final long id;

    private String html;
    private int order;

    private Item(long id, String html) {
      this(id, html, 0);
    }

    private Item(long id, String html, int order) {
      this.id = id;

      this.html = html;
      this.order = order;
    }

    private String getHtml() {
      return html;
    }

    private long getId() {
      return id;
    }

    private int getOrder() {
      return order;
    }

    private void setHtml(String html) {
      this.html = html;
    }
  }

  private static final String CONTAINER_STYLE = "bee-FavoritesContainer";
  private static final String HEADER_STYLE = "bee-FavoritesHeader";
  private static final String GROUP_STYLE = "bee-FavoritesGroup";
  private static final String ITEM_STYLE = "bee-FavoritesItem";

  private static final String VIEW_NAME = "Favorites";

  private static final String COL_USER = "User";
  private static final String COL_GROUP = "Group";
  private static final String COL_ITEM = "Item";
  private static final String COL_ORDER = "Order";
  private static final String COL_HTML = "Html";

  private static final double HEADER_SIZE = 24;
  private static final Unit HEADER_UNIT = Unit.PX;

  private static final List<BeeColumn> columns = Lists.newArrayList();

  private int groupIndex = BeeConst.UNDEF;
  private int itemIndex = BeeConst.UNDEF;
  private int orderIndex = BeeConst.UNDEF;
  private int htmlIndex = BeeConst.UNDEF;

  public Favorites() {
    super(HEADER_UNIT);
    addStyleName(CONTAINER_STYLE);

    int index = 0;
    for (Group group : Group.values()) {
      Widget display = new Vertical();
      display.addStyleName(GROUP_STYLE);

      Widget header = new Html(group.getCaption());
      header.addStyleName(HEADER_STYLE);

      add(display, header, HEADER_SIZE);
      group.setWidgetIndex(index++);
    }
  }

  public void addItem(Group group, long id, String html) {
    int order = group.maxOrder() + 1;
    Item item = new Item(id, html, order);
    group.add(item);
    getDisplay(group).add(group.createItemWidget(item));

    Queries.insert(VIEW_NAME,
        DataUtils.getColumns(columns, groupIndex, itemIndex, orderIndex, htmlIndex),
        Lists.newArrayList(BeeUtils.toString(group.ordinal()), BeeUtils.toString(id),
            BeeUtils.toString(order), BeeUtils.trim(html)), null);
  }

  public void bookmark(String viewName, final IsRow row, List<BeeColumn> sourceColumns,
      List<String> expressions) {
    if (BeeUtils.anyEmpty(viewName, row, sourceColumns, expressions)) {
      return;
    }

    final Group group = Group.getByViewName(viewName);
    if (group == null) {
      Global.inform("Bookmarks does not support view", viewName);
      return;
    }

    Item item = group.find(row.getId());
    if (item != null) {
      Global.inform("Row is already bookmarked as", item.getHtml());
      return;
    }

    List<String> values = DataUtils.translate(expressions, sourceColumns, row);
    String html = BeeUtils.transformCollection(values, BeeConst.STRING_SPACE);

    Global.inputString("Bookmark", null, new DialogCallback<String>() {
      @Override
      public void onSuccess(String value) {
        addItem(group, row.getId(), value);
        showWidget(group.getWidgetIndex());

        BeeKeeper.getBus().fireEvent(new BookmarkEvent(group, row.getId()));
      }
    }, html);
  }

  public boolean containsItem(Group group, long id) {
    return group.contains(id);
  }

  public void load() {
    if (!BeeKeeper.getUser().isLoggedIn()) {
      BeeKeeper.getLog().warning(NameUtils.getName(this), "user not active");
      return;
    }

    Queries.getRowSet(VIEW_NAME, null, BeeKeeper.getUser().getFilter(COL_USER),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            BeeUtils.overwrite(columns, result.getColumns());

            groupIndex = DataUtils.getColumnIndex(COL_GROUP, columns);
            itemIndex = DataUtils.getColumnIndex(COL_ITEM, columns);
            orderIndex = DataUtils.getColumnIndex(COL_ORDER, columns);
            htmlIndex = DataUtils.getColumnIndex(COL_HTML, columns);

            loadItems(result);
          }
        });
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    Group group = Group.getByViewName(event.getViewName());
    if (group != null) {
      for (RowInfo rowInfo : event.getRows()) {
        removeItem(group, rowInfo.getId());
      }
    }
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    Group group = Group.getByViewName(event.getViewName());
    if (group != null) {
      removeItem(group, event.getRowId());
    }
  }

  public boolean removeItem(Group group, long id) {
    Item item = group.find(id);
    if (item == null) {
      return false;
    }

    HasIndexedWidgets display = getDisplay(group);
    display.remove(display.getWidget(group.indexOf(item)));

    Filter filter = Filter.and(
        ComparisonFilter.isEqual(COL_GROUP, new IntegerValue(group.ordinal())),
        ComparisonFilter.isEqual(COL_ITEM, new LongValue(id)));
    Queries.delete(VIEW_NAME, filter, null);

    return group.remove(item);
  }

  public boolean updateItem(Group group, long id, String html) {
    Item item = group.find(id);
    if (item == null || BeeUtils.equalsTrim(item.getHtml(), html)) {
      return false;
    }
    item.setHtml(html);

    refreshDisplay(group);

    // Queries.updateCell(VIEW_NAME, id, BeeConst.UNDEF, columns.get(htmlIndex), oldValue, html,
    // null);
    return true;
  }

  private void clearData() {
    for (Group group : Group.values()) {
      if (!group.isEmpty()) {
        group.clear();
        getDisplay(group).clear();
      }
    }
  }

  private Item createItem(BeeRow row) {
    Long itm = row.getLong(itemIndex);
    if (itm == null) {
      return null;
    }

    Integer ord = row.getInteger(orderIndex);
    String html = row.getString(htmlIndex);

    return new Item(itm, html.trim(), BeeUtils.unbox(ord));
  }

  private HasIndexedWidgets getDisplay(Group group) {
    return (HasIndexedWidgets) getWidget(group.getWidgetIndex());
  }

  private Group getGroup(int index) {
    if (index >= 0 && index < Group.values().length) {
      return Group.values()[index];
    } else {
      return null;
    }
  }

  private void loadItems(BeeRowSet rowSet) {
    if (rowSet.isEmpty()) {
      return;
    }

    clearData();

    for (BeeRow row : rowSet.getRows()) {
      Integer grp = row.getInteger(groupIndex);
      Group group = (grp == null) ? null : getGroup(grp);
      if (group == null) {
        BeeKeeper.getLog().severe("favorite group id not found:", grp);
        continue;
      }

      Item item = createItem(row);
      if (item != null) {
        group.add(item);
        getDisplay(group).add(group.createItemWidget(item));
      }
    }
  }

  private void refreshDisplay(Group group) {
    HasIndexedWidgets display = getDisplay(group);
    display.clear();

    for (Item item : group.items) {
      display.add(group.createItemWidget(item.getId(), item.getHtml()));
    }
  }
}
