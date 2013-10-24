package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Video extends FertileElement {

  public Video() {
    super();
  }

  public Video addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Video append(List<? extends Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Video append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Video autoplay() {
    setAttribute(Attributes.AUTOPLAY, true);
    return this;
  }

  public Video controls() {
    setAttribute(Attributes.CONTROLS, true);
    return this;
  }

  public Video crossoriginAnonymous() {
    setAttribute(Attributes.CROSS_ORIGIN, Keywords.CORS_SETTINGS_ANONYMOUS);
    return this;
  }

  public Video crossoriginUseCredentials() {
    setAttribute(Attributes.CROSS_ORIGIN, Keywords.CORS_SETTINGS_USE_CREDENTIALS);
    return this;
  }

  public Video height(int value) {
    setAttribute(Attributes.HEIGHT, value);
    return this;
  }

  public Video id(String value) {
    setId(value);
    return this;
  }

  public Video insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Video lang(String value) {
    setLang(value);
    return this;
  }

  public Video loop() {
    setAttribute(Attributes.LOOP, true);
    return this;
  }

  public Video mediaGroup(String value) {
    setAttribute(Attributes.MEDIA_GROUP, value);
    return this;
  }

  public Video muted() {
    setAttribute(Attributes.MUTED, true);
    return this;
  }

  public Video noAutoplay() {
    setAttribute(Attributes.AUTOPLAY, false);
    return this;
  }

  public Video noControls() {
    setAttribute(Attributes.CONTROLS, false);
    return this;
  }

  public Video noLoop() {
    setAttribute(Attributes.LOOP, false);
    return this;
  }

  public Video notMuted() {
    setAttribute(Attributes.MUTED, false);
    return this;
  }

  public Video poster(String value) {
    setAttribute(Attributes.POSTER, value);
    return this;
  }

  public Video preloadAuto() {
    setAttribute(Attributes.PRELOAD, Keywords.PRELOAD_AUTO);
    return this;
  }

  public Video preloadMetaData() {
    setAttribute(Attributes.PRELOAD, Keywords.PRELOAD_META_DATA);
    return this;
  }

  public Video preloadNone() {
    setAttribute(Attributes.PRELOAD, Keywords.PRELOAD_NONE);
    return this;
  }

  public Video remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Video src(String value) {
    setAttribute(Attributes.SRC, value);
    return this;
  }

  public Video text(String text) {
    super.appendText(text);
    return this;
  }

  public Video title(String value) {
    setTitle(value);
    return this;
  }

  public Video width(int value) {
    setAttribute(Attributes.WIDTH, value);
    return this;
  }
}
