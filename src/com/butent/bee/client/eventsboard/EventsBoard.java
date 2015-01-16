package com.butent.bee.client.eventsboard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.PhotoRenderer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.values.Position;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.HandlesUpdateEvents;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class EventsBoard extends Flow implements Presenter, RowInsertEvent.Handler,
    HandlesUpdateEvents, DataChangeEvent.Handler {

  private static final Set<Action> HIDDEN_ACTIONS = Sets.newHashSet(Action.DELETE);

  private static final String STYLE_HEADER = "header";

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "EventsBoard-";
  private static final String STYLE_CONTENT = "content";
  private static final String STYLE_CONTENT_ROW = STYLE_CONTENT + "-row";
  private static final String STYLE_CONTENT_COL = STYLE_CONTENT + "-col";
  private static final String STYLE_CONTENT_PHOTO = STYLE_CONTENT + "-Photo";

  private static final String CONTENT_COL_PHOTO = "PublisherPhoto";
  private static final String CONTENT_COL_PUBLISHER_INFO = "PublisherInfo";
  private static final String CONTENT_COL_EVENT_NOTE = "EventNote";
  private static final String CELL_EVENT_TYPE = "EventType";
  private static final String CELL_EVENT_PUBLISH = "EventPublished";
  private static final String CELL_EVENT_PUBLISHER = "EventPublisher";
  private static final String CELL_EVENT_NOTE = "EventNote";

  private final BeeLogger logger = LogUtils.getLogger(EventsBoard.class);
  private final Collection<HandlerRegistration> registry = Lists.newArrayList();

  private HeaderView headerView;
  private long relatedId = BeeConst.LONG_UNDEF;
  private Flow content = new Flow(STYLE_PREFIX + STYLE_CONTENT);
  private BeeRowSet oldData;

  @Override
  public void handleAction(Action action) {
    switch (action) {
      case REFRESH:
        refresh(true);
        break;
      case ADD:
        add();
        break;
      default:
        break;
    }
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
    if (event.hasView(getEventsDataViewName())) {
      refresh(false);
    }
  }

  @Override
  public String getEventSource() {
    return null;
  }

  @Override
  public HeaderView getHeader() {
    return headerView;
  }

  @Override
  public View getMainView() {
    return null;
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    if (event.hasView(getEventsDataViewName())) {
      refresh(false);
    }
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
    if (event.hasView(getEventsDataViewName())) {
      refresh(false);
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (event.hasView(getEventsDataViewName())) {
      refresh(false);
    }
  }

  @Override
  protected void onUnload() {
    logger.debug("Unloading registry");
    EventUtils.clearRegistry(registry);
    super.onUnload();
  }

  @Override
  public void onViewUnload() {
  }

  @Override
  public void setEventSource(String eventSource) {
  }

  public void create(HasWidgets widget, long relId) {
    Assert.notNull(widget);
    this.relatedId = relId;
    createHeaderView();
    createContent();
    refresh(true);
    loadHandlerRegistry();
    widget.add(this);
  }

  public long getRelatedId() {
    return relatedId;
  }

  public void add() {
    DataInfo data = Data.getDataInfo(getEventsDataViewName());

    BeeRow row = RowFactory.createEmptyRow(data, true);
    RowFactory.createRow(data.getNewRowForm(), data.getCaption(), data, row, null,
        getNewEventFormInterceptor(), null);
  }

  public void refresh(boolean cleanCache) {
    if (content == null) {
      return;
    }

    getData(content, cleanCache);
  }

  protected abstract IdentifiableWidget getAddEventActionWidget();

  protected abstract String getAddEventFromName();

  protected abstract Set<Action> getEnabledActions();

  protected abstract Order getEventsDataOrder();

  protected abstract String getEventsDataViewName();

  protected abstract String getEventNoteColumnName();

  protected abstract String getEventTypeColumnName();

  protected abstract AbstractFormInterceptor getNewEventFormInterceptor();

  protected abstract String getPublisherPhotoColumnName();

  protected abstract String getPublisherFirstNameColumnName();

  protected abstract String getPublisherLastNameColumnName();

  protected abstract String getPublishTimeColumnName();

  protected abstract String getRelatedColumnName();


  @SuppressWarnings("unused")
  protected void afterCreateEventRow(BeeRowSet rs, BeeRow row, Flow eventRow) {
  }

  @SuppressWarnings("unused")
  protected void afterCreateEventCell(Flow eventRow, Flow eventCell, String name) {
  }

  @SuppressWarnings("unused")
  protected void afterCreateEventNoteCell(BeeRowSet rs, BeeRow row, Flow widget, Flow cell) {
  }

  @SuppressWarnings("unused")
  protected void beforeCreateEventNoteCell(BeeRowSet rs, BeeRow row, Flow widget) {
  }

  @SuppressWarnings("unused")
  protected void beforeCreateEventRow(BeeRowSet rs, BeeRow row, HasWidgets widget, int rowLevel) {
  }

  @SuppressWarnings("unused")
  protected void beforeCreateEventCell(Flow eventRow, String name) {
  }

  protected Flow createEventRowCell(Flow eventRow, String name, String styleName) {
    beforeCreateEventCell(eventRow, name);
    
    Flow eventCell = new Flow();
    eventCell.addStyleName(STYLE_PREFIX + STYLE_CONTENT_COL);

    if (!BeeUtils.isEmpty(name)) {
      eventCell.addStyleName(STYLE_PREFIX + STYLE_CONTENT_COL + BeeConst.STRING_MINUS + name);
    }
    if (!BeeUtils.isEmpty(styleName)) {
      eventCell.addStyleName(styleName);
    }
    
    if (!BeeUtils.isEmpty(getStylePrefix()) && !BeeUtils.isEmpty(name)) {
      eventCell.addStyleName(getStylePrefix() + name);
    }

    eventRow.add(eventCell);
    return eventCell;
  }

  protected Widget createCellHtmlItem(String name, String html) {
    Assert.notNull(name);
    Widget widget = new CustomDiv(STYLE_PREFIX + STYLE_CONTENT + BeeConst.STRING_MINUS + name);

    if (!BeeUtils.isEmpty(getStylePrefix())) {
      widget.addStyleName(getStylePrefix() + STYLE_CONTENT + BeeConst.STRING_MINUS + name);
    }

    if (!BeeUtils.isEmpty(html)) {
      widget.getElement().setInnerHTML(html);
    }

    return widget;
  }

  protected BeeRowSet getOldData() {
    return oldData;
  }

  protected String getStylePrefix() {
    return null;
  }

  protected String getParentEventColumnName() {
    return null;
  }

  protected void setOldData(BeeRowSet oldData) {
    this.oldData = oldData;
  }

  private void createChildEventRows(BeeRowSet rs, Multimap<Long, Long> data,
      long parent, HasWidgets widget, int rowLevel) {
    
    if (data.containsKey(parent)) {
      for (long id : data.get(parent)) {
        BeeRow row = rs.getRowById(id);
        createEventRow(rs, row, widget, rowLevel);
        createChildEventRows(rs, data, parent + 1, widget, rowLevel);
      }
    }
  }

  private void createContent() {
    if (!BeeUtils.isEmpty(getStylePrefix())) {
      content.addStyleName(getStylePrefix() + STYLE_CONTENT);
    }

    StyleUtils.setProperty(content, StyleUtils.STYLE_POSITION, Position.ABSOLUTE);

    if (headerView != null) {
      StyleUtils.setTop(content, headerView.getHeight());
    }

    add(content);
  }


  private void createEventRow(BeeRowSet rs, BeeRow row, HasWidgets widget, int rowLevel) {
    beforeCreateEventRow(rs, row, widget, rowLevel);

    Flow contentRow = new Flow();
    contentRow.addStyleName(STYLE_PREFIX + STYLE_CONTENT_ROW);

    if (!BeeUtils.isEmpty(getStylePrefix())) {
      contentRow.addStyleName(getStylePrefix() + STYLE_CONTENT_ROW);
    }
    
    // TODO: max row level;
    
    if (!BeeUtils.isEmpty(getPublisherPhotoColumnName())) {
      createPhotoCell(rs, row, contentRow);
    }

    createPublisherInfoCell(rs, row, contentRow);

    createEventNoteCell(rs, row, contentRow);

    afterCreateEventRow(rs, row, contentRow);

    widget.add(contentRow);
  }

  private void createEventNoteCell(BeeRowSet rs, BeeRow row, Flow widget) {
    beforeCreateEventNoteCell(rs, row, widget);
    Flow cell = createEventRowCell(widget, CONTENT_COL_EVENT_NOTE, null);

    int idxNote = rs.getColumnIndex(getEventNoteColumnName());

    if (BeeUtils.isNegative(idxNote)) {
      logger.warning("column ", getEventNoteColumnName(), "not found in view ",
          getEventsDataViewName());
      return;
    }

    cell.add(createCellHtmlItem(CELL_EVENT_NOTE, row.getString(idxNote)));

    afterCreateEventNoteCell(rs, row, widget, cell);
  }
  private void createHeaderView() {
    headerView = new HeaderImpl();
    headerView.setViewPresenter(this);
    IdentifiableWidget add = null;

    if (getEnabledActions().contains(Action.ADD) && getAddEventActionWidget() != null) {
      add = getAddEventActionWidget();
      if (add instanceof HasClickHandlers) {
        HIDDEN_ACTIONS.add(Action.ADD);

        ((HasClickHandlers) add).addClickHandler(new ClickHandler() {

          @Override
          public void onClick(ClickEvent arg0) {
            handleAction(Action.ADD);
          }
        });
      }
    }

    headerView.create(getCaption(), true, false, getEventsDataViewName(), null,
        getEnabledActions(), Action.NO_ACTIONS, HIDDEN_ACTIONS);

    if (!BeeUtils.isEmpty(getStylePrefix())) {
      headerView.addStyleName(getStylePrefix() + STYLE_HEADER);
    }

    headerView.clearCommandPanel();

    if (add != null) {
      headerView.addCommandItem(add);
    }
    add(headerView);
  }

  private void createPhotoCell(BeeRowSet rs, BeeRow row, Flow widget) {
    Flow cell = createEventRowCell(widget, CONTENT_COL_PHOTO, null);
    
    int idxPhoto = rs.getColumnIndex(getPublisherPhotoColumnName());
    
    if (BeeUtils.isNegative(idxPhoto)) {
      logger.warning("column ", getPublisherPhotoColumnName(), "not found in view ",
          getEventsDataViewName());
      return;
    }
    
    String photo = row.getString(idxPhoto);
    
    if (BeeUtils.isEmpty(photo)) {
      return;
    }
    
    Image image = new Image(PhotoRenderer.getUrl(photo));
    image.addStyleName(STYLE_PREFIX + STYLE_CONTENT_PHOTO);

    if (BeeUtils.isEmpty(getStylePrefix())) {
      image.addStyleName(getStylePrefix() + STYLE_CONTENT_PHOTO);
    }

    cell.add(image);
  }

  private void createPublisherInfoCell(BeeRowSet rs, BeeRow row, Flow widget) {
    Flow cell = createEventRowCell(widget, CONTENT_COL_PUBLISHER_INFO, null);

    if (!BeeUtils.isEmpty(getEventTypeColumnName())) {
      int idxEvent = rs.getColumnIndex(getEventTypeColumnName());
      
      if (!BeeUtils.isNegative(idxEvent)) {
        String text =
            EnumUtils.getCaption(rs.getColumn(idxEvent).getEnumKey(), row.getInteger(idxEvent));

        cell.add(createCellHtmlItem(CELL_EVENT_TYPE, text));

      } else {
        logger.warning("column", getEventTypeColumnName(), "not found in view",
            getEventsDataViewName());
      }
    }

    if (!BeeUtils.isEmpty(getPublishTimeColumnName())) {
      int idxCol = rs.getColumnIndex(getPublishTimeColumnName());

      if (!BeeUtils.isNegative(idxCol)) {
        DateTime publishTime = row.getDateTime(idxCol);
        if (publishTime != null) {
          cell.add(createCellHtmlItem(CELL_EVENT_PUBLISH, Format.getDefaultDateTimeFormat().format(
              publishTime)));
        }

      } else {
        logger.warning("column", getPublishTimeColumnName(), "not found in view",
            getEventsDataViewName());
      }
    }

    if (!BeeUtils.isEmpty(getPublisherFirstNameColumnName())
        || !BeeUtils.isEmpty(getPublisherFirstNameColumnName())) {
     
      int idxFirst = rs.getColumnIndex(getPublisherFirstNameColumnName());
      int idxLast = rs.getColumnIndex(getPublisherLastNameColumnName());
      String fullName = BeeConst.STRING_EMPTY;

      if (!BeeUtils.isNegative(idxFirst)) {
        fullName = row.getString(idxFirst);
      }

      if (!BeeUtils.isNegative(idxLast)) {
        fullName = BeeUtils.joinWords(fullName, row.getString(idxFirst));
      }

      cell.add(createCellHtmlItem(CELL_EVENT_PUBLISHER, fullName));
    }

  }

  private RowSetCallback getDataCallback(final HasWidgets cont, final boolean clearCache) {
    return new RowSetCallback() {

      @Override
      public void onSuccess(BeeRowSet result) {
        logger.debug("parse event data from", getEventsDataViewName());
        if (!clearCache && getOldData() != null && result != null) {
          if (getOldData().getNumberOfRows() == result.getNumberOfRows()
              && getOldData().getRow(getOldData().getNumberOfRows() - 1).getId()
                == result.getRow(result.getNumberOfRows() - 1).getId()) {
            // TODO: create some methods validate that data is same;
            return;
          }
        }
        cont.clear();
        setOldData(result);

        prepareCascadedStructure(cont, result);
      }
    };
  }

  private void getData(final HasWidgets cont, final boolean clearCache) {
    Assert.isTrue(DataUtils.isId(getRelatedId()));
    Assert.notEmpty(getRelatedColumnName());
    Assert.notEmpty(getEventsDataViewName());

    Filter filter = Filter.equals(getRelatedColumnName(), getRelatedId());

    DataInfo info = Data.getDataInfo(getEventsDataViewName());

    Queries.getRowSet(getEventsDataViewName(), info.getColumnNames(false), filter,
        getEventsDataOrder(), getDataCallback(cont, clearCache));
  }

  private void loadHandlerRegistry() {
    logger.debug("load register of", getEventsDataViewName());
    registry.add(BeeKeeper.getBus().registerRowInsertHandler(this, false));
    registry.addAll(BeeKeeper.getBus().registerUpdateHandler(this, false));
    registry.add(BeeKeeper.getBus().registerDataChangeHandler(this, false));
  }

  private void prepareCascadedStructure(HasWidgets widget, BeeRowSet rs) {
    String parentColumnName = getParentEventColumnName();

    List<Long> roots = Lists.newArrayList();
    Multimap<Long, Long> data = HashMultimap.create();

    if (BeeUtils.isEmpty(parentColumnName)) {
      roots = rs.getRowIds();
    } else {
      int idxParent = rs.getColumnIndex(parentColumnName);

      if (BeeUtils.isNegative(idxParent)) {
        logger.error(null, "Parent column", parentColumnName, "not found in view",
            getEventsDataViewName());
        return;
      }

      for (BeeRow row : rs.getRows()) {
        Long parent = row.getLong(idxParent);

        if (parent == null) {
          roots.add(row.getId());
        } else {
          data.put(parent, row.getId());
        }
      }
    }

    for (long id : roots) {
      BeeRow row = rs.getRowById(id);
      createEventRow(rs, row, widget, 0);
      createChildEventRows(rs, data, id, widget, 1);
    }

    if (widget instanceof ComplexPanel) {
      ComplexPanel panel = (ComplexPanel) widget;

      if (panel.getWidgetCount() > 0 && DomUtils.isVisible(panel.getParent())) {
        final Widget last = panel.getWidget(panel.getWidgetCount() - 1);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
          @Override
          public void execute() {
            last.getElement().scrollIntoView();
          }
        });
      }
    }
  }
}
