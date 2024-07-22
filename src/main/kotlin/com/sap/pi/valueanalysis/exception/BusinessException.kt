package com.sap.pi.valueanalysis.exception

import io.ktor.http.*

class BusinessException : RuntimeException {

    var code: HttpStatusCode = HttpStatusCode.BadRequest

    constructor(message: String) : super(message)

    constructor(code: HttpStatusCode, message: String) : super(message) {
        this.code = code
    }
}
