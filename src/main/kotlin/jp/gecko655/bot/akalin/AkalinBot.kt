package jp.gecko655.bot.akalin

import jp.gecko655.bot.AbstractCron
import twitter4j.StatusUpdate

public class AkalinBot : AbstractCron() {

    override fun twitterCron() {
        //Twitterに書き出し
        val status = StatusUpdate(" ")
        when ((Math.random() * 10).toInt()) {
            0 -> updateStatusWithMedia(status, "赤座あかり ゆるゆり", 100)
            in 1..3 -> updateStatusWithMedia(status, "歳納京子 ゆるゆり", 100)
            in 4..6 -> updateStatusWithMedia(status, "船見結衣 ゆるゆり", 100)
            in 4..6 -> updateStatusWithMedia(status, "吉川ちなつ ゆるゆり", 100)
        }

    }

}
