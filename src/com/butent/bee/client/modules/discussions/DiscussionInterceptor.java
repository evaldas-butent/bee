package com.butent.bee.client.modules.discussions;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.FileGroup;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.DndTarget;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.render.PhotoRenderer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.discussions.DiscussionsUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

class DiscussionInterceptor extends AbstractFormInterceptor {
  
  private static final class CommentDialog extends DialogBox {

    private static final String STYLE_DIALOG = DISCUSSIONS_STYLE_PREFIX + "commentDialog";
    private static final String STYLE_CELL = "Cell";

    private CommentDialog(String caption) {
      super(caption, STYLE_DIALOG);

      addDefaultCloseBox();

      HtmlTable container = new HtmlTable();
      container.addStyleName(STYLE_DIALOG + "-container");

      setWidget(container);
    }

    private void addAction(String caption, ScheduledCommand command) {
      String styleName = STYLE_DIALOG + "-action";

      Button button = new Button(caption, command);
      button.addStyleName(styleName);

      HtmlTable table = getContainer();
      int row = table.getRowCount();
      int col = 0;

      table.setWidget(row, col, button);

      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);
      table.getCellFormatter().setHorizontalAlignment(row, col, TextAlign.CENTER);

      table.getCellFormatter().setColSpan(row, col, 2);
    }

    private String addComment(boolean required) {
      String styleName = STYLE_DIALOG + "commentLabel";
      Label label = new Label(Localized.getConstants().discussComment());
      label.addStyleName(styleName);
      if (required) {
        label.addStyleName(StyleUtils.NAME_REQUIRED);
      }

      HtmlTable table = getContainer();
      int row = table.getRowCount();
      int col = 0;

      table.setWidget(row, col, label);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);
      col++;

      InputArea input = new InputArea();
      styleName = STYLE_DIALOG + "-commentArea";

      input.addStyleName(styleName);

      table.setWidget(row, col, input);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);

      return input.getId();
    }

    private String addFileCollector() {
      HtmlTable table = getContainer();
      int row = table.getRowCount();
      int col = 0;

      String styleName = STYLE_DIALOG + "-filesLabel";
      Label label = new Label(Localized.getConstants().discussFiles());
      label.addStyleName(styleName);

      table.setWidget(row, col, label);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);
      col++;

      styleName = STYLE_DIALOG + "fileCillector";
      FileCollector collector = new FileCollector(new Image(Global.getImages().attachment()));
      collector.addStyleName(styleName);

      table.setWidget(row, col, collector);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);

      Widget panel = getWidget();
      if (panel instanceof DndTarget) {
        collector.bindDnd((DndTarget) panel);
      }

      return collector.getId();
    }

    private void dispaly() {
      center();
      UiHelper.focus(getContent());
    }

    @SuppressWarnings("unused")
    private void display(String focusId) {
      center();
      UiHelper.focus(getChild(focusId));
    }

    private Widget getChild(String id) {
      return DomUtils.getChildQuietly(getContent(), id);
    }

    private String getComment(String id) {
      Widget child = getChild(id);
      if (child instanceof InputArea) {
        return ((InputArea) child).getValue();
      } else {
        return null;
      }
    }

    private HtmlTable getContainer() {
      return (HtmlTable) getContent();
    }

    private List<NewFileInfo> getFiles(String id) {
      Widget child = getChild(id);
      if (child instanceof FileCollector) {
        return ((FileCollector) child).getFiles();
      } else {
        return Lists.newArrayList();
      }
    }
  }

  private static final String STYLE_COMMENT = DISCUSSIONS_STYLE_PREFIX + "comment-";
  private static final String STYLE_COMMENT_ROW = STYLE_COMMENT + "row";
  private static final String STYLE_COMMENT_COL = STYLE_COMMENT + "col-";
  private static final String STYLE_COMMENT_FILES = STYLE_COMMENT + "files";

  private final List<String> relations = Lists.newArrayList(PROP_COMPANIES, PROP_PERSONS,
      PROP_APPOINTMENTS, PROP_TASKS, PROP_DOCUMENTS);
  private final long userId;

  DiscussionInterceptor() {
    super();
    this.userId = BeeKeeper.getUser().getUserId();
  }
  
  @Override
  public void afterRefresh(FormView form, IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (row == null) {
      return;
    }

    Integer status = row.getInteger(form.getDataIndex(COL_STATUS));
    Long owner = row.getLong(form.getDataIndex(COL_OWNER));
    
    for (final DiscussionEvent event : DiscussionEvent.values()) {
      String label = event.getCommandLabel();

      if (!BeeUtils.isEmpty(label) && isEventEnabled(event, status, owner)) {
        header.addCommandItem(new Button(label, new ClickHandler() {

          @Override
          public void onClick(ClickEvent e) {
            doEvent(event);
          }

        }));
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new DiscussionInterceptor();
  }

  @Override
  public void onSaveChanges(SaveChangesEvent event) {
    IsRow oldRow = event.getOldRow();
    IsRow newRow = event.getNewRow();

    if (oldRow == null || newRow == null) {
      return;
    }

    if (event.isEmpty() && DiscussionsUtils.sameMembers(oldRow, newRow)
        && getUpdatedRelations(oldRow, newRow).isEmpty()) {
      return;
    }

    event.consume();

    ParameterList params = createParams(DiscussionEvent.MODIFY, null);

    sendRequest(params, new Callback<ResponseObject>() {

      @Override
      public void onSuccess(ResponseObject result) {
        BeeRow data = getResponseRow(DiscussionEvent.MODIFY.getCaption(), result, this);

        if (data != null) {
          BeeKeeper.getBus().fireEvent(new RowUpdateEvent(VIEW_DISCUSSIONS, data));
        }
      }
    });
  }

  @Override
  public boolean onStartEdit(final FormView form, final IsRow row, ScheduledCommand focusCommand) {
    Long owner = row.getLong(form.getDataIndex(COL_OWNER));

    form.setEnabled(isOwner(userId, owner));

    BeeRow visitedRow = DataUtils.cloneRow(row);
    BeeRowSet rowSet = new BeeRowSet(form.getViewName(), form.getDataColumns());
    rowSet.addRow(visitedRow);

    ParameterList params = DiscussionsKeeper.createDiscussionRpcParameters(DiscussionEvent.VISIT);
    params.addDataItem(VAR_DISCUSSION_DATA, Codec.beeSerialize(rowSet));
    params.addDataItem(VAR_DISCUSSION_USERS, getDiscussionMembers(form, row));

    sendRequest(params, new Callback<ResponseObject>() {
      @Override
      public void onFailure(String... reason) {
        form.updateRow(row, true);
        form.notifySevere(reason);
      }

      @Override
      public void onSuccess(ResponseObject result) {
        BeeRow data = getResponseRow(DiscussionEvent.VISIT.getCaption(), result, this);
        if (data == null) {
          return;
        }

        BeeKeeper.getBus().fireEvent(new RowUpdateEvent(VIEW_DISCUSSIONS, data));

        Widget fileWidget = form.getWidgetByName(PROP_FILES);
        if (fileWidget instanceof FileGroup) {
          ((FileGroup) fileWidget).clear();
        }

        List<StoredFile> files = StoredFile.restoreCollection(data.getProperty(PROP_FILES));
        if (!files.isEmpty()) {
          if (fileWidget instanceof FileGroup) {
            for (StoredFile file : files) {
              if (file.getRelatedId() == null) {
                ((FileGroup) fileWidget).addFile(file);
              }
            }
          }
        }
        String comments = data.getProperty(PROP_COMMENTS);

        if (!BeeUtils.isEmpty(comments)) {
          showCommentsAndMarks(form, BeeRowSet.restore(comments), files);
        }
        
        form.getWidgetByName(COL_DESCRIPTION).getElement().setInnerHTML(
            data.getString(form.getDataIndex(COL_DESCRIPTION)));

        form.updateRow(data, true);
      }
    });
    return false;
  }

  private static Widget createCommentCell(String colName, String value) {
    Widget widget = new CustomDiv(STYLE_COMMENT + colName);
    if (!BeeUtils.isEmpty(value)) {
      widget.getElement().setInnerHTML(value);
    }

    return widget;
  }

  private static List<StoredFile> filterCommentFiles(List<StoredFile> input, long commentId) {
    if (input.isEmpty()) {
      return input;
    }

    List<StoredFile> result = Lists.newArrayList();

    for (StoredFile file : input) {
      Long id = file.getRelatedId();
      if (id != null && id == commentId) {
        result.add(file);
      }
    }
    return result;
  }

  private static String getDiscussionMembers(FormView form, IsRow row) {
    return DataUtils.buildIdList(DiscussionsUtils.getDiscussionMembers(row, form.getDataColumns()));
  }

  private static BeeRow getResponseRow(String caption, ResponseObject ro, Callback<?> callback) {
    if (!Queries.checkResponse(caption, VIEW_DISCUSSIONS, ro, BeeRow.class, callback)) {
      return null;
    }

    BeeRow row = BeeRow.restore((String) ro.getResponse());
    if (row == null && callback != null) {
      callback.onFailure(caption, VIEW_DISCUSSIONS, "cannot restore row");
    }

    return row;
  }

  private static boolean isOwner(long user, long owner) {
    return user == owner;
  }

  private static boolean isPhoto(BeeRow row, BeeRowSet rowSet) {
    return !BeeUtils.isEmpty(row.getString(rowSet.getColumnIndex(CommonsConstants.COL_PHOTO)));
  }

  private static void sendRequest(ParameterList params, final Callback<ResponseObject> callback) {
    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasErrors()) {
          if (callback != null) {
            callback.onFailure(response.getErrors());
          }
        } else {
          if (callback != null) {
            callback.onSuccess(response);
          }
        }
      }
    });
  }
  
  private static void showComment(Flow panel, BeeRow row, List<BeeColumn> columns,
      List<StoredFile> files, boolean renderPhoto) {

    Flow container = new Flow();
    container.addStyleName(STYLE_COMMENT_ROW);
    
    Flow colPhoto = new Flow();
    colPhoto.addStyleName(STYLE_COMMENT_ROW + CommonsConstants.COL_PHOTO);
    
    if (renderPhoto) {
      String photo = row.getString(DataUtils.getColumnIndex(CommonsConstants.COL_PHOTO, columns));
      if (!BeeUtils.isEmpty(photo)) {
        Image image = new Image(PhotoRenderer.getUrl(photo));
        image.addStyleName(STYLE_COMMENT +  CommonsConstants.COL_PHOTO);
        colPhoto.add(image);
      }
    }
    
    container.add(colPhoto);

    Flow colPublisher = new Flow();
    colPublisher.addStyleName(STYLE_COMMENT_COL + COL_PUBLISHER);
    
    DateTime publishTime = row.getDateTime(DataUtils.getColumnIndex(COL_PUBLISH_TIME, columns));

    if (publishTime != null) {
      colPublisher.add(createCommentCell(COL_PUBLISH_TIME, Format.getDefaultDateTimeFormat()
          .format(publishTime)));
    }

    String publisher = BeeUtils.joinWords(
        row.getString(DataUtils.getColumnIndex(COL_PUBLISHER_FIRST_NAME, columns)),
        row.getString(DataUtils.getColumnIndex(COL_PUBLISHER_LAST_NAME, columns)));

    if (!BeeUtils.isEmpty(publisher)) {
      colPublisher.add(createCommentCell(COL_PUBLISHER, publisher));
    }

    container.add(colPublisher);

    Flow colComment = new Flow();
    colComment.addStyleName(STYLE_COMMENT_COL + COL_COMMENT_TEXT);

    String text = row.getString(DataUtils.getColumnIndex(COL_COMMENT_TEXT, columns));

    if (!BeeUtils.isEmpty(text)) {
      colComment.add(createCommentCell(COL_COMMENT, text));
    }

    container.add(colComment);

    panel.add(container);

    if (!files.isEmpty()) {
      Simple fileContainer = new Simple();
      fileContainer.addStyleName(STYLE_COMMENT_FILES);
      
      FileGroup fileGroup = new FileGroup();
      fileGroup.addFiles(files);

      fileContainer.setWidget(fileGroup);
      panel.add(fileContainer);
    }
  }

  private static void showCommentsAndMarks(FormView form, BeeRowSet rowSet,
      List<StoredFile> files) {
    Widget widget = form.getWidgetByName(VIEW_DISCUSSIONS_COMMENTS);

    if (!(widget instanceof Flow) || DataUtils.isEmpty(rowSet)) {
      return;
    }

    Flow panel = (Flow) widget;
    panel.clear();
   
    for (BeeRow row : rowSet.getRows()) {
      showComment(panel, row, rowSet.getColumns(), filterCommentFiles(files, row.getId()),
          isPhoto(row, rowSet));
    }
    
    if (panel.getWidgetCount() > 0 && form.asWidget().isVisible()) {
      final Widget last = panel.getWidget(panel.getWidgetCount() - 1);
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          last.getElement().scrollIntoView();
        }
      });
    }
  }

  private static void showError(String message) {
    Global.showError(Localized.getConstants().error(), Lists.newArrayList(message));
  }

  private ParameterList createParams(DiscussionEvent event, BeeRow newRow, String comment) {
    FormView form = getFormView();
    String viewName = form.getViewName();

    IsRow oldRow = form.getOldRow();

    BeeRowSet updated = DataUtils.getUpdated(viewName, form.getDataColumns(), oldRow, newRow,
        form.getChildrenForUpdate());

    if (!DataUtils.isEmpty(updated)) {
      BeeRow updRow = updated.getRow(0);

      for (int i = 0; i < updated.getNumberOfColumns(); i++) {
        int index = form.getDataIndex(updated.getColumnId(i));

        newRow.setValue(index, oldRow.getString(index));
        newRow.preliminaryUpdate(index, updRow.getString(i));
      }
    }

    BeeRowSet rowSet = new BeeRowSet(viewName, form.getDataColumns());
    rowSet.addRow(newRow);

    ParameterList params = DiscussionsKeeper.createDiscussionRpcParameters(event);
    params.addDataItem(VAR_DISCUSSION_DATA, Codec.beeSerialize(rowSet));
    params.addDataItem(VAR_DISCUSSION_USERS, getDiscussionMembers(form, oldRow));

    if (!BeeUtils.isEmpty(comment)) {
      params.addDataItem(VAR_DISCUSSION_COMMENT, comment);
    }

    return params;
  }

  private ParameterList createParams(DiscussionEvent event, String comment) {
    return createParams(event, getNewRow(), comment);
  }

  private void doActivate() {
    // TODO: do active deactivated discussion
  }

  private void doClose() {
    // TODO: do discussion status closed;
  }

  private void doComment(Long replayedCommentId) {
    final CommentDialog dialog = new CommentDialog(Localized.getConstants().discussComment());

    final String cid = dialog.addComment(true);
    final String fid = dialog.addFileCollector();

    dialog.addAction(Localized.getConstants().actionSave(), new ScheduledCommand() {

      @Override
      public void execute() {
        String comment = dialog.getComment(cid);
        
        if (BeeUtils.isEmpty(comment)) {
          showError(Localized.getConstants().crmEnterComment());
        }

        final long discussionId = getDiscussionId();

        ParameterList params = createParams(DiscussionEvent.COMMENT, comment);

        final List<NewFileInfo> files = dialog.getFiles(fid);

        dialog.close();
        
        sendRequest(params, new Callback<ResponseObject>() {
          @Override
          public void onFailure(String... reason) {
            getFormView().notifySevere(reason);
          }

          @Override
          public void onSuccess(ResponseObject result) {
            BeeRow data = getResponseRow(DiscussionEvent.COMMENT.getCaption(), result, this);
            if (data == null) {
              return;
            }

            onResponse(data);
            
            Long commentId = BeeUtils.toLongOrNull(data.getProperty(PROP_LAST_COMMENT));
            if (DataUtils.isId(commentId) && !files.isEmpty()) {
              sendFiles(files, discussionId, commentId);
            }
          }
          
        });
      }
    });

    dialog.dispaly();

    if (replayedCommentId == null) {
      // dielog.setCapt
      return;
    }
    // TODO: do replay discussion comment
  }

  private void doEvent(DiscussionEvent event) {
    if (!isEventEnabled(event, getStatus(), getOwner())) {
      showError(Localized.getConstants().actionNotAllowed());
    }

    switch (event) {
      case ACTIVATE:
        doActivate();
        break;
      case CLOSE:
        doClose();
        break;
      case COMMENT:
        doComment(null);
        break;
      case CREATE:
        break;
      case DEACTIVATE:
        break;
      case MARK:
        doMark(null);
        break;
      case MODIFY:
        break;
      case REPLY:
        break;
      case VISIT:
        break;
    }
  }
  
  private static void doMark(Long commentId) {
    if (commentId == null) {
      // TODO: do discussion mark
      return;
    }
    // TODO: do discussion comment mark
  }

  private long getDiscussionId() {
    return getFormView().getActiveRow().getId();
  }

  private Long getLong(String colName) {
    return getFormView().getActiveRow().getLong(getFormView().getDataIndex(colName));
  }

  private BeeRow getNewRow() {
    return DataUtils.cloneRow(getFormView().getActiveRow());
  }

  private Long getOwner() {
    return getLong(COL_OWNER);
  }

  private Integer getStatus() {
    return getFormView().getActiveRow().getInteger(getFormView().getDataIndex(COL_STATUS));
  }
  
  private List<String> getUpdatedRelations(IsRow oldRow, IsRow newRow) {
    List<String> updatedRelations = Lists.newArrayList();
    
    if (oldRow == null || newRow == null) {
      return updatedRelations;
    }
    
    for (String relation : relations) {
      if (!DataUtils.sameIdSet(oldRow.getProperty(relation), newRow.getProperty(relation))) {
        updatedRelations.add(relation);
      }
    }
    return updatedRelations; 
  }

  private boolean isEventEnabled(DiscussionEvent event, Integer status, Long owner) {   

    if (event == null || status == null || owner == null || (!isMember(userId) && !isPublic())) {
      return false;
    }

    switch (event) {
      case ACTIVATE:
        return DiscussionStatus.in(status, DiscussionStatus.INACTIVE);
      case CLOSE:
        return DiscussionStatus.in(status, DiscussionStatus.ACTIVE) && isOwner(userId, owner);
      case COMMENT:
        return DiscussionStatus.in(status, DiscussionStatus.ACTIVE);
      case CREATE:
        return false;
      case DEACTIVATE:
        return false;
      case MARK:
        return DiscussionStatus.in(status, DiscussionStatus.ACTIVE);
      case MODIFY:
        return isOwner(userId, owner);
      case REPLY:
        return false;
      case VISIT:
        return false;
    }

    return false;
  }

  private boolean isMember(Long user) {
    if (!DataUtils.isId(user)) {
      return false;
    }
    
    List<Long> members =
        DiscussionsUtils.getDiscussionMembers(getFormView().getActiveRow(), getFormView()
            .getDataColumns());

    if (BeeUtils.isEmpty(members)) {
      return false;
    }

    return members.contains(user);
  }

 
  private boolean isPublic() {
    return getFormView().getActiveRow().getBoolean(getFormView().getDataIndex(COL_ACCESSIBILITY));
  }

  private void onResponse(BeeRow data) {
    BeeKeeper.getBus().fireEvent(new RowUpdateEvent(VIEW_DISCUSSIONS, data));

    FormView form = getFormView();

    String comments = data.getProperty(PROP_COMMENTS);

    if (!BeeUtils.isEmpty(comments)) {
      List<StoredFile> files = StoredFile.restoreCollection(data.getProperty(PROP_FILES));
      showCommentsAndMarks(form, BeeRowSet.restore(comments), files);
    }
  }

  private void requeryComments(final long discussionId) {
    ParameterList params = DiscussionsKeeper.createArgs(SVC_GET_DISCUSSION_DATA);
    params.addDataItem(VAR_DISCUSSION_ID, discussionId);
    
    Callback<ResponseObject> callback = new Callback<ResponseObject>() {
      @Override
      public void onFailure(String... reason) {
        getFormView().notifySevere(reason);
      }

      @Override
      public void onSuccess(ResponseObject result) {
        if (getFormView().getActiveRow().getId() != discussionId) {
          return;
        }

        BeeRow data = getResponseRow(SVC_GET_DISCUSSION_DATA, result, this);
        if (data != null) {
          onResponse(data);
        }
      }
    };

    sendRequest(params, callback);
  }

  private void sendFiles(final List<NewFileInfo> files, final long discussionId,
      final long commentId) {
    
    final Holder<Integer> counter = Holder.of(0);
    
    final List<BeeColumn> columns =
        Data.getColumns(VIEW_DISCUSSIONS_FILES, Lists.newArrayList(COL_DISCUSSION, COL_COMMENT,
            COL_FILE, COL_CAPTION));

    for (final NewFileInfo fileInfo : files) {
      FileUtils.uploadFile(fileInfo, new Callback<Long>() {

        @Override
        public void onSuccess(Long result) {
          List<String> values = Lists.newArrayList(BeeUtils.toString(discussionId),
              BeeUtils.toString(commentId), BeeUtils.toString(result), fileInfo.getCaption());

          Queries.insert(VIEW_DISCUSSIONS_FILES, columns, values, null, new RowCallback() {
            @Override
            public void onSuccess(BeeRow row) {
              counter.set(counter.get() + 1);
              if (counter.get() == files.size()) {
                requeryComments(discussionId);
              }
            }
          });
        }

      });
    }
  }
}
