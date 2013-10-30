package com.butent.bee.client.modules.discussions;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

class RowTransformHandler implements RowTransformEvent.Handler {

  private final List<String> discussionColumns = Lists.newArrayList(COL_SUBJECT);

  private DataInfo discussionsViewInfo;

  @Override
  public void onRowTransform(RowTransformEvent event) {
    if (event.hasView(VIEW_DISCUSSIONS)) {
      event.setResult(BeeUtils.joinWords(DataUtils.join(getDiscussionsViewInfo(), event.getRow(),
          discussionColumns, BeeConst.STRING_SPACE), getDiscussionStatus(event.getRow())));
    }
  }

  private String getDiscussionStatus(BeeRow row) {
    DiscussionStatus status =
        NameUtils.getEnumByIndex(DiscussionStatus.class, row.getInteger(getDiscussionsViewInfo()
            .getColumnIndex(COL_STATUS)));
    return (status == null) ? null : status.getCaption();
  }

  private DataInfo getDiscussionsViewInfo() {
    if (this.discussionsViewInfo == null) {
      this.discussionsViewInfo = Data.getDataInfo(VIEW_DISCUSSIONS);
    }

    return this.discussionsViewInfo;
  }

}
