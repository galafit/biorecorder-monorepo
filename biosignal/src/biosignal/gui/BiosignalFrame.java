package biosignal.gui;

import biosignal.application.DataAppendListener;
import biosignal.application.Facade;
import biosignal.application.ProviderConfig;
import biosignal.application.ProviderConfigListener;
import biosignal.filter.XYData;
import com.biorecorder.bdfrecorder.gui.RecorderView;
import com.biorecorder.bichart.GroupingApproximation;
import com.biorecorder.bichart.ProcessingConfig;
import com.biorecorder.bichart.traces.LineTraceConfig;
import com.biorecorder.bichart.traces.LineTracePainter;
import com.biorecorder.bichart.traces.VerticalLinePainter;
import com.biorecorder.datalyb.time.TimeInterval;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class BiosignalFrame extends JFrame {
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color MENU_BG_COLOR = Color.LIGHT_GRAY;
    private static final Color MENU_TEXT_COLOR = Color.BLACK;

    private static final int X_START = 10; // Левый  угол фрейма от начала экрана
    private static final int Y_START = 10; // Верхий угол фрейма  от начала экрана

    private static final int HEIGHT_START = 1000; // Высота фрейма
    public static final int WIDTH_START = 1800;  // Ширина фрейма
    private final Facade facade;
    private BiChartPanel chartPanel;
    private long startTimeMs = 0;
    private long endTimeMs = 1000;
    private RecorderView recorderPanel;

    public BiosignalFrame(Facade facade) {
        super("Biosignal");
        this.facade = facade;

        // create menu bar
        JToolBar menu = new JToolBar();
        menu.setBackground(MENU_BG_COLOR);
        menu.setForeground(MENU_TEXT_COLOR);
        menu.setBorder(BorderFactory.createEmptyBorder());
        // file menu
        JButton fileButton = new JButton("File");
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = chooseFileToRead(facade);
                if (file != null) {
                    facade.chooseFileDataProvider(file, true);
                    if(recorderPanel != null) {
                        recorderPanel.stop();
                        recorderPanel.close();
                        recorderPanel = null;
                    }
                    setTitle(file.getName());
                }
            }
        });
        menu.add(fileButton);
        // biorecorder menu
        JButton biorecorderButton = new JButton("BioRecorder");
        menu.add(biorecorderButton);
        biorecorderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if(recorderPanel == null) {
                    recorderPanel = new RecorderView(facade.chooseRecorderDataProvider());
                    recorderPanel.setParentWindow(BiosignalFrame.this);
                }
                setTitle("BioRecorder");
                JDialog recorderDialog = new BioRecorderDialog(recorderPanel, BiosignalFrame.this);
            }
        });
        add(menu, BorderLayout.NORTH);

        // create empty chart panel
        chartPanel = createChartPanel(facade, null);
        add(chartPanel, BorderLayout.CENTER);

        // setLocation(X_START, Y_START);
        // setPreferredSize(new Dimension(WIDTH_START, HEIGHT_START));
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        Insets insets = tk.getScreenInsets(getGraphicsConfiguration());
        setPreferredSize(new Dimension(d.width - insets.left - insets.right, d.height - insets.top - insets.bottom));
        setPreferredSize(new Dimension(1300,1000 ));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        facade.addProviderConfigListener(new ProviderConfigListener() {
            @Override
            public void receiveConfig(ProviderConfig providerConfig) {
                updateChartPanel(providerConfig);
            }
        });



        facade.addDataAppendListener(new DataAppendListener() {
            @Override
            public void onDataAppend() {
                chartPanel.dataAppended();
                chartPanel.repaint();
            }
        });
       // addKeyListener(new LetterKeyListener());
        //Вызов менеджера раскладки по умолчанию
        pack();
        setVisible(true);
    }

    private void close() {
        facade.finish();
        if(recorderPanel != null) {
            recorderPanel.close();
        }
        System.exit(0);  // Закрытие приложения без ошибок
    }

    private static BiChartPanel createChartPanel(Facade facade, ProcessingConfig processingConfig) {
        if(processingConfig == null) {
           processingConfig = new ProcessingConfig();
        }

        boolean isTimeXAxis = facade.isDateTime(); // XAxis: false - index; true - time
        boolean scrollsAtEnd = true;
        BiChartPanel chartPanel = new BiChartPanel(isTimeXAxis, processingConfig, scrollsAtEnd);
        int[] chartDataChannels1 = facade.getChartDataChannels1();
        int[] chartDataChannels2 = facade.getChartDataChannels2();
        int[] navDataChannels = facade.getNavigatorDataChannels();
        boolean isYOpposite = false;
        boolean isXOpposite;
        for (int i = 0; i < chartDataChannels1.length; i++) {
            isXOpposite = false;
            int channel = chartDataChannels1[i];
            XYData xyData = facade.getData(channel);
            GroupingApproximation grApprox = facade.getDataGroupingApproximation(channel);
            if (i > 0) {
                chartPanel.addChartStack();
            }
            LineTraceConfig lineConfig = new LineTraceConfig();
            lineConfig.setLineWidth(1);
            lineConfig.setMarkSize(3);
            chartPanel.addChartTrace(xyData.getName(), xyData, grApprox, new LineTracePainter(lineConfig), isXOpposite, isYOpposite);
        }

        for (int i = 0; i < chartDataChannels2.length; i++) {
            isXOpposite = true;
            int channel = chartDataChannels2[i];
            XYData xyData = facade.getData(channel);
            GroupingApproximation grApprox = facade.getDataGroupingApproximation(channel);
            chartPanel.addChartStack();
            LineTraceConfig lineConfig = new LineTraceConfig();
            lineConfig.setLineWidth(1);
            lineConfig.setMarkSize(3);
            chartPanel.addChartTrace(xyData.getName(), xyData, grApprox, new LineTracePainter(lineConfig), isXOpposite, isYOpposite);
        }
        for (int i = 0; i < navDataChannels.length; i++) {
            int channel = navDataChannels[i];
            XYData xyData = facade.getData(channel);
            GroupingApproximation grApprox = facade.getDataGroupingApproximation(channel);
            if (i > 0) {
                chartPanel.addNavigatorStack();
            }
            chartPanel.addNavigatorTrace(xyData.getName(), xyData, grApprox, new VerticalLinePainter());
        }
        return chartPanel;
    }

    private File chooseFileToRead(Facade facade) {
        String[] fileExtensions = facade.getFileExtensions();
        String extensionDescription = fileExtensions[0];
        for (int i = 1; i < fileExtensions.length; i++) {
            extensionDescription = extensionDescription.concat(", ").concat(fileExtensions[i]);
        }
        JFileChooser fileChooser = new JFileChooser();
        File dirToRead = new File(System.getProperty("user.dir"), "records");
        fileChooser.setCurrentDirectory(dirToRead);
        fileChooser.setFileFilter(new FileNameExtensionFilter(extensionDescription, fileExtensions));
        int fileChooserState = fileChooser.showOpenDialog(this);
        if (fileChooserState == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            return file;
        }
        return null;
    }

    private void updateChartPanel(ProviderConfig providerConfig) {
        getContentPane().remove(chartPanel);
        int width = chartPanel.getWidth();
        int height = chartPanel.getHeight();
        long groupingInterval = providerConfig.getRecordingTimeMs() / width;
        ProcessingConfig processingConfig = new ProcessingConfig();
        processingConfig.setGroupingIntervals(groupingInterval);
        processingConfig.setGroupingTimeIntervals(TimeInterval.getUpper(groupingInterval, true));
        chartPanel = createChartPanel(facade, processingConfig);
        chartPanel.setPreferredSize(new Dimension(width, height));
        add(chartPanel);
        revalidate();
    }



 /*   class LetterKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_S) {
                double[] range = chartPanel.getChartXRange();
                startTimeMs = (long) range[0];
                System.out.println("start: " + startTimeMs);
            }
            if (e.getKeyCode() == KeyEvent.VK_E) {
                double[] range = chartPanel.getChartXRange();
                endTimeMs = (long) range[1];
                System.out.println("end: " + endTimeMs);
            }
            if (e.getKeyCode() == KeyEvent.VK_U) {
                System.out.println("read data from " + startTimeMs + " till: " + endTimeMs);
                facade.setReadTimeInterval(startTimeMs, endTimeMs - startTimeMs);
                updateChartPanel();
                facade.start();
            }
            if (e.getKeyCode() == KeyEvent.VK_F) {
                System.out.println("read full data");
                facade.setFullReadInterval();
                updateChartPanel();
                facade.start();
            }
            if (e.getKeyCode() == KeyEvent.VK_C) {
                String file = facade.copyReadIntervalToFile();
                System.out.println("read interval saved to file: " + file);
            }
        }
    }*/
}



