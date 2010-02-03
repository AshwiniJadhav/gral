/* OpenJChart : a free plotting library for the Java(tm) platform
 *
 * (C) Copyright 2009-2010, by Erich Seifert and Michael Seifert.
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

package openjchart.plots.shapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.text.Format;
import java.text.NumberFormat;

import openjchart.plots.Label;
import openjchart.util.SettingChangeEvent;
import openjchart.util.Settings;
import openjchart.util.SettingsListener;

/**
 * Abstract class implementing functions for the administration of settings.
 */
public abstract class AbstractShapeRenderer implements ShapeRenderer, SettingsListener {
	private final Settings settings;

	/**
	 * Creates a new AbstractShapeRenderer object with default shape and
	 * color.
	 */
	public AbstractShapeRenderer() {
		settings = new Settings(this);

		setSettingDefault(KEY_SHAPE, new Rectangle2D.Double(-2.5, -2.5, 5.0, 5.0));
		setSettingDefault(KEY_COLOR, Color.BLACK);

		setSettingDefault(KEY_VALUE_DISPLAYED, false);
		setSettingDefault(KEY_FORMAT, NumberFormat.getInstance());
	}

	/**
	 * Draws the specified value for the specified shape.
	 * @param g2d Graphics2D to be used for drawing.
	 * @param shape Shape to draw into.
	 * @param value Value to be displayed.
	 */
	protected void drawValue(Graphics2D g2d, Shape shape, Object value) {
		Format format = getSetting(KEY_FORMAT);
		String text = format.format(value);
		Label valueLabel = new Label(text);
		valueLabel.setBounds(shape.getBounds2D());
		valueLabel.draw(g2d);
	}

	@Override
	public <T> T getSetting(String key) {
		return settings.<T>get(key);
	}

	@Override
	public <T> void setSetting(String key, T value) {
		settings.<T>set(key, value);
	}

	@Override
	public <T> void removeSetting(String key) {
		settings.remove(key);
	}

	@Override
	public <T> void setSettingDefault(String key, T value) {
		settings.<T>setDefault(key, value);
	}

	@Override
	public <T> void removeSettingDefault(String key) {
		settings.removeDefault(key);
	}

	@Override
	public void settingChanged(SettingChangeEvent event) {
	}
}