package com.butent.bee.shared.news;

import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;

public interface HeadlineProducer {
  Headline produce(Feed feed, long userId, BeeRowSet rowSet, IsRow row, boolean isNew);
}
