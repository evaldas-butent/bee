package com.butent.bee.client.modules.administration;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.client.widget.InputTimeOfDay;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.ui.UserInterface.Component;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

class UserSettingsForm extends AbstractFormInterceptor {

  private static final String DEFAULT_TASKS_MAILING_TIME = "8:00";

  private static InputBoolean checkBox;
  private static InputTimeOfDay timeInput;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, ClassifierConstants.COL_TASKS_MAILING_TIME)
        && widget instanceof InputTimeOfDay) {
      timeInput = (InputTimeOfDay) widget;
      timeInput.setMaxLength(0);

      timeInput.addValueChangeHandler(new ValueChangeHandler<String>() {

        @Override
        public void onValueChange(ValueChangeEvent<String> arg0) {

          if (checkBox != null && BeeUtils.isEmpty(timeInput.getValue())) {
            checkBox.setValue(BeeUtils.toString(false));
            ValueChangeEvent.fire(checkBox, checkBox.getValue());
          }

        }
      });

      timeInput.addEditStopHandler(new EditStopEvent.Handler() {

        @Override
        public void onEditStop(EditStopEvent event) {
          String timeValue = timeInput.getValue();

          if (checkBox != null && !BeeUtils.isEmpty(timeValue) && !BeeUtils.toBoolean(checkBox
              .getValue())) {

            checkBox.setValue(BeeUtils.toString(true));
            ValueChangeEvent.fire(checkBox, checkBox.getValue());

          } else if (checkBox != null && BeeUtils.isEmpty(timeInput.getValue()) && BeeUtils
              .toBoolean(checkBox.getValue())) {

            checkBox.setValue(BeeUtils.toString(false));
            ValueChangeEvent.fire(checkBox, checkBox.getValue());

          }

          timeInput.setFocus(true);

        }
      });

    } else if (BeeUtils.same(name, ClassifierConstants.COL_REMIND_TASKS)
        && widget instanceof InputBoolean) {

      checkBox = (InputBoolean) widget;
      checkBox.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {

          boolean checked = BeeUtils.toBoolean(checkBox.getValue());

          if (checked && timeInput != null && BeeUtils.isEmpty(timeInput.getValue())) {
            timeInput.setValue(DEFAULT_TASKS_MAILING_TIME);
            timeInput.fireEvent(new EditStopEvent(State.CHANGED));

          } else if (!checked && timeInput != null) {
            timeInput.clearValue();
            timeInput.fireEvent(new EditStopEvent(State.CHANGED));
          }

          checkBox.setFocus(true);

        }
      });
    }
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    Global.getParameter(MailConstants.PRM_DEFAULT_ACCOUNT, new Consumer<String>() {
      @Override
      public void accept(String input) {
        if (input == null || input.isEmpty()) {
          timeInput.setEnabled(false);
          checkBox.setEnabled(false);
        }
      }
    });
    if (row != null) {
      Long userId = row.getLong(form.getDataIndex(COL_USER));

      if (BeeKeeper.getUser().is(userId)) {
        if (!getHeaderView().hasCommands()) {
          getHeaderView().addCommandItem(new Button(Localized.dictionary().changePassword(),
              new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  PasswordService.change();
                }
              }));
        }

      } else if (getHeaderView().hasCommands()) {
        getHeaderView().clearCommandPanel();
      }

      if (DataUtils.isId(userId) && BeeKeeper.getUser().isDataVisible(VIEW_USER_ROLES)) {
        final Widget roleWidget = form.getWidgetByName(VIEW_USER_ROLES);

        Queries.getRowSet(VIEW_USER_ROLES, Lists.newArrayList(ALS_ROLE_NAME),
            Filter.equals(COL_USER, userId), new Queries.RowSetCallback() {

              @Override
              public void onSuccess(BeeRowSet result) {
                List<String> roles = new ArrayList<>();

                if (!DataUtils.isEmpty(result)) {
                  int index = result.getColumnIndex(ALS_ROLE_NAME);
                  for (BeeRow r : result) {
                    roles.add(r.getString(index));
                  }
                }

                if (roleWidget != null) {
                  roleWidget.getElement().setInnerText(BeeUtils.joinItems(roles));
                }
              }
            });
      }
    }
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    if (NewsConstants.GRID_USER_FEEDS.equals(name)) {
      return BeeKeeper.getScreen().getUserInterface().hasComponent(Component.NEWS);
    } else {
      return super.beforeCreateWidget(name, description);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new UserSettingsForm();
  }
}