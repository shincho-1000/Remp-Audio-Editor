package com.project.rempaudioeditor.utils;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.customviews.WaveForm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class FileConverter {
    public static ArrayList<Integer> formatFft(@NonNull byte[] fft) {
        ArrayList<Integer> newFft = new ArrayList<>();
        int distance = 3; // new distance between two consecutive bars in the original array
        int currentRepetition; // the loop has to be repeated
        boolean ifAdd = true; // to half the total no of bars
        boolean addAfter = true; // to add the bar after the previous bar or before it

        for (currentRepetition = 0; currentRepetition < distance; currentRepetition++) {
            for (int i = currentRepetition; i < fft.length; i += 4) {
                if (ifAdd) {
                    if (addAfter) {
                        newFft.add((int) fft[i]);
                        addAfter = false;
                    } else {
                        newFft.add(newFft.size()-1, (int) fft[i]);
                        addAfter = true;
                    }
                    ifAdd = false;
                }
                else {
                    ifAdd = true;
                }
            }
        }

        return newFft;
    }

    public static final float BARS_PER_SEC = 10;

    public static WaveForm createWaveForm(@NonNull Context context, @NonNull Uri uriFile) {
        WaveForm waveForm = new WaveForm(context);

        MediaMetadataRetriever audio_data_retriever = new MediaMetadataRetriever();
        audio_data_retriever.setDataSource(context, uriFile);
        int audio_duration_in_milisec = Integer
                .parseInt(audio_data_retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(context, uriFile, null);
            mediaExtractor.selectTrack(0); // which track? lets assume single/mono for this example
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(0);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            MediaCodec mediaCodec = MediaCodec.createDecoderByType(mime);
            mediaCodec.configure(mediaFormat, null, null, 0);
            mediaCodec.start();

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            boolean inputDone = false;
            int index = 0;
            int sample_count = 0;
            long sum = 0;
            ArrayList<Double> waveValues = new ArrayList<>();
            while (true)
            {
                if (!inputDone) // process input stream and queue it up for output processing
                {
                    int inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);
                    if (inputBufferIndex >= 0)
                    {
                        ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                        int chunkSize = mediaExtractor.readSampleData(inputBuffer, 0);
                        if (chunkSize <= 0)
                        {
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        }
                        else
                        {
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, chunkSize, mediaExtractor.getSampleTime(), 0);
                            mediaExtractor.advance();
                        }
                    }
                }

                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000000);
                if (outputBufferIndex >= 0)
                {
                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex); // PCM 16-bit output
                    int outputFrameRate = mediaCodec.getOutputFormat(outputBufferIndex).getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    outputBuffer.position(0);
                    // !!! Sub-sample the buffer based upon your needed sampling rate for display
                    ShortBuffer pcm16bitBuffer = outputBuffer.asShortBuffer();
                    while (pcm16bitBuffer.hasRemaining())
                    {
                        short value = pcm16bitBuffer.get();
                        if (index >= outputFrameRate/BARS_PER_SEC) {
                            if (sample_count > 0) {
                                waveValues.add(Math.pow(Math.abs((double) (sum / sample_count)), 0.7));
                            } else {
                                waveValues.add((double) 0);
                            }
                            sum = 0;
                            sample_count = 0;

                            index = 0;
                        }
                        if (value > 0) {
                            sum += value;
                            sample_count++;
                        }
                        index++;
                        // store the prior values and avg./max/... them for later display based upon some subsampling rate
                    }
                    pcm16bitBuffer.clear();
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        break;
                }
            }
            waveForm.setBars(waveValues);
            mediaCodec.stop();
            mediaCodec.release();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return waveForm;
    }

    /**
     * @param srcPath  the path of source video file.
     * @param dstPath  the path of destination video file.
     * @param startMs  starting time in milliseconds for trimming. Set to
     *                 negative if starting from beginning.
     * @param endMs    end time for trimming in milliseconds. Set to negative if
     *                 no trimming at the end.
     */

    public static void extractAudioFromVideo(Context context, Uri srcPath, String dstPath, int startMs, int endMs) throws IOException {
        // Set up MediaExtractor to read from the source.
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(context, srcPath, null);
        int trackCount = extractor.getTrackCount();
        // Set up MediaMuxer for the destination.
        MediaMuxer muxer;
        muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        HashMap<Integer, Integer> indexMap = new HashMap<>(trackCount);
        int bufferSize = -1;
        for (int i = 0; i < trackCount; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            boolean selectCurrentTrack = mime.startsWith("audio/");

            if (selectCurrentTrack) {
                extractor.selectTrack(i);
                int dstIndex = muxer.addTrack(format);
                indexMap.put(i, dstIndex);
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    bufferSize = Math.max(newSize, bufferSize);
                }
            }
        }
        if (bufferSize < 0) {
            bufferSize = 1024 * 1024;
        }
        // Set up the orientation and starting time for extractor.
        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        String degreesString = retrieverSrc.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (degreesString != null) {
            int degrees = Integer.parseInt(degreesString);
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees);
            }
        }
        if (startMs > 0) {
            extractor.seekTo(startMs * 1000L, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        }
        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
        // for copying each sample and stop when we get to the end of the source
        // file or exceed the end time of the trimming.
        int offset = 0;
        int trackIndex;
        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        muxer.start();
        while (true) {
            bufferInfo.offset = offset;
            bufferInfo.size = extractor.readSampleData(dstBuf, offset);
            if (bufferInfo.size < 0) {
                Log.d("AudioExtractorDecoder", "Saw input EOS.");
                bufferInfo.size = 0;
                break;
            } else {
                bufferInfo.presentationTimeUs = extractor.getSampleTime();
                if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000L)) {
                    Log.d("AudioExtractorDecoder", "The current sample is over the trim end time.");
                    break;
                } else {
                    trackIndex = extractor.getSampleTrackIndex();
                    muxer.writeSampleData(indexMap.get(trackIndex), dstBuf, bufferInfo);
                    extractor.advance();
                }
            }
        }
        muxer.stop();
        muxer.release();
    }
}
