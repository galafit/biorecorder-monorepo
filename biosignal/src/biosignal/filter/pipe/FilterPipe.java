package biosignal.filter.pipe;

import biosignal.application.DataListener;
import biosignal.filter.BiFilter;
import biosignal.filter.Filter;
import biosignal.filter.XYData;

import java.util.ArrayList;
import java.util.List;

public class FilterPipe {
    private YPipe input;
    private List<Pipe> outputList = new ArrayList<>();

    public FilterPipe() {
        this(0, 1);
    }

    public FilterPipe(Filter f) {
        this(0, 1, f);
    }

    public FilterPipe(double startValue, double step) {
        this(startValue, step, new NullFilter());
    }

    public FilterPipe(double startValue, double step, Filter f) {
        input = new YPipe(startValue, step, f);
        outputList.add(input);
    }

    public void receiveData(int[] data, int from, int length) {
        int end = from + length;
        for (int i = from; i < end; i++) {
            input.put(data[i]);
        }
    }

    public FilterPipe then(int branchNumber, Filter f) {
        Pipe output = outputList.get(branchNumber);
        YPipe newOutput = new YPipe(input.getStartValue(), input.getStep(), f);
        output.addYReceiver(newOutput);
        outputList.set(branchNumber, newOutput);
        return this;
    }

    public FilterPipe then(int branchNumber, BiFilter f) {
        Pipe output = outputList.get(branchNumber);
        XYPipe newOutput = new XYPipe(f);
        output.addXYReceiver(newOutput);
        outputList.set(branchNumber, newOutput);
        return this;
    }

    public FilterPipe then(Filter f) {
      return then(outputList.size() - 1, f);
    }

    public FilterPipe then(BiFilter f) {
        return then(outputList.size() - 1, f);
    }

    public FilterPipe newBranch(int branchNumber, Filter f) {
        Pipe output = outputList.get(branchNumber);
        YPipe newOutput = new YPipe(input.getStartValue(), input.getStep(), f);
        output.addYReceiver(newOutput);
        outputList.add(branchNumber + 1, newOutput);
        return this;
    }

    public FilterPipe newBranch(int branchNumber, BiFilter f) {
        Pipe output = outputList.get(branchNumber);
        XYPipe newOutput = new XYPipe(f);
        output.addXYReceiver(newOutput);
        outputList.add(branchNumber + 1, newOutput);
        return this;
    }
    public FilterPipe newBranch(Filter f) {
        return newBranch(outputList.size() - 1, f);
    }

    public FilterPipe newBranch(BiFilter f) {
        return newBranch(outputList.size() - 1, f);
    }

    public XYData accumulateData(int branchNumber) {
        return outputList.get(branchNumber).enableDataAccumulation();
    }

    public XYData accumulateData() {
        return accumulateData(outputList.size() - 1);
    }

}
