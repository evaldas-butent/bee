package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.Event.Type;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;

import com.butent.bee.client.ui.CompositeService;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.ParentRowEvent;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * creates handlers for events such as mouse and keyboard actions or server responses.
 */

public class EventManager implements Module {

  private final SimpleEventBus eventBus;

  private HandlerRegistration exitRegistry = null;

  public EventManager() {
    this.eventBus = new SimpleEventBus();
  }

  public <H> HandlerRegistration addHandler(Type<H> type, H handler) {
    Assert.notNull(type);
    Assert.notNull(handler);
    return eventBus.addHandler(type, handler);
  }

  public <H> HandlerRegistration addHandlerToSource(Type<H> type, Object source, H handler) {
    Assert.notNull(type);
    Assert.notNull(source);
    Assert.notNull(handler);
    return eventBus.addHandlerToSource(type, source, handler);
  }

  public boolean dispatchService(Stage stage, Widget source) {
    Assert.notNull(stage);
    return dispatchService(stage.getService(), stage.getStage(), source);
  }

  public boolean dispatchService(String svc) {
    return dispatchService(svc, null, null);
  }

  public boolean dispatchService(String svc, String stg, Widget source) {
    Assert.notEmpty(svc);

    if (CompositeService.isRegistered(svc)) {
      return CompositeService.doService(svc, stg, source);

    } else if (Service.isRpcService(svc)) {
      BeeKeeper.getRpc().makeGetRequest(svc);
      return true;
    } else if (Service.isUiService(svc)) {
      return dispatchUiService(svc, source);
    } else if (Service.isCompositeService(svc)) {
      return dispatchCompositeService(svc, stg, source);
    } else {
      Global.showError("Unknown service type", svc);
      return false;
    }
  }

  public void end() {
  }

  public void fireEvent(Event<?> event) {
    Assert.notNull(event);
    eventBus.fireEvent(event);
  }

  public void fireEventFromSource(Event<?> event, Object source) {
    Assert.notNull(event);
    Assert.notNull(source);
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

  public HandlerRegistration registerCellUpdateHandler(CellUpdateEvent.Handler handler) {
    return CellUpdateEvent.register(eventBus, handler);
  }

  public Collection<HandlerRegistration> registerDataHandler(HandlesAllDataEvents handler) {
    Assert.notNull(handler);

    List<HandlerRegistration> registry = Lists.newArrayList();
    registry.add(registerCellUpdateHandler(handler));
    registry.add(registerMultiDeleteHandler(handler));
    registry.add(registerRowDeleteHandler(handler));
    registry.add(registerRowInsertHandler(handler));
    registry.add(registerRowUpdateHandler(handler));

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

  public HandlerRegistration registerMultiDeleteHandler(MultiDeleteEvent.Handler handler) {
    return MultiDeleteEvent.register(eventBus, handler);
  }

  public HandlerRegistration registerParentRowHandler(Object source, ParentRowEvent.Handler handler) {
    return ParentRowEvent.register(eventBus, source, handler);
  }

  public HandlerRegistration registerRowActionHandler(RowActionEvent.Handler handler) {
    return RowActionEvent.register(eventBus, handler);
  }
  
  public HandlerRegistration registerRowDeleteHandler(RowDeleteEvent.Handler handler) {
    return RowDeleteEvent.register(eventBus, handler);
  }

  public HandlerRegistration registerRowInsertHandler(RowInsertEvent.Handler handler) {
    return RowInsertEvent.register(eventBus, handler);
  }

  public HandlerRegistration registerRowUpdateHandler(RowUpdateEvent.Handler handler) {
    return RowUpdateEvent.register(eventBus, handler);
  }

  public void removeExitHandler() {
    if (this.exitRegistry != null) {
      this.exitRegistry.removeHandler();
      this.exitRegistry = null;
    }
  }

  public void start() {
  }

  private boolean dispatchCompositeService(String svc, String stg, Widget source) {
    Assert.notEmpty(svc);
    Assert.notEmpty(stg);

    boolean ok = false;

    if (svc.equals(Service.GET_CLASS)) {
      if (stg.equals(Stage.STAGE_GET_PARAMETERS)) {
        Global.inputVars(new Stage(Service.GET_CLASS, Stage.STAGE_CONFIRM),
            "Class Info", Service.VAR_CLASS_NAME, Service.VAR_PACKAGE_LIST);
        ok = true;
      } else if (stg.equals(Stage.STAGE_CONFIRM)) {
        String cls = BeeUtils.trim(Global.getVarValue(Service.VAR_CLASS_NAME));
        String pck = BeeUtils.trim(Global.getVarValue(Service.VAR_PACKAGE_LIST));

        if (BeeUtils.isEmpty(cls)) {
          Global.showError("Class name not specified");
        } else if (cls.length() < 2) {
          Global.showError("Class name", cls, "too short");
        } else {
          Global.closeDialog(source);
          BeeKeeper.getRpc().makePostRequest(Service.GET_CLASS_INFO,
              XmlUtils.createString(Service.XML_TAG_DATA,
                  Service.VAR_CLASS_NAME, cls, Service.VAR_PACKAGE_LIST, pck));
          ok = true;
        }
      } else {
        Global.showError("Unknown composite service stage", svc, stg);
      }

    } else if (svc.equals(Service.GET_XML)) {
      if (stg.equals(Stage.STAGE_GET_PARAMETERS)) {
        Global.inputVars(new Stage(Service.GET_XML, Stage.STAGE_CONFIRM),
            "Xml Info", Service.VAR_XML_SOURCE, Service.VAR_XML_TRANSFORM,
            Service.VAR_XML_TARGET, Service.VAR_XML_RETURN);
        ok = true;
      } else if (stg.equals(Stage.STAGE_CONFIRM)) {
        String src = Global.getVarValue(Service.VAR_XML_SOURCE);
        if (BeeUtils.isEmpty(src)) {
          Global.showError("Source not specified");
        } else {
          Global.closeDialog(source);
          BeeKeeper.getRpc().makePostRequest(Service.GET_XML_INFO,
              XmlUtils.fromVars(Service.XML_TAG_DATA,
                  Service.VAR_XML_SOURCE, Service.VAR_XML_TRANSFORM,
                  Service.VAR_XML_TARGET, Service.VAR_XML_RETURN));
          ok = true;
        }
      } else {
        Global.showError("Unknown composite service stage", svc, stg);
      }

    } else if (svc.equals(Service.GET_DATA)) {
      if (stg.equals(Stage.STAGE_GET_PARAMETERS)) {
        Global.inputVars(new Stage(Service.GET_DATA, Stage.STAGE_CONFIRM),
            "Jdbc Test", Service.VAR_JDBC_QUERY,
            Service.VAR_CONNECTION_AUTO_COMMIT,
            Service.VAR_CONNECTION_HOLDABILITY,
            Service.VAR_CONNECTION_READ_ONLY,
            Service.VAR_CONNECTION_TRANSACTION_ISOLATION,
            Service.VAR_STATEMENT_CURSOR_NAME,
            Service.VAR_STATEMENT_ESCAPE_PROCESSING,
            Service.VAR_STATEMENT_FETCH_DIRECTION,
            Service.VAR_STATEMENT_FETCH_SIZE,
            Service.VAR_STATEMENT_MAX_FIELD_SIZE,
            Service.VAR_STATEMENT_MAX_ROWS,
            Service.VAR_STATEMENT_POOLABLE,
            Service.VAR_STATEMENT_QUERY_TIMEOUT,
            Service.VAR_STATEMENT_RS_TYPE,
            Service.VAR_STATEMENT_RS_CONCURRENCY,
            Service.VAR_STATEMENT_RS_HOLDABILITY,
            Service.VAR_RESULT_SET_FETCH_DIRECTION,
            Service.VAR_RESULT_SET_FETCH_SIZE,
            Service.VAR_JDBC_RETURN);
        ok = true;
      } else if (stg.equals(Stage.STAGE_CONFIRM)) {
        String sql = Global.getVarValue(Service.VAR_JDBC_QUERY);
        if (BeeUtils.isEmpty(sql)) {
          Global.showError("Query not specified");
        } else {
          Global.closeDialog(source);
          BeeKeeper.getRpc().makePostRequest(Service.DB_JDBC,
              XmlUtils.fromVars(Service.XML_TAG_DATA,
                  Service.VAR_JDBC_QUERY,
                  Service.VAR_CONNECTION_AUTO_COMMIT,
                  Service.VAR_CONNECTION_HOLDABILITY,
                  Service.VAR_CONNECTION_READ_ONLY,
                  Service.VAR_CONNECTION_TRANSACTION_ISOLATION,
                  Service.VAR_STATEMENT_CURSOR_NAME,
                  Service.VAR_STATEMENT_ESCAPE_PROCESSING,
                  Service.VAR_STATEMENT_FETCH_DIRECTION,
                  Service.VAR_STATEMENT_FETCH_SIZE,
                  Service.VAR_STATEMENT_MAX_FIELD_SIZE,
                  Service.VAR_STATEMENT_MAX_ROWS,
                  Service.VAR_STATEMENT_POOLABLE,
                  Service.VAR_STATEMENT_QUERY_TIMEOUT,
                  Service.VAR_STATEMENT_RS_TYPE,
                  Service.VAR_STATEMENT_RS_CONCURRENCY,
                  Service.VAR_STATEMENT_RS_HOLDABILITY,
                  Service.VAR_RESULT_SET_FETCH_DIRECTION,
                  Service.VAR_RESULT_SET_FETCH_SIZE,
                  Service.VAR_JDBC_RETURN));
          ok = true;
        }
      } else {
        Global.showError("Unknown composite service stage", svc, stg);
      }

    } else {
      Global.showError("Unknown composite service", svc, stg);
    }

    return ok;
  }

  private boolean dispatchUiService(String svc, Widget source) {
    if (svc.equals(Service.CLOSE_DIALOG)) {
      return Global.closeDialog(source);
    } else if (svc.equals(Service.CONFIRM_DIALOG)) {
      return Global.closeDialog(source);
    } else if (svc.equals(Service.CANCEL_DIALOG)) {
      return Global.closeDialog(source);
    } else if (svc.equals(Service.REFRESH_MENU)) {
      return BeeKeeper.getMenu().drawMenu();
    } else {
      Global.showError("Unknown UI service", svc);
      return false;
    }
  }
}
