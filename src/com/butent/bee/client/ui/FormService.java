package com.butent.bee.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Panel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.BeeStage;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class FormService extends CompositeService {

  public enum Stages {
    CHOOSE_FORM, SHOW_FORM
  }

  public static final String NAME = PREFIX + "form";

  protected FormService(String... serviceId) {
    super(serviceId);
  }

  @Override
  protected CompositeService create(String svcId) {
    return new FormService(NAME, svcId);
  }

  @Override
  protected boolean doStage(String stg, Object... params) {
    Stages stage = Stages.valueOf(stg);
    boolean ok = true;
    final String fld = "form_name";

    switch (stage) {
      case CHOOSE_FORM:
        BeeKeeper.getRpc().makeGetRequest("rpc_ui_form_list",
            new ResponseCallback() {
              @Override
              public void onResponse(JsArrayString arr) {
                Assert.notNull(arr);
                Assert.parameterCount(arr.length(), 1);

                BeeRowSet res = BeeRowSet.restore(arr.get(0));
                int rc = res.getNumberOfRows();

                List<String> lst = new ArrayList<String>(rc);
                for (int i = 0; i < rc; i++) {
                  lst.add(res.getString(i, 0));
                }
                if (BeeUtils.isEmpty(lst)) {
                  Global.showError("NO FORMS");
                  destroy();
                } else {
                  if (!Global.isVar(fld)) {
                    Global.createVar(fld, "Form name", BeeType.STRING, null);
                    Global.getVar(fld).setWidget(BeeWidget.LIST);
                  }
                  Global.getVar(fld).setItems(lst);
                  Global.getVar(fld).setValue(lst.get(0));

                  Global.inputVars(new BeeStage(self(), Stages.SHOW_FORM.name()), "Load form", fld);
                }
              }
            });
        break;

      case SHOW_FORM:
        GwtEvent<?> event = (GwtEvent<?>) params[0];
        String fName = Global.getVarValue(fld);

        if (BeeUtils.isEmpty(fName)) {
          Global.showError("Form name not specified");
          ok = false;
        } else {
          Global.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest("rpc_ui_form",
              XmlUtils.createString(BeeService.XML_TAG_DATA, fld, fName),
              new ResponseCallback() {
                @Override
                public void onResponse(JsArrayString arr) {
                  Assert.notNull(arr);
                  Assert.parameterCount(arr.length(), 1);
                  String data = arr.get(0);

                  if (!BeeUtils.isEmpty(data)) {
                    UiComponent c = UiComponent.restore(data);
                    BeeKeeper.getUi().updateMenu(MenuService.buidComponentTree(c));
                    BeeKeeper.getUi().updateActivePanel((Panel) c.createInstance(), true);
                  }
                }
              });
          destroy();
        }
        break;

      default:
        Global.showError("Unhandled stage: " + stage);
        destroy();
        ok = false;
        break;
    }
    return ok;
  }

  @Override
  protected String getName() {
    return NAME;
  }
}
