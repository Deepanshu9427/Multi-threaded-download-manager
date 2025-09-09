package worker;
import Models.DownloadInfo;
import Models.ChunkInfo;
import progress.ProgressTracker;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadWorker implements Runnable {
    private final DownloadInfo downloadInfo;
    private final ChunkInfo chunkInfo;
    private final ProgressTracker progressTracker;
    private static final int BUFFER_SIZE = 8192; // 8KB buffer
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY = 1000; // 1 second

    public DownloadWorker(DownloadInfo downloadInfo, ChunkInfo chunkInfo, ProgressTracker progressTracker) {
        this.downloadInfo = downloadInfo;
        this.chunkInfo = chunkInfo;
        this.progressTracker = progressTracker;
    }

    @Override
    public void run() {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < MAX_RETRIES) {
            try {
                downloadChunk();
                progressTracker.markChunkCompleted(chunkInfo.getChunkId());
                System.out.println("Chunk " + chunkInfo.getChunkId() + " completed successfully");
                return;

            } catch (Exception e) {
                lastException = e;
                retryCount++;
                System.err.println("Chunk " + chunkInfo.getChunkId() + " failed (attempt " +
                        retryCount + "/" + MAX_RETRIES + "): " + e.getMessage());

                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY * retryCount); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }

        System.err.println("Chunk " + chunkInfo.getChunkId() + " failed after " + MAX_RETRIES + " attempts");
        // In a real implementation, you'd notify the download manager of the failure
    }

    private void downloadChunk() throws IOException {
        URL url = new URL(downloadInfo.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // Configure connection
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "Java Download Manager 1.0");

            // Set range header for partial content
            if (downloadInfo.supportsRangeRequests() && downloadInfo.getThreadCount() > 1) {
                String rangeHeader = "bytes=" + chunkInfo.getStartByte() + "-" + chunkInfo.getEndByte();
                connection.setRequestProperty("Range", rangeHeader);
                System.out.println("Chunk " + chunkInfo.getChunkId() + " requesting: " + rangeHeader);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                throw new IOException("Server responded with code: " + responseCode);
            }

            // Download the chunk
            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream outputStream = new FileOutputStream(chunkInfo.getTempFilePath())) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // Update progress
                    chunkInfo.setDownloadedBytes(totalBytesRead);
                    progressTracker.updateChunkProgress(chunkInfo.getChunkId(), totalBytesRead);
                }
            }

        } finally {
            connection.disconnect();
        }
    }
}