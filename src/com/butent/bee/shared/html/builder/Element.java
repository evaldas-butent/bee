package com.butent.bee.shared.html.builder;

import com.google.common.collect.Lists;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

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

  public void setClassName(String clazz) {
    classes.clear();
    addClassName(clazz);
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

  public void setData(String key, String value) {
    if (!BeeUtils.isEmpty(key)) {
      setAttribute(Attribute.DATA_PREFIX + key.trim(), value);
    }
  }

  public void setDirAuto() {
    setAttribute(Attribute.DIR, Keywords.DIR_AUTO);
  }

  public void setDirLtr() {
    setAttribute(Attribute.DIR, Keywords.DIR_LTR);
  }

  public void setDirRtl() {
    setAttribute(Attribute.DIR, Keywords.DIR_RTL);
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

  public void setDropZone(String dropZone) {
    setAttribute(Attribute.DROPZONE, dropZone);
  }

  public void setHidden(boolean hidden) {
    setAttribute(Attribute.HIDDEN, hidden);
  }

  public void setId(String id) {
    setAttribute(Attribute.ID, id);
  }

  public void setInert(boolean inert) {
    setAttribute(Attribute.INERT, inert);
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

  public void setLang(String lang) {
    setAttribute(Attribute.LANG, lang);
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

  public void setSpellCheck(Boolean spellCheck) {
    if (spellCheck == null) {
      removeAttribute(Attribute.SPELL_CHECK);
    } else if (spellCheck) {
      setAttribute(Attribute.SPELL_CHECK, Keywords.SPELL_CHECK_ENABLED);
    } else {
      setAttribute(Attribute.SPELL_CHECK, Keywords.SPELL_CHECK_DISABLED);
    }
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

  public void setTabIndex(int tabIndex) {
    setAttribute(Attribute.TAB_INDEX, BeeUtils.toString(tabIndex));
  }

  public void setTitle(String title) {
    setAttribute(Attribute.TITLE, title);
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

  @Override
  protected String build() {
    StringBuilder sb = new StringBuilder(buildStart());
    sb.append(buildEnd());
    return sb.toString();
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
