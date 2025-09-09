import java.io.File;
import core.*;
import Models.*;
import progress.*;


public class Main {
    public static void main(String[] args) {
        // Example usage
        String url = "https://www.examplefile.com/video/mp4/10-mb-mp4";
        String destinationPath = System.getProperty("user.home") + File.separator + "Downloads";

        DownloadManager downloadManager = new DownloadManager();

        // Create a progress listener
        ProgressListener listener = new ProgressListener() {
            @Override
            public void onProgressUpdate(DownloadInfo downloadInfo) {
                System.out.printf("Progress: %.2f%% (%d/%d bytes)%n",
                        downloadInfo.getProgress(),
                        downloadInfo.getDownloadedBytes(),
                        downloadInfo.getFileSize());
            }

            @Override
            public void onDownloadCompleted(DownloadInfo downloadInfo) {
                System.out.println("Download completed successfully!");
                System.out.println("File saved to: " + downloadInfo.getDestinationPath() +
                        File.separator + downloadInfo.getFileName());
            }

            @Override
            public void onDownloadFailed(DownloadInfo downloadInfo, Exception exception) {
                System.err.println("Download failed: " + exception.getMessage());
            }

            @Override
            public void onSpeedUpdate(long bytesPerSecond) {
                System.out.printf("Speed: %s/s%n", formatBytes(bytesPerSecond));
            }
        };

        // Start download
        System.out.println("Starting download...");
        downloadManager.downloadFile(url, destinationPath, listener);

        System.out.println("Download process completed.");
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}