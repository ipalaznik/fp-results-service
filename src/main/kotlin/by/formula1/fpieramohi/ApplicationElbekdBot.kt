package by.formula1.fpieramohi

import by.formula1.fpieramohi.livetiming.mapCurrentResultsToText
import by.formula1.fpieramohi.livetiming.readTimingsFromStreaming
import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.Message

fun main() {
    val token = "995210316:AAFIzn6mH317Y_-MoV5S4rxN8bQdgNS6tuw"
    val bot = Bot.createPolling(token)

//    bot.chain("/gettimings") { message -> sendTimings(bot, message) }
//        .build()

    bot.onCommand("/instantresults") { message -> bot.sendMessage(message.first.chatId(), mapCurrentResultsToText() ) }

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

private fun Message.chatId() = this.chat.id.toChatId()
