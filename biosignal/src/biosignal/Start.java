package biosignal;

import biosignal.application.EdfProvider;
import biosignal.application.MainFacade;
import biosignal.gui.MainFrame;

import java.io.File;

public class Start {
    private MainFacade mainFacade;
    private MainFrame frame;

    public Start(){
        String originalFilename = "ecg_15-03-2021.bdf";
        File recordsDir = new File(System.getProperty("user.dir"), "records");
        File originalFile = new File(recordsDir, originalFilename);

        EdfProvider edfProvider = new EdfProvider(originalFile);
        mainFacade = new MainFacade(edfProvider);
        frame  = new MainFrame(mainFacade);
    }

    public static void main(String[] args) {
        new Start();
    }
}
