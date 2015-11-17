package org.apache.spark.scheduler.cluster

import org.apache.spark.SparkContext
import org.apache.spark.scheduler.{SchedulerBackend, TaskSchedulerImpl, TaskScheduler, ExternalClusterManager}

/**
 * Snappy's cluster manager that is responsible for creating
 * scheduler and scheduler backend.
 *
 * Created by hemant
 */
object SnappyEmbeddedModeClusterManager extends ExternalClusterManager {

  var schedulerBackend: Option[SnappyCoarseGrainedSchedulerBackend] = None
  def createTaskScheduler(sc: SparkContext): TaskScheduler = {
    // If there is an application that is trying to join snappy
    // as lead in embedded mode, we need the locator to connect
    // to the snappy distributed system and hence the locator is
    // passed in masterurl itself.
    if (sc.master.startsWith("snappydata://"))
      sc.conf.set("snappydata.store.locators", sc.master.replaceFirst("snappydata://", ""))
    new TaskSchedulerImpl(sc)
  }

  def canCreate(masterURL: String): Boolean =
    if (masterURL.startsWith("snappydata")) true else false

  def createSchedulerBackend(sc: SparkContext,
      scheduler: TaskScheduler): SchedulerBackend = {
    schedulerBackend = Some(
      new SnappyCoarseGrainedSchedulerBackend(
        scheduler.asInstanceOf[TaskSchedulerImpl], sc.env.rpcEnv))
    schedulerBackend.get
  }

  def intialize(scheduler: TaskScheduler,
      backend: SchedulerBackend): Unit = {
    scheduler.asInstanceOf[TaskSchedulerImpl].initialize(backend)
  }
}