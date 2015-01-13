package com.butent.bee.client.modules.projects;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.PhotoRenderer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.values.Position;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants.ProjectEvent;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

class ProjectEventsHandler extends Flow implements Presenter {
  private static final LocalizableConstants LC = Localized.getConstants();
  private static final Set<Action> ENABLED_ACTIONS = Sets.newHashSet(Action.REFRESH);

  private static final String STYLE_PREFIX = ProjectsKeeper.STYLE_PREFIX + "Events-";
  private static final String STYLE_HEADER = STYLE_PREFIX + "header";
  private static final String STYLE_CONTENT = STYLE_PREFIX + "content";
  private static final String STYLE_COMMENT_ROW = STYLE_CONTENT + "-comment-row";
  private static final String STYLE_COMMENT_ROW_PHOTO = STYLE_COMMENT_ROW + "-Photo";
  private static final String STYLE_COMMENT_ROW_PUBLISHER = STYLE_COMMENT_ROW + "-Publisher";
  private static final String STYLE_COMMENT_ROW_COMMENT = STYLE_COMMENT_ROW + "-Comment";
  private static final String STYLE_CONTENT_PHOTO = STYLE_CONTENT + "-Photo";

  private final BeeLogger logger = LogUtils.getLogger(ProjectEventsHandler.class);

  private IsRow projectData;
  private HeaderView headerView;
  private Flow content;
  private String caption;

  @Override
  public void handleAction(Action action) {
    switch (action) {
      case REFRESH:
        refresh();
        break;

      default:
        break;
    }
  }

  @Override
  public String getCaption() {
    return caption;
  }

  @Override
  public String getEventSource() {
    return null;
  }

  @Override
  public HeaderView getHeader() {
    return headerView;
  }

  @Override
  public View getMainView() {
    return null;
  }

  @Override
  public void onViewUnload() {
  }

  @Override
  public void setEventSource(String eventSource) {
  }

  public void create(HasWidgets widget, IsRow prjData) {
    clear();
    Assert.notNull(widget);
    setProjectData(prjData);
    setCaption(LC.prjComments());
    createHeaderView();
    createContent();
    refresh();
    widget.add(this);
  }

  public IsRow getProjectData() {
    return projectData;
  }

