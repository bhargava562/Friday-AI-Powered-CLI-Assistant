package com.friday.ai_assistant.tools;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
public class MockDemoWorking {
    public static String pasteText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            // Check if the clipboard contains text data
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            // Handle exceptions if the data format is wrong or inaccessible
            System.err.println("Failed to paste text: " + e.getMessage());
        }
        return "";
    }
}
