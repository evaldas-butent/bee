package com.butent.bee.client.ui;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class PasswordService extends CompositeService {

  public static final String STG_GET_PASS = "get_password";
  private static final String STG_SAVE_PASS = "save_password";
  private static final String PASSWORD = "Password";

  private FormView formView = null;
  private String oldPass;
  private Variable varOld;
  private Variable varNew;
  private Variable varNew2;

  @Override
  protected boolean doStage(final String stg, Object... params) {
    boolean ok;

    if (stg.equals(STG_GET_PASS)) {
      ok = (params[0] instanceof FormView);

      if (ok) {
        formView = (FormView) params[0];
        List<Variable> vars = Lists.newArrayList();
        oldPass = formView.getActiveRow().getString(formView.getDataIndex(PASSWORD));

        if (!BeeUtils.isEmpty(oldPass)) {
          varOld = new Variable("Old password", BeeType.STRING, "", BeeWidget.PASSWORD);
          vars.add(varOld);
        }
        varNew = new Variable("New password", BeeType.STRING, "", BeeWidget.PASSWORD);
        vars.add(varNew);
        varNew2 = new Variable("Repeat new password", BeeType.STRING, "", BeeWidget.PASSWORD);
        vars.add(varNew2);

        Global.inputVars(getStage(STG_SAVE_PASS), "Change password", vars.toArray(new Variable[0]));
        return ok;
      }

    } else if (stg.equals(STG_SAVE_PASS)) {
      ok = true;
      Assert.notNull(formView);
      Global.closeDialog((Widget) params[0]);

      if (!BeeUtils.isEmpty(oldPass)) {
        String oPass = varOld.getValue();

        if (BeeUtils.isEmpty(oPass)) {
          ok = false;
          Global.showError("Old password is required");

        } else if (!BeeUtils.equals(Codec.md5(oPass), oldPass)) {
          ok = false;
          Global.showError("Old password is invalid");
        }
      }
      if (ok) {
        String nPass = varNew.getValue();

        if (BeeUtils.isEmpty(nPass)) {
          ok = false;
          Global.showError("New password is required");

        } else if (!BeeUtils.equals(nPass, varNew2.getValue())) {
          ok = false;
          Global.showError("New passwords doesn't match");

        } else {
          formView.updateCell(PASSWORD, Codec.md5(nPass));
        }
      }

    } else {
      ok = false;
      Global.showError("Unknown service [", name(), "] stage:", stg);
    }
    destroy();
    return ok;
  }

  @Override
  protected CompositeService getInstance() {
    return new PasswordService();
  }
}
