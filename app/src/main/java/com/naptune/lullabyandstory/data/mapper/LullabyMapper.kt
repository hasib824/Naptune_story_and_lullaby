package com.naptune.lullabyandstory.data.mapper

import com.naptune.lullabyandstory.data.local.entity.LullabyLocalEntity
import com.naptune.lullabyandstory.data.model.LullabyRemoteModel
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel

// Appwrite Model to Room Entity
fun LullabyRemoteModel.toEntity(): LullabyLocalEntity {
    return LullabyLocalEntity(
        documentId = this.documentId,
        id = this.id,
        musicName = this.musicName,
        musicPath = this.musicPath,
        musicSize = this.musicSize,
        imagePath = this.imagePath,
        musicLength = this.musicLength,
        isDownloaded = false,
        isFavourite = false,
        musicLocalPath = null,
        popularity_count = this.popularity_count,
        isFree = this.isFree
    )
}

// Appwrite Model to Domain Model
fun LullabyRemoteModel.toDomainModel(): LullabyDomainModel {
    return LullabyDomainModel(
        documentId = this.documentId,
        id = this.id,
        musicName = this.musicName,
        musicPath = this.musicPath,
        musicSize = this.musicSize,
        imagePath = this.imagePath,
        musicLength = this.musicLength,
        isDownloaded = false,
        isFavourite = false,
        musicLocalPath = null,
        popularity_count = this.popularity_count,
        isFree = this.isFree
    )
}

// Domain Model to Room Entity
fun LullabyDomainModel.toEntity(): LullabyLocalEntity {
    return LullabyLocalEntity(
        documentId = this.documentId,
        id = this.id,
        musicName = this.musicName,
        musicPath = this.musicPath,
        musicSize = this.musicSize,
        imagePath = this.imagePath,
        musicLength = this.musicLength,
        isDownloaded = this.isDownloaded,
        isFavourite = this.isFavourite,
        musicLocalPath = null,
        popularity_count = this.popularity_count,
        isFree = this.isFree
    )
}

// Room Entity to Domain Model
fun LullabyLocalEntity.toDomainModel(): LullabyDomainModel {
    return LullabyDomainModel(
        documentId = this.documentId,
        id = this.id,
        musicName = this.musicName,
        musicPath = this.musicPath,
        musicSize = this.musicSize,
        imagePath = this.imagePath,
        musicLength = this.musicLength,
        isDownloaded = this.isDownloaded,
        isFavourite = this.isFavourite,
        musicLocalPath = this.musicLocalPath,
        popularity_count = this.popularity_count, // Local data doesn't have this, default to 0
        isFree = this.isFree // Local data doesn't have this, default to false
    )
}

// List mapping functions
fun List<LullabyRemoteModel>.toDomainModelList(): List<LullabyDomainModel> {
    return this.map { it.toDomainModel() }
}

fun List<LullabyRemoteModel>.toEntityList(): List<LullabyLocalEntity> {
    return this.map { it.toEntity() }
}

fun List<LullabyLocalEntity>.localToDomainModelList(): List<LullabyDomainModel> {
    return this.map { it.toDomainModel() }
}
