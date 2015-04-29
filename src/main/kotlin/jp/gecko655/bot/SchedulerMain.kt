package jp.gecko655.bot

import org.quartz.JobBuilder.newJob
import org.quartz.SimpleScheduleBuilder.repeatSecondlyForever
import org.quartz.TriggerBuilder.newTrigger

import java.util.TimeZone

import org.quartz.CronScheduleBuilder
import org.quartz.DateBuilder
import org.quartz.Job
import org.quartz.JobDetail
import org.quartz.ScheduleBuilder
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger
import org.quartz.impl.StdSchedulerFactory
import kotlin.properties.Delegates

object SchedulerMain {
    private val scheduler: Scheduler by Delegates.lazy{
        StdSchedulerFactory.getDefaultScheduler()
    }
    fun main(args: Array<String>){
        System.out.println("Scheduler Started!!!")

        scheduler.start()

    }

    private fun setSchedule(classForExecute: Class<Job>, schedule: ScheduleBuilder<Trigger>) {
        val jobDetail = newJob(classForExecute).build()

        val trigger = newTrigger()
                .startNow()
                .withSchedule(schedule)
                .build()

        scheduler.scheduleJob(jobDetail, trigger)
        System.out.println(classForExecute.getName() + " has been scheduled")

    }

}

