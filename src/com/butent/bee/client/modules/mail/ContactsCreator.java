package com.butent.bee.client.modules.mail;

import com.butent.bee.client.Global;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.font.FontAwesome;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.utils.Codec;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ContactsCreator extends AbstractGridInterceptor {

  private Long targetId;
  private boolean isNewsletter;
  private String viewName;

  public ContactsCreator(Long targetId, boolean isNewsletter, String viewName) {
    this.targetId = targetId;
    this.isNewsletter = isNewsletter;
    this.viewName = viewName;
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    HeaderView header = presenter.getHeader();
    header.clearCommandPanel();

    FaLabel addChecked = new FaLabel(FontAwesome.CALENDAR_CHECK_O);
    FaLabel addAll = new FaLabel(FontAwesome.CALENDAR_PLUS_O);

    addChecked.setTitle(Localized.dictionary().addMarked());
    addAll.setTitle(Localized.dictionary().addAll());

    addChecked.addClickHandler(clickEvent -> {
      Set<Long> ids = new HashSet<>();

      for (RowInfo row : getGridView().getSelectedRows(GridView.SelectedRows.ALL)) {
        ids.add(row.getId());
      }

      addContacts(ids);
    });

    addAll.addClickHandler(clickEvent -> Global.confirm(Localized.dictionary().askAddAll(),
            () -> Queries.getRowSet(viewName, Arrays.asList(Data.getColumns(viewName).get(0).getId()),
              Filter.notNull(COL_EMAIL), result -> {
      if (!result.isEmpty()) {
        Set<Long> ids = new HashSet<>(result.getRowIds());
        addContacts(ids);
      }
    })));

    header.addCommandItem(addChecked);
    header.addCommandItem(addAll);

  }

  private void addContacts(Set<Long> ids) {
    if (ids.isEmpty()) {
      getGridView().notifyWarning(Localized.dictionary().selectAtLeastOneRow());
      return;
    }

    String colName;
    final String refreshView;
    switch (getViewName()) {
      case VIEW_SELECT_COMPANIES:
        colName = COL_COMPANY;
        refreshView = VIEW_NEWS_COMPANIES;
        break;

      case VIEW_SELECT_COMPANY_PERSONS:
        colName = COL_COMPANY_PERSON;
        refreshView = VIEW_NEWS_COMPANY_PERSONS;
        break;

      case VIEW_SELECT_COMPANY_CONTACTS:
        colName = COL_COMPANY_CONTACT;
        refreshView = VIEW_NEWS_COMPANY_CONTACTS;
        break;

      case VIEW_SELECT_PERSONS:
        colName = COL_PERSON;
        refreshView = VIEW_NEWS_PERSONS;
        break;

      default:
        return;
    }

    if (DataUtils.isId(targetId) && !isNewsletter) {
      BeeRowSet rowSet =
              new BeeRowSet(VIEW_RCPS_GROUPS_CONTACTS, Data.getColumns(VIEW_RCPS_GROUPS_CONTACTS));
      int indexId = Data.getColumnIndex(MailConstants.VIEW_RCPS_GROUPS_CONTACTS,
              COL_RECIPIENTS_GROUP);
      int index = Data.getColumnIndex(MailConstants.VIEW_RCPS_GROUPS_CONTACTS, colName);

      for (Long id : ids) {
        BeeRow row = DataUtils.createEmptyRow(rowSet.getNumberOfColumns());
        row.setValue(indexId, targetId);
        row.setValue(index, id);
        rowSet.addRow(row);
      }
      Queries.insertRows(rowSet, result -> DataChangeEvent.fireRefresh(BeeKeeper.getBus(), refreshView));

    } else if (ids.size() > 0 && DataUtils.isId(targetId)) {
      ParameterList params = MailKeeper.createArgs(SVC_GET_NEWSLETTER_CONTACTS);
      params.addDataItem(Service.VAR_DATA, Codec.beeSerialize(ids));
      params.addDataItem(Service.VAR_COLUMN, colName);
      params.addDataItem(COL_NEWSLETTER, targetId);

      BeeKeeper.getRpc().makeRequest(params,
              response -> DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_NEWSLETTER_CONTACTS));
    }

    Popup.getActivePopup().close();
  }
}
