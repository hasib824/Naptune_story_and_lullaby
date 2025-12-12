package com.naptune.lullabyandstory.data.mapper

import com.naptune.lullabyandstory.data.local.entity.TranslationLocalEntity
import com.naptune.lullabyandstory.data.model.TranslationRemoteModel
import com.naptune.lullabyandstory.domain.model.TranslationDomainModel

/**
 * Convert TranslationRemoteModel to TranslationLocalEntity
 */
fun TranslationRemoteModel.toEntity(lullabyDocumentId: String): TranslationLocalEntity {
    return TranslationLocalEntity(
        translationId = this.documentId,
        lullabyDocumentId = lullabyDocumentId,
        lullabyId = this.id,
        musicNameEn = this.musicNameEn,
        musicNameEs = this.musicNameEs,
        musicNameFr = this.musicNameFr,
        musicNameDe = this.musicNameDe,
        musicNamePt = this.musicNamePt,
        musicNameHi = this.musicNameHi,
        musicNameAr = this.musicNameAr
    )
}

/**
 * Convert List<TranslationRemoteModel> to List<TranslationLocalEntity>
 * with lullaby document ID mapping
 */
fun List<TranslationRemoteModel>.toEntityList(lullabyIdToDocumentMap: Map<String, String>): List<TranslationLocalEntity> {
    return this.mapNotNull { translation ->
        val lullabyDocumentId = lullabyIdToDocumentMap[translation.id]
        if (lullabyDocumentId != null) {
            translation.toEntity(lullabyDocumentId)
        } else {
            android.util.Log.w("TranslationMapper", "⚠️ No matching lullaby found for translation ID: ${translation.id}")
            null // Skip if no matching lullaby found
        }
    }
}

/**
 * Convert TranslationLocalEntity to TranslationDomainModel
 */
fun TranslationLocalEntity.toDomainModel(): TranslationDomainModel {
    return TranslationDomainModel(
        translationId = this.translationId,
        lullabyDocumentId = this.lullabyDocumentId,
        lullabyId = this.lullabyId,
        musicNameEn = this.musicNameEn,
        musicNameEs = this.musicNameEs,
        musicNameFr = this.musicNameFr,
        musicNameDe = this.musicNameDe,
        musicNamePt = this.musicNamePt,
        musicNameHi = this.musicNameHi,
        musicNameAr = this.musicNameAr
    )
}

/**
 * Convert List<TranslationLocalEntity> to List<TranslationDomainModel>
 */
fun List<TranslationLocalEntity>.toDomainModelList(): List<TranslationDomainModel> {
    return this.map { it.toDomainModel() }
}

/**
 * Convert TranslationDomainModel to TranslationLocalEntity
 */
fun TranslationDomainModel.toEntity(): TranslationLocalEntity {
    return TranslationLocalEntity(
        translationId = this.translationId,
        lullabyDocumentId = this.lullabyDocumentId,
        lullabyId = this.lullabyId,
        musicNameEn = this.musicNameEn,
        musicNameEs = this.musicNameEs,
        musicNameFr = this.musicNameFr,
        musicNameDe = this.musicNameDe,
        musicNamePt = this.musicNamePt,
        musicNameHi = this.musicNameHi,
        musicNameAr = this.musicNameAr
    )
}

/**
 * Convert TranslationRemoteModel to TranslationDomainModel directly
 */
fun TranslationRemoteModel.toDomainModel(lullabyDocumentId: String): TranslationDomainModel {
    return TranslationDomainModel(
        translationId = this.documentId,
        lullabyDocumentId = lullabyDocumentId,
        lullabyId = this.id,
        musicNameEn = this.musicNameEn,
        musicNameEs = this.musicNameEs,
        musicNameFr = this.musicNameFr,
        musicNameDe = this.musicNameDe,
        musicNamePt = this.musicNamePt,
        musicNameHi = this.musicNameHi,
        musicNameAr = this.musicNameAr
    )
}