package core;

import Models.ChunkInfo;
import Models.DownloadInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChunkCalculator {
    private static final long MIN_CHUNK_SIZE = 1024 * 1024;
    private static final int MAX_THREADS = 8;
    private static final int MIN_THREADS = 1;
    public List<ChunkInfo> calculateChunks(DownloadInfo downloadInfo){
        List<ChunkInfo> chunks = new ArrayList<>();
        long fileSize = downloadInfo.getFileSize();
        int threadCount = calculateOptimalThreadCount(fileSize,downloadInfo.supportsRangeRequests());
        downloadInfo.setThreadCount(threadCount);
        if(threadCount ==1 || !downloadInfo.supportsRangeRequests()){
            String tempFilePath = createTempFilePath(downloadInfo,0);
            chunks.add(new ChunkInfo(0,0,fileSize-1,tempFilePath));
            return chunks;
        }
        long chunkSize = fileSize/threadCount;
        long remainderBytes = fileSize % threadCount;
        for(int i=0;i<threadCount;i++){
            long startByte = i* chunkSize;
            long endByte = startByte + chunkSize -1;
            if(i== threadCount-1){
                endByte += remainderBytes;
            }
            String tempFilePath = createTempFilePath(downloadInfo, i);
            chunks.add(new ChunkInfo(i,startByte,endByte,tempFilePath));
        }
        System.out.println("Created "+ threadCount + "chunks for download");
        return chunks;
    }
    private int calculateOptimalThreadCount(long fileSize,boolean supportRanges){
        if(!supportRanges || fileSize< MIN_CHUNK_SIZE) return MIN_THREADS;
        int threads = (int) Math.min(fileSize / MIN_CHUNK_SIZE, MAX_THREADS);
        return Math.max(threads,MIN_THREADS);
    }

    private String createTempFilePath(DownloadInfo downloadInfo, int chunkId){
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = downloadInfo.getFileName() + ".part" + chunkId;
        return tempDir + File.separator + fileName;
    }
}
