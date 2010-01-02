/* OpenJChart : a free plotting library for the Java(tm) platform
 *
 * (C) Copyright 2009, by Erich Seifert and Michael Seifert.
 *
 * This file is part of OpenJChart.
 *
 * OpenJChart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenJChart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenJChart.  If not, see <http://www.gnu.org/licenses/>.
 */

package openjchart.tests.plots.axes;

import static org.junit.Assert.assertEquals;
import openjchart.plots.axes.Axis;

import org.junit.Before;
import org.junit.Test;

public class AxisTest {
	private Axis axis;

	@Before
	public void setUp() {
		axis = new Axis(-5, 5);
	}

	@Test
	public void testGetMin() {
		assertEquals(-5, axis.getMin());
	}

	@Test
	public void testGetMax() {
		assertEquals(5, axis.getMax());
	}

	@Test
	public void testGetRange() {
		double delta = 1e-5;
		assertEquals(10, axis.getRange(), delta);
	}
}