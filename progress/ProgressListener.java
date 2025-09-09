package progress;
import Models.DownloadInfo;
public interface ProgressListener {
    void onProgressUpdate(DownloadInfo downloadInfo);
    void onDownloadCompleted(DownloadInfo downloadInfo);
    void onDownloadFailed(DownloadInfo downloadInfo, Exception exception);
    void onSpeedUpdate(long bytesPerSecond);
}
