package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class AssessmentReportsForm extends AbstractFormInterceptor {

  private static final String STORAGE_KEY_PREFIX = "AssessmentReports_";

  private static final String NAME_START_DATE = "StartDate";
  private static final String NAME_END_DATE = "EndDate";

  private static final String NAME_DEPARTMENTS = "Departments";
  private static final String NAME_MANAGERS = "Managers";

  private static final List<String> NAME_GROUP_BY =
      Lists.newArrayList("Group0", "Group1", "Group2");

  private static String storageKey(String name, long user) {
    return STORAGE_KEY_PREFIX + name + user;
  }

  AssessmentReportsForm() {
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    return super.beforeAction(action, presenter);
  }

  @Override
  public FormInterceptor getInstance() {
    return new AssessmentReportsForm();
  }

  @Override
  public void onLoad(FormView form) {
    Long user = BeeKeeper.getUser().getUserId();
    if (!DataUtils.isId(user)) {
      return;
    }

    Widget widget = form.getWidgetByName(NAME_START_DATE);
    DateTime dateTime = BeeKeeper.getStorage().getDateTime(storageKey(NAME_START_DATE, user));
    if (widget instanceof InputDateTime && dateTime != null) {
      ((InputDateTime) widget).setDateTime(dateTime);
    }

    widget = form.getWidgetByName(NAME_END_DATE);
    dateTime = BeeKeeper.getStorage().getDateTime(storageKey(NAME_END_DATE, user));
    if (widget instanceof InputDateTime && dateTime != null) {
      ((InputDateTime) widget).setDateTime(dateTime);
    }

    widget = form.getWidgetByName(NAME_DEPARTMENTS);
    String idList = BeeKeeper.getStorage().get(storageKey(NAME_DEPARTMENTS, user));
    if (widget instanceof MultiSelector && !BeeUtils.isEmpty(idList)) {
      ((MultiSelector) widget).render(idList);
    }

    widget = form.getWidgetByName(NAME_MANAGERS);
    idList = BeeKeeper.getStorage().get(storageKey(NAME_MANAGERS, user));
    if (widget instanceof MultiSelector && !BeeUtils.isEmpty(idList)) {
      ((MultiSelector) widget).render(idList);
    }

    for (String groupName : NAME_GROUP_BY) {
      widget = form.getWidgetByName(groupName);
      Integer index = BeeKeeper.getStorage().getInteger(storageKey(groupName, user));
      if (widget instanceof ListBox && BeeUtils.isPositive(index)) {
        ((ListBox) widget).setSelectedIndex(index);
      }
    }
  }

  @Override
  public void onUnload(FormView form) {
    Long user = BeeKeeper.getUser().getUserId();
    if (!DataUtils.isId(user)) {
      return;
    }

    BeeKeeper.getStorage().set(storageKey(NAME_START_DATE, user), getDateTime(NAME_START_DATE));
    BeeKeeper.getStorage().set(storageKey(NAME_END_DATE, user), getDateTime(NAME_END_DATE));

    BeeKeeper.getStorage().set(storageKey(NAME_DEPARTMENTS, user),
        getEditorValue(NAME_DEPARTMENTS));
    BeeKeeper.getStorage().set(storageKey(NAME_MANAGERS, user),
        getEditorValue(NAME_MANAGERS));

    for (String groupName : NAME_GROUP_BY) {
      Widget widget = form.getWidgetByName(groupName);
      if (widget instanceof ListBox) {
        Integer index = ((ListBox) widget).getSelectedIndex();
        if (!BeeUtils.isPositive(index)) {
          index = null;
        }

        BeeKeeper.getStorage().set(storageKey(groupName, user), index);
      }
    }
  }

  private DateTime getDateTime(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof InputDateTime) {
      return ((InputDateTime) widget).getDateTime();
    } else {
      return null;
    }
  }

  private String getEditorValue(String name) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof HasStringValue) {
      return ((HasStringValue) widget).getValue();
    } else {
      return null;
    }
  }
}
