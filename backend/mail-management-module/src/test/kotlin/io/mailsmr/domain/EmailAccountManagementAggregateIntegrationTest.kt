package io.mailsmr.domain

import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.DummySSLSocketFactory
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import com.icegreen.greenmail.util.ServerSetupTest
import io.mailsmr.infrastructure.protocols.IMAPProtocols
import io.mailsmr.infrastructure.protocols.SMTPProtocols
import io.mailsmr.util.AESDecryptionUtil.encryptPassword
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.test.context.ContextConfiguration
import java.security.Security


@ContextConfiguration
internal class EmailAccountManagementAggregateIntegrationTest {
    private val threadPoolTaskScheduler: ThreadPoolTaskScheduler = ThreadPoolTaskScheduler()

    init {
        threadPoolTaskScheduler.initialize()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val greenMail: GreenMailExtension = GreenMailExtension(
            ServerSetup.verbose(
                arrayOf(
                    ServerSetupTest.SMTP, ServerSetupTest.IMAP,
                    ServerSetupTest.SMTPS, ServerSetupTest.IMAPS
                )
            )
        )

        val dummyIMAPCredentials = EmailAccountConnectionProperties.IMAPConnectionProperties(
            IMAPProtocols.IMAP,
            "",
            0,
            "",
            "",
            ""
        )

        val dummySMTPCredentials = EmailAccountConnectionProperties.SMTPConnectionProperties(
            SMTPProtocols.SMTP,
            "",
            0,
            "",
            "",
            ""
        )
    }

    @Test
    fun receivingAnEmailShouldBePossibleThroughAnEmailAccountAggregate_imap() {
        // arrange
        val encryptionPassword = "secret"

        greenMail.setUser("from@mailsmr-test.io", "from@mailsmr-test.io", "1234From")
        greenMail.setUser("to@mailsmr-test.io", "to@mailsmr-test.io", "1234To")

        val expectedMessageSubject = "subject"
        val expectedMessageBody = "body"
        GreenMailUtil.sendTextEmailTest(
            "to@mailsmr-test.io",
            "from@mailsmr-test.io",
            expectedMessageSubject,
            expectedMessageBody
        )

        val emailAccountCredentials = EmailAccountConnectionProperties(
            EmailAccountConnectionProperties.IMAPConnectionProperties(
                IMAPProtocols.IMAP,
                greenMail.imap.bindTo,
                greenMail.imap.port,
                "to@mailsmr-test.io",
                "to@mailsmr-test.io",
                encryptPassword("1234To", encryptionPassword)
            ),
            dummySMTPCredentials
        )

        // act
        val emailAccountManagementAggregate = EmailAccountManagementAggregate(emailAccountCredentials, threadPoolTaskScheduler.scheduledThreadPoolExecutor)
        emailAccountManagementAggregate.connectImap(encryptionPassword)
        val folder = emailAccountManagementAggregate.getFolders()[0]
        val messages = folder.getMessages()

        // assert
        val numberOfMessages = messages.size
        assertEquals(1, numberOfMessages)

        val receivedMessageSubject = messages[0].getSubject()
        assertEquals(expectedMessageSubject, receivedMessageSubject)

        val receivedMessageBody = messages[0].getContent()
        assertEquals(expectedMessageBody, receivedMessageBody)
    }

    @Test
    fun receivingAnEmailShouldBePossibleThroughAnEmailAccountAggregate_imaps() {
        Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory::class.java.name)

        // arrange
        val encryptionPassword = "secret"

        greenMail.setUser("from@mailsmr-test.io", "from@mailsmr-test.io", "1234From")
        greenMail.setUser("to@mailsmr-test.io", "to@mailsmr-test.io", "1234To")

        val expectedMessageSubject = "subject"
        val expectedMessageBody = "body"
        GreenMailUtil.sendTextEmailTest(
            "to@mailsmr-test.io",
            "from@mailsmr-test.io",
            expectedMessageSubject,
            expectedMessageBody
        )

        val emailAccountCredentials = EmailAccountConnectionProperties(
            EmailAccountConnectionProperties.IMAPConnectionProperties(
                IMAPProtocols.IMAPS,
                greenMail.imaps.bindTo,
                greenMail.imaps.port,
                "to@mailsmr-test.io",
                "to@mailsmr-test.io",
                encryptPassword("1234To", encryptionPassword)
            ),
            dummySMTPCredentials
        )

        // act
        val emailAccountManagementAggregate = EmailAccountManagementAggregate(emailAccountCredentials, threadPoolTaskScheduler.scheduledThreadPoolExecutor)
        emailAccountManagementAggregate.connectImap(encryptionPassword)
        val folder = emailAccountManagementAggregate.getFolders()[0]
        val messages = folder.getMessages()

        // assert
        val numberOfMessages = messages.size
        assertEquals(1, numberOfMessages)

        val receivedMessageSubject = messages[0].getSubject()
        assertEquals(expectedMessageSubject, receivedMessageSubject)

        val receivedMessageBody = messages[0].getContent()
        assertEquals(expectedMessageBody, receivedMessageBody)
    }
}
