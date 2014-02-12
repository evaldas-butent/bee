package com.butent.bee.client.modules.discussions;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.PhotoRenderer;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

class AnnouncementsBoardInterceptor extends AbstractFormInterceptor {
  private static final String WIDGET_ADS_CONTENT = "AdsContent";
  private static final String STYLE_PREFIX = "bee-discuss-adsFormContent-";

  @Override
  public FormInterceptor getInstance() {
    return new AnnouncementsBoardInterceptor();
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
   if (action.compareTo(Action.REFRESH) == 0) {
      renderContent(getFormView());
   }
   
    return super.beforeAction(action, presenter);
  }

  private static void renderContent(FormView form) {
    Widget w = Assert.notNull(form.getWidgetByName(WIDGET_ADS_CONTENT));
    
    if (!(w instanceof HtmlTable)) {
      Assert.notImplemented();
    }
    
    final HtmlTable adsTable = (HtmlTable) w;
    adsTable.clear();
    ParameterList params = DiscussionsKeeper.createArgs(SVC_GET_ANNOUNCEMENTS_DATA);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.isEmpty()) {
          BeeKeeper.getScreen().notifyInfo(Localized.getConstants().noData());
          return;
        }

        if (!response.hasResponse(SimpleRowSet.class)) {
          BeeKeeper.getScreen().notifyInfo(Localized.getConstants().noData());
          return;
        }

        SimpleRowSet rs = SimpleRowSet.restore(response.getResponseAsString());
        int row = adsTable.getRowCount();

        for (String[] rsRow : rs.getRows()) {

          adsTable.setHtml(row, 0, rsRow[rs.getColumnIndex(ALS_TOPIC_NAME)]);
          adsTable.getCellFormatter().setColSpan(row, 0, 2);
          adsTable.getRow(row).addClassName(STYLE_PREFIX + ALS_TOPIC_NAME);
          
          if (!BeeUtils.isEmpty(rsRow[rs.getColumnIndex(COL_IMPORTANT)])) {
            adsTable.getRow(row).addClassName(STYLE_PREFIX + COL_IMPORTANT);
          }
          row++;
          adsTable.setHtml(row, 0, renderDateTime(rsRow[rs.getColumnIndex(COL_CREATED)]),
              STYLE_PREFIX + COL_CREATED);
          

          adsTable.setHtml(row, 1, rsRow[rs.getColumnIndex(COL_SUBJECT)], STYLE_PREFIX
              + COL_SUBJECT);


          row++;
          adsTable.setHtml(row, 0, renderPhotoAndAuthor(rsRow, rs, STYLE_PREFIX + COL_OWNER
              + BeeConst.STRING_MINUS),
              STYLE_PREFIX
                  + COL_OWNER);

          adsTable.setHtml(row, 1, rsRow[rs.getColumnIndex(COL_DESCRIPTION)], STYLE_PREFIX
              + COL_DESCRIPTION);

          row++;
          adsTable.setHtml(row, 0, BeeConst.STRING_EMPTY);

          adsTable.getRow(row).addClassName(STYLE_PREFIX + BeeConst.EMPTY);
          adsTable.getCellFormatter().setColSpan(row, 0, 2);
          row++;
        }

      }
    });
  }

  private static String renderDateTime(String timestamp) {
    String result;
    result = TimeUtils.renderDateTime(BeeUtils.toLong(timestamp));
    return result;
  }

  private static String renderPhotoAndAuthor(String[] rsRow, SimpleRowSet rs, String stylePref) {
    String fullName = BeeUtils.joinWords(rsRow[rs.getColumnIndex(CommonsConstants.COL_FIRST_NAME)],
        rsRow[rs.getColumnIndex(CommonsConstants.COL_LAST_NAME)]);
    Flow container = new Flow();

    Flow colPublisher = new Flow();
    colPublisher.addStyleName(stylePref + COL_PUBLISHER);

    if (!BeeUtils.isEmpty(fullName)) {
      colPublisher.add(createCell(stylePref + COL_PUBLISHER, fullName));
    }

    container.add(colPublisher);
    
    Flow colPhoto = new Flow();
    colPhoto.addStyleName(stylePref + CommonsConstants.COL_PHOTO);

    if (!BeeUtils.isEmpty(rsRow[rs.getColumnIndex(CommonsConstants.COL_PHOTO)])) {
      renderPhoto(rsRow, rs, stylePref, colPhoto);
    }
    container.add(colPhoto);
  
    return container.toString();
  }
  
  private static void renderPhoto(String[] rsRow, SimpleRowSet rs, String stylePref,
      Flow container) {
    String photo =
        rsRow[rs.getColumnIndex(CommonsConstants.COL_PHOTO)];
    if (!BeeUtils.isEmpty(photo)) {
      Image image = new Image(PhotoRenderer.getUrl(photo));
      image.addStyleName(stylePref + CommonsConstants.COL_PHOTO);
      container.add(image);
    }
  }

  private static Widget createCell(String style, String value) {
    Widget widget = new CustomDiv(style);
    if (!BeeUtils.isEmpty(value)) {
      widget.getElement().setInnerHTML(value);
    }

    return widget;
  }

}
