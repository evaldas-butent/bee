package com.butent.bee.client.modules.mail;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.ModalGrid;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

public class RecipientsGroupsGrid extends AbstractGridInterceptor {

  private String gridName;
  Long parentId;
  private String column;

  public RecipientsGroupsGrid(String gridName, String column) {
    this.gridName = gridName;
    this.column = column;
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    HeaderView header = presenter.getHeader();
    header.clearCommandPanel();

    FaLabel delete = new FaLabel(FontAwesome.WINDOW_CLOSE);
    delete.setTitle(Localized.dictionary().removeAll());
    delete.addClickHandler(clickEvent -> deleteContacts());

    header.addCommandItem(delete);

    super.afterCreatePresenter(presenter);
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    presenter.getGridView().ensureRelId(result -> {
      if (!BeeUtils.isEmpty(gridName)) {
        parentId = result;
        getGridDialog();
      }
    });
    return false;
  }

  private void getGridDialog() {
    if (DataUtils.isId(parentId)) {

      String inView = "";
      String inColumn = "";
      String idColumn = "";
      String viewName = "";
      switch (gridName) {
        case VIEW_SELECT_COMPANIES:
          inView = VIEW_NEWS_COMPANIES;
          inColumn = COL_COMPANY;
          idColumn = "CompanyID";
          viewName = VIEW_COMPANIES;
          break;

        case VIEW_SELECT_COMPANY_CONTACTS:
          inView = VIEW_NEWS_COMPANY_CONTACTS;
          inColumn = COL_COMPANY_CONTACT;
          idColumn = "CompanyContactId";
          viewName = VIEW_COMPANY_CONTACTS;
          break;

        case VIEW_SELECT_COMPANY_PERSONS:
          inView = VIEW_NEWS_COMPANY_PERSONS;
          inColumn = COL_COMPANY_PERSON;
          idColumn = "CompanyPersonID";
          viewName = VIEW_COMPANY_PERSONS;
          break;

        case VIEW_SELECT_PERSONS:
          inView = VIEW_NEWS_PERSONS;
          inColumn = COL_PERSON;
          idColumn = "PersonID";
          viewName = VIEW_PERSONS;
          break;

      }
      GridFactory.openGrid(gridName, new ContactsCreator(parentId, false, viewName), GridOptions
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
          RowEditor.openForm(FORM_COMPANY, Data.getDataInfo(VIEW_COMPANIES), Filter.compareId(companyId));
        }

        break;

      case VIEW_SELECT_PERSONS:
        Long personId = row.getLong(Data.getColumnIndex(VIEW_NEWS_PERSONS, COL_PERSON));
        if (DataUtils.isId(personId)) {
          RowEditor.openForm(FORM_PERSON, Data.getDataInfo(VIEW_PERSONS), Filter.compareId(personId));
        }

        break;

      case VIEW_SELECT_COMPANY_PERSONS:
        Long companyPersonId = row.getLong(Data.getColumnIndex(VIEW_NEWS_COMPANY_PERSONS, COL_COMPANY_PERSON));
        if (DataUtils.isId(companyPersonId)) {
          RowEditor.openForm(FORM_COMPANY_PERSON, Data.getDataInfo(VIEW_COMPANY_PERSONS),
                  Filter.compareId(companyPersonId));
        }

        break;
    }
  }

  private void deleteContacts() {
    getGridView().ensureRelId(id -> Global.confirm(Localized.dictionary().askDeleteAll(), () -> {
      if (DataUtils.isId(id)) {
        Queries.delete(VIEW_RCPS_GROUPS_CONTACTS,
          Filter.and(Filter.equals(COL_RECIPIENTS_GROUP, id), Filter.notNull(column)),
          result -> Data.refreshLocal(VIEW_RCPS_GROUPS_CONTACTS));
      }
    }));
  }
}
