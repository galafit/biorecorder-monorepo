package biosignal.application;

import com.biorecorder.bichart.configs.GroupingApproximation;
import com.biorecorder.data.sequence.DoubleSequence;
import com.biorecorder.data.sequence.IntSequence;

public class XYValues {
    private GroupingApproximation yGroupingApproximation = GroupingApproximation.AVERAGE;
    private boolean isRegular;
    double startValue;
    double step;
    private final DoubleSequence xValues;
    private final IntSequence yValues;

    public XYValues(double startValue, double step, IntSequence yValues) {
       isRegular = true;
       this.startValue = startValue;
       this.step = step;
       this.yValues = yValues;
       xValues = new DoubleSequence() {
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

    public XYValues(DoubleSequence xValues, IntSequence yValues) {
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

    public double getXValue(int index) {
        return xValues.get(index);
    }

    public double getYValue(int index) {
        return yValues.get(index);
    }

    public DoubleSequence getXValues() {
        return xValues;
    }

    public IntSequence getYValues() {
        return yValues;
    }

    public int size() {
        return yValues.size();
    }
}
