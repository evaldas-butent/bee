package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.LocalProvider;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParametersHandler extends AbstractGridCallback {

  public static class ParameterFormHandler extends AbstractFormCallback {
    @Override
    public FormCallback getInstance() {
      return new ParameterFormHandler();
    }
  }

  private static final String MODULE = "Module";
  private static final String NAME = "Name";
  private static final String TYPE = "Type";
  private static final String VALUE = "Value";
  private static final String DESCRIPTION = "Description";

  private static final List<BeeColumn> columns = Lists.newArrayList(
      new BeeColumn(ValueType.TEXT, MODULE, false),
      new BeeColumn(ValueType.TEXT, NAME, false),
      new BeeColumn(ValueType.TEXT, TYPE, false),
      new BeeColumn(ValueType.TEXT, VALUE, true),
      new BeeColumn(ValueType.TEXT, DESCRIPTION, true));

  private final String module;
  private LocalProvider provider = null;
  private Map<String, BeeParameter> params = Maps.newLinkedHashMap();

  private Long cnt = 0L;
  private Map<Long, String> ref = Maps.newHashMap();

  public ParametersHandler(String module) {
    Assert.notEmpty(module);
    this.module = module;
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    boolean ok;

    switch (action) {
      case REFRESH:
        requery();
        ok = false;
        break;

      default:
        ok = super.beforeAction(action, presenter);
    }
    return ok;
  }

  @Override
  public int beforeDeleteRow(GridPresenter presenter, final IsRow row, boolean confirm) {
    if (confirm) {
      Global.confirm(ref.get(row.getId()), "Remove parameter?", new BeeCommand() {
        @Override
        public void execute() {
          delete(row.getId());
        }
      });
    } else {
      delete(row.getId());
    }
    return GridCallback.DELETE_CANCEL;
  }

  @Override
  public int beforeDeleteRows(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows) {

    int c = selectedRows.size();
    Long[] ids = new Long[c];
    int i = 0;

    for (RowInfo rowInfo : selectedRows) {
      ids[i++] = rowInfo.getId();
    }
    delete(ids);

    return GridCallback.DELETE_CANCEL;
  }

  @Override
  public Pair<String, String> getDeleteRowsMessage(int selectedRows) {
    return Pair.of("Remove current parameter", BeeUtils.concat(1, "Remove", selectedRows,
        "selected parameters"));
  }

  @Override
  public BeeRowSet getInitialRowSet() {
    return new BeeRowSet(columns);
  }

  @Override
  public GridCallback getInstance() {
    return new ParametersHandler(module);
  }

  @Override
  public boolean onPrepareForInsert(GridView gridView, List<? extends IsColumn> cols,
      List<String> values) {

    Map<String, String> data = Maps.newHashMap();

    for (int i = 0; i < cols.size(); i++) {
      data.put(cols.get(i).getId(), values.get(i));
    }
    if (params.containsKey(data.get(NAME))) {
      gridView.notifySevere("Dublicate parameter name:", data.get(NAME));
    } else {
      update(gridView, 0, new BeeParameter(module,
          data.get(NAME), data.get(TYPE), data.get(VALUE), data.get(DESCRIPTION)));
    }
    return false;
  }

  @Override
  public boolean onPrepareForUpdate(GridView gridView, long id, long version,
      List<? extends IsColumn> cols, List<String> oldValues, List<String> newValues) {

    BeeParameter prm = params.get(gridView.getActiveRow().getString(id(NAME)));

    Map<String, String> data = Maps.newHashMap();

    for (int i = 0; i < cols.size(); i++) {
      data.put(cols.get(i).getId(), newValues.get(i));
    }
    update(gridView, id, new BeeParameter(module,
        BeeUtils.notEmpty(data.get(NAME), prm.getName()),
        BeeUtils.notEmpty(data.get(TYPE), prm.getType()),
        BeeUtils.notEmpty(data.get(VALUE), prm.getValue()),
        BeeUtils.notEmpty(data.get(DESCRIPTION), prm.getDescription())));
    return false;
  }

  @Override
  public void onShow(GridPresenter presenter) {
    if (presenter != null && presenter.getDataProvider() instanceof LocalProvider) {
      provider = (LocalProvider) presenter.getDataProvider();
      requery();
    }
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    newRow.setValue(id(MODULE), module);
    newRow.setValue(id(TYPE), oldRow != null ? oldRow.getString(id(TYPE)) : null);
    return true;
  }

  private void delete(Long... ids) {
    final Set<String> prm = Sets.newHashSet();

    for (Long id : ids) {
      prm.add(ref.get(id));
    }
    ParameterList args =
        CommonsEventHandler.createArgs(CommonsConstants.SVC_REMOVE_PARAMETERS);
    args.addDataItem(CommonsConstants.VAR_PARAMETERS_MODULE, module);
    args.addDataItem(CommonsConstants.VAR_PARAMETERS, Codec.beeSerialize(prm));

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          Global.showError((Object[]) response.getErrors());

        } else if (response.hasResponse()) {
          for (String s : Codec.beeDeserializeCollection((String) response.getResponse())) {
            BeeParameter def = BeeParameter.restore(s);
            params.put(def.getName(), def);
            prm.remove(def.getName());
          }
          for (String p : prm) {
            params.remove(p);
          }
          refresh();

        } else {
          Global.showError("Unknown response");
        }
      }
    });
  }

  private CellGrid getGrid() {
    return getGridPresenter().getGridView().getGrid();
  }

  private int id(String colName) {
    return DataUtils.getColumnIndex(colName, columns);
  }

  private void refresh() {
    if (provider == null) {
      return;
    }
    provider.clear();
    cnt = 0L;
    ref.clear();

    for (BeeParameter prm : params.values()) {
      String[] values = new String[columns.size()];
      values[id(MODULE)] = prm.getModule();
      values[id(NAME)] = prm.getName();
      values[id(TYPE)] = prm.getType();
      values[id(VALUE)] = prm.getValue();
      values[id(DESCRIPTION)] = prm.getDescription();

      ref.put(++cnt, prm.getName());
      BeeRow row = new BeeRow(cnt, values);
      provider.addRow(row);
      getGrid().insertRow(row); // TODO provider must do it
    }
  }

  private void requery() {
    ParameterList args = CommonsEventHandler.createArgs(CommonsConstants.SVC_GET_PARAMETERS);
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
          refresh();

        } else {
          Global.showError("Unknown response");
        }
      }
    });
  }

  private void update(final GridView gridView, final long id, BeeParameter parameter) {
    ParameterList args = CommonsEventHandler.createArgs(CommonsConstants.SVC_SAVE_PARAMETERS);
    args.addDataItem(CommonsConstants.VAR_PARAMETERS, Codec.beeSerialize(parameter));

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          Global.showError((Object[]) response.getErrors());

        } else if (response.hasResponse(BeeParameter.class)) {
          BeeParameter prm = BeeParameter.restore((String) response.getResponse());
          boolean newMode = BeeUtils.isEmpty(id);

          String[] values = new String[columns.size()];
          values[id(MODULE)] = prm.getModule();
          values[id(NAME)] = prm.getName();
          values[id(TYPE)] = prm.getType();
          values[id(VALUE)] = prm.getValue();
          values[id(DESCRIPTION)] = prm.getDescription();

          params.put(prm.getName(), prm);

          if (newMode) {
            ref.put(++cnt, prm.getName());
            BeeRow newRow = new BeeRow(cnt, values);
            provider.addRow(newRow);
            gridView.finishNewRow(newRow);
          } else {
            gridView.getGrid().onRowUpdate(new RowUpdateEvent(null, new BeeRow(id, values)));
          }
        } else {
          Global.showError("Unknown response");
        }
      }
    });
  }
}
