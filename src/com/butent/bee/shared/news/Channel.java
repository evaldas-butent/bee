package com.butent.bee.shared.news;

import com.butent.bee.shared.time.DateTime;

import java.util.List;

public interface Channel {
  List<Headline> getHeadlines(Feed feed, long userId, DateTime startDate);
}
