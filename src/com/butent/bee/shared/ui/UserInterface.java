package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;
import java.util.Map;

public enum UserInterface implements HasCaption {
  DESKTOP {
    @Override
    public Map<String, String> getMeta() {
      return Maps.newHashMap();
    }

    @Override
    public List<String> getScripts() {
      return Lists.newArrayList("settings");
    }

    @Override
    public String getShortName() {
      return "desktop";
    }
    
    @Override
    public List<String> getStyleSheets() {
      return Lists.newArrayList(MAIN_STYLE_SHEET, CalendarConstants.STYLE_SHEET,
          CrmConstants.STYLE_SHEET, EcConstants.STYLE_SHEET, MailConstants.STYLE_SHEET,
          TradeConstants.STYLE_SHEET, TransportConstants.STYLE_SHEET);
    }

    @Override
    public String getTitle() {
      return TITLE;
    }
  },
  
  TABLET {
    @Override
    public Map<String, String> getMeta() {
      Map<String, String> meta = Maps.newHashMap();

      meta.put("gwt:property", "screen=tablet");

      meta.put("apple-mobile-web-app-capable", "yes");
      meta.put("viewport", "width=device-width, user-scalable=yes");
      
      return meta;
    }

    @Override
    public List<String> getScripts() {
      return Lists.newArrayList("tabletsettings");
    }

    @Override
    public String getShortName() {
      return "tablet";
    }
    
    @Override
    public List<String> getStyleSheets() {
      return Lists.newArrayList(MAIN_STYLE_SHEET, CalendarConstants.STYLE_SHEET,
          CrmConstants.STYLE_SHEET, EcConstants.STYLE_SHEET, MailConstants.STYLE_SHEET,
          TradeConstants.STYLE_SHEET, TransportConstants.STYLE_SHEET);
    }

    @Override
    public String getTitle() {
      return "B-novo";
    }
  },
  
  MOBILE {
    @Override
    public Map<String, String> getMeta() {
      Map<String, String> meta = Maps.newHashMap();

      meta.put("gwt:property", "screen=mobile");

      meta.put("apple-mobile-web-app-capable", "yes");
      meta.put("viewport", "width=device-width, user-scalable=yes");
      
      return meta;
    }

    @Override
    public List<String> getScripts() {
      return Lists.newArrayList("mobilesettings");
    }

    @Override
    public String getShortName() {
      return "mobile";
    }
    
    
    @Override
    public List<String> getStyleSheets() {
      return Lists.newArrayList(MAIN_STYLE_SHEET, CalendarConstants.STYLE_SHEET,
          CrmConstants.STYLE_SHEET, EcConstants.STYLE_SHEET, MailConstants.STYLE_SHEET,
          TradeConstants.STYLE_SHEET, TransportConstants.STYLE_SHEET);
    }

    @Override
    public String getTitle() {
      return TITLE;
    }
  },
  
  E_COMMERCE {
    @Override
    public Map<String, String> getMeta() {
      Map<String, String> meta = Maps.newHashMap();
      meta.put("gwt:property", "screen=ec");
      return meta;
    }

    @Override
    public List<String> getScripts() {
      return Lists.newArrayList("ecsettings");
    }

    @Override
    public String getShortName() {
      return "ec";
    }
    
    
    @Override
    public List<String> getStyleSheets() {
      return Lists.newArrayList(MAIN_STYLE_SHEET, EcConstants.CLIENT_STYLE_SHEET);
    }

    @Override
    public String getTitle() {
      return TITLE;
    }
  },
  
  SELF_SERVICE {
    @Override
    public Map<String, String> getMeta() {
      Map<String, String> meta = Maps.newHashMap();
      meta.put("gwt:property", "screen=trss");
      return meta;
    }

    @Override
    public List<String> getScripts() {
      return Lists.newArrayList("settings");
    }

    @Override
    public String getShortName() {
      return "trss";
    }
    
    
    @Override
    public List<String> getStyleSheets() {
      return Lists.newArrayList(MAIN_STYLE_SHEET);
    }

    @Override
    public String getTitle() {
      return TITLE;
    }
  };
  
  public static final UserInterface DEFAULT = DESKTOP;

  public static final String MAIN_STYLE_SHEET = "bee";

  public static final String TITLE = "B-novo";

  public static UserInterface getByShortName(String input) {
    for (UserInterface ui : values()) {
      if (BeeUtils.same(ui.getShortName(), input)) {
        return ui;
      }
    }

    for (UserInterface ui : values()) {
      if (BeeUtils.containsSame(input, ui.getShortName())) {
        return ui;
      }
    }

    return NameUtils.getEnumByName(UserInterface.class, input);
  }
  
  @Override
  public String getCaption() {
    return name();
  }
  
  public abstract Map<String, String> getMeta();

  public abstract List<String> getScripts();

  public abstract String getShortName();

  public abstract List<String> getStyleSheets();

  public abstract String getTitle();
}
