package com.butent.bee.shared.testutils;

import java.util.ArrayList;
import java.util.Collection;

import com.butent.bee.server.sql.HasSource;

public class HasSourceClass implements HasSource {

	public HasSourceClass() {
	}

	Collection<String> a;
	@Override
	public Collection<String> getSources() {
		a = new ArrayList<String>();
		a.add("sql");
		a.add("test");
		return null;
	}

}
