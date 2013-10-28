package com.butent.bee.client.modules.ec;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Features;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.images.Images;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

class ArticleGraphicsHandler extends AbstractGridInterceptor {

  private static final class PictureRenderer extends AbstractCellRenderer {

    private static final ImageElement imageElement;

    static {
      imageElement = Document.get().createImageElement();
      imageElement.setAlt("picture");

      imageElement.setClassName(EcStyles.name("Picture"));
    }

    private final int typeIdx;
    private final int resourceIdx;

    private PictureRenderer(int typeIdx, int resourceIdx) {
      super(null);
      this.typeIdx = typeIdx;
      this.resourceIdx = resourceIdx;
    }

    @Override
    public String render(IsRow row) {
      if (row == null) {
        return null;
      }
      String picture = EcUtils.picture(row.getString(typeIdx), row.getString(resourceIdx));

      if (picture == null) {
        return null;
      }
      imageElement.setSrc(picture);
      return imageElement.getString();
    }
  }

  private FileCollector collector;

  ArticleGraphicsHandler() {
  }
  
  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {
    
    if (BeeUtils.same(columnName, COL_TCD_GRAPHICS_RESOURCE)) {
      column.getCell().addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showPicture(event);
        }
      });

      column.getCell().addKeyDownHandler(new KeyDownHandler() {
        @Override
        public void onKeyDown(KeyDownEvent event) {
          if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            showPicture(event);
          }
        }
      });
    }
    
    return super.afterCreateColumn(columnName, dataColumns, column, header, footer, editableColumn);
  }
  
  @Override
  public boolean beforeAction(Action action, final GridPresenter presenter) {
    if (action == Action.ADD) {
      getCollector().clickInput();
      return false;

    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new ArticleGraphicsHandler();
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription) {

    if (BeeUtils.same(columnName, COL_TCD_GRAPHICS_RESOURCE)) {
      return new PictureRenderer(DataUtils.getColumnIndex(COL_TCD_GRAPHICS_TYPE, dataColumns),
          DataUtils.getColumnIndex(COL_TCD_GRAPHICS_RESOURCE, dataColumns));
    } else {
      return super.getRenderer(columnName, dataColumns, columnDescription);
    }
  }

  @Override
  public boolean onLoad(GridDescription gridDescription) {
    if (!Features.supportsFileApi() && gridDescription != null) {
      gridDescription.getDisabledActions().add(Action.ADD);
    }

    return super.onLoad(gridDescription);
  }

  private FileCollector getCollector() {
    if (collector == null) {
      collector = FileCollector.headless(new Consumer<Collection<NewFileInfo>>() {
        @Override
        public void accept(Collection<NewFileInfo> input) {
          Collection<NewFileInfo> files = sanitize(input);
          Long articleId = getGridView().getRelId();

          if (!files.isEmpty() && DataUtils.isId(articleId)) {
            sendFiles(articleId, files);
          }
        }
      });
      
      collector.setAccept(Keywords.ACCEPT_IMAGE);

      getGridView().add(collector);
    }
    return collector;
  }

  private int getMaxSort() {
    int result = 0;

    List<? extends IsRow> data = getGridView().getRowData();
    if (!BeeUtils.isEmpty(data)) {
      int index = getDataIndex(COL_TCD_SORT);
      for (IsRow row : data) {
        result = Math.max(result, BeeUtils.unbox(row.getInteger(index)));
      }
    }

    return result;
  }

  private List<NewFileInfo> sanitize(Collection<NewFileInfo> input) {
    List<NewFileInfo> result = Lists.newArrayList();
    if (BeeUtils.isEmpty(input)) {
      return result;
    }

    List<String> errors = Lists.newArrayList();

    for (NewFileInfo nfi : input) {
      long size = nfi.getSize();

      if (size > Images.MAX_SIZE_FOR_DATA_URL) {
        errors.add(BeeUtils.join(BeeConst.STRING_COLON + BeeConst.STRING_SPACE, nfi.getName(),
            Localized.getMessages().fileSizeExceeded(size, Images.MAX_SIZE_FOR_DATA_URL)));
      } else {
        result.add(nfi);
      }
    }

    if (!errors.isEmpty()) {
      result.clear();
      getGridView().notifyWarning(ArrayUtils.toArray(errors));
    }

    return result;
  }

  private void sendFiles(final Long articleId, Collection<NewFileInfo> files) {
    final Holder<Integer> latch = Holder.of(files.size());
    final Holder<Integer> sort = Holder.of(getMaxSort());

    for (NewFileInfo fileInfo : files) {
      FileUtils.readAsDataURL(fileInfo.getFile(), new Consumer<String>() {

        @Override
        public void accept(String input) {
          sort.set(sort.get() + 1);

          ParameterList params = EcKeeper.createArgs(SVC_UPLOAD_GRAPHICS);
          params.addQueryItem(COL_TCD_ARTICLE, articleId);
          params.addQueryItem(COL_TCD_SORT, sort.get());

          params.addDataItem(COL_TCD_GRAPHICS_RESOURCE, input);

          BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              latch.set(latch.get() - 1);
              if (!BeeUtils.isPositive(latch.get())) {
                DataChangeEvent.fireRefresh(getViewName());
              }
            }
          });
        }
      });
    }
  }

  private void showPicture(DomEvent<?> event) {
    Element cellElement = event.getRelativeElement();
    if (cellElement == null) {
      return;
    }
    
    ImageElement imageElement = DomUtils.getImageElement(cellElement);
    if (imageElement == null) {
      return;
    }
    
    if (event.getSource() instanceof AbstractCell) {
      ((AbstractCell<?>) event.getSource()).setEventCanceled(true);
    }
    
    Image image = new Image(imageElement.getSrc());
    EcStyles.add(image, getViewName(), "picture");

    Global.showModalWidget(image);
  }
}
