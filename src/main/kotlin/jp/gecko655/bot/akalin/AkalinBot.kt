package jp.gecko655.bot.akalin

import jp.gecko655.bot.AbstractCron
import twitter4j.StatusUpdate

public class AkalinBot : AbstractCron() {

    override fun twitterCron() {
        //Twitterに書き出し
        val status = StatusUpdate(" ")
        val query = when ((Math.random() * 20).toInt()) {
            in 0..10 -> "赤座あかり ゆるゆり"
            else -> "赤座あかり 誕生日 ゆるゆり"
        }
        updateStatusWithMedia(status, query, 100)

    }

}
