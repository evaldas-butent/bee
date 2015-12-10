package com.butent.bee.client.modules.tasks;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.event.logical.SelectorEvent.Handler;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfoList;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class TaskTemplateForm extends AbstractFormInterceptor {
  private enum TemplateUsers {
      EXECUTOR(PROP_EXECUTORS),
      OBSERVER(PROP_OBSERVERS);

    private final String property;

    TemplateUsers(String property) {
      this.property = property;
    }

    String getProperty() {
      return property;
    }
  }

  private static final String NAME_EXECUTORS = "Executors";
  private static final String NAME_OBSERVERS = "Observers";
  private static final String NAME_FILES = "Files";

  private final Map<Long, List<Long>> readyUpdateExecutors = new ConcurrentHashMap<>();
  private final Map<Long, List<Long>> readyUpdateObservers = new ConcurrentHashMap<>();
  private final Map<Long, List<FileInfo>> readyUpdateFiles = new ConcurrentHashMap<>();

  private final List<Long> readyInsertExecutors = new ArrayList<>();
  private final List<Long> readyInsertObservers = new ArrayList<>();
  private final List<FileInfo> readyInsertFiles = new ArrayList<>();

  private final BeeLogger log = LogUtils.getLogger(TaskTemplateForm.class);

  private MultiSelector executors;
  private MultiSelector observers;
  private FileCollector files;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    switch (name) {
      case NAME_EXECUTORS:
        if (widget instanceof MultiSelector) {
          setExecutors((MultiSelector) widget);
        }
        break;
      case NAME_OBSERVERS:
        if (widget instanceof MultiSelector) {
          setObservers((MultiSelector) widget);
        }
        break;

      case NAME_FILES:
        if (widget instanceof FileCollector) {
          setFiles((FileCollector) widget);
        }
        break;

      default:
        super.afterCreateWidget(name, widget, callback);
        break;
    }
  }

  @Override
  public void afterInsertRow(IsRow result, boolean forced) {
    if (!DataUtils.isId(result.getId())) {
      log.warning("Incorrect id after insert");
      return;
    }

    for (final TemplateUsers usr : TemplateUsers.values()) {
      List<Long> ids = new ArrayList<>();
      switch (usr) {
        case EXECUTOR:
          ids = readyInsertExecutors;
          break;
        case OBSERVER:
          ids = readyInsertObservers;
          break;
      }

      insertUsers(result.getId(), usr, ids,
          new Supplier<Boolean>() {
            @Override
            public Boolean get() {
              switch (usr) {
                case EXECUTOR:
                  readyInsertExecutors.clear();
                  break;
                case OBSERVER:
                  readyInsertObservers.clear();
                  break;
              }

              return null;
            }
          });
    }

    uploadFiles(result.getId(), readyInsertFiles, new Supplier<Boolean>() {

      @Override
      public Boolean get() {
        readyInsertFiles.clear();
        if (getFiles() != null) {
          getFiles().clear();
        }
        return null;
      }

    });
  }

  @Override
  public void afterUpdateRow(final IsRow result) {
    for (final TemplateUsers usr : TemplateUsers.values()) {
      List<Long> ids = new ArrayList<>();
      switch (usr) {
        case EXECUTOR:
          ids = readyUpdateExecutors.get(result.getId());
          break;
        case OBSERVER:
          ids = readyUpdateObservers.get(result.getId());
          break;
      }

      updateUsers(result.getId(), usr, ids, new Supplier<Boolean>() {

        @Override
        public Boolean get() {
          switch (usr) {
            case EXECUTOR:
              readyUpdateExecutors.remove(result.getId());
              break;
            case OBSERVER:
              readyUpdateObservers.remove(result.getId());
              break;
          }

          return null;
        }
      });
    }
    updateFiles(result.getId(), readyUpdateFiles.get(result.getId()), new Supplier<Boolean>() {

      @Override
      public Boolean get() {
        readyUpdateFiles.remove(result.getId());
        if (getFiles() != null) {
          getFiles().clear();
        }
        return null;
      }
    });
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (!DataUtils.isNewRow(row)) {
      fillRowProperties(form, row);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new TaskTemplateForm();
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    if (!readyInsertExecutors.isEmpty() || !readyInsertObservers.isEmpty()
        || !readyInsertFiles.isEmpty()) {
      log.warning("Multiple records try insert");
      event.consume();
      getFormView().getViewPresenter().handleAction(Action.CLOSE);
    }

    prepareInsertUsers();
    prepareUploadFiles();
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    prepareUpdateUsers(event.getNewRow());
    prepareUpdateFiles(event.getNewRow());
    if (isDataChanged(event.getOldRow(), event.getNewRow(), getFiles() == null ? null : getFiles()
        .getFiles())) {
      afterUpdateRow(event.getNewRow());
    }
  }

  public static void deleteFiles(Long taskId, List<Long> files, final Supplier<Boolean> supplier) {
    if (BeeUtils.isEmpty(files)) {
      doCallback(supplier);
      return;
    }

    Filter filter = Filter.and(Filter.equals(COL_TASK_TEMPLATE, taskId), Filter.any(
        AdministrationConstants.COL_FILE, files));

    Queries.delete(VIEW_TASK_TML_FILES, filter, new IntCallback() {

      @Override
      public void onSuccess(Integer result) {
        doCallback(supplier);
      }
    });
  }

  static void getTemplateFiles(final long rowId, final Callback<List<FileInfo>> callback) {
    Queries.getRowSet(VIEW_TASK_TML_FILES, Lists.newArrayList(AdministrationConstants.COL_FILE,
        AdministrationConstants.ALS_FILE_NAME, AdministrationConstants.ALS_FILE_SIZE,
        AdministrationConstants.ALS_FILE_TYPE), getUpdateFilesFilter(rowId), new RowSetCallback() {

          @Override
          public void onSuccess(BeeRowSet result) {
            List<FileInfo> fileList = new ArrayList<>();
            if (callback == null) {
              return;
            }

            for (int i = 0; i < result.getNumberOfRows(); i++) {
              Long fileId = result.getLong(i, AdministrationConstants.COL_FILE);
              String name = result.getString(i, AdministrationConstants.ALS_FILE_NAME);
              Long size = result.getLong(i, AdministrationConstants.ALS_FILE_SIZE);
              String type = result.getString(i, AdministrationConstants.ALS_FILE_TYPE);

              FileInfo f = new FileInfo(fileId, name, size, type);

              fileList.add(f);
            }

            callback.onSuccess(fileList);
          }
        });
  }

  private static void deleteUsers(final List<Long> ids,
      final Supplier<Boolean> supplier) {
    if (BeeUtils.isEmpty(ids)) {
      doCallback(supplier);
      return;
    }

    Filter filter = Filter.idIn(ids);

    Queries.delete(VIEW_TASK_TML_USERS, filter, new IntCallback() {

      @Override
      public void onSuccess(Integer result) {
        doCallback(supplier);
      }
    });
  }

  private static void doCallback(Supplier<Boolean> supplier) {
    if (supplier != null) {
      supplier.get();
    }
  }

  private static Filter getUpdateFilesFilter(long rowId) {
    Filter filter = Filter.equals(COL_TASK_TEMPLATE, rowId);

    return filter;
  }

  private static Filter getUpdateFilter(long rowId, TemplateUsers usrType) {
    return getUpdateFilter(rowId, usrType, null);
  }

  private static Filter getUpdateFilter(long rowId, TemplateUsers usrType, List<String> users) {
    Filter filter = Filter.and(
        Filter.equals(COL_TASK_TEMPLATE, rowId),
        TemplateUsers.EXECUTOR.equals(usrType) ? Filter.notNull(COL_EXECUTOR) : Filter.isNull(
            COL_EXECUTOR));

    if (!BeeUtils.isEmpty(users)) {
      filter = Filter.and(filter, Filter.anyString(AdministrationConstants.COL_USER, users));
    }

    return filter;
  }

  private static void getTemplateUsers(final long rowId, final TemplateUsers usrType,
      final Callback<Set<Long>> callback) {
    Queries.getRowSet(VIEW_TASK_TML_USERS, Lists.newArrayList(AdministrationConstants.COL_USER),
        getUpdateFilter(rowId, usrType), new RowSetCallback() {

          @Override
          public void onSuccess(BeeRowSet result) {
            if (callback != null) {
              callback.onSuccess(result.getDistinctLongs(0));
            }
          }
        });
  }

  private static void insertUsers(final long rowId, final TemplateUsers usrType,
      final List<Long> userList, final Supplier<Boolean> supplier) {
    if (BeeUtils.isEmpty(userList)) {
      doCallback(supplier);
      return;
    }

    List<BeeColumn> columns =
        Data.getColumns(VIEW_TASK_TML_USERS, Lists.newArrayList(COL_TASK_TEMPLATE,
            AdministrationConstants.COL_USER, COL_EXECUTOR));

    BeeRowSet rowSet = new BeeRowSet(VIEW_TASK_TML_USERS, columns);

    for (Long id : userList) {
      BeeRow row = DataUtils.createEmptyRow(columns.size());
      row.setValue(rowSet.getColumnIndex(COL_TASK_TEMPLATE), rowId);
      row.setValue(rowSet.getColumnIndex(AdministrationConstants.COL_USER), id);
      row.setValue(rowSet.getColumnIndex(COL_EXECUTOR), TemplateUsers.EXECUTOR.equals(usrType)
          ? Boolean.TRUE : null);

      rowSet.addRow(row);
    }

    Queries.insertRows(rowSet, new RpcCallback<RowInfoList>() {

      @Override
      public void onSuccess(RowInfoList result) {
        doCallback(supplier);
      }
    });
  }

  private static boolean isDataChanged(IsRow old, IsRow row, List<FileInfo> files) {
    return isFilesChanged(old, files) || isUserValuesChanged(old, row);
  }

  private static boolean isFilesChanged(IsRow old, List<FileInfo> files) {
    if (files == null) {
      return false;
    }

    return !BeeUtils.same(old.getProperty(PROP_FILES), BeeUtils.toString(files.hashCode()));
  }

  private static boolean isUserValuesChanged(IsRow old, IsRow row) {
    boolean result = false;

    for (TemplateUsers usr : TemplateUsers.values()) {

      result = result || !BeeUtils.sameElements(DataUtils.parseIdSet(old.getProperty(usr
          .getProperty())), DataUtils.parseIdSet(row.getProperty(usr.getProperty())));
    }

    return result;

  }

  private static void uploadFiles(final long rowId, final List<FileInfo> fList,
      final Supplier<Boolean> supplier) {
    if (BeeUtils.isEmpty(fList)) {
      doCallback(supplier);
      return;
    }

    final List<BeeColumn> columns = Data.getColumns(VIEW_TASK_TML_FILES,
        Lists.newArrayList(COL_TASK_TEMPLATE, AdministrationConstants.COL_FILE, COL_CAPTION));

    for (final FileInfo f : fList) {
      FileUtils.uploadFile(f, new Callback<Long>() {

        @Override
        public void onSuccess(Long fileId) {
          List<String> values = Lists.newArrayList(BeeUtils.toString(rowId),
              BeeUtils.toString(fileId), f.getCaption());
          Queries.insert(VIEW_TASK_TML_FILES, columns, values);
        }
      });
    }

    doCallback(supplier);
  }

  private static void updateFiles(final long rowId, final List<FileInfo> fList,
      final Supplier<Boolean> supplier) {

    final List<String> fileColumns = Lists.newArrayList(COL_TASK_TEMPLATE,
        AdministrationConstants.COL_FILE, COL_CAPTION);

    Filter filter = getUpdateFilesFilter(rowId);

    Queries.getRowSet(VIEW_TASK_TML_FILES, fileColumns, filter, new RowSetCallback() {

      @Override
      public void onSuccess(BeeRowSet tmlFiles) {
        final List<Long> deletions = new ArrayList<>();
        final List<FileInfo> insertions = new ArrayList<>();
        final Map<Long, FileInfo> updates = new HashMap<>();
        insertions.addAll(fList);

        for (BeeRow tmlFile : tmlFiles) {
          Long fileId = tmlFile.getLong(tmlFiles.getColumnIndex(AdministrationConstants.COL_FILE));

          if (!contains(fileId, fList)) {
            deletions.add(fileId);
          } else {
            updates.put(fileId, getFileInfoById(fileId, fList));
            insertions.remove(getFileInfoById(fileId, fList));
          }

        }

        if (BeeUtils.isEmpty(deletions)) {
          uploadFiles(rowId, insertions, new Supplier<Boolean>() {

            @Override
            public Boolean get() {
              if (BeeUtils.isEmpty(updates)) {
                doCallback(supplier);
              } else {
                doUpdate(rowId, updates, supplier);
              }

              return null;
            }
          });
        } else {
          deleteFiles(rowId, deletions, new Supplier<Boolean>() {

            @Override
            public Boolean get() {
              uploadFiles(rowId, insertions, new Supplier<Boolean>() {
                @Override
                public Boolean get() {
                  doUpdate(rowId, updates, supplier);
                  return null;
                }
              });
              return null;
            }

          });
        }

      }

      private boolean contains(Long fileId, List<FileInfo> files) {
        if (BeeUtils.isEmpty(files)) {
          return false;
        }
        for (FileInfo f : files) {
          if (f.getId() == fileId) {
            return true;
          }
        }

        return false;
      }

      private FileInfo getFileInfoById(Long fileId, List<FileInfo> files) {

        for (FileInfo f : files) {
          if (f.getId() == fileId) {
            return f;
          }
        }

        return null;
      }

      private void doUpdate(Long taskId, Map<Long, FileInfo> files, Supplier<Boolean> supp) {
        List<BeeColumn> columns = Data.getColumns(VIEW_TASK_TML_FILES,
            Lists.newArrayList(COL_TASK_TEMPLATE, AdministrationConstants.COL_FILE, COL_CAPTION));

        for (FileInfo f : files.values()) {
          List<String> values = Lists.newArrayList(BeeUtils.toString(taskId),
              BeeUtils.toString(f.getId()), f.getCaption());
          Queries.insert(VIEW_TASK_TML_FILES, columns, values);
        }

        doCallback(supp);
      }

    });

  }

  private void fillRowProperties(final FormView form, final IsRow row) {
    for (final TemplateUsers usr : TemplateUsers.values()) {
      getTemplateUsers(row.getId(), usr, new Callback<Set<Long>>() {

        @Override
        public void onSuccess(Set<Long> result) {
          row.setProperty(usr.getProperty(), DataUtils.buildIdList(result));
          form.getOldRow().setProperty(usr.getProperty(), DataUtils.buildIdList(result));
          form.refreshBySource(usr.getProperty());
        }
      });
    }

    if (getFiles() != null) {
      getFiles().clear();
    }

    getTemplateFiles(row.getId(), new Callback<List<FileInfo>>() {

      @Override
      public void onSuccess(List<FileInfo> result) {
        if (getFiles() != null) {
          getFiles().addFiles(result);
        }
        row.setProperty(PROP_FILES, BeeUtils.toString(result.hashCode()));
        form.getOldRow().setProperty(PROP_FILES, BeeUtils.toString(result.hashCode()));

      }
    });
  }

  private static void updateUsers(final long rowId, final TemplateUsers usrType,
      final List<Long> userList, final Supplier<Boolean> updated) {

    final List<String> userColumns = Lists.newArrayList(COL_TASK_TEMPLATE,
        AdministrationConstants.COL_USER);

    Filter filter = getUpdateFilter(rowId, usrType);

    Queries.getRowSet(VIEW_TASK_TML_USERS, userColumns, filter, new RowSetCallback() {

      @Override
      public void onSuccess(BeeRowSet tmlUsers) {
        final List<Long> deletions = new ArrayList<>();
        final List<Long> insertions = new ArrayList<>();
        insertions.addAll(userList);

        for (BeeRow tmlUser : tmlUsers) {
          Long user = tmlUser.getLong(tmlUsers.getColumnIndex(
              AdministrationConstants.COL_USER));

          if (!userList.contains(user)) {
            deletions.add(tmlUser.getId());
          } else {
            insertions.remove(user);
          }
        }

        if (BeeUtils.isEmpty(deletions)) {
          insertUsers(rowId, usrType, insertions, updated);
        } else {
          deleteUsers(deletions, new Supplier<Boolean>() {
            @Override
            public Boolean get() {
              insertUsers(rowId, usrType, insertions, updated);

              return null;
            }
          });
        }
      }
    });
  }

  private MultiSelector getExecutors() {
    return executors;
  }

  private FileCollector getFiles() {
    return files;
  }

  private MultiSelector getObservers() {
    return observers;
  }

  private Handler getSelectorHandler(final TemplateUsers exclude) {
    return new Handler() {

      @Override
      public void onDataSelector(SelectorEvent event) {
        if (event.isExclusions()) {
          setSelectorExclusions(event, exclude);
        }
      }
    };
  }

  private void prepareInsertUsers() {
    if (getExecutors() != null) {
      readyInsertExecutors.addAll(getExecutors().getIds());
    }

    if (getObservers() != null) {
      readyInsertObservers.addAll(getObservers().getIds());
    }
  }

  private void prepareUpdateFiles(IsRow row) {
    if (getFiles() != null) {
      readyUpdateFiles.put(row.getId(), getFiles().getFiles());
    }
  }

  private void prepareUpdateUsers(IsRow row) {
    if (getExecutors() != null) {
      readyUpdateExecutors.put(row.getId(), DataUtils.parseIdList(row.getProperty(PROP_EXECUTORS)));
    }

    if (getObservers() != null) {
      readyUpdateObservers.put(row.getId(), DataUtils.parseIdList(row.getProperty(PROP_OBSERVERS)));
    }
  }

  private void prepareUploadFiles() {
    if (getFiles() != null) {
      readyInsertFiles.addAll(getFiles().getFiles());
    }
  }

  private void setExecutors(MultiSelector executors) {
    this.executors = executors;

    if (executors == null) {
      return;
    }

    this.executors.addSelectorHandler(getSelectorHandler(TemplateUsers.OBSERVER));
  }

  private void setFiles(FileCollector files) {
    this.files = files;

    if (files == null) {
      return;
    }
    files.bindDnd(getFormView());
    this.files.clear();
  }

  private void setObservers(MultiSelector observers) {
    this.observers = observers;

    if (observers == null) {
      return;
    }

    this.observers.addSelectorHandler(getSelectorHandler(TemplateUsers.EXECUTOR));
  }

  private void setSelectorExclusions(SelectorEvent event, TemplateUsers exclude) {
    Set<Long> exclusions = DataUtils.parseIdSet(getFormView().getActiveRow().getProperty(
        exclude.getProperty()));
    if (!BeeUtils.isEmpty(event.getExclusions())) {
      exclusions.addAll(event.getExclusions());
    }

    event.consume();
    event.getSelector().getOracle().setExclusions(exclusions);
  }

}
