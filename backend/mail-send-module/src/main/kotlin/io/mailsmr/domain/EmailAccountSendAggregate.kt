package io.mailsmr.domain

// TODO
class EmailAccountSendAggregate(
) {


//    private var _smtpEmailAccount: SMTPEmailAccount? = null
//    private var smtpEmailAccount: SMTPEmailAccount
//        get() {
//            if (_smtpEmailAccount == null) {
//                throw NoConnectionEstablishedException("An SMTP connection needs to be opened with the decryption password before accessing the account.")
//            }
//            return _smtpEmailAccount!!
//        }
//        private set(value) {
//            _smtpEmailAccount = value
//        }


    fun connectSmtp(decryptionPassword: String) {
        TODO()
    }

    fun sendMessage() {
        TODO()
    }
}
