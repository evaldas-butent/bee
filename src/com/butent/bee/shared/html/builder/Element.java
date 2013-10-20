package com.butent.bee.shared.html.builder;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.HasCssName;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.CssAngle;
import com.butent.bee.shared.css.CssFrequency;
import com.butent.bee.shared.css.CssTime;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.css.CssProperty;
import com.butent.bee.shared.css.values.*;
import com.butent.bee.shared.css.values.Float;
import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.ui.Color;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;

public class Element extends Node {

  private final String tag;

  private final List<Attribute> attributes = Lists.newArrayList();

  private final List<String> classes = Lists.newArrayList();
  private final List<Style> styles = Lists.newArrayList();

  protected Element() {
    super();
    this.tag = NameUtils.getClassName(getClass()).toLowerCase();
  }

  public void addClassName(String clazz) {
    if (!BeeUtils.isEmpty(clazz) && !BeeUtils.containsSame(classes, clazz)) {
      classes.add(clazz.trim());
    }
  }

  @Override
  public String build(int indentStart, int indentStep) {
    StringBuilder sb = new StringBuilder(Node.indent(indentStart, buildStart()));
    sb.append(buildEnd());
    return sb.toString();
  }

  public String getAttribute(String name) {
    Attribute attribute = findAttribute(name);
    return (attribute == null) ? null : attribute.getValue();
  }

  public String getData(String key) {
    return BeeUtils.isEmpty(key) ? null : getAttribute(Attribute.DATA_PREFIX + key.trim());
  }

  public String getId() {
    return getAttribute(Attribute.ID);
  }

  public String getStyle(String name) {
    Style style = findStyle(name);
    return (style == null) ? null : style.getValue();
  }

  public String getTag() {
    return tag;
  }

  public String getTitle() {
    return getAttribute(Attribute.TITLE);
  }

  public boolean removeAttribute(String name) {
    int index = BeeConst.UNDEF;

    for (int i = 0; i < attributes.size(); i++) {
      if (BeeUtils.same(attributes.get(i).getName(), name)) {
        index = i;
        break;
      }
    }

    if (BeeConst.isUndef(index)) {
      return false;
    } else {
      return attributes.remove(index) != null;
    }
  }

  public boolean removeStyle(String name) {
    int index = BeeConst.UNDEF;

    for (int i = 0; i < styles.size(); i++) {
      if (BeeUtils.same(styles.get(i).getName(), name)) {
        index = i;
        break;
      }
    }

    if (BeeConst.isUndef(index)) {
      return false;
    } else {
      return styles.remove(index) != null;
    }
  }

  public void setAccessKey(String accessKey) {
    setAttribute(Attribute.ACCESS_KEY, accessKey);
  }

  public void setAlignContent(AlignContent alignContent) {
    setStyle(CssProperty.ALIGN_CONTENT, alignContent);
  }

  public void setAlignItems(AlignItems alignItems) {
    setStyle(CssProperty.ALIGN_ITEMS, alignItems);
  }

  public void setAlignmentAdjust(AlignmentAdjust alignmentAdjust) {
    setStyle(CssProperty.ALIGNMENT_ADJUST, alignmentAdjust);
  }

  public void setAlignmentAdjust(double value, CssUnit unit) {
    setStyle(CssProperty.ALIGNMENT_ADJUST, value, unit);
  }

  public void setAlignmentAdjust(int value, CssUnit unit) {
    setStyle(CssProperty.ALIGNMENT_ADJUST, value, unit);
  }

  public void setAlignmentBaseline(AlignmentBaseline alignmentBaseline) {
    setStyle(CssProperty.ALIGNMENT_BASELINE, alignmentBaseline);
  }

  public void setAlignSelf(AlignSelf alignSelf) {
    setStyle(CssProperty.ALIGN_SELF, alignSelf);
  }

  public void setAll(All all) {
    setStyle(CssProperty.ALL, all);
  }

  public void setAnchorPoint(String anchorPoint) {
    setStyle(CssProperty.ANCHOR_POINT, anchorPoint);
  }

  public void setAnimation(String animation) {
    setStyle(CssProperty.ANIMATION, animation);
  }

  public void setAnimationDelay(String animationDelay) {
    setStyle(CssProperty.ANIMATION_DELAY, animationDelay);
  }

  public void setAnimationDirection(AnimationDirection animationDirection) {
    setStyle(CssProperty.ANIMATION_DIRECTION, animationDirection);
  }

  public void setAnimationDuration(String animationDuration) {
    setStyle(CssProperty.ANIMATION_DURATION, animationDuration);
  }

  public void setAnimationFillMode(AnimationFillMode animationFillMode) {
    setStyle(CssProperty.ANIMATION_FILL_MODE, animationFillMode);
  }

  public void setAnimationIterationCount(int value) {
    setStyle(CssProperty.ANIMATION_ITERATION_COUNT, value);
  }

  public void setAnimationIterationCount(String animationIterationCount) {
    setStyle(CssProperty.ANIMATION_ITERATION_COUNT, animationIterationCount);
  }

  public void setAnimationName(String animationName) {
    setStyle(CssProperty.ANIMATION_NAME, animationName);
  }

  public void setAnimationPlayState(AnimationPlayState animationPlayState) {
    setStyle(CssProperty.ANIMATION_PLAY_STATE, animationPlayState);
  }

  public void setAnimationTimingFunction(AnimationTimingFunction animationTimingFunction) {
    setStyle(CssProperty.ANIMATION_TIMING_FUNCTION, animationTimingFunction);
  }

  public void setAnimationTimingFunction(String value) {
    setStyle(CssProperty.ANIMATION_TIMING_FUNCTION, value);
  }

  public void setAppearance(String appearance) {
    setStyle(CssProperty.APPEARANCE, appearance);
  }

  public void setAttribute(String name, boolean value) {
    if (!value) {
      removeAttribute(name);

    } else if (!BeeUtils.isEmpty(name)) {
      Attribute attribute = findAttribute(name);
      if (attribute == null) {
        attributes.add(new BooleanAttribute(name, value));
      } else {
        attribute.setValue(name);
      }
    }
  }

  public void setAttribute(String name, double value) {
    setAttribute(name, BeeUtils.toString(value));
  }

  public void setAttribute(String name, int value) {
    setAttribute(name, Integer.toString(value));
  }

  public void setAttribute(String name, String value) {
    if (value == null) {
      removeAttribute(name);

    } else if (!BeeUtils.isEmpty(name)) {
      Attribute attribute = findAttribute(name);
      if (attribute == null) {
        attributes.add(new Attribute(name, value));
      } else {
        attribute.setValue(value);
      }
    }
  }

  public void setAzimuth(String azimuth) {
    setStyle(CssProperty.AZIMUTH, azimuth);
  }

  public void setBackfaceVisibility(String backfaceVisibility) {
    setStyle(CssProperty.BACKFACE_VISIBILITY, backfaceVisibility);
  }

  public void setBackground(String background) {
    setStyle(CssProperty.BACKGROUND, background);
  }

  public void setBackgroundAttachment(BackgroundAttachment backgroundAttachment) {
    setStyle(CssProperty.BACKGROUND_ATTACHMENT, backgroundAttachment);
  }

  public void setBackgroundAttachment(Collection<BackgroundAttachment> values) {
    setStyle(CssProperty.BACKGROUND_ATTACHMENT, Style.join(BeeConst.STRING_COMMA, values));
  }

  public void setBackgroundClip(BackgroundClip backgroundClip) {
    setStyle(CssProperty.BACKGROUND_CLIP, backgroundClip);
  }

  public void setBackgroundClip(Collection<BackgroundClip> values) {
    setStyle(CssProperty.BACKGROUND_CLIP, Style.join(BeeConst.STRING_SPACE, values));
  }

  public void setBackgroundColor(Color color) {
    if (color == null || BeeUtils.isEmpty(color.getBackground())) {
      removeStyle(CssProperty.BACKGROUND_COLOR);
    } else {
      setStyle(CssProperty.BACKGROUND_COLOR, color.getBackground());
    }
  }

  public void setBackgroundColor(String backgroundColor) {
    setStyle(CssProperty.BACKGROUND_COLOR, backgroundColor);
  }

  public void setBackgroundImage(String backgroundImage) {
    setStyle(CssProperty.BACKGROUND_IMAGE, backgroundImage);
  }

  public void setBackgroundOrigin(BackgroundOrigin backgroundOrigin) {
    setStyle(CssProperty.BACKGROUND_ORIGIN, backgroundOrigin);
  }

  public void setBackgroundOrigin(Collection<BackgroundOrigin> values) {
    setStyle(CssProperty.BACKGROUND_ORIGIN, Style.join(BeeConst.STRING_SPACE, values));
  }

  public void setBackgroundPosition(BackgroundPosition backgroundPosition) {
    setStyle(CssProperty.BACKGROUND_POSITION, backgroundPosition);
  }

  public void setBackgroundPosition(Collection<BackgroundPosition> values) {
    setStyle(CssProperty.BACKGROUND_POSITION, Style.join(BeeConst.STRING_SPACE, values));
  }

  public void setBackgroundPosition(String backgroundPosition) {
    setStyle(CssProperty.BACKGROUND_POSITION, backgroundPosition);
  }

  public void setBackgroundRepeat(BackgroundRepeat backgroundRepeat) {
    setStyle(CssProperty.BACKGROUND_REPEAT, backgroundRepeat);
  }

  public void setBackgroundRepeat(BackgroundRepeat horizontal, BackgroundRepeat vertical) {
    if (horizontal == null) {
      setStyle(CssProperty.BACKGROUND_REPEAT, vertical);

    } else if (vertical == null) {
      setStyle(CssProperty.BACKGROUND_REPEAT, horizontal);

    } else {
      setStyle(CssProperty.BACKGROUND_REPEAT, BeeUtils.joinWords(horizontal.getCssName(),
          vertical.getCssName()));
    }
  }

  public void setBackgroundRepeat(String backgroundRepeat) {
    setStyle(CssProperty.BACKGROUND_REPEAT, backgroundRepeat);
  }

  public void setBackgroundSize(BackgroundSize backgroundSize) {
    setStyle(CssProperty.BACKGROUND_SIZE, backgroundSize);
  }

  public void setBackgroundSize(double value, CssUnit unit) {
    setStyle(CssProperty.BACKGROUND_SIZE, value, unit);
  }

  public void setBackgroundSize(double width, CssUnit widthUnit,
      double height, CssUnit heightUnit) {
    setStyle(CssProperty.BACKGROUND_SIZE, BeeUtils.joinWords(CssUnit.format(width, widthUnit),
        CssUnit.format(height, heightUnit)));
  }

  public void setBackgroundSize(int value, CssUnit unit) {
    setStyle(CssProperty.BACKGROUND_SIZE, value, unit);
  }

  public void setBackgroundSize(int width, CssUnit widthUnit, int height, CssUnit heightUnit) {
    setStyle(CssProperty.BACKGROUND_SIZE, BeeUtils.joinWords(CssUnit.format(width, widthUnit),
        CssUnit.format(height, heightUnit)));
  }

  public void setBackgroundSize(String backgroundSize) {
    setStyle(CssProperty.BACKGROUND_SIZE, backgroundSize);
  }

  public void setBaselineShift(BaselineShift baselineShift) {
    setStyle(CssProperty.BASELINE_SHIFT, baselineShift);
  }

  public void setBaselineShift(double value, CssUnit unit) {
    setStyle(CssProperty.BASELINE_SHIFT, value, unit);
  }

  public void setBaselineShift(int value, CssUnit unit) {
    setStyle(CssProperty.BASELINE_SHIFT, value, unit);
  }

  public void setBinding(String binding) {
    setStyle(CssProperty.BINDING, binding);
  }

  public void setBleed(double value, CssUnit unit) {
    setStyle(CssProperty.BLEED, value, unit);
  }

  public void setBleed(int value, CssUnit unit) {
    setStyle(CssProperty.BLEED, value, unit);
  }

  public void setBookmarkLabel(String bookmarkLabel) {
    setStyle(CssProperty.BOOKMARK_LABEL, bookmarkLabel);
  }

  public void setBookmarkLevel(int bookmarkLevel) {
    setStyle(CssProperty.BOOKMARK_LEVEL, bookmarkLevel);
  }

  public void setBookmarkLevel(String bookmarkLevel) {
    setStyle(CssProperty.BOOKMARK_LEVEL, bookmarkLevel);
  }

  public void setBookmarkState(BookmarkState bookmarkState) {
    setStyle(CssProperty.BOOKMARK_STATE, bookmarkState);
  }

  public void setBookmarkTarget(String bookmarkTarget) {
    setStyle(CssProperty.BOOKMARK_TARGET, bookmarkTarget);
  }

  public void setBorder(String border) {
    setStyle(CssProperty.BORDER, border);
  }

  public void setBorderBottom(String borderBottom) {
    setStyle(CssProperty.BORDER_BOTTOM, borderBottom);
  }

  public void setBorderBottomColor(String borderBottomColor) {
    setStyle(CssProperty.BORDER_BOTTOM_COLOR, borderBottomColor);
  }

  public void setBorderBottomLeftRadius(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_BOTTOM_LEFT_RADIUS, value, unit);
  }

  public void setBorderBottomLeftRadius(double v1, CssUnit u1, double v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_BOTTOM_LEFT_RADIUS, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderBottomLeftRadius(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_BOTTOM_LEFT_RADIUS, value, unit);
  }

  public void setBorderBottomLeftRadius(int v1, CssUnit u1, int v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_BOTTOM_LEFT_RADIUS, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderBottomLeftRadius(String borderBottomLeftRadius) {
    setStyle(CssProperty.BORDER_BOTTOM_LEFT_RADIUS, borderBottomLeftRadius);
  }

  public void setBorderBottomRightRadius(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_BOTTOM_RIGHT_RADIUS, value, unit);
  }

  public void setBorderBottomRightRadius(double v1, CssUnit u1, double v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_BOTTOM_RIGHT_RADIUS, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderBottomRightRadius(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_BOTTOM_RIGHT_RADIUS, value, unit);
  }

  public void setBorderBottomRightRadius(int v1, CssUnit u1, int v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_BOTTOM_RIGHT_RADIUS, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderBottomRightRadius(String borderBottomRightRadius) {
    setStyle(CssProperty.BORDER_BOTTOM_RIGHT_RADIUS, borderBottomRightRadius);
  }

  public void setBorderBottomStyle(BorderStyle borderBottomStyle) {
    setStyle(CssProperty.BORDER_BOTTOM_STYLE, borderBottomStyle);
  }

  public void setBorderBottomWidth(BorderWidth borderBottomWidth) {
    setStyle(CssProperty.BORDER_BOTTOM_WIDTH, borderBottomWidth);
  }

  public void setBorderBottomWidth(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_BOTTOM_WIDTH, value, unit);
  }

  public void setBorderBottomWidth(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_BOTTOM_WIDTH, value, unit);
  }

  public void setBorderCollapse(BorderCollapse borderCollapse) {
    setStyle(CssProperty.BORDER_COLLAPSE, borderCollapse);
  }

  public void setBorderColor(String borderColor) {
    setStyle(CssProperty.BORDER_COLOR, borderColor);
  }

  public void setBorderImage(String borderImage) {
    setStyle(CssProperty.BORDER_IMAGE, borderImage);
  }

  public void setBorderImageOutset(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_IMAGE_OUTSET, value, unit);
  }

  public void setBorderImageOutset(double v1, CssUnit u1, double v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_IMAGE_OUTSET, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderImageOutset(double v1, CssUnit u1, double v2, CssUnit u2,
      double v3, CssUnit u3) {
    setStyle(CssProperty.BORDER_IMAGE_OUTSET, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2), CssUnit.format(v3, u3)));
  }

  public void setBorderImageOutset(double v1, CssUnit u1, double v2, CssUnit u2,
      double v3, CssUnit u3, double v4, CssUnit u4) {
    setStyle(CssProperty.BORDER_IMAGE_OUTSET, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2), CssUnit.format(v3, u3), CssUnit.format(v4, u4)));
  }

  public void setBorderImageOutset(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_IMAGE_OUTSET, value, unit);
  }

  public void setBorderImageOutset(int v1, CssUnit u1, int v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_IMAGE_OUTSET, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderImageOutset(int v1, CssUnit u1, int v2, CssUnit u2, int v3, CssUnit u3) {
    setStyle(CssProperty.BORDER_IMAGE_OUTSET, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2), CssUnit.format(v3, u3)));
  }

  public void setBorderImageOutset(int v1, CssUnit u1, int v2, CssUnit u2, int v3, CssUnit u3,
      int v4, CssUnit u4) {
    setStyle(CssProperty.BORDER_IMAGE_OUTSET, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2), CssUnit.format(v3, u3), CssUnit.format(v4, u4)));
  }

  public void setBorderImageRepeat(BorderImageRepeat borderImageRepeat) {
    setStyle(CssProperty.BORDER_IMAGE_REPEAT, borderImageRepeat);
  }

  public void setBorderImageRepeat(BorderImageRepeat v1, BorderImageRepeat v2) {
    if (v1 == null) {
      setStyle(CssProperty.BORDER_IMAGE_REPEAT, v2);
    } else if (v2 == null) {
      setStyle(CssProperty.BORDER_IMAGE_REPEAT, v1);
    } else {
      setStyle(CssProperty.BORDER_IMAGE_REPEAT, BeeUtils.joinWords(v1.getCssName(),
          v2.getCssName()));
    }
  }

  public void setBorderImageSlice(String borderImageSlice) {
    setStyle(CssProperty.BORDER_IMAGE_SLICE, borderImageSlice);
  }

  public void setBorderImageSource(String borderImageSource) {
    setStyle(CssProperty.BORDER_IMAGE_SOURCE, borderImageSource);
  }

  public void setBorderImageWidth(String borderImageWidth) {
    setStyle(CssProperty.BORDER_IMAGE_WIDTH, borderImageWidth);
  }

  public void setBorderLeft(String borderLeft) {
    setStyle(CssProperty.BORDER_LEFT, borderLeft);
  }

  public void setBorderLeftColor(String borderLeftColor) {
    setStyle(CssProperty.BORDER_LEFT_COLOR, borderLeftColor);
  }

  public void setBorderLeftStyle(BorderStyle borderLeftStyle) {
    setStyle(CssProperty.BORDER_LEFT_STYLE, borderLeftStyle);
  }

  public void setBorderLeftWidth(BorderWidth borderLeftWidth) {
    setStyle(CssProperty.BORDER_LEFT_WIDTH, borderLeftWidth);
  }

  public void setBorderLeftWidth(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_LEFT_WIDTH, value, unit);
  }

  public void setBorderLeftWidth(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_LEFT_WIDTH, value, unit);
  }

  public void setBorderRadius(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_RADIUS, value, unit);
  }

  public void setBorderRadius(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_RADIUS, value, unit);
  }

  public void setBorderRadius(String borderRadius) {
    setStyle(CssProperty.BORDER_RADIUS, borderRadius);
  }

  public void setBorderRadius(String horizontal, String vertical) {
    setStyle(CssProperty.BORDER_RADIUS, BeeUtils.join(BeeConst.STRING_SLASH, horizontal, vertical));
  }

  public void setBorderRight(String borderRight) {
    setStyle(CssProperty.BORDER_RIGHT, borderRight);
  }

  public void setBorderRightColor(String borderRightColor) {
    setStyle(CssProperty.BORDER_RIGHT_COLOR, borderRightColor);
  }

  public void setBorderRightStyle(BorderStyle borderRightStyle) {
    setStyle(CssProperty.BORDER_RIGHT_STYLE, borderRightStyle);
  }

  public void setBorderRightWidth(BorderWidth borderRightWidth) {
    setStyle(CssProperty.BORDER_RIGHT_WIDTH, borderRightWidth);
  }

  public void setBorderRightWidth(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_RIGHT_WIDTH, value, unit);
  }

  public void setBorderRightWidth(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_RIGHT_WIDTH, value, unit);
  }

  public void setBorderSpacing(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_SPACING, value, unit);
  }

  public void setBorderSpacing(double hor, CssUnit horUnit, double vert, CssUnit vertUnit) {
    setStyle(CssProperty.BORDER_SPACING, BeeUtils.joinWords(CssUnit.format(hor, horUnit),
        CssUnit.format(vert, vertUnit)));
  }

  public void setBorderSpacing(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_SPACING, value, unit);
  }

  public void setBorderSpacing(int hor, CssUnit horUnit, int vert, CssUnit vertUnit) {
    setStyle(CssProperty.BORDER_SPACING, BeeUtils.joinWords(CssUnit.format(hor, horUnit),
        CssUnit.format(vert, vertUnit)));
  }

  public void setBorderSpacing(String borderSpacing) {
    setStyle(CssProperty.BORDER_SPACING, borderSpacing);
  }

  public void setBorderStyle(BorderStyle borderStyle) {
    setStyle(CssProperty.BORDER_STYLE, borderStyle);
  }

  public void setBorderTop(String borderTop) {
    setStyle(CssProperty.BORDER_TOP, borderTop);
  }

  public void setBorderTopColor(String borderTopColor) {
    setStyle(CssProperty.BORDER_TOP_COLOR, borderTopColor);
  }

  public void setBorderTopLeftRadius(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_TOP_LEFT_RADIUS, value, unit);
  }

  public void setBorderTopLeftRadius(double v1, CssUnit u1, double v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_TOP_LEFT_RADIUS, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderTopLeftRadius(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_TOP_LEFT_RADIUS, value, unit);
  }

  public void setBorderTopLeftRadius(int v1, CssUnit u1, int v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_TOP_LEFT_RADIUS, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderTopLeftRadius(String borderTopLeftRadius) {
    setStyle(CssProperty.BORDER_TOP_LEFT_RADIUS, borderTopLeftRadius);
  }

  public void setBorderTopRightRadius(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_TOP_RIGHT_RADIUS, value, unit);
  }

  public void setBorderTopRightRadius(double v1, CssUnit u1, double v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_TOP_RIGHT_RADIUS, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderTopRightRadius(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_TOP_RIGHT_RADIUS, value, unit);
  }

  public void setBorderTopRightRadius(int v1, CssUnit u1, int v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_TOP_RIGHT_RADIUS, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderTopRightRadius(String borderTopRightRadius) {
    setStyle(CssProperty.BORDER_TOP_RIGHT_RADIUS, borderTopRightRadius);
  }

  public void setBorderTopStyle(BorderStyle borderTopStyle) {
    setStyle(CssProperty.BORDER_TOP_STYLE, borderTopStyle);
  }

  public void setBorderTopWidth(BorderWidth borderTopWidth) {
    setStyle(CssProperty.BORDER_TOP_WIDTH, borderTopWidth);
  }

  public void setBorderTopWidth(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_TOP_WIDTH, value, unit);
  }

  public void setBorderTopWidth(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_TOP_WIDTH, value, unit);
  }

  public void setBorderWidth(BorderWidth borderWidth) {
    setStyle(CssProperty.BORDER_WIDTH, borderWidth);
  }

  public void setBorderWidth(Collection<BorderWidth> values) {
    setStyle(CssProperty.BORDER_WIDTH, Style.join(BeeConst.STRING_SPACE, values));
  }

  public void setBorderWidth(double value, CssUnit unit) {
    setStyle(CssProperty.BORDER_WIDTH, value, unit);
  }

  public void setBorderWidth(double v1, CssUnit u1, double v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_WIDTH, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderWidth(double v1, CssUnit u1, double v2, CssUnit u2, double v3, CssUnit u3) {
    setStyle(CssProperty.BORDER_WIDTH, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2), CssUnit.format(v3, u3)));
  }

  public void setBorderWidth(double v1, CssUnit u1, double v2, CssUnit u2, double v3, CssUnit u3,
      double v4, CssUnit u4) {
    setStyle(CssProperty.BORDER_WIDTH, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2), CssUnit.format(v3, u3), CssUnit.format(v4, u4)));
  }

  public void setBorderWidth(int value, CssUnit unit) {
    setStyle(CssProperty.BORDER_WIDTH, value, unit);
  }

  public void setBorderWidth(int v1, CssUnit u1, int v2, CssUnit u2) {
    setStyle(CssProperty.BORDER_WIDTH, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setBorderWidth(int v1, CssUnit u1, int v2, CssUnit u2, int v3, CssUnit u3) {
    setStyle(CssProperty.BORDER_WIDTH, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2), CssUnit.format(v3, u3)));
  }

  public void setBorderWidth(int v1, CssUnit u1, int v2, CssUnit u2, int v3, CssUnit u3,
      int v4, CssUnit u4) {
    setStyle(CssProperty.BORDER_WIDTH, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2), CssUnit.format(v3, u3), CssUnit.format(v4, u4)));
  }

  public void setBorderWidth(String borderWidth) {
    setStyle(CssProperty.BORDER_WIDTH, borderWidth);
  }

  public void setBottom(double value, CssUnit unit) {
    setStyle(CssProperty.BOTTOM, value, unit);
  }

  public void setBottom(int value, CssUnit unit) {
    setStyle(CssProperty.BOTTOM, value, unit);
  }

  public void setBottom(String bottom) {
    setStyle(CssProperty.BOTTOM, bottom);
  }

  public void setBoxDecorationBreak(BoxDecorationBreak boxDecorationBreak) {
    setStyle(CssProperty.BOX_DECORATION_BREAK, boxDecorationBreak);
  }

  public void setBoxShadow(String boxShadow) {
    setStyle(CssProperty.BOX_SHADOW, boxShadow);
  }

  public void setBoxSizing(BoxSizing boxSizing) {
    setStyle(CssProperty.BOX_SIZING, boxSizing);
  }

  public void setBreakAfter(BreakAfter breakAfter) {
    setStyle(CssProperty.BREAK_AFTER, breakAfter);
  }

  public void setBreakBefore(BreakBefore breakBefore) {
    setStyle(CssProperty.BREAK_BEFORE, breakBefore);
  }

  public void setBreakInside(BreakInside breakInside) {
    setStyle(CssProperty.BREAK_INSIDE, breakInside);
  }

  public void setCaptionSide(CaptionSide captionSide) {
    setStyle(CssProperty.CAPTION_SIDE, captionSide);
  }

  public void setClassName(String clazz) {
    classes.clear();
    addClassName(clazz);
  }

  public void setClear(Clear clear) {
    setStyle(CssProperty.CLEAR, clear);
  }

  public void setClip(String clip) {
    setStyle(CssProperty.CLIP, clip);
  }

  public void setColor(Color color) {
    if (color == null || BeeUtils.isEmpty(color.getForeground())) {
      removeStyle(CssProperty.COLOR);
    } else {
      setStyle(CssProperty.COLOR, color.getForeground());
    }
  }

  public void setColor(String color) {
    setStyle(CssProperty.COLOR, color);
  }

  public void setColorProfile(String colorProfile) {
    setStyle(CssProperty.COLOR_PROFILE, colorProfile);
  }

  public void setColumnCount(int value) {
    setStyle(CssProperty.COLUMN_COUNT, value);
  }

  public void setColumnCount(String columnCount) {
    setStyle(CssProperty.COLUMN_COUNT, columnCount);
  }

  public void setColumnFill(ColumnFill columnFill) {
    setStyle(CssProperty.COLUMN_FILL, columnFill);
  }

  public void setColumnGap(double value, CssUnit unit) {
    setStyle(CssProperty.COLUMN_GAP, value, unit);
  }

  public void setColumnGap(int value, CssUnit unit) {
    setStyle(CssProperty.COLUMN_GAP, value, unit);
  }

  public void setColumnGap(String columnGap) {
    setStyle(CssProperty.COLUMN_GAP, columnGap);
  }

  public void setColumnRule(String columnRule) {
    setStyle(CssProperty.COLUMN_RULE, columnRule);
  }

  public void setColumnRuleColor(String columnRuleColor) {
    setStyle(CssProperty.COLUMN_RULE_COLOR, columnRuleColor);
  }

  public void setColumnRuleStyle(BorderStyle columnRuleStyle) {
    setStyle(CssProperty.COLUMN_RULE_STYLE, columnRuleStyle);
  }

  public void setColumnRuleWidth(BorderWidth columnRuleWidth) {
    setStyle(CssProperty.COLUMN_RULE_WIDTH, columnRuleWidth);
  }

  public void setColumnRuleWidth(double value, CssUnit unit) {
    setStyle(CssProperty.COLUMN_RULE_WIDTH, value, unit);
  }

  public void setColumnRuleWidth(int value, CssUnit unit) {
    setStyle(CssProperty.COLUMN_RULE_WIDTH, value, unit);
  }

  public void setColumns(String columns) {
    setStyle(CssProperty.COLUMNS, columns);
  }

  public void setColumnSpan(ColumnSpan columnSpan) {
    setStyle(CssProperty.COLUMN_SPAN, columnSpan);
  }

  public void setColumnWidth(double value, CssUnit unit) {
    setStyle(CssProperty.COLUMN_WIDTH, value, unit);
  }

  public void setColumnWidth(int value, CssUnit unit) {
    setStyle(CssProperty.COLUMN_WIDTH, value, unit);
  }

  public void setColumnWidth(String columnWidth) {
    setStyle(CssProperty.COLUMN_WIDTH, columnWidth);
  }

  public void setContent(String content) {
    setStyle(CssProperty.CONTENT, content);
  }

  public void setContentEditable(Boolean editable) {
    if (editable == null) {
      removeAttribute(Attribute.CONTENT_EDITABLE);
    } else if (editable) {
      setAttribute(Attribute.CONTENT_EDITABLE, Keywords.CONTENT_IS_EDITABLE);
    } else {
      setAttribute(Attribute.CONTENT_EDITABLE, Keywords.CONTENT_NOT_EDITABLE);
    }
  }

  public void setContextMenu(String contextMenu) {
    setAttribute(Attribute.CONTEXT_MENU, contextMenu);
  }

  public void setCounterIncrement(String counterIncrement) {
    setStyle(CssProperty.COUNTER_INCREMENT, counterIncrement);
  }

  public void setCounterReset(String counterReset) {
    setStyle(CssProperty.COUNTER_RESET, counterReset);
  }

  public void setCrop(String crop) {
    setStyle(CssProperty.CROP, crop);
  }

  public void setCue(String cue) {
    setStyle(CssProperty.CUE, cue);
  }

  public void setCueAfter(String cueAfter) {
    setStyle(CssProperty.CUE_AFTER, cueAfter);
  }

  public void setCueBefore(String cueBefore) {
    setStyle(CssProperty.CUE_BEFORE, cueBefore);
  }

  public void setCursor(Cursor cursor) {
    setStyle(CssProperty.CURSOR, cursor);
  }

  public void setData(String key, String value) {
    if (!BeeUtils.isEmpty(key)) {
      setAttribute(Attribute.DATA_PREFIX + key.trim(), value);
    }
  }

  public void setDirAuto() {
    setAttribute(Attribute.DIR, Keywords.DIR_AUTO);
  }

  public void setDirection(Direction direction) {
    setStyle(CssProperty.DIRECTION, direction);
  }

  public void setDirLtr() {
    setAttribute(Attribute.DIR, Keywords.DIR_LTR);
  }

  public void setDirRtl() {
    setAttribute(Attribute.DIR, Keywords.DIR_RTL);
  }

  public void setDisplay(Display display) {
    setStyle(CssProperty.DISPLAY, display);
  }

  public void setDominantBaseline(DominantBaseline dominantBaseline) {
    setStyle(CssProperty.DOMINANT_BASELINE, dominantBaseline);
  }

  public void setDraggable(Boolean draggable) {
    if (draggable == null) {
      removeAttribute(Attribute.DRAGGABLE);
    } else if (draggable) {
      setAttribute(Attribute.DRAGGABLE, Keywords.IS_DRAGGABLE);
    } else {
      setAttribute(Attribute.DRAGGABLE, Keywords.NOT_DRAGGABLE);
    }
  }

  public void setDropInitialAfterAdjust(double value, CssUnit unit) {
    setStyle(CssProperty.DROP_INITIAL_AFTER_ADJUST, value, unit);
  }

  public void setDropInitialAfterAdjust(DropInitialAfterAdjust dropInitialAfterAdjust) {
    setStyle(CssProperty.DROP_INITIAL_AFTER_ADJUST, dropInitialAfterAdjust);
  }

  public void setDropInitialAfterAdjust(int value, CssUnit unit) {
    setStyle(CssProperty.DROP_INITIAL_AFTER_ADJUST, value, unit);
  }

  public void setDropInitialAfterAlign(DropInitialAfterAlign dropInitialAfterAlign) {
    setStyle(CssProperty.DROP_INITIAL_AFTER_ALIGN, dropInitialAfterAlign);
  }

  public void setDropInitialBeforeAdjust(double value, CssUnit unit) {
    setStyle(CssProperty.DROP_INITIAL_BEFORE_ADJUST, value, unit);
  }

  public void setDropInitialBeforeAdjust(DropInitialBeforeAdjust dropInitialBeforeAdjust) {
    setStyle(CssProperty.DROP_INITIAL_BEFORE_ADJUST, dropInitialBeforeAdjust);
  }

  public void setDropInitialBeforeAdjust(int value, CssUnit unit) {
    setStyle(CssProperty.DROP_INITIAL_BEFORE_ADJUST, value, unit);
  }

  public void setDropInitialBeforeAlign(DropInitialBeforeAlign dropInitialBeforeAlign) {
    setStyle(CssProperty.DROP_INITIAL_BEFORE_ALIGN, dropInitialBeforeAlign);
  }

  public void setDropInitialSize(double value, CssUnit unit) {
    setStyle(CssProperty.DROP_INITIAL_SIZE, value, unit);
  }

  public void setDropInitialSize(int value, CssUnit unit) {
    setStyle(CssProperty.DROP_INITIAL_SIZE, value, unit);
  }

  public void setDropInitialSize(String dropInitialSize) {
    setStyle(CssProperty.DROP_INITIAL_SIZE, dropInitialSize);
  }

  public void setDropInitialValue(int value) {
    setStyle(CssProperty.DROP_INITIAL_VALUE, value);
  }

  public void setDropInitialValue(String dropInitialValue) {
    setStyle(CssProperty.DROP_INITIAL_VALUE, dropInitialValue);
  }

  public void setDropZone(String dropZone) {
    setAttribute(Attribute.DROPZONE, dropZone);
  }

  public void setElevation(String elevation) {
    setStyle(CssProperty.ELEVATION, elevation);
  }

  public void setEmptyCells(EmptyCells emptyCells) {
    setStyle(CssProperty.EMPTY_CELLS, emptyCells);
  }

  public void setFlex(String flex) {
    setStyle(CssProperty.FLEX, flex);
  }

  public void setFlexBasis(double value, CssUnit unit) {
    setStyle(CssProperty.FLEX_BASIS, value, unit);
  }

  public void setFlexBasis(int value, CssUnit unit) {
    setStyle(CssProperty.FLEX_BASIS, value, unit);
  }

  public void setFlexBasis(String flexBasis) {
    setStyle(CssProperty.FLEX_BASIS, flexBasis);
  }

  public void setFlexDirection(FlexDirection flexDirection) {
    setStyle(CssProperty.FLEX_DIRECTION, flexDirection);
  }

  public void setFlexFlow(String flexFlow) {
    setStyle(CssProperty.FLEX_FLOW, flexFlow);
  }

  public void setFlexGrow(double flexGrow) {
    setStyle(CssProperty.FLEX_GROW, BeeUtils.toString(flexGrow));
  }

  public void setFlexGrow(int flexGrow) {
    setStyle(CssProperty.FLEX_GROW, flexGrow);
  }

  public void setFlexShrink(double flexShrink) {
    setStyle(CssProperty.FLEX_SHRINK, BeeUtils.toString(flexShrink));
  }

  public void setFlexShrink(int flexShrink) {
    setStyle(CssProperty.FLEX_SHRINK, flexShrink);
  }

  public void setFlexWrap(FlexWrap flexWrap) {
    setStyle(CssProperty.FLEX_WRAP, flexWrap);
  }

  public void setFloat(Float value) {
    setStyle(CssProperty.FLOAT, value);
  }

  public void setFloat(String value) {
    setStyle(CssProperty.FLOAT, value);
  }

  public void setFloatOffset(double value, CssUnit unit) {
    setStyle(CssProperty.FLOAT_OFFSET, value, unit);
  }

  public void setFloatOffset(double hor, CssUnit horUnit, double vert, CssUnit vertUnit) {
    setStyle(CssProperty.FLOAT_OFFSET, BeeUtils.joinWords(CssUnit.format(hor, horUnit),
        CssUnit.format(vert, vertUnit)));
  }

  public void setFloatOffset(int value, CssUnit unit) {
    setStyle(CssProperty.FLOAT_OFFSET, value, unit);
  }

  public void setFloatOffset(int hor, CssUnit horUnit, int vert, CssUnit vertUnit) {
    setStyle(CssProperty.FLOAT_OFFSET, BeeUtils.joinWords(CssUnit.format(hor, horUnit),
        CssUnit.format(vert, vertUnit)));
  }

  public void setFloatOffset(String floatOffset) {
    setStyle(CssProperty.FLOAT_OFFSET, floatOffset);
  }

  public void setFont(String font) {
    setStyle(CssProperty.FONT, font);
  }

  public void setFontFamily(String fontFamily) {
    setStyle(CssProperty.FONT_FAMILY, fontFamily);
  }

  public void setFontFeatureSettings(String fontFeatureSettings) {
    setStyle(CssProperty.FONT_FEATURE_SETTINGS, fontFeatureSettings);
  }

  public void setFontKerning(FontKerning fontKerning) {
    setStyle(CssProperty.FONT_KERNING, fontKerning);
  }

  public void setFontLanguageOverride(String fontLanguageOverride) {
    setStyle(CssProperty.FONT_LANGUAGE_OVERRIDE, fontLanguageOverride);
  }

  public void setFontSynthesis(String fontSynthesis) {
    setStyle(CssProperty.FONT_SYNTHESIS, fontSynthesis);
  }

  public void setFontSize(double value, CssUnit unit) {
    setStyle(CssProperty.FONT_SIZE, value, unit);
  }

  public void setFontSize(FontSize fontSize) {
    setStyle(CssProperty.FONT_SIZE, fontSize);
  }

  public void setFontSize(int value, CssUnit unit) {
    setStyle(CssProperty.FONT_SIZE, value, unit);
  }

  public void setFontSizeAdjust(double fontSizeAdjust) {
    setStyle(CssProperty.FONT_SIZE_ADJUST, BeeUtils.toString(fontSizeAdjust));
  }

  public void setFontSizeAdjust(String fontSizeAdjust) {
    setStyle(CssProperty.FONT_SIZE_ADJUST, fontSizeAdjust);
  }

  public void setFontStyle(FontStyle fontStyle) {
    setStyle(CssProperty.FONT_STYLE, fontStyle);
  }

  public void setFontStretch(FontStretch fontStretch) {
    setStyle(CssProperty.FONT_STRETCH, fontStretch);
  }

  public void setFontVariant(FontVariant fontVariant) {
    setStyle(CssProperty.FONT_VARIANT, fontVariant);
  }

  public void setFontVariant(String fontVariant) {
    setStyle(CssProperty.FONT_VARIANT, fontVariant);
  }

  public void setFontVariantAlternates(String fontVariantAlternates) {
    setStyle(CssProperty.FONT_VARIANT_ALTERNATES, fontVariantAlternates);
  }

  public void setFontVariantCaps(FontVariantCaps fontVariantCaps) {
    setStyle(CssProperty.FONT_VARIANT_CAPS, fontVariantCaps);
  }

  public void setFontVariantEastAsian(String fontVariantEastAsian) {
    setStyle(CssProperty.FONT_VARIANT_EAST_ASIAN, fontVariantEastAsian);
  }

  public void setFontVariantLigatures(String fontVariantLigatures) {
    setStyle(CssProperty.FONT_VARIANT_LIGATURES, fontVariantLigatures);
  }

  public void setFontVariantNumeric(String fontVariantNumeric) {
    setStyle(CssProperty.FONT_VARIANT_NUMERIC, fontVariantNumeric);
  }

  public void setFontVariantPosition(FontVariantPosition fontVariantPosition) {
    setStyle(CssProperty.FONT_VARIANT_POSITION, fontVariantPosition);
  }

  public void setFontWeight(FontWeight fontWeight) {
    setStyle(CssProperty.FONT_WEIGHT, fontWeight);
  }

  public void setGrid(String grid) {
    setStyle(CssProperty.GRID, grid);
  }

  public void setGridArea(String gridArea) {
    setStyle(CssProperty.GRID_AREA, gridArea);
  }

  public void setGridAutoColumns(String gridAutoColumns) {
    setStyle(CssProperty.GRID_AUTO_COLUMNS, gridAutoColumns);
  }

  public void setGridAutoFlow(String gridAutoFlow) {
    setStyle(CssProperty.GRID_AUTO_FLOW, gridAutoFlow);
  }

  public void setGridAutoPosition(String gridAutoPosition) {
    setStyle(CssProperty.GRID_AUTO_POSITION, gridAutoPosition);
  }

  public void setGridAutoRows(String gridAutoRows) {
    setStyle(CssProperty.GRID_AUTO_ROWS, gridAutoRows);
  }

  public void setGridColumn(String gridColumn) {
    setStyle(CssProperty.GRID_COLUMN, gridColumn);
  }

  public void setGridColumnEnd(String gridColumnEnd) {
    setStyle(CssProperty.GRID_COLUMN_END, gridColumnEnd);
  }

  public void setGridColumnStart(String gridColumnStart) {
    setStyle(CssProperty.GRID_COLUMN_START, gridColumnStart);
  }

  public void setGridRow(String gridRow) {
    setStyle(CssProperty.GRID_ROW, gridRow);
  }

  public void setGridRowEnd(String gridRowEnd) {
    setStyle(CssProperty.GRID_ROW_END, gridRowEnd);
  }

  public void setGridRowStart(String gridRowStart) {
    setStyle(CssProperty.GRID_ROW_START, gridRowStart);
  }

  public void setGridTemplate(String gridTemplate) {
    setStyle(CssProperty.GRID_TEMPLATE, gridTemplate);
  }

  public void setGridTemplateAreas(String gridTemplateAreas) {
    setStyle(CssProperty.GRID_TEMPLATE_AREAS, gridTemplateAreas);
  }

  public void setGridTemplateColumns(String gridTemplateColumns) {
    setStyle(CssProperty.GRID_TEMPLATE_COLUMNS, gridTemplateColumns);
  }

  public void setGridTemplateRows(String gridTemplateRows) {
    setStyle(CssProperty.GRID_TEMPLATE_ROWS, gridTemplateRows);
  }

  public void setHangingPunctuation(HangingPunctuation hangingPunctuation) {
    setStyle(CssProperty.HANGING_PUNCTUATION, hangingPunctuation);
  }

  public void setHeight(double value, CssUnit unit) {
    setStyle(CssProperty.HEIGHT, value, unit);
  }

  public void setHeight(int value, CssUnit unit) {
    setStyle(CssProperty.HEIGHT, value, unit);
  }

  public void setHeight(String height) {
    setStyle(CssProperty.HEIGHT, height);
  }

  public void setHidden(boolean hidden) {
    setAttribute(Attribute.HIDDEN, hidden);
  }

  public void setHyphens(Hyphens hyphens) {
    setStyle(CssProperty.HYPHENS, hyphens);
  }

  public void setIcon(String icon) {
    setStyle(CssProperty.ICON, icon);
  }

  public void setId(String id) {
    setAttribute(Attribute.ID, id);
  }

  public void setImageOrientation(int value, CssAngle angle) {
    setStyle(CssProperty.IMAGE_ORIENTATION, CssAngle.format(value, angle));
  }

  public void setImageOrientation(String imageOrientation) {
    setStyle(CssProperty.IMAGE_ORIENTATION, imageOrientation);
  }

  public void setImageRendering(ImageRendering imageRendering) {
    setStyle(CssProperty.IMAGE_RENDERING, imageRendering);
  }

  public void setImageResolution(String imageResolution) {
    setStyle(CssProperty.IMAGE_RESOLUTION, imageResolution);
  }

  public void setImeMode(ImeMode imeMode) {
    setStyle(CssProperty.IME_MODE, imeMode);
  }

  public void setInert(boolean inert) {
    setAttribute(Attribute.INERT, inert);
  }

  public void setInlineBoxAlign(InlineBoxAlign inlineBoxAlign) {
    setStyle(CssProperty.INLINE_BOX_ALIGN, inlineBoxAlign);
  }

  public void setInlineBoxAlign(int value) {
    setStyle(CssProperty.INLINE_BOX_ALIGN, value);
  }

  public void setItemId(String itemId) {
    setAttribute(Attribute.ITEM_ID, itemId);
  }

  public void setItemProp(String itemProp) {
    setAttribute(Attribute.ITEM_PROP, itemProp);
  }

  public void setItemRef(String itemRef) {
    setAttribute(Attribute.ITEM_REF, itemRef);
  }

  public void setItemScope(boolean itemScope) {
    setAttribute(Attribute.ITEM_SCOPE, itemScope);
  }

  public void setItemType(String itemType) {
    setAttribute(Attribute.ITEM_TYPE, itemType);
  }

  public void setJustifyContent(JustifyContent justifyContent) {
    setStyle(CssProperty.JUSTIFY_CONTENT, justifyContent);
  }

  public void setLang(String lang) {
    setAttribute(Attribute.LANG, lang);
  }

  public void setLeft(double value, CssUnit unit) {
    setStyle(CssProperty.LEFT, value, unit);
  }

  public void setLeft(int value, CssUnit unit) {
    setStyle(CssProperty.LEFT, value, unit);
  }

  public void setLeft(String left) {
    setStyle(CssProperty.LEFT, left);
  }

  public void setLetterSpacing(double value, CssUnit unit) {
    setStyle(CssProperty.LETTER_SPACING, value, unit);
  }

  public void setLetterSpacing(int value, CssUnit unit) {
    setStyle(CssProperty.LETTER_SPACING, value, unit);
  }

  public void setLetterSpacing(String letterSpacing) {
    setStyle(CssProperty.LETTER_SPACING, letterSpacing);
  }

  public void setLineBreak(LineBreak lineBreak) {
    setStyle(CssProperty.LINE_BREAK, lineBreak);
  }

  public void setLineHeight(double lineHeight) {
    setStyle(CssProperty.LINE_HEIGHT, BeeUtils.toString(lineHeight));
  }

  public void setLineHeight(double value, CssUnit unit) {
    setStyle(CssProperty.LINE_HEIGHT, value, unit);
  }

  public void setLineHeight(int value, CssUnit unit) {
    setStyle(CssProperty.LINE_HEIGHT, value, unit);
  }

  public void setLineHeight(String lineHeight) {
    setStyle(CssProperty.LINE_HEIGHT, lineHeight);
  }

  public void setLineStacking(LineStacking lineStacking) {
    setStyle(CssProperty.LINE_STACKING, lineStacking);
  }

  public void setLineStackingRuby(LineStackingRuby lineStackingRuby) {
    setStyle(CssProperty.LINE_STACKING_RUBY, lineStackingRuby);
  }

  public void setLineStackingShift(LineStackingShift lineStackingShift) {
    setStyle(CssProperty.LINE_STACKING_SHIFT, lineStackingShift);
  }

  public void setLineStackingStrategy(LineStackingStrategy lineStackingStrategy) {
    setStyle(CssProperty.LINE_STACKING_STRATEGY, lineStackingStrategy);
  }

  public void setListStyle(String listStyle) {
    setStyle(CssProperty.LIST_STYLE, listStyle);
  }

  public void setListStyleImage(String listStyleImage) {
    setStyle(CssProperty.LIST_STYLE_IMAGE, listStyleImage);
  }

  public void setListStylePosition(ListStylePosition listStylePosition) {
    setStyle(CssProperty.LIST_STYLE_POSITION, listStylePosition);
  }

  public void setListStyleType(String listStyleType) {
    setStyle(CssProperty.LIST_STYLE_TYPE, listStyleType);
  }

  public void setMargin(double value, CssUnit unit) {
    setStyle(CssProperty.MARGIN, value, unit);
  }

  public void setMargin(double vert, CssUnit vertUnit, double hor, CssUnit horUnit) {
    setStyle(CssProperty.MARGIN, BeeUtils.joinWords(CssUnit.format(vert, vertUnit),
        CssUnit.format(hor, horUnit)));
  }

  public void setMargin(double top, CssUnit topUnit, double hor, CssUnit horUnit,
      double bottom, CssUnit bottomUnit) {
    setStyle(CssProperty.MARGIN, BeeUtils.joinWords(CssUnit.format(top, topUnit),
        CssUnit.format(hor, horUnit), CssUnit.format(bottom, bottomUnit)));
  }

  public void setMargin(double top, CssUnit topUnit, double right, CssUnit rightUnit,
      double bottom, CssUnit bottomUnit, double left, CssUnit leftUnit) {
    setStyle(CssProperty.MARGIN, BeeUtils.joinWords(CssUnit.format(top, topUnit),
        CssUnit.format(right, rightUnit), CssUnit.format(bottom, bottomUnit),
        CssUnit.format(left, leftUnit)));
  }

  public void setMargin(int value, CssUnit unit) {
    setStyle(CssProperty.MARGIN, value, unit);
  }

  public void setMargin(int vert, CssUnit vertUnit, int hor, CssUnit horUnit) {
    setStyle(CssProperty.MARGIN, BeeUtils.joinWords(CssUnit.format(vert, vertUnit),
        CssUnit.format(hor, horUnit)));
  }

  public void setMargin(int top, CssUnit topUnit, int hor, CssUnit horUnit,
      int bottom, CssUnit bottomUnit) {
    setStyle(CssProperty.MARGIN, BeeUtils.joinWords(CssUnit.format(top, topUnit),
        CssUnit.format(hor, horUnit), CssUnit.format(bottom, bottomUnit)));
  }

  public void setMargin(int top, CssUnit topUnit, int right, CssUnit rightUnit,
      int bottom, CssUnit bottomUnit, int left, CssUnit leftUnit) {
    setStyle(CssProperty.MARGIN, BeeUtils.joinWords(CssUnit.format(top, topUnit),
        CssUnit.format(right, rightUnit), CssUnit.format(bottom, bottomUnit),
        CssUnit.format(left, leftUnit)));
  }

  public void setMargin(String margin) {
    setStyle(CssProperty.MARGIN, margin);
  }

  public void setMargin(String vertical, String horizontal) {
    setStyle(CssProperty.MARGIN, BeeUtils.joinWords(vertical, horizontal));
  }

  public void setMargin(String top, String horizontal, String bottom) {
    setStyle(CssProperty.MARGIN, BeeUtils.joinWords(top, horizontal, bottom));
  }

  public void setMargin(String top, String right, String bottom, String left) {
    setStyle(CssProperty.MARGIN, BeeUtils.joinWords(top, right, bottom, left));
  }

  public void setMarginBottom(double value, CssUnit unit) {
    setStyle(CssProperty.MARGIN_BOTTOM, value, unit);
  }

  public void setMarginBottom(int value, CssUnit unit) {
    setStyle(CssProperty.MARGIN_BOTTOM, value, unit);
  }

  public void setMarginBottom(String marginBottom) {
    setStyle(CssProperty.MARGIN_BOTTOM, marginBottom);
  }

  public void setMarginLeft(double value, CssUnit unit) {
    setStyle(CssProperty.MARGIN_LEFT, value, unit);
  }

  public void setMarginLeft(int value, CssUnit unit) {
    setStyle(CssProperty.MARGIN_LEFT, value, unit);
  }

  public void setMarginLeft(String marginLeft) {
    setStyle(CssProperty.MARGIN_LEFT, marginLeft);
  }

  public void setMarginRight(double value, CssUnit unit) {
    setStyle(CssProperty.MARGIN_RIGHT, value, unit);
  }

  public void setMarginRight(int value, CssUnit unit) {
    setStyle(CssProperty.MARGIN_RIGHT, value, unit);
  }

  public void setMarginRight(String marginRight) {
    setStyle(CssProperty.MARGIN_RIGHT, marginRight);
  }

  public void setMarginTop(double value, CssUnit unit) {
    setStyle(CssProperty.MARGIN_TOP, value, unit);
  }

  public void setMarginTop(int value, CssUnit unit) {
    setStyle(CssProperty.MARGIN_TOP, value, unit);
  }

  public void setMarginTop(String marginTop) {
    setStyle(CssProperty.MARGIN_TOP, marginTop);
  }

  public void setMarkerOffset(double value, CssUnit unit) {
    setStyle(CssProperty.MARKER_OFFSET, value, unit);
  }

  public void setMarkerOffset(int value, CssUnit unit) {
    setStyle(CssProperty.MARKER_OFFSET, value, unit);
  }

  public void setMarkerOffset(String markerOffset) {
    setStyle(CssProperty.MARKER_OFFSET, markerOffset);
  }

  public void setMarks(String marks) {
    setStyle(CssProperty.MARKS, marks);
  }

  public void setMarqueeDirection(MarqueeDirection marqueeDirection) {
    setStyle(CssProperty.MARQUEE_DIRECTION, marqueeDirection);
  }

  public void setMarqueeLoop(int marqueeLoop) {
    setStyle(CssProperty.MARQUEE_LOOP, marqueeLoop);
  }

  public void setMarqueeLoop(String marqueeLoop) {
    setStyle(CssProperty.MARQUEE_LOOP, marqueeLoop);
  }

  public void setMarqueePlayCount(int marqueePlayCount) {
    setStyle(CssProperty.MARQUEE_PLAY_COUNT, marqueePlayCount);
  }

  public void setMarqueePlayCount(String marqueePlayCount) {
    setStyle(CssProperty.MARQUEE_PLAY_COUNT, marqueePlayCount);
  }

  public void setMarqueeSpeed(MarqueeSpeed marqueeSpeed) {
    setStyle(CssProperty.MARQUEE_SPEED, marqueeSpeed);
  }

  public void setMarqueeStyle(MarqueeStyle marqueeStyle) {
    setStyle(CssProperty.MARQUEE_STYLE, marqueeStyle);
  }

  public void setMaxHeight(double value, CssUnit unit) {
    setStyle(CssProperty.MAX_HEIGHT, value, unit);
  }

  public void setMaxHeight(int value, CssUnit unit) {
    setStyle(CssProperty.MAX_HEIGHT, value, unit);
  }

  public void setMaxHeight(String maxHeight) {
    setStyle(CssProperty.MAX_HEIGHT, maxHeight);
  }

  public void setMaxLines(int maxLines) {
    setStyle(CssProperty.MAX_LINES, maxLines);
  }

  public void setMaxLines(String maxLines) {
    setStyle(CssProperty.MAX_LINES, maxLines);
  }

  public void setMaxWidth(double value, CssUnit unit) {
    setStyle(CssProperty.MAX_WIDTH, value, unit);
  }

  public void setMaxWidth(int value, CssUnit unit) {
    setStyle(CssProperty.MAX_WIDTH, value, unit);
  }

  public void setMaxWidth(String maxWidth) {
    setStyle(CssProperty.MAX_WIDTH, maxWidth);
  }

  public void setMinHeight(double value, CssUnit unit) {
    setStyle(CssProperty.MIN_HEIGHT, value, unit);
  }

  public void setMinHeight(int value, CssUnit unit) {
    setStyle(CssProperty.MIN_HEIGHT, value, unit);
  }

  public void setMinHeight(String minHeight) {
    setStyle(CssProperty.MIN_HEIGHT, minHeight);
  }

  public void setMinWidth(double value, CssUnit unit) {
    setStyle(CssProperty.MIN_WIDTH, value, unit);
  }

  public void setMinWidth(int value, CssUnit unit) {
    setStyle(CssProperty.MIN_WIDTH, value, unit);
  }

  public void setMinWidth(String minWidth) {
    setStyle(CssProperty.MIN_WIDTH, minWidth);
  }

  public void setMoveTo(String moveTo) {
    setStyle(CssProperty.MOVE_TO, moveTo);
  }

  public void setNavDown(String navDown) {
    setStyle(CssProperty.NAV_DOWN, navDown);
  }

  public void setNavIndex(int navIndex) {
    setStyle(CssProperty.NAV_INDEX, navIndex);
  }

  public void setNavIndex(String navIndex) {
    setStyle(CssProperty.NAV_INDEX, navIndex);
  }

  public void setNavLeft(String navLeft) {
    setStyle(CssProperty.NAV_LEFT, navLeft);
  }

  public void setNavRight(String navRight) {
    setStyle(CssProperty.NAV_RIGHT, navRight);
  }

  public void setNavUp(String navUp) {
    setStyle(CssProperty.NAV_UP, navUp);
  }

  public void setObjectFit(ObjectFit objectFit) {
    setStyle(CssProperty.OBJECT_FIT, objectFit);
  }

  public void setObjectPosition(BackgroundPosition objectPosition) {
    setStyle(CssProperty.OBJECT_POSITION, objectPosition);
  }

  public void setObjectPosition(Collection<BackgroundPosition> values) {
    setStyle(CssProperty.OBJECT_POSITION, Style.join(BeeConst.STRING_SPACE, values));
  }

  public void setObjectPosition(String objectPosition) {
    setStyle(CssProperty.OBJECT_POSITION, objectPosition);
  }

  public void setOnAbort(String onAbort) {
    setAttribute(Attribute.ON_ABORT, onAbort);
  }

  public void setOnBlur(String onBlur) {
    setAttribute(Attribute.ON_BLUR, onBlur);
  }

  public void setOnCancel(String onCancel) {
    setAttribute(Attribute.ON_CANCEL, onCancel);
  }

  public void setOnCanPlay(String onCanPlay) {
    setAttribute(Attribute.ON_CAN_PLAY, onCanPlay);
  }

  public void setOnCanPlayThrough(String onCanPlayThrough) {
    setAttribute(Attribute.ON_CAN_PLAY_THROUGH, onCanPlayThrough);
  }

  public void setOnChange(String onChange) {
    setAttribute(Attribute.ON_CHANGE, onChange);
  }

  public void setOnClick(String onClick) {
    setAttribute(Attribute.ON_CLICK, onClick);
  }

  public void setOnClose(String onClose) {
    setAttribute(Attribute.ON_CLOSE, onClose);
  }

  public void setOnContextMenu(String onContextMenu) {
    setAttribute(Attribute.ON_CONTEXT_MENU, onContextMenu);
  }

  public void setOnCueChange(String onCueChange) {
    setAttribute(Attribute.ON_CUE_CHANGE, onCueChange);
  }

  public void setOnDblClick(String onDblClick) {
    setAttribute(Attribute.ON_DBL_CLICK, onDblClick);
  }

  public void setOnDrag(String onDrag) {
    setAttribute(Attribute.ON_DRAG, onDrag);
  }

  public void setOnDragEnd(String onDragEnd) {
    setAttribute(Attribute.ON_DRAG_END, onDragEnd);
  }

  public void setOnDragEnter(String onDragEnter) {
    setAttribute(Attribute.ON_DRAG_ENTER, onDragEnter);
  }

  public void setOnDragExit(String onDragExit) {
    setAttribute(Attribute.ON_DRAG_EXIT, onDragExit);
  }

  public void setOnDragLeave(String onDragLeave) {
    setAttribute(Attribute.ON_DRAG_LEAVE, onDragLeave);
  }

  public void setOnDragOver(String onDragOver) {
    setAttribute(Attribute.ON_DRAG_OVER, onDragOver);
  }

  public void setOnDragStart(String onDragStart) {
    setAttribute(Attribute.ON_DRAG_START, onDragStart);
  }

  public void setOnDrop(String onDrop) {
    setAttribute(Attribute.ON_DROP, onDrop);
  }

  public void setOnDurationChange(String onDurationChange) {
    setAttribute(Attribute.ON_DURATION_CHANGE, onDurationChange);
  }

  public void setOnEmptied(String onEmptied) {
    setAttribute(Attribute.ON_EMPTIED, onEmptied);
  }

  public void setOnEnded(String onEnded) {
    setAttribute(Attribute.ON_ENDED, onEnded);
  }

  public void setOnError(String onError) {
    setAttribute(Attribute.ON_ERROR, onError);
  }

  public void setOnFocus(String onFocus) {
    setAttribute(Attribute.ON_FOCUS, onFocus);
  }

  public void setOnInput(String onInput) {
    setAttribute(Attribute.ON_INPUT, onInput);
  }

  public void setOnInvalid(String onInvalid) {
    setAttribute(Attribute.ON_INVALID, onInvalid);
  }

  public void setOnKeyDown(String onKeyDown) {
    setAttribute(Attribute.ON_KEY_DOWN, onKeyDown);
  }

  public void setOnKeyPress(String onKeyPress) {
    setAttribute(Attribute.ON_KEY_PRESS, onKeyPress);
  }

  public void setOnKeyUp(String onKeyUp) {
    setAttribute(Attribute.ON_KEY_UP, onKeyUp);
  }

  public void setOnLoad(String onLoad) {
    setAttribute(Attribute.ON_LOAD, onLoad);
  }

  public void setOnLoadedData(String onLoadedData) {
    setAttribute(Attribute.ON_LOADED_DATA, onLoadedData);
  }

  public void setOnLoadedMetaData(String onLoadedMetaData) {
    setAttribute(Attribute.ON_LOADED_META_DATA, onLoadedMetaData);
  }

  public void setOnLoadStart(String onLoadStart) {
    setAttribute(Attribute.ON_LOAD_START, onLoadStart);
  }

  public void setOnMouseDown(String onMouseDown) {
    setAttribute(Attribute.ON_MOUSE_DOWN, onMouseDown);
  }

  public void setOnMouseEnter(String onMouseEnter) {
    setAttribute(Attribute.ON_MOUSE_ENTER, onMouseEnter);
  }

  public void setOnMouseLeave(String onMouseLeave) {
    setAttribute(Attribute.ON_MOUSE_LEAVE, onMouseLeave);
  }

  public void setOnMouseMove(String onMouseMove) {
    setAttribute(Attribute.ON_MOUSE_MOVE, onMouseMove);
  }

  public void setOnMouseOut(String onMouseOut) {
    setAttribute(Attribute.ON_MOUSE_OUT, onMouseOut);
  }

  public void setOnMouseOver(String onMouseOver) {
    setAttribute(Attribute.ON_MOUSE_OVER, onMouseOver);
  }

  public void setOnMouseUp(String onMouseUp) {
    setAttribute(Attribute.ON_MOUSE_UP, onMouseUp);
  }

  public void setOnMouseWheel(String onMouseWheel) {
    setAttribute(Attribute.ON_MOUSE_WHEEL, onMouseWheel);
  }

  public void setOnPause(String onPause) {
    setAttribute(Attribute.ON_PAUSE, onPause);
  }

  public void setOnPlay(String onPlay) {
    setAttribute(Attribute.ON_PLAY, onPlay);
  }

  public void setOnPlaying(String onPlaying) {
    setAttribute(Attribute.ON_PLAYING, onPlaying);
  }

  public void setOnProgress(String onProgress) {
    setAttribute(Attribute.ON_PROGRESS, onProgress);
  }

  public void setOnRateChange(String onRateChange) {
    setAttribute(Attribute.ON_RATE_CHANGE, onRateChange);
  }

  public void setOnReset(String onReset) {
    setAttribute(Attribute.ON_RESET, onReset);
  }

  public void setOnScroll(String onScroll) {
    setAttribute(Attribute.ON_SCROLL, onScroll);
  }

  public void setOnSeeked(String onSeeked) {
    setAttribute(Attribute.ON_SEEKED, onSeeked);
  }

  public void setOnSeeking(String onSeeking) {
    setAttribute(Attribute.ON_SEEKING, onSeeking);
  }

  public void setOnSelect(String onSelect) {
    setAttribute(Attribute.ON_SELECT, onSelect);
  }

  public void setOnShow(String onShow) {
    setAttribute(Attribute.ON_SHOW, onShow);
  }

  public void setOnSort(String onSort) {
    setAttribute(Attribute.ON_SORT, onSort);
  }

  public void setOnStalled(String onStalled) {
    setAttribute(Attribute.ON_STALLED, onStalled);
  }

  public void setOnSubmit(String onSubmit) {
    setAttribute(Attribute.ON_SUBMIT, onSubmit);
  }

  public void setOnSuspend(String onSuspend) {
    setAttribute(Attribute.ON_SUSPEND, onSuspend);
  }

  public void setOnTimeUpdate(String onTimeUpdate) {
    setAttribute(Attribute.ON_TIME_UPDATE, onTimeUpdate);
  }

  public void setOnVolumeChange(String onVolumeChange) {
    setAttribute(Attribute.ON_VOLUME_CHANGE, onVolumeChange);
  }

  public void setOnWaiting(String onWaiting) {
    setAttribute(Attribute.ON_WAITING, onWaiting);
  }

  public void setOpacity(double opacity) {
    setStyle(CssProperty.OPACITY, BeeUtils.toString(opacity));
  }

  public void setOpacity(String opacity) {
    setStyle(CssProperty.OPACITY, opacity);
  }

  public void setOrder(int order) {
    setStyle(CssProperty.ORDER, order);
  }

  public void setOrder(String order) {
    setStyle(CssProperty.ORDER, order);
  }

  public void setOrphans(int orphans) {
    setStyle(CssProperty.ORPHANS, orphans);
  }

  public void setOrphans(String orphans) {
    setStyle(CssProperty.ORPHANS, orphans);
  }

  public void setOutline(String outline) {
    setStyle(CssProperty.OUTLINE, outline);
  }

  public void setOutlineColor(String outlineColor) {
    setStyle(CssProperty.OUTLINE_COLOR, outlineColor);
  }

  public void setOutlineOffset(double value, CssUnit unit) {
    setStyle(CssProperty.OUTLINE_OFFSET, value, unit);
  }

  public void setOutlineOffset(int value, CssUnit unit) {
    setStyle(CssProperty.OUTLINE_OFFSET, value, unit);
  }

  public void setOutlineOffset(String outlineOffset) {
    setStyle(CssProperty.OUTLINE_OFFSET, outlineOffset);
  }

  public void setOutlineStyle(BorderStyle outlineStyle) {
    setStyle(CssProperty.OUTLINE_STYLE, outlineStyle);
  }

  public void setOutlineStyle(String outlineStyle) {
    setStyle(CssProperty.OUTLINE_STYLE, outlineStyle);
  }

  public void setOutlineWidth(BorderWidth outlineWidth) {
    setStyle(CssProperty.OUTLINE_WIDTH, outlineWidth);
  }

  public void setOutlineWidth(double value, CssUnit unit) {
    setStyle(CssProperty.OUTLINE_WIDTH, value, unit);
  }

  public void setOutlineWidth(int value, CssUnit unit) {
    setStyle(CssProperty.OUTLINE_WIDTH, value, unit);
  }

  public void setOutlineWidth(String outlineWidth) {
    setStyle(CssProperty.OUTLINE_WIDTH, outlineWidth);
  }

  public void setOverflow(Overflow overflow) {
    setStyle(CssProperty.OVERFLOW, overflow);
  }

  public void setOverflow(Overflow x, Overflow y) {
    setStyle(CssProperty.OVERFLOW_X, x);
    setStyle(CssProperty.OVERFLOW_Y, y);
  }

  public void setOverflowY(Overflow overflowY) {
    setStyle(CssProperty.OVERFLOW_Y, overflowY);
  }

  public void setOverflowStyle(OverflowStyle overflowStyle) {
    setStyle(CssProperty.OVERFLOW_STYLE, overflowStyle);
  }

  public void setOverflowStyle(String overflowStyle) {
    setStyle(CssProperty.OVERFLOW_STYLE, overflowStyle);
  }

  public void setOverflowWrap(OverflowWrap overflowWrap) {
    setStyle(CssProperty.OVERFLOW_WRAP, overflowWrap);
  }

  public void setOverflowX(Overflow overflowX) {
    setStyle(CssProperty.OVERFLOW_X, overflowX);
  }

  public void setPadding(double value, CssUnit unit) {
    setStyle(CssProperty.PADDING, value, unit);
  }

  public void setPadding(double vert, CssUnit vertUnit, double hor, CssUnit horUnit) {
    setStyle(CssProperty.PADDING, BeeUtils.joinWords(CssUnit.format(vert, vertUnit),
        CssUnit.format(hor, horUnit)));
  }

  public void setPadding(double top, CssUnit topUnit, double hor, CssUnit horUnit,
      double bottom, CssUnit bottomUnit) {
    setStyle(CssProperty.PADDING, BeeUtils.joinWords(CssUnit.format(top, topUnit),
        CssUnit.format(hor, horUnit), CssUnit.format(bottom, bottomUnit)));
  }

  public void setPadding(double top, CssUnit topUnit, double right, CssUnit rightUnit,
      double bottom, CssUnit bottomUnit, double left, CssUnit leftUnit) {
    setStyle(CssProperty.PADDING, BeeUtils.joinWords(CssUnit.format(top, topUnit),
        CssUnit.format(right, rightUnit), CssUnit.format(bottom, bottomUnit),
        CssUnit.format(left, leftUnit)));
  }

  public void setPadding(int value, CssUnit unit) {
    setStyle(CssProperty.PADDING, value, unit);
  }

  public void setPadding(int vert, CssUnit vertUnit, int hor, CssUnit horUnit) {
    setStyle(CssProperty.PADDING, BeeUtils.joinWords(CssUnit.format(vert, vertUnit),
        CssUnit.format(hor, horUnit)));
  }

  public void setPadding(int top, CssUnit topUnit, int hor, CssUnit horUnit,
      int bottom, CssUnit bottomUnit) {
    setStyle(CssProperty.PADDING, BeeUtils.joinWords(CssUnit.format(top, topUnit),
        CssUnit.format(hor, horUnit), CssUnit.format(bottom, bottomUnit)));
  }

  public void setPadding(int top, CssUnit topUnit, int right, CssUnit rightUnit,
      int bottom, CssUnit bottomUnit, int left, CssUnit leftUnit) {
    setStyle(CssProperty.PADDING, BeeUtils.joinWords(CssUnit.format(top, topUnit),
        CssUnit.format(right, rightUnit), CssUnit.format(bottom, bottomUnit),
        CssUnit.format(left, leftUnit)));
  }

  public void setPadding(String padding) {
    setStyle(CssProperty.PADDING, padding);
  }

  public void setPadding(String vertical, String horizontal) {
    setStyle(CssProperty.PADDING, BeeUtils.joinWords(vertical, horizontal));
  }

  public void setPadding(String top, String horizontal, String bottom) {
    setStyle(CssProperty.PADDING, BeeUtils.joinWords(top, horizontal, bottom));
  }

  public void setPadding(String top, String right, String bottom, String left) {
    setStyle(CssProperty.PADDING, BeeUtils.joinWords(top, right, bottom, left));
  }

  public void setPaddingBottom(double value, CssUnit unit) {
    setStyle(CssProperty.PADDING_BOTTOM, value, unit);
  }

  public void setPaddingBottom(int value, CssUnit unit) {
    setStyle(CssProperty.PADDING_BOTTOM, value, unit);
  }

  public void setPaddingBottom(String paddingBottom) {
    setStyle(CssProperty.PADDING_BOTTOM, paddingBottom);
  }

  public void setPaddingLeft(double value, CssUnit unit) {
    setStyle(CssProperty.PADDING_LEFT, value, unit);
  }

  public void setPaddingLeft(int value, CssUnit unit) {
    setStyle(CssProperty.PADDING_LEFT, value, unit);
  }

  public void setPaddingLeft(String paddingLeft) {
    setStyle(CssProperty.PADDING_LEFT, paddingLeft);
  }

  public void setPaddingRight(double value, CssUnit unit) {
    setStyle(CssProperty.PADDING_RIGHT, value, unit);
  }

  public void setPaddingRight(int value, CssUnit unit) {
    setStyle(CssProperty.PADDING_RIGHT, value, unit);
  }

  public void setPaddingRight(String paddingRight) {
    setStyle(CssProperty.PADDING_RIGHT, paddingRight);
  }

  public void setPaddingTop(double value, CssUnit unit) {
    setStyle(CssProperty.PADDING_TOP, value, unit);
  }

  public void setPaddingTop(int value, CssUnit unit) {
    setStyle(CssProperty.PADDING_TOP, value, unit);
  }

  public void setPaddingTop(String paddingTop) {
    setStyle(CssProperty.PADDING_TOP, paddingTop);
  }

  public void setPage(String page) {
    setStyle(CssProperty.PAGE, page);
  }

  public void setPageBreakAfter(PageBreakAfter pageBreakAfter) {
    setStyle(CssProperty.PAGE_BREAK_AFTER, pageBreakAfter);
  }

  public void setPageBreakBefore(PageBreakBefore pageBreakBefore) {
    setStyle(CssProperty.PAGE_BREAK_BEFORE, pageBreakBefore);
  }

  public void setPageBreakInside(PageBreakInside pageBreakInside) {
    setStyle(CssProperty.PAGE_BREAK_INSIDE, pageBreakInside);
  }

  public void setPagePolicy(PagePolicy pagePolicy) {
    setStyle(CssProperty.PAGE_POLICY, pagePolicy);
  }

  public void setPause(int value, CssTime time) {
    removeStyle(CssProperty.PAUSE);
    setStyle(CssProperty.PAUSE_BEFORE, CssTime.format(value, time));
    setStyle(CssProperty.PAUSE_AFTER, CssTime.format(value, time));
  }

  public void setPause(int before, CssTime beforeTime, int after, CssTime afterTime) {
    removeStyle(CssProperty.PAUSE);
    setStyle(CssProperty.PAUSE_BEFORE, CssTime.format(before, beforeTime));
    setStyle(CssProperty.PAUSE_AFTER, CssTime.format(after, afterTime));
  }

  public void setPause(Pause pause) {
    removeStyle(CssProperty.PAUSE);
    setStyle(CssProperty.PAUSE_BEFORE, pause);
    setStyle(CssProperty.PAUSE_AFTER, pause);
  }

  public void setPause(Pause before, Pause after) {
    removeStyle(CssProperty.PAUSE);
    setStyle(CssProperty.PAUSE_BEFORE, before);
    setStyle(CssProperty.PAUSE_AFTER, after);
  }

  public void setPauseAfter(int value, CssTime time) {
    setStyle(CssProperty.PAUSE_AFTER, CssTime.format(value, time));
  }

  public void setPauseAfter(Pause pauseAfter) {
    setStyle(CssProperty.PAUSE_AFTER, pauseAfter);
  }

  public void setPauseBefore(int value, CssTime time) {
    setStyle(CssProperty.PAUSE_BEFORE, CssTime.format(value, time));
  }

  public void setPauseBefore(Pause pauseBefore) {
    setStyle(CssProperty.PAUSE_BEFORE, pauseBefore);
  }

  public void setPerspective(double value, CssUnit unit) {
    setStyle(CssProperty.PERSPECTIVE, value, unit);
  }

  public void setPerspective(int value, CssUnit unit) {
    setStyle(CssProperty.PERSPECTIVE, value, unit);
  }

  public void setPerspective(String perspective) {
    setStyle(CssProperty.PERSPECTIVE, perspective);
  }

  public void setPerspectiveOrigin(double value, CssUnit unit) {
    setStyle(CssProperty.PERSPECTIVE_ORIGIN, value, unit);
  }

  public void setPerspectiveOrigin(double hor, CssUnit horUnit, double vert, CssUnit vertUnit) {
    setStyle(CssProperty.PERSPECTIVE_ORIGIN, BeeUtils.joinWords(CssUnit.format(hor, horUnit),
        CssUnit.format(vert, vertUnit)));
  }

  public void setPerspectiveOrigin(int value, CssUnit unit) {
    setStyle(CssProperty.PERSPECTIVE_ORIGIN, value, unit);
  }

  public void setPerspectiveOrigin(int hor, CssUnit horUnit, int vert, CssUnit vertUnit) {
    setStyle(CssProperty.PERSPECTIVE_ORIGIN, BeeUtils.joinWords(CssUnit.format(hor, horUnit),
        CssUnit.format(vert, vertUnit)));
  }

  public void setPerspectiveOrigin(PerspectiveOrigin perspectiveOrigin) {
    setStyle(CssProperty.PERSPECTIVE_ORIGIN, perspectiveOrigin);
  }

  public void setPerspectiveOrigin(PerspectiveOrigin horizontal, PerspectiveOrigin vertical) {
    if (horizontal == null && vertical == null) {
      removeStyle(CssProperty.PERSPECTIVE_ORIGIN);
    } else {
      PerspectiveOrigin first = BeeUtils.nvl(horizontal, PerspectiveOrigin.CENTER);
      PerspectiveOrigin second = BeeUtils.nvl(vertical, PerspectiveOrigin.CENTER);
      setStyle(CssProperty.PERSPECTIVE_ORIGIN,
          BeeUtils.joinWords(first.getCssName(), second.getCssName()));
    }
  }

  public void setPerspectiveOrigin(String perspectiveOrigin) {
    setStyle(CssProperty.PERSPECTIVE_ORIGIN, perspectiveOrigin);
  }

  public void setPitch(double value, CssFrequency frequency) {
    setStyle(CssProperty.PITCH, CssFrequency.format(value, frequency));
  }

  public void setPitch(int value, CssFrequency frequency) {
    setStyle(CssProperty.PITCH, CssFrequency.format(value, frequency));
  }

  public void setPitch(Pitch pitch) {
    setStyle(CssProperty.PITCH, pitch);
  }

  public void setPitch(String pitch) {
    setStyle(CssProperty.PITCH, pitch);
  }

  public void setPitchRange(double pitchRange) {
    setStyle(CssProperty.PITCH_RANGE, BeeUtils.toString(pitchRange));
  }

  public void setPitchRange(int pitchRange) {
    setStyle(CssProperty.PITCH_RANGE, pitchRange);
  }

  public void setPitchRange(String pitchRange) {
    setStyle(CssProperty.PITCH_RANGE, pitchRange);
  }

  public void setPlayDuring(String playDuring) {
    setStyle(CssProperty.PLAY_DURING, playDuring);
  }

  public void setPosition(Position position) {
    setStyle(CssProperty.POSITION, position);
  }

  public void setPresentationLevel(int presentationLevel) {
    setStyle(CssProperty.PRESENTATION_LEVEL, presentationLevel);
  }

  public void setPresentationLevel(PresentationLevel presentationLevel) {
    setStyle(CssProperty.PRESENTATION_LEVEL, presentationLevel);
  }

  public void setPunctuationTrim(PunctuationTrim punctuationTrim) {
    setStyle(CssProperty.PUNCTUATION_TRIM, punctuationTrim);
  }

  public void setQuotes(String quotes) {
    setStyle(CssProperty.QUOTES, quotes);
  }

  public void setRenderingIntent(RenderingIntent renderingIntent) {
    setStyle(CssProperty.RENDERING_INTENT, renderingIntent);
  }

  public void setResize(Resize resize) {
    setStyle(CssProperty.RESIZE, resize);
  }

  public void setRest(int value, CssTime time) {
    removeStyle(CssProperty.REST);
    setStyle(CssProperty.REST_BEFORE, CssTime.format(value, time));
    setStyle(CssProperty.REST_AFTER, CssTime.format(value, time));
  }

  public void setRest(int before, CssTime beforeTime, int after, CssTime afterTime) {
    removeStyle(CssProperty.REST);
    setStyle(CssProperty.REST_BEFORE, CssTime.format(before, beforeTime));
    setStyle(CssProperty.REST_AFTER, CssTime.format(after, afterTime));
  }

  public void setRest(Rest rest) {
    removeStyle(CssProperty.REST);
    setStyle(CssProperty.REST_BEFORE, rest);
    setStyle(CssProperty.REST_AFTER, rest);
  }

  public void setRest(Rest before, Rest after) {
    removeStyle(CssProperty.REST);
    setStyle(CssProperty.REST_BEFORE, before);
    setStyle(CssProperty.REST_AFTER, after);
  }

  public void setRest(String rest) {
    setStyle(CssProperty.REST, rest);
  }

  public void setRestAfter(int value, CssTime time) {
    setStyle(CssProperty.REST_AFTER, CssTime.format(value, time));
  }

  public void setRestAfter(Rest restAfter) {
    setStyle(CssProperty.REST_AFTER, restAfter);
  }

  public void setRestBefore(int value, CssTime time) {
    setStyle(CssProperty.REST_BEFORE, CssTime.format(value, time));
  }

  public void setRestBefore(Rest restBefore) {
    setStyle(CssProperty.REST_BEFORE, restBefore);
  }

  public void setRichness(double richness) {
    setStyle(CssProperty.RICHNESS, BeeUtils.toString(richness));
  }

  public void setRichness(int richness) {
    setStyle(CssProperty.RICHNESS, richness);
  }

  public void setRichness(String richness) {
    setStyle(CssProperty.RICHNESS, richness);
  }

  public void setRight(double value, CssUnit unit) {
    setStyle(CssProperty.RIGHT, value, unit);
  }

  public void setRight(int value, CssUnit unit) {
    setStyle(CssProperty.RIGHT, value, unit);
  }

  public void setRight(String right) {
    setStyle(CssProperty.RIGHT, right);
  }

  public void setRotation(double value, CssAngle angle) {
    setStyle(CssProperty.ROTATION, CssAngle.format(value, angle));
  }

  public void setRotation(int value, CssAngle angle) {
    setStyle(CssProperty.ROTATION, CssAngle.format(value, angle));
  }

  public void setRotation(String rotation) {
    setStyle(CssProperty.ROTATION, rotation);
  }

  public void setRotationPoint(BackgroundPosition rotationPoint) {
    setStyle(CssProperty.ROTATION_POINT, rotationPoint);
  }

  public void setRotationPoint(BackgroundPosition first, BackgroundPosition second) {
    if (first == null && second == null) {
      removeStyle(CssProperty.ROTATION_POINT);
    } else {
      setStyle(CssProperty.ROTATION_POINT, Style.join(BeeConst.STRING_SPACE, first, second));
    }
  }

  public void setRotationPoint(double value, CssUnit unit) {
    setStyle(CssProperty.ROTATION_POINT, value, unit);
  }

  public void setRotationPoint(double v1, CssUnit u1, double v2, CssUnit u2) {
    setStyle(CssProperty.ROTATION_POINT, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setRotationPoint(int value, CssUnit unit) {
    setStyle(CssProperty.ROTATION_POINT, value, unit);
  }

  public void setRotationPoint(int v1, CssUnit u1, int v2, CssUnit u2) {
    setStyle(CssProperty.ROTATION_POINT, BeeUtils.joinWords(CssUnit.format(v1, u1),
        CssUnit.format(v2, u2)));
  }

  public void setRotationPoint(String rotationPoint) {
    setStyle(CssProperty.ROTATION_POINT, rotationPoint);
  }

  public void setRubyAlign(RubyAlign rubyAlign) {
    setStyle(CssProperty.RUBY_ALIGN, rubyAlign);
  }

  public void setRubyMerge(RubyMerge rubyMerge) {
    setStyle(CssProperty.RUBY_MERGE, rubyMerge);
  }

  public void setRubyPosition(RubyPosition rubyPosition) {
    setStyle(CssProperty.RUBY_POSITION, rubyPosition);
  }

  public void setRubyPosition(RubyPosition p1, RubyPosition p2) {
    setStyle(CssProperty.RUBY_POSITION, Style.join(BeeConst.STRING_SPACE, p1, p2));
  }

  public void setSize(String size) {
    setStyle(CssProperty.SIZE, size);
  }

  public void setSpeak(Speak speak) {
    setStyle(CssProperty.SPEAK, speak);
  }

  public void setSpeakAs(Collection<SpeakAs> values) {
    setStyle(CssProperty.SPEAK_AS, Style.join(BeeConst.STRING_SPACE, values));
  }

  public void setSpeakAs(SpeakAs speakAs) {
    setStyle(CssProperty.SPEAK_AS, speakAs);
  }

  public void setSpeakHeader(SpeakHeader speakHeader) {
    setStyle(CssProperty.SPEAK_HEADER, speakHeader);
  }

  public void setSpeakNumeral(SpeakNumeral speakNumeral) {
    setStyle(CssProperty.SPEAK_NUMERAL, speakNumeral);
  }

  public void setSpeakPunctuation(SpeakPunctuation speakPunctuation) {
    setStyle(CssProperty.SPEAK_PUNCTUATION, speakPunctuation);
  }

  public void setSpeechRate(double speechRate) {
    setStyle(CssProperty.SPEECH_RATE, BeeUtils.toString(speechRate));
  }

  public void setSpeechRate(int speechRate) {
    setStyle(CssProperty.SPEECH_RATE, speechRate);
  }

  public void setSpeechRate(SpeechRate speechRate) {
    setStyle(CssProperty.SPEECH_RATE, speechRate);
  }

  public void setSpellCheck(Boolean spellCheck) {
    if (spellCheck == null) {
      removeAttribute(Attribute.SPELL_CHECK);
    } else if (spellCheck) {
      setAttribute(Attribute.SPELL_CHECK, Keywords.SPELL_CHECK_ENABLED);
    } else {
      setAttribute(Attribute.SPELL_CHECK, Keywords.SPELL_CHECK_DISABLED);
    }
  }

  public void setStyle(String name, double value, CssUnit unit) {
    setStyle(name, CssUnit.format(value, unit));
  }

  public void setStyle(String name, HasCssName value) {
    if (value == null) {
      removeStyle(name);
    } else {
      setStyle(name, value.getCssName());
    }
  }

  public void setStyle(String name, int value) {
    setStyle(name, Integer.toString(value));
  }

  public void setStyle(String name, int value, CssUnit unit) {
    setStyle(name, CssUnit.format(value, unit));
  }

  public void setStyle(String name, String value) {
    if (value == null) {
      removeStyle(name);

    } else if (!BeeUtils.isEmpty(name)) {
      Style style = findStyle(name);
      if (style == null) {
        styles.add(new Style(name, value));
      } else {
        style.setValue(value);
      }
    }
  }

  public void setStress(double stress) {
    setStyle(CssProperty.STRESS, BeeUtils.toString(stress));
  }

  public void setStress(int stress) {
    setStyle(CssProperty.STRESS, stress);
  }

  public void setStringSet(String stringSet) {
    setStyle(CssProperty.STRING_SET, stringSet);
  }

  public void setTabIndex(int tabIndex) {
    setAttribute(Attribute.TAB_INDEX, BeeUtils.toString(tabIndex));
  }

  public void setTableLayout(TableLayout tableLayout) {
    setStyle(CssProperty.TABLE_LAYOUT, tableLayout);
  }

  public void setTabSize(double value, CssUnit unit) {
    setStyle(CssProperty.TAB_SIZE, value, unit);
  }

  public void setTabSize(int tabSize) {
    setStyle(CssProperty.TAB_SIZE, tabSize);
  }

  public void setTabSize(int value, CssUnit unit) {
    setStyle(CssProperty.TAB_SIZE, value, unit);
  }

  public void setTarget(String target) {
    setStyle(CssProperty.TARGET, target);
  }

  public void setTargetName(String targetName) {
    setStyle(CssProperty.TARGET_NAME, targetName);
  }

  public void setTargetName(TargetName targetName) {
    setStyle(CssProperty.TARGET_NAME, targetName);
  }

  public void setTargetNew(TargetNew targetNew) {
    setStyle(CssProperty.TARGET_NEW, targetNew);
  }

  public void setTargetPosition(TargetPosition targetPosition) {
    setStyle(CssProperty.TARGET_POSITION, targetPosition);
  }

  public void setTextAlign(TextAlign textAlign) {
    setStyle(CssProperty.TEXT_ALIGN, textAlign);
  }

  public void setTextAlignLast(TextAlignLast textAlignLast) {
    setStyle(CssProperty.TEXT_ALIGN_LAST, textAlignLast);
  }

  public void setTextDecoration(String textDecoration) {
    setStyle(CssProperty.TEXT_DECORATION, textDecoration);
  }

  public void setTextDecorationColor(Color color) {
    if (color == null || BeeUtils.isEmpty(color.getForeground())) {
      removeStyle(CssProperty.TEXT_DECORATION_COLOR);
    } else {
      setStyle(CssProperty.TEXT_DECORATION_COLOR, color.getForeground());
    }
  }

  public void setTextDecorationColor(String textDecorationColor) {
    setStyle(CssProperty.TEXT_DECORATION_COLOR, textDecorationColor);
  }

  public void setTextDecorationLine(Collection<TextDecorationLine> values) {
    setStyle(CssProperty.TEXT_DECORATION_LINE, Style.join(BeeConst.STRING_SPACE, values));
  }

  public void setTextDecorationLine(TextDecorationLine textDecorationLine) {
    setStyle(CssProperty.TEXT_DECORATION_LINE, textDecorationLine);
  }

  public void setTextDecorationSkip(Collection<TextDecorationSkip> values) {
    setStyle(CssProperty.TEXT_DECORATION_SKIP, Style.join(BeeConst.STRING_SPACE, values));
  }

  public void setTextDecorationSkip(TextDecorationSkip textDecorationSkip) {
    setStyle(CssProperty.TEXT_DECORATION_SKIP, textDecorationSkip);
  }

  public void setTextDecorationStyle(TextDecorationStyle textDecorationStyle) {
    setStyle(CssProperty.TEXT_DECORATION_STYLE, textDecorationStyle);
  }

  public void setTextEmphasis(String textEmphasis) {
    setStyle(CssProperty.TEXT_EMPHASIS, textEmphasis);
  }

  public void setTextEmphasisColor(Color color) {
    if (color == null || BeeUtils.isEmpty(color.getForeground())) {
      removeStyle(CssProperty.TEXT_EMPHASIS_COLOR);
    } else {
      setStyle(CssProperty.TEXT_EMPHASIS_COLOR, color.getForeground());
    }
  }

  public void setTextEmphasisColor(String textEmphasisColor) {
    setStyle(CssProperty.TEXT_EMPHASIS_COLOR, textEmphasisColor);
  }

  public void setTextEmphasisPosition(TextEmphasisPosition textEmphasisPosition) {
    setStyle(CssProperty.TEXT_EMPHASIS_POSITION, textEmphasisPosition);
  }

  public void setTextEmphasisPosition(TextEmphasisPosition p1, TextEmphasisPosition p2) {
    setStyle(CssProperty.TEXT_EMPHASIS_POSITION, Style.join(BeeConst.STRING_SPACE, p1, p2));
  }

  public void setTextEmphasisStyle(Collection<TextEmphasisStyle> values) {
    setStyle(CssProperty.TEXT_EMPHASIS_STYLE, Style.join(BeeConst.STRING_SPACE, values));
  }

  public void setTextEmphasisStyle(String textEmphasisStyle) {
    setStyle(CssProperty.TEXT_EMPHASIS_STYLE, textEmphasisStyle);
  }

  public void setTextEmphasisStyle(TextEmphasisStyle textEmphasisStyle) {
    setStyle(CssProperty.TEXT_EMPHASIS_STYLE, textEmphasisStyle);
  }

  public void setTextHeight(TextHeight textHeight) {
    setStyle(CssProperty.TEXT_HEIGHT, textHeight);
  }

  public void setTextIndent(double value, CssUnit unit) {
    setStyle(CssProperty.TEXT_INDENT, value, unit);
  }

  public void setTextIndent(int value, CssUnit unit) {
    setStyle(CssProperty.TEXT_INDENT, value, unit);
  }

  public void setTextIndent(String textIndent) {
    setStyle(CssProperty.TEXT_INDENT, textIndent);
  }

  public void setTextJustify(TextJustify textJustify) {
    setStyle(CssProperty.TEXT_JUSTIFY, textJustify);
  }

  public void setTextOutline(String textOutline) {
    setStyle(CssProperty.TEXT_OUTLINE, textOutline);
  }

  public void setTextOverflow(String textOverflow) {
    setStyle(CssProperty.TEXT_OVERFLOW, textOverflow);
  }

  public void setTextOverflow(TextOverflow textOverflow) {
    setStyle(CssProperty.TEXT_OVERFLOW, textOverflow);
  }

  public void setTextOverflow(TextOverflow v1, TextOverflow v2) {
    setStyle(CssProperty.TEXT_OVERFLOW, Style.join(BeeConst.STRING_SPACE, v1, v2));
  }

  public void setTextShadow(String textShadow) {
    setStyle(CssProperty.TEXT_SHADOW, textShadow);
  }

  public void setTextSpaceCollapse(String textSpaceCollapse) {
    setStyle(CssProperty.TEXT_SPACE_COLLAPSE, textSpaceCollapse);
  }

  public void setTextTransform(TextTransform textTransform) {
    setStyle(CssProperty.TEXT_TRANSFORM, textTransform);
  }

  public void setTextUnderlinePosition(TextUnderlinePosition textUnderlinePosition) {
    setStyle(CssProperty.TEXT_UNDERLINE_POSITION, textUnderlinePosition);
  }

  public void setTextUnderlinePosition(TextUnderlinePosition v1, TextUnderlinePosition v2) {
    setStyle(CssProperty.TEXT_UNDERLINE_POSITION, Style.join(BeeConst.STRING_SPACE, v1, v2));
  }

  public void setTextWrap(String textWrap) {
    setStyle(CssProperty.TEXT_WRAP, textWrap);
  }

  public void setTitle(String title) {
    setAttribute(Attribute.TITLE, title);
  }

  public void setTop(double value, CssUnit unit) {
    setStyle(CssProperty.TOP, value, unit);
  }

  public void setTop(int value, CssUnit unit) {
    setStyle(CssProperty.TOP, value, unit);
  }

  public void setTop(String top) {
    setStyle(CssProperty.TOP, top);
  }

  public void setTransform(String transform) {
    setStyle(CssProperty.TRANSFORM, transform);
  }

  public void setTransformOrigin(double value, CssUnit unit) {
    setStyle(CssProperty.TRANSFORM_ORIGIN, value, unit);
  }

  public void setTransformOrigin(double hor, CssUnit horUnit, double vert, CssUnit vertUnit) {
    setStyle(CssProperty.TRANSFORM_ORIGIN, BeeUtils.joinWords(CssUnit.format(hor, horUnit),
        CssUnit.format(vert, vertUnit)));
  }

  public void setTransformOrigin(int value, CssUnit unit) {
    setStyle(CssProperty.TRANSFORM_ORIGIN, value, unit);
  }

  public void setTransformOrigin(int hor, CssUnit horUnit, int vert, CssUnit vertUnit) {
    setStyle(CssProperty.TRANSFORM_ORIGIN, BeeUtils.joinWords(CssUnit.format(hor, horUnit),
        CssUnit.format(vert, vertUnit)));
  }

  public void setTransformOrigin(String transformOrigin) {
    setStyle(CssProperty.TRANSFORM_ORIGIN, transformOrigin);
  }

  public void setTransformOrigin(TransformOrigin transformOrigin) {
    setStyle(CssProperty.TRANSFORM_ORIGIN, transformOrigin);
  }

  public void setTransformOrigin(TransformOrigin horizontal, TransformOrigin vertical) {
    if (horizontal == null && vertical == null) {
      removeStyle(CssProperty.TRANSFORM_ORIGIN);
    } else {
      TransformOrigin first = BeeUtils.nvl(horizontal, TransformOrigin.CENTER);
      TransformOrigin second = BeeUtils.nvl(vertical, TransformOrigin.CENTER);
      setStyle(CssProperty.TRANSFORM_ORIGIN,
          BeeUtils.joinWords(first.getCssName(), second.getCssName()));
    }
  }

  public void setTransformStyle(TransformStyle transformStyle) {
    setStyle(CssProperty.TRANSFORM_STYLE, transformStyle);
  }

  public void setTransition(String transition) {
    setStyle(CssProperty.TRANSITION, transition);
  }

  public void setTransitionDelay(double value, CssTime time) {
    setStyle(CssProperty.TRANSITION_DELAY, CssTime.format(value, time));
  }

  public void setTransitionDelay(int value, CssTime time) {
    setStyle(CssProperty.TRANSITION_DELAY, CssTime.format(value, time));
  }

  public void setTransitionDelay(String transitionDelay) {
    setStyle(CssProperty.TRANSITION_DELAY, transitionDelay);
  }

  public void setTransitionDuration(double value, CssTime time) {
    setStyle(CssProperty.TRANSITION_DURATION, CssTime.format(value, time));
  }

  public void setTransitionDuration(int value, CssTime time) {
    setStyle(CssProperty.TRANSITION_DURATION, CssTime.format(value, time));
  }

  public void setTransitionDuration(String transitionDuration) {
    setStyle(CssProperty.TRANSITION_DURATION, transitionDuration);
  }

  public void setTransitionProperty(String transitionProperty) {
    setStyle(CssProperty.TRANSITION_PROPERTY, transitionProperty);
  }

  public void setTransitionTimingFunction(String transitionTimingFunction) {
    setStyle(CssProperty.TRANSITION_TIMING_FUNCTION, transitionTimingFunction);
  }

  public void setTransitionTimingFunction(TransitionTimingFunction transitionTimingFunction) {
    setStyle(CssProperty.TRANSITION_TIMING_FUNCTION, transitionTimingFunction);
  }

  public void setTranslate(Boolean translate) {
    if (translate == null) {
      removeAttribute(Attribute.TRANSLATE);
    } else if (translate) {
      setAttribute(Attribute.TRANSLATE, Keywords.TRANSLATION_ENABLED);
    } else {
      setAttribute(Attribute.TRANSLATE, Keywords.TRANSLATION_DISABLED);
    }
  }

  public void setUnicodeBidi(UnicodeBidi unicodeBidi) {
    setStyle(CssProperty.UNICODE_BIDI, unicodeBidi);
  }

  public void setVerticalAlign(double value, CssUnit unit) {
    setStyle(CssProperty.VERTICAL_ALIGN, value, unit);
  }

  public void setVerticalAlign(int value, CssUnit unit) {
    setStyle(CssProperty.VERTICAL_ALIGN, value, unit);
  }

  public void setVerticalAlign(VerticalAlign verticalAlign) {
    setStyle(CssProperty.VERTICAL_ALIGN, verticalAlign);
  }

  public void setVisibility(Visibility visibility) {
    setStyle(CssProperty.VISIBILITY, visibility);
  }

  public void setVoiceBalance(double voiceBalance) {
    setStyle(CssProperty.VOICE_BALANCE, BeeUtils.toString(voiceBalance));
  }

  public void setVoiceBalance(int voiceBalance) {
    setStyle(CssProperty.VOICE_BALANCE, voiceBalance);
  }

  public void setVoiceBalance(VoiceBalance voiceBalance) {
    setStyle(CssProperty.VOICE_BALANCE, voiceBalance);
  }

  public void setVoiceDuration(double value, CssTime time) {
    setStyle(CssProperty.VOICE_DURATION, CssTime.format(value, time));
  }

  public void setVoiceDuration(int value, CssTime time) {
    setStyle(CssProperty.VOICE_DURATION, CssTime.format(value, time));
  }

  public void setVoiceDuration(String voiceDuration) {
    setStyle(CssProperty.VOICE_DURATION, voiceDuration);
  }

  public void setVoiceFamily(String voiceFamily) {
    setStyle(CssProperty.VOICE_FAMILY, voiceFamily);
  }

  public void setVoicePitch(String voicePitch) {
    setStyle(CssProperty.VOICE_PITCH, voicePitch);
  }

  public void setVoiceRange(String voiceRange) {
    setStyle(CssProperty.VOICE_RANGE, voiceRange);
  }

  public void setVoiceRate(double percentage) {
    setStyle(CssProperty.VOICE_RATE, CssUnit.format(percentage, CssUnit.PCT));
  }

  public void setVoiceRate(int percentage) {
    setStyle(CssProperty.VOICE_RATE, CssUnit.format(percentage, CssUnit.PCT));
  }

  public void setVoiceRate(String voiceRate) {
    setStyle(CssProperty.VOICE_RATE, voiceRate);
  }

  public void setVoiceRate(VoiceRate voiceRate) {
    setStyle(CssProperty.VOICE_RATE, voiceRate);
  }

  public void setVoiceStress(VoiceStress voiceStress) {
    setStyle(CssProperty.VOICE_STRESS, voiceStress);
  }

  public void setVoiceVolume(String voiceVolume) {
    setStyle(CssProperty.VOICE_VOLUME, voiceVolume);
  }

  public void setVolume(double volume) {
    setStyle(CssProperty.VOLUME, BeeUtils.toString(volume));
  }

  public void setVolume(int volume) {
    setStyle(CssProperty.VOLUME, volume);
  }

  public void setVolume(String volume) {
    setStyle(CssProperty.VOLUME, volume);
  }

  public void setVolume(Volume volume) {
    setStyle(CssProperty.VOLUME, volume);
  }

  public void setWhiteSpace(WhiteSpace whiteSpace) {
    setStyle(CssProperty.WHITE_SPACE, whiteSpace);
  }

  public void setWidows(int widows) {
    setStyle(CssProperty.WIDOWS, widows);
  }

  public void setWidows(String widows) {
    setStyle(CssProperty.WIDOWS, widows);
  }

  public void setWidth(double value, CssUnit unit) {
    setStyle(CssProperty.WIDTH, value, unit);
  }

  public void setWidth(int value, CssUnit unit) {
    setStyle(CssProperty.WIDTH, value, unit);
  }

  public void setWidth(String width) {
    setStyle(CssProperty.WIDTH, width);
  }

  public void setWordBreak(WordBreak wordBreak) {
    setStyle(CssProperty.WORD_BREAK, wordBreak);
  }

  public void setWordSpacing(double value, CssUnit unit) {
    setStyle(CssProperty.WORD_SPACING, value, unit);
  }

  public void setWordSpacing(int value, CssUnit unit) {
    setStyle(CssProperty.WORD_SPACING, value, unit);
  }

  public void setWordSpacing(String wordSpacing) {
    setStyle(CssProperty.WORD_SPACING, wordSpacing);
  }

  public void setWordWrap(WordWrap wordWrap) {
    setStyle(CssProperty.WORD_WRAP, wordWrap);
  }

  public void setZIndex(int zIndex) {
    setStyle(CssProperty.Z_INDEX, zIndex);
  }

  public void setZIndex(String zIndex) {
    setStyle(CssProperty.Z_INDEX, zIndex);
  }

  protected String buildEnd() {
    return " />";
  }

  protected String buildStart() {
    StringBuilder sb = new StringBuilder("<");
    sb.append(tag);

    if (!classes.isEmpty()) {
      Attribute cs = new Attribute(Attribute.CLASS, BeeUtils.join(BeeConst.STRING_SPACE, classes));
      sb.append(cs.build());
    }

    if (!styles.isEmpty()) {
      Attribute st = new Attribute(Attribute.STYLE, BeeUtils.join(BeeConst.STRING_SPACE, styles));
      sb.append(st.build());
    }

    for (Attribute attribute : attributes) {
      sb.append(attribute.build());
    }

    return sb.toString();
  }

  private Attribute findAttribute(String name) {
    for (Attribute attribute : attributes) {
      if (BeeUtils.same(attribute.getName(), name)) {
        return attribute;
      }
    }
    return null;
  }

  private Style findStyle(String name) {
    for (Style style : styles) {
      if (BeeUtils.same(style.getName(), name)) {
        return style;
      }
    }
    return null;
  }
}
