package com.butent.bee.client.modules.administration;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomConfigForm extends AbstractFormInterceptor implements ClickHandler {

  private static final String[] OBJECTS = new String[] {
      COL_CONFIG_MODULE, COL_CONFIG_TYPE, COL_CONFIG_OBJECT};

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, COL_CONFIG_OBJECT) && widget instanceof HasClickHandlers) {
      ((HasClickHandlers) widget).addClickHandler(this);
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    form.getViewPresenter().getHeader().clearCommandPanel();

    if (!DataUtils.isNewRow(row)) {
      FaLabel diff = new FaLabel(FontAwesome.CODE_FORK);
      diff.setTitle(Localized.dictionary().differences());

      diff.addClickHandler(clickEvent -> {
        Map<String, String> data = new HashMap<>();

        for (String obj : OBJECTS) {
          data.put(obj, getStringValue(obj));
        }
        data.put(COL_CONFIG_DATA, getStringValue(COL_CONFIG_DATA));

        CustomConfigGrid.showDiff(data);
      });
      form.getViewPresenter().getHeader().addCommandItem(diff);
    }
    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CustomConfigForm();
  }

  @Override
  public boolean isWidgetEditable(EditableWidget editableWidget, IsRow row) {
    if (ArrayUtils.contains(OBJECTS, editableWidget.getColumnId())) {
      return DataUtils.isNewRow(row);
    }
    return super.isWidgetEditable(editableWidget, row);
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    if (!DataUtils.isNewRow(getActiveRow())) {
      return;
    }
    ParameterList args = AdministrationKeeper.createArgs(SVC_GET_CONFIG_OBJECTS);
    for (String obj : OBJECTS) {
      if (!Objects.equals(obj, COL_CONFIG_OBJECT)) {
        args.addNotEmptyData(obj, getStringValue(obj));
      }
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        List<String> options =
            Arrays.asList(Codec.beeDeserializeCollection(response.getResponseAsString()));

        if (!BeeUtils.isEmpty(options)) {
          Global.choice(Localized.dictionary().object(), null, options, index -> {
            updateValue(COL_CONFIG_OBJECT, options.get(index));
            getConfigData(COL_CONFIG_OBJECT, options.get(index));
          });
        }
      }
    });
  }

  @Override
  public void onEditEnd(EditEndEvent event, Object source) {
    if (event.valueChanged() && ArrayUtils.contains(OBJECTS, event.getColumn().getId())) {
      getConfigData(event.getColumn().getId(), event.getNewValue());
    }
    super.onEditEnd(event, source);
  }

  private void getConfigData(String object, String value) {
    ParameterList args = AdministrationKeeper.createArgs(SVC_GET_CONFIG_OBJECT);

    for (String obj : OBJECTS) {
      if (!Objects.equals(obj, object)) {
        args.addNotEmptyData(obj, getStringValue(obj));
      }
    }
    args.addNotEmptyData(object, value);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        updateValue(COL_CONFIG_DATA, response.getResponseAsString());
      }
    });
  }

  private void updateValue(String source, String value) {
    getActiveRow().setValue(getDataIndex(source), value);
    getFormView().refreshBySource(source);
  }
}
