package com.butent.bee.client.ui;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.layout.Scroll;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.SimpleBoolean;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.XmlState;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class StateService extends CompositeService {

  public static final String SVC_GET_STATES = Service.DATA_SERVICE_PREFIX + "get_states";
  public static final String SVC_SAVE_STATES = Service.DATA_SERVICE_PREFIX + "save_states";
  private static final String STG_DELETE = "delete";
  private static final String STG_NEW = "new";
  private static final String STG_CANCEL = "cancel";

  private static final int NAME = 0;
  private static final int USER = 1;
  private static final int ROLE = 2;
  private static final int CHECKED = 3;

  List<Pair<XmlState, Variable[]>> lst;

  @Override
  protected boolean doStage(String stg, Object... params) {
    boolean ok = true;

    if (lst == null) {
      lst = Lists.newArrayList();
    }
    if (stg.equals(SVC_GET_STATES)) {
      BeeKeeper.getRpc().makeGetRequest(stg,
          new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              Assert.notNull(response);

              if (response.hasResponse()) {
                for (String xml : Codec.beeDeserializeCollection((String) response.getResponse())) {
                  XmlState state = XmlState.restore(xml);
                  Variable[] vars = new Variable[3];
                  vars[NAME] = new Variable(BeeType.STRING, state.name);
                  vars[USER] = new Variable("USER", BeeType.BOOLEAN,
                      BeeUtils.toString(state.userMode));
                  vars[ROLE] = new Variable("ROLE", BeeType.BOOLEAN,
                      BeeUtils.toString(state.roleMode));
                  vars[CHECKED] = new Variable(BeeType.BOOLEAN, BeeUtils.toString(state.checked));
                  lst.add(new Pair<XmlState, Variable[]>(state, vars));
                }
                refresh();

              } else {
                destroy();
              }
            }
          });
      return ok;

    } else if (stg.equals(STG_DELETE)) {
      lst.remove(BeeUtils.unbox((Integer) params[0]));
      refresh();
      return ok;

    } else if (stg.equals(STG_NEW)) {
      Variable[] vars = new Variable[3];
      vars[NAME] = new Variable(BeeType.STRING);
      vars[USER] = new Variable("USER", BeeType.BOOLEAN);
      vars[ROLE] = new Variable("ROLE", BeeType.BOOLEAN);
      vars[CHECKED] = new Variable(BeeType.BOOLEAN);
      lst.add(new Pair<XmlState, Variable[]>(new XmlState(), vars));
      refresh();
      return ok;

    } else if (stg.equals(SVC_SAVE_STATES)) {
      List<XmlState> states = Lists.newArrayList();

      for (Pair<XmlState, Variable[]> pair : lst) {
        XmlState state = pair.getA();
        Variable[] vars = pair.getB();
        state.name = vars[NAME].getValue();
        state.userMode = vars[USER].getBoolean();
        state.roleMode = vars[ROLE].getBoolean();
        state.checked = vars[CHECKED].getBoolean();
        states.add(state);
      }
      BeeKeeper.getRpc().sendText(stg, Codec.beeSerialize(states));
      return ok;

    } else if (stg.equals(STG_CANCEL)) {
      BeeKeeper.getScreen().getActivePanel().clear();

    } else {
      ok = false;
      Global.showError("Unknown service [", getId(), "] stage:", stg);
    }
    destroy();
    return ok;
  }

  @Override
  protected CompositeService getInstance() {
    return new StateService();
  }

  private void refresh() {
    FlexTable ft = new FlexTable();
    ft.setBorderWidth(1);
    int r = 0;
    ft.setText(r, 0, "State name");
    ft.setText(r, 1, "Applies to");
    ft.setText(r, 2, "Checked by default");
    ft.alignCenter(r, 0);
    ft.alignCenter(r, 1);
    ft.alignCenter(r, 2);

    for (int i = 0; i < lst.size(); i++) {
      Variable[] vars = lst.get(i).getB();
      r++;
      FlexTable mw = new FlexTable();
      mw.setWidget(0, 0, new BeeCheckBox(vars[USER]));
      mw.setWidget(1, 0, new BeeCheckBox(vars[ROLE]));
      ft.setWidget(r, 1, mw);
      ft.setWidget(r, 2, new SimpleBoolean(vars[CHECKED]));
      ft.alignCenter(r, 1);
      ft.alignCenter(r, 2);

      if (lst.get(i).getA().isProtected()) {
        ft.setWidget(r, 0, new BeeLabel(vars[NAME].getValue()));
      } else {
        ft.setWidget(r, 0, new InputText(vars[NAME]));
        final int idx = r - 1;
        ft.setWidget(r, 3, new BeeImage(Global.getImages().editDelete(), new BeeCommand() {
          @Override
          public void execute() {
            Global.getMsgBoxen().confirm("Delete State ?", new BeeCommand() {
              @Override
              public void execute() {
                doStage(STG_DELETE, idx);
              }
            }, StyleUtils.NAME_SCARY);
          }
        }));
      }
    }
    r++;
    ft.setWidget(r, 0, new BeeButton("SAVE", getStage(SVC_SAVE_STATES)));
    ft.alignLeft(r, 0);
    ft.setWidget(r, 1, new BeeImage(Global.getImages().editAdd(), new BeeCommand() {
      @Override
      public void execute() {
        doStage(STG_NEW);
      }
    }));
    ft.alignCenter(r, 1);
    ft.setWidget(r, 2, new BeeButton("CANCEL", getStage(STG_CANCEL)));
    ft.alignRight(r, 2);

    BeeKeeper.getScreen().updateActivePanel(new Scroll(ft));
  }
}
