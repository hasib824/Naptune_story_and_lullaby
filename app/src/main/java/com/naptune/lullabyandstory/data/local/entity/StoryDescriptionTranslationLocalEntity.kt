package com.naptune.lullabyandstory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "story_description_translation_table",
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
data class StoryDescriptionTranslationLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "story_description_translation_id")
    val storyDescriptionTranslationId: String,

    @ColumnInfo(name = "story_document_id")
    val storyDocumentId: String, // Foreign key to StoryLocalEntity

    @ColumnInfo(name = "story_id")
    val storyId: String, // Common ID for matching with remote data

    @ColumnInfo(name = "story_description_en")
    val storyDescriptionEn: String,

    @ColumnInfo(name = "story_description_es")
    val storyDescriptionEs: String,

    @ColumnInfo(name = "story_description_fr")
    val storyDescriptionFr: String,

    @ColumnInfo(name = "story_description_de")
    val storyDescriptionDe: String,

    @ColumnInfo(name = "story_description_pt")
    val storyDescriptionPt: String,

    @ColumnInfo(name = "story_description_hi")
    val storyDescriptionHi: String,

    @ColumnInfo(name = "story_description_ar")
    val storyDescriptionAr: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)