package biosignal.gui;

import com.biorecorder.bdfrecorder.gui.RecorderView;

import javax.swing.*;
import java.awt.*;

public class BioRecorderDialog extends JDialog {

    public BioRecorderDialog(RecorderView recorderPanel, Window dialogOwner) {
        super(dialogOwner);
        setModal(false);
        recorderPanel.setParentWindow(this);
        add(recorderPanel);
        pack();
        // place the window to the screen center
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
