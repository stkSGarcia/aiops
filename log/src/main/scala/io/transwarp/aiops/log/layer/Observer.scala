package io.transwarp.aiops.log.layer

import java.util

trait Observer {
  var subjects = new util.ArrayList[Subject]

   def setSubject(subject: Subject): Unit = {
     subjects.add(subject)
   }

   def update(subject: Subject)
}
