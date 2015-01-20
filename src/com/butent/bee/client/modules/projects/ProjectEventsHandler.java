package com.butent.bee.client.modules.projects;

import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.eventsboard.EventsBoard;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants.ProjectEvent;
import com.butent.bee.shared.ui.Action;

import java.util.Set;

class ProjectEventsHandler extends EventsBoard {
  private static final LocalizableConstants LC = Localized.getConstants();
  private static final Set<Action> ENABLED_ACTIONS = Sets.newHashSet(Action.REFRESH, Action.ADD);

  private static final String STYLE_PREFIX = ProjectsKeeper.STYLE_PREFIX + "Events-";

  @Override
  public String getCaption() {
    return LC.prjComments();
  }

  @Override
  protected IdentifiableWidget getAddEventActionWidget() {
    if (ProjectEvent.COMMENT.getCommandIcon() != null) {
      FaLabel label = new FaLabel(ProjectEvent.COMMENT.getCommandIcon());
      label.setTitle(ProjectEvent.COMMENT.getCommandLabel());
      return label;
    }
    return null;
  }

  @Override
  protected String getAddEventFromName() {
    return FORM_NEW_COMMENT;
  }

  @Override
  protected Set<Action> getEnabledActions() {
    return ENABLED_ACTIONS;
  }

  @Override
  protected AbstractFormInterceptor getNewEventFormInterceptor() {
    return new NewProjectComment(getRelatedId());
  }

  @Override
  protected Order getEventsDataOrder() {
    Order order = Order.ascending(COL_PUBLISH_TIME);
    return order;
  }

  @Override
  protected String getEventsDataViewName() {
    return VIEW_PROJECT_EVENTS;
  }

  @Override
  protected String getRelatedColumnName() {
    return COL_PROJECT;
  }

  @Override
  protected String getStylePrefix() {
    return STYLE_PREFIX;
  }

  @Override
  protected String getEventNoteColumnName() {
    return COL_COMMENT;
  }

  @Override
  protected String getEventTypeColumnName() {
    return COL_EVENT;
  }

  @Override
  protected String getPublisherPhotoColumnName() {
    return ClassifierConstants.COL_PHOTO;
  }

  @Override
  protected String getPublisherFirstNameColumnName() {
    return ALS_PUBLISHER_FIRST_NAME;
  }

  @Override
  protected String getPublisherLastNameColumnName() {
    return ALS_PUBLISHER_LAST_NAME;
  }

  @Override
  protected String getPublishTimeColumnName() {
    return COL_PUBLISH_TIME;
  }

}
