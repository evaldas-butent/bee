package com.butent.bee.client.imports;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.imports.ImportType;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;
import java.util.Objects;

public class ImportOptionsGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  @Override
  public void onReadyForInsert(final GridView gridView, final ReadyForInsertEvent event) {
    List<BeeColumn> columns = event.getColumns();

    for (int i = 0; i < columns.size(); i++) {
      if (BeeUtils.same(columns.get(i).getId(), COL_IMPORT_TYPE)) {
        ImportType type = EnumUtils.getEnumByIndex(ImportType.class, event.getValues().get(i));

        switch (type) {
          case TRACKING:
            event.consume();
            event.getColumns().add(DataUtils.getColumn(COL_IMPORT_DATA, gridView.getDataColumns()));
            event.getValues().add(TransportConstants.TBL_VEHICLE_TRACKING);
            gridView.fireEvent(event);
            return;

          case DATA:
            event.consume();
            Tree tree = new Tree(Localized.dictionary().modules());
            tree.setPixelSize(500, 600);
            Multimap<String, String> multi = TreeMultimap.create();

            for (DataInfo dataInfo : Data.getDataInfoProvider().getViews()) {
              String viewName = dataInfo.getViewName();
              String module = dataInfo.getModule();
              ModuleAndSub moduleAndSub = ModuleAndSub.parse(module);

              if (Objects.nonNull(moduleAndSub)) {
                module = moduleAndSub.getModule().getCaption();

                if (moduleAndSub.hasSubModule()) {
                  module += " (" + moduleAndSub.getSubModule().getCaption() + ")";
                }
              }
              multi.put(module, viewName);
            }
            for (String module : multi.keySet()) {
              TreeItem item = tree.addItem(module);
              multi.get(module).forEach(vw -> item.addItem(Data.getViewCaption(vw)).setTitle(vw));
            }
            Global.inputWidget(Localized.dictionary().data(), tree, () -> {
              TreeItem selected = tree.getSelectedItem();

              if (Objects.nonNull(selected)) {
                event.getColumns().add(DataUtils.getColumn(COL_IMPORT_DATA,
                    gridView.getDataColumns()));
                event.getValues().add(selected.getTitle());
                gridView.fireEvent(event);
              }
            });
            return;

          default:
            break;
        }
        break;
      }
    }
    super.onReadyForInsert(gridView, event);
  }
}
