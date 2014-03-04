package com.butent.bee.client.modules.tasks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.Callback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RequestBuilder extends AbstractFormInterceptor {

  private final Map<Long, NewFileInfo> filesToUpload = Maps.newHashMap();

  public RequestBuilder(Map<Long, NewFileInfo> files) {
    if (files != null) {
      filesToUpload.putAll(files);
    }
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof FileCollector) {
      ((FileCollector) widget).bindDnd(getFormView());
      ((FileCollector) widget).addFiles(filesToUpload.values());
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new RequestBuilder(null);
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, final ReadyForInsertEvent event) {
    event.consume();
    String viewName = getFormView().getViewName();

    Queries.insert(viewName, event.getColumns(), event.getValues(), event.getChildren(),
        new RowInsertCallback(viewName, event.getSourceId()) {
          @Override
          public void onSuccess(BeeRow result) {
            super.onSuccess(result);
            event.getCallback().onSuccess(result);
            createFiles(result.getId());
          }
        });
  }

  private void createFiles(Long requestId) {
    Widget widget = getFormView().getWidgetByName("Files");

    if (widget instanceof FileCollector && !((FileCollector) widget).isEmpty()) {
      final List<BeeColumn> columns = Data.getColumns(TBL_REQUEST_FILES,
          Lists.newArrayList(COL_REQUEST, COL_FILE, COL_CAPTION));

      for (final NewFileInfo fileInfo : ((FileCollector) widget).getFiles()) {
        Long file = null;

        for (Entry<Long, NewFileInfo> fileToUpload : filesToUpload.entrySet()) {
          if (BeeUtils.same(key(fileInfo), key(fileToUpload.getValue()))) {
            file = fileToUpload.getKey();
            break;
          }
        }
        final String request = BeeUtils.toString(requestId);
        final String fileName = BeeUtils.notEmpty(fileInfo.getCaption(), fileInfo.getName());

        if (DataUtils.isId(file)) {
          Queries.insert(TBL_REQUEST_FILES, columns,
              Lists.newArrayList(request, BeeUtils.toString(file), fileName));
        } else {
          FileUtils.uploadFile(fileInfo, new Callback<Long>() {
            @Override
            public void onSuccess(Long result) {
              Queries.insert(TBL_REQUEST_FILES, columns,
                  Lists.newArrayList(request, BeeUtils.toString(result), fileName));
            }
          });
        }
      }
      ((FileCollector) widget).clear();
    }
  }

  private static String key(NewFileInfo fileInfo) {
    return BeeUtils.joinWords(fileInfo.getName(), fileInfo.getSize());
  }
}
