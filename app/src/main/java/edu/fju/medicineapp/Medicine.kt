package edu.fju.medicineapp

import android.os.Parcelable
import android.os.Parcel


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

//data class Medicines(
//    val medicationCode: String,
//    val genericName: String,
//    val chineseBrandName: String,
//    val englishBrandName: String,
//    val medicationMark: String,
//    val doseUnitCode: String,
//    val doseUnitChineseName: String,
//    val doseUnitEnglishName: String,
//    val imageAPath: String,
//    val imageBPath: String
//)



data class Medicines(
    val medicationCode: String, // 新增的欄位
    val genericName: String,
    val chineseBrandName: String,
    val englishBrandName: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "" // 讀取 medicineCode
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(medicationCode) // 寫入 medicineCode
        parcel.writeString(genericName)
        parcel.writeString(chineseBrandName)
        parcel.writeString(englishBrandName)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Medicines> {
        override fun createFromParcel(parcel: Parcel): Medicines {
            return Medicines(parcel)
        }

        override fun newArray(size: Int): Array<Medicines?> {
            return arrayOfNulls(size)
        }
    }
}

