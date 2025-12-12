package com.naptune.lullabyandstory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ✅ StoryAudioLanguage Local Entity
 * Stores multilingual audio paths for stories in Room database
 *
 * Foreign Key Relationship: storyDocumentId -> StoryLocalEntity.documentId
 * Direct document_id relationship - no mapping needed! 🎯
 */
@Entity(
    tableName = "story_audio_language_table",
    foreignKeys = [
        ForeignKey(
            entity = StoryLocalEntity::class,
            parentColumns = ["document_id"],
            childColumns = ["story_document_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["story_document_id"]) // ✅ Index for JOIN performance
    ]
)
data class StoryAudioLanguageLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "story_audio_language_id")
    val storyAudioLanguageId: String,

    @ColumnInfo(name = "story_document_id")
    val storyDocumentId: String,        // Foreign key to Story.documentId

    @ColumnInfo(name = "audio_path_en")
    val audioPathEn: String,

    @ColumnInfo(name = "audio_path_es")
    val audioPathEs: String,

    @ColumnInfo(name = "audio_path_fr")
    val audioPathFr: String,

    @ColumnInfo(name = "audio_path_de")
    val audioPathDe: String,

    @ColumnInfo(name = "audio_path_pt")
    val audioPathPt: String,

    @ColumnInfo(name = "audio_path_hi")
    val audioPathHi: String,

    @ColumnInfo(name = "audio_path_ar")
    val audioPathAr: String
)