package com.naptune.lullabyandstory.domain.usecase.translation

import com.naptune.lullabyandstory.data.repository.LullabyRepositoryImpl
import com.naptune.lullabyandstory.domain.model.TranslationDomainModel
import javax.inject.Inject

class GetTranslationByLullabyIdUseCase @Inject constructor(
    private val lullabyRepository: LullabyRepositoryImpl
) {

    suspend operator fun invoke(lullabyDocumentId: String): TranslationDomainModel? {
        return lullabyRepository.getTranslationByLullabyDocumentId(lullabyDocumentId)
    }
}