package com.aap.worldflags.data

/**
 * Flag data with weight. This is used to pick the questions.
 */
data class CountryFlagDataWithWeight(val flagData: FlagData, val common: Boolean, val weight: Int)