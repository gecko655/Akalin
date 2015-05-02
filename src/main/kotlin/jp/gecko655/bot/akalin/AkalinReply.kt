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
        private val whoPattern = Pattern.compile("( 誰$| だれ$|誰[^だで]|だれ[^だで]|誰だ[^と]?|だれだ[^と]?| 違う| ちがう)")
    }
    init {
        format.setTimeZone(TimeZone.getDefault())
    }

    override fun twitterCron() {
        val lastStatus = DBConnection.getLastStatus()
        val replies = twitter.getMentionsTimeline((Paging()).count(20))
                .filter { isValidReply(it,lastStatus) }
        if(replies.isEmpty()){
            logger.log(Level.FINE, "No replies found. Stop.")
            return
        }
        DBConnection.setLastStatus(replies.get(0))
        if (lastStatus == null) {
            logger.log(Level.INFO, "memcache saved. Stop. " + replies.get(0).getUser().getName() + "'s tweet at " + format.format(replies.get(0).getCreatedAt()))
            return
        }

        replies.forEach({ reply ->
            if (!twitter.friendsFollowers()
                    .showFriendship(twitter.getId(), reply.getUser().getId())
                    .isSourceFollowingTarget())
                followBack(reply)
            when {
                whoPattern.matcher(reply.getText()).find() -> {
                    // put image URL to black-list
                    who(reply)
                }
                else -> {
                    //auto reply
                    val update = StatusUpdate("@" + reply.getUser().getScreenName() + " ")
                    update.setInReplyToStatusId(reply.getId())
                    when ((Math.random() * 10).toInt()) {
                        0 -> updateStatusWithMedia(update, "赤座あかり ゆるゆり かわいい", 100)
                        in 1..3 -> updateStatusWithMedia(update, "歳納京子 ゆるゆり かわいい", 100)
                        in 4..6 -> updateStatusWithMedia(update, "船見結衣 ゆるゆり かわいい", 100)
                        in 7..9 -> updateStatusWithMedia(update, "吉川ちなつ ゆるゆり かわいい", 100)
                    }
                }
            }
        })

    }

    private fun isValidReply(reply: Status, lastStatus: Status?): Boolean {
        return lastStatus?.getCreatedAt()?.before(reply.getCreatedAt()) ?:true
    }

    private fun followBack(reply: Status) {
        twitter.createFriendship(reply.getUser().getId())
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
