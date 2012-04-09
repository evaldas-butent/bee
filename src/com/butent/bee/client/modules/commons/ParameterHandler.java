package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;

public class ParameterHandler extends AbstractFormCallback {
  private final String module;

  private HasWidgets container = null;
  private FlexTable flex = null;
  private Map<String, BeeParameter> params = Maps.newLinkedHashMap();

  public ParameterHandler(String module) {
    Assert.notEmpty(module);
    this.module = module;
  }

  @Override
  public void afterCreateWidget(String name, Widget widget) {
    if (BeeUtils.same(name, "Container") && widget instanceof HasWidgets) {
      container = (HasWidgets) widget;
      requery();
    }
  }

  @Override
  public boolean beforeAction(Action action, FormPresenter presenter) {
    if (action == Action.REFRESH) {
      requery();

    } else if (action == Action.SAVE && flex != null) {
      List<BeeParameter> upd = Lists.newArrayList();

      for (int i = 1; i < flex.getRowCount(); i++) {
        String name = flex.getText(i, 1);
        String value = ((InputText) flex.getWidget(i, 3)).getValue();
        BeeParameter prm = params.get(name);

        if (!BeeUtils.equals(value, prm.getValue())) {
          prm.setValue(value);
          upd.add(prm);
        }
      }
      if (BeeUtils.isEmpty(upd)) {
        presenter.getView().getContent().notifyWarning("No changes");
      } else {
        ParameterList args = CommonEventHandler.createArgs(CommonsConstants.SVC_SAVE_PARAMETERS);
        args.addDataItem(CommonsConstants.VAR_PARAMETERS_CHANGES, Codec.beeSerialize(upd));

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);

            if (response.hasErrors()) {
              Global.showError((Object[]) response.getErrors());

            } else if (response.hasResponse(Boolean.class)) {
              if (!BeeUtils.toBoolean((String) response.getResponse())) {
                requery();
              }
            } else {
              Global.showError("Unknown response");
            }
          }
        });
      }
      return false;
    }
    return true;
  }

  @Override
  public FormCallback getInstance() {
    return new ParameterHandler(module);
  }

  private void requery() {
    if (container == null) {
      return;
    }
    ParameterList args = CommonEventHandler.createArgs(CommonsConstants.SVC_GET_PARAMETERS);
    args.addDataItem(CommonsConstants.VAR_PARAMETERS_MODULE, module);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          Global.showError((Object[]) response.getErrors());
        } else if (response.hasResponse()) {
          params.clear();

          for (String prm : Codec.beeDeserializeCollection((String) response.getResponse())) {
            BeeParameter p = BeeParameter.restore(prm);
            params.put(p.getName(), p);
          }
          if (flex == null) {
            container.clear();
            flex = new FlexTable();
            flex.setBorderWidth(1);
            container.add(flex);
          } else {
            flex.clear();
          }
          flex.setText(0, 0, "Modulis");
          flex.setText(0, 1, "Parametras");
          flex.setText(0, 2, "Tipas");
          flex.setText(0, 3, "Reikšmė");
          flex.setText(0, 4, "Aprašymas");
          int i = 0;

          for (BeeParameter prm : params.values()) {
            flex.setText(++i, 0, prm.getModule());
            flex.setText(i, 1, prm.getName());
            flex.setText(i, 2, prm.getType());
            InputText w = new InputText();
            w.setValue(prm.getValue());
            flex.setWidget(i, 3, w);
            flex.setText(i, 4, prm.getDescription());
          }
        } else {
          Global.showError("Unknown response");
        }
      }
    });
  }
}
