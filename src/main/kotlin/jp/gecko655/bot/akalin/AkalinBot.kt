package jp.gecko655.bot.akalin

import jp.gecko655.bot.AbstractCron
import twitter4j.StatusUpdate

public class AkalinBot : AbstractCron() {

    override fun twitterCron() {
        //Twitterに書き出し
        val status = StatusUpdate(" ")
        if (((Math.random() * 10).toInt()) == 1) {
            //10%
            updateStatusWithMedia(status, "山岸さん 一週間フレンズ。", 100)
        } else {
            updateStatusWithMedia(status, "藤宮さん 一週間フレンズ。", 100)
        }

    }

}
