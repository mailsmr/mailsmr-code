package io.mailsmr.interfaces.ws.dtos

// TODO refine
data class WebSocketEmailsResponseDto(
    var accountId: Long = 0,
    var folderName: String? = null,
//    var emailEnvelopes: List<EmailEnvelope>? = null TODO
    var type: WebSocketEmailsResponseType? = null,
)
