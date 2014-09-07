package com.baselet.elementnew.element.plot.elements;

import java.util.Arrays;
import java.util.List;

import com.baselet.diagram.draw.DrawHandler;
import com.baselet.elementnew.element.plot.drawer.PlotGridDrawConfig;
import com.baselet.elementnew.element.plot.parser.PlotConstants;
import com.baselet.elementnew.element.plot.parser.PlotConstants.AxisList;
import com.baselet.elementnew.element.plot.parser.PlotConstants.AxisShow;
import com.baselet.elementnew.element.plot.parser.PlotState;

public class LinePlot extends AbstractPlot {

	public LinePlot(DrawHandler drawer, PlotGridDrawConfig plotDrawConfig, PlotState plotState, int xPos, int yPos) {
		super(drawer, plotDrawConfig, plotState, xPos, yPos);
	}

	@Override
	public void plot(int columnCount, int rowCount) {
		setPlotPosition(columnCount, rowCount);
		Boolean tilt = plotState.getValueAsBoolean(PlotConstants.KEY_BOOL_PLOT_TILT, PlotConstants.PLOT_TILT_DEFAULT);
		plotState.checkIfAllValuesUsed();
		plot.drawPlotAndDescValueAxis(!tilt, false, true, false);
	}

	@Override
	protected List<AxisShow> defaultDescAxisShow() {
		return Arrays.asList(AxisShow.Axis, AxisShow.Line, AxisShow.Marker, AxisShow.Text);
	}

	@Override
	protected List<AxisShow> defaultValueAxisShow() {
		return defaultDescAxisShow();
	}

	@Override
	protected List<AxisList> defaultValueAxisList() {
		return Arrays.asList(AxisList.Relevant);
	}

	@Override
	protected int getMaxAllowedValueRows() {
		return Integer.MAX_VALUE;
	}

}
