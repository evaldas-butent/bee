package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.Event.Type;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;

import com.butent.bee.client.screen.BookmarkEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.ParentRowEvent;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;

import java.util.Collection;
import java.util.List;

/**
 * creates handlers for events such as mouse and keyboard actions or server responses.
 */

public class EventManager implements Module {

  private final EventBus priorBus;
  private final EventBus eventBus;

  private HandlerRegistration exitRegistry = null;

  public EventManager() {
    this.priorBus = new SimpleEventBus();
    this.eventBus = new SimpleEventBus();
  }

  public <H> HandlerRegistration addHandler(Type<H> type, H handler, boolean prior) {
    Assert.notNull(type);
    Assert.notNull(handler);

    return getBus(prior).addHandler(type, handler);
  }

  public <H> HandlerRegistration addHandlerToSource(Type<H> type, Object source, H handler,
      boolean prior) {
    Assert.notNull(type);
    Assert.notNull(source);
    Assert.notNull(handler);

    return getBus(prior).addHandlerToSource(type, source, handler);
  }

  public boolean dispatchService(String svc) {
    return dispatchService(svc, null);
  }

  public boolean dispatchService(String svc, Widget source) {
    Assert.notEmpty(svc);

    if (Service.isRpcService(svc)) {
      BeeKeeper.getRpc().makeGetRequest(svc);
      return true;
    } else if (Service.isUiService(svc)) {
      return dispatchUiService(svc, source);
    } else {
      Global.showError("Unknown service type", svc);
      return false;
    }
  }

  public void end() {
  }

  public void fireEvent(Event<?> event) {
    Assert.notNull(event);

    priorBus.fireEvent(event);
    eventBus.fireEvent(event);
  }

  public void fireEventFromSource(Event<?> event, Object source) {
    Assert.notNull(event);
    Assert.notNull(source);

    priorBus.fireEventFromSource(event, source);
    eventBus.fireEventFromSource(event, source);
  }

  public String getName() {
    return getClass().getName();
  }

  public int getPriority(int p) {
    switch (p) {
      case PRIORITY_INIT:
        return DO_NOT_CALL;
      case PRIORITY_START:
        return DO_NOT_CALL;
      case PRIORITY_END:
        return DO_NOT_CALL;
      default:
        return DO_NOT_CALL;
    }
  }

  public void init() {
    initEvents();
  }

  public void initEvents() {
  }

  public HandlerRegistration registerBookmarkHandler(BookmarkEvent.Handler handler, boolean prior) {
    return BookmarkEvent.register(getBus(prior), handler);
  }

  public HandlerRegistration registerCellUpdateHandler(CellUpdateEvent.Handler handler,
      boolean prior) {
    return CellUpdateEvent.register(getBus(prior), handler);
  }

  public Collection<HandlerRegistration> registerDataHandler(HandlesAllDataEvents handler,
      boolean prior) {
    Assert.notNull(handler);

    List<HandlerRegistration> registry = Lists.newArrayList();
    registry.add(registerCellUpdateHandler(handler, prior));
    registry.add(registerMultiDeleteHandler(handler, prior));
    registry.add(registerRowDeleteHandler(handler, prior));
    registry.add(registerRowInsertHandler(handler, prior));
    registry.add(registerRowUpdateHandler(handler, prior));

    return registry;
  }

  public void registerExitHandler(final String message) {
    Assert.notNull(message);

    removeExitHandler();
    this.exitRegistry = Window.addWindowClosingHandler(new ClosingHandler() {
      public void onWindowClosing(ClosingEvent event) {
        event.setMessage(message);
      }
    });
  }

  public HandlerRegistration registerMultiDeleteHandler(MultiDeleteEvent.Handler handler,
      boolean prior) {
    return MultiDeleteEvent.register(getBus(prior), handler);
  }

  public HandlerRegistration registerParentRowHandler(Object source, ParentRowEvent.Handler handler,
      boolean prior) {
    return ParentRowEvent.register(getBus(prior), source, handler);
  }

  public HandlerRegistration registerRowActionHandler(RowActionEvent.Handler handler,
      boolean prior) {
    return RowActionEvent.register(getBus(prior), handler);
  }

  public HandlerRegistration registerRowDeleteHandler(RowDeleteEvent.Handler handler,
      boolean prior) {
    return RowDeleteEvent.register(getBus(prior), handler);
  }

  public HandlerRegistration registerRowInsertHandler(RowInsertEvent.Handler handler,
      boolean prior) {
    return RowInsertEvent.register(getBus(prior), handler);
  }

  public HandlerRegistration registerRowTransformHandler(RowTransformEvent.Handler handler,
      boolean prior) {
    return RowTransformEvent.register(getBus(prior), handler);
  }
  
  public HandlerRegistration registerRowUpdateHandler(RowUpdateEvent.Handler handler,
      boolean prior) {
    return RowUpdateEvent.register(getBus(prior), handler);
  }

  public void removeExitHandler() {
    if (this.exitRegistry != null) {
      this.exitRegistry.removeHandler();
      this.exitRegistry = null;
    }
  }

  public void start() {
  }

  private boolean dispatchUiService(String svc, Widget source) {
    if (svc.equals(Service.CLOSE_DIALOG)) {
      return Global.closeDialog(source);
    } else if (svc.equals(Service.REFRESH_MENU)) {
      return BeeKeeper.getMenu().loadMenu();
    } else {
      Global.showError("Unknown UI service", svc);
      return false;
    }
  }
  
  private EventBus getBus(boolean prior) {
    return prior ? priorBus : eventBus;
  }
}
