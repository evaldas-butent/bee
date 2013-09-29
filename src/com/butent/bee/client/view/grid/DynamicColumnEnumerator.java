package com.butent.bee.client.view.grid;

import java.util.Collection;

public interface DynamicColumnEnumerator {
  Collection<DynamicColumnIdentity> getDynamicColumns(GridView gridView, String dynGroup);
}
