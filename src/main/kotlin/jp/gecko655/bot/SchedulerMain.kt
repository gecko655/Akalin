package jp.gecko655.bot

import jp.gecko655.bot.akalin.AkalinBot
import jp.gecko655.bot.akalin.AkalinReply
import org.jetbrains.kotlin.load.java.structure
import org.quartz.*
import org.quartz.JobBuilder.newJob
import org.quartz.SimpleScheduleBuilder.repeatSecondlyForever
import org.quartz.TriggerBuilder.newTrigger

import java.util.TimeZone

import org.quartz.impl.StdSchedulerFactory
import kotlin.properties.Delegates

private val scheduler: Scheduler =  StdSchedulerFactory.getDefaultScheduler()

fun main(args: Array<String>){
    System.out.println("Scheduler Started!!!")

    setSchedule(javaClass<AkalinReply>(), SimpleScheduleBuilder.repeatSecondlyForever(2*60))
    setSchedule(javaClass<AkalinBot>(), SimpleScheduleBuilder.repeatSecondlyForever(4*60*60))

    scheduler.start()

}

private fun setSchedule(classForExecute: Class<out Job>, schedule: SimpleScheduleBuilder) {
    val jobDetail = newJob(classForExecute).build()

    val trigger = newTrigger()
            .startNow()
            .withSchedule(schedule)
            .build()

    scheduler.scheduleJob(jobDetail, trigger)
    System.out.println(classForExecute.getName() + " has been scheduled")

}


