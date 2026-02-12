import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/***
 * @Author: Masood
 * @Date: 2026-02-12
 * @Description: Parallel Downloader class in java using RandomAccessFile
 */
public class ParallelDownloader {

    public void downloadInParallel(String fileUrl, String destinationPath, int numberOfThreads) throws Exception {

        FileMetadata metadata = new FileMetadata();
        long fileSize = metadata.getMetaData(fileUrl);

        if (fileSize == -1) {
            System.out.println("Download failed: Invalid content length or ranges not supported.");
            return;
        }

        long partSize = fileSize / numberOfThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {

            long startByte = i * partSize;
            long endByte;

            if (i == numberOfThreads - 1) endByte = fileSize - 1;
            else endByte = startByte + partSize - 1;

            DownloadTaskP task = new DownloadTaskP(fileUrl, destinationPath, startByte, endByte);

            executor.execute(task);
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        System.out.println("Parallel download complete.");
    }


}

class DownloadTaskP implements Runnable {

    private String fileUrl;
    private String destinationPath;
    private long startByte;
    private long endByte;

    public DownloadTaskP(String fileUrl, String destinationPath, long startByte, long endByte) {
        this.fileUrl = fileUrl;
        this.destinationPath = destinationPath;
        this.startByte = startByte;
        this.endByte = endByte;
    }

    @Override
    public void run() {

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        RandomAccessFile file = null;

        try {

            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();

            String range = "bytes=" + startByte + "-" + endByte;
            connection.setRequestProperty("Range", range);

            inputStream = connection.getInputStream();

            file = new RandomAccessFile(destinationPath, "rw");

            file.seek(startByte);

            byte[] buffer = new byte[4096];


            int bytesRead = inputStream.read(buffer);

            while (bytesRead != -1) {

                file.write(buffer, 0, bytesRead);

                bytesRead = inputStream.read(buffer);
            }

            System.out.println("Finished chunk: " + range);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {

                if (file != null) file.close();
                if (inputStream != null) inputStream.close();
                if (connection != null) connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}