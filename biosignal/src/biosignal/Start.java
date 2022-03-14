package biosignal;

import biosignal.application.*;
import biosignal.gui.BiosignalFrame;

import java.io.File;

public class Start {
    public Start(){
        String originalFilename = "ecg_15-03-2021.bdf";
        File recordsDir = new File(System.getProperty("user.dir"), "records");
        File originalFile = new File(recordsDir, originalFilename);
        //new TestFrame(new TestFacade(new ConfiguratorECG(), false));
        new BiosignalFrame(new MainFacade(new ConfiguratorTestForAdc(), false));

    }

    public static void main(String[] args) {
        new Start();
    }
}
