package com.butent.bee.egg.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.utils.BeeXml;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.BeeType;
import com.butent.bee.egg.shared.BeeWidget;
import com.butent.bee.egg.shared.ui.UiComponent;
import com.butent.bee.egg.shared.utils.BeeUtils;

class FormService extends CompositeService {

  private enum Stages {
    REQUEST_FORM_LIST, CHOOSE_FORM, REQUEST_FORM, SHOW_FORM
  }

  private Stages stage = null;

  protected FormService() {
  }

  protected FormService(String serviceId) {
    super(serviceId);
    nextStage();
  }

  @Override
  protected CompositeService create(String svcId) {
    return new FormService(svcId);
  }

  @Override
  protected boolean doStage(Object... params) {
    Assert.notNull(stage);
    boolean ok = true;
    String fld = "form_name";

    switch (stage) {
      case REQUEST_FORM_LIST:
        BeeKeeper.getRpc().makeGetRequest(adoptService("rpc_ui_form_list"));
        break;

      case CHOOSE_FORM:
        JsArrayString arr = (JsArrayString) params[0];
        int cc = (Integer) params[1];

        String[] lst = new String[arr.length() - cc];
        for (int i = cc; i < arr.length(); i++) {
          lst[i - 1] = arr.get(i);
        }
        if (!BeeGlobal.isField(fld)) {
          BeeGlobal.createField(fld, "Form name", BeeType.TYPE_STRING,
              lst[0], BeeWidget.LIST, lst);
        }

        BeeGlobal.inputFields(new BeeStage(adoptService("comp_ui_form"),
            BeeStage.STAGE_CONFIRM), "Load form", fld);
        break;

      case REQUEST_FORM:
        GwtEvent<?> event = (GwtEvent<?>) params[0];

        String fName = BeeGlobal.getFieldValue(fld);

        if (BeeUtils.isEmpty(fName)) {
          BeeGlobal.showError("Form name not specified");
          ok = false;
        } else {
          BeeGlobal.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(adoptService("rpc_ui_form"),
              BeeXml.createString(BeeService.XML_TAG_DATA, fld, fName));
        }
        break;

      case SHOW_FORM:
        JsArrayString fArr = (JsArrayString) params[0];
        UiComponent c = UiComponent.restore(fArr.get(0));

        showForm(c);
        BeeKeeper.getUi().updateActivePanel((Panel) c.createInstance(), true);
        break;

      default:
        BeeGlobal.showError("Unhandled stage: " + stage);
        unregister();
        ok = false;
        break;
    }

    if (ok) {
      nextStage();
    }
    return ok;
  }

  private void nextStage() {
    int x = 0;

    if (!BeeUtils.isEmpty(stage)) {
      x = stage.ordinal() + 1;
    }

    if (x < Stages.values().length) {
      stage = Stages.values()[x];
    } else {
      unregister();
    }
  }

  private void showForm(UiComponent c) {
    Widget root = MenuService.buidComponentTree(c);
    BeeKeeper.getUi().updateMenu(root);
  }
}
