//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//
package com.ClarifAI.main.sample.src.textualModule;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat;
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback;

import java.util.LinkedList;

public class MicrophoneStream extends PullAudioInputStreamCallback {
    private static final int SAMPLE_RATE = 16000;
    private final AudioStreamFormat format;
    private AudioRecord recorder;

    private LinkedList<Short> amplitudeQueue = new LinkedList<>();
    private static final int SECONDS = 5;
    private static final int SAMPLES_IN_5_SECONDS = SAMPLE_RATE * SECONDS;

    public MicrophoneStream() {
        this.format = AudioStreamFormat.getWaveFormatPCM(SAMPLE_RATE, (short)16, (short)1);
        this.initMic();
    }

    public AudioStreamFormat getFormat() {
        return this.format;
    }

    @Override
    public int read(byte[] bytes) {
        if (this.recorder != null) {
            long ret = this.recorder.read(bytes, 0, bytes.length);
            detectAnomaly(bytes);
            return (int) ret;
        }
        return 0;
    }

    private void detectAnomaly(byte[] bytes) {
        // Extract short from byte array and add to amplitudeQueue
        for (int i = 0; i < bytes.length; i += 2) {
            short amplitude = (short) ((bytes[i] & 0xFF) | (bytes[i + 1] << 8));
            amplitudeQueue.add(amplitude);

            // Remove older samples if queue has more than 5 seconds of data
            if (amplitudeQueue.size() > SAMPLES_IN_5_SECONDS) {
                amplitudeQueue.poll();
            }
        }

        // Calculate average amplitude
        double avgAmplitude = 0;
        for (Short amplitude : amplitudeQueue) {
            avgAmplitude += Math.abs(amplitude);
        }
        avgAmplitude /= amplitudeQueue.size();

        // Detect sudden change in amplitude
        for (int i = 0; i < bytes.length; i += 2) {
            short amplitude = (short) ((bytes[i] & 0xFF) | (bytes[i + 1] << 8));
            if (Math.abs(amplitude) > avgAmplitude * (1 + Settings.abnormalSoundThreshold)) {
                Log.d("AnomalyDetector", "Anomaly Detected");
                DetectionService.AbnormalSoundDetected();
            }
        }
    }

    @Override
    public void close() {
        this.recorder.release();
        this.recorder = null;
    }

    private void initMic() {
        // Note: currently, the Speech SDK support 16 kHz sample rate, 16 bit samples, mono (single-channel) only.
        AudioFormat af = new AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build();
        this.recorder = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                .setAudioFormat(af)
                .build();

        this.recorder.startRecording();
    }
}
