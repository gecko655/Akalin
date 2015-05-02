package jp.gecko655.bot.akalin

import jp.gecko655.bot.AbstractCron
import twitter4j.StatusUpdate

public class AkalinBot : AbstractCron() {

    override fun twitterCron() {
        //Twitterに書き出し
        val status = StatusUpdate(" ")
        when ((Math.random() * 13).toInt()) {
            0 -> updateStatusWithMedia(status, "赤座あかり ゆるゆり", 100)
            in 1..3 -> updateStatusWithMedia(status, "歳納京子 ゆるゆり", 100)
            in 4..6 -> updateStatusWithMedia(status, "船見結衣 ゆるゆり", 100)
            in 7..9 -> updateStatusWithMedia(status, "吉川ちなつ ゆるゆり", 100)
            10 -> updateStatusWithMedia(status, "向日葵 ゆるゆり", 100)
            11 -> updateStatusWithMedia(status, "櫻子 ゆるゆり", 100)
            else -> updateStatusWithMedia(status, "ゆるゆり", 100)
        }

    }

}
