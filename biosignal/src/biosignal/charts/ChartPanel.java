package biosignal.charts;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChartPanel extends JPanel {

    private AxysX axysX = new AxysX();
    private AxysY axysY = new AxysY();

    private final List<Trace> listTraces = new ArrayList<>();
    Trace t;

    //  Отступы от границ фрейма для зоны рисования
    //  top = right = bottom = left = 20;
    private final Indent indent = new Indent(20);

    private int height;
    private int width;

    public void addTrace(int[] data) {
        listTraces.add(new Trace());
        if (listTraces.size() > 0) {
            listTraces.get(listTraces.size() - 1).setData(data);
        }
    }
    public void setTraceData(int trace, int[] data) {
        if (data.length >= 0) {
            listTraces.get(trace).setData(data);
        }
    }

    public void setIndent(int top, int right, int bottom, int left) {
        indent.setTop(top);
        indent.setRight(right);
        indent.setBottom(bottom);
        indent.setLeft(left);
        setStartEnd();
    }

    @Override
    public void setPreferredSize(Dimension dimension) {
        super.setPreferredSize(dimension);
        height = dimension.height;
        width  = dimension.width;
        setStartEnd();
    }

    @Override
    public void setSize(Dimension dimension) {
        super.setSize(dimension);
        height = dimension.height;
        width  = dimension.width;
        setStartEnd();
    }

    public Indent getIndent() { return indent; }

    public void setMinMaxAxysX(int min, int max) {
        axysX.setMinMax(min, max);
    }

    public void setMinMaxAxysY(int min, int max) {
        axysY.setMinMax(min, max);
    }

    public int getXLength(){
        return axysX.getLength();
    }

    private void setStartEnd(){
        axysX.setStartEnd(indent.getLeft(), width - indent.getLeft());
        axysY.setStartEnd(height - indent.getBottom(), indent.getBottom());
    }

    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        Color[] colors = new Color[]{ Color.RED, Color.BLACK, Color.BLUE };

        axysX.draw(g, height - indent.getBottom());
        axysY.draw(g, indent.getLeft());

//        for (int i = 0; i < listTraces.size(); i++) {
//            t = listTraces.get(i);
//            if (t != null) {
//                t.draw(g, axysX, axysY, colors[i]);
//            }
//        }

        listTraces.get(0).draw(g, axysX, axysY, colors[0]);

    }
}
