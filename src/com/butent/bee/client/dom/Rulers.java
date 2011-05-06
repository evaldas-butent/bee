package com.butent.bee.client.dom;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Controls scale parameters between lenght measure units and screen pixels.
 */
public class Rulers {

  private static final double unitRulerScale = 10.0;

  private static final double cmToPx = 1.0 / unitRulerScale;
  private static final double mmToPx = 0.1 / unitRulerScale;
  private static final double inToPx = 2.54 / unitRulerScale;
  private static final double ptToPx = inToPx / 72.0;
  private static final double pcToPx = ptToPx * 12.0;

  private static final SpanElement lineRuler;
  private static final DivElement areaRuler;

  private static final DivElement fixedUnitRuler;
  private static final DivElement relativeUnitRuler;

  static {
    String idPrefix = "ruler";

    lineRuler = Document.get().createSpanElement();
    DomUtils.createId(lineRuler, idPrefix);
    lineRuler.setInnerHTML(BeeConst.HTML_NBSP);

    Style style = lineRuler.getStyle();
    style.setPosition(Position.ABSOLUTE);
    style.setZIndex(-100);
    style.setTop(-100, Unit.PX);
    StyleUtils.setWordWrap(style, false);

    Document.get().getBody().appendChild(lineRuler);

    areaRuler = Document.get().createDivElement();
    DomUtils.createId(areaRuler, idPrefix);
    areaRuler.setInnerHTML(BeeConst.HTML_NBSP);

    style = areaRuler.getStyle();
    style.setPosition(Position.ABSOLUTE);
    style.setZIndex(-200);
    style.setTop(-200, Unit.PX);
    StyleUtils.setWordWrap(style, true);

    Document.get().getBody().appendChild(areaRuler);

    fixedUnitRuler = Document.get().createDivElement();
    DomUtils.createId(fixedUnitRuler, idPrefix);
    fixedUnitRuler.setInnerHTML(BeeConst.HTML_NBSP);

    style = fixedUnitRuler.getStyle();
    style.setPosition(Position.ABSOLUTE);
    style.setZIndex(-300);
    style.setTop(-300, Unit.PX);

    style.setWidth(unitRulerScale, Unit.CM);
    style.setHeight(unitRulerScale, Unit.CM);

    Document.get().getBody().appendChild(fixedUnitRuler);

    relativeUnitRuler = Document.get().createDivElement();
    DomUtils.createId(relativeUnitRuler, idPrefix);
    relativeUnitRuler.setInnerHTML(BeeConst.HTML_NBSP);

    style = relativeUnitRuler.getStyle();
    style.setPosition(Position.ABSOLUTE);
    style.setZIndex(-400);
    style.setTop(-400, Unit.PX);

    style.setWidth(unitRulerScale, Unit.EM);
    style.setHeight(unitRulerScale, Unit.EX);

    Document.get().getBody().appendChild(relativeUnitRuler);
  }

  public static Dimensions getAreaDimensions(String html) {
    return getAreaDimensions(html, null);
  }

  public static Dimensions getAreaDimensions(String html, Font font) {
    Assert.notNull(html);
    return getDimensions(areaRuler, html, font);
  }

  public static int getAreaHeight(String html) {
    return getAreaHeight(html, null);
  }

  public static int getAreaHeight(String html, Font font) {
    Assert.notNull(html);
    return getHeight(areaRuler, html, font);
  }

  public static int getAreaWidth(String html) {
    return getAreaWidth(html, null);
  }

  public static int getAreaWidth(String html, Font font) {
    Assert.notNull(html);
    return getWidth(areaRuler, html, font);
  }

  public static int getIntPixels(double value, Unit unit) {
    return BeeUtils.toInt(getPixels(value, unit));
  }

  public static int getIntPixels(double value, Unit unit, double containerSize) {
    return BeeUtils.toInt(getPixels(value, unit, containerSize));
  }

  public static int getIntPixels(double value, Unit unit, Font font) {
    return BeeUtils.toInt(getPixels(value, unit, font));
  }

  public static int getIntPixels(double value, Unit unit, Font font, double containerSize) {
    return BeeUtils.toInt(getPixels(value, unit, font, containerSize));
  }

  public static Dimensions getLineDimensions(String html) {
    return getLineDimensions(html, null);
  }

  public static Dimensions getLineDimensions(String html, Font font) {
    Assert.notNull(html);
    return getDimensions(lineRuler, html, font);
  }

  public static int getLineHeight(String html) {
    return getLineHeight(html, null);
  }

  public static int getLineHeight(String html, Font font) {
    Assert.notNull(html);
    return getHeight(lineRuler, html, font);
  }

  public static int getLineWidth(String html) {
    return getLineWidth(html, null);
  }

  public static int getLineWidth(String html, Font font) {
    Assert.notNull(html);
    return getWidth(lineRuler, html, font);
  }

  public static double getPixels(double value, Unit unit) {
    return getPixels(value, unit, null);
  }

  public static double getPixels(double value, Unit unit, double containerSize) {
    return getPixels(value, unit, null, containerSize);
  }

  public static double getPixels(double value, Unit unit, Font font) {
    return getPixels(value, unit, font, 0);
  }

  public static double getPixels(double value, Unit unit, Font font, double containerSize) {
    Assert.notNull(unit);

    if (isUnitSizeFixed(unit)) {
      return value * getFixedUnitSizeInPixels(unit);
    } else if (isUnitFontDependent(unit)) {
      return value * getFontDependentUnitSizeInPixels(unit, font);
    } else if (isUnitContainerDependent(unit)) {
      return value * getContainerDependentUnitSizeInPixels(unit, containerSize);
    } else {
      Assert.untouchable();
      return 0;
    }
  }

  private static double getContainerDependentUnitSizeInPixels(Unit unit, double containerSize) {
    switch (unit) {
      case PCT:
        return containerSize / 100.0;
      default:
        Assert.untouchable();
        return 0;
    }
  }

  private static Dimensions getDimensions(Element ruler, String html, Font font) {
    if (html == null) {
      return null;
    }

    prepareRuler(ruler, html, font);

    int width = ruler.getOffsetWidth();
    int height = ruler.getOffsetHeight();

    resetRuler(ruler, font);

    return new Dimensions(width, height);
  }

  private static double getFixedUnitSizeInPixels(Unit unit) {
    switch (unit) {
      case PX:
        return 1;
      case CM:
        return fixedUnitRuler.getOffsetWidth() * cmToPx;
      case MM:
        return fixedUnitRuler.getOffsetWidth() * mmToPx;
      case IN:
        return fixedUnitRuler.getOffsetWidth() * inToPx;
      case PT:
        return fixedUnitRuler.getOffsetWidth() * ptToPx;
      case PC:
        return fixedUnitRuler.getOffsetWidth() * pcToPx;
      default:
        Assert.untouchable();
        return 0;
    }
  }

  private static double getFontDependentUnitSizeInPixels(Unit unit, Font font) {
    double size;
    prepareRuler(relativeUnitRuler, font);

    switch (unit) {
      case EM:
        size = relativeUnitRuler.getOffsetWidth() / unitRulerScale;
        break;
      case EX:
        size = relativeUnitRuler.getOffsetHeight() / unitRulerScale;
        break;
      default:
        Assert.untouchable();
        size = 0;
    }

    resetRuler(relativeUnitRuler, font);
    return size;
  }

  private static int getHeight(Element ruler, String html, Font font) {
    if (html == null) {
      return 0;
    }

    prepareRuler(ruler, html, font);
    int height = ruler.getOffsetHeight();
    resetRuler(ruler, font);

    return height;
  }

  private static int getWidth(Element ruler, String html, Font font) {
    if (html == null) {
      return 0;
    }

    prepareRuler(ruler, html, font);
    int width = ruler.getOffsetWidth();
    resetRuler(ruler, font);

    return width;
  }

  private static boolean inList(Unit unit, Unit... units) {
    if (unit == null || units == null) {
      return false;
    }
    return ArrayUtils.contains(unit, units);
  }

  private static boolean isUnitContainerDependent(Unit unit) {
    return inList(unit, Unit.PCT);
  }

  private static boolean isUnitFontDependent(Unit unit) {
    return inList(unit, Unit.EM, Unit.EX);
  }

  private static boolean isUnitSizeFixed(Unit unit) {
    return inList(unit, Unit.PX, Unit.CM, Unit.MM, Unit.IN, Unit.PC, Unit.PT);
  }

  private static void prepareRuler(Element ruler, Font font) {
    if (font != null) {
      font.applyTo(ruler);
    }
  }

  private static void prepareRuler(Element ruler, String html, Font font) {
    if (font != null) {
      font.applyTo(ruler);
    }
    if (html != null) {
      ruler.setInnerHTML(html);
    }
  }

  private static void resetRuler(Element ruler, Font font) {
    if (font != null) {
      font.removeFrom(ruler);
    }
    ruler.setInnerHTML(BeeConst.HTML_NBSP);
  }

  private Rulers() {
  }
}
