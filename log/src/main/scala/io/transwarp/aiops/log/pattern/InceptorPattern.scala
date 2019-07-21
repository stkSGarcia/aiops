package io.transwarp.aiops.log.pattern

import java.util.regex.Pattern

object InceptorPattern {
  /**
    * sample:
    * 2018-04-10 08:41:53,098 INFO  ql.Driver: (PerfLogger.java:PerfLogEnd(137))
    * [HiveServer2-Handler-Pool: Thread-1732(SessionHandle=1fcdf292-4179-4f17-9772-2e468c333915)]
    * - </PERFLOG method=Driver.execute start=1523364112194 end=1523364113098 duration=904>
    *
    */
  val sessionPattern = Pattern.compile("(?<=\\(SessionHandle\\=)[0-9a-z\\-]+(?=\\))")
  /**
    * sample:
    * 2018-04-18 08:33:12,138 INFO  thrift.ThriftCLIService: (ThriftCLIService.java:CloseSession(494))
    * [HiveServer2-Handler-Pool: Thread-3299()] - Closed a session: SessionHandle [778008cb-af33-476d-abf2-51422527990e]
    */
  val sessionClosePattern = Pattern.compile("Closed a session: SessionHandle \\[[a-z0-9-]+\\]")

  /**
    * sample:
    * 2018-04-09 08:09:32,014 INFO  parse.ParseDriver: (ParseDriver.java:parse(211)) [HiveServer2-Handler-Pool: Thread-380(SessionHandle=0aaf8727-17a7-431a-8ac4-88d14f9096c5)] - Parsing command: use jenkins_holodesk_globalindex_onshiva
    */
  val sqlPattern = Pattern.compile("(?<=Parsing command: )[^\\n](.|\\n)+")
}
