package com.butent.bee.client.modules.discussions;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.discussions.DiscussionsUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class DiscussionInterceptor extends AbstractFormInterceptor {

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
            // doEvent(event);
          }

        }));
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new DiscussionInterceptor();
  }

  private boolean isEventEnabled(DiscussionEvent event, Integer status, Long owner) {

    if (event == null || status == null || owner == null || !isMember(userId)) {
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
        DiscussionsUtils.getDiscussionMembers(getActiveRow(), getFormView().getDataColumns());

    if (BeeUtils.isEmpty(members)) {
      return false;
    }

    return members.contains(user);
  }

  private static boolean isOwner(long user, long owner) {
    return user == owner;
  }

}
