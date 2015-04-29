package jp.gecko655.bot

/**
 * Created by gecko655 on 15/04/29.
 */

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Base64
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.StreamHandler

import org.bson.Document

import twitter4j.Status

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import kotlin.properties.Delegates


public class DBConnection {
    companion object {

        private val logger: Logger by Delegates.lazy{
            val l = Logger.getLogger(javaClass<DBConnection>().getName())
            l.setLevel(Level.INFO)
            l
        }

        private val mongoClientURI = MongoClientURI(System.getenv("MONGOLAB_URI"))
        private val client = MongoClient(mongoClientURI)
        private val db = client.getDatabase(mongoClientURI.getDatabase())
        private val imageUrlCollectionName = "imageUrl"
        private val blackListCollectionName = "blackList"
        private val lastStatusCollectionName = "lastStatus"
        private val urlKey = "url"
        private val reportedUserKey = "reporteduser"
        private val statusIdKey = "statusid"
        private val lastStatusKey = "laststatus"

        public fun storeImageUrl(succeededStatus: Status, fetchedImage: FetchedImage) {
            val collection = db.getCollection(imageUrlCollectionName)
            val doc = Document(statusIdKey, succeededStatus.getId())
            doc.put(urlKey, fetchedImage.url)
            collection.insertOne(doc)
        }

        public fun storeImageUrlToBlackList(statusIdToReport: Long, reportedUser: String) {
            val imageUrlCollection = db.getCollection(imageUrlCollectionName)
            val imageUrlDoc = imageUrlCollection.find(Filters.eq<Long>(statusIdKey, statusIdToReport)).first()
            if (imageUrlDoc != null) {
                val url = imageUrlDoc.getString(urlKey)
                val blackListCollection = db.getCollection(blackListCollectionName)
                val doc = Document(urlKey, url)
                doc.put(reportedUserKey, reportedUser)
                blackListCollection.insertOne(doc)
                logger.log(Level.INFO, "The image URL: " + url + " was stored to the black list.")
            } else {
                logger.log(Level.WARNING, "Image URL was not found in data collection")
            }
        }

        public fun isInBlackList(link: String): Boolean {
            val blackListCollection = db.getCollection(blackListCollectionName)
            return blackListCollection.find(Filters.eq<String>(urlKey, link)).iterator().hasNext()
        }

        public fun getLastStatus(): Status? {
            val lastStatusCollection = db.getCollection(lastStatusCollectionName)
            val doc = lastStatusCollection.find(Filters.exists(lastStatusKey)).first()
            if (doc == null)
                return null
            return fromBase64(doc.getString(lastStatusKey))
        }

        public fun setLastStatus(status: Status) {
            val lastStatusCollection = db.getCollection(lastStatusCollectionName)
            val document = Document(lastStatusKey, toBase64(status))
            lastStatusCollection.replaceOne(Filters.exists(lastStatusKey), document, UpdateOptions().upsert(true))
        }

        private fun toBase64(status: Status): String {
            try {
                val bos = ByteArrayOutputStream()
                val out = ObjectOutputStream(bos)
                out.writeObject(status)
                return Base64.getEncoder().encodeToString(bos.toByteArray())
            } catch (e: IOException) {
                throw Error()
            }


        }

        private fun fromBase64(s: String): Status {
            try {
                val bArray = Base64.getDecoder().decode(s)
                val bis = ByteArrayInputStream(bArray)
                val `in` = ObjectInputStream(bis)
                return `in`.readObject() as Status
            } catch (e: IOException) {
                throw Error()
            } catch (e: ClassNotFoundException) {
                throw Error()
            }


        }
    }

}

