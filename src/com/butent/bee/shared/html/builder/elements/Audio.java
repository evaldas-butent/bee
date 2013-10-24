package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Audio extends FertileElement {

  public Audio() {
    super();
  }

  public Audio addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Audio append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Audio append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Audio autoplay() {
    setAttribute(Attributes.AUTOPLAY, true);
    return this;
  }

  public Audio controls() {
    setAttribute(Attributes.CONTROLS, true);
    return this;
  }

  public Audio crossoriginAnonymous() {
    setAttribute(Attributes.CROSS_ORIGIN, Keywords.CORS_SETTINGS_ANONYMOUS);
    return this;
  }

  public Audio crossoriginUseCredentials() {
    setAttribute(Attributes.CROSS_ORIGIN, Keywords.CORS_SETTINGS_USE_CREDENTIALS);
    return this;
  }

  public Audio id(String value) {
    setId(value);
    return this;
  }

  public Audio insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Audio lang(String value) {
    setLang(value);
    return this;
  }

  public Audio loop() {
    setAttribute(Attributes.LOOP, true);
    return this;
  }

  public Audio mediaGroup(String value) {
    setAttribute(Attributes.MEDIA_GROUP, value);
    return this;
  }

  public Audio muted() {
    setAttribute(Attributes.MUTED, true);
    return this;
  }

  public Audio noAutoplay() {
    setAttribute(Attributes.AUTOPLAY, false);
    return this;
  }

  public Audio noControls() {
    setAttribute(Attributes.CONTROLS, false);
    return this;
  }

  public Audio noLoop() {
    setAttribute(Attributes.LOOP, false);
    return this;
  }

  public Audio notMuted() {
    setAttribute(Attributes.MUTED, false);
    return this;
  }

  public Audio preloadAuto() {
    setAttribute(Attributes.PRELOAD, Keywords.PRELOAD_AUTO);
    return this;
  }

  public Audio preloadMetaData() {
    setAttribute(Attributes.PRELOAD, Keywords.PRELOAD_META_DATA);
    return this;
  }

  public Audio preloadNone() {
    setAttribute(Attributes.PRELOAD, Keywords.PRELOAD_NONE);
    return this;
  }

  public Audio remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Audio src(String value) {
    setAttribute(Attributes.SRC, value);
    return this;
  }

  public Audio text(String text) {
    super.appendText(text);
    return this;
  }

  public Audio title(String value) {
    setTitle(value);
    return this;
  }
}
