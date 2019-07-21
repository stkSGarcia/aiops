package scala.io.transwarp.aiops.log.model

import java.util
import java.util.regex.Pattern

import com.alibaba.fastjson.JSON
import io.transwarp.aiops.log.conf.{GoalTemplate, TaskTemplate}

//import org.apache.commons.io.IOUtils

class Load {
   def getPath(): Unit ={
//     println(IOUtils.toString(getClass.getClassLoader.getResourceAsStream(PlainDocDao.SettingFile)))
      println(getClass.getClassLoader.getResource("/log/inceptor.json"))

     val taskList = new util.ArrayList[TaskTemplate]

     // parsing and logical plan
     taskList.add(new TaskTemplate(0, "parsing and logical plan", pre = "*<PERFLOG method=compile>*", occur = null,
       post = "*<PERFLOG method=Driver.execute>*"))

     // physical plan
     taskList.add(new TaskTemplate(1, "physical plan", pre = ""))


     // execute
     taskList.add(new TaskTemplate(2, "execute ", pre = ""))




//     val goal = new GoalTemplate("INCEPTOR", )

//     JSON.


   }
}

object Test {

  //    [HiveServer2-Handler-Pool: Thread-1732(SessionHandle=1fcdf292-4179-4f17-9772-2e468c333915)]
  def main(args: Array[String]): Unit = {
     val sourceString = "2018-04-10 08:41:50,828 INFO  ql.Driver: (PerfLogger.java:PerfLogBegin(110)) [HiveServer2-Handler-Pool: Thread-1732(SessionHandle=1fcdf292-4179-4f17-9772-2e468c333915)] - <PERFLOG method=Driver.run>"
////    val sourceString = "2018-04-10 08:41:52,287 INFO  scheduler.DAGScheduler: (Logging.scala:logInfo(59)) [sparkDriver-akka.actor.default-dispatcher-15()] - Parents of final stage: List()"
//     val sourceString = " "

    val sessionPattern = Pattern.compile("(?<=\\(SessionHandle\\=)[0-9a-z\\-]+(?=\\))")
    val matcher = sessionPattern.matcher(sourceString)

    if (matcher.find()) {
       println(matcher.group(0))
    }

  }
}
