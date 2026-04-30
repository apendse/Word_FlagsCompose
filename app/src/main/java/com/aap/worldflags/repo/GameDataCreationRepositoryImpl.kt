package com.aap.worldflags.repo

import android.content.Context
import com.aap.worldflags.data.CountryType
import com.aap.worldflags.data.FlagData
import com.google.gson.Gson
import com.google.gson.stream.JsonToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

private const val COUNTRY_ASSET = "countries.json"
private const val COUNTRY_PROPERTIES = "country_properties.json"
private const val CONFUSING_COUNTRY_FLAG_LIST = "flags_often_confused.json"

class GameDataCreationRepositoryImpl @Inject constructor(private val appContext: Context,): GameDataCreationRepository {

    private lateinit var countryFlagListImportant: List<FlagData>
    private lateinit var countryFlagListLessKnown: List<FlagData>
    private lateinit var countryFlagListCombined: List<FlagData>
    private lateinit var countryCodeToFlagDataCombinedMap: Map<String, FlagData>
    private lateinit var countryToConfusingCountriesMap: Map<String, List<FlagData>>

    override suspend fun readAssets() {
        withContext(Dispatchers.IO) {
            val gson = Gson()
            val countryCodeToFlagDataMap = readCountries(appContext, gson, COUNTRY_ASSET)

            val (highBucket, _) = readCountryData(appContext, gson, COUNTRY_PROPERTIES)


            if (FlagImages.images.size != countryCodeToFlagDataMap.size) {
                throw RuntimeException("Data is corrupt! countries ${countryCodeToFlagDataMap.size} flags ${FlagImages.images.size}")
            }
            // There are some countries which are classified as imporatant. There are some countries classified
            // as less important. e.g British Indian Ocean Territory. This is a territory officially owned by the UK but it has its own
            // flag and it is somewhat autonomous. 
            val countryFlagListTempCombined = mutableListOf<FlagData>()
            val countryFlagListTempImportant = mutableListOf<FlagData>()
            val countryFlagListTempLessImportant = mutableListOf<FlagData>()
            val countryCodeToFlagDataCombinedMapTemp = mutableMapOf<String, FlagData>()
            FlagImages.images.forEach { code, drawable ->
                val flagDataWithoutDrawable = countryCodeToFlagDataMap[code] ?: throw RuntimeException("Country code $code missing")
                val flagData = flagDataWithoutDrawable.copy(flagDrawable = drawable)
                countryFlagListTempCombined.add(flagData)

                if (highBucket.containsKey(code)) {
                    countryFlagListTempImportant.add(flagData)
                } else {
                    countryFlagListTempLessImportant.add(flagData)
                }
            }
            countryFlagListCombined = countryFlagListTempCombined
            countryFlagListTempCombined.forEach {
                countryCodeToFlagDataCombinedMapTemp[it.countryCode] = it
            }
            countryCodeToFlagDataCombinedMap = countryCodeToFlagDataCombinedMapTemp
            countryFlagListImportant = countryFlagListTempImportant
            countryFlagListLessKnown = countryFlagListTempLessImportant
            val countriesWithConfusingFlags = getCountriesWithConfusingFlags(appContext, gson, CONFUSING_COUNTRY_FLAG_LIST)
            val countryToConfusingCountriesMapTemp = mutableMapOf<String, List<FlagData>>()
            countriesWithConfusingFlags.forEach { list ->
                list.forEach{countryCode ->
                    // flag data for the given country
                    val countryCode = countryCodeToFlagDataCombinedMap[countryCode]!!.countryCode
                    // get the country list without the given country, convert it to FlagData
                    val listWithoutTheGivenCountry = list.filter { it != countryCode }.map {countryCodeToFlagDataCombinedMap[it]!! }
                    // create a map entry country name -> FlagData of confusing country
                    countryToConfusingCountriesMapTemp[countryCode] =  listWithoutTheGivenCountry
                }
            }
            countryToConfusingCountriesMap = countryToConfusingCountriesMapTemp
        }

    }

