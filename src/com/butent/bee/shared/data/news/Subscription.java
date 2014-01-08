package com.butent.bee.shared.data.news;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class Subscription implements BeeSerializable, HasCaption {

  public static Subscription restore(String s) {
    Assert.notEmpty(s);

    Subscription subscription = new Subscription();
    subscription.deserialize(s);
    return subscription;
  }

  private Feed feed;

  private String caption;
  private DateTime date;

  private final List<Headline> headlines = Lists.newArrayList();

  public Subscription(Feed feed, String caption, DateTime date) {
    this.feed = feed;
    this.caption = caption;
    this.date = date;
  }

  private Subscription() {
  }

  public void add(Headline headline) {
    if (headline != null) {
      headlines.add(headline);
    }
  }

  public void clear() {
    headlines.clear();
  }

  public int countNew() {
    int count = 0;

    for (Headline headline : headlines) {
      if (headline.isNew()) {
        count++;
      }
    }

    return count;
  }

  public int countUpdated() {
    int count = 0;

    for (Headline headline : headlines) {
      if (headline.isUpdated()) {
        count++;
      }
    }

    return count;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 4);

    int i = 0;
    setFeed(Codec.unpack(Feed.class, arr[i++]));
    setCaption(arr[i++]);
    setDate(DateTime.restore(arr[i++]));

    String[] hArr = Codec.beeDeserializeCollection(arr[i++]);

    if (!isEmpty()) {
      clear();
    }
    if (hArr != null) {
      for (String h : hArr) {
        add(Headline.restore(h));
      }
    }
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public DateTime getDate() {
    return date;
  }

  public Feed getFeed() {
    return feed;
  }

  public List<Headline> getHeadlines() {
    return headlines;
  }

  public String getLabel() {
    if (getFeed() == null) {
      return getCaption();
    } else {
      return BeeUtils.notEmpty(getCaption(), getFeed().getCaption());
    }
  }

  public boolean isEmpty() {
    return headlines.isEmpty();
  }

  @Override
  public String serialize() {
    Object[] arr = new Object[] {Codec.pack(getFeed()), getCaption(), getDate(), headlines};
    return Codec.beeSerialize(arr);
  }

  public int size() {
    return headlines.size();
  }

  private void setCaption(String caption) {
    this.caption = caption;
  }

  private void setDate(DateTime date) {
    this.date = date;
  }

  private void setFeed(Feed feed) {
    this.feed = feed;
  }
}
