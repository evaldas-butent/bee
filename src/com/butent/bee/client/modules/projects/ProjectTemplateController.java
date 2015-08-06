package com.butent.bee.client.modules.projects;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.screen.HasDomain;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Link;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.html.builder.Style;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.Localizable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;
import java.util.Map;

public class ProjectTemplateController extends Flow implements HasDomain {

  private static final String STYLE_PREFIX = ProjectsKeeper.STYLE_PREFIX  + "Template-";
  private static final String STYLE_CONTROLLER = STYLE_PREFIX  + "Controller";
  private static final String STYLE_TEMPLATE = STYLE_PREFIX + "Template";
  private static final String STYLE_TEMPLATE_NAME = STYLE_PREFIX + "TemplateName";
  private static final String STYLE_TEMPLATE_RECORD = STYLE_PREFIX + "TemplateRecord";
  private static final String STYLE_TEMPLATE_RECORD_ITEM = STYLE_PREFIX + "TemplateRecordItem";

  private Flow content;
  private final Map<String, FlowPanel> templates = Maps.newConcurrentMap();
  public ProjectTemplateController(FormView form, IsRow row) {
    content = new Flow(STYLE_CONTROLLER);
    add(content);
  }

  @Override
  public Domain getDomain() {
    return Domain.PROJECT_TEMPLATE;
  }

  public void addTemplateEntry(String viewTemplate, final String viewDest,
      final List<String> templateColumns, final List<String> destColumns,
      final List<String> nameCols, Filter relFilter, long id, String relIdColumn) {

    Assert.isFalse(templateColumns.size() == destColumns.size(),
        "templateColumns mus be same as destColumns");

    DataInfo viewData = Data.getDataInfo(viewTemplate);

    final FlowPanel templ = new FlowPanel();
    templ.addStyleName(STYLE_TEMPLATE);

    Flow caption = new Flow(STYLE_TEMPLATE_NAME);
    caption.getElement().setInnerText(Localized.maybeTranslate(viewData.getCaption()));
    templ.add(caption);

    content.add(templ);
    templates.put(viewTemplate, templ);

    Queries.getRowSet(viewTemplate, viewData.getColumnNames(false), relFilter,
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            for (int i = 0; i < result.getNumberOfRows(); i++) {
              Flow fRow = new Flow(STYLE_TEMPLATE_RECORD);
              fRow.addStyleName(BeeUtils.join(BeeConst.STRING_MINUS, STYLE_TEMPLATE_RECORD, i));
              fRow.addStyleName(BeeConst.CSS_CLASS_PREFIX + "InternalLink");

              for (String nameCol : nameCols) {
                Flow item = new Flow(STYLE_TEMPLATE_RECORD_ITEM);
                item.addStyleName(BeeUtils.join(BeeConst.STRING_MINUS,
                    STYLE_TEMPLATE_RECORD_ITEM, i, nameCol));

                item.getElement().setInnerText(result.getString(i, nameCol));
                fRow.add(item);
              }

              fRow.addClickHandler(getTemplateClickHandler(viewDest));

              templ.add(fRow);
            }

            hideContent();
          }
        });

    hideContent();
  }

  private ClickHandler getTemplateClickHandler(final String destView) {
    return new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        DataInfo destDataInfo = Data.getDataInfo(destView);
        BeeRow destRow = RowFactory.createEmptyRow(destDataInfo, true);
      }
    };
  }

  public void showTemplateContent(String viewName) {
    hideContent();
    if (templates.containsKey(viewName)) {
      StyleUtils.setVisible(templates.get(viewName), true);
    }
  }


  public void hideContent() {
    for(FlowPanel templ : templates.values()) {
      StyleUtils.setVisible(templ, false);
    }
  }
}
