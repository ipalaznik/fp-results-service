package by.formula1.fpieramohi

import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selectors.byText
import com.codeborne.selenide.Selenide.*
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeOptions
import java.util.concurrent.TimeUnit
import org.litote.kmongo.*
import java.time.ZonedDateTime
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

data class Tweet(val timestamp: ZonedDateTime, val message: String, val mediaLink: List<String>, val account: String)

class ApplicationElbekdBotKtTest {

    @Test
    fun test1() {
        val options = ChromeOptions()
//            .setHeadless(true)
            .addArguments("--lang=en_US")
        Configuration.browserCapabilities = options
        open("https://twitter.com/home")
        `$`("input[autocomplete=\"username\"]").value = "F1Pieramohi"
        `$`(byText("Next")).click()

        TimeUnit.SECONDS.sleep(1)
        `$`("input[autocomplete=\"current-password\"]").value = "wBG#dCPUB5hcprYoMPg*PX8hD6qeHnSuqhw3ErfJVCqXQaTWTMT*2&\$"

        `$`(byText("Log in")).click()
        TimeUnit.SECONDS.sleep(1)
        `$`(byText("Accept all cookies")).click()

        TimeUnit.SECONDS.sleep(1)
        `$`(By.xpath("//span[.='Following']")).click()

        TimeUnit.SECONDS.sleep(1)
        val newTweets = `$`(By.xpath("//span[.='Tweeted']"))
        if (newTweets.isDisplayed) {
            newTweets.click()
            TimeUnit.SECONDS.sleep(1)
        }

        val tweets = `$$`("div[data-testid=\"tweetText\"]")
        println("Test")
        println(tweets.first().text)
        tweets.forEach { println(it.text) }

        TimeUnit.SECONDS.sleep(20)
    }

    @Test
    fun testMongo() {
        val client = KMongo.createClient()
        val database = client.getDatabase("test")
        val col = database.getCollection<Tweet>()

        col.insertOne(Tweet(ZonedDateTime.now(), "Bottas 1", emptyList(), "Alfa Romeo"))
        col.insertOne(Tweet(ZonedDateTime.now(), "Bottas 2", emptyList(), "Alfa Romeo"))
        col.insertOne(Tweet(ZonedDateTime.now(), "Bottas 3", emptyList(), "Alfa Romeo"))
        val tweet = col.withDocumentClass<Tweet>()
            .find()
            .sort("{_id:-1}")
            .limit(1)
            .first()

        assertNotNull(tweet)
        assertTrue { tweet.message == "Bottas 3" }
    }
}