package com.butent.bee.client.modules.transport;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.HasNativeEvent;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.DndSource;
import com.butent.bee.client.event.DndTarget;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Procedure;
import com.butent.bee.shared.utils.BeeUtils;

class DndHelper {

  enum ContentType {
    CARGO
  }
  
  static final Predicate<Long> alwaysTarget = Predicates.alwaysTrue();

  private static ContentType dataType = null;
  
  private static Long dataId = null;
  private static Long relatedId = null;
  
  private static String dataDescription = null;
  
  static String getDataDescription() {
    return dataDescription;
  }

  static Long getRelatedId() {
    return relatedId;
  }

  static void makeSource(final DndSource widget, final ContentType contentType,
      final Long contentId, final Long relId, final String contentDescription,
      final String dragStyle) {

    Assert.notNull(widget);
    Assert.notNull(contentType);
    Assert.notNull(contentId);

    DomUtils.setDraggable(widget.asWidget());

    widget.addDragStartHandler(new DragStartHandler() {
      @Override
      public void onDragStart(DragStartEvent event) {
        if (!BeeUtils.isEmpty(dragStyle)) {
          widget.asWidget().addStyleName(dragStyle);
        }

        EventUtils.allowMove(event);
        EventUtils.setDndData(event, contentId);

        setDataType(contentType);
        
        setDataId(contentId);
        setRelatedId(relId);

        setDataDescription(contentDescription);
      }
    });

    widget.addDragEndHandler(new DragEndHandler() {
      @Override
      public void onDragEnd(DragEndEvent event) {
        if (!BeeUtils.isEmpty(dragStyle)) {
          widget.asWidget().removeStyleName(dragStyle);
        }
        reset();
      }
    });
  }

  static void makeTarget(final DndTarget widget, final ContentType contentType,
      final String overStyle, final Predicate<Long> targetPredicate, final Procedure<Long> onDrop) {

    Assert.notNull(widget);
    Assert.notNull(contentType);
    Assert.notNull(targetPredicate);
    Assert.notNull(onDrop);

    widget.addDragEnterHandler(new DragEnterHandler() {
      @Override
      public void onDragEnter(DragEnterEvent event) {
        if (isTarget(contentType, targetPredicate)) {
          if (!BeeUtils.isEmpty(overStyle)) {
            widget.asWidget().addStyleName(overStyle);
          }
        }
      }
    });

    widget.addDragOverHandler(new DragOverHandler() {
      @Override
      public void onDragOver(DragOverEvent event) {
        if (isTarget(widget, event, contentType, targetPredicate)) {
          event.preventDefault();
          EventUtils.selectDropMove(event);
        }
      }
    });

    widget.addDragLeaveHandler(new DragLeaveHandler() {
      @Override
      public void onDragLeave(DragLeaveEvent event) {
        if (isTarget(widget, event, contentType, targetPredicate)) {
          if (!BeeUtils.isEmpty(overStyle)) {
            widget.asWidget().removeStyleName(overStyle);
          }
        }
      }
    });

    widget.addDropHandler(new DropHandler() {
      @Override
      public void onDrop(DropEvent event) {
        if (isTarget(widget, event, contentType, targetPredicate)) {
          event.stopPropagation();
          onDrop.call(getDataId());
        }
      }
    });
  }

  private static Long getDataId() {
    return dataId;
  }

  private static ContentType getDataType() {
    return dataType;
  }

  private static boolean isTarget(ContentType contentType, Predicate<Long> targetPredicate) {
    return getDataType() == contentType && targetPredicate.apply(getDataId());
  }
  
  private static boolean isTarget(DndTarget widget, HasNativeEvent event, ContentType contentType,
      Predicate<Long> targetPredicate) {
    return widget.getId().equals(EventUtils.getEventTargetId(event.getNativeEvent()))
        && isTarget(contentType, targetPredicate);
  }

  private static void reset() {
    setDataType(null);
    
    setDataId(null);
    setRelatedId(null);
    
    setDataDescription(null);
  }

  private static void setDataDescription(String dataDescription) {
    DndHelper.dataDescription = dataDescription;
  }
  
  private static void setDataId(Long dataId) {
    DndHelper.dataId = dataId;
  }

  private static void setDataType(ContentType dataType) {
    DndHelper.dataType = dataType;
  }

  private static void setRelatedId(Long relatedId) {
    DndHelper.relatedId = relatedId;
  }

  private DndHelper() {
  }
}
