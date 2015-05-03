package jp.gecko655.bot.akalin

import jp.gecko655.bot.AbstractCron
import twitter4j.StatusUpdate

public class AkalinBot : AbstractCron() {

    override fun twitterCron() {
        //Twitterに書き出し
        val status = StatusUpdate(" ")
        when ((Math.random() * 20).toInt()) {
            0 -> updateStatusWithMedia(status, "赤座あかり ゆるゆり", 100)
            in 1..4 -> updateStatusWithMedia(status, "歳納京子 ゆるゆり", 100)
            in 5..8 -> updateStatusWithMedia(status, "船見結衣 ゆるゆり", 100)
            in 9..12 -> updateStatusWithMedia(status, "吉川ちなつ ゆるゆり", 100)
            in 13..14 -> updateStatusWithMedia(status, "向日葵 ゆるゆり", 100)
            in 15..16 -> updateStatusWithMedia(status, "櫻子 ゆるゆり", 100)
            17 -> updateStatusWithMedia(status, "杉浦綾乃 ゆるゆり", 100)
            18 -> updateStatusWithMedia(status, "池田千歳 ゆるゆり", 100)
            else -> updateStatusWithMedia(status, "ゆるゆり", 100)
        }

    }

}
