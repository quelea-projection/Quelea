/* 
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.main.actionhandlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.javafx.dialog.Dialog;
import org.quelea.services.languages.LabelGrabber;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.QueleaApp;
import org.quelea.windows.main.StatusPanel;
import org.quelea.windows.multimedia.RecordingEncoder;

/**
 * Class to handle the recordings.
 *
 * @author Arvid
 */
public class RecordingsHandler {

    private static final Logger LOGGER = LoggerUtils.getLogger();
    private Dialog noDevicesDialog;
    private File wavFile;
    private final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(Calendar.getInstance().getTime());
    private final String path = QueleaProperties.get().getRecordingsPath(); // Get path from settings
    private final AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE; // format of audio file
    private AudioInputStream ais;
    private TargetDataLine targetLine; // the line from which audio data is captured
    private AudioFormat format;
    //Sound level variables
//    private Thread captureThread;
//    private float level;
//    private final static float MAX_8_BITS_SIGNED = Byte.MAX_VALUE;
//    private final static float MAX_8_BITS_UNSIGNED = 0xff;
//    private final static float MAX_16_BITS_SIGNED = Short.MAX_VALUE;
//    private final static float MAX_16_BITS_UNSIGNED = 0xffff;
    private boolean isRecording;
    private boolean finishedSaving;
    private StatusPanel statusPanel;
    private RecordingEncoder encoder;

    /**
     * Defines an audio format
     */
    private AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        return format;
    }

    /**
     * Initialize a new recording. Captures the sound and saves it to a WAV file
     *
     * @param pb
     * @param textField
     * @param tb
     */
    public void start(ProgressBar pb, TextField textField, ToggleButton tb) {
        try {
            isRecording = true;
            String fileName = timeStamp;
            wavFile = new File(path, fileName + ".wav");
            Platform.runLater(() -> {
                textField.setText(fileName);
            });
            format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (AudioSystem.isLineSupported(info)) {
                LOGGER.log(Level.INFO, "Capturing audio");
                targetLine = (TargetDataLine) AudioSystem.getLine(info);
                targetLine.open(format);
                targetLine.start();   // start capturing
                ais = new AudioInputStream(targetLine);
                startBuffering(pb, tb);
            } else {
                LOGGER.log(Level.INFO, "No recording device found");
                Platform.runLater(() -> {
                    Dialog.Builder setRecordingWarningBuilder = new Dialog.Builder()
                            .create()
                            .setTitle(LabelGrabber.INSTANCE.getLabel("recording.no.devices.title"))
                            .setMessage(LabelGrabber.INSTANCE.getLabel("recording.no.devices.message"))
                            .addLabelledButton(LabelGrabber.INSTANCE.getLabel("ok.button"), (ActionEvent t) -> {
                                noDevicesDialog.hide();
                                noDevicesDialog = null;
                            });
                    noDevicesDialog = setRecordingWarningBuilder.setWarningIcon().build();
                    noDevicesDialog.show();
                });
                Platform.runLater(() -> {
                    QueleaApp.get().getMainWindow().getMainToolbar().stopRecording();
                });
            }
        } catch (LineUnavailableException ex) {
            LOGGER.log(Level.WARNING, "Line unavailable", ex);
        }
    }

    /**
     * Closes the target data line to finish capturing and recording
     *
     * @param textField
     * @param tb
     * @throws UnsupportedAudioFileException
     */
    public void finish(TextField textField, ToggleButton tb) throws UnsupportedAudioFileException {
        try {
            if (targetLine == null) { //Means we never started recording, probably no available devices
                return;
            }
            targetLine.stop();
            targetLine.close();
            try {
                ais.close();
            } catch (IOException ex) {
                Logger.getLogger(RecordingsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            File newName = new File(path, textField.getText().replaceAll("[^\\p{L}0-9.-]", "_") + ".wav");
            wavFile.renameTo(newName);

            // Convert to MP3 if setting is checked
            if (QueleaProperties.get().getConvertRecordings()) {
                if (!Utils.isMac()) {
                    String[] options = {":sout=#transcode{vcodec=none,acodec=mp3,ab=128,channels=2,samplerate=44100}:std{access=file{no-overwrite},mux=mp3,dst='" + newName.getAbsolutePath().replace(".wav", ".mp3") + "'} vlc://quit"};
                    new RecordingEncoder(newName.getAbsolutePath(), options).run();
                } else {
                    convertOSX(newName.getAbsolutePath());
                }
            }

            LOGGER.log(Level.INFO, "Saved");
        } finally {
            isRecording = false;
            finishedSaving = true;
        }
    }

    /**
     * Method to start capturing sound.
     *
     * @param pb ProgressBar to display sound input level
     * @param tb ToggleButton to display time
     */
    private void startBuffering(ProgressBar pb, ToggleButton tb) {
        new Thread() {
            public void run() {
                try {
                    AudioSystem.write(ais, fileType, wavFile);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Error writing to wav", ex);
                }
            }
        }.start();
//        Runnable runner = new Runnable() {
//
//            @Override
//            public void run() {
//                running = true;
//        while (running) {
//                    calculateLevel(buffer, 0, 0);
//                    Platform.runLater(() -> {
//                        pb.setProgress(level);
//                    });
//                    long elapsedTimeMillis;
//                    elapsedTimeMillis = System.currentTimeMillis() - startTime + tempTime;
//                    setTime(elapsedTimeMillis, tb);
//
//                    // Change the color of the ProgressBar depending on level
//                    // Proper limits should be checked
//                    Platform.runLater(() -> {
//                        if (level > 0.9f) {
//                            pb.setStyle("-fx-accent: red;");
//                        } else if (level > 0.7) {
//                            pb.setStyle("-fx-accent: orange;");
//                        } else {
//                            pb.setStyle("-fx-accent: green;");
//                        }
//                    });
//
//                }
//                Platform.runLater(() -> {
//                    tb.setText("");
//                });
//            }
//
//        };
//        captureThread = new Thread(runner);
//        captureThread.start();
        }

//    /**
//     * Method to calculate input level.
//     *
//     * @param buffer
//     * @param readPoint
//     * @param leftOver
//     */
//    private void calculateLevel(byte[] buffer,
//            int readPoint,
//            int leftOver) {
//        int max = 0;
//        boolean use16Bit = (format.getSampleSizeInBits() == 16);
//        boolean signed = (format.getEncoding()
//                == AudioFormat.Encoding.PCM_SIGNED);
//        boolean bigEndian = (format.isBigEndian());
//        if (use16Bit) {
//            for (int i = readPoint; i < buffer.length - leftOver; i += 2) {
//                int value = 0;
//                // deal with endianness
//                int hiByte = (bigEndian ? buffer[i] : buffer[i + 1]);
//                int loByte = (bigEndian ? buffer[i + 1] : buffer[i]);
//                if (signed) {
//                    short shortVal = (short) hiByte;
//                    shortVal = (short) ((shortVal << 8) | (byte) loByte);
//                    value = shortVal;
//                } else {
//                    value = (hiByte << 8) | loByte;
//                }
//                max = Math.max(max, value);
//            } // for
//        } else {
//            // 8 bit - no endianness issues, just sign
//            for (int i = readPoint; i < buffer.length - leftOver; i++) {
//                int value = 0;
//                if (signed) {
//                    value = buffer[i];
//                } else {
//                    short shortVal = 0;
//                    shortVal = (short) (shortVal | buffer[i]);
//                    value = shortVal;
//                }
//                max = Math.max(max, value);
//            } // for
//        } // 8 bit
//        // express max as float of 0.0 to 1.0 of max value
//        // of 8 or 16 bits (signed or unsigned)
//        if (signed) {
//            if (use16Bit) {
//                level = (float) max / MAX_16_BITS_SIGNED;
//            } else {
//                level = (float) max / MAX_8_BITS_SIGNED;
//            }
//        } else if (use16Bit) {
//            level = (float) max / MAX_16_BITS_UNSIGNED;
//        } else {
//            level = (float) max / MAX_8_BITS_UNSIGNED;
//        }
//    } // calculateLevel


    public boolean getIsRecording() {
        return isRecording;
    }

    public boolean getFinishedSaving() {
        return finishedSaving;
    }

    /**
     * @return the status
     */
    public boolean isConverting() {
        if (encoder != null) {
            return encoder.isConverting();
        } else {
            return false;
        }
    }

    /**
     * Use Mac command line to convert recording to MP3 and delete old file.
     *
     * @param file original to convert.
     */
    private void convertOSX(String file) {
        Platform.runLater(() -> {
            statusPanel = QueleaApp.get().getStatusGroup().addPanel(LabelGrabber.INSTANCE.getLabel("Converting recording to MP3"));
        });
        Process vlcProcess;
        List<String> vlcArgs = new ArrayList<>();
        {
            vlcArgs.add("/Applications/VLC.app/Contents/MacOS/VLC");
            vlcArgs.add(file);
            vlcArgs.add("-I");
            vlcArgs.add("dummy");
            vlcArgs.add("--sout=#transcode{vcodec=none,acodec=mp3,ab=128,channels=2,samplerate=44100}:std{access=file{no-overwrite},mux=mp3,dst=" + file.replace("wav", "mp3") + "}");
            vlcArgs.add("vlc://quit");
        }

        try {
            vlcProcess = Runtime.getRuntime().exec(vlcArgs.toArray(new String[0]));
        } catch (Exception exc) {
            LOGGER.log(Level.INFO, "Failed to start VLC", exc);
        } finally {
            // Wait a second to make sure the conversion starts before deleting the old file
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            String error;
            Path oldFile = Paths.get(file);
            do {
                try {
                    if (new File(file.replace("wav", "mp3")).exists()) {
                        Files.delete(oldFile);
                    }
                    error = "";
                } catch (NoSuchFileException | DirectoryNotEmptyException x) {
                    error = "";
                } catch (IOException x) {
                    // If the file is still being read by the system,
                    // keep trying to delete until it's avaiable again.
                    error = "busy";
                }
            } while (!error.equals(""));
            if (statusPanel != null) {
                Platform.runLater(() -> {
                    statusPanel.done();
                });
            }
        }
    }
    
}
