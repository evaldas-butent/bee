package com.butent.bee.client.modules.discussions;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.DateTimeFormat.PredefinedFormat;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.render.PhotoRenderer;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.values.Display;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

class AnnouncementsBoardInterceptor extends AbstractFormInterceptor {
  private static final String WIDGET_ADS_CONTENT = "AdsContent";
  private static final String STYLE_PREFIX = "bee-discuss-adsFormContent-";
  private static final String STYLE_HAPPY_DAY = "-happyDay";
  private static final String STYLE_BIRTH_LIST = "-birthList";

  @Override
  public FormInterceptor getInstance() {
    return new AnnouncementsBoardInterceptor();
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
   if (action.compareTo(Action.REFRESH) == 0) {
      renderContent(getFormView());
      return false;
   }
   
    return super.beforeAction(action, presenter);
  }

  @Override
  public void onStart(FormView form) {
    renderContent(form);
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
        boolean publishedBirths = false;

        for (String[] rsRow : rs.getRows()) {

          if (!BeeUtils.isEmpty(rsRow[rs.getColumnIndex(ALS_BIRTHDAY)])
              && BeeUtils.isEmpty(rsRow[rs.getColumnIndex(COL_SUBJECT)])) {
            if (!publishedBirths) {
              renderBirthdaySection(rsRow, rs, adsTable);
              publishedBirths = true;
            }
          } else if (!BeeUtils.isEmpty(rsRow[rs.getColumnIndex(ALS_BIRTHDAY)])
              && !BeeUtils.isEmpty(rsRow[rs.getColumnIndex(COL_SUBJECT)])) {
            if (!publishedBirths) {
              renderBirthdaySection(rsRow, rs, adsTable);
              publishedBirths = true;
            }
            renderAnnoucementsSection(rsRow, rs, adsTable);
          } else {
            renderAnnoucementsSection(rsRow, rs, adsTable);
          }

        }

      }
    });
  }

  protected static void renderAnnoucementsSection(String[] rsRow, SimpleRowSet rs,
      HtmlTable adsTable) {
    int row = adsTable.getRowCount();
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

  protected static void renderBirthdaySection(String[] rsRow, SimpleRowSet rs, HtmlTable adsTable) {
    int row = adsTable.getRowCount();
    int startRow = row;
    adsTable.setHtml(row, 0, rsRow[rs.getColumnIndex(ALS_TOPIC_NAME)]);
    adsTable.getCellFormatter().setColSpan(row, 0, 2);
    adsTable.getRow(row).addClassName(STYLE_PREFIX + ALS_TOPIC_NAME);
    adsTable.getRow(row).addClassName(STYLE_PREFIX + ALS_TOPIC_NAME + ALS_BIRTHDAY);
    StyleUtils.setDisplay(adsTable.getRow(row), Display.NONE);

    row++;
    Image img = new Image(Global.getImages().cake());
    img.addStyleName(STYLE_PREFIX + COL_OWNER + ALS_BIRTHDAY);
    adsTable.setHtml(row, 0, img.toString(), STYLE_PREFIX + COL_OWNER);
    adsTable.getCellFormatter().setRowSpan(row, 0, 2);

    adsTable.setHtml(row, 1, Localized.getConstants().birthdaysParties(), STYLE_PREFIX
        + COL_SUBJECT);
    adsTable.getRow(row).addClassName(STYLE_PREFIX + ALS_BIRTHDAY);
    StyleUtils.setDisplay(adsTable.getRow(row), Display.NONE);

    row++;
    int contentRow = row;
    adsTable.setHtml(row, 0, BeeConst.STRING_EMPTY, STYLE_PREFIX
        + COL_DESCRIPTION);

    adsTable.getRow(row).addClassName(STYLE_PREFIX + ALS_BIRTHDAY);
    StyleUtils.setDisplay(adsTable.getRow(row), Display.NONE);

    row++;
    adsTable.setHtml(row, 0, BeeConst.STRING_EMPTY);

    adsTable.getRow(row).addClassName(STYLE_PREFIX + BeeConst.EMPTY);
    adsTable.getCellFormatter().setColSpan(row, 0, 2);
    StyleUtils.setDisplay(adsTable.getRow(row), Display.NONE);
    row++;
    int finishRow = row;

    renderBirthdaysList(startRow, contentRow, finishRow, adsTable);
  }

  private static void renderBirthdaysList(final int startRow, final int contentRow,
      final int finishRow, final HtmlTable adsTable) {
    ParameterList params = DiscussionsKeeper.createArgs(SVC_GET_BIRTHDAYS);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      
      @Override
      public void onResponse(ResponseObject response) {
        if (response.isEmpty()) {
          adsTable.setHtml(contentRow, 0, Localized.getConstants().noData());
        }
        
        if (!response.hasResponse(SimpleRowSet.class)) {
          adsTable.setHtml(contentRow, 0, Localized.getConstants().actionCanNotBeExecuted());
          return;
        }
        
        SimpleRowSet rs = SimpleRowSet.restore(response.getResponseAsString());

        HtmlTable listTbl = new HtmlTable();
        int row = listTbl.getRowCount();
        
        for (String[] birthListData : rs.getRows()) {
          listTbl.setHtml(row, 0, birthListData[rs.getColumnIndex(COL_NAME)]);

          listTbl.setHtml(row, 1, DateTimeFormat.getFormat(PredefinedFormat.MONTH_DAY)
              .format(new JustDate(BeeUtils.toLong(birthListData[rs
                  .getColumnIndex(CommonsConstants.COL_DATE_OF_BIRTH)]))));
          listTbl.getRow(row).addClassName(STYLE_PREFIX + COL_DESCRIPTION
              + ALS_BIRTHDAY + STYLE_BIRTH_LIST);
          
          JustDate now = new JustDate();
          
          if (now.getDoy() == (new JustDate(BeeUtils.toLong(birthListData[rs
              .getColumnIndex(CommonsConstants.COL_DATE_OF_BIRTH)]))).getDoy()) {
            listTbl.getRow(row).addClassName(STYLE_PREFIX + COL_DESCRIPTION
                + ALS_BIRTHDAY + STYLE_HAPPY_DAY);
          }
          row++;
        }
        
        adsTable.setHtml(contentRow, 0, listTbl.toString());

        for (int i = startRow; i < finishRow; i++) {
          StyleUtils.setDisplay(adsTable.getRow(i), Display.TABLE_ROW);
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
