package biosignal.application;

import com.biorecorder.bichart.configs.GroupingApproximation;
import com.biorecorder.datalyb.series.DoubleSeries;
import com.biorecorder.datalyb.series.IntSeries;

public class XYData {
    private GroupingApproximation yGroupingApproximation = GroupingApproximation.AVERAGE;
    private boolean isRegular;
    double startValue;
    double step;
    private final DoubleSeries xValues;
    private final IntSeries yValues;

    public XYData(double startValue, double step, IntSeries yValues) {
       isRegular = true;
       this.startValue = startValue;
       this.step = step;
       this.yValues = yValues;
       xValues = new DoubleSeries() {
           @Override
           public int size() {
               return yValues.size();
           }

           @Override
           public double get(int index) {
               return yValues.get(index);
           }
       };
    }

    public XYData(DoubleSeries xValues, IntSeries yValues) {
        isRegular = false;
        this.xValues = xValues;
        this.yValues = yValues;

    }

    public GroupingApproximation getGroupingApproximationY() {
        return yGroupingApproximation;
    }

    public void setGroupingApproximationY(GroupingApproximation yGroupingApproximation) {
        this.yGroupingApproximation = yGroupingApproximation;
    }

    public boolean isRegular() {
        return isRegular;
    }

    public double getStartValue() {
        if(isRegular) {
            return startValue;
        } else {
           return xValues.get(0);
        }
    }

    public double getStep() {
        if(isRegular) {
            return step;
        } else {
            return xValues.get(1) - xValues.get(0);
        }
    }

    public double getX(int index) {
        return xValues.get(index);
    }

    public double getY(int index) {
        return yValues.get(index);
    }

    public DoubleSeries getXValues() {
        return xValues;
    }

    public IntSeries getYValues() {
        return yValues;
    }

    public int size() {
        return yValues.size();
    }
}
