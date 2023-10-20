package by.formula1.fpieramohi

import by.formula1.fpieramohi.livetiming.sendTimingsFromStreaming
import com.elbekd.bot.Bot
import com.elbekd.bot.api.TelegramApi
import com.elbekd.bot.feature.chain.chain
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.Message
import com.elbekd.bot.types.MessageEntity

fun main() {
    val token = "995210316:AAFIzn6mH317Y_-MoV5S4rxN8bQdgNS6tuw"
    val bot = Bot.createPolling(token)

//    bot.chain("/gettimings") { message -> sendTimings(bot, message) }
//        .build()

    bot.chain("/instantresults") { message -> sendTimingsFromStreaming(bot, message) }
        .build()

    bot.chain("/practise 1") { message -> sendTimingsFromStreaming(bot, message) }
        .build()

    bot.chain("/debug") { message -> bot.sendMessage(message.chat.id.toChatId(), "Дашлі эмодзі для дэбагу!") }
        .then { message -> bot.sendMessage(message.chat.id.toChatId(), debug(message)) }
        .build()

    bot.chain("/emoji") { message ->
        bot.sendMessage(message.chat.id.toChatId(),
            "1",
            entities = listOf(MessageEntity(MessageEntity.Type.CUSTOM_EMOJI, 0, 2, customEmojiId = "5208706285255534951"))) }
//        .then { message -> bot.sendMessage(message.chat.id.toChatId(), debug(message)) }
        .build()

    bot.start()

//    subscribeToF1StreamingData()
}

private fun debug(message: Message) = message.text ?: "Вы даслалі нічога"

//private suspend fun sendTimings(bot: Bot, message: Message) {
//    bot.sendMessage(
//        message.chat.id.toChatId(),
//        findLatestResultRows()?.mapToMessageString() ?: "No results selected from DB, I'm sorry!"
//    )
//}
