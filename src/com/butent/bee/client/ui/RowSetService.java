package com.butent.bee.client.ui;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.tree.BeeTree;
import com.butent.bee.client.tree.BeeTreeItem;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements asynchronous management of row sets - chunks of data table for representation to a
 * user.
 */
public class RowSetService extends CompositeService {

  /**
   * Contains a list of stages for row set service.
   */
  public static enum Stages {
    CHOOSE_TABLE, REQUEST_TABLE,
    INSERT, SAVE, CANCEL,
    FILTER_STATES, SHOW_STATE, REQUEST_STATETABLE
  }

  public static final String NAME = PREFIX + "rowset";

  private String tbl = Service.VAR_VIEW_NAME;
  private String limit = Service.VAR_VIEW_LIMIT;
  private String offset = Service.VAR_VIEW_OFFSET;
  private String stt = Service.VAR_VIEW_STATES;

  private BeeRowSet rs;

  protected RowSetService(String... svcId) {
    super(svcId);
  }

  @Override
  protected CompositeService create(String svcId) {
    return new RowSetService(NAME, svcId);
  }

  @Override
  protected boolean doStage(String stg, Object... params) {
    Stages stage = Stages.valueOf(stg);
    boolean ok = true;

    if (!Global.isVar(tbl)) {
      Global.createVar(tbl, "Choose table", BeeType.STRING, null, BeeWidget.LIST);
    }
    if (!Global.isVar(limit)) {
      Global.createVar(limit, "Limit", BeeType.INT, null, BeeWidget.TEXT);
    }
    if (!Global.isVar(offset)) {
      Global.createVar(offset, "Offset", BeeType.INT, null, BeeWidget.TEXT);
    }
    if (!Global.isVar(stt)) {
      Global.createVar(stt, "Choose state", BeeType.STRING, null, BeeWidget.LIST);
    }
    switch (stage) {
      case CHOOSE_TABLE:
        BeeKeeper.getRpc().makeGetRequest(Service.GET_TABLE_LIST,
            new ResponseCallback() {
              @Override
              public void onResponse(JsArrayString arr) {
                Assert.notNull(arr);
                Assert.parameterCount(arr.length(), 1);

                List<String> lst = Lists.newArrayList(Codec.beeDeserialize(arr.get(0)));

                if (BeeUtils.isEmpty(lst)) {
                  Global.showError("NO TABLES");
                  destroy();
                } else {
                  Global.getVar(tbl).setItems(lst);
                  Global.getVar(tbl).setValue(lst.get(0));

                  Global.inputVars(new Stage(self(), Stages.REQUEST_TABLE.name()), "ALL TABLES",
                      tbl, limit, offset);
                }
              }
            });
        break;

      case REQUEST_TABLE:
        GwtEvent<?> event = (GwtEvent<?>) params[0];
        String table = Global.getVarValue(tbl);

        if (BeeUtils.isEmpty(table)) {
          Global.showError("Table name not specified");
          ok = false;
        } else {
          Global.closeDialog(event);
          Queries.getRowSet(table, null, null, Global.getVarInt(limit), Global.getVarInt(offset),
              Global.getVarValue(stt), CachingPolicy.NONE,
              new Queries.RowSetCallback() {
                public void onResponse(BeeRowSet rowSet) {
                  rs = rowSet;
                  refresh();
                }
              });
          Global.getVar(tbl).setValue("");
          Global.getVar(stt).setValue("");
        }
        break;

      case SAVE:
        BeeRowSet upd = rs.getChanges();

        if (BeeUtils.isEmpty(upd)) {
          BeeKeeper.getLog().info("Nothing to update");
        } else {
          BeeKeeper.getRpc().makePostRequest(Service.COMMIT, ContentType.BINARY, upd.serialize(),
              new ResponseCallback() {
                @Override
                public void onResponse(JsArrayString arr) {
                  Assert.notNull(arr);
                  Assert.parameterCount(arr.length(), 1);
                  String data = arr.get(0);

                  if (BeeUtils.isEmpty(data)) {
                    rs.rollback();
                  } else {
                    rs.commit(BeeRowSet.restore(data));
                  }
                  refresh();
                }
              });
        }
        break;

      case INSERT:
        rs.addEmptyRow();
        refresh();
        break;

      case CANCEL:
        rs.rollback();
        refresh();
        break;

      case FILTER_STATES:
        table = rs.getViewName();
        Global.getVar(tbl).setValue(table);

        if (BeeUtils.isEmpty(table)) {
          Global.showError("Table name not specified");
          ok = false;
        } else {
          BeeKeeper.getRpc().makePostRequest(Service.GET_STATES,
              XmlUtils.createString(Service.XML_TAG_DATA, tbl, table),
              new ResponseCallback() {
                @Override
                public void onResponse(JsArrayString arr) {
                  Assert.notNull(arr);
                  Assert.parameterCount(arr.length(), 1);

                  List<String> res = Lists.newArrayList(Codec.beeDeserialize(arr.get(0)));
                  int rc = res.size();
                  List<String> lst = new ArrayList<String>();
                  int x = 1;

                  for (int i = 0; i < rc; i++) {
                    String current = res.get(i);
                    lst.add(current);

                    for (int j = x; j < rc; j++) {
                      for (int k = j; k < rc; k++) {
                        lst.add(current + " " + res.get(k));
                      }
                      current += " " + res.get(j);
                    }
                    x++;
                  }
                  if (BeeUtils.isEmpty(lst)) {
                    Global.showError("TABLE " + Global.getVarValue(tbl) + " HAS NO STATES");
                  } else {
                    Global.getVar(stt).setItems(lst);
                    Global.getVar(stt).setValue(lst.get(0));
                    Global.inputVars(new Stage(self(), Stages.REQUEST_TABLE.name()),
                        Global.getVarValue(tbl), stt);
                  }
                }
              });
        }
        break;

      case SHOW_STATE:
        table = rs.getViewName();
        Global.getVar(tbl).setValue(table);

        BeeKeeper.getRpc().makePostRequest(Service.GET_STATES,
              XmlUtils.createString(Service.XML_TAG_DATA, tbl, table),
              new ResponseCallback() {
                @Override
                public void onResponse(JsArrayString arr) {
                  Assert.notNull(arr);
                  Assert.parameterCount(arr.length(), 1);

                  List<String> lst = Lists.newArrayList(Codec.beeDeserialize(arr.get(0)));

                  if (BeeUtils.isEmpty(lst)) {
                    Global.showError("NO STATES");
                  } else {
                    Global.getVar(stt).setItems(lst);
                    Global.getVar(stt).setValue(lst.get(0));
                    Global.inputVars(new Stage(self(), Stages.REQUEST_STATETABLE.name()),
                        BeeUtils.ifString(Global.getVarValue(tbl), "ALL TABLES"), stt);
                  }
                }
              });
        break;

      case REQUEST_STATETABLE:
        event = (GwtEvent<?>) params[0];
        String states = Global.getVarValue(stt);

        if (BeeUtils.isEmpty(states)) {
          Global.showError("State name not specified");
          ok = false;
        } else {
          Global.closeDialog(event);
          BeeKeeper.getRpc().makePostRequest(Service.GET_STATE_TABLE,
              XmlUtils.createString(Service.XML_TAG_DATA
                  , tbl, Global.getVarValue(tbl)
                  , stt, states),
              new ResponseCallback() {
                @Override
                public void onResponse(JsArrayString arr) {
                  Assert.notNull(arr);
                  Assert.parameterCount(arr.length(), 1);
                  String data = arr.get(0);

                  if (!BeeUtils.isEmpty(data)) {
                    rs = BeeRowSet.restore(data);
                    refresh();
                  }
                }
              });
          Global.getVar(tbl).setValue("");
          Global.getVar(stt).setValue("");
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

  private Widget getTree(BeeRowSet brs) {
    BeeTree root = new BeeTree();
    BeeTreeItem item = new BeeTreeItem("RowSet");
    root.addItem(item);
    item.addItem("ViewName: " + brs.getViewName());

    BeeTreeItem cols = new BeeTreeItem("Columns");
    for (BeeColumn col : brs.getColumnArray()) {
      BeeTreeItem c = new BeeTreeItem(col.getId());
      c.addItem("Type: (" + col.getSqlType() + ") " + col.getType());
      c.addItem("Prec: " + col.getPrecision());
      c.addItem("Scale: " + col.getScale());
      c.addItem("Nullable: " + col.getNullable());
      cols.addItem(c);
    }
    item.addItem(cols);

    BeeTreeItem rows = new BeeTreeItem("Data");
    for (int i = 0; i < brs.getNumberOfRows(); i++) {
      BeeTreeItem r = new BeeTreeItem("Row" + brs.getRow(i).getId());

      for (int j = 0; j < brs.getNumberOfColumns(); j++) {
        r.addItem(brs.getColumnLabel(j) + ": " + brs.getString(i, j));
      }
      rows.addItem(r);
    }
    item.addItem(rows);

    return root;
  }

  private void refresh() {
    BeeKeeper.getUi().updateMenu(getTree(rs));

    if (rs.isEmpty()) {
      BeeKeeper.getUi().updateActivePanel(new BeeLabel("RowSet is empty"));
    } else {
      BeeKeeper.getUi().showGrid(rs);
    }
    HasWidgets panel = BeeKeeper.getUi().getActivePanel();

    FlowPanel buttons = new FlowPanel();
    buttons.add(new BeeButton("NEW", self(), Stages.INSERT.name()));
    buttons.add(new BeeButton("SAVE", self(), Stages.SAVE.name()));
    buttons.add(new BeeButton("CANCEL", self(), Stages.CANCEL.name()));
    buttons.add(new BeeButton("FILTER STATES", self(), Stages.FILTER_STATES.name()));
    buttons.add(new BeeButton("SHOW STATE", self(), Stages.SHOW_STATE.name()));

    Split root = new Split();
    root.addNorth(buttons, 25);
    root.add(panel.iterator().next(), ScrollBars.BOTH);
    BeeKeeper.getUi().updateActiveQuietly(root, ScrollBars.BOTH);
  }
}
