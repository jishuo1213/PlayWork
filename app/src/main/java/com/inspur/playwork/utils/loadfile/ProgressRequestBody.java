/**
 * Copyright 2015 ZhangQu Li
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inspur.playwork.utils.loadfile;

import android.util.Log;


import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class ProgressRequestBody extends RequestBody {

    private static final String TAG = "ProgressRequestBody";

    private static final int SEGMENT_SIZE = 4096; // okio.Segment.SIZE

    private final File file;

    private final String contentType;

    private final ProgressRequestListener progressListener;

    private long contentLength;

    public ProgressRequestBody(File file, String contentType, ProgressRequestListener listener) {
        this.file = file;
        this.contentType = contentType;
        this.progressListener = listener;
    }

    @Override
    public long contentLength() {
        contentLength = file.length();
        return contentLength;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(file);
            long total = 0;
            long read;

            long reportedSize = contentLength / 50;
            long writeBytes = 0;

            while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                writeBytes += read;
                if (progressListener != null) {
                    if (writeBytes >= reportedSize && total != contentLength) {
                        progressListener.onRequestProgress(total, contentLength, false);
                        writeBytes = 0;
                    } else if (total == contentLength) {
                        progressListener.onRequestProgress(total, contentLength, true);
                    }
                }
            }
        } finally {
            Util.closeQuietly(source);
        }
    }
}