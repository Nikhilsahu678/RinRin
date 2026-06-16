package com.rinrin.app.data.remote

import com.squareup.moshi.Json

data class ImgbbResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: ImgbbData?
)

data class ImgbbData(
    @Json(name = "display_url") val displayURL: String
)
