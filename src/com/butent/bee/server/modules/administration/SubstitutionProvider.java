package com.butent.bee.server.modules.administration;

import java.util.Set;

public interface SubstitutionProvider {
  Set<Long> substitute(Long user, Long substitute, String reason, String note);
}
