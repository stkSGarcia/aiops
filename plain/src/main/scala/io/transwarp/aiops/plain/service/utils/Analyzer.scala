package io.transwarp.aiops.plain.service.utils

class Analyzer(tokenizer: Tokenizer) {
  private val normal = new StringBuilder
  private val exception = new StringBuilder
  private val buffer = new StringBuilder
  private var curState = State.UNKNOWN // determine buffer state

  analyze

  def analyze: Unit = {
    while (tokenizer.hasNext) {
      val token = tokenizer.next
      token.typ match {
        case TokenType.SPACE | TokenType.NUMBER | TokenType.ASCII => buffer.append(" ")
        case TokenType.LF =>
          curState match {
            case State.WORD | State.NON_ASCII | State.UNKNOWN =>
              normal.append(buffer).append("\n")
              buffer.clear
            case State.EXCEPTION =>
              exception.append(buffer).append("\n")
              buffer.clear
          }
          curState = State.UNKNOWN
        case TokenType.WORD =>
          curState match {
            case State.WORD | State.EXCEPTION =>
            case State.NON_ASCII =>
              normal.append(buffer)
              buffer.clear
              curState = State.WORD
            case State.UNKNOWN =>
              curState = State.WORD
          }
          buffer.append(token.value)
        case TokenType.NON_ASCII =>
          curState match {
            case State.WORD =>
              normal.append(buffer)
              buffer.clear
            case State.EXCEPTION =>
              exception.append(buffer)
              buffer.clear
            case State.NON_ASCII | State.UNKNOWN =>
          }
          buffer.append(token.value)
          curState = State.NON_ASCII
        case TokenType.AT | TokenType.CAUSED_BY | TokenType.EXCEPTION =>
          curState match {
            case State.WORD | State.EXCEPTION | State.UNKNOWN =>
            case State.NON_ASCII =>
              normal.append(buffer)
              buffer.clear
          }
          buffer.append(token.value)
          curState = State.EXCEPTION
        case TokenType.EOF =>
          curState match {
            case State.EXCEPTION => exception.append(buffer)
            case _ => normal.append(buffer)
          }
          buffer.clear
        case _ => throw new RuntimeException("Unknown token type.")
      }
    }
  }

  def normalString: String = normal.toString

  def exceptionString: String = exception.toString
}

object State extends Enumeration {
  type State = Value
  val UNKNOWN = Value
  val WORD = Value
  val NON_ASCII = Value
  val EXCEPTION = Value
}
