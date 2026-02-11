package com.masood

import FileMetadata
import java.io.File

fun main() {


    var url = "http://localhost:8080/abc.txt"
    val fileMetadata = FileMetadata();


    fileMetadata.getMetaData(url);


}