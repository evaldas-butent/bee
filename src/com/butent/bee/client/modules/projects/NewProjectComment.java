package com.butent.bee.client.modules.projects;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants.ProjectEvent;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class NewProjectComment extends AbstractFormInterceptor {
  private static final String WIDGET_FILES = "Files";

  private Long projectId;
  private Long currentUserId = BeeKeeper.getUser().getUserId();

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof FileCollector) {
      ((FileCollector) widget).bindDnd(getFormView());
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new NewProjectComment(projectId);
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    event.consume();
    final FormView form = getFormView();

    if (form == null) {
      return;
    }

    IsRow row = form.getActiveRow();

    if (row == null) {
      return;
    }

    String viewName = form.getViewName();

    String comment = null;

    if (!BeeUtils.isNegative(form.getDataIndex(COL_COMMENT))) {
      comment = row.getString(form.getDataIndex(COL_COMMENT));
    }

    DateTime time = new DateTime();

    List<BeeColumn> columns = Data.getColumns(VIEW_PROJECT_EVENTS,
        Lists.newArrayList(COL_PROJECT, COL_PUBLISHER, COL_PUBLISH_TIME, COL_COMMENT, COL_EVENT));

    List<String> values = Lists.newArrayList(BeeUtils.toString(getProjectId()),
        BeeUtils.toString(currentUserId), BeeUtils.toString(time.getTime()), comment,
        BeeUtils.toString(ProjectEvent.COMMENT.ordinal()));

    Queries.insert(viewName, columns, values, event.getChildren(),
        new RowInsertCallback(viewName, event.getSourceId()) {

          @Override
          public void onSuccess(BeeRow result) {
            super.onSuccess(result);
            createFiles(result.getId());
            Popup.getActivePopup().close();
          }

        });
  }

  public NewProjectComment(Long projectId) {
    this();
    this.projectId = projectId;
  }

  private NewProjectComment() {
    this.projectId = null;
  }

  private void createFiles(final Long rowId) {
    final Widget widget = getFormView().getWidgetByName(WIDGET_FILES);
    if (widget instanceof FileCollector && !((FileCollector) widget).isEmpty()) {

      final Holder<Integer> counter = Holder.of(0);
      final List<BeeColumn> columns =
          Data.getColumns(VIEW_PROJECT_FILES,
              Lists.newArrayList(COL_PROJECT, COL_PROJECT_EVENT, AdministrationConstants.COL_FILE,
                  COL_CAPTION));

      for (final FileInfo fileInfo : ((FileCollector) widget).getFiles()) {
        FileUtils.uploadFile(fileInfo, new Callback<Long>() {

          @Override
          public void onSuccess(Long result) {
            List<String> values = Lists.newArrayList(BeeUtils.toString(getProjectId()),
                BeeUtils.toString(rowId), BeeUtils.toString(result), fileInfo.getCaption());

            Queries.insert(VIEW_PROJECT_FILES, columns, values, null, new RowCallback() {

              @Override
              public void onSuccess(BeeRow row) {
                counter.set(counter.get() + 1);
                if (counter.get() == ((FileCollector) widget).getFiles().size()) {
                  ((FileCollector) widget).clear();
                }
              }
            });
          }
        });
      }
    }
  }

  private Long getProjectId() {
    return projectId;
  }

}
