package com.sap.pi.valueanalysis.util

import com.sap.pi.valueanalysis.api.UserInformation

data class RequestContext(
    val user: UserInformation,
    val authToken: String,
)
