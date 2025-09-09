package progress;

import Models.ChunkInfo;
import Models.DownloadInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ProgressTracker {
    private final DownloadInfo downloadInfo;
    private final ProgressListener listener;
    private final ConcurrentHashMap<Integer, ChunkInfo> chunks;
    private final ScheduledExecutorService scheduler;
    private final AtomicLong lastDownloadedBytes;
    private long lastUpdateTime;

    public ProgressTracker(DownloadInfo downloadInfo,ProgressListener listener){
        this.downloadInfo = downloadInfo;
        this.listener = listener;
        this.chunks = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.lastDownloadedBytes = new AtomicLong(0);
        this.lastUpdateTime = System.currentTimeMillis();

        // Start progress monitoring
        startProgressMonitoring();
    }

    public void addChunk(ChunkInfo chunk) {
        chunks.put(chunk.getChunkId(), chunk);
    }

    public void updateChunkProgress(int chunkId, long downloadedBytes){
        ChunkInfo chunk = chunks.get(chunkId);
        if(chunk != null){
            chunk.setDownloadedBytes(downloadedBytes);
            updateOverallProgress();
        }
    }
    public void markChunkCompleted(int chunkId){
        ChunkInfo chunk = chunks.get(chunkId);
        if(chunk != null){
            chunk.setCompleted(true);
            updateOverallProgress();
        }
    }
    private void updateOverallProgress(){
        long totalDownload = chunks.values().stream().mapToLong(ChunkInfo::getDownloadedBytes).sum();
        downloadInfo.setDownloadedBytes(totalDownload);
        if(listener != null){
            listener.onProgressUpdate(downloadInfo);
        }
    }
    private void startProgressMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentBytes = downloadInfo.getDownloadedBytes();
            long currentTime = System.currentTimeMillis();

            long bytesDownloaded = currentBytes - lastDownloadedBytes.get();
            long timeElapsed = currentTime - lastUpdateTime;

            if (timeElapsed > 0) {
                long speed = (bytesDownloaded * 1000) / timeElapsed; // bytes per second
                if (listener != null) {
                    listener.onSpeedUpdate(speed);
                }
            }

            lastDownloadedBytes.set(currentBytes);
            lastUpdateTime = currentTime;
        }, 1, 1, TimeUnit.SECONDS);
    }

    public boolean isDownloadComplete() {
        return chunks.values().stream().allMatch(ChunkInfo::isCompleted);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
