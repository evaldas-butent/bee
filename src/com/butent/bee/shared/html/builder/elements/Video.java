package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.builder.Attribute;
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
    setAttribute(Attribute.AUTOPLAY, true);
    return this;
  }

  public Video controls() {
    setAttribute(Attribute.CONTROLS, true);
    return this;
  }

  public Video crossoriginAnonymous() {
    setAttribute(Attribute.CROSS_ORIGIN, Keywords.CORS_SETTINGS_ANONYMOUS);
    return this;
  }

  public Video crossoriginUseCredentials() {
    setAttribute(Attribute.CROSS_ORIGIN, Keywords.CORS_SETTINGS_USE_CREDENTIALS);
    return this;
  }

  public Video height(int value) {
    setAttribute(Attribute.HEIGHT, value);
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
    setAttribute(Attribute.LOOP, true);
    return this;
  }

  public Video mediaGroup(String value) {
    setAttribute(Attribute.MEDIA_GROUP, value);
    return this;
  }

  public Video muted() {
    setAttribute(Attribute.MUTED, true);
    return this;
  }

  public Video noAutoplay() {
    setAttribute(Attribute.AUTOPLAY, false);
    return this;
  }

  public Video noControls() {
    setAttribute(Attribute.CONTROLS, false);
    return this;
  }

  public Video noLoop() {
    setAttribute(Attribute.LOOP, false);
    return this;
  }

  public Video notMuted() {
    setAttribute(Attribute.MUTED, false);
    return this;
  }

  public Video poster(String value) {
    setAttribute(Attribute.POSTER, value);
    return this;
  }

  public Video preloadAuto() {
    setAttribute(Attribute.PRELOAD, Keywords.PRELOAD_AUTO);
    return this;
  }

  public Video preloadMetaData() {
    setAttribute(Attribute.PRELOAD, Keywords.PRELOAD_META_DATA);
    return this;
  }

  public Video preloadNone() {
    setAttribute(Attribute.PRELOAD, Keywords.PRELOAD_NONE);
    return this;
  }

  public Video remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Video src(String value) {
    setAttribute(Attribute.SRC, value);
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
    setAttribute(Attribute.WIDTH, value);
    return this;
  }
}
