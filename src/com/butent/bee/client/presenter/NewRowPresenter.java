package com.butent.bee.client.presenter;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.data.ParentRowCreator;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;
import java.util.List;

public class NewRowPresenter extends AbstractPresenter implements ParentRowCreator {

  public static final String STYLE_CONTAINER = "bee-NewRowContainer";
  public static final String STYLE_HEADER = "bee-NewRowHeader";
  public static final String STYLE_CAPTION = "bee-NewRowCaption";

  private final DataInfo dataInfo;
  private final FormView formView;

  private final Complex container;
  private HandlesActions actionDelegate = null;

  public NewRowPresenter(FormView formView, DataInfo dataInfo, String caption) {

    this.formView = formView;
    this.dataInfo = dataInfo;

    HeaderView header = createHeader(caption);
    this.container = createContainer(header);

    header.setViewPresenter(this);
    formView.setViewPresenter(this);
  }

  @Override
  public boolean createParentRow(NotificationListener notificationListener,
      final Callback<IsRow> callback) {

    if (!formView.validate(notificationListener, false)) {
      return false;
    }
    IsRow row = formView.getActiveRow();
    if (!DataUtils.isNewRow(row)) {
      return true;
    }

    insert(row, new Callback<IsRow>() {
      @Override
      public void onFailure(String... reason) {
        if (callback != null) {
          callback.onFailure(reason);
        }
      }

      @Override
      public void onSuccess(IsRow result) {
        formView.updateRow(result, true);
        if (callback != null) {
          callback.onSuccess(result);
        }
      }
    });
    return true;
  }

  @Override
  public String getCaption() {
    for (Widget child : container) {
      if (child instanceof HasCaption) {
        return ((HasCaption) child).getCaption();
      }
    }
    return null;
  }

  @Override
  public HeaderView getHeader() {
    return ViewHelper.getHeader(getWidget().asWidget());
  }

  @Override
  public IdentifiableWidget getWidget() {
    return container;
  }

  @Override
  public void handleAction(Action action) {
    if (getActionDelegate() != null) {
      getActionDelegate().handleAction(action);
    }
  }

  public void save(final RowCallback callback) {
    if (!formView.validate(formView, true)) {
      return;
    }
    IsRow row = formView.getActiveRow();

    if (DataUtils.isNewRow(row)) {
      insert(row, new Callback<IsRow>() {
        @Override
        public void onFailure(String... reason) {
          formView.notifySevere(reason);
        }

        @Override
        public void onSuccess(IsRow result) {
          if (callback != null && result instanceof BeeRow) {
            callback.onSuccess((BeeRow) result);
          }
        }
      });

    } else {
      int upd = Queries.update(dataInfo.getViewName(), dataInfo.getColumns(), formView.getOldRow(),
          row, new RowCallback() {
            @Override
            public void onFailure(String... reason) {
              formView.notifySevere(reason);
            }

            @Override
            public void onSuccess(BeeRow result) {
              BeeKeeper.getBus().fireEvent(new RowUpdateEvent(dataInfo.getViewName(), result));
              if (callback != null) {
                callback.onSuccess(result);
              }
            }
          });
      
      if (upd == 0 && callback != null && row instanceof BeeRow) {
        callback.onSuccess((BeeRow) row);
      }
    }
  }

  public void setActionDelegate(HandlesActions actionDelegate) {
    this.actionDelegate = actionDelegate;
  }

  private Complex createContainer(HeaderView headerView) {
    Complex formContainer = new Complex();
    formContainer.addStyleName(STYLE_CONTAINER);

    formContainer.addTopHeightFillHorizontal(headerView.asWidget(), 0, headerView.getHeight());
    formContainer.addTopBottomFillHorizontal(formView.asWidget(), headerView.getHeight(), 0);

    return formContainer;
  }

  private HeaderView createHeader(String caption) {
    HeaderView formHeader = GWT.create(HeaderImpl.class);
    formHeader.asWidget().addStyleName(STYLE_HEADER);

    formHeader.create(caption, false, false, null, EnumSet.of(Action.SAVE, Action.CLOSE),
        EnumSet.of(Action.PRINT));
    formHeader.addCaptionStyle(STYLE_CAPTION);

    return formHeader;
  }

  private HandlesActions getActionDelegate() {
    return actionDelegate;
  }

  private void insert(IsRow row, final Callback<IsRow> callback) {
    List<BeeColumn> columns = Lists.newArrayList();
    List<String> values = Lists.newArrayList();

    for (int i = 0; i < dataInfo.getColumnCount(); i++) {
      BeeColumn column = dataInfo.getColumns().get(i);

      String value = row.getString(i);
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      if (column.isWritable()) {
        columns.add(column);
        values.add(value);
      }
    }

    if (columns.isEmpty()) {
      callback.onFailure("New Row", "all columns cannot be empty");
      return;
    }

    if (formView.getFormCallback() != null) {
      ReadyForInsertEvent event = new ReadyForInsertEvent(columns, values, callback);
      if (!formView.getFormCallback().onReadyForInsert(event)) {
        return;
      }
    }

    Queries.insert(dataInfo.getViewName(), columns, values, new RowCallback() {
      @Override
      public void onFailure(String... reason) {
        if (callback == null) {
          formView.notifySevere(reason);
        } else {
          callback.onFailure(reason);
        }
      }

      @Override
      public void onSuccess(BeeRow result) {
        BeeKeeper.getBus().fireEvent(new RowInsertEvent(dataInfo.getViewName(), result));
        if (callback != null) {
          callback.onSuccess(result);
        }
      }
    });
  }
}
