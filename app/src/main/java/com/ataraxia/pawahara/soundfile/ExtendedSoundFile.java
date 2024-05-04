package com.ataraxia.pawahara.soundfile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;


public class ExtendedSoundFile extends SoundFile {

//    public ExtendedSoundFile() {
//        super();
//    }
//
//    public SoundFile appendSoundFiles(List<SoundFile> soundFiles, ProgressListener progressListener) {
//        int totalSamples = 0;
//        int totalChannels = 0;
//        List<ByteBuffer> audioDataList = new ArrayList<>();
//
//        for (SoundFile soundFile : soundFiles) {
//            if (soundFile != null) {
//                ByteBuffer audioData = soundFile.getDecodedData();
//                totalSamples += audioData.remaining();
//                totalChannels = soundFile.getChannels();
//                audioDataList.add(audioData);
//            }
//        }
//
//        ByteBuffer combinedData = ByteBuffer.allocate(totalSamples * 2); // Assuming 16-bit audio
//
//        for (ByteBuffer audioData : audioDataList) {
//            audioData.rewind();
//            combinedData.put(audioData);
//        }
//
//        combinedData.order(ByteOrder.LITTLE_ENDIAN);
//        combinedData.rewind();
//
//        // Create a new SoundFile from the combined audio data
//        SoundFile combinedSoundFile = new SoundFile();
//        combinedSoundFile.setProgressListener(progressListener);// You can set a progress listener if needed
//        combinedSoundFile.setDecodedData(combinedData, totalChannels);
//
//        return combinedSoundFile;
//    }
}
