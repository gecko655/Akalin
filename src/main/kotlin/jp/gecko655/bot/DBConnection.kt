package jp.gecko655.bot

import twitter4j.Status

/**
 * Created by gecko655 on 15/04/29.
 */
public object DBConnection {
    fun isInBlackList(url: String): Boolean {
        return false
    }

    fun storeImageUrl(succeededStatus: Status, fetchedImage: FetchedImage) {
    }
}