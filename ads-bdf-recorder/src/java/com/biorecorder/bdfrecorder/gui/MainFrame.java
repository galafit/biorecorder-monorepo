package com.biorecorder.bdfrecorder.gui;

import com.biorecorder.bdfrecorder.AvailableComportsListener;
import com.biorecorder.bdfrecorder.Message;
import com.biorecorder.bdfrecorder.ProgressListener;
import com.biorecorder.bdfrecorder.StateChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by galafit on 31/7/18.
 */
public class MainFrame extends JFrame {
    private static final String TITLE = "BioRecorder";
    private final RecorderView recorderView;

    public MainFrame(RecorderViewModel recorder) throws HeadlessException {
        setTitle(TITLE);
        recorderView = new RecorderView(recorder);
        recorderView.setParentWindow(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                recorderView.close();
            }
        });
        add(recorderView);
        pack();
        // place the window to the screen center
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
