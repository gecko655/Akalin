package jp.gecko655.bot.akalin

import jp.gecko655.bot.AbstractCron
import twitter4j.StatusUpdate

public class AkalinBot : AbstractCron() {

    override fun twitterCron() {
        //Twitterに書き出し
        val status = StatusUpdate(" ")
        val query = when ((Math.random() * 20).toInt()) {
            0 -> "赤座あかり ゆるゆり"
            in 1..4 -> "歳納京子 ゆるゆり"
            in 5..8 -> "船見結衣 ゆるゆり"
            in 9..12 -> "吉川ちなつ ゆるゆり"
            in 13..14 -> "向日葵 ゆるゆり"
            in 15..16 -> "櫻子 ゆるゆり"
            17 -> "杉浦綾乃 ゆるゆり"
            18 -> "池田千歳 ゆるゆり"
            else -> "ゆるゆり"
        }
        updateStatusWithMedia(status, query, 100)

    }

}
