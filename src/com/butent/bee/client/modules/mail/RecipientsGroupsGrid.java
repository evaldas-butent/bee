package com.butent.bee.client.modules.mail;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.ModalGrid;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

public class RecipientsGroupsGrid extends AbstractGridInterceptor {

  private String gridName;
  Long parentId;

  public RecipientsGroupsGrid(String gridName) {
    this.gridName = gridName;
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    presenter.getGridView().ensureRelId(new IdCallback() {

      @Override
      public void onSuccess(Long result) {
        if (!BeeUtils.isEmpty(gridName)) {
          parentId = result;
          getGridDialog();
        }
      }
    });
    return false;
  }

  private void getGridDialog() {
    if (DataUtils.isId(parentId)) {

      String inView = "";
      String inColumn = "";
      String idColumn = "";
      switch (gridName) {
        case VIEW_SELECT_COMPANIES:
          inView = VIEW_NEWS_COMPANIES;
          inColumn = COL_COMPANY;
          idColumn = "CompanyID";
          break;

        case VIEW_SELECT_COMPANY_CONTACTS:
          inView = VIEW_NEWS_COMPANY_CONTACTS;
          inColumn = COL_COMPANY_CONTACT;
          idColumn = "CompanyContactId";
          break;

        case VIEW_SELECT_COMPANY_PERSONS:
          inView = VIEW_NEWS_COMPANY_PERSONS;
          inColumn = COL_COMPANY_PERSON;
          idColumn = "CompanyPersonID";
          break;

        case VIEW_SELECT_PERSONS:
          inView = VIEW_NEWS_PERSONS;
          inColumn = COL_PERSON;
          idColumn = "PersonID";
          break;

      }
      GridFactory.openGrid(gridName, new ContactsCreator(parentId, false), GridOptions
          .forFilter(Filter.isNot(Filter.in(idColumn, inView, inColumn, Filter.and(Filter
              .notNull(inColumn), Filter.equals(COL_RECIPIENTS_GROUP, parentId))))), ModalGrid
          .opener(800, CssUnit.PX, 90, CssUnit.PCT));
    }
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    event.consume();
    IsRow row = event.getRowValue();

    switch (gridName) {
      case VIEW_SELECT_COMPANIES:
        Long companyId = row.getLong(Data.getColumnIndex(VIEW_NEWS_COMPANIES, COL_COMPANY));
        if (DataUtils.isId(companyId)) {
          RowEditor.openForm(FORM_COMPANY,
              Data.getDataInfo(VIEW_COMPANIES),
              companyId,
              Opener.MODAL);
        }

        break;

      case VIEW_SELECT_PERSONS:
        Long personId = row.getLong(Data.getColumnIndex(VIEW_NEWS_PERSONS, COL_PERSON));
        if (DataUtils.isId(personId)) {
          RowEditor.openForm(FORM_PERSON,
              Data.getDataInfo(VIEW_PERSONS),
              personId,
              Opener.MODAL);
        }

        break;

      case VIEW_SELECT_COMPANY_PERSONS:
        Long companyPersonId = row.getLong(
                                Data.getColumnIndex(VIEW_NEWS_COMPANY_PERSONS, COL_COMPANY_PERSON));
        if (DataUtils.isId(companyPersonId)) {
          RowEditor.openForm(FORM_COMPANY_PERSON,
              Data.getDataInfo(VIEW_COMPANY_PERSONS),
              companyPersonId,
              Opener.MODAL);
        }

        break;
    }
  }
}
