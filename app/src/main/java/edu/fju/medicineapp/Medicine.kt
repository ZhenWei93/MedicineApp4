package edu.fju.medicineapp

data class Medicine(
    val genericName: String,
    val chineseBrandName: String,
    val englishBrandName: String,
    val appearance: String,
    val dosage: String,
    val purpose: String,
    val storageMethod: String,
    val sideEffect: String,
    val notice: String,
    val pregnancyBreastFeedingChildNotice: String,
    val imageAPath: String,
    val imageBPath: String
)

data class Medicines(
    val medicationCode: String,
    val genericName: String,
    val chineseBrandName: String,
    val englishBrandName: String,
    val medicationMark: String,
    val doseUnitCode: String,
    val doseUnitChineseName: String,
    val doseUnitEnglishName: String,
    val imageAPath: String,
    val imageBPath: String
)