package com.butent.bee.client.data;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dialog.ModalForm;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.presenter.RowPresenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.utils.BeeUtils;

public class RowEditor {

  private static final BeeLogger logger = LogUtils.getLogger(RowEditor.class);

  public static final String DIALOG_STYLE = "bee-EditRow";

  public static void openRow(String viewName, BeeRow row) {
    openRow(viewName, row, null, null);
  }

  public static void openRow(String viewName, BeeRow row, RowCallback rowCallback) {
    openRow(viewName, row, null, rowCallback);
  }

  public static void openRow(String viewName, BeeRow row, UIObject target,
      RowCallback rowCallback) {
    Assert.notEmpty(viewName);

    RowActionEvent event = new RowActionEvent(viewName, row, Service.EDIT_ROW);
    BeeKeeper.getBus().fireEvent(event);
    if (event.isConsumed()) {
      return;
    }

    DataInfo dataInfo = Data.getDataInfo(viewName);
    if (dataInfo == null) {
      return;
    }

    String formName = dataInfo.getEditForm();
    if (BeeUtils.isEmpty(formName)) {
      logger.warning(viewName, "edit form not specified");
      return;
    }

    openRow(formName, dataInfo, row, target, rowCallback);
  }

  public static void openRow(String formName, DataInfo dataInfo, BeeRow row,
      UIObject target, RowCallback rowCallback) {
    Assert.notEmpty(formName);

    Assert.notNull(dataInfo);
    Assert.notNull(row);

    getForm(formName, dataInfo, row, target, rowCallback);
  }

  private static void getForm(String formName, final DataInfo dataInfo, final BeeRow row,
      final UIObject target, final RowCallback rowCallback) {

    FormFactory.createFormView(formName, dataInfo.getViewName(), dataInfo.getColumns(), true,
        new FormFactory.FormViewCallback() {
          @Override
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.setEditing(true);
              result.start(null);

              openForm(result, dataInfo, row, target, rowCallback);
            }
          }
        });
  }

  private static void openForm(final FormView formView, final DataInfo dataInfo,
      final IsRow oldRow, UIObject target, final RowCallback callback) {

    final RowPresenter presenter = new RowPresenter(formView);
    final ModalForm dialog = new ModalForm(presenter.getWidget(), formView, false, true);

    final CloseCallback close = new CloseCallback() {
      @Override
      public void onClose() {
        dialog.hide();
        if (callback != null) {
          callback.onCancel();
        }
      }

      @Override
      public void onSave() {
        if (validate(formView)) {
          int upd = update(dataInfo, oldRow, formView.getActiveRow(), formView, new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              dialog.hide();
              if (callback != null) {
                callback.onSuccess(result);
              }
            }
          });
          
          if (upd == 0) {
            onClose();
          }
        }
      }
    };

    presenter.setActionDelegate(new HandlesActions() {
      @Override
      public void handleAction(Action action) {
        if (Action.CLOSE.equals(action)) {
          formView.onClose(close);

        } else if (Action.SAVE.equals(action)) {
          close.onSave();

        } else if (Action.PRINT.equals(action)) {
          Printer.print(presenter);
        }
      }
    });

    dialog.setOnSave(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        if (formView.checkOnSave()) {
          presenter.handleAction(Action.SAVE);
        }
      }
    });

    dialog.setOnEscape(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        if (formView.checkOnClose()) {
          presenter.handleAction(Action.CLOSE);
        }
      }
    });

    dialog.addAttachHandler(new AttachEvent.Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached()) {
          formView.updateRow(DataUtils.cloneRow(oldRow), true);
        }
      }
    });

    if (target == null) {
      dialog.center();
    } else {
      dialog.showRelativeTo(target);
    }

    UiHelper.focus(formView.getRootWidget());
  }

  private static int update(final DataInfo dataInfo, IsRow oldRow, IsRow newRow,
      final NotificationListener notificationListener, final RowCallback callback) {

    return Queries.update(dataInfo.getViewName(), dataInfo.getColumns(), oldRow, newRow,
        new RowCallback() {
          @Override
          public void onFailure(String... reason) {
            notificationListener.notifySevere(reason);
          }

          @Override
          public void onSuccess(BeeRow result) {
            BeeKeeper.getBus().fireEvent(new RowUpdateEvent(dataInfo.getViewName(), result));
            if (callback != null) {
              callback.onSuccess(result);
            }
          }
        });
  }

  private static boolean validate(FormView formView) {
    return formView.validate(formView, true);
  }

  private RowEditor() {
  }
}
