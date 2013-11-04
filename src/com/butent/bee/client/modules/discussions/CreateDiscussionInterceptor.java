package com.butent.bee.client.modules.discussions;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.richtext.RichTextEditor;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

class CreateDiscussionInterceptor extends AbstractFormInterceptor {

  private static final String WIDGET_ACCESSIBILITY = "Accessibility";
  private static final String WIDGET_DESCRIPTION = "Description";
  private static final String WIDGET_FILES = "Files";
  private static final String WIDGET_MEMBERS = "Members";

  CreateDiscussionInterceptor() {
    super();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof FileCollector) {
      ((FileCollector) widget).bindDnd(getFormView());
    }

    if (BeeUtils.same(name, WIDGET_ACCESSIBILITY) && widget instanceof InputBoolean) {
      final InputBoolean ac = (InputBoolean) widget;
      ac.setValue(BeeConst.STRING_TRUE);
      
      ac.addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          MultiSelector ms = (MultiSelector) getFormView().getWidgetByName(WIDGET_MEMBERS);
          ms.setEnabled(!BeeUtils.toBoolean(ac.getValue()));
        }
      });
    }
    
    if (BeeUtils.same(name, WIDGET_MEMBERS) && widget instanceof MultiSelector) {
      MultiSelector ms = (MultiSelector) widget;
      ms.setEnabled(false);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CreateDiscussionInterceptor();
  }

  @Override
  public void onReadyForInsert(final ReadyForInsertEvent event) {
    event.consume();
    IsRow activeRow = getFormView().getActiveRow();
    
    boolean discussPublic = BeeUtils.toBoolean(
        ((InputBoolean) getFormView().getWidgetByName(WIDGET_ACCESSIBILITY)).getValue());
    
    String description = ((RichTextEditor) getFormView().getWidgetByName(WIDGET_DESCRIPTION))
        .getValue();
    
    if (!discussPublic && BeeUtils.isEmpty(activeRow.getProperty(PROP_MEMBERS))) {
      event.getCallback().onFailure(Localized.getConstants().discussSelectMembers());
      return;
    }
    
    BeeRow newRow = DataUtils.cloneRow(activeRow);
    
    if (!BeeUtils.isEmpty(description)) {
      Data.setValue(VIEW_DISCUSSIONS, newRow, COL_DESCRIPTION, description);
    }
    
    Data.setValue(VIEW_DISCUSSIONS, newRow, COL_ACCESSIBILITY, BeeUtils.toInt(discussPublic));

    BeeRowSet rowSet =
        Queries.createRowSetForInsert(VIEW_DISCUSSIONS, getFormView().getDataColumns(), newRow,
            null, true);
    ParameterList args = DiscussionsKeeper.createDiscussionRpcParameters(DiscussionEvent.CREATE);
    args.addDataItem(VAR_DISCUSSION_DATA, Codec.beeSerialize(rowSet));

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          event.getCallback().onFailure(response.getErrors());
        } else if (response.hasResponse(String.class)) {
          List<Long> discussions = DataUtils.parseIdList((String) response.getResponse());

          if (discussions.isEmpty()) {
            event.getCallback().onFailure(Localized.getConstants().discussNotCreated());
            return;
          }

          createFiles(discussions);

          event.getCallback().onSuccess(null);

          String message = Localized.getConstants().discussCreatedNewDiscussion();
          GridView gridView = getGridView();
          if (gridView != null) {
            gridView.notifyInfo(message);
            gridView.getViewPresenter().handleAction(Action.REFRESH);
          } else {
            BeeKeeper.getScreen().notifyInfo(message);
          }
        } else {
          event.getCallback().onFailure("Unknown response");
        }
      }
    });
  }

  private void createFiles(final List<Long> discussions) {
   
    Widget widget = getFormView().getWidgetByName(WIDGET_FILES);

    if (widget instanceof FileCollector && !((FileCollector) widget).isEmpty()) {
      List<NewFileInfo> files = Lists.newArrayList(((FileCollector) widget).getFiles());
      
      final List<BeeColumn> columns =
          Data.getColumns(VIEW_DISCUSSIONS_FILES, Lists.newArrayList(COL_DISCUSSION, COL_FILE,
              COL_CAPTION));

      for (final NewFileInfo fileInfo : files) {
        FileUtils.uploadFile(fileInfo, new Callback<Long>() {

          @Override
          public void onSuccess(Long result) {
            for (long discussionId : discussions) {
              List<String> values =
                  Lists.newArrayList(BeeUtils.toString(discussionId), BeeUtils.toString(result),
                      fileInfo.getCaption());
              Queries.insert(VIEW_DISCUSSIONS_FILES, columns, values);
            }
          }

        });
      }

      ((FileCollector) widget).clear();
    }
  }
}
