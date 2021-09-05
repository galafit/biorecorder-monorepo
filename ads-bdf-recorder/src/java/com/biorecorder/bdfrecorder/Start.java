package com.biorecorder.bdfrecorder;

import com.biorecorder.bdfrecorder.gui.MainFrame;
import com.biorecorder.bdfrecorder.gui.RecorderViewModel;

public class Start {
    public static void main(String[] args) {
        JsonPreferences preferences = new JsonPreferences();
        RecorderViewModel bdfRecorder = new RecorderViewModelImpl(new EdfBioRecorderApp(), preferences);
        MainFrame recorderView = new MainFrame(bdfRecorder);
        bdfRecorder.addProgressListener(recorderView);
        bdfRecorder.addAvailableComportsListener(recorderView);
        bdfRecorder.addStateChangeListener(recorderView);

    }
}
