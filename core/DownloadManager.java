package core;

import progress.ProgressTracker;
import progress.ProgressListener;
import Models.DownloadInfo;
import Models.ChunkInfo;

import worker.DownloadWorker;
import java.util.List;
import java.util.concurrent.*;

public class DownloadManager {
    private final URLAnalyzer urlAnalyzer;
    private final ChunkCalculator chunkCalculator;
    private final FileAssembler fileAssembler;
    private ExecutorService threadPool;

    public DownloadManager() {
        this.urlAnalyzer = new URLAnalyzer();
        this.chunkCalculator = new ChunkCalculator();
        this.fileAssembler = new FileAssembler();
    }

    public void downloadFile(String url, String destinationPath, ProgressListener listener) {
        DownloadInfo downloadInfo = new DownloadInfo(url, destinationPath);
        ProgressTracker progressTracker = new ProgressTracker(downloadInfo, listener);

        try {
            // Phase 1: Analyze URL and get file information
            System.out.println("Analyzing URL: " + url);
            downloadInfo.setStatus(DownloadInfo.DownloadStatus.INITIALIZING);
            urlAnalyzer.analyzeURL(downloadInfo);

            // Phase 2: Calculate chunks
            List<ChunkInfo> chunks = chunkCalculator.calculateChunks(downloadInfo);

            // Add chunks to progress tracker
            for (ChunkInfo chunk : chunks) {
                progressTracker.addChunk(chunk);
            }

            // Phase 3: Start download
            downloadInfo.setStatus(DownloadInfo.DownloadStatus.DOWNLOADING);
            startDownload(downloadInfo, chunks, progressTracker);

            // Phase 4: Wait for completion
            waitForDownloadCompletion(progressTracker);

            // Phase 5: Assemble file
            System.out.println("All chunks downloaded. Assembling file...");
            fileAssembler.assembleFile(downloadInfo, chunks);

            // Phase 6: Complete
            downloadInfo.setStatus(DownloadInfo.DownloadStatus.COMPLETED);
            if (listener != null) {
                listener.onDownloadCompleted(downloadInfo);
            }

        } catch (Exception e) {
            downloadInfo.setStatus(DownloadInfo.DownloadStatus.FAILED);
            if (listener != null) {
                listener.onDownloadFailed(downloadInfo, e);
            }
            System.err.println("Download failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup(progressTracker);
        }
    }

    private void startDownload(DownloadInfo downloadInfo, List<ChunkInfo> chunks, ProgressTracker progressTracker) {
        threadPool = Executors.newFixedThreadPool(downloadInfo.getThreadCount());

        System.out.println("Starting download with " + downloadInfo.getThreadCount() + " threads");

        for (ChunkInfo chunk : chunks) {
            DownloadWorker worker = new DownloadWorker(downloadInfo, chunk, progressTracker);
            threadPool.submit(worker);
        }

        threadPool.shutdown(); // No more tasks will be accepted
    }

    private void waitForDownloadCompletion(ProgressTracker progressTracker) throws InterruptedException {
        // Wait for all threads to complete
        if (!threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
            System.err.println("Download threads did not complete in expected time");
        }

        // Additional check to ensure all chunks are completed
        while (!progressTracker.isDownloadComplete()) {
            Thread.sleep(100);
        }
    }

    private void cleanup(ProgressTracker progressTracker) {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }

        if (progressTracker != null) {
            progressTracker.shutdown();
        }
    }
}
