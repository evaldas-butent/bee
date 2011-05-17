package com.butent.bee.client;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.Event.Type;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;

import com.butent.bee.client.event.BeeBlurHandler;
import com.butent.bee.client.event.BeeChangeHandler;
import com.butent.bee.client.event.BeeClickHandler;
import com.butent.bee.client.event.BeeKeyPressHandler;
import com.butent.bee.client.event.BeeValueChangeHandler;
import com.butent.bee.client.ui.CompositeService;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * creates handlers for events such as mouse and keyboard actions or server responses.
 */

public class EventManager implements Module {
  private BeeClickHandler clickHandler = null;
  private BeeKeyPressHandler keyHandler = null;

  private BeeValueChangeHandler<Boolean> boolVch = null;
  private BeeValueChangeHandler<String> stringVch = null;
  private BeeValueChangeHandler<Integer> intVch = null;

  private BeeChangeHandler vch = null;

  private BeeBlurHandler blurHandler = null;

  private final SimpleEventBus eventBus;

  public EventManager() {
    this.eventBus = new SimpleEventBus();
  }

  public void addBlurHandler(Widget w, boolean sink) {
    Assert.notNull(w);
    if (sink) {
      w.addDomHandler(ensureBlurHandler(), BlurEvent.getType());
    } else {
      w.addHandler(ensureBlurHandler(), BlurEvent.getType());
    }
  }

  public void addBoolVch(HasValueChangeHandlers<Boolean> w) {
    Assert.notNull(w);
    w.addValueChangeHandler(ensureBoolVch());
  }

  public void addClickHandler(HasClickHandlers w) {
    Assert.notNull(w);
    w.addClickHandler(ensureClickHandler());
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
  
  public void addIntVch(HasValueChangeHandlers<Integer> w) {
    Assert.notNull(w);
    w.addValueChangeHandler(ensureIntVch());
  }

  public void addKeyHandler(HasKeyPressHandlers w) {
    Assert.notNull(w);
    w.addKeyPressHandler(ensureKeyHandler());
  }

  public void addStringVch(HasValueChangeHandlers<String> w) {
    Assert.notNull(w);
    w.addValueChangeHandler(ensureStringVch());
  }

  public void addVch(HasChangeHandlers w) {
    Assert.notNull(w);
    w.addChangeHandler(ensureVch());
  }

  public boolean dispatchService(String svc, String stg, Event<?> event) {
    Assert.notEmpty(svc);

    if (Service.isRpcService(svc)) {
      BeeKeeper.getRpc().makeGetRequest(svc);
      return true;
    } else if (Service.isUiService(svc)) {
      return dispatchUiService(svc, event);
    } else if (Service.isCompositeService(svc)) {
      return dispatchCompositeService(svc, stg, event);
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

  public HandlerRegistration registerMultiDeleteHandler(MultiDeleteEvent.Handler handler) {
    return MultiDeleteEvent.register(eventBus, handler);
  }
  
  public HandlerRegistration registerRowDeleteHandler(RowDeleteEvent.Handler handler) {
    return RowDeleteEvent.register(eventBus, handler);
  }
 
  public void start() {
  }

  private boolean dispatchCompositeService(String svc, String stg, Event<?> event) {
    Assert.notEmpty(svc);
    Assert.notEmpty(stg);

    boolean ok = false;

    if (svc.equals(Service.GET_CLASS)) {
      if (stg.equals(Stage.STAGE_GET_PARAMETERS)) {
        Global.inputVars(new Stage(Service.GET_CLASS, Stage.STAGE_CONFIRM),
            "Class Info", Service.VAR_CLASS_NAME, Service.VAR_PACKAGE_LIST);
        ok = true;
      } else if (stg.equals(Stage.STAGE_CONFIRM)) {
        String cls = Global.getVarValue(Service.VAR_CLASS_NAME);
        String pck = Global.getVarValue(Service.VAR_PACKAGE_LIST);

        if (BeeUtils.isEmpty(cls)) {
          Global.showError("Class name not specified");
        } else if (cls.trim().length() < 2) {
          Global.showError("Class name", cls, "too short");
        } else {
          Global.closeDialog(event);
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
          Global.closeDialog(event);
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
          Global.closeDialog(event);
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

    } else if (svc.equals(Service.GET_LOGIN)) {
      if (stg.equals(Stage.STAGE_GET_PARAMETERS)) {
        Global.inputVars(new Stage(Service.GET_LOGIN, Stage.STAGE_CONFIRM),
            "Login", Service.VAR_LOGIN, Service.VAR_PASSWORD);
        ok = true;

      } else if (stg.equals(Stage.STAGE_CONFIRM)) {
        String usr = Global.getVarValue(Service.VAR_LOGIN);
        String pwd = Global.getVarValue(Service.VAR_PASSWORD);

        if (BeeUtils.isEmpty(usr)) {
          Global.showError("Login name not specified");
        } else if (BeeUtils.isEmpty(pwd)) {
          Global.showError("Password not specified");
        } else {
          Global.setVarValue(Service.VAR_LOGIN, "");
          Global.setVarValue(Service.VAR_PASSWORD, "");
          Global.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(Service.LOGIN,
              XmlUtils.createString(Service.XML_TAG_DATA,
                    Service.VAR_LOGIN, usr, Service.VAR_PASSWORD, pwd));
          ok = true;
        }
      } else {
        Global.showError("Unknown composite service stage", svc, stg);
      }

    } else if (CompositeService.isRegistered(svc)) {
      CompositeService.doService(svc, stg, event);

    } else {
      Global.showError("Unknown composite service", svc, stg);
    }

    return ok;
  }

  private boolean dispatchUiService(String svc, Event<?> event) {
    if (svc.equals(Service.CLOSE_DIALOG)) {
      return Global.closeDialog(event);
    } else if (svc.equals(Service.CONFIRM_DIALOG)) {
      return Global.closeDialog(event);
    } else if (svc.equals(Service.CANCEL_DIALOG)) {
      return Global.closeDialog(event);
    } else if (svc.equals(Service.REFRESH_MENU)) {
      return BeeKeeper.getMenu().drawMenu();
    } else {
      Global.showError("Unknown UI service", svc);
      return false;
    }
  }

  private BeeBlurHandler ensureBlurHandler() {
    if (blurHandler == null) {
      blurHandler = new BeeBlurHandler();
    }
    return blurHandler;
  }

  private BeeValueChangeHandler<Boolean> ensureBoolVch() {
    if (boolVch == null) {
      boolVch = new BeeValueChangeHandler<Boolean>();
    }
    return boolVch;
  }

  private BeeClickHandler ensureClickHandler() {
    if (clickHandler == null) {
      clickHandler = new BeeClickHandler();
    }
    return clickHandler;
  }

  private BeeValueChangeHandler<Integer> ensureIntVch() {
    if (intVch == null) {
      intVch = new BeeValueChangeHandler<Integer>();
    }
    return intVch;
  }

  private BeeKeyPressHandler ensureKeyHandler() {
    if (keyHandler == null) {
      keyHandler = new BeeKeyPressHandler();
    }
    return keyHandler;
  }

  private BeeValueChangeHandler<String> ensureStringVch() {
    if (stringVch == null) {
      stringVch = new BeeValueChangeHandler<String>();
    }
    return stringVch;
  }

  private BeeChangeHandler ensureVch() {
    if (vch == null) {
      vch = new BeeChangeHandler();
    }
    return vch;
  }
}
