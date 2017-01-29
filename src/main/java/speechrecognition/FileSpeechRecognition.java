package speechrecognition;

import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileSpeechRecognition implements Runnable {

    private StreamSpeechRecognizer recognizer;
    private JTextArea logs;
    private InputStream inputStream;
    private JLabel status;

    public FileSpeechRecognition(StreamSpeechRecognizer recognizer,
                                 JTextArea logs,
                                 File file,
                                 JLabel status) {

        this.recognizer = recognizer;
        this.logs = logs;
        this.status = status;

        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logs.append("\nFile not found.\n");
            e.printStackTrace();
        }
    }

    public void run() {

        recognizer.startRecognition(inputStream);
        SpeechResult speechResult;
        status.setText("Status: Working");

        while((speechResult = recognizer.getResult()) != null) {

            StringBuilder builder = new StringBuilder();
            builder.append("    Hypothesis: '");
            builder.append(speechResult.getHypothesis());
            builder.append("'.\n");

            System.out.println(builder.toString());
            logs.append(builder.toString());

        }

        recognizer.stopRecognition();
        status.setText("Status: Idle");
    }
}
