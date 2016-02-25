/**
 * Created by gecko655 on 15/06/14.
 */

import jp.gecko655.bot.AbstractCron
import org.junit.Before
import org.junit.Test
import javax.imageio.ImageIO
import org.junit.Assert.*

public class CronTest {
    var cron :AbstractCron? = null
    @Before
    fun setup(){
        cron = object: AbstractCron(){
            override fun twitterCron() {
                throw UnsupportedOperationException()
            }
        }
    }

    @Test
    fun test() {
        val fetchedImage = cron!!.getImageUrl("gecko655")
        val image =ImageIO.read(fetchedImage.instream)
        assertTrue("image size is too small.", image.getHeight()+image.getWidth()>=600)
    }
}