package com.yber.java.scheduler;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

public class TestQuartz {
    public static void main(String[] args) throws SchedulerException {

        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = stdSchedulerFactory.getScheduler();


        //任务
        JobDetail build = JobBuilder.newJob(JobImpl.class).withIdentity("myJob", "jobGroup").build();

        //触发器
        Date triggerDate = new Date();
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(2).repeatForever();
        TriggerBuilder<Trigger> triggerTriggerBuilder = TriggerBuilder.newTrigger().withIdentity("myTrigger", "triggerGroup");
        SimpleTrigger build1 = triggerTriggerBuilder.startAt(triggerDate).withSchedule(simpleScheduleBuilder).build();

        //任务与触发器放入调度器
        scheduler.scheduleJob(build,build1);
        scheduler.start();
    }
}
