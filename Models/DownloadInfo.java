package Models;

public class DownloadInfo {
    private String url, fileName,destinationPath;
    private long fileSize,downloadedBytes;
    private boolean supportsRangeRequests;
    private int threadCount;
    private DownloadStatus status;

    public enum DownloadStatus{
        INITIALIZING, DOWNLOADING, PAUSED, COMPLETED, FAILED
    }

    public DownloadInfo(String url,String destinationPath){
        this.url = url;
        this.destinationPath =destinationPath;
        this.downloadedBytes = 0;
        this.status = DownloadStatus.INITIALIZING;
    }
    public String getUrl(){return url;}
    public void setUrl(String url){this.url = url;}

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getDestinationPath() { return destinationPath; }
    public void setDestinationPath(String destinationPath) { this.destinationPath = destinationPath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public boolean supportsRangeRequests() { return supportsRangeRequests; }
    public void setSupportsRangeRequests(boolean supportsRangeRequests) {
        this.supportsRangeRequests = supportsRangeRequests;
    }

    public int getThreadCount() { return threadCount; }
    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }

    public long getDownloadedBytes() { return downloadedBytes; }
    public void setDownloadedBytes(long downloadedBytes) { this.downloadedBytes = downloadedBytes; }

    public DownloadStatus getStatus() { return status; }
    public void setStatus(DownloadStatus status) { this.status = status; }

    public double getProgress(){
        return fileSize > 0 ? (double) downloadedBytes / fileSize * 100 : 0;
    }

}
