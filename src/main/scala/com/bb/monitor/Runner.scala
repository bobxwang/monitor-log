package com.bb.monitor


import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.commons.io.input.{Tailer, TailerListenerAdapter}

object Runner extends App {

  Thread.setDefaultUncaughtExceptionHandler((t, e) => {
    println(s"${t.getName} -->  ${e.getMessage}")
  })

  import java.util.concurrent.Executors

  val fixPool = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())

  val path = if (args.length == 0) System.getProperty("user.home") + "/logs/**.log" else args(0)
  println(path)
  val file = new File(path)
  FileUtils.touch(file)

  val tailer = new Tailer(file, new TailerListenerAdapter() {
    override def fileNotFound(): Unit = println(s"${file.getName} can not found")

    override def fileRotated(): Unit = println(s"s${file.getName} was rotated")

    override def handle(line: String): Unit = {

      fixPool.submit(new Runnable {
        override def run(): Unit = Analy.analy(line)
      })
    }

    override def handle(ex: Exception): Unit = println(s"has error --> ${ex}")
  }, 5000, true)

  val thread = new Thread(tailer)
  thread.start()

  Runtime.getRuntime.addShutdownHook(new Thread() {
    () => {
      try {
        tailer.stop()
        println("monitor stop success")
      } catch {
        case _: Exception =>
      }
      try {
        thread.interrupt()
        println("monitor thread stop success")
      } catch {
        case _: Exception =>
      }
    }
  })
}
