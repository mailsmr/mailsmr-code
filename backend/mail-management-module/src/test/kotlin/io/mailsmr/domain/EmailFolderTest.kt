package io.mailsmr.domain

import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.DummySSLSocketFactory
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import com.icegreen.greenmail.util.ServerSetupTest
import io.mailsmr.infrastructure.protocols.IMAPProtocols
import io.mailsmr.infrastructure.protocols.SMTPProtocols
import io.mailsmr.util.AESDecryptionUtil
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.security.Security
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

// TODO Add tests for mailserver that do not support idle (Greenmail: https://github.com/greenmail-mail-test/greenmail/pull/382)

//@ContextConfiguration
@SpringBootTest
internal class EmailFolderTest {

    @TestConfiguration
    class TestConfig {

        @Bean
        fun threadPoolTaskScheduler(): ThreadPoolTaskScheduler {
            val threadPoolTaskScheduler = ThreadPoolTaskScheduler()
            threadPoolTaskScheduler.poolSize = 5
            threadPoolTaskScheduler.setThreadNamePrefix(
                "ThreadPoolTaskScheduler"
            )
            threadPoolTaskScheduler.setErrorHandler { error -> error.printStackTrace() }
            return threadPoolTaskScheduler
        }
    }

    var deterministicScheduler = ScheduledThreadPoolExecutor(2)

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

    @BeforeEach
    fun salami() {
//        deterministicScheduler = DeterministicScheduler()

//        deterministicScheduler.schedule(
//            { println("SCHEDULED SALAMI") },
//            3,
//            TimeUnit.SECONDS
//        )
//
//        // TODO figure out how to handle delays in test
//        deterministicScheduler.tick(3, TimeUnit.SECONDS)
//
//        deterministicScheduler = DeterministicScheduler()
    }

    @Test
    fun subscribe_shouldAddListenerThatGetsTriggeredOnNewMails() {

        // arrange
        val countDownLatch = CountDownLatch(1)
        val encryptionPassword = "secret"

        greenMail.setUser(
            "from@mailsmr-test.io",
            "from@mailsmr-test.io",
            "1234From"
        )
        greenMail.setUser("to@mailsmr-test.io", "to@mailsmr-test.io", "1234To")

        Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory::class.java.name)
        val emailAccountCredentials = EmailAccountConnectionProperties(
            EmailAccountConnectionProperties.IMAPConnectionProperties(
                IMAPProtocols.IMAP,
                greenMail.imap.bindTo,
                greenMail.imap.port,
                "to@mailsmr-test.io",
                "to@mailsmr-test.io",
                AESDecryptionUtil.encryptPassword("1234To", encryptionPassword)
            ),
            dummySMTPCredentials
        )


        val emailAccountManagementAggregate = EmailAccountManagementAggregate(emailAccountCredentials, deterministicScheduler, Duration.ofMillis(50))
        emailAccountManagementAggregate.connectImap(encryptionPassword)
        val folder = emailAccountManagementAggregate.getFolder("INBOX")
        folder.subscribe(object : EmailFolderChangeListener {
            override fun any(any: Any) {
                countDownLatch.countDown()
            }
        })

        // act

        val expectedMessageSubject = "subject"
        val expectedMessageBody = "body"
        GreenMailUtil.sendTextEmailTest(
            "to@mailsmr-test.io",
            "from@mailsmr-test.io",
            expectedMessageSubject,
            expectedMessageBody
        )

        assertNotEquals(0, folder.unreadMessageCount)

        greenMail.waitForIncomingEmail(1)

        // assert
        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))
    }
}
