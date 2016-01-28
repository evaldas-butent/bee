package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.media.client.MediaBase;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.utils.BeeUtils;

public class Video extends MediaBase implements IdentifiableWidget {

  public Video() {
    this(Document.get().createVideoElement());
  }

  public Video(String styleName) {
    this();
    if (!BeeUtils.isEmpty(styleName)) {
      addStyleName(styleName);
    }
  }

  public Video(VideoElement element) {
    super(element);
    init();
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "video";
  }

  public String getPoster() {
    return getVideoElement().getPoster();
  }

  public VideoElement getVideoElement() {
    return getMediaElement().cast();
  }

  public int getVideoHeight() {
    return getVideoElement().getVideoHeight();
  }

  public int getVideoWidth() {
    return getVideoElement().getVideoWidth();
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setPoster(String url) {
    getVideoElement().setPoster(url);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
