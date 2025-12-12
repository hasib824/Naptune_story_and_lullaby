package com.naptune.lullabyandstory.data.mapper

import com.naptune.lullabyandstory.data.local.entity.StoryLocalEntity
import com.naptune.lullabyandstory.data.model.StoryRemoteModel
import com.naptune.lullabyandstory.domain.model.StoryDomainModel

fun StoryRemoteModel.remoteToDomainModel(): StoryDomainModel {
    return StoryDomainModel(
        documentId = this.documentId,
        id = this.id,
        storyName = this.storyName,
        storyDescription = this.storyDescription,
        storyAudioPath = this.storyAudioPath,
        imagePath = this.imagePath,
        story_reading_time = this.story_reading_time,
        story_listen_time_in_millis = this.story_listen_time_in_millis,
        popularity_count = this.popularity_count, // Local data doesn't have this, default to 0
        isFree = this.isFree // Local data doesn't have this, default to false
    )
}


fun StoryRemoteModel.remoteToLocalEntity(): StoryLocalEntity {
    return StoryLocalEntity(
        documentId = this.documentId,
        id = this.id,
        storyName = this.storyName,
        storyDescription = this.storyDescription,
        storyAudioPath = this.storyAudioPath,
        imagePath = this.imagePath,
        story_reading_time = this.story_reading_time,
        popularity_count = this.popularity_count, // Local data doesn't have this, default to 0
        isFree = this.isFree, // Local data doesn't have this, default to false
        story_listen_time_in_millis = this.story_listen_time_in_millis,

    )
}

fun List<StoryRemoteModel>.remoteToLocalModelList(): List<StoryLocalEntity> {
    return this.map {
        it.remoteToLocalEntity()
    }
}

fun List<StoryRemoteModel>.remoteToDomainModelList(): List<StoryDomainModel> {
    return this.map { it ->
        it.remoteToDomainModel()
    }
}

fun StoryLocalEntity.localToDomainModel(): StoryDomainModel {
    return StoryDomainModel(
        documentId = this.documentId,
        id = this.id,
        storyName = this.storyName,
        storyDescription = this.storyDescription,
        storyAudioPath = this.storyAudioPath,
        imagePath = this.imagePath,
        story_reading_time = this.story_reading_time,
        story_listen_time_in_millis = this.story_listen_time_in_millis,
        isFavourite = this.isFavourite,
        popularity_count = this.popularity_count, // Local data doesn't have this, default to 0
        isFree = this.isFree // Local data doesn't have this, default to false
    )
}

fun List<StoryLocalEntity>.localToDomainModelList() : List<StoryDomainModel>
{
    return this.map {
        it.localToDomainModel()
    }
}