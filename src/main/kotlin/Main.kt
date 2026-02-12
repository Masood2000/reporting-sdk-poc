package com.masood

import FileMetadata
import ParallelDownloader
import ParallelDownloaderCoroutines
import ParallelDownloaderMerge
import SimpleDownloader
import java.io.File
import java.util.*

/***
 * @Author: Masood
 * @Date: 2026-02-12
 */
suspend fun main(args: Array<String>) {

    val NUM_THREADS = 8
    val url = "http://localhost:8080/my-local-file.txt"

    val outputDirectoryPath = if (args.isNotEmpty()) {
        args[0]
    } else {
        "D:/Learning/jetbrains/Integrating reporting SDK into web frontends/server/downloads/"
    }

    val downloaderType = if (args.size >= 2) {
        args[1].lowercase()
    } else {
        "seek"   // default downloader
    }

    val date = Date()
    val time = date.toLocaleString()
        .replace(" ", "-")
        .replace(",", "")
        .replace(":", "-")

    val outputFile = File(outputDirectoryPath)
    outputFile.mkdirs() // ensure directory exists

    val outputFilePath =
        outputFile.absolutePath + "/downloaded-file-$time.txt"

    val fileMetadata = FileMetadata()

    println("--- FETCHING METADATA ---")
    fileMetadata.getMetaData(url)
    println("--------------------------\n")

    val start = System.currentTimeMillis()

    when (downloaderType) {
        "simple" -> {
            println("[*] Using Simple Downloader")
            SimpleDownloader().downloadFile(url, outputFilePath)
        }

        "seek" -> {
            println("[*] Using Parallel Downloader (Seek Strategy)")
            ParallelDownloader().downloadInParallel(url, outputFilePath, NUM_THREADS)
        }

        "merge" -> {
            println("[*] Using Parallel Downloader (Merge Strategy)")
            ParallelDownloaderMerge().downloadInParallel(url, outputFilePath, NUM_THREADS)
        }

        "coroutines" -> {
            println("[*] Using Parallel Downloader (Coroutines)")
            ParallelDownloaderCoroutines().downloadInParallel(url, outputFilePath, NUM_THREADS)
        }

        else -> {
            println(" Invalid downloader type!")
            println("Available options: simple | seek | merge | coroutines")
            return
        }
    }

    val end = System.currentTimeMillis()

    println("--------------------------------------------------")
    println("Download completed using '$downloaderType'")
    println("Finished in ${end - start} ms")
}
