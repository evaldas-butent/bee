package com.butent.bee.shared.testutils;

import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasLength;
import com.butent.bee.shared.HasLongValue;
import com.butent.bee.shared.Transformable;

/**
 * Object for testing {@link com.butent.bee.shared.HasLongValue}.
 */
public class TransObjectLong implements BeeSerializable, Transformable, HasLength, HasLongValue {

	public static final double DOUBLE_DEFAULT_VALUE = 5.0;
	public double doubleValue=0;
	public long longValue = 0;
	private double Digit;
	
	public double getDigit() {
		return Digit;
	}

	public void setDigit(double digit) {
		Digit = digit;
	}

	public TransObjectLong() {
		setDigit(DOUBLE_DEFAULT_VALUE);
	}

	@Override
	public String transform() {
		return Double.toString(this.Digit);
	}
	
	@Override
	public long getLong() {
		return longValue;
	}

	@Override
	public void setValue(long value) {
		longValue = value;
	}

	@Override
	public int getInt() {
		return 0;
	}

	@Override
	public void setValue(int value) {
	}

	@Override
	public void deserialize(String s) {
	}

	@Override
	public String serialize() {
		return "Hello world";
	}

	@Override
	public int getLength() {
		return 5;
	}
}
