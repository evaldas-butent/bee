package com.butent.bee.client.modules.commons;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import elemental.html.File;

public class PersonFormInterceptor extends AbstractFormInterceptor {

  private static final String PHOTO_FILE_WIDGET_NAME = "PhotoFile";
  private static final String PHOTO_IMAGE_WIDGET_NAME = "PhotoImg";
  private static final String UNSET_PHOTO_WIDGET_NAME = "unsetPhoto";
  private static final String DEFAULT_PHOTO_IMAGE_FILE = "images/silver/person_profile.png";
  private static final long MAX_UPLOAD_FILE_SIZE = 1258292L; /* ~1.2 MB */

  private BeeImage photoImageWidget = null;
  private NewFileInfo photoImageAttachment = null;
  private boolean photoImageChanged = false;

  private String uploadedPhotoFileName = DEFAULT_PHOTO_IMAGE_FILE;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, PHOTO_FILE_WIDGET_NAME) && widget instanceof FileCollector) {
      final FileCollector fc = (FileCollector) widget;
      fc.addSelectionHandler(new SelectionHandler<NewFileInfo>() {

        @Override
        public void onSelection(SelectionEvent<NewFileInfo> event) {
          fc.clear();
          fc.getFiles().add(event.getSelectedItem());
          photoImageChanged = true;

          if (photoImageWidget != null) {
            photoImageAttachment = event.getSelectedItem();

            if (!BeeUtils.containsSame(photoImageAttachment.getFile().getType(), "image")) {
              BeeKeeper.getScreen().notifyWarning(
                  Localized.messages.invalidImageFileType(photoImageAttachment.getFile()
                      .getName()));
            } else if (photoImageAttachment.getSize() > MAX_UPLOAD_FILE_SIZE) {
              BeeKeeper.getScreen().notifyWarning(Localized.messages.exceededFileSize());
            } else {
              showImageInFormBeforeUpload(photoImageWidget, event.getSelectedItem().getFile());
              return;
            }

            fc.clear();
            photoImageWidget.setUrl(uploadedPhotoFileName);
            photoImageChanged = false;
            photoImageAttachment = null;
          }
        }
      });
    }
    if (BeeUtils.same(name, PHOTO_IMAGE_WIDGET_NAME) && widget instanceof BeeImage) {
      if (widget != null) {
        photoImageWidget = (BeeImage) widget;
      }
    }
    if (BeeUtils.same(name, UNSET_PHOTO_WIDGET_NAME) && widget instanceof BeeImage) {
      if (widget != null) {
        BeeImage unsetPhotoBtn = (BeeImage) widget;
        unsetPhotoBtn.addClickHandler(new ClickHandler() {

          @Override
          public void onClick(ClickEvent event) {
            IsRow row = UiHelper.getDataView((Widget) event.getSource()).getActiveRow();
            FormView form = UiHelper.getForm((Widget) event.getSource());

            row.setValue(form.getDataIndex(COL_PHOTO), (Long) null);
            photoImageChanged = false;
            photoImageAttachment = null;
            uploadedPhotoFileName = DEFAULT_PHOTO_IMAGE_FILE;

            if (photoImageWidget != null) {
              photoImageWidget.setUrl(uploadedPhotoFileName);
            }
          }
        });
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return this;
  }

  @Override
  public void onReadyForInsert(ReadyForInsertEvent event) {
    final FormView form = getFormView();
    final IsRow row = form.getActiveRow();

    if (row == null) {
      return;
    }

    if (photoImageChanged && (photoImageAttachment != null)) {
      event.consume();
      FileUtils.upload(photoImageAttachment, new Callback<Long>() {

        @Override
        public void onFailure(String... reason) {
          super.onFailure(reason);
          photoImageChanged = false;
          photoImageAttachment = null;
          BeeKeeper.getScreen().notifySevere(Localized.messages.imageUploadFailed());
          form.getViewPresenter().handleAction(Action.SAVE);
        }

        @Override
        public void onSuccess(Long fileId) {
          photoImageChanged = false;
          photoImageAttachment = null;
          row.setValue(form.getDataIndex(COL_PHOTO), fileId);
          form.getViewPresenter().handleAction(Action.SAVE);
        }

      });
    }
  }

  @Override
  public void onSaveChanges(SaveChangesEvent event) {
    event.consume();
    final FormView form = getFormView();
    final IsRow oldRow = form.getOldRow();
    final IsRow row = form.getActiveRow();
    final int colPhotoId = form.getDataIndex(COL_PHOTO);

    if ((row == null) || (oldRow == null)) {
      return;
    }

    if (photoImageChanged && (photoImageAttachment != null)) {
      // form.getViewPresenter().handleAction(Action.CLOSE);
      FileUtils.upload(photoImageAttachment, new Callback<Long>() {

        @Override
        public void onFailure(String... reason) {
          super.onFailure(reason);
          BeeKeeper.getScreen().notifySevere(Localized.messages.imageUploadFailed());
        }

        @Override
        public void onSuccess(Long result) {
          row.setValue(colPhotoId, result);
          photoImageChanged = false;
          photoImageAttachment = null;

          /* if uploading successful do save again */
          Queries.update(VIEW_PERSONS, form.getDataColumns(), oldRow, row, form
              .getChildrenForUpdate(), new RowUpdateCallback(VIEW_PERSONS));
        }
      });
    } else {
      Queries.update(VIEW_PERSONS, form.getDataColumns(), oldRow, row, form
          .getChildrenForUpdate(), new RowUpdateCallback(VIEW_PERSONS));
    }
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    showPhoto(form, row);
    return super.onStartEdit(form, row, focusCommand);
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    showPhoto(form, newRow);
    super.onStartNewRow(form, oldRow, newRow);
  }

  private native void showImageInFormBeforeUpload(BeeImage image, File f) /*-{
    if ($wnd.File && $wnd.FileReader && $wnd.Blob) {

      var reader = new FileReader();

      reader.onload = (function(theFile) {
        return function(e) {
          image.@com.butent.bee.client.widget.BeeImage::setUrl(Ljava/lang/String;)(e.target.result);
        };
      })(f);

      reader.readAsDataURL(f);
    } else {
      //@com.butent.bee.client.Global::showError(Ljava/lang/String;)("Jūsų naršyklė nepalaiko bylų įkėlimo");
    }

  }-*/;

  private void showPhoto(FormView form, IsRow row) {
    Long photoId = row.getLong(form.getDataIndex(COL_PHOTO));

    if (photoId != null && photoImageWidget != null) {
      uploadedPhotoFileName =
          FileUtils.getUrl(row.getString(form.getDataIndex(ALS_PHOTO_FILE_NAME)), photoId);
      photoImageWidget.setUrl(uploadedPhotoFileName);
      photoImageChanged = false;
      photoImageAttachment = null;
    } else if (photoImageWidget != null) {
      photoImageWidget.setUrl(DEFAULT_PHOTO_IMAGE_FILE);
    }
  }
}
