package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.LocalProvider;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.event.logical.CloseEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ParametersGrid extends AbstractGridInterceptor {

  private static Boolean getBoolean(BeeParameter parameter) {
    return parameter.supportsUsers()
        ? parameter.getBoolean(BeeKeeper.getUser().getUserId()) : parameter.getBoolean();
  }

  private static Collection<String> getCollection(BeeParameter parameter) {
    return parameter.supportsUsers()
        ? parameter.getCollection(BeeKeeper.getUser().getUserId()) : parameter.getCollection();
  }

  private static JustDate getDate(BeeParameter parameter) {
    return parameter.supportsUsers()
        ? parameter.getDate(BeeKeeper.getUser().getUserId()) : parameter.getDate();
  }

  private static DateTime getDateTime(BeeParameter parameter) {
    return parameter.supportsUsers()
        ? parameter.getDateTime(BeeKeeper.getUser().getUserId()) : parameter.getDateTime();
  }

  private static Map<String, String> getMap(BeeParameter parameter) {
    return parameter.supportsUsers()
        ? parameter.getMap(BeeKeeper.getUser().getUserId()) : parameter.getMap();
  }

  private static Number getNumber(BeeParameter parameter) {
    return parameter.supportsUsers()
        ? parameter.getNumber(BeeKeeper.getUser().getUserId()) : parameter.getNumber();
  }

  private static String getText(BeeParameter parameter) {
    return parameter.supportsUsers()
        ? parameter.getText(BeeKeeper.getUser().getUserId()) : parameter.getText();
  }

  private static Long getTime(BeeParameter parameter) {
    return parameter.supportsUsers()
        ? parameter.getTime(BeeKeeper.getUser().getUserId()) : parameter.getTime();
  }

  private static final String NAME = "Name";
  private static final String VALUE = "Value";
  private static final String DESCRIPTION = "Description";

  private static final List<BeeColumn> columns = Lists.newArrayList(
      new BeeColumn(ValueType.TEXT, NAME, false),
      new BeeColumn(ValueType.TEXT, VALUE, true),
      new BeeColumn(ValueType.TEXT, DESCRIPTION, true));

  private final String module;
  private LocalProvider provider;
  private final List<BeeParameter> params = Lists.newArrayList();

  public ParametersGrid(String module) {
    Assert.notEmpty(module);
    this.module = module;
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    if (action != Action.REFRESH) {
      return super.beforeAction(action, presenter);
    }
    requery();
    return false;
  }

  @Override
  public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
    return new BeeRowSet(columns);
  }

  @Override
  public GridInterceptor getInstance() {
    return new ParametersGrid(module);
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    event.consume();
    final String column = event.getColumnId();

    if (!BeeUtils.same(column, VALUE) || event.isReadOnly()) {
      return;
    }
    final CellGrid grid = getGridView().getGrid();
    final IsRow row = event.getRowValue();
    final BeeParameter param = params.get(BeeUtils.toInt(row.getId()));

    final Consumer<String> consumer = new Consumer<String>() {
      @Override
      public void accept(String value) {
        set(param, value);
        row.setValue(getGridView().getDataIndex(column), render(param));
        grid.refreshCellContent(row.getId(), column);
      }
    };
    if (event.isDelete()) {
      consumer.accept(null);
    } else {
      if (param.getType() == ParameterType.BOOLEAN) {
        consumer.accept(BeeUtils.toString(!BeeUtils.unbox(getBoolean(param))));
      } else {
        grid.setEditing(true);
        final InputText text = new InputText();
        StyleUtils.copyBox(event.getSourceElement(), text.getElement());
        StyleUtils.copyFont(event.getSourceElement(), text.getElement());
        text.setValue(param.getValue());

        final Popup popup = new Popup(OutsideClick.CLOSE, null);
        popup.setHideOnEscape(true);
        popup.setHideOnSave(true);
        popup.setWidget(text);

        popup.addCloseHandler(new CloseEvent.Handler() {
          @Override
          public void onClose(CloseEvent ev) {
            if (!ev.keyboardEscape()) {
              consumer.accept(text.getValue());
            }
            grid.setEditing(false);
            grid.refocus();
          }
        });
        popup.showOnTop(event.getSourceElement());
        UiHelper.focus(text);
      }
    }
  }

  @Override
  public void onShow(GridPresenter presenter) {
    if (presenter != null && presenter.getDataProvider() instanceof LocalProvider) {
      provider = (LocalProvider) presenter.getDataProvider();
      requery();
    }
  }

  private void reset(BeeParameter param) {
    ParameterList args = CommonsKeeper.createArgs(SVC_RESET_PARAMETER);
    args.addDataItem(VAR_PARAMETER, param.getName());

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(getGridView());
      }
    });
  }

  private void set(BeeParameter param, String value) {
    if (param.supportsUsers()) {
      param.setUserValue(BeeKeeper.getUser().getUserId(), value);
    } else {
      param.setValue(value);
    }
    ParameterList args = CommonsKeeper.createArgs(SVC_SET_PARAMETER);
    args.addDataItem(VAR_PARAMETER, param.getName());

    if (value != null) {
      args.addDataItem(VAR_PARAMETER_VALUE, value);
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(getGridView());
      }
    });
  }

  private static int indexOf(String colName) {
    return DataUtils.getColumnIndex(colName, columns);
  }

  private void requery() {
    if (provider == null) {
      return;
    }
    provider.clear();
    params.clear();
    ParameterList args = CommonsKeeper.createArgs(SVC_GET_PARAMETERS);
    args.addDataItem(VAR_PARAMETERS_MODULE, module);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(getGridView());

        if (response.hasErrors()) {
          return;
        }
        for (String prm : Codec.beeDeserializeCollection(response.getResponseAsString())) {
          BeeParameter p = BeeParameter.restore(prm);
          params.add(p);

          String[] values = new String[columns.size()];
          values[indexOf(NAME)] = p.getName();
          values[indexOf(VALUE)] = render(p);
          values[indexOf(DESCRIPTION)] = Localized.translate("prm" + p.getName());

          provider.addRow(new BeeRow(params.size() - 1, values));
        }
        provider.refresh(false);
      }
    });
  }

  public String render(BeeParameter param) {
    String value = null;

    switch (param.getType()) {
      case BOOLEAN:
        value = BeeUtils.unbox(getBoolean(param))
            ? Localized.getConstants().yes() : Localized.getConstants().no();
        break;

      case COLLECTION:
        Collection<String> collection = getCollection(param);

        if (!BeeUtils.isEmpty(collection)) {
          value = "..." + BeeUtils.parenthesize(collection);
        }
        break;

      case DATE:
        JustDate date = getDate(param);

        if (date != null) {
          value = date.toString();
        }
        break;

      case DATETIME:
        DateTime dateTime = getDateTime(param);

        if (dateTime != null) {
          value = dateTime.toCompactString();
        }
        break;

      case MAP:
        Map<String, String> map = getMap(param);

        if (!BeeUtils.isEmpty(map)) {
          value = "..." + BeeUtils.parenthesize(map.size());
        }
        break;

      case NUMBER:
        Number number = getNumber(param);

        if (number != null) {
          value = number.toString();
        }
        break;

      case RELATION:
        value = param.getOptions();
        break;

      case TEXT:
        value = getText(param);
        break;

      case TIME:
        Long time = getTime(param);

        if (time != null) {
          value = TimeUtils.renderTime(time, false);
        }
        break;
    }
    return value;
  }
}