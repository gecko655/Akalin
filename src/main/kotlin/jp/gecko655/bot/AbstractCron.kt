package jp.gecko655.bot

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.customsearch.Customsearch
import com.google.api.services.customsearch.model.Search
import org.quartz.Job
import org.quartz.JobExecutionContext
import twitter4j.StatusUpdate
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.io.InputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.properties.Delegates

abstract class AbstractCron : Job {

    protected val logger :Logger by Delegates.lazy {
        val l = Logger.getLogger("Fujimiya")
        l.setLevel(Level.INFO)
        l
    }


    private val consumerKey = System.getenv("consumerKey")
    private val consumerSecret = System.getenv("consumerSecret")
    private val accessToken = System.getenv("accessToken")
    private val accessTokenSecret = System.getenv("accessTokenSecret")
    private val customSearchCx = System.getenv("customSearchCx")
    private val customSearchKey = System.getenv("customSearchKey")
    protected val twitter :Twitter by Delegates.lazy{
        val cb = ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
        TwitterFactory(cb.build()).getInstance()

    }
    private val search :Customsearch by Delegates.lazy{
        val builder = Customsearch.Builder(NetHttpTransport(), JacksonFactory(), null).setApplicationName("Google")
        builder.build()
    }

    override fun execute(context: JobExecutionContext?) {
        try {
            twitterCron()
        }catch(e: Exception){
            e.printStackTrace()
        }

    }
    abstract protected fun twitterCron()

    public fun getImageUrl(query: String, maxRankOfResult: Int = 100) :FetchedImage{
        val search = getSearchResult(query,maxRankOfResult)
        val items = search.getItems()
        for(result in items){
            val i = items.indexOf(result)
            logger.log(Level.INFO,"query: " + query + " URL: "+result.getLink())
            logger.log(Level.INFO,"page URL: "+result.getImage().getContextLink())
            if(result.getImage().getWidth()+result.getImage().getHeight()<600){
                logger.log(Level.INFO,"Result No."+i+" is too small image. next.")
                continue
            }
            if(DBConnection.isInBlackList(result.getLink())){
                logger.log(Level.INFO,"Result No."+i+" is included in the blacklist. next.")
                continue
            }
            val connection = (URL(result.getLink())).openConnection() as HttpURLConnection
            connection.setRequestMethod("GET")
            connection.setInstanceFollowRedirects(false)
            connection.connect()
            if(connection.getResponseCode()==200){
                return FetchedImage(connection.getInputStream(),result.getLink());
            }else{
                logger.log(Level.INFO,"Result No."+i+" occurs error while fetching the image. next.");
                continue;
            }
        }
        throw ConnectException("Connection failed 10 times")
    }

    private val apiLimit = 100
    private val pageSize = 10
    private fun getSearchResult(query: String, _maxRankOfResult: Int): Search {
        val maxRankOfResult =
                if(_maxRankOfResult <= apiLimit - pageSize + 1)
                    _maxRankOfResult else (apiLimit - pageSize + 1)
        val list = search.cse().list(query)
        val rand = (Math.random()*maxRankOfResult+1).toLong();
        list.setCx(customSearchCx)
            .setKey(customSearchKey)
            .setSearchType("image")
            .setNum(pageSize.toLong())
            .setStart(rand)
        logger.log(Level.INFO,"rand: "+rand)
        return list.execute()

    }

    public fun updateStatusWithMedia(update: StatusUpdate, query: String, maxRankOfResult: Int){
        val fetchedImage = getImageUrl(query,maxRankOfResult)
        update.media("fujimiya.jpg",fetchedImage.instream)
        (1..10).forEach { i ->
            try {
                val succeededStatus = twitter.updateStatus(update)
                logger.log(Level.INFO, "Successfully tweeted: " + succeededStatus.getText());
                DBConnection.storeImageUrl(succeededStatus, fetchedImage)
                return
            } catch(e: TwitterException) {
                logger.log(Level.INFO, "updateStatusWithMedia failed. try again. " + e.getErrorMessage())
            }
        }
        logger.log(Level.SEVERE,"updateStatusWithMedia failed 10 times. Stop.")



    }

}

class FetchedImage (instream:InputStream, url:String) {
    val instream = instream
    val url = url
}


