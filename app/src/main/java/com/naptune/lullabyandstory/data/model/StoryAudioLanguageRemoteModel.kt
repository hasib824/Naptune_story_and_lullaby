package com.naptune.lullabyandstory.data.model

/**
 * ✅ StoryAudioLanguage Remote Model
 * Represents multilingual audio paths for stories from Appwrite
 * Collection ID: story_audio_path_collection_id
 *
 * Foreign Key Relationship: documentId = StoryRemoteModel.documentId
 * No mapping needed - direct document_id relationship! 🎯
 */
data class StoryAudioLanguageRemoteModel(
    val documentId: String = "",        // Primary key AND foreign key to Story.documentId
    val audioPathEn: String = "",       // English audio path
    val audioPathEs: String = "",       // Spanish audio path
    val audioPathFr: String = "",       // French audio path
    val audioPathDe: String = "",       // German audio path
    val audioPathPt: String = "",       // Portuguese audio path
    val audioPathHi: String = "",       // Hindi audio path
    val audioPathAr: String = ""        // Arabic audio path
)