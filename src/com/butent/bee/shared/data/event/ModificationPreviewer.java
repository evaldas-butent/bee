package com.butent.bee.shared.data.event;

public interface ModificationPreviewer {

  default boolean previewCellUpdate(CellUpdateEvent event) {
    return true;
  }

  default boolean previewDataChange(DataChangeEvent event) {
    return true;
  }

  default boolean previewMultiDelete(MultiDeleteEvent event) {
    return true;
  }

  default boolean previewRowDelete(RowDeleteEvent event) {
    return true;
  }

  default boolean previewRowInsert(RowInsertEvent event) {
    return true;
  }

  default boolean previewRowUpdate(RowUpdateEvent event) {
    return true;
  }
}
