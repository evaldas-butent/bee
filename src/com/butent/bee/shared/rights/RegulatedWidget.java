package com.butent.bee.shared.rights;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;

public enum RegulatedWidget implements HasLocalizedCaption {

  NEWS {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.domainNews();
    }
  },
  ONLINE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.domainOnline();
    }
  },
  ADMIN(ModuleAndSub.of(Module.ADMINISTRATION)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return "Admin";
    }
  },
  AUDIT {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.actionAudit();
    }
  },
  DOCUMENT_TREE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.documentTree();
    }
  },
  TO_ERP {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trSendToERP();
    }
  },
  COMPANY_STRUCTURE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.companyStructure();
    }
  };

  private final ModuleAndSub moduleAndSub;

  RegulatedWidget() {
    this(null);
  }

  RegulatedWidget(ModuleAndSub moduleAndSub) {
    this.moduleAndSub = moduleAndSub;
  }

  @Override
  public String getCaption() {
    return getCaption(Localized.getConstants());
  }

  public ModuleAndSub getModuleAndSub() {
    return moduleAndSub;
  }

  public String getName() {
    return BeeUtils.proper(name());
  }
}
