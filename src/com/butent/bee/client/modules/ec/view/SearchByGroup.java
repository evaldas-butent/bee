package com.butent.bee.client.modules.ec.view;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcGroup;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

class SearchByGroup extends EcView implements HasCaption {

  private static final String STYLE_NAME = "searchByGroup";

  private static final String STYLE_GROUP_SELECTED = EcStyles.name(STYLE_NAME, "groupSelected");
  
  private final boolean moto;

  private final List<EcGroup> groups = Lists.newArrayList();
  private int groupIndex = BeeConst.UNDEF;

  private final Flow groupPanel;
  private final Flow filtersAndItemsPanel;

  SearchByGroup(boolean moto) {
    super();
    this.moto = moto;

    this.groupPanel = new Flow(EcStyles.name(STYLE_NAME, "groups"));
    this.filtersAndItemsPanel = new Flow(EcStyles.name(STYLE_NAME, "filtersAndItems"));
  }

  @Override
  public String getCaption() {
    return moto ? Localized.getConstants().ecBikeItems()
        : Localized.getConstants().ecGeneralItems();
  }

  @Override
  protected void createUi() {
    ParameterList params = EcKeeper.createArgs(EcConstants.SVC_GET_ITEM_GROUPS);
    if (moto) {
      params.addQueryItem(EcConstants.COL_GROUP_MOTO, 1);
    }

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        EcKeeper.dispatchMessages(response);

        if (response != null && response.hasResponse()) {
          String[] arr = Codec.beeDeserializeCollection(response.getResponseAsString());
          if (arr != null) {
            for (String s : arr) {
              groups.add(EcGroup.restore(s));
            }
          }
        }
        
        render();
      }
    });
  }

  @Override
  protected String getPrimaryStyle() {
    return STYLE_NAME;
  }

  private int getGroupIndex() {
    return groupIndex;
  }
  
  private void render() {
    if (!isEmpty()) {
      clear();
    }
    
    if (groups.isEmpty()) {
      add(renderNoData(getCaption()));

    } else {
      add(groupPanel);
      add(filtersAndItemsPanel);
      
      renderGroups();
    }
  }

  private void renderGroups() {
    if (!groupPanel.isEmpty()) {
      groupPanel.clear();
    }

    Label caption = new Label(Localized.getConstants().ecSelectGroup());
    EcStyles.add(caption, STYLE_NAME, "groupsCaption");
    groupPanel.add(caption);
    
    String groupStyle = EcStyles.name(STYLE_NAME, "group");

    ClickHandler clickHandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (event.getSource() instanceof Widget) {
          selectGroup(DomUtils.getDataIndexInt(((Widget) event.getSource()).getElement()));
        }
      }
    };
    
    for (int i = 0; i < groups.size(); i++) {
      EcGroup group = groups.get(i);

      CustomDiv groupWidget = new CustomDiv(groupStyle);
      groupWidget.setHTML(group.getName());
      DomUtils.setDataIndex(groupWidget.getElement(), i);
      
      groupWidget.addClickHandler(clickHandler);
      
      groupPanel.add(groupWidget);
    }
  }

  private void selectGroup(int index) {
    if (BeeUtils.isIndex(groups, index) && getGroupIndex() != index) {
      if (!BeeConst.isUndef(getGroupIndex())) {
        Element selected = DomUtils.getChildByDataIndex(groupPanel.getElement(), getGroupIndex());
        if (selected != null) {
          selected.removeClassName(STYLE_GROUP_SELECTED);
        }
      }
      
      setGroupIndex(index);

      Element selected = DomUtils.getChildByDataIndex(groupPanel.getElement(), index);
      if (selected != null) {
        selected.addClassName(STYLE_GROUP_SELECTED);
      }
      
      LogUtils.getRootLogger().debug(index, groups.get(index).getName());
    }
  }
  
  private void setGroupIndex(int groupIndex) {
    this.groupIndex = groupIndex;
  }
}
