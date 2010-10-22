/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2010 Erich Seifert <info[at]erichseifert.de>, Michael Seifert <michael.seifert[at]gmx.net>
 *
 * This file is part of GRAL.
 *
 * GRAL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GRAL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GRAL.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.erichseifert.gral.plots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.util.MathUtils;

/**
 * Class that controls the zoom of a Plot.
 */
public class PlotNavigator {
	private final Plot plot;
	private final Map<String, NavigationInfo> infos;
	private final Set<NavigationListener> navigationListeners;

	private double zoomMin;
	private double zoomMax;

	/**
	 * Utility class for storing navigational information for an axis.
	 */
	private static final class NavigationInfo {
		private final Number minOriginal;
		private final Number maxOriginal;
		private final double centerOriginal;
		private final double rangeOriginal;
		private double center;
		private double zoom;

		/**
		 * Constructor which creates an new <code>NavigationInfo</code> instance.
		 * @param min Minimum value in axis units.
		 * @param max Maximum value in axis units.
		 * @param center Center in axis units.
		 */
		public NavigationInfo(Number min, Number max, double center) {
			this.minOriginal = min;
			this.maxOriginal = max;
			this.centerOriginal = center;
			this.rangeOriginal = max.doubleValue() - min.doubleValue();
			this.center = centerOriginal;
			this.zoom = 1.0;
		}

		/**
		 * Returns the original minimum value.
		 * @return Original minimum value.
		 */
		public Number getMinOriginal() {
			return minOriginal;
		}
		/**
		 * Returns the original maximum value.
		 * @return Original maximum value.
		 */
		public Number getMaxOriginal() {
			return maxOriginal;
		}
		/**
		 * Returns the original center value.
		 * @return Original center value.
		 */
		public double getCenterOriginal() {
			return centerOriginal;
		}
		/**
		 * Returns the original data range.
		 * @return Original data range.
		 */
		public double getRangeOriginal() {
			return rangeOriginal;
		}

		/**
		 * Returns the current center value.
		 * @return Current center value.
		 */
		public double getCenter() {
			return center;
		}
		/**
		 * Sets the current center value.
		 * @param center New center value.
		 */
		public void setCenter(double center) {
			this.center = center;
		}

		/**
		 * Returns the current zoom factor.
		 * @return Current zoom factor.
		 */
		public double getZoom() {
			return zoom;
		}
		/**
		 * Sets the current zoom factor.
		 * @param zoom New zoom factor.
		 */
		public void setZoom(double zoom) {
			this.zoom = zoom;
		}
	}

	/**
	 * Creates a new <code>PlotZoomer</code> object that is responsible for the specified plot.
	 * @param plot Plot to be zoomed.
	 */
	public PlotNavigator(Plot plot) {
		navigationListeners = new HashSet<NavigationListener>();
		this.plot = plot;
		infos = new HashMap<String, NavigationInfo>();
		for (String axisName : plot.getAxesNames()) {
			Axis axis = plot.getAxis(axisName);
			double min = 0.0;
			double max = 0.0;
			Number center = 0.0;
			AxisRenderer renderer = plot.getAxisRenderer(axisName);
			if (renderer != null) {
				min = renderer.worldToView(axis, axis.getMin(), false);
				max = renderer.worldToView(axis, axis.getMax(), false);
				center = renderer.viewToWorld(axis, (min + max)/2.0, false);
			}
			NavigationInfo info = new NavigationInfo(
					axis.getMin(), axis.getMax(), center.doubleValue());
			infos.put(axisName, info);
		}
		zoomMin = 1e-2;
		zoomMax = 1e+2;
	}

	private void refresh() {
		for (Entry<String, NavigationInfo> entry: infos.entrySet()) {
			String axisName = entry.getKey();
			AxisRenderer renderer = plot.getAxisRenderer(axisName);
			if (renderer == null) {
				continue;
			}
			Axis axis = getPlot().getAxis(axisName);
			NavigationInfo info = entry.getValue();

			// Original range in screen units
			// Most up-to-date view coordinates (axis's layout) must be used
			double minOrig = renderer.worldToView(axis, info.getMinOriginal(), true);
			double maxOrig = renderer.worldToView(axis, info.getMaxOriginal(), true);
			double rangeOrig = maxOrig - minOrig;

			// New axis scale
			double zoom = info.getZoom();
			double range = rangeOrig*zoom;
			double center = renderer.worldToView(axis, info.getCenter(), true);
			Number min = renderer.viewToWorld(axis, center - 0.5*range, true);
			Number max = renderer.viewToWorld(axis, center + 0.5*range, true);

			// Change axis
			axis.setRange(min, max);
		}
	}

	/**
	 * Returns the plot stored in this instance.
	 * @return Stored plot object.
	 */
	public Plot getPlot() {
		return plot;
	}

	/**
	 * Returns the average zoom factor of all axes.
	 * @return Average zoom factor of all axes.
	 */
	public double getZoom() {
		double zoom = 0.0;
		int count = 0;
		for (NavigationInfo info: infos.values()) {
			if (Double.isNaN(info.getZoom())) {
				continue;
			}
			zoom += info.getZoom();
			count++;
		}
		return zoom / count;
	}

	/**
	 * Sets the plot's zoom level to the specified value.
	 * @param zoomNew New zoom level.
	 */
	public void setZoom(double zoomNew) {
		if (zoomNew <= 0.0 || Double.isNaN(zoomNew) || Double.isInfinite(zoomNew)) {
			return;
		}
		zoomNew = MathUtils.limit(zoomNew, zoomMin, zoomMax);
		boolean changed = false;
		for (Map.Entry<String, NavigationInfo> entry: infos.entrySet()) {
			String axisName = entry.getKey();
			NavigationInfo info = entry.getValue();
			double zoomOld = info.getZoom();
			if (zoomOld == zoomNew) {
				continue;
			}
			changed = true;
			info.setZoom(zoomNew);
			fireZoomChanged(axisName, zoomOld, zoomNew);
		}
		if (changed) {
			refresh();
		}
	}

	/**
	 * Returns the center point of the specified axis.
	 * @param axisName Name of the axis.
	 * @return Center point in axis units.
	 */
	public Number getCenter(String axisName) {
		if (!infos.containsKey(axisName)) {
			return null;
		}
		return infos.get(axisName).getCenter();
	}

	/**
	 * Sets a new center point for the specified axis.
	 * @param axisName Name of the axis.
	 * @param center New center point in axis units.
	 */
	public void setCenter(String axisName, Number center) {
		Number centerOld = getCenter(axisName);
		if (centerOld.equals(center)) {
			return;
		}
		infos.get(axisName).setCenter(center.doubleValue());
		fireCenterChanged(axisName, centerOld, center);
		refresh();
	}

	/**
	 * Resets the plot's zoom to the original value.
	 */
	public void reset() {
		for (Map.Entry<String, NavigationInfo> entry: infos.entrySet()) {
			String axisName = entry.getKey();
			NavigationInfo info = entry.getValue();

			double centerOriginal = info.getCenterOriginal();
			double centerOld = info.getCenter();
			info.setCenter(centerOriginal);
			fireCenterChanged(axisName, centerOld, centerOriginal);

			double zoomOld = info.getZoom();
			info.setZoom(1.0);
			fireZoomChanged(axisName, zoomOld, 1.0);
		}
		refresh();
	}

	/**
	 * Returns the minimal zoom factor.
	 * @return Minimal zoom factor.
	 */
	public double getZoomMin() {
		return zoomMin;
	}
	/**
	 * Sets the minimal zoom factor.
	 * @param min New minimal zoom factor.
	 */
	public void setZoomMin(double min) {
		this.zoomMin = min;
	}

	/**
	 * Returns the minimal zoom factor.
	 * @return Maximal zoom factor.
	 */
	public double getZoomMax() {
		return zoomMax;
	}
	/**
	 * Sets the maximal zoom factor.
	 * @param max New maximal zoom factor.
	 */
	public void setZoomMax(double max) {
		this.zoomMax = max;
	}

	/**
	 * Adds the specified listener object that gets notified on changes to
	 * navigation information like panning or zooming.
	 * @param l Listener object
	 */
	public void addNavigationListener(NavigationListener l) {
		navigationListeners.add(l);
	}

	/**
	 * Removes the specified listener object, i.e. it doesn't get notified on
	 * changes to navigation information like panning or zooming.
	 * @param l Listener object
	 */
	public void removeNavigationListener(NavigationListener l) {
		navigationListeners.remove(l);
	}

	/**
	 * Notifies all navigation listeners that the center has been changed.
	 * @param axisName Name of the axis that was changed
	 * @param centerOld Previous center point
	 * @param centerNew New center point
	 */
	protected void fireCenterChanged(String axisName, Number centerOld, Number centerNew) {
		for (NavigationListener l : navigationListeners) {
			l.centerChanged(this, axisName, centerOld, centerNew);
		}
	}

	/**
	 * Notifies all navigation listeners that the zoom level has been changed.
	 * @param axisName Name of the axis that was changed
	 * @param zoomOld Previous zoom level
	 * @param zoomNew New zoom level
	 */
	protected void fireZoomChanged(String axisName, double zoomOld, double zoomNew) {
		for (NavigationListener l : navigationListeners) {
			l.zoomChanged(this, axisName, zoomOld, zoomNew);
		}
	}
}
