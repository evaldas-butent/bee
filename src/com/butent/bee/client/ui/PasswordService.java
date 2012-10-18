package com.butent.bee.client.ui;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.DialogCallback;
import com.butent.bee.client.dialog.Popup;
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
          varOld = new Variable(Global.CONSTANTS.oldPassword(), BeeType.STRING, "",
              BeeWidget.PASSWORD);
          vars.add(varOld);
        }
        varNew = new Variable(Global.CONSTANTS.newPassword(), BeeType.STRING, "",
            BeeWidget.PASSWORD);
        vars.add(varNew);
        varNew2 = new Variable(Global.CONSTANTS.repeatNewPassword(), BeeType.STRING, "",
            BeeWidget.PASSWORD);
        vars.add(varNew2);

        Global.getInpBoxen().inputVars(Global.CONSTANTS.changePassword(), vars,
            new DialogCallback() {
              @Override
              public boolean onConfirm(Popup popup) {
                return doStage(STG_SAVE_PASS);
              }
            });
        return ok;
      }

    } else if (stg.equals(STG_SAVE_PASS)) {
      ok = true;
      Assert.notNull(formView);

      if (!BeeUtils.isEmpty(oldPass)) {
        String oPass = varOld.getValue();

        if (BeeUtils.isEmpty(oPass)) {
          ok = false;
          Global.showError(Global.CONSTANTS.oldPasswordIsRequired());

        } else if (!Objects.equal(Codec.md5(oPass), oldPass)) {
          ok = false;
          Global.showError(Global.CONSTANTS.oldPasswordIsInvalid());
        }
      }
      if (ok) {
        String nPass = varNew.getValue();

        if (BeeUtils.isEmpty(nPass)) {
          ok = false;
          Global.showError(Global.CONSTANTS.newPasswordIsRequired());

        } else if (!Objects.equal(nPass, varNew2.getValue())) {
          ok = false;
          Global.showError(Global.CONSTANTS.newPasswordsDoesNotMatch());

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
