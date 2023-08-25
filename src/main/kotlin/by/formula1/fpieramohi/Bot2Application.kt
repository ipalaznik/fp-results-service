package by.formula1.fpieramohi

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId

fun main() {
    val bot = bot {
        token = "YOUR_API_KEY"
        dispatch {
            command("start") {
                bot.getStickerSet("")
                val result = bot.sendMessage(chatId = ChatId.fromId(message.chat.id),
                    text = "Hi there!")
                result.fold({
                    // do something here with the response
                },{
                    // do something with the error
                })
            }
        }
    }
    bot.startPolling()
}
