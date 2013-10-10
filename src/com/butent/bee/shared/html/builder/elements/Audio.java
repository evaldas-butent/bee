package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Keywords;
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

  public Audio append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Audio append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Audio autoplay() {
    setAttribute(Attribute.AUTOPLAY, true);
    return this;
  }

  public Audio controls() {
    setAttribute(Attribute.CONTROLS, true);
    return this;
  }

  public Audio crossoriginAnonymous() {
    setAttribute(Attribute.CROSSORIGIN, Keywords.CORS_SETTINGS_ANONYMOUS);
    return this;
  }

  public Audio crossoriginUseCredentials() {
    setAttribute(Attribute.CROSSORIGIN, Keywords.CORS_SETTINGS_USE_CREDENTIALS);
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
    setAttribute(Attribute.LOOP, true);
    return this;
  }

  public Audio mediaGroup(String value) {
    setAttribute(Attribute.MEDIAGROUP, value);
    return this;
  }
  
  public Audio muted() {
    setAttribute(Attribute.MUTED, true);
    return this;
  }

  public Audio noAutoplay() {
    setAttribute(Attribute.AUTOPLAY, false);
    return this;
  }

  public Audio noControls() {
    setAttribute(Attribute.CONTROLS, false);
    return this;
  }

  public Audio noLoop() {
    setAttribute(Attribute.LOOP, false);
    return this;
  }
  
  public Audio notMuted() {
    setAttribute(Attribute.MUTED, false);
    return this;
  }

  public Audio preloadAuto() {
    setAttribute(Attribute.PRELOAD, Keywords.PRELOAD_AUTO);
    return this;
  }

  public Audio preloadMetadata() {
    setAttribute(Attribute.PRELOAD, Keywords.PRELOAD_METADATA);
    return this;
  }

  public Audio preloadNone() {
    setAttribute(Attribute.PRELOAD, Keywords.PRELOAD_NONE);
    return this;
  }

  public Audio remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Audio src(String value) {
    setAttribute(Attribute.SRC, value);
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
