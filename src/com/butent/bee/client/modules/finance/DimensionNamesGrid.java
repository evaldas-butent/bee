package com.butent.bee.client.modules.finance;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.menu.MenuItem;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Objects;

public class DimensionNamesGrid extends AbstractGridInterceptor {

  public DimensionNamesGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new DimensionNamesGrid();
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    if (gridDescription != null) {
      gridDescription.setFilter(Filter.isLessEqual(Dimensions.COL_ORDINAL,
          new IntegerValue(Dimensions.getObserved())));
    }

    return super.initDescription(gridDescription);
  }

  @Override
  public boolean previewCellUpdate(CellUpdateEvent event) {
    IsRow target = getGridView().getGrid().getRowById(event.getRowId());
    Integer ordinal = (target == null)
        ? null : target.getInteger(getDataIndex(Dimensions.COL_ORDINAL));

    if (ordinal != null) {
      String language = Localized.dictionary().languageTag();

      IsRow copy = DataUtils.cloneRow(target);
      event.applyTo(copy);

      if (BeeUtils.containsSame(event.getSourceName(), Dimensions.COL_PLURAL_NAME)) {
        String plural = DataUtils.getTranslation(getDataColumns(), copy,
            Dimensions.COL_PLURAL_NAME, language);

        if (!BeeUtils.isEmpty(plural)) {
          Dimensions.setPlural(ordinal, plural);

          List<MenuItem> menuItems = BeeKeeper.getMenu().filter(MenuService.EXTRA_DIMENSIONS);
          String parameter = Dimensions.menuParameter(ordinal);

          boolean changed = false;

          for (MenuItem menuItem : menuItems) {
            if (Objects.equals(parameter, menuItem.getParameters())) {
              changed = !BeeUtils.equalsTrim(menuItem.getLabel(), plural);
              if (changed) {
                menuItem.setLabel(plural.trim());
              }
              break;
            }
          }

          if (changed) {
            BeeKeeper.getMenu().refresh();
          }
        }

      } else if (BeeUtils.containsSame(event.getSourceName(), Dimensions.COL_SINGULAR_NAME)) {
        String singular = DataUtils.getTranslation(getDataColumns(), copy,
            Dimensions.COL_SINGULAR_NAME, language);

        if (!BeeUtils.isEmpty(singular)) {
          Dimensions.setSingular(ordinal, singular);
        }
      }
    }

    return true;
  }
}
