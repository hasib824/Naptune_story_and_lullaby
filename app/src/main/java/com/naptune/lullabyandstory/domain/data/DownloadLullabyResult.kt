package com.naptune.lullabyandstory.domain.data

import okio.Path

sealed class DownloadLullabyResult {
    data class Progress(val id : String , val progressPercentige: Int) : DownloadLullabyResult()
    data class Completed(val documentId: String, val muusciLocalPath: String) : DownloadLullabyResult()
    data class Error(val id : String,val message: String) : DownloadLullabyResult()
}