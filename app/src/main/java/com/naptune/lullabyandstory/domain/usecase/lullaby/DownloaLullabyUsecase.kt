package com.naptune.lullabyandstory.domain.usecase.lullaby

import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.repository.LullabyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloaLullabyUsecase @Inject constructor(
    private val lullabyRepository: LullabyRepository
) {
    suspend operator fun invoke(
        lullabyItem: LullabyDomainModel
    ): Flow<DownloadLullabyResult> {
        return lullabyRepository.downloadLullaby(lullabyItem)
    }
}