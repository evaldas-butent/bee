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
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
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

  private static final List<BeeColumn> columns = Lists.newArrayList(
      new BeeColumn(ValueType.TEXT, "Module", false),
      new BeeColumn(ValueType.TEXT, "Name", false),
      new BeeColumn(ValueType.TEXT, "Type", false),
      new BeeColumn(ValueType.TEXT, "Value", true),
      new BeeColumn(ValueType.TEXT, "Description", true));

  private static final int moduleIndex = 0;
  private static final int nameIndex = 1;
  private static final int typeIndex = 2;
  private static final int valueIndex = 3;
  private static final int descriptionIndex = 4;

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
      case REQUERY:
        requery();
        ok = false;
        break;

      default:
        ok = super.beforeAction(action, presenter);
    }
    return ok;
  }

  @Override
  public int beforeDeleteRow(GridPresenter presenter, final IsRow row) {
    Global.confirm(ref.get(row.getId()), "Remove parameter?", new BeeCommand() {
      @Override
      public void execute() {
        delete(row.getId());
      }
    });
    return -1;
  }

  @Override
  public int beforeDeleteRows(GridPresenter presenter, IsRow activeRow,
      final Collection<RowInfo> selectedRows) {

    final int c = selectedRows.size();

    Global.confirm(BeeUtils.concat(1, "Remove", c, "parameters?"), new BeeCommand() {
      @Override
      public void execute() {
        Long[] ids = new Long[c];
        int i = 0;

        for (RowInfo rowInfo : selectedRows) {
          ids[i++] = rowInfo.getId();
        }
        delete(ids);
      }
    });
    return -1;
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
  public boolean onPrepareForInsert(GridView gridView, IsRow newRow) {
    if (params.containsKey(newRow.getString(nameIndex))) {
      gridView.notifySevere("Dublicate parameter name:", newRow.getString(nameIndex));
    } else {
      update(gridView, newRow);
    }
    return false;
  }

  @Override
  public boolean onPrepareForUpdate(GridView gridView, IsRow oldRow, IsRow newRow) {
    boolean upd = false;

    for (int i = 0; i < columns.size(); i++) {
      if (!BeeUtils.equals(oldRow.getValue(i), newRow.getValue(i))) {
        upd = true;
        break;
      }
    }
    if (upd) {
      update(gridView, newRow);
    } else {
      gridView.notifyWarning("No changes");
    }
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
    newRow.setValue(moduleIndex, module);
    newRow.setValue(typeIndex, oldRow != null ? oldRow.getString(typeIndex) : null);
    return true;
  }

  private void delete(Long... ids) {
    final Set<String> prm = Sets.newHashSet();

    for (Long id : ids) {
      prm.add(ref.get(id));
    }
    ParameterList args =
        CommonEventHandler.createArgs(CommonsConstants.SVC_REMOVE_PARAMETERS);
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
    return getGridPresenter().getView().getContent().getGrid();
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
      values[moduleIndex] = prm.getModule();
      values[nameIndex] = prm.getName();
      values[typeIndex] = prm.getType();
      values[valueIndex] = prm.getValue();
      values[descriptionIndex] = prm.getDescription();

      ref.put(++cnt, prm.getName());
      BeeRow row = new BeeRow(cnt, values);
      provider.addRow(row);
      getGrid().insertRow(row); // TODO provider must do it
    }
  }

  private void requery() {
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
          refresh();

        } else {
          Global.showError("Unknown response");
        }
      }
    });
  }

  private void update(final GridView gridView, final IsRow row) {
    ParameterList args = CommonEventHandler.createArgs(CommonsConstants.SVC_SAVE_PARAMETERS);
    args.addDataItem(CommonsConstants.VAR_PARAMETERS,
        Codec.beeSerialize(new BeeParameter(module, row.getString(nameIndex),
            row.getString(typeIndex), row.getString(valueIndex), row.getString(descriptionIndex))));

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          Global.showError((Object[]) response.getErrors());

        } else if (response.hasResponse(BeeParameter.class)) {
          BeeParameter prm = BeeParameter.restore((String) response.getResponse());
          boolean newMode = !params.containsKey(prm.getName());

          String[] values = new String[columns.size()];
          values[moduleIndex] = prm.getModule();
          values[nameIndex] = prm.getName();
          values[typeIndex] = prm.getType();
          values[valueIndex] = prm.getValue();
          values[descriptionIndex] = prm.getDescription();

          params.put(prm.getName(), prm);

          if (newMode) {
            ref.put(++cnt, prm.getName());
            BeeRow newRow = new BeeRow(cnt, values);
            provider.addRow(newRow);
            gridView.finishNewRow(newRow);
          } else {
            gridView.getGrid()
                .onRowUpdate(new RowUpdateEvent(null, new BeeRow(row.getId(), values)));
          }
        } else {
          Global.showError("Unknown response");
        }
      }
    });
  }
}