    /**
     * Reads the lists of countries whose flags are similar. They're likely to confuse the players.
     * e.g. Malaysia and Liberia have similar flags.
     *      New Zealand and Australia have similar flags.
     *      Iceland and Denmark have similar flags.
     */
    private fun getCountriesWithConfusingFlags(appContext: Context, gson: Gson, asset: String): List<List<String>> {
        val bufferedReader = BufferedReader(InputStreamReader(appContext.assets.open(asset)))
        val jsonReader = gson.newJsonReader(bufferedReader)
        jsonReader.beginObject()
        val list = mutableListOf<MutableList<String>>()
        while(jsonReader.hasNext()) {
            val token = jsonReader.peek()
            if (token == JsonToken.NAME) {
                jsonReader.nextName()
            }

            if (token == JsonToken.BEGIN_ARRAY) {
                jsonReader.beginArray()
                var doWork = true
                while(doWork) {
                    var token1 = jsonReader.peek()
                    if (token1 == JsonToken.BEGIN_ARRAY) {
                        jsonReader.beginArray()
                        val currentList = mutableListOf<String>()
                        var tokenArray = jsonReader.peek()
                        while(tokenArray == JsonToken.STRING) {
                            currentList.add(jsonReader.nextString())
                            tokenArray = jsonReader.peek()
                        }
                        if (tokenArray == JsonToken.END_ARRAY) {
                            list.add(currentList)
                            jsonReader.endArray()
                        }
                    } else if (token1 == JsonToken.END_ARRAY) {
                        doWork = false
                    }

                }
            }
            if (token == JsonToken.END_ARRAY) {
                jsonReader.endArray()
            }
            if (token == JsonToken.END_OBJECT) {
                jsonReader.endObject()
                break
            }
        }
        return list
    }

    private fun readCountries(appContext: Context, gson: Gson, asset: String): Map<String, FlagData> {

        val bufferedReader = BufferedReader(InputStreamReader(appContext.assets.open(asset)))
        val jsonReader = gson.newJsonReader(bufferedReader)

        val countryCodeToNameMap: MutableMap<String, FlagData> = mutableMapOf()
        jsonReader.beginObject()
        while(jsonReader.hasNext()) {
            val token = jsonReader.peek()
            var countryCode: String
            var countryName: String
            if (token == JsonToken.NAME) {
                countryCode = jsonReader.nextName()
                countryName = jsonReader.nextString()
                countryCodeToNameMap[countryCode] = FlagData(countryCode, countryName, 0)
            }
            if (token == JsonToken.END_OBJECT) {
                jsonReader.endObject()
                break
            }
        }
        return countryCodeToNameMap
    }

    // Reads the country code and its importance.
    private fun readCountryData(appContext: Context, gson: Gson, asset: String): Pair<Map<String, Int>, Map<String, Int>> {
        val bufferedReader = BufferedReader(InputStreamReader(appContext.assets.open(asset)))
        val jsonReader = gson.newJsonReader(bufferedReader)
        val importantMap = mutableMapOf<String, Int>()
        val lessImportantMap = mutableMapOf<String, Int>()
        jsonReader.beginObject()
        while(jsonReader.hasNext()) {
            val token = jsonReader.peek()
            var countryCode: String
            var value: Int
            if (token == JsonToken.NAME) {
                countryCode = jsonReader.nextName()
                value = jsonReader.nextInt()
                if (value == 5) {
                    importantMap[countryCode] = value
                } else {
                    lessImportantMap[countryCode] = value
                }
            }
            if (token == JsonToken.END_OBJECT) {
                jsonReader.endObject()
                break
            }
        }
        return Pair(importantMap, lessImportantMap)
    }

    override fun getConfusingAnswers(countryCode: String): List<FlagData> {
        return countryToConfusingCountriesMap[countryCode] ?: emptyList()
    }

    override fun getFlags(countryType: CountryType): List<FlagData> {
        return when(countryType) {
            CountryType.COUNTRY_FAMOUS -> countryFlagListImportant
            CountryType.COUNTRY_NOT_FAMOUS -> countryFlagListLessKnown
            CountryType.COUNTRY_FULL_LIST -> countryFlagListCombined
        }
    }

}