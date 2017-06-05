package com.butent.bee.client.modules.discussions;


import com.butent.bee.client.data.*;
import com.butent.bee.client.render.*;
import com.butent.bee.shared.data.*;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

public class DiscussModeRenderer extends AbstractRowModeRenderer {
    @Override
    public boolean hasUserProperty(IsRow row, Long userId) {
        return row.hasPropertyValue(PROP_USER, userId);
    }

    @Override
    public Long getLastUpdate(IsRow row, Long userId) {
        return Data.getLong(VIEW_DISCUSSIONS, row, ALS_LAST_COMMENT_PUBLISH_TIME);

    }

    @Override
    public Long getLastAccess(IsRow row, Long userId) {
        return row.getPropertyLong(PROP_LAST_ACCESS, userId);
    }
}
