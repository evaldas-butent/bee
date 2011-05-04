package com.butent.bee.shared.testutils;

import com.butent.bee.shared.HasIntValue;
import com.butent.bee.shared.HasLength;
import com.butent.bee.shared.Transformable;

/**
 * Object for testing {@link com.butent.bee.shared.HasIntValue}.
 */
public class TransObjectInt implements Transformable, HasLength, HasIntValue {

	public static final double DOUBLE_DEFAULT_VALUE = 5.0;
	public int intValue = 0;
	public long longValue = 0;
	private double Digit;

	public double getDigit() {
		return Digit;
	}

	public void setDigit(double digit) {
		Digit = digit;
	}

	@SuppressWarnings("static-access")
	public TransObjectInt() {
		setDigit(this.DOUBLE_DEFAULT_VALUE);
	}

	@Override
	public String transform() {
		return Double.toString(this.Digit);
	}

	@Override
	public int getInt() {
		return intValue;
	}

	@Override
	public void setValue(int value) {
		intValue = value;
	}

	@Override
	public int getLength() {
		return 5;
	}
}
