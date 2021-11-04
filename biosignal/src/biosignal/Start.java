package biosignal;

import biosignal.application.EdfProvider;
import biosignal.application.MainFacade;
import biosignal.gui.MainFrame;

import java.io.File;

public class Start {
    public Start(){
        String originalFilename = "ecg_15-03-2021.bdf";
        File recordsDir = new File(System.getProperty("user.dir"), "records");
        File originalFile = new File(recordsDir, originalFilename);
        new MainFrame(new MainFacade());
    }

    public static void main(String[] args) {
        new Start();
    }
}
