package app;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Chessmate extends Application {
    @Override
    public void start(Stage stage) {
        Display display = new Display();

        stage.setTitle("Chessmate");
        stage.setScene(new Scene(display));
        stage.setMinHeight(350); stage.setMinWidth(450);

        stage.show();
        display.showHomeScreen();

        // For debugging GUI only...
        boolean debug = true;
        if(debug) {
            Thread windowInfoDumper = new Thread(() -> {
                try {
                    while(true) {
                        File logFile = new File("UI Components.xml");
                        logFile.createNewFile();
                        FileWriter fileWriter = new FileWriter("UI Components.xml");
                        printComponentInfo(display, fileWriter); tabSpace++;
                        fileWriter.flush();

                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            windowInfoDumper.setDaemon(true);
            windowInfoDumper.start();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static int tabSpace = 1;
    private static void printComponentInfo(Region region, FileWriter fileWriter) throws IOException {
        String[] packages = region.getClass().getName().split("\\.", 0);
        String className = packages[packages.length-1];
        fileWriter.write("<" + className + "> Width: " + region.getWidth() + " Height: " + region.getHeight() + "\n");
        for(Node c : region.getChildrenUnmodifiable()) {
            if(c instanceof Region) {
                Region innerRegion = (Region) c;
                for(int i = 0; i< tabSpace; i++) fileWriter.write("    ");
                tabSpace++;
                printComponentInfo(innerRegion, fileWriter);
            }
            else {
                packages = c.getClass().getName().split("\\.", 0);
                className = packages[packages.length-1];
                for(int i = 0; i< tabSpace; i++) fileWriter.write("    ");
                fileWriter.write("<" + className + "> Width: " +
                        c.getBoundsInParent().getWidth() + " Height: " +
                        c.getBoundsInParent().getHeight() + " </" + className + ">\n");
            }
        }
        tabSpace--;
        for(int i = 0; i< tabSpace; i++) fileWriter.write("    ");
        fileWriter.write("</" + className + ">\n");
    }
}



