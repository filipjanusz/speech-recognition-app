package speechrecognition;

import edu.cmu.sphinx.api.LiveSpeechRecognizer;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpeechRecognition implements Runnable {

    private AtomicBoolean flag;
    private LiveSpeechRecognizer recognizer;
    private String line = "";
    private JTextArea logs;

    public SpeechRecognition(LiveSpeechRecognizer recognizer, JTextArea logs) {
        this.recognizer = recognizer;
        this.logs = logs;
        flag = new AtomicBoolean(true);
    }

    public void terminate() {
        flag.set(false);
    }

    public void startAgain() {
        flag.set(true);
        line = "";
    }

    public void run() {

        while(flag.get()) {

            if (!line.equals("")) {
                StringBuilder builder = new StringBuilder();
                builder.append("    Result: '");
                builder.append(line);
                builder.append("'");
                builder.append("\n");
                logs.append(builder.toString());
            }

            line = recognizer.getResult().getHypothesis();

        }

    }
}
