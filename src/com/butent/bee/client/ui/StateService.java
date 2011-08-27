package com.butent.bee.client.ui;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeImage;
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
  private static final int MODE = 1;
  private static final int CHECKED = 2;

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
                  vars[MODE] = new Variable(BeeType.STRING, state.mode);
                  vars[MODE].setItems(Lists.newArrayList("USER", "ROLE", "USER and ROLE"));
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
      vars[MODE] = new Variable(BeeType.STRING, "ROLE");
      vars[MODE].setItems(Lists.newArrayList("USER", "ROLE", "USER and ROLE"));
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
        state.mode = vars[MODE].getValue();
        state.checked = vars[CHECKED].getBoolean();
        states.add(state);
      }
      ParameterList args = BeeKeeper.getRpc().createParameters(stg);
      args.addPositionalData(Codec.beeSerialize(states));

      BeeKeeper.getRpc().makeGetRequest(args);
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
    ft.setText(r, NAME, "State name");
    ft.setText(r, MODE, "Applies to");
    ft.setText(r, CHECKED, "Checked by default");
    ft.alignCenter(r, NAME);
    ft.alignCenter(r, MODE);
    ft.alignCenter(r, CHECKED);

    for (int i = 0; i < lst.size(); i++) {
      Variable[] vars = lst.get(i).getB();
      r++;
      ft.setWidget(r, NAME, new InputText(vars[NAME]));
      ft.setWidget(r, MODE, new RadioGroup(vars[MODE], true));
      ft.setWidget(r, CHECKED, new SimpleBoolean(vars[CHECKED]));
      ft.alignCenter(r, NAME);
      ft.alignCenter(r, MODE);
      ft.alignCenter(r, CHECKED);
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
    r++;
    ft.setWidget(r, NAME, new BeeButton("SAVE", getStage(SVC_SAVE_STATES)));
    ft.alignLeft(r, NAME);
    ft.setWidget(r, MODE, new BeeImage(Global.getImages().editAdd(), new BeeCommand() {
      @Override
      public void execute() {
        doStage(STG_NEW);
      }
    }));
    ft.alignCenter(r, MODE);
    ft.setWidget(r, CHECKED, new BeeButton("CANCEL", getStage(STG_CANCEL)));
    ft.alignRight(r, CHECKED);

    BeeKeeper.getScreen().updateActivePanel(ft);
  }
}
