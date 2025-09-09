package Models;

public class ChunkInfo {
    private int chunkId;
    private long startByte;
    private long endByte;
    private long downloadedBytes;
    private String tempFilePath;
    private boolean completed;

    public ChunkInfo(int chunkId, long startByte, long endByte, String tempFilePath) {
        this.chunkId = chunkId;
        this.startByte = startByte;
        this.endByte = endByte;
        this.tempFilePath = tempFilePath;
        this.downloadedBytes = 0;
        this.completed = false;
    }

    // Getters and setters
    public int getChunkId() { return chunkId; }
    public long getStartByte() { return startByte; }
    public long getEndByte() { return endByte; }
    public long getDownloadedBytes() { return downloadedBytes; }
    public void setDownloadedBytes(long downloadedBytes) { this.downloadedBytes = downloadedBytes; }
    public String getTempFilePath() { return tempFilePath; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getChunkSize() {
        return endByte - startByte + 1;
    }

    public double getProgress() {
        return getChunkSize() > 0 ? (double) downloadedBytes / getChunkSize() * 100 : 0;
    }
}
