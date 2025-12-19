package com.naptune.lullabyandstory.domain.usecase.lullaby

import com.naptune.lullabyandstory.domain.data.DownloadLullabyResult
import com.naptune.lullabyandstory.domain.model.LullabyDomainModel
import com.naptune.lullabyandstory.domain.repository.LullabyDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for downloading lullabies.
 * Updated to use LullabyDataRepository (ISP compliant).
 */
@Singleton
class DownloaLullabyUsecase @Inject constructor(
    private val lullabyDataRepository: LullabyDataRepository
) {
    suspend operator fun invoke(
        lullabyItem: LullabyDomainModel
    ): Flow<DownloadLullabyResult> {
        return lullabyDataRepository.downloadLullaby(lullabyItem)
    }
}