/**
 * transformuojantis objektas
 */
package com.butent.bee.shared.testutils;

import com.butent.bee.shared.HasDoubleValue;
import com.butent.bee.shared.HasLength;
import com.butent.bee.shared.Transformable;

public class TransObject implements Transformable, HasLength, HasDoubleValue {

	public static final double DOUBLE_DEFAULT_VALUE = 5.0;
	public double doubleValue = 0;
	public long longValue = 0;
	private double Digit;

	public double getDigit() {
		return Digit;
	}

	public void setDigit(double digit) {
		Digit = digit;
	}

	@SuppressWarnings("static-access")
	public TransObject() {
		setDigit(this.DOUBLE_DEFAULT_VALUE);
	}

	@Override
	public String transform() {
		return Double.toString(this.Digit);
	}

	@Override
	public double getDouble() {

		return doubleValue;
	}

	@Override
	public void setValue(double value) {
		doubleValue = value;

	}

	@Override
	public long getLong() {
		return 0;
	}

	@Override
	public void setValue(long value) {
	}

	@Override
	public int getInt() {
		return 0;
	}

	public int sqlval = 8088;

	@Override
	public void setValue(int value) {
		sqlval = value;
	}

	@Override
	public int getLength() {
		return 5;
	}

}
