package com.aap.worldflags.repo

import com.aap.worldflags.data.CountryType
import com.aap.worldflags.data.FlagData

/*
    Static game data
 */
interface GameDataCreationRepository {
    suspend fun readAssets()
    fun getFlags(countryType: CountryType): List<FlagData>
    fun getConfusingAnswers(countryCode: String): List<FlagData>
}

