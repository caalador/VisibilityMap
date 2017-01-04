package org.percepta.mgrankvi;

import junit.framework.Assert;
import org.junit.Test;

// JUnit tests here
public class MyComponentTest {

	@Test
	public void thisAlwaysPasses() {
		Assert.assertEquals(true, true);
	}

	@Test
	public void getTestImage() {
		new ImageToLines().getLines("/Users/Mikael/Desktop/dungeon/Dungeon2.png");
	}
}
