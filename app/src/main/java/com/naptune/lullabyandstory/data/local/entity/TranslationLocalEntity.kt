package com.naptune.lullabyandstory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "translation_table",
    foreignKeys = [
        ForeignKey(
            entity = LullabyLocalEntity::class,
            parentColumns = ["document_id"],
            childColumns = ["lullaby_document_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lullaby_document_id"])]
)
data class TranslationLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "translation_id")
    val translationId: String,

    @ColumnInfo(name = "lullaby_document_id")
    val lullabyDocumentId: String, // Foreign key to LullabyLocalEntity

    @ColumnInfo(name = "lullaby_id")
    val lullabyId: String, // Common ID for matching with remote data

    @ColumnInfo(name = "music_name_en")
    val musicNameEn: String,

    @ColumnInfo(name = "music_name_es")
    val musicNameEs: String,

    @ColumnInfo(name = "music_name_fr")
    val musicNameFr: String,

    @ColumnInfo(name = "music_name_de")
    val musicNameDe: String,

    @ColumnInfo(name = "music_name_pt")
    val musicNamePt: String,

    @ColumnInfo(name = "music_name_hi")
    val musicNameHi: String,

    @ColumnInfo(name = "music_name_ar")
    val musicNameAr: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)