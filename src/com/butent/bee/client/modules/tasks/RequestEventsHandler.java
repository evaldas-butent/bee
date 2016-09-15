package com.butent.bee.client.modules.tasks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.eventsboard.EventsBoard;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.html.builder.Factory;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskEvent;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RequestEventsHandler extends EventsBoard {
  private static final Dictionary LC = Localized.dictionary();

  private final Set<Action> enabledActions = Sets.newHashSet(Action.REFRESH, Action.ADD);
  private final HeaderView header;
  private boolean eventsOrder;

  public RequestEventsHandler(HeaderView header, boolean eventsOrder) {
    this.header = header;
    this.eventsOrder = eventsOrder;
  }

  @Override
  public String getViewKey() {
    return null;
  }

  @Override
  public String getCaption() {
    return LC.prjComments();
  }

  @Override
  protected void afterCreateEventNoteCell(BeeRowSet rs, BeeRow row, Flow widget, Flow cell) {
    int idxProp = rs.getColumnIndex(COL_EVENT_PROPERTIES);

    if (BeeConst.isUndef(idxProp)) {
      return;
    }

    String prop = row.getString(idxProp);

    if (BeeUtils.isEmpty(prop)) {
      return;
    }

    List<String> pairedData = Lists.newArrayList(Codec.beeDeserializeCollection(prop));

    if (pairedData.size() < 2) {
      return;
    }

    Flow rowCell = createEventRowCell(cell, COL_EVENT_PROPERTIES, null, false);

    if (Objects.equals(pairedData.get(0), "0")) {
      rowCell.add(createLinkedTask(prop));
    } else {
      String html = getCommentCellHtml(pairedData);
      if (BeeUtils.isEmpty(html)) {
        return;
      }

      rowCell.add(createCellHtmlItem(COL_EVENT_PROPERTIES, html));
    }

  }

  @Override
  public void createHeaderView() {
    if (header != null) {
      IdentifiableWidget add = null;

      if (getEnabledActions().contains(Action.ADD) && getAddEventActionWidget() != null) {
        add = getAddEventActionWidget();
        if (add instanceof HasClickHandlers) {

          ((HasClickHandlers) add).addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
              handleAction(Action.ADD);
            }
          });
        }
      }

      if (add != null) {
        header.addCommandItem(add);
      }

      IdentifiableWidget refresh = getAddEventActionRefresh();

      if (refresh instanceof HasClickHandlers) {

        ((HasClickHandlers) refresh).addClickHandler(new ClickHandler() {

          @Override
          public void onClick(ClickEvent arg0) {
            handleAction(Action.REFRESH);
          }
        });
      }

      if (refresh != null) {
        header.addCommandItem(refresh);
      }
    }
  }

  public void setEventsOrder(boolean eventsOrder) {
    this.eventsOrder = eventsOrder;
  }

  @Override
  protected IdentifiableWidget getAddEventActionWidget() {
    FaLabel label = new FaLabel(FontAwesome.COMMENT_O);
    label.setTitle(Localized.dictionary().crmActionComment());
    return label;
  }

  @Override
  protected String getAddEventFromName() {
    return FORM_NEW_REQUEST_COMMENT;
  }

  @Override
  protected Set<Action> getEnabledActions() {
    return enabledActions;
  }

  @Override
  protected Order getEventsDataOrder() {
    return new Order(COL_PUBLISH_TIME, eventsOrder);
  }

  @Override
  protected String getEventsDataViewName() {
    return VIEW_REQUEST_EVENTS;
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
  protected AbstractFormInterceptor getNewEventFormInterceptor() {
    return new NewRequestCommentForm(getRelatedId());
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

  @Override
  protected String getRelatedColumnName() {
    return COL_REQUEST;
  }

  private static IdentifiableWidget getAddEventActionRefresh() {
    FaLabel label = new FaLabel(FontAwesome.REFRESH);
    label.setTitle(Localized.dictionary().actionRefreshComments());
    return label;
  }

  private static Widget createLinkedTask(String prop) {
    Widget widget = new Flow("bee-crm-taskEvent-EventNote");
    Map<String, String> data = Codec.deserializeLinkedHashMap(prop);

    if (data == null) {
      return widget;
    }

    if (!data.containsKey(BeeUtils.toString(TaskEvent.CREATE.ordinal()))) {
      return widget;
    }

    List<Long> extTasks =
        DataUtils.parseIdList(data.get(BeeUtils.toString(TaskEvent.CREATE.ordinal())));

    widget.getElement().setInnerHTML(Localized.dictionary().crmTasksDelegatedTasks());

    for (final Long extTaskId : extTasks) {
      InternalLink url = new InternalLink(BeeUtils.toString(extTaskId));
      url.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          RowEditor.open(VIEW_TASKS, extTaskId, Opener.NEW_TAB);
        }
      });

      ((Flow) widget).add(url);
    }

    return widget;
  }

  private static String getCommentCellHtml(List<String> pairedData) {
    Map<String, String> viewOldList = Codec.deserializeLinkedHashMap(pairedData.get(0));
    Map<String, String> viewNewList = Codec.deserializeLinkedHashMap(pairedData.get(1));

    String html = BeeConst.STRING_EMPTY;

    for (String view : viewOldList.keySet()) {
      Map<String, String> newChanges = Codec.deserializeLinkedHashMap(viewNewList.get(view));
      final Map<String, String> oldChanges = Codec.deserializeLinkedHashMap(viewOldList.get(view));

      if (newChanges.isEmpty() && oldChanges.isEmpty()) {
        continue;
      }

      for (final String col : oldChanges.keySet()) {
        String oldValue = oldChanges.get(col);
        String newValue = newChanges.get(col);

        String columnLabel = col;

        columnLabel = Data.getColumnLabel(view, col);

        if (BeeUtils.isEmpty(oldValue) && BeeUtils.isEmpty(newValue)) {
          continue;
        }

        String direction = "->";

        if (BeeUtils.isEmpty(oldValue)) {
          oldValue = LC.filterNullLabel();
        }

        if (BeeUtils.isEmpty(newValue)) {
          newValue = LC.filterNullLabel();
        }

        if (BeeUtils.same(oldValue, newValue)) {
          newValue = BeeConst.STRING_EMPTY;
          direction = BeeConst.STRING_EMPTY;
        }

        html +=
            BeeUtils.joinWords(columnLabel, BeeConst.STRING_COLON, oldValue,
                direction, newValue, Factory.br().build());
      }
    }
    return html;
  }

}