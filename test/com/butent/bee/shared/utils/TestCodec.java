package com.butent.bee.shared.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.communication.ResponseObject;

import org.junit.Test;

import java.util.Calendar;
import java.util.Collection;

public class TestCodec {
  @Test
  public void testBeeSerializeDeserialize() {
    Object[] arr =
        new Object[] {
            null, "", "c10", 5, -26543.735, 'y', true, false, Calendar.getInstance().getTime()};

    for (Object obj : arr) {
      String s = Codec.beeSerialize(obj);

      org.junit.Assert.assertEquals((obj == null) ? obj : BeeUtils.transform(obj),
          Codec.beeDeserialize(s));
      org.junit.Assert.assertNull(Codec.beeDeserializeCollection(s));
    }

    Object[][] multiArr = new Object[][] {
        new String[0],
        new String[] {null},
        new String[] {""},
        new String[] {"aaa", "", "vv", null, "x"}};

    for (Object[] obj : multiArr) {
      org.junit.Assert.assertArrayEquals(obj,
          Codec.beeDeserializeCollection(Codec.beeSerialize(obj)));

      Collection<Object> ob = Lists.newArrayList(obj);

      org.junit.Assert.assertArrayEquals(obj,
          Codec.beeDeserializeCollection(Codec.beeSerialize(ob)));

      org.junit.Assert.assertArrayEquals(obj,
          Codec.beeDeserializeCollection(Codec.beeSerialize(Sets.newLinkedHashSet(ob))));
    }
    System.out.println(Codec.beeSerialize(ResponseObject.response("pyp").addWarning("bum")));
    System.out.println(Codec.beeSerialize(new Object[] {
        new String[] {"aa", "", "b"}, new int[] {1, 2, -1}}));
  }
}
