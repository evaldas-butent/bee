package com.butent.bee.egg.client;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.event.BeeBlurHandler;
import com.butent.bee.egg.client.event.BeeChangeHandler;
import com.butent.bee.egg.client.event.BeeClickHandler;
import com.butent.bee.egg.client.event.BeeKeyPressHandler;
import com.butent.bee.egg.client.event.BeeValueChangeHandler;
import com.butent.bee.egg.client.ui.CompositeService;
import com.butent.bee.egg.client.utils.BeeXml;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeBus implements BeeModule {
  private BeeClickHandler clickHandler = null;
  private BeeKeyPressHandler keyHandler = null;

  private BeeValueChangeHandler<Boolean> boolVch = null;
  private BeeValueChangeHandler<String> stringVch = null;
  private BeeValueChangeHandler<Integer> intVch = null;

  private BeeChangeHandler vch = null;

  private BeeBlurHandler blurHandler = null;

  private HandlerManager eventBus;

  public BeeBus(HandlerManager bus) {
    this.eventBus = bus;
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
      return BeeKeeper.getRpc().dispatchService(svc);
    } else if (BeeService.isUiService(svc)) {
      return dispatchUiService(svc, event);
    } else if (BeeService.isCompositeService(svc)) {
      return dispatchCompositeService(svc, stg, event);
    } else {
      BeeGlobal.showError("Unknown service type", svc);
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
        BeeGlobal.inputFields(new BeeStage(BeeService.SERVICE_GET_CLASS,
            BeeStage.STAGE_CONFIRM), "Class Info", BeeService.FIELD_CLASS_NAME,
            BeeService.FIELD_PACKAGE_LIST);
        ok = true;
      } else if (stg.equals(BeeStage.STAGE_CONFIRM)) {
        String cls = BeeGlobal.getFieldValue(BeeService.FIELD_CLASS_NAME);
        String pck = BeeGlobal.getFieldValue(BeeService.FIELD_PACKAGE_LIST);

        if (BeeUtils.isEmpty(cls)) {
          BeeGlobal.showError("Class name not specified");
        } else if (cls.trim().length() < 2) {
          BeeGlobal.showError("Class name", cls, "too short");
        } else {
          BeeGlobal.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(
              BeeService.SERVICE_CLASS_INFO,
              BeeXml.createString(BeeService.XML_TAG_DATA,
                  BeeService.FIELD_CLASS_NAME, cls,
                  BeeService.FIELD_PACKAGE_LIST, pck));
          ok = true;
        }
      } else {
        BeeGlobal.showError("Unknown composite service stage", svc, stg);
      }
    } else if (svc.equals(BeeService.SERVICE_GET_XML)) {
      if (stg.equals(BeeStage.STAGE_GET_PARAMETERS)) {
        BeeGlobal.inputFields(new BeeStage(BeeService.SERVICE_GET_XML,
            BeeStage.STAGE_CONFIRM), "Xml Info", BeeService.FIELD_XML_FILE);
        ok = true;
      } else if (stg.equals(BeeStage.STAGE_CONFIRM)) {
        String fnm = BeeGlobal.getFieldValue(BeeService.FIELD_XML_FILE);
        if (BeeUtils.isEmpty(fnm)) {
          BeeGlobal.showError("File name not specified");
        } else {
          BeeGlobal.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(
              BeeService.SERVICE_XML_INFO,
              BeeXml.createString(BeeService.XML_TAG_DATA,
                  BeeService.FIELD_XML_FILE, fnm));
          ok = true;
        }
      } else {
        BeeGlobal.showError("Unknown composite service stage", svc, stg);
      }
    } else if (svc.equals(BeeService.SERVICE_GET_DATA)) {
      if (stg.equals(BeeStage.STAGE_GET_PARAMETERS)) {
        BeeGlobal.inputFields(new BeeStage(BeeService.SERVICE_GET_DATA,
            BeeStage.STAGE_CONFIRM), "Jdbc Test", BeeService.FIELD_JDBC_QUERY,
            BeeService.FIELD_CONNECTION_AUTO_COMMIT,
            BeeService.FIELD_CONNECTION_HOLDABILITY,
            BeeService.FIELD_CONNECTION_READ_ONLY,
            BeeService.FIELD_CONNECTION_TRANSACTION_ISOLATION,
            BeeService.FIELD_STATEMENT_CURSOR_NAME,
            BeeService.FIELD_STATEMENT_ESCAPE_PROCESSING,
            BeeService.FIELD_STATEMENT_FETCH_DIRECTION,
            BeeService.FIELD_STATEMENT_FETCH_SIZE,
            BeeService.FIELD_STATEMENT_MAX_FIELD_SIZE,
            BeeService.FIELD_STATEMENT_MAX_ROWS,
            BeeService.FIELD_STATEMENT_POOLABLE,
            BeeService.FIELD_STATEMENT_QUERY_TIMEOUT,
            BeeService.FIELD_STATEMENT_RS_TYPE,
            BeeService.FIELD_STATEMENT_RS_CONCURRENCY,
            BeeService.FIELD_STATEMENT_RS_HOLDABILITY,
            BeeService.FIELD_RESULT_SET_FETCH_DIRECTION,
            BeeService.FIELD_RESULT_SET_FETCH_SIZE,
            BeeService.FIELD_JDBC_RETURN);
        ok = true;
      } else if (stg.equals(BeeStage.STAGE_CONFIRM)) {
        String sql = BeeGlobal.getFieldValue(BeeService.FIELD_JDBC_QUERY);
        if (BeeUtils.isEmpty(sql)) {
          BeeGlobal.showError("Query not specified");
        } else {
          BeeGlobal.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(
              BeeService.SERVICE_DB_JDBC,
              BeeXml.fromFields(BeeService.XML_TAG_DATA,
                  BeeService.FIELD_JDBC_QUERY,
                  BeeService.FIELD_CONNECTION_AUTO_COMMIT,
                  BeeService.FIELD_CONNECTION_HOLDABILITY,
                  BeeService.FIELD_CONNECTION_READ_ONLY,
                  BeeService.FIELD_CONNECTION_TRANSACTION_ISOLATION,
                  BeeService.FIELD_STATEMENT_CURSOR_NAME,
                  BeeService.FIELD_STATEMENT_ESCAPE_PROCESSING,
                  BeeService.FIELD_STATEMENT_FETCH_DIRECTION,
                  BeeService.FIELD_STATEMENT_FETCH_SIZE,
                  BeeService.FIELD_STATEMENT_MAX_FIELD_SIZE,
                  BeeService.FIELD_STATEMENT_MAX_ROWS,
                  BeeService.FIELD_STATEMENT_POOLABLE,
                  BeeService.FIELD_STATEMENT_QUERY_TIMEOUT,
                  BeeService.FIELD_STATEMENT_RS_TYPE,
                  BeeService.FIELD_STATEMENT_RS_CONCURRENCY,
                  BeeService.FIELD_STATEMENT_RS_HOLDABILITY,
                  BeeService.FIELD_RESULT_SET_FETCH_DIRECTION,
                  BeeService.FIELD_RESULT_SET_FETCH_SIZE,
                  BeeService.FIELD_JDBC_RETURN));
          ok = true;
        }
      } else {
        BeeGlobal.showError("Unknown composite service stage", svc, stg);
      }
    } else if (svc.startsWith("comp_ui_")) {
      String svcId = CompositeService.extractServiceId(svc);

      if (BeeUtils.isEmpty(svcId)) {
        svcId = BeeUtils.createUniqueName("svc");
        BeeGlobal.registerService(svcId, CompositeService.extractService(svc));
      }
      CompositeService service = BeeGlobal.getService(svcId);
      service.doService(event);
    } else {
      BeeGlobal.showError("Unknown composite service", svc, stg);
    }

    return ok;
  }

  private boolean dispatchUiService(String svc, GwtEvent<?> event) {
    if (svc.equals(BeeService.SERVICE_CLOSE_DIALOG)) {
      return BeeGlobal.closeDialog(event);
    } else if (svc.equals(BeeService.SERVICE_CONFIRM_DIALOG)) {
      return BeeGlobal.closeDialog(event);
    } else if (svc.equals(BeeService.SERVICE_CANCEL_DIALOG)) {
      return BeeGlobal.closeDialog(event);
    } else if (svc.equals(BeeService.SERVICE_REFRESH_MENU)) {
      return BeeKeeper.getMenu().drawMenu();
    } else {
      BeeGlobal.showError("Unknown UI service", svc);
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
