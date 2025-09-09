package core;

import Models.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileAssembler {
    private static final int BUFFER_SIZE = 64 * 1024; // 64KB buffer for assembly

    public void assembleFile(DownloadInfo downloadInfo, List<ChunkInfo> chunks) throws IOException {
        String finalFilePath = downloadInfo.getDestinationPath() + File.separator + downloadInfo.getFileName();

        System.out.println("Assembling file: " + finalFilePath);

        try (FileOutputStream finalOutput = new FileOutputStream(finalFilePath)) {
            // Sort chunks by chunk ID to ensure correct order
            chunks.sort((c1, c2) -> Integer.compare(c1.getChunkId(), c2.getChunkId()));

            for (ChunkInfo chunk : chunks) {
                if (!chunk.isCompleted()) {
                    throw new IOException("Chunk " + chunk.getChunkId() + " is not completed");
                }

                File tempFile = new File(chunk.getTempFilePath());
                if (!tempFile.exists()) {
                    throw new IOException("Temporary file not found: " + chunk.getTempFilePath());
                }

                // Copy chunk data to final file
                try (FileInputStream chunkInput = new FileInputStream(tempFile);
                     BufferedInputStream bufferedInput = new BufferedInputStream(chunkInput)) {

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;

                    while ((bytesRead = bufferedInput.read(buffer)) != -1) {
                        finalOutput.write(buffer, 0, bytesRead);
                    }
                }

                System.out.println("Assembled chunk " + chunk.getChunkId());
            }
        }

        // Verify file size
        long finalFileSize = Files.size(Paths.get(finalFilePath));
        if (finalFileSize != downloadInfo.getFileSize()) {
            throw new IOException("File size mismatch. Expected: " + downloadInfo.getFileSize() +
                    ", Actual: " + finalFileSize);
        }

        // Clean up temporary files
        cleanupTempFiles(chunks);

        System.out.println("File assembly completed successfully");
    }

    private void cleanupTempFiles(List<ChunkInfo> chunks) {
        for (ChunkInfo chunk : chunks) {
            File tempFile = new File(chunk.getTempFilePath());
            if (tempFile.exists()) {
                if (tempFile.delete()) {
                    System.out.println("Deleted temp file: " + chunk.getTempFilePath());
                } else {
                    System.err.println("Failed to delete temp file: " + chunk.getTempFilePath());
                }
            }
        }
    }
}
