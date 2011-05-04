package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.communication.ContentType;

/**
 * Tests {@link com.butent.bee.shared.BeeResource}.
 */
public class TestBeeResource {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public final void testDeserialize() {
		
		BeeResource resource = new BeeResource("14Name13URI13ZIP15false17Content");
		assertEquals("Name", resource.getName());
		assertEquals("URI", resource.getUri());
		assertEquals(ContentType.ZIP, resource.getType());
		assertEquals(false,resource.isReadOnly());
		assertEquals("Content",resource.getContent());
	}

	@Test
	public final void testSerialize() {
		
		BeeResource resource = new BeeResource("http://localhost/image.jpg", "This will be an image", ContentType.IMAGE, true);
		assertEquals("0226http://localhost/image.jpg15IMAGE14true221This will be an image", resource.serialize());
		
		BeeResource resource1 = new BeeResource();
		assertEquals("00015false0", resource1.serialize());
		
		BeeResource resource2 = new BeeResource("", ContentType.UNKNOWN);
		assertEquals("0017UNKNOWN15false0", resource2.serialize());
		
		BeeResource resource3 = new BeeResource("URI", "Content");
		assertEquals("013URI015false17Content", resource3.serialize());
		
		BeeResource resource4 = new BeeResource("URI", "Content", true);
		assertEquals("013URI014true17Content", resource4.serialize());
		
		BeeResource resource5 = new BeeResource("URI", "Content", ContentType.ZIP);
		assertEquals("013URI13ZIP15false17Content", resource5.serialize());
		
		BeeResource resource6 = new BeeResource("URI", "Content", ContentType.ZIP);
		resource6.setName("Name");
		assertEquals("14Name13URI13ZIP15false17Content", resource6.serialize());
		
		BeeResource resource7 = new BeeResource(null, null, null);
		resource6.setName(null);
		assertEquals("00015false0", resource7.serialize());
		
		BeeResource resource8 = new BeeResource("", "", null);
		resource6.setName("");
		assertEquals("00015false0", resource8.serialize());
	}
}
