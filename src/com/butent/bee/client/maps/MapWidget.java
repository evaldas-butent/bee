package com.butent.bee.client.maps;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RequiresResize;

import com.butent.bee.client.widget.CustomWidget;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.ui.HasCaption;

public final class MapWidget extends CustomWidget implements RequiresResize, HasCaption {

  public static MapWidget create(MapOptions options) {
    return create(options, null);
  }

  public static MapWidget create(MapOptions options, String caption) {
    if (options == null) {
      return null;
    } else {
      return new MapWidget(options, caption);
    }
  }

  private MapOptions mapOptions;
  private MapImpl impl;
  
  private String caption;

  private MapWidget(MapOptions options, String caption) {
    super(Document.get().createDivElement());

    this.mapOptions = options;
    this.caption = caption;
  }

  public void fitBounds(LatLngBounds bounds) {
    if (impl != null) {
      impl.fitBounds(bounds);
    }
  }

  public LatLngBounds getBounds() {
    return (impl == null) ? null : impl.getBounds();
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public LatLng getCenter() {
    return (impl == null) ? null : impl.getCenter();
  }

  public Element getDiv() {
    return (impl == null) ? null : impl.getDiv();
  }

  public int getHeading() {
    return (impl == null) ? BeeConst.UNDEF : impl.getHeading();
  }

  @Override
  public String getIdPrefix() {
    return "map";
  }

  public MapTypeId getMapTypeId() {
    return (impl == null) ? null : impl.getMapTypeId();
  }

  public int getTilt() {
    return (impl == null) ? BeeConst.UNDEF : impl.getTilt();
  }

  public int getZoom() {
    return (impl == null) ? BeeConst.UNDEF : impl.getZoom();
  }

  @Override
  public void onResize() {
    triggerResize();
  }

  public void panBy(int x, int y) {
    if (impl != null) {
      impl.panBy(x, y);
    }
  }

  public void panTo(LatLng latLng) {
    if (impl != null) {
      impl.panTo(latLng);
    }
  }

  public void panToBounds(LatLngBounds latLngBounds) {
    if (impl != null) {
      impl.panToBounds(latLngBounds);
    }
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setCenter(LatLng latlng) {
    if (impl != null) {
      impl.setCenter(latlng);
    }
  }

  public void setHeading(int heading) {
    if (impl != null) {
      impl.setHeading(heading);
    }
  }

  public void setMapTypeId(MapTypeId mapTypeId) {
    if (impl != null) {
      impl.setMapTypeId(mapTypeId);
    }
  }

  public void setOptions(MapOptions options) {
    this.mapOptions = options;
    if (impl != null) {
      impl.setOptions(options);
    }
  }

  public void setTilt(int tilt) {
    if (impl != null) {
      impl.setTilt(tilt);
    }
  }

  public void setZoom(int zoom) {
    if (impl != null) {
      impl.setZoom(zoom);
    }
  }

  public void triggerResize() {
    if (impl != null) {
      LatLng center = getCenter();
      impl.triggerResize();
      setCenter(center);
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    this.impl = MapImpl.create(getElement(), mapOptions);
  }
}
