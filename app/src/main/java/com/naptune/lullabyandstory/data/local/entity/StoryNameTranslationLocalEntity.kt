package com.naptune.lullabyandstory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "story_name_translation_table",
    foreignKeys = [
        ForeignKey(
            entity = StoryLocalEntity::class,
            parentColumns = ["document_id"],
            childColumns = ["story_document_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["story_document_id"])]
)
data class StoryNameTranslationLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "story_name_translation_id")
    val storyNameTranslationId: String,

    @ColumnInfo(name = "story_document_id")
    val storyDocumentId: String, // Foreign key to StoryLocalEntity

    @ColumnInfo(name = "story_id")
    val storyId: String, // Common ID for matching with remote data

    @ColumnInfo(name = "story_name_en")
    val storyNameEn: String,

    @ColumnInfo(name = "story_name_es")
    val storyNameEs: String,

    @ColumnInfo(name = "story_name_fr")
    val storyNameFr: String,

    @ColumnInfo(name = "story_name_de")
    val storyNameDe: String,

    @ColumnInfo(name = "story_name_pt")
    val storyNamePt: String,

    @ColumnInfo(name = "story_name_hi")
    val storyNameHi: String,

    @ColumnInfo(name = "story_name_ar")
    val storyNameAr: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)