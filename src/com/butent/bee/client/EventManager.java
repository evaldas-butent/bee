package com.butent.bee.client;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.event.BeeBlurHandler;
import com.butent.bee.client.event.BeeChangeHandler;
import com.butent.bee.client.event.BeeClickHandler;
import com.butent.bee.client.event.BeeKeyPressHandler;
import com.butent.bee.client.event.BeeValueChangeHandler;
import com.butent.bee.client.ui.CompositeService;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.BeeStage;
import com.butent.bee.shared.utils.BeeUtils;

public class EventManager implements Module {
  private BeeClickHandler clickHandler = null;
  private BeeKeyPressHandler keyHandler = null;

  private BeeValueChangeHandler<Boolean> boolVch = null;
  private BeeValueChangeHandler<String> stringVch = null;
  private BeeValueChangeHandler<Integer> intVch = null;

  private BeeChangeHandler vch = null;

  private BeeBlurHandler blurHandler = null;

  private SimpleEventBus eventBus;

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

  public boolean dispatchService(String svc, String stg, GwtEvent<?> event) {
    Assert.notEmpty(svc);

    if (BeeService.isRpcService(svc)) {
      BeeKeeper.getRpc().makeGetRequest(svc);
      return true;
    } else if (BeeService.isUiService(svc)) {
      return dispatchUiService(svc, event);
    } else if (BeeService.isCompositeService(svc)) {
      return dispatchCompositeService(svc, stg, event);
    } else {
      Global.showError("Unknown service type", svc);
      return false;
    }
  }

  public void end() {
  }

  public void fireEvent(GwtEvent<?> ev) {
    eventBus.fireEvent(ev);
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

  public void start() {
  }

  private boolean dispatchCompositeService(String svc, String stg,
      GwtEvent<?> event) {
    Assert.notEmpty(svc);
    Assert.notEmpty(stg);

    boolean ok = false;

    if (svc.equals(BeeService.SERVICE_GET_CLASS)) {
      if (stg.equals(BeeStage.STAGE_GET_PARAMETERS)) {
        Global.inputVars(new BeeStage(BeeService.SERVICE_GET_CLASS, BeeStage.STAGE_CONFIRM),
            "Class Info", BeeService.VAR_CLASS_NAME, BeeService.VAR_PACKAGE_LIST);
        ok = true;
      } else if (stg.equals(BeeStage.STAGE_CONFIRM)) {
        String cls = Global.getVarValue(BeeService.VAR_CLASS_NAME);
        String pck = Global.getVarValue(BeeService.VAR_PACKAGE_LIST);

        if (BeeUtils.isEmpty(cls)) {
          Global.showError("Class name not specified");
        } else if (cls.trim().length() < 2) {
          Global.showError("Class name", cls, "too short");
        } else {
          Global.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(BeeService.SERVICE_CLASS_INFO,
              XmlUtils.createString(BeeService.XML_TAG_DATA,
                  BeeService.VAR_CLASS_NAME, cls, BeeService.VAR_PACKAGE_LIST, pck));
          ok = true;
        }
      } else {
        Global.showError("Unknown composite service stage", svc, stg);
      }

    } else if (svc.equals(BeeService.SERVICE_GET_XML)) {
      if (stg.equals(BeeStage.STAGE_GET_PARAMETERS)) {
        Global.inputVars(new BeeStage(BeeService.SERVICE_GET_XML,
            BeeStage.STAGE_CONFIRM), "Xml Info", BeeService.VAR_XML_SOURCE,
            BeeService.VAR_XML_TRANSFORM, BeeService.VAR_XML_TARGET,
            BeeService.VAR_XML_RETURN);
        ok = true;
      } else if (stg.equals(BeeStage.STAGE_CONFIRM)) {
        String src = Global.getVarValue(BeeService.VAR_XML_SOURCE);
        if (BeeUtils.isEmpty(src)) {
          Global.showError("Source not specified");
        } else {
          Global.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(
              BeeService.SERVICE_XML_INFO,
              XmlUtils.fromVars(BeeService.XML_TAG_DATA,
                  BeeService.VAR_XML_SOURCE, BeeService.VAR_XML_TRANSFORM,
                  BeeService.VAR_XML_TARGET, BeeService.VAR_XML_RETURN));
          ok = true;
        }
      } else {
        Global.showError("Unknown composite service stage", svc, stg);
      }

    } else if (svc.equals(BeeService.SERVICE_GET_DATA)) {
      if (stg.equals(BeeStage.STAGE_GET_PARAMETERS)) {
        Global.inputVars(new BeeStage(BeeService.SERVICE_GET_DATA,
            BeeStage.STAGE_CONFIRM), "Jdbc Test", BeeService.VAR_JDBC_QUERY,
            BeeService.VAR_CONNECTION_AUTO_COMMIT,
            BeeService.VAR_CONNECTION_HOLDABILITY,
            BeeService.VAR_CONNECTION_READ_ONLY,
            BeeService.VAR_CONNECTION_TRANSACTION_ISOLATION,
            BeeService.VAR_STATEMENT_CURSOR_NAME,
            BeeService.VAR_STATEMENT_ESCAPE_PROCESSING,
            BeeService.VAR_STATEMENT_FETCH_DIRECTION,
            BeeService.VAR_STATEMENT_FETCH_SIZE,
            BeeService.VAR_STATEMENT_MAX_FIELD_SIZE,
            BeeService.VAR_STATEMENT_MAX_ROWS,
            BeeService.VAR_STATEMENT_POOLABLE,
            BeeService.VAR_STATEMENT_QUERY_TIMEOUT,
            BeeService.VAR_STATEMENT_RS_TYPE,
            BeeService.VAR_STATEMENT_RS_CONCURRENCY,
            BeeService.VAR_STATEMENT_RS_HOLDABILITY,
            BeeService.VAR_RESULT_SET_FETCH_DIRECTION,
            BeeService.VAR_RESULT_SET_FETCH_SIZE,
            BeeService.VAR_JDBC_RETURN);
        ok = true;
      } else if (stg.equals(BeeStage.STAGE_CONFIRM)) {
        String sql = Global.getVarValue(BeeService.VAR_JDBC_QUERY);
        if (BeeUtils.isEmpty(sql)) {
          Global.showError("Query not specified");
        } else {
          Global.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(
              BeeService.SERVICE_DB_JDBC,
              XmlUtils.fromVars(BeeService.XML_TAG_DATA,
                  BeeService.VAR_JDBC_QUERY,
                  BeeService.VAR_CONNECTION_AUTO_COMMIT,
                  BeeService.VAR_CONNECTION_HOLDABILITY,
                  BeeService.VAR_CONNECTION_READ_ONLY,
                  BeeService.VAR_CONNECTION_TRANSACTION_ISOLATION,
                  BeeService.VAR_STATEMENT_CURSOR_NAME,
                  BeeService.VAR_STATEMENT_ESCAPE_PROCESSING,
                  BeeService.VAR_STATEMENT_FETCH_DIRECTION,
                  BeeService.VAR_STATEMENT_FETCH_SIZE,
                  BeeService.VAR_STATEMENT_MAX_FIELD_SIZE,
                  BeeService.VAR_STATEMENT_MAX_ROWS,
                  BeeService.VAR_STATEMENT_POOLABLE,
                  BeeService.VAR_STATEMENT_QUERY_TIMEOUT,
                  BeeService.VAR_STATEMENT_RS_TYPE,
                  BeeService.VAR_STATEMENT_RS_CONCURRENCY,
                  BeeService.VAR_STATEMENT_RS_HOLDABILITY,
                  BeeService.VAR_RESULT_SET_FETCH_DIRECTION,
                  BeeService.VAR_RESULT_SET_FETCH_SIZE,
                  BeeService.VAR_JDBC_RETURN));
          ok = true;
        }
      } else {
        Global.showError("Unknown composite service stage", svc, stg);
      }
    } else if (CompositeService.isRegistered(svc)) {
      CompositeService.doService(svc, event, stg);
    } else {
      Global.showError("Unknown composite service", svc, stg);
    }

    return ok;
  }

  private boolean dispatchUiService(String svc, GwtEvent<?> event) {
    if (svc.equals(BeeService.SERVICE_CLOSE_DIALOG)) {
      return Global.closeDialog(event);
    } else if (svc.equals(BeeService.SERVICE_CONFIRM_DIALOG)) {
      return Global.closeDialog(event);
    } else if (svc.equals(BeeService.SERVICE_CANCEL_DIALOG)) {
      return Global.closeDialog(event);
    } else if (svc.equals(BeeService.SERVICE_REFRESH_MENU)) {
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
