package com.butent.bee.client.composite;

import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.HasSummaryChangeHandlers;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.event.logical.SummaryChangeEvent;
import com.butent.bee.client.event.logical.SummaryChangeEvent.Handler;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.HasFosterParent;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Map;

public class Gallery extends Flow implements HasViewName, HasFosterParent, ParentRowEvent.Handler,
    HasSummaryChangeHandlers {

  private static final BeeLogger logger = LogUtils.getLogger(Gallery.class);

  private static final String ATTR_PICTURE_COLUMN = "pictureColumn";
  private static final String ATTR_ORDER_COLUMN = "orderColumn";
  private static final String ATTR_PARENT_REL_COLUMN = "parentRelColumn";

  private static final String STYLE_NAME = BeeConst.CSS_CLASS_PREFIX + "Gallery";
  private static final String STYLE_PREFIX = STYLE_NAME + "-";

  private static final String STYLE_COMMAND_PANEL = STYLE_PREFIX + "command-panel";
  private static final String STYLE_PICTURE_PANEL = STYLE_PREFIX + "picture-panel";

  public static Gallery create(Map<String, String> attributes) {
    Assert.notNull(attributes);

    String viewName = getAttribute(attributes, UiConstants.ATTR_VIEW_NAME, true);
    String relColumn = getAttribute(attributes, UiConstants.ATTR_REL_COLUMN, true);
    String pictureColumn = getAttribute(attributes, ATTR_PICTURE_COLUMN, true);

    if (BeeUtils.allNotEmpty(viewName, relColumn, pictureColumn)) {
      String orderColumn = getAttribute(attributes, ATTR_ORDER_COLUMN, false);
      String parentRelColumn = getAttribute(attributes, ATTR_PARENT_REL_COLUMN, false);

      return new Gallery(viewName, relColumn, pictureColumn, orderColumn, parentRelColumn);

    } else {
      return null;
    }
  }

  private static String getAttribute(Map<String, String> attributes, String name,
      boolean required) {

    String value = attributes.get(name);
    if (required && BeeUtils.isEmpty(value)) {
      logger.severe(NameUtils.getClassName(Gallery.class), "attribute", name, "not found");
    }

    return value;
  }

  private final String viewName;

  private final String relColumn;
  private final String pictureColumn;
  private final String orderColumn;

  private final String parentRelColumn;
  private int parentIndex = BeeConst.UNDEF;

  private String parentId;
  private HandlerRegistration parentRowReg;

  private boolean summarize = true;

  private final Flow commandPanel;
  private final Flow picturePanel;

  public Gallery(String viewName, String relColumn, String pictureColumn) {
    this(viewName, relColumn, pictureColumn, null, null);
  }

  public Gallery(String viewName, String relColumn, String pictureColumn, String orderColumn) {
    this(viewName, relColumn, pictureColumn, orderColumn, null);
  }

  public Gallery(String viewName, String relColumn, String pictureColumn, String orderColumn,
      String parentRelColumn) {

    super(STYLE_NAME);

    this.viewName = viewName;

    this.relColumn = relColumn;
    this.pictureColumn = pictureColumn;
    this.orderColumn = orderColumn;

    this.parentRelColumn = parentRelColumn;

    this.commandPanel = new Flow(STYLE_COMMAND_PANEL);
    add(commandPanel);

    this.picturePanel = new Flow(STYLE_PICTURE_PANEL);
    add(picturePanel);
  }

  @Override
  public com.google.gwt.event.shared.HandlerRegistration addSummaryChangeHandler(Handler handler) {
    return addHandler(handler, SummaryChangeEvent.getType());
  }

  @Override
  public String getIdPrefix() {
    return "gallery";
  }

  @Override
  public String getParentId() {
    return parentId;
  }

  @Override
  public Value getSummary() {
    return new IntegerValue(picturePanel.getWidgetCount());
  }

  @Override
  public String getViewName() {
    return viewName;
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    clearPictures();

    ensureParentIndex(event.getViewName());
    Long parentValue = getParentValue(event.getRow());

    if (DataUtils.isId(parentValue)) {
      Filter filter = Filter.equals(relColumn, parentValue);
      Order order = BeeUtils.isEmpty(orderColumn) ? null : Order.ascending(orderColumn);

      Queries.getRowSet(viewName, null, filter, order, new Queries.RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          if (!result.isEmpty()) {
            render(result);
            SummaryChangeEvent.maybeFire(Gallery.this);
          }
        }
      });
    }
  }

  @Override
  public void setParentId(String parentId) {
    this.parentId = parentId;
    if (isAttached()) {
      register();
    }
  }

  @Override
  public void setSummarize(boolean summarize) {
    this.summarize = summarize;
  }

  @Override
  public boolean summarize() {
    return summarize;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    register();
  }

  @Override
  protected void onUnload() {
    unregister();
    super.onUnload();
  }

  private void clearPictures() {
    if (!picturePanel.isEmpty()) {
      picturePanel.clear();
      SummaryChangeEvent.maybeFire(this);
    }
  }

  private void ensureParentIndex(String parentViewName) {
    if (BeeConst.isUndef(getParentIndex())) {
      if (BeeUtils.isEmpty(parentRelColumn)) {
        setParentIndex(DataUtils.ID_INDEX);
      } else {
        setParentIndex(Data.getColumnIndex(parentViewName, parentRelColumn));
      }
    }
  }

  private int getParentIndex() {
    return parentIndex;
  }

  private HandlerRegistration getParentRowReg() {
    return parentRowReg;
  }

  private Long getParentValue(IsRow row) {
    if (row == null) {
      return null;
    } else if (getParentIndex() >= 0) {
      return row.getLong(getParentIndex());
    } else {
      return row.getId();
    }
  }

  private void register() {
    unregister();
    if (!BeeUtils.isEmpty(getParentId())) {
      setParentRowReg(BeeKeeper.getBus().registerParentRowHandler(getParentId(), this, false));
    }
  }

  private void render(BeeRowSet data) {
    int orderIndex =
        BeeUtils.isEmpty(orderColumn) ? BeeConst.UNDEF : data.getColumnIndex(orderColumn);
    int pictureIndex = data.getColumnIndex(pictureColumn);

    for (IsRow row : data) {
      Image image = new Image();
      image.setUrl(row.getString(pictureIndex));

      if (!BeeConst.isUndef(orderIndex)) {
        DomUtils.setDataProperty(image.getElement(), orderColumn, row.getInteger(orderIndex));
      }

      picturePanel.add(image);
    }
  }

  private void setParentIndex(int parentIndex) {
    this.parentIndex = parentIndex;
  }

  private void setParentRowReg(HandlerRegistration parentRowReg) {
    this.parentRowReg = parentRowReg;
  }

  private void unregister() {
    if (getParentRowReg() != null) {
      getParentRowReg().removeHandler();
      setParentRowReg(null);
    }
  }
}