  public void refresh() {
    if (content == null) {
      logger.warning("Widget ", getId(), "not created");
    }

    content.clear();

    getData(content);
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setProjectData(IsRow projectData) {
    this.projectData = projectData;
  }

  private void createHeaderView() {
    headerView = new HeaderImpl();

    headerView.create(getCaption(), true, true, VIEW_PROJECT_EVENTS, null,
        ENABLED_ACTIONS, null, null);
    headerView.addStyleName(STYLE_HEADER);
    headerView.setViewPresenter(this);

    String label = ProjectEvent.COMMENT.getCommandLabel();
    FontAwesome icon = ProjectEvent.COMMENT.getCommandIcon();

    IdentifiableWidget button = icon != null ? new FaLabel(icon) : new Button(label);
    ((HasClickHandlers) button).addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent arg) {

      }
    });

    if (button instanceof FaLabel) {
      ((FaLabel) button).setTitle(label);
    }

    headerView.addCommandItem(button);

    add(headerView);
  }

  private void createContent() {
    content = new Flow(STYLE_CONTENT);
    StyleUtils.setProperty(content, StyleUtils.STYLE_POSITION, Position.ABSOLUTE);

    if (headerView != null) {
      StyleUtils.setTop(content, headerView.getHeight());
    }

    add(content);
  }

  private void getData(final HasWidgets cont) {
    IsRow prjData = getProjectData();

    if (prjData == null) {
      return;
    }

    long prjId = prjData.getId();

    if (!DataUtils.isId(prjId)) {
      return;
    }
    
    List<String> columns = Lists.newArrayList(COL_PUBLISHER,
        ALS_PUBLISHER_FIRST_NAME, ALS_PUBLISHER_LAST_NAME, ClassifierConstants.COL_PHOTO,
        COL_PUBLISH_TIME, COL_COMMENT, COL_EVENT);
    Filter filter = Filter.equals(COL_PROJECT, prjId);
    
    Order order = Order.ascending(COL_PUBLISH_TIME);
    
    Queries.getRowSet(VIEW_PROJECT_EVENTS, columns, filter, order, 
        getProjectEventsCallback(cont));
  }

  private static RowSetCallback getProjectEventsCallback(final HasWidgets cont) {
    return new RowSetCallback() {

      @Override
      public void onSuccess(BeeRowSet result) {
        if (result == null) {
          return;
        }

        for (IsRow row : result) {
          drawCommentBar(cont, row, result);
        }

      }
    };
  }

  private static void drawCommentBar(HasWidgets cont, IsRow row, BeeRowSet columns) {
    Flow commentBar = new Flow();

    commentBar.addStyleName(STYLE_COMMENT_ROW);

    drawPublisher(commentBar, row, columns);
    drawCommentContent(commentBar, row, columns);

    cont.add(commentBar);
  }

  private static void drawCommentContent(Flow commentBar, IsRow row, BeeRowSet columns) {
    int idxComment = columns.getColumnIndex(COL_COMMENT);

    Flow colComment = new Flow();
    colComment.addStyleName(STYLE_COMMENT_ROW_COMMENT);

    String comment = BeeConst.STRING_EMPTY;

    if (!BeeUtils.isNegative(idxComment)) {
      comment = row.getString(idxComment);
    }

    if (!BeeUtils.isEmpty(comment)) {
      colComment.add(createTextCell(COL_COMMENT, comment));
    }

    commentBar.add(colComment);
  }

  private static void drawPublisher(Flow commentBar, IsRow row, BeeRowSet columns) {
    int idxPhoto = columns.getColumnIndex(ClassifierConstants.COL_PHOTO);
    int idxFName = columns.getColumnIndex(ALS_PUBLISHER_FIRST_NAME);
    int idxLName = columns.getColumnIndex(ALS_PUBLISHER_LAST_NAME);
    int idxDate = columns.getColumnIndex(COL_PUBLISH_TIME);

    Flow colPhoto = new Flow();
    colPhoto.addStyleName(STYLE_COMMENT_ROW_PHOTO);

    if (!BeeUtils.isNegative(idxPhoto)) {
      renderPhoto(colPhoto, row.getString(idxPhoto));
    }
    
    commentBar.add(colPhoto);
    
    Flow colPublisher = new Flow();
    colPublisher.addStyleName(STYLE_COMMENT_ROW_PUBLISHER);

    String fullName = BeeConst.STRING_EMPTY;

    if (!BeeUtils.isNegative(idxFName)) {
      fullName = row.getString(idxFName);
    }

    if (!BeeUtils.isNegative(idxLName)) {
      fullName = BeeUtils.joinWords(fullName, row.getString(idxLName));
    }
    
    if (!BeeUtils.isEmpty(fullName)) {
      colPublisher.add(createTextCell(COL_PUBLISHER, fullName));
    }

    DateTime publishTime = null;

    if (!BeeUtils.isNegative(idxDate)) {
      publishTime = row.getDateTime(idxDate);
    }

    if (publishTime != null) {
      colPublisher.add(createTextCell(COL_PUBLISH_TIME, Format.getDefaultDateTimeFormat()
          .format(publishTime)));
    }

    commentBar.add(colPublisher);

  }

  private static void renderPhoto(Flow container, String photoPath) {
    if (BeeUtils.isEmpty(photoPath)) {
      return;
    }

    Image image = new Image(PhotoRenderer.getUrl(photoPath));
    image.addStyleName(STYLE_CONTENT_PHOTO);
    container.add(image);
  }

  private static Widget createTextCell(String styleSuffix, String text) {
    Widget widget = new CustomDiv(STYLE_CONTENT + BeeConst.STRING_MINUS + styleSuffix);
    if (!BeeUtils.isEmpty(text)) {
      widget.getElement().setInnerHTML(text);
    }

    return widget;
  }

}
