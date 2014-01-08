package com.butent.bee.shared.data.news;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public final class Headline implements BeeSerializable, HasCaption {

  private enum Type {
    NEW, UPDATED
  }

  public static final String CAPTION_SEPARATOR = " ";
      
  public static Headline forInsert(long id, String caption) {
    return new Headline(id, caption, Type.NEW);
  }

  public static Headline forUpdate(long id, String caption) {
    return new Headline(id, caption, Type.UPDATED);
  }
  
  public static Headline restore(String s) {
    Assert.notEmpty(s);

    Headline headline = new Headline();
    headline.deserialize(s);
    return headline;
  }

  private long id;
  private String caption;

  private Type type;

  private Headline() {
  }

  private Headline(long id, String caption, Type type) {
    this.id = id;
    this.caption = caption;
    this.type = type;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 3);

    int i = 0;
    setId(BeeUtils.toLong(arr[i++]));
    setCaption(arr[i++]);
    setType(Codec.unpack(Type.class, arr[i++]));
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public long getId() {
    return id;
  }

  public boolean isNew() {
    return getType() == Type.NEW;
  }
  
  public boolean isUpdated() {
    return getType() == Type.UPDATED;
  }

  @Override
  public String serialize() {
    List<String> values = Lists.newArrayList(Long.toString(getId()), getCaption(),
        Codec.pack(getType()));
    return Codec.beeSerialize(values);
  }

  private Type getType() {
    return type;
  }

  private void setCaption(String caption) {
    this.caption = caption;
  }

  private void setId(long id) {
    this.id = id;
  }

  private void setType(Type type) {
    this.type = type;
  }
}
