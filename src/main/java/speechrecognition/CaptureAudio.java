package speechrecognition;

import javax.sound.sampled.*;
import java.io.File;

public class CaptureAudio {

    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;
    private File audioFile;

    public CaptureAudio(File audioFile) {
        this.audioFile = audioFile;
    }

    public void startCaptureAudio() {

        try {
            audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            if(!AudioSystem.isLineSupported(dataLineInfo)) { System.err.println("Line not supported."); }
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            new CaptureThread().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

    private AudioFormat getAudioFormat() {

        float sampleRate = 44100.0F;
        int sampleSizeInBits = 16;
        int channels = 2;
        int frameSize = 4;
        float frameRate = 44100.0F;
        AudioFormat.Encoding signed = AudioFormat.Encoding.PCM_SIGNED;
        boolean bigEndian = false;
        return new AudioFormat(signed, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);

    }

    public void stopCaptureAudio() {

        targetDataLine.stop();
        targetDataLine.close();

    }

    private class CaptureThread extends Thread {

        public void run() {
            AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
            AudioInputStream audioInputStream = new AudioInputStream(targetDataLine);

            try {
                AudioSystem.write(audioInputStream, fileType, audioFile);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }

    }
}
