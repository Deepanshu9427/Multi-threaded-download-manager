package core;

import Models.DownloadInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLAnalyzer {
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 15000;

    public void analyzeURL(DownloadInfo downloadInfo) throws IOException {
        URL url = new URL(downloadInfo.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try{
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent","Java Download Manager 1.O)");

            int responseCode = connection.getResponseCode();

            if(responseCode != HttpURLConnection.HTTP_OK){
                throw new IOException("server responded with code: " + responseCode);
            }
            long fileSize  = connection.getContentLengthLong();
            if(fileSize <= 0){
                String contentLength = connection.getHeaderField("Content-Length");
                if(contentLength != null){
                    fileSize  = Long.parseLong(contentLength);
                }
            }
            downloadInfo.setFileSize(fileSize);
            String acceptRanges = connection.getHeaderField("Accept-Ranges");
            boolean supportRanges = "bytes".equalsIgnoreCase(acceptRanges);
            downloadInfo.setSupportsRangeRequests(supportRanges);

            String fileName = extractFileName(url, connection);
            downloadInfo.setFileName(fileName);

            System.out.println("File analysis complete:");
            System.out.println("File size: " + fileSize + " bytes");
            System.out.println("Supports range requests: " + supportRanges);
            System.out.println("File name: " + fileName);
        }
        finally {
            connection.disconnect();
        }
    }
    private String extractFileName(URL url, HttpURLConnection connection) {
        // Try to get filename from Content-Disposition header
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            String filename = contentDisposition.substring(
                    contentDisposition.indexOf("filename=") + 9);
            if (filename.startsWith("\"") && filename.endsWith("\"")) {
                filename = filename.substring(1, filename.length() - 1);
            }
            return filename;
        }

        // Extract from URL path
        String path = url.getPath();
        if (path != null && !path.isEmpty() && path.contains("/")) {
            String filename = path.substring(path.lastIndexOf("/") + 1);
            if (!filename.isEmpty()) {
                return filename;
            }
        }

        // Default filename
        return "download_" + System.currentTimeMillis();
    }
}
