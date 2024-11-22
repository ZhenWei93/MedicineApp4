package edu.fju.medicineapp

// 資料類別，用於解析 JSON 回應
data class MedicineDetailsResponseData(
    val result: Medicine
)
data class ResponseData(
    val status: String,
    val result: List<Medicines> // 確認result類行為List<Medicine>
)
