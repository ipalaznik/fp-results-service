package by.formula1.fpieramohi

import by.formula1.fpieramohi.livetiming.*
import com.elbekd.bot.Bot
import com.elbekd.bot.model.ChatId
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.Message
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

val isPolling = AtomicBoolean(true)
var messageId: Long? = null
val streamingChannelId = ChatId.StringId("-1002030253232")

enum class Session(val text: String) {
    FP1("Практыка 1"),
    FP2("Практыка 2"),
    FP3("Практыка 3"),
    SPRINT("Спрынт"),
    Q1("Кваліфікацыя - 1-ы сегмент"),
    Q2("Кваліфікацыя - 2-і сегмент"),
    Q3("Кваліфікацыя - 3-і сегмент"),
    RACE("Гонка")
}

fun main() {
    val tokenOld = "995210316:AAFIzn6mH317Y_-MoV5S4rxN8bQdgNS6tuw"
    val tokenFPStream = "6982534249:AAHYj17iv2PQSc7pDg4WOYgb25EGcEgO21Y"
    val bot = Bot.createPolling(tokenFPStream)

//    bot.chain("/gettimings") { message -> sendTimings(bot, message) }
//        .build()

    bot.onCommand("/instantresults") { message -> bot.sendMessage(message.first.chatId(), mapCurrentResultsToText() ) }
    bot.onCommand("/race1") { message -> bot.sendMessage(message.first.chatId(), mapCurrentRaceResultsToText() ) }
    bot.onCommand("/race2") { message -> bot.sendMessage(message.first.chatId(), mapCurrentRaceResultsToText(false) ) }


    bot.onCommand("/race") { message ->
        GlobalScope.launch {
            subscribeToResults(bot, Session.RACE)
        }
    }

    bot.onCommand("/fp1") { message ->
        GlobalScope.launch {
            subscribeToResults(bot, Session.FP1)
        }
    }

    bot.onCommand("/fp2") { message ->
        GlobalScope.launch {
            subscribeToResults(bot, Session.FP2)
        }
    }

    bot.onCommand("/fp3") { message ->
        GlobalScope.launch {
            subscribeToResults(bot, Session.FP3)
        }
    }

    bot.onCommand("/Q1") { message ->
        GlobalScope.launch {
            subscribeToResults(bot, Session.Q1)
        }
    }

    bot.onCommand("/Q2") { message ->
        GlobalScope.launch {
            subscribeToResults(bot, Session.Q2)
        }
    }

    bot.onCommand("/Q3") { message ->
        GlobalScope.launch {
            subscribeToResults(bot, Session.Q3)
        }
    }

    bot.onCommand("/stop") { message ->
        isPolling.set(false)
    }

//    GlobalScope.launch {
//        while (isPolling.get()) {
//            println("update results")
//            manualUpdateForTestSake()
//            TimeUnit.MILLISECONDS.sleep(200)
//        }
//    }

//    bot.chain("/debug") { message -> bot.sendMessage(message.chatId(), "Дашлі эмодзі для дэбагу!") }
//        .then { message -> bot.sendMessage(message.chatId(), debug(message)) }
//        .build()
//
//    bot.chain("/emoji") { message ->
//        bot.sendMessage(
//            message.chatId(),
//            "1",
//            entities = listOf(MessageEntity(MessageEntity.Type.CUSTOM_EMOJI, 0, 2, customEmojiId = "5208706285255534951"))) }
////        .then { message -> bot.sendMessage(message.chat.id.toChatId(), debug(message)) }
//        .build()

    bot.start()

    readTimingsFromStreaming()
}

private suspend fun subscribeToResults(
    bot: Bot,
    session: Session
) {
    isPolling.set(true)
    while (isPolling.get()) {
        try {
            println("isUpdated = ${isUpdated.get()}")
            if (messageId == null) {
//                val timingMessage = bot.sendMessage(message.first.chatId(), mapCurrentRaceResultsToText())
                val timingMessage = bot.sendMessage(streamingChannelId, mapCurrentRaceResultsToText())
//                val timingMessage = bot.sendMessage(streamingChannelId, createSessionText(session))
                messageId = timingMessage.messageId
            } else {
                bot.editMessageText(streamingChannelId, messageId, text = createSessionText(session))
                isUpdated.set(false)
            }
        } finally {
            println("sleep 3 second")
            TimeUnit.SECONDS.sleep(3)
        }
    }
}

private fun createSessionText(session: Session) = """
    ${session.text}

${mapCurrentRaceResultsToText()}

Абноўлена: ${LocalDateTime.now().format(ISO_LOCAL_TIME)}""".trimIndent()

private fun Message.chatId() = this.chat.id.toChatId()
