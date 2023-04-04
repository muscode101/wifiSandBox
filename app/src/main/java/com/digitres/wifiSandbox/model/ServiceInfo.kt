package com.digitres.wifiSandbox.model

data class ServiceInfo(
    var attendantsNumber: Int = 0,
    var availabilitiesType: String = "",
    var categoryId: Any? = null,
    var currency: String = "",
    var description: String = "",
    var duration: Int = 0,
    var id: Int = 0,
    var location: String = "",
    var name: String = "",
    var price: Int = 0,
    var userId: Int = 0
)