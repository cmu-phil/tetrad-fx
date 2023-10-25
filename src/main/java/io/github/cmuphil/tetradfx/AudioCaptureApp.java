package io.github.cmuphil.tetradfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioCaptureApp extends Application {

    private TargetDataLine microphone;
    private File audioFile = new File("recorded.wav");

    public static void main(String[] args) {
        // Get all available mixers
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            // Get target lines supported by this mixer
            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();

            for (Line.Info lineInfo : targetLineInfos) {
                if (lineInfo instanceof DataLine.Info) {
                    DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
                    AudioFormat[] supportedFormats = dataLineInfo.getFormats();

                    System.out.println("Mixer: " + mixerInfo.getName());
                    System.out.println("Supported Audio Formats:");

                    for (AudioFormat format : supportedFormats) {
                        System.out.println(format);
                    }
                    System.out.println("------------------------------");
                }
            }
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Button startButton = new Button("Start Recording");
        Button stopButton = new Button("Stop Recording");
        Button playButton = new Button("Play Recording");
        VBox root = new VBox(10, startButton, stopButton, playButton);

        startButton.setOnAction(e -> captureAudio());
        stopButton.setOnAction(e -> stopRecording());
        playButton.setOnAction(e -> playRecording());

        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Audio Capture");
        primaryStage.show();
    }

    public void captureAudio() {
        try {
//            AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, true);
            AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                return;
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            Thread targetThread = new Thread(() -> {
                AudioInputStream audioStream = new AudioInputStream(microphone);
                try {
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            targetThread.start();

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    public void stopRecording() {
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
    }

    public void playRecording() {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }
}



