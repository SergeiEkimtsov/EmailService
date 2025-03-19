package com.ekimtsovss.emailjavaservice;

import javax.swing.*;
import java.io.File;

public class AttachmentChooser {
    public static File[] chooseAttachment(){
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setMultiSelectionEnabled(true);
        var option = jFileChooser.showOpenDialog(null);
        if (option==JFileChooser.APPROVE_OPTION){
            return jFileChooser.getSelectedFiles();
        }
        return new File[]{};
    }
}