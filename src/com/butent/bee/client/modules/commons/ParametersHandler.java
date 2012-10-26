package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.LocalProvider;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

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
  private static final String DESCRIPTION = "Description";
  private static final String USER_MODE = "UserMode";
  private static final String VALUE = "Value";
  private static final String USER_VALUE = "UserValue";

  private static final List<BeeColumn> columns = Lists.newArrayList(
      new BeeColumn(ValueType.TEXT, MODULE, false),
      new BeeColumn(ValueType.TEXT, NAME, false),
      new BeeColumn(ValueType.TEXT, TYPE, false),
      new BeeColumn(ValueType.TEXT, DESCRIPTION, true),
      new BeeColumn(ValueType.TEXT, USER_MODE, true),
      new BeeColumn(ValueType.TEXT, VALUE, true),
      new BeeColumn(ValueType.TEXT, USER_VALUE, true));

  private final String module;
  private LocalProvider provider = null;
  private final Map<String, BeeParameter> params = Maps.newLinkedHashMap();

  private Long cnt = 0L;
  private final Map<Long, String> ref = Maps.newHashMap();

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
  public int beforeDeleteRow(GridPresenter presenter, IsRow row) {
    delete(row.getId());
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
  public BeeRowSet getInitialRowSet() {
    return new BeeRowSet(columns);
  }

  @Override
  public GridCallback getInstance() {
    return new ParametersHandler(module);
  }

  @Override
  public boolean onReadyForInsert(GridView gridView, ReadyForInsertEvent event) {

    Map<String, String> data = Maps.newHashMap();

    for (int i = 0; i < event.getColumns().size(); i++) {
      data.put(event.getColumns().get(i).getId(), event.getValues().get(i));
    }

    if (params.containsKey(data.get(NAME))) {
      String[] msg = new String[] {"Dublicate parameter name:", data.get(NAME)};
      if (event.getCallback() == null) {
        gridView.notifySevere(msg);
      } else {
        event.getCallback().onFailure(msg);
      }

    } else {
      update(gridView, 0, new BeeParameter(module, data.get(NAME),
          NameUtils.getEnumByName(ParameterType.class, data.get(TYPE)),
          data.get(DESCRIPTION), BeeUtils.toBoolean(data.get(USER_MODE)), data.get(VALUE)),
          event.getCallback());
    }
    return false;
  }

  @Override
  public boolean onReadyForUpdate(GridView gridView, ReadyForUpdateEvent event) {
    return onSaveChanges(gridView, SaveChangesEvent.of(event));
  }

  @Override
  public boolean onSaveChanges(GridView gridView, SaveChangesEvent event) {

    String prmName = gridView.getActiveRow().getString(id(NAME));

    if (event.getColumns().size() == 1 
        && BeeUtils.same(event.getColumns().get(0).getId(), USER_VALUE)) {
      return change(gridView, event.getRowId(), prmName, event.getNewValues().get(0),
          event.getCallback());
    }
    BeeParameter prm = params.get(prmName);

    Map<String, String> data = Maps.newHashMap();

    for (int i = 0; i < event.getColumns().size(); i++) {
      data.put(event.getColumns().get(i).getId(), event.getNewValues().get(i));
    }
    if (data.containsKey(TYPE)) {
      prm.setType(NameUtils.getEnumByName(ParameterType.class, data.get(TYPE)));
    }
    if (data.containsKey(DESCRIPTION)) {
      prm.setDescription(data.get(DESCRIPTION));
    }
    if (data.containsKey(USER_MODE)) {
      prm.setUserMode(BeeUtils.toBoolean(data.get(USER_MODE)));
    }
    if (data.containsKey(VALUE)) {
      prm.setValue(data.get(VALUE));
    }

    update(gridView, event.getRowId(), prm, event.getCallback());
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

  private boolean change(final GridView gridView, final long id, final String name,
      final String value, final Callback<IsRow> callback) {

    ParameterList args = CommonsEventHandler.createArgs(CommonsConstants.SVC_SET_PARAMETER);
    args.addDataItem(CommonsConstants.VAR_PARAMETERS_MODULE, module);
    args.addDataItem(CommonsConstants.VAR_PARAMETERS, name);

    if (!BeeUtils.isEmpty(value)) {
      args.addDataItem(CommonsConstants.VAR_PARAMETER_VALUE, value);
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          if (callback == null) {
            Global.showError(response.getErrors());
          } else {
            callback.onFailure(response.getErrors());
          }

        } else if (response.hasResponse(Boolean.class)) {
          BeeParameter prm = params.get(name);

          if (prm.supportsUsers()) {
            prm.setUserValue(BeeKeeper.getUser().getUserId(), value);
          } else {
            prm.setValue(value);
            gridView.getGrid().getRowById(id).setValue(id(VALUE), value);
          }
          gridView.getGrid().onCellUpdate(new CellUpdateEvent(null, id, 0, USER_VALUE,
              id(USER_VALUE), value));
          if (callback != null) {
            callback.onSuccess(null);
          }

        } else {
          String msg = "Unknown response";
          if (callback == null) {
            Global.showError(msg);
          } else {
            callback.onFailure(msg);
          }
        }
      }
    });
    return false;
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
          Global.showError(response.getErrors());

        } else if (response.hasResponse(Boolean.class)) {
          requery();

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
      values[id(TYPE)] = prm.getType().name();
      values[id(DESCRIPTION)] = prm.getDescription();
      values[id(USER_MODE)] = BeeUtils.toString(prm.supportsUsers());
      values[id(VALUE)] = prm.getValue();
      values[id(USER_VALUE)] = prm.supportsUsers()
          ? prm.getUserValue(BeeKeeper.getUser().getUserId()) : prm.getValue();

      ref.put(++cnt, prm.getName());
      BeeRow row = new BeeRow(cnt, values);
      provider.addRow(row);
      getGrid().insertRow(row, false); // TODO provider must do it
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
          Global.showError(response.getErrors());

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

  private void update(final GridView gridView, final long id, final BeeParameter parameter,
      final Callback<IsRow> callback) {

    ParameterList args = CommonsEventHandler.createArgs(CommonsConstants.SVC_CREATE_PARAMETER);
    args.addDataItem(CommonsConstants.VAR_PARAMETERS, Codec.beeSerialize(parameter));

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          if (callback == null) {
            Global.showError(response.getErrors());
          } else {
            callback.onFailure(response.getErrors());
          }

        } else if (response.hasResponse(Boolean.class)) {
          boolean newMode = (id == DataUtils.NEW_ROW_ID);

          String[] values = new String[columns.size()];
          values[id(MODULE)] = parameter.getModule();
          values[id(NAME)] = parameter.getName();
          values[id(TYPE)] = parameter.getType().name();
          values[id(DESCRIPTION)] = parameter.getDescription();
          values[id(USER_MODE)] = BeeUtils.toString(parameter.supportsUsers());
          values[id(VALUE)] = parameter.getValue();
          values[id(USER_VALUE)] = parameter.supportsUsers()
              ? parameter.getUserValue(BeeKeeper.getUser().getUserId()) : parameter.getValue();

          params.put(parameter.getName(), parameter);

          if (newMode) {
            ref.put(++cnt, parameter.getName());
            BeeRow newRow = new BeeRow(cnt, values);
            provider.addRow(newRow);
            if (callback == null) {
              gridView.finishNewRow(newRow);
            } else {
              callback.onSuccess(newRow);
            }

          } else {
            BeeRow row = new BeeRow(id, values);
            gridView.getGrid().onRowUpdate(new RowUpdateEvent(null, row));
            if (callback != null) {
              callback.onSuccess(row);
            }
          }

        } else {
          String msg = "Unknown response";
          if (callback == null) {
            Global.showError(msg);
          } else {
            callback.onFailure(msg);
          }
        }
      }
    });
  }
}