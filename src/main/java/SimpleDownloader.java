import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/***
 * @Author: Masood
 * @Date: 2026-02-11
 * @Description: Simple Downloader class in java
 */
public class SimpleDownloader {

    public void downloadFile(String fileURL, String outputPath) throws Exception {

        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {

            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(outputPath);

            byte[] buffer = new byte[4096];
            int bytesRead = inputStream.read(buffer);

            while (bytesRead != -1) {

                outputStream.write(buffer, 0, bytesRead);

                bytesRead = inputStream.read(buffer);

            }

            outputStream.close();
            inputStream.close();

            System.out.println("File downloaded successfully: " + outputPath);
        }

        else System.out.println("No file to download. Server replied HTTP code: " + responseCode);

        connection.disconnect();
    }

}






