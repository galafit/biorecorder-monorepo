package biosignal.gui;

import biosignal.application.Facade;
import biosignal.application.XYData;
import com.biorecorder.bichart.GroupingApproximation;
import com.biorecorder.bichart.traces.LineTraceConfig;
import com.biorecorder.bichart.traces.LineTracePainter;
import com.biorecorder.bichart.traces.VerticalLinePainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private static final int X_START = 10; // Левый  угол фрейма от начала экрана
    private static final int Y_START = 10; // Верхий угол фрейма  от начала экрана

    private static final int HEIGHT_START = 1000; // Высота фрейма
    public   static final int WIDTH_START =1800;  // Ширина фрейма
    private final Facade facade;
    private BiChartPanel chartPanel;
    private long startTimeMs = 0;
    private long endTimeMs = 1000;

    public MainFrame(Facade facade) {
        super("MainFrame");
        this.facade = facade;
        facade.read();
        chartPanel = createChartPanel(facade);
        add(chartPanel);
        setLocation(X_START, Y_START);
        setPreferredSize(new Dimension(WIDTH_START, HEIGHT_START));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

//      Вызов менеджера раскладки по умолчанию
        pack();
        addKeyListener(new LetterKeyListener());
        setVisible(true);
    }

    private void close() {
        facade.finish();
        System.exit(0);  // Закрытие приложения без ошибок
    }

    private static  BiChartPanel createChartPanel(Facade facade){
        boolean isTimeXAxis = false; // XAxis: false - index; true - time
        BiChartPanel chartPanel = new BiChartPanel(isTimeXAxis);
        for (int i = 0; i < facade.getCanalsCount() - 2; i++) {
            XYData xyData = facade.getData(i);
            xyData.setGroupingApproximationY(GroupingApproximation.HIGH);
            if(i > 0) {
                chartPanel.addChartStack();
                xyData.setGroupingApproximationY(GroupingApproximation.HIGH);
            }
            LineTraceConfig lineConfig = new LineTraceConfig();
            lineConfig.setLineWidth(1);
            lineConfig.setMarkSize(3);
            chartPanel.addChartTrace("trace"+i, xyData, new LineTracePainter(lineConfig));
        }


        XYData xyData = facade.getData(3);
        xyData.setGroupingApproximationY(GroupingApproximation.HIGH);

        LineTraceConfig lineConfig = new LineTraceConfig();
        lineConfig.setLineWidth(1);
        lineConfig.setMarkSize(3);
        chartPanel.addChartStack();
        chartPanel.addChartTrace("rhythm", xyData, new LineTracePainter(lineConfig), true, false);

        chartPanel.addNavigatorTrace("rhythm", xyData, new VerticalLinePainter());
        return chartPanel;
    }

    class LetterKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_S) {
                double[] range = chartPanel.getChartXRange();
                startTimeMs = (long) range[0];
                System.out.println("start: "+ startTimeMs);
            }
            if (e.getKeyCode() == KeyEvent.VK_E) {
                double[] range = chartPanel.getChartXRange();
                endTimeMs = (long) range[1];
                System.out.println("end: "+ endTimeMs);
            }
            if (e.getKeyCode() == KeyEvent.VK_U) {
                System.out.println("read data from " + startTimeMs + " till: "+ endTimeMs);
                facade.setReadTimeInterval(startTimeMs, endTimeMs - startTimeMs);
                facade.read();
                getContentPane().remove(chartPanel);
                int width = chartPanel.getWidth();
                int height = chartPanel.getHeight();
                chartPanel = createChartPanel(facade);
                chartPanel.setPreferredSize(new Dimension(width, height));
                add(chartPanel);
                revalidate();
                repaint();
            }
            if (e.getKeyCode() == KeyEvent.VK_F) {
                System.out.println("read full data");
                facade.setFullReadInterval();
                facade.read();
                getContentPane().remove(chartPanel);
                int width = chartPanel.getWidth();
                int height = chartPanel.getHeight();
                chartPanel = createChartPanel(facade);
                chartPanel.setPreferredSize(new Dimension(width, height));
                add(chartPanel);
                revalidate();
                repaint();
            }
            if (e.getKeyCode() == KeyEvent.VK_C) {
               String file = facade.copyReadIntervalToFile();
               System.out.println("read interval saved to file: " + file);
            }
        }
    }
}



