package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.MenuManager.MenuCallback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.grid.interceptor.UniqueChildInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.utils.BeeUtils;

public final class CommonsKeeper {

  private static class RowTransformHandler implements RowTransformEvent.Handler {
    @Override
    public void onRowTransform(RowTransformEvent event) {
      if (event.hasView(VIEW_USERS)) {
        event.setResult(DataUtils.join(Data.getDataInfo(VIEW_USERS), event.getRow(),
            Lists.newArrayList(COL_LOGIN, COL_FIRST_NAME, COL_LAST_NAME, ALS_COMPANY_NAME),
            BeeConst.STRING_SPACE));

      } else if (event.hasView(VIEW_COMPANIES)) {
        event.setResult(DataUtils.join(Data.getDataInfo(VIEW_COMPANIES), event.getRow(),
            Lists.newArrayList(COL_NAME, COL_COMPANY_CODE, COL_PHONE, COL_EMAIL_ADDRESS,
                COL_ADDRESS, ALS_CITY_NAME, ALS_COUNTRY_NAME), BeeConst.STRING_SPACE));

      } else if (event.hasView(VIEW_PERSONS)) {
        event.setResult(DataUtils.join(Data.getDataInfo(VIEW_PERSONS), event.getRow(),
            Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME, COL_PHONE, COL_EMAIL_ADDRESS,
                COL_ADDRESS, ALS_CITY_NAME, ALS_COUNTRY_NAME), BeeConst.STRING_SPACE));
      }
    }
  }

  private static class UserFormInterceptor extends AbstractFormInterceptor {
    @Override
    public void afterCreateWidget(String name, final IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (BeeUtils.same(name, "ChangePassword") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            changePassword();
          }
        });
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return this;
    }

    @Override
    public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
      if (BeeUtils.isEmpty(getStringValue(COL_PASSWORD))) {
        event.consume();
        changePassword();
      }
    }

    private void changePassword() {
      PasswordService.changePassword(getFormView());
    }
  }

  public static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(COMMONS_MODULE);
    args.addQueryItem(COMMONS_METHOD, name);
    return args;
  }

  public static void register() {
    BeeKeeper.getMenu().registerMenuCallback("items", new MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        GridFactory.openGrid("Items", new ItemGridHandler(BeeUtils.startsSame(parameters, "s")));
      }
    });

    BeeKeeper.getMenu().registerMenuCallback(SVC_UPDATE_EXCHANGE_RATES, new MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        CommonsUtils.updateExchangeRates();
      }
    });

    FormFactory.registerFormInterceptor("User", new UserFormInterceptor());
    FormFactory.registerFormInterceptor("Item", new ItemFormHandler());
    FormFactory.registerFormInterceptor(FORM_PERSON, new PersonFormInterceptor());
    FormFactory.registerFormInterceptor(FORM_COMPANY, new CompanyForm());

    GridFactory.registerGridInterceptor(NewsConstants.GRID_USER_FEEDS, new UserFeedsInterceptor());

    GridFactory.registerGridInterceptor(GRID_USER_GROUP_MEMBERS, new UniqueChildInterceptor(
        Localized.getConstants().userGroupAddMembers(),
        COL_UG_GROUP, COL_UG_USER, VIEW_USERS, Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME),
        Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME, ALS_COMPANY_NAME, ALS_POSITION_NAME)));

    SelectorEvent.register(new CommonsSelectorHandler());

    BeeKeeper.getBus().registerRowTransformHandler(new RowTransformHandler(), false);
  }

  private CommonsKeeper() {
  }
}
