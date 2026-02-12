package com.masood

import FileMetadata
import ParallelDownloaderMerge
import SimpleDownloader
import java.io.File
import java.util.*


fun main() {

    val NUM_THREADS:Int = 8;

    val url = "http://localhost:8080/file.exe" //480 mb file for testing

    val date = Date()
    val time = date.toLocaleString().replace(" ","-").replace(",","").replace(":","-")

    val outputFile = File("D:/Learning/jetbrains/Integrating reporting SDK into web frontends/server/downloads/")
    val outputFilePathS = outputFile.absolutePath  +"/downloaded-file-" + time +"S.exe";
    val outputFilePathP = outputFile.absolutePath  +"/downloaded-file-" + time +"P.exe";


    val fileMetadata = FileMetadata();
    val simpleDownloader = SimpleDownloader();
    val mergeParallelDownloader = ParallelDownloaderMerge();

    fileMetadata.getMetaData(url);


    val startTimeS = System.currentTimeMillis()

    simpleDownloader.downloadFile(url, outputFilePathS);


    val endTimeS = System.currentTimeMillis()


    val startTimeP = System.currentTimeMillis()

    mergeParallelDownloader.downloadInParallel(url,outputFilePathP,NUM_THREADS);

    val endTimeP = System.currentTimeMillis()

    val durationS: Long = endTimeS - startTimeS
    val durationP: Long = endTimeP - startTimeP

    println(">> Simple Download finished in " + durationS + " ms (" + (durationS / 1000.0) + " seconds)")
    println(">> Parallel Download finished in " + durationP + " ms (" + (durationP / 1000.0) + " seconds)")







}


