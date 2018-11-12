package template.tool;

import processing.app.ui.Toolkit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

// based on
// https://github.com/processing/processing/blob/master/app/src/processing/app/ui/ColorChooser.java

// TODO: use trees to show all files inside data/
// https://docs.oracle.com/javase/tutorial/uiswing/components/tree.html

// TODO: update shader list when switching between sketches
/*
LIST  <  LIST
      >
      X
*/

public class ShaderChooser {
    JDialog window;
    File pathFrom, pathTo;
    Path sourceFile;
    private DefaultListModel userShadersModel;
    private DefaultListModel templateShadersModel;
    private JList userShadersList;
    private JList templateShadersList;
    private JButton editButton;
    private JButton deleteButton;
    private JButton renameButton;
    private JTextField filenameTextField;
    private JButton createButton;

    public ShaderChooser(Frame owner, boolean modal,
                         ActionListener actionListener) {
        window = new JDialog(owner, "Edit Shader", modal);

        Container pane = window.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));

        JPanel paneLeft = new JPanel();
        JPanel paneRight = new JPanel();

        paneLeft.setLayout(new BorderLayout());
        paneRight.setLayout(new BorderLayout());


        // ---------------------------------------------
        // 1. user
        userShadersModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        userShadersList = new JList(userShadersModel);
        userShadersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userShadersList.setSelectedIndex(0);
        userShadersList.addListSelectionListener(
                listSelectionEvent -> {
                    String sourceFileName =
                            (String) userShadersList.getSelectedValue();
                    filenameTextField.setText(sourceFileName);
                    sourceFile = new File(
                            pathTo.getAbsolutePath() + File.separator +
                                    sourceFileName).toPath();
                    editButton.setEnabled(true);
                    renameButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                });
        userShadersList.setVisibleRowCount(18);
        JScrollPane userShadersScroll = new JScrollPane(userShadersList);

        paneLeft.add(new JLabel("Sketch"), BorderLayout.PAGE_START);
        paneLeft.add(userShadersScroll, BorderLayout.CENTER);

        // ---------------------------------------------
        // buttons
        editButton = new JButton("edit");
        renameButton = new JButton("rename");
        deleteButton = new JButton("delete");

        editButton.addActionListener(actionEvent -> {
            try {
                Runtime.getRuntime().exec(new String[]{
                        "xterm", "-hold", "-e",
                        "cd " + pathTo.getAbsolutePath() + " ; vi " +
                                sourceFile.getFileName()});
                // p.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        renameButton.addActionListener(actionEvent -> {
            String targetFileName = filenameTextField.getText();
            if (targetFileName.length() < 6) {
                return;
            }
            if (!targetFileName.contains(".")) {
                targetFileName = targetFileName + ".glsl";
            }
            Path targetFile = new File(pathTo.getAbsolutePath() +
                    File.separator + targetFileName).toPath();
            try {
                Files.move(sourceFile, targetFile);
                int index = userShadersList.getSelectedIndex();
                userShadersModel.remove(index);
                userShadersModel.addElement(targetFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        deleteButton.addActionListener(actionEvent -> {
            try {
                Files.deleteIfExists(sourceFile.toAbsolutePath());
                int index = userShadersList.getSelectedIndex();
                userShadersModel.remove(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
            filenameTextField.setText("");
        });

        JPanel buttons1 = new JPanel();
        buttons1.setLayout(new BoxLayout(buttons1, BoxLayout.LINE_AXIS));
        buttons1.add(editButton);
        buttons1.add(Box.createHorizontalStrut(5));
        buttons1.add(renameButton);
        buttons1.add(Box.createHorizontalStrut(5));
        buttons1.add(deleteButton);
        buttons1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        paneLeft.add(buttons1, BorderLayout.PAGE_END);


        // ---------------------------------------------
        // 2. template list
        templateShadersModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        templateShadersList = new JList(templateShadersModel);
        templateShadersList
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateShadersList.setSelectedIndex(0);
        templateShadersList.addListSelectionListener(
                listSelectionEvent -> {
                    String sourceFileName =
                            (String) templateShadersList.getSelectedValue();
                    filenameTextField.setText(sourceFileName);
                    sourceFile = new File(
                            pathFrom.getAbsolutePath() + File.separator +
                                    sourceFileName).toPath();
                    editButton.setEnabled(false);
                    renameButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                });
        templateShadersList.setVisibleRowCount(18);
        JScrollPane templateShadersScroll =
                new JScrollPane(templateShadersList);

        paneRight.add(new JLabel("Templates"), BorderLayout.PAGE_START);
        paneRight.add(templateShadersScroll, BorderLayout.CENTER);

        // ---------------------------------------------
        // buttons
        createButton = new JButton("create");
        createButton.addActionListener(actionEvent -> {
            String targetFileName = filenameTextField.getText();
            if (targetFileName.length() < 6) {
                return;
            }
            if (!targetFileName.contains(".")) {
                targetFileName = targetFileName + ".glsl";
            }
            Path targetFile = new File(pathTo.getAbsolutePath() +
                    File.separator + targetFileName).toPath();
            try {
                Files.copy(sourceFile, targetFile);
                userShadersModel.addElement(targetFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        filenameTextField = new JTextField(10);

        JPanel buttons2 = new JPanel();
        buttons2.setLayout(new BoxLayout(buttons2, BoxLayout.LINE_AXIS));
        buttons2.add(filenameTextField);
        buttons2.add(Box.createHorizontalStrut(5));
        buttons2.add(createButton);
        buttons2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        paneRight.add(buttons2, BorderLayout.PAGE_END);

        pane.add(paneLeft);
        pane.add(paneRight);


        // ---------------------------------------------

        window.pack();
        //window.setResizable(false);

        window.setLocationRelativeTo(null);

        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                hide();
            }
        });
        Toolkit.registerWindowCloseKeys(window.getRootPane(),
                actionEvent -> hide());

        Toolkit.setIcon(window);
    }

    public void show() {
        window.setVisible(true);
    }


    public void hide() {
        window.setVisible(false);
    }

    private void setTemplateShaders(File[] files) {
        templateShadersModel.clear();
        if (files != null) {
            for (File f : files) {
                templateShadersModel.addElement(f.getName());
            }
        }
    }

    private void setOwnShaders(File[] files) {
        userShadersModel.clear();
        if (files != null) {
            for (File f : files) {
                userShadersModel.addElement(f.getName());
            }
        }
    }

    public void setPaths(File pathFrom, File pathTo) {
        this.pathFrom = pathFrom;
        this.pathTo = pathTo;

        try {
            // Create data folder if missing
            Files.createDirectories(pathTo.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void populate() {
        if (pathFrom.exists() && pathFrom.isDirectory()) {
            File[] files = pathFrom.listFiles(
                    (dir, name) -> name.matches(".*\\.(glsl|vert|frag)$"));
            Arrays.sort(files);
            setTemplateShaders(files);
        } else {
            setTemplateShaders(null);
        }

        if (pathTo.exists() && pathTo.isDirectory()) {

            // Recursive version, for the future
//            try {
//                Files.walk(Paths.get(dataPath))
//                        .filter(Files::isRegularFile)
//                        .forEach(System.out::println);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            // This is not recursive
            File[] files = pathTo.listFiles(
                    (dir, name) -> name.matches(".*\\.(glsl|vert|frag)$"));
            Arrays.sort(files);
            setOwnShaders(files);

        } else {
            setOwnShaders(null);
        }

    }
}