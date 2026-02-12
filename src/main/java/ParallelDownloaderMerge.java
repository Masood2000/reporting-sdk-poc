import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/***
 * @Author: Masood
 * @Date: 2026-02-12
 * @Description: Parallel Downloader class using Merge Strategy
 */
public class ParallelDownloaderMerge {

    public void downloadInParallel(String fileUrl, String destinationPath, int numberOfThreads) throws Exception {

        FileMetadata metadata = new FileMetadata();
        long fileSize = metadata.getMetaData(fileUrl);

        if (fileSize == -1) {
            System.out.println("Download failed: Invalid content length or ranges not supported.");
            return;
        }

        long partSize = fileSize / numberOfThreads;

        String[] partFileNames = new String[numberOfThreads];

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {

            long startByte = i * partSize;
            long endByte;

            if (i == numberOfThreads - 1) endByte = fileSize - 1;
            else endByte = startByte + partSize - 1;

            partFileNames[i] = destinationPath + ".part" + i;

            DownloadTask task = new DownloadTask(fileUrl, partFileNames[i], startByte, endByte);
            executor.execute(task);
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        System.out.println("All parts downloaded. Starting merge...");

        mergeParts(partFileNames, destinationPath);

    }

    private void mergeParts(String[] partFiles, String finalPath) throws IOException {

        FileOutputStream finalOutputStream = null;

        try {
            finalOutputStream = new FileOutputStream(finalPath);

            for (int i = 0; i < partFiles.length; i++) {

                File partFile = new File(partFiles[i]);
                FileInputStream partInputStream = null;

                try {
                    partInputStream = new FileInputStream(partFile);

                    byte[] buffer = new byte[4096];

                    int bytesRead = partInputStream.read(buffer);

                    while (bytesRead != -1) {

                        finalOutputStream.write(buffer, 0, bytesRead);

                        bytesRead = partInputStream.read(buffer);

                    }

                } finally {

                    if (partInputStream != null) partInputStream.close();
                }

                partFile.delete();
                System.out.println("Merged and deleted: " + partFiles[i]);

            }
        } finally {
            if (finalOutputStream != null) finalOutputStream.close();
        }

        System.out.println("Merge Complete: " + finalPath);
    }

}

class DownloadTask implements Runnable {
    private String fileUrl;
    private String partFileName;
    private long startByte;
    private long endByte;

    public DownloadTask(String fileUrl, String partFileName, long startByte, long endByte) {
        this.fileUrl = fileUrl;
        this.partFileName = partFileName;
        this.startByte = startByte;
        this.endByte = endByte;
    }

    @Override
    public void run() {

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream partFileOutputStream = null;

        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();

            String range = "bytes=" + startByte + "-" + endByte;
            connection.setRequestProperty("Range", range);

            inputStream = connection.getInputStream();

            partFileOutputStream = new FileOutputStream(partFileName);

            byte[] buffer = new byte[4096];
            int bytesRead = inputStream.read(buffer);

            while (bytesRead != -1) {

                partFileOutputStream.write(buffer, 0, bytesRead);

                bytesRead = inputStream.read(buffer);
            }

            System.out.println("Downloaded: " + partFileName);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (partFileOutputStream != null) partFileOutputStream.close();
                if (inputStream != null) inputStream.close();
                if (connection != null) connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}