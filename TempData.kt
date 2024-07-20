package com.temp.lifestylegps



data class TempData(
    val imei: String,
    val params : param,
    val name: String,

)

data class param(
     val temp1 : Double
)
