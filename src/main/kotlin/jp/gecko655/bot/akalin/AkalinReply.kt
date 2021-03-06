package jp.gecko655.bot.akalin


import jp.gecko655.bot.AbstractCron
import jp.gecko655.bot.DBConnection
import twitter4j.Paging
import twitter4j.Status
import twitter4j.StatusUpdate
import twitter4j.TwitterException
import java.text.DateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.TimeZone
import java.util.logging.Level
import java.util.regex.Pattern

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
        if(replies.isEmpty()){
            logger.log(Level.INFO, "No replies found. Stop.")
            return
        }
        DBConnection.setLastStatus(replies.get(0))
        
        if (lastStatus == null) {
            logger.log(Level.INFO, "memcache saved. Stop. " + replies.get(0).getUser().getName() + "'s tweet at " + format.format(replies.get(0).getCreatedAt()))
            return
        }
        val validReplies = replies.filter { isValidReply(it,lastStatus) }
        if(validReplies.isEmpty()){
            logger.log(Level.FINE, "No valid replies. Stop.")
            return
        }

        validReplies.forEach({ reply ->
            if (!twitter.friendsFollowers()
                    .showFriendship(twitter.getId(), reply.getUser().getId())
                    .isSourceFollowingTarget())
                followBack(reply)
            when {
                whoPattern.matcher(reply.getText()).find()//The reply has 誰 format
                        &&reply.getInReplyToStatusId()>0//The reply replies to a specific tweet.
                        &&twitter.showStatus(reply.getInReplyToStatusId()).getMediaEntities().isNotEmpty()
                            //The specific tweet has at least 1 media entry
                -> {
                    // put image URL to black-list
                    who(reply)
                }
                else -> {
                    //auto reply
                    val update = StatusUpdate("@" + reply.getUser().getScreenName() + " ")
                    update.setInReplyToStatusId(reply.getId())
                    val query = when ((Math.random() * 20).toInt()) {
                        0 -> "赤座あかり ゆるゆり かわいい"
                        in 1..4 -> "歳納京子 ゆるゆり かわいい"
                        in 5..8 -> "船見結衣 ゆるゆり かわいい"
                        in 9..12 -> "吉川ちなつ ゆるゆり かわいい"
                        in 13..14 -> "向日葵 ゆるゆり かわいい"
                        in 15..16 -> "櫻子 ゆるゆり かわいい"
                        17 -> "杉浦綾乃 ゆるゆり かわいい"
                        18 -> "池田千歳 ゆるゆり かわいい"
                        else -> "ゆるゆり かわいい"
                    }
                    updateStatusWithMedia(update, query, 100)
                }
            }
        })

    }

    private fun isValidReply(reply: Status, lastStatus: Status?): Boolean {
        if(Duration.between(
                reply.getCreatedAt().toInstant(),
                LocalDateTime.now().toInstant(ZoneOffset.UTC))
                .toHours()>12)
            return false;
        return lastStatus?.getCreatedAt()?.before(reply.getCreatedAt()) ?:false
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
