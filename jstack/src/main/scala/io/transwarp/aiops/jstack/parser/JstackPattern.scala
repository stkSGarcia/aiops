package io.transwarp.aiops.jstack.parser

import java.util.regex.Pattern

private[jstack] object JstackPattern {
  val timePattern: Pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})")
  val headPattern: Pattern = Pattern.compile(
    "^\"(.*)\"" + // group 1: threadName
      "(?:\\s#([0-9]+))?" + // group 2: threadId
      "(\\sdaemon)?" + // group 3: isDaemon
      "(?:\\sprio=([0-9]{1,2}))?" + // group 4: prio
      "(?:\\sos_prio=([0-9]{1,2}))?" + // group 5: os_prio
      "(?:\\stid=([a-z0-9]+))?" + // group 6: tid
      "(?:\\snid=([a-z0-9]+))?" + // group 7: nid
      "\\s(.*)$") // group 8: trailing
  val threadStatePattern: Pattern = Pattern.compile("(?<=State:\\s)[A-Z_]+")
  val lockPattern: Pattern = Pattern.compile("locked.*<([0-9a-z]+)>\\s\\(a\\s(.*)\\)")
  val waitLockPattern: Pattern = Pattern.compile("wait.*<([0-9a-z]+)>\\s\\(a\\s(.*)\\)")
}
