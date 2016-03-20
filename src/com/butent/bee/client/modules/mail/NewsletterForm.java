package com.butent.bee.client.modules.mail;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dialog.ModalGrid;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.mail.MailConstants.RecipientsGroupsVisibility;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Arrays;
import java.util.List;

public class NewsletterForm extends AbstractFormInterceptor {

  @Override
  public void afterCreateWidget(String widName, IdentifiableWidget wid,
      WidgetDescriptionCallback cb) {
    if (wid instanceof ChildGrid && BeeUtils.same(widName, VIEW_NEWSLETTER_CONTACTS)) {
      ChildGrid grid = (ChildGrid) wid;
      grid.setGridInterceptor(new AbstractGridInterceptor() {

        @Override
        public GridInterceptor getInstance() {
          return null;
        }

        @Override
        public boolean beforeAddRow(final GridPresenter presenter, boolean copy) {
          getGridView().ensureRelId(new IdCallback() {

            @Override
            public void onSuccess(final Long result) {

              Dictionary lc = Localized.getConstants();
              List<String> captions =
                  Arrays.asList(lc.mailRecipientsGroups(), lc.companies(), lc.persons(), lc
                      .companyPersons(), lc.additionalContacts());

              Global.choice(null,
                  Localized.getConstants().chooseContactSource(), captions, new ChoiceCallback() {

                    @Override
                    public void onSuccess(int value) {
                      switch (value) {
                        case 0:
                          final MultiSelector multi =
                              MultiSelector.autonomous(VIEW_RECIPIENTS_GROUPS, Lists
                                  .newArrayList(COL_GROUP_NAME));

                          Filter filter =
                              Filter.or(Filter.equals(CalendarConstants.COL_CREATOR, BeeKeeper
                                  .getUser().getUserId()), Filter.equals(
                                  CalendarConstants.COL_VISIBILITY,
                                  RecipientsGroupsVisibility.PUBLIC.ordinal()));

                          multi.getOracle().setAdditionalFilter(filter, true);

                          Global.inputWidget(Localized.getConstants().mailRecipientsGroups(),
                              multi, new InputCallback() {

                                @Override
                                public void onSuccess() {
                                  ParameterList params =
                                      MailKeeper.createArgs(SVC_GET_NEWSLETTER_CONTACTS);
                                  params.addDataItem(COL_NEWSLETTER, result);
                                  params.addDataItem(Service.VAR_DATA, Codec.beeSerialize(multi
                                      .getIds()));

                                  BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {

                                    @Override
                                    public void onResponse(ResponseObject response) {
                                      DataChangeEvent.fireRefresh(BeeKeeper.getBus(),
                                          VIEW_NEWSLETTER_CONTACTS);
                                    }
                                  });
                                }

                                @Override
                                public String getErrorMessage() {
                                  if (BeeUtils.isEmpty(multi.getIds())) {
                                    return Localized.getConstants().valueRequired();
                                  }
                                  return InputCallback.super.getErrorMessage();
                                }
                              });

                          break;

                        case 1:
                          GridFactory.openGrid(VIEW_SELECT_COMPANIES, new ContactsCreator(result,
                              true), null, ModalGrid.opener(800, CssUnit.PX, 600, CssUnit.PX));
                          break;

                        case 2:
                          GridFactory.openGrid(VIEW_SELECT_PERSONS, new ContactsCreator(result,
                              true), null, ModalGrid.opener(800, CssUnit.PX, 600, CssUnit.PX));
                          break;

                        case 3:
                          GridFactory.openGrid(VIEW_SELECT_COMPANY_PERSONS, new ContactsCreator(
                              result, true), null, ModalGrid.opener(800, CssUnit.PX, 600,
                              CssUnit.PX));
                          break;

                        case 4:
                          GridFactory.openGrid(VIEW_SELECT_COMPANY_CONTACTS, new ContactsCreator(
                              result, true), null, ModalGrid.opener(800, CssUnit.PX, 600,
                              CssUnit.PX));
                          break;
                      }
                    }
                  });
            }
          });
          return false;
        }
      });
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new NewsletterForm();
  }
}
