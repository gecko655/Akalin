package jp.gecko655.bot.akalin


import java.text.DateFormat
import java.util.TimeZone
import java.util.logging.Level
import java.util.regex.Pattern

import jp.gecko655.bot.AbstractCron
import jp.gecko655.bot.DBConnection
import twitter4j.Paging
import twitter4j.Relationship
import twitter4j.Status
import twitter4j.StatusUpdate
import twitter4j.TwitterException

public class AkalinReply : AbstractCron() {

    companion object {
        val format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
        private val keishouPattern = Pattern.compile("(くん|さん|君|ちゃん)$")
        private val whoPattern = Pattern.compile("( 誰$| だれ$|誰[^だで]|だれ[^だで]|誰だ[^と]?|だれだ[^と]?| 違う| ちがう)")
    }
    {
        format.setTimeZone(TimeZone.getDefault())
    }

    override fun twitterCron() {
        try {
            val replies = twitter.getMentionsTimeline((Paging()).count(20))
            val lastStatus = DBConnection.getLastStatus()
            DBConnection.setLastStatus(replies.get(0))
            if (lastStatus == null) {
                logger.log(Level.INFO, "memcache saved. Stop. " + replies.get(0).getUser().getName() + "'s tweet at " + format.format(replies.get(0).getCreatedAt()))
                return
            }

            for (reply in replies) {
                if (isOutOfDate(reply, lastStatus))
                    break
                val relation = twitter.friendsFollowers().showFriendship(twitter.getId(), reply.getUser().getId())

                if (!relation.isSourceFollowingTarget()) {
                    followBack(reply)
                } else if (whoPattern.matcher(reply.getText()).find()) {
                    // put latest image URL to black-list
                    who(reply)
                } else {
                    //auto reply (when fujimiya-san follows the replier)
                    val update = StatusUpdate("@" + reply.getUser().getScreenName() + " ")
                    update.setInReplyToStatusId(reply.getId())
                    if (((Math.random() * 10).toInt()) == 1) {
                        //10%
                        updateStatusWithMedia(update, "山岸沙希 かわいい 一週間フレンズ。", 100)
                    } else {
                        updateStatusWithMedia(update, "藤宮香織 かわいい 一週間フレンズ。", 100)
                    }
                }
            }
        } catch (e: TwitterException) {
            logger.log(Level.WARNING, e.toString())
            e.printStackTrace()
        }

    }

    private fun isOutOfDate(reply: Status, lastStatus: Status): Boolean {
        if (reply.getCreatedAt().getTime() - lastStatus.getCreatedAt().getTime() <= 0) {
            logger.log(Level.INFO, reply.getUser().getName() + "'s tweet at " + format.format(reply.getCreatedAt()) + " is out of date")
            return true
        }
        return false
    }

    private fun followBack(reply: Status) {
        twitter.createFriendship(reply.getUser().getId())
        var userName = reply.getUser().getName()
        if (!keishouPattern.matcher(userName).find()) {
            userName = userName + "くん"
        }
        val update = StatusUpdate("@" + reply.getUser().getScreenName() + " もしかして、あなたが" + userName + "？")
        update.setInReplyToStatusId(reply.getId())
        twitter.updateStatus(update)
    }

    private fun who(reply: Status) {
        //Store the url to the black list.
        DBConnection.storeImageUrlToBlackList(reply.getInReplyToStatusId(), reply.getUser().getScreenName())

        try {
            //Delete the reported tweet.
            twitter.destroyStatus(reply.getInReplyToStatusId())

            //Apologize to the report user.
            val update = StatusUpdate("@" + reply.getUser().getScreenName() + " 間違えちゃった。ごめんね！")
            update.setInReplyToStatusId(reply.getId())
            twitter.updateStatus(update)
        } catch (e: TwitterException) {
            e.printStackTrace()
        }

    }

}
