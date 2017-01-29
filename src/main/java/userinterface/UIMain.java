package userinterface;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import speechrecognition.FileSpeechRecognition;
import speechrecognition.SpeechRecognition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UIMain extends JFrame {

    private static final String ACOUSTIC_MODEL = "src/main/resources/acousticmodel";
    private static final String DICTIONARY_PATH = "src/main/resources/dictionary/cmudict-en-us.dict";
    private static final String GRAMMAR_PATH = "src/main/resources/grammar/";
    private static final String LANGUAGE_MODEL = "src/main/resources/languagemodel/languagemodel.lm";

    private JPanel basic;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logs;
    private JScrollPane scroll;
    private JLabel status;
    private JFileChooser chooser;
    private JMenuItem start;
    private JMenuItem stop;
    private JMenuItem decipher;
    private JMenuItem saveLogs;
    private JMenuItem exit;

    private Configuration configuration;
    private SpeechRecognition recognition;
    private LiveSpeechRecognizer recognizer;
    private StreamSpeechRecognizer streamRecognizer;
    private Thread speechRecognitionThread;

    public UIMain() {

        userInterfaceInitialize();

    }

    private void userInterfaceInitialize() {

        basic = new JPanel();
        basic.setLayout(new BoxLayout(basic, BoxLayout.Y_AXIS));
        add(basic);

        //Top panel
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(10, 10));
        topPanel.setMinimumSize(new Dimension(0, 311));

        logs = new JTextArea();
        logs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        logs.setEnabled(false);
        logs.setDisabledTextColor(Color.GRAY);
        logs.setText("");
        scroll = new JScrollPane(logs);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        topPanel.add(scroll);

        status = new JLabel("Status: Idle");
        topPanel.add(status, BorderLayout.SOUTH);

        //Bottom panel
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setMinimumSize(new Dimension(480, 250));

        startButton = new JButton("Start");
        startButton.addActionListener(new StartButtonListener());
        startButton.setEnabled(true);
        bottomPanel.add(startButton);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(new StopButtonListener());
        stopButton.setEnabled(false);
        bottomPanel.add(stopButton);

        basic.add(topPanel);
        basic.add(bottomPanel);

        //Menu
        createMenu();

        //Frame
        setTitle("SpeechRecognitionApp");
        setSize(new Dimension(500, 400));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //CmuSphinx configuration
        try {
            configuration = new Configuration();
            configuration.setAcousticModelPath(ACOUSTIC_MODEL);
            configuration.setDictionaryPath(DICTIONARY_PATH);
            //configuration.setLanguageModelPath(LANGUAGE_MODEL);
            configuration.setGrammarPath(GRAMMAR_PATH);
            configuration.setUseGrammar(true);
            configuration.setGrammarName("dialog");
            recognizer = new LiveSpeechRecognizer(configuration);
            recognizer.startRecognition(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createMenu() {

        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        start = new JMenuItem("Start Recognition");
        start.addActionListener(new StartButtonListener());
        fileMenu.add(start);

        stop = new JMenuItem("Stop Recognition");
        stop.addActionListener(new StopButtonListener());
        stop.setEnabled(false);
        fileMenu.add(stop);
        fileMenu.addSeparator();

        decipher = new JMenuItem("Decipher File");
        decipher.addActionListener(new DecipherMenuItemListener());
        fileMenu.add(decipher);
        fileMenu.addSeparator();

        saveLogs = new JMenuItem("Save Logs");
        saveLogs.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                int result = chooser.showSaveDialog(null);

                if(result == JFileChooser.APPROVE_OPTION) {
                    File choosenFile = chooser.getSelectedFile();
                    FileWriter fileWriter = null;
                    try {
                        fileWriter = new FileWriter(choosenFile + ".txt");
                        fileWriter.write(logs.getText());
                        logs.append("\nLogs saved to file (" + chooser.getName(choosenFile) + ".txt).");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                        System.out.println(ioException.getMessage());
                    } finally {
                        try{
                            if (fileWriter != null) {
                                fileWriter.close();
                            }
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                            System.out.println(ioException.getMessage());
                        }

                    }
                }
            }
        });
        fileMenu.add(saveLogs);
        fileMenu.addSeparator();

        exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exit);

    }

    private class DecipherMenuItemListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            try {
                streamRecognizer = new StreamSpeechRecognizer(configuration);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            int result = chooser.showOpenDialog(null);

            if(result == JFileChooser.APPROVE_OPTION) {
                File choosenFile = chooser.getSelectedFile();
                FileSpeechRecognition fileRecognition = new FileSpeechRecognition(streamRecognizer, logs, choosenFile, status);

                logs.append("\nFile loaded (" + chooser.getName(choosenFile) + ").\n");

                Thread fileSpeechRecognitionThread = new Thread(fileRecognition);
                fileSpeechRecognitionThread.start();
            }

        }
    }

    private class StartButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            startButton.setEnabled(false);
            start.setEnabled(false);
            stopButton.setEnabled(true);
            stop.setEnabled(true);
            status.setText("Status: Working");

            if(recognition == null) {
                recognition = new SpeechRecognition(recognizer, logs);
            } else {
                recognition.startAgain();
            }

            speechRecognitionThread = new Thread(recognition);
            speechRecognitionThread.start();

            System.out.println("Speech recognition started...");
            logs.append("\nSpeech recognition started... \n");

        }
    }

    private class StopButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            startButton.setEnabled(true);
            start.setEnabled(true);
            stopButton.setEnabled(false);
            stop.setEnabled(false);
            status.setText("Status: Idle");

            recognition.terminate();

            System.out.println("Speech recognition stopped...");
            logs.append("Speech recognition stopped... \n");

        }
    }
}
