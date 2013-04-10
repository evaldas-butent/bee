package com.butent.bee.client.data;

import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.dialog.ModalForm;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.event.logical.OpenEvent;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.presenter.RowPresenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.WidgetFactory;
import com.butent.bee.client.ui.WidgetSupplier;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
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

import java.util.EnumSet;
import java.util.Set;

public class RowEditor {

  public static final String DIALOG_STYLE = "bee-EditRow";
  public static final String EDITABLE_RELATION_STYLE = "bee-EditableRelation";

  private static final BeeLogger logger = LogUtils.getLogger(RowEditor.class);

  private static final String HAS_DELEGATE = "*";

  private static final Set<String> hasEditorDelegates = Sets.newHashSet();

  public static String getFormName(String formName, DataInfo dataInfo) {
    if (!BeeUtils.isEmpty(formName)) {
      return formName;
    }
    if (dataInfo == null) {
      return formName;
    }

    if (!BeeUtils.isEmpty(dataInfo.getEditForm())) {
      return dataInfo.getEditForm();
    }

    if (hasEditorDelegates.contains(dataInfo.getViewName())) {
      return HAS_DELEGATE;
    } else {
      return null;
    }
  }

  public static String getSupplierKey(String viewName, long rowId) {
    Assert.notEmpty(viewName);
    return BeeUtils.join(BeeConst.STRING_UNDER, "row", BeeUtils.normalize(viewName), rowId);
  }

  public static void openRow(String viewName, IsRow row, boolean modal) {
    openRow(viewName, row, modal, null, null, modal ? null : PresenterCallback.SHOW_IN_NEW_TAB);
  }

  public static void openRow(String viewName, IsRow row, boolean modal, UIObject target,
      RowCallback rowCallback, PresenterCallback presenterCallback) {
    Assert.notEmpty(viewName);

    DataInfo dataInfo = Data.getDataInfo(viewName);
    if (dataInfo == null) {
      return;
    }

    String formName = getFormName(null, dataInfo);
    openRow(formName, dataInfo, row, modal, target, rowCallback, presenterCallback);
  }

  public static void openRow(String formName, DataInfo dataInfo, IsRow row, boolean modal,
      UIObject target, RowCallback rowCallback, PresenterCallback presenterCallback) {

    Assert.notNull(dataInfo);
    Assert.notNull(row);

    RowActionEvent event = new RowActionEvent(dataInfo.getViewName(), row, Service.EDIT_ROW);
    BeeKeeper.getBus().fireEvent(event);
    if (event.isConsumed()) {
      return;
    }

    if (BeeUtils.isEmpty(formName) || HAS_DELEGATE.equals(formName)) {
      logger.warning(dataInfo.getViewName(), "edit form not specified");
      return;
    }

    createForm(formName, dataInfo, row, modal, target, rowCallback, presenterCallback);
  }

  public static void openRow(String formName, DataInfo dataInfo, long rowId) {
    openRow(formName, dataInfo, rowId, false, null, null);
  }

  public static void openRow(String formName, DataInfo dataInfo, long rowId,
      boolean modal, UIObject target, RowCallback rowCallback) {
    Assert.notNull(dataInfo);
    getRow(formName, dataInfo, rowId, modal, target, rowCallback);
  }

  public static boolean openRow(String viewName, Long rowId, boolean modal,
      RowCallback rowCallback) {
    if (BeeUtils.isEmpty(viewName) || !DataUtils.isId(rowId)) {
      return false;
    }

    DataInfo dataInfo = Data.getDataInfo(viewName);
    if (dataInfo == null) {
      return false;
    }

    String formName = getFormName(null, dataInfo);
    if (BeeUtils.isEmpty(formName)) {
      return false;
    }

    getRow(formName, dataInfo, rowId, modal, null, rowCallback);
    return true;
  }

  public static void openRow(String formName, String viewName, IsRow row, boolean modal,
      RowCallback rowCallback) {
    Assert.notEmpty(viewName);

    DataInfo dataInfo = Data.getDataInfo(viewName);
    if (dataInfo == null) {
      return;
    }

    openRow(formName, dataInfo, row, modal, null, rowCallback, PresenterCallback.SHOW_IN_NEW_TAB);
  }

  public static void registerHasDelegate(String viewName) {
    hasEditorDelegates.add(viewName);
  }

  private static void createForm(String formName, final DataInfo dataInfo, final IsRow row,
      final boolean modal, final UIObject target, final RowCallback rowCallback,
      final PresenterCallback presenterCallback) {

    FormFactory.createFormView(formName, dataInfo.getViewName(), dataInfo.getColumns(), true,
        new FormFactory.FormViewCallback() {
          @Override
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.setEditing(true);
              result.start(null);

              openForm(formDescription, result, dataInfo, row,
                  modal || Popup.getActivePopup() != null, target, rowCallback, presenterCallback);
            }
          }
        });
  }

  private static void getRow(final String formName, final DataInfo dataInfo, final long rowId,
      final boolean modal, final UIObject target, final RowCallback rowCallback) {

    String supplierKey = getSupplierKey(dataInfo.getViewName(), rowId);

    if (!modal && !WidgetFactory.hasSupplier(supplierKey)) {
      WidgetSupplier supplier = new WidgetSupplier() {
        @Override
        public void create(final Callback<IdentifiableWidget> callback) {
          getRow(formName, dataInfo, rowId, modal, null, null, new PresenterCallback() {
            @Override
            public void onCreate(Presenter presenter) {
              callback.onSuccess(presenter.getWidget());
            }
          });
        }
      };

      WidgetFactory.registerSupplier(supplierKey, supplier);
    }

    getRow(formName, dataInfo, rowId, modal, target, rowCallback,
        PresenterCallback.SHOW_IN_NEW_TAB);
  }

  private static void getRow(final String formName, final DataInfo dataInfo, long rowId,
      final boolean modal, final UIObject target, final RowCallback rowCallback,
      final PresenterCallback presenterCallback) {

    Queries.getRow(dataInfo.getViewName(), rowId, new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        openRow(formName, dataInfo, result, modal, target, rowCallback, presenterCallback);
      }
    });
  }

  private static void openForm(FormDescription formDescription, final FormView formView,
      final DataInfo dataInfo, final IsRow oldRow, final boolean modal, UIObject target,
      final RowCallback callback, PresenterCallback presenterCallback) {
    
    Set<Action> enabledActions = EnumSet.of(Action.SAVE, Action.PRINT, Action.CLOSE);
    Set<Action> disabledActions = Sets.newHashSet();
    
    if (formDescription != null) {
      Set<Action> actions = formDescription.getEnabledActions();
      if (!BeeUtils.isEmpty(actions)) {
        disabledActions.addAll(enabledActions);
        disabledActions.removeAll(actions);
        enabledActions.retainAll(actions);
      }
      
      actions = formDescription.getDisabledActions();
      if (!BeeUtils.isEmpty(actions)) {
        enabledActions.removeAll(actions);
        disabledActions.addAll(actions);
      }
    }

    final RowPresenter presenter = new RowPresenter(formView, dataInfo, oldRow.getId(),
        DataUtils.getRowCaption(dataInfo, oldRow), enabledActions, disabledActions);
    final ModalForm dialog =
        modal ? new ModalForm(presenter.getWidget().asWidget(), formView, false) : null;

    final CloseCallback close = new CloseCallback() {
      @Override
      public void onClose() {
        closeForm();
        if (callback != null) {
          callback.onCancel();
        }
      }

      @Override
      public void onSave() {
        if (validate(formView)) {
          update(dataInfo, oldRow, formView.getActiveRow(), formView,
              new RowCallback() {
                @Override
                public void onCancel() {
                  onClose();
                }

                @Override
                public void onSuccess(BeeRow result) {
                  closeForm();
                  if (callback != null) {
                    callback.onSuccess(result);
                  }
                }
              });
        }
      }

      private void closeForm() {
        if (modal) {
          dialog.close();
        } else {
          BeeKeeper.getScreen().closeWidget(presenter.getWidget());
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
          if (formView.printHeader()) {
            Printer.print(presenter);
          } else {
            Printer.print(formView);
          }
        }
      }
    });

    final ScheduledCommand focusCommand = new ScheduledCommand() {
      @Override
      public void execute() {
        UiHelper.focus(formView.asWidget());
      }
    };

    if (modal) {
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

      dialog.addOpenHandler(new OpenEvent.Handler() {
        @Override
        public void onOpen(OpenEvent event) {
          formView.editRow(oldRow, focusCommand);
        }
      });

      if (target == null) {
        dialog.center();
      } else {
        dialog.showRelativeTo(target.getElement());
      }

    } else {
      if (presenterCallback != null) {
        presenterCallback.onCreate(presenter);
      }
      formView.editRow(oldRow, focusCommand);
    }
  }

  private static void update(final DataInfo dataInfo, IsRow oldRow, IsRow newRow,
      final FormView formView, final RowCallback callback) {

    SaveChangesEvent event = SaveChangesEvent.create(oldRow, newRow, dataInfo.getColumns(),
        formView.getChildrenForUpdate(), callback);

    formView.onSaveChanges(event);
    if (event.isConsumed() || event.isEmpty()) {
      callback.onCancel();
      return;
    }

    BeeRowSet updated = DataUtils.getUpdated(dataInfo.getViewName(), oldRow.getId(),
        oldRow.getVersion(), event.getColumns(), event.getOldValues(), event.getNewValues(),
        event.getChildren());
    
    if (DataUtils.isEmpty(updated) && BeeUtils.isEmpty(event.getChildren())) {
      return;
    }

    RowCallback updateCallback = new RowCallback() {
      @Override
      public void onFailure(String... reason) {
        formView.notifySevere(reason);
      }

      @Override
      public void onSuccess(BeeRow result) {
        BeeKeeper.getBus().fireEvent(new RowUpdateEvent(dataInfo.getViewName(), result));
        callback.onSuccess(result);
      }
    };
    
    if (DataUtils.isEmpty(updated)) {
      Queries.updateChildren(dataInfo.getViewName(), oldRow.getId(), event.getChildren(),
          updateCallback);
    } else {
      Queries.updateRow(updated, updateCallback);
    }
  }

  private static boolean validate(FormView formView) {
    return formView.validate(formView, true);
  }

  private RowEditor() {
  }
}
