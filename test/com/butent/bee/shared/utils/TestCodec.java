package com.butent.bee.shared.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.shared.BeeSerializable;

import org.junit.Test;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

public class TestCodec {
  @Test
  public void testBeeSerializeDeserialize() {
    Object[] arr = new Object[] {
        null, "", "ac10", 5, -26543.735, 'y', true, false, Calendar.getInstance().getTime()};

    for (Object obj : arr) {
      String s = Codec.beeSerialize(obj);

      org.junit.Assert.assertEquals((obj == null) ? obj : BeeUtils.transform(obj),
          Codec.beeDeserialize(s));
      org.junit.Assert.assertNull(Codec.beeDeserializeCollection(s));
    }

    BeeSerializable ser = new BeeSerializable() {
      @Override
      public void deserialize(String s) {
      }

      @Override
      public String serialize() {
        return "SomeSerialzedText";
      }
    };
    org.junit.Assert.assertEquals(Codec.beeDeserialize(Codec.beeSerialize(ser)), ser.serialize());
    org.junit.Assert.assertArrayEquals(
        Codec.beeDeserializeCollection(Codec.beeSerialize(new Object[] {ser, ser, ser})),
        new Object[] {ser.serialize(), ser.serialize(), ser.serialize()});

    String[] values = new String[] {"aaa", "", "vv", null, "x", "1234623"};

    Map<String, String> valueMap = Maps.newLinkedHashMap();
    for (int i = 0; i < values.length; i += 2) {
      valueMap.put(values[i], values[i + 1]);
    }

    Object[] mArr = new Object[] {
        null,
        new String[0],
        new String[] {null},
        new String[] {""},
        values,
        Lists.newArrayList(values),
        Sets.newLinkedHashSet(Lists.newArrayList(values)),
        valueMap};

    String[] res = Codec.beeDeserializeCollection(Codec.beeSerialize(mArr));
    org.junit.Assert.assertNotNull(res);
    org.junit.Assert.assertEquals(res.length, mArr.length);

    for (int i = 0; i < mArr.length; i++) {
      String[] obj;

      if (mArr[i] instanceof Collection) {
        obj = ((Collection<?>) mArr[i]).toArray(new String[0]);
      } else if (mArr[i] instanceof Map) {
        obj = values;
      } else {
        obj = (String[]) mArr[i];
      }
      org.junit.Assert.assertArrayEquals(obj, Codec.beeDeserializeCollection(res[i]));
    }
  }
}
