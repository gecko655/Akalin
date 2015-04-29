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

    val logger by Delegates.lazy {
        val l = Logger.getLogger("Fujimiya")
        l.setLevel(Level.INFO)
        l
    }


    val consumerKey = System.getenv("consumerKey")
    val consumerSecret = System.getenv("consumerSecret")
    val accessToken = System.getenv("accessToken")
    val accessTokenSecret = System.getenv("accessTokenSecret")
    val customSearchCx = System.getenv("customSearchCx")
    val customSearchKey = System.getenv("customSearchKey")
    val twitter :Twitter by Delegates.lazy{
        val cb = ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
        TwitterFactory(cb.build()).getInstance()

    }
    val search :Customsearch by Delegates.lazy{
        val builder = Customsearch.Builder(NetHttpTransport(), JacksonFactory(), null).setApplicationName("Google")
        builder.build()
    }

    fun getFujimiyaUrl(query: String) = getFujimiyaUrl(query,100)
    fun getFujimiyaUrl(query: String, maxRankOfResult: Int) :FetchedImage{
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

    val apiLimit = 100
    val pageSize = 10
    fun getSearchResult(query: String, _maxRankOfResult: Int): Search {
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

    fun updateStatusWithMedia(update: StatusUpdate, query: String, maxRankOfResult: Int){
        val fetchedImage = getFujimiyaUrl(query,maxRankOfResult)
        update.media("fujimiya.jpg",fetchedImage.instream)
        for(i in 1..10){
            try {
                val succeededStatus = twitter.updateStatus(update)
                logger.log(Level.INFO, "Successfully tweeted: " + succeededStatus.getText());
                DBConnection.storeImageUrl(succeededStatus, fetchedImage)
                return
            }catch(e: TwitterException){
                logger.log(Level.INFO,"updateStatusWithMedia failed. try again. "+ e.getErrorMessage())
            }
        }
        logger.log(Level.SEVERE,"updateStatusWithMedia failed 10 times. Stop.")



    }

}

class FetchedImage (instream:InputStream, url:String) {
    val instream = instream
    val url = url
}


