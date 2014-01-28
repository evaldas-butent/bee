package com.butent.bee.shared.css.values;

import com.butent.bee.shared.css.HasCssName;

public enum SpeakAs implements HasCssName {
  NORMAL {
    @Override
    public String getCssName() {
      return "normal";
    }
  },
  SPELL_OUT {
    @Override
    public String getCssName() {
      return "spell-out";
    }
  },
  DIGITS {
    @Override
    public String getCssName() {
      return "digits";
    }
  },
  LITERAL_PUNCTUATION {
    @Override
    public String getCssName() {
      return "literal-punctuation";
    }
  },
  NO_PUNCTUATION {
    @Override
    public String getCssName() {
      return "no-punctuation";
    }
  }
}
