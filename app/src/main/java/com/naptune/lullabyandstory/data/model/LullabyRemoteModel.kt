package com.naptune.lullabyandstory.data.model

data class LullabyRemoteModel(
    val documentId: String = "",
    val id: String = "",
    val musicName: String = "",
    val musicPath: String = "",
    val musicSize: String = "",
    val imagePath: String = "",
    val musicLength: String = "",
    val popularity_count: Long = 0,
    val isFree: Boolean = false,
)
