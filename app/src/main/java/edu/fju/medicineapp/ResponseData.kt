package edu.fju.medicineapp

// 資料類別，用於解析 JSON 回應
data class MedicineDetailsResponseData(
    val result: Medicine
)
data class ResponseData(
    val status: String,
    val result: List<Medicines> // 确认result类型为List<Medicine>
)
