package io.transwarp.aiops.plain.service.util

import scala.collection.mutable.ArrayBuffer


object State extends Enumeration {
  type State = Value
  val NORM = Value(0)
  val TEMP = Value(1)
  // exception state is not a stable state
  val EXCEPTION = Value(2)
  val EXCEPTION_TMP = Value(3)
}

class Analyzer(tokens: ArrayBuffer[Token]) {
  val normalBuf = new StringBuilder
  var tmpBuf = new ArrayBuffer[Token]
  val exceptionBuf = new StringBuilder
  var exceptionTmpBuf = new StringBuilder
  var currState = State.NORM
  analyze()


  def getNormalString(): String = {
    normalBuf.toString.trim
  }

  def getExceptionString(): String = {
    exceptionBuf.toString.trim
  }

  private def copyTemp2Norm() = {
    tmpBuf.toIterator.foreach(putToken2Norm)
  }

  private def copyTemp2Exception() = {
    tmpBuf.toIterator.foreach(putToken2Exception)
  }

  private def putToken(token: Token, buf: StringBuilder): Unit = {
    buf.append(" ");
    buf.append(token.value)
  }

  private def putToken2Norm(token: Token) = {
    normalBuf.append(" ")
    normalBuf.append(token.value)
  }


  private def putToken2Exception(token: Token) = {
    if (token.typ != TokenType.Num) {
      exceptionBuf.append(" ")
      exceptionBuf.append(token.value)
    }
  }

  private def norm2Temp(token: Token): Boolean = {
    token.typ match {
      case TokenType.AsciiDot | TokenType.Exception | TokenType.Ascii => true
      case _ => false
    }
  }

  private def temp2Exception(token: Token): Boolean = {
    token.typ == TokenType.AT
  }

  private def temp2Norm(token: Token): Boolean = {
    token.typ match {
      case TokenType.Eof | TokenType.NonAscii => true
      case _ => false
    }
  }

  private def temp2Temp(token: Token): Boolean = {
    token.typ match {
      case TokenType.Ascii | TokenType.AsciiDot | TokenType.Num | TokenType.Exception => true
      case _ => false
    }
  }

  private def exceptionTmp2Norm(token: Token): Boolean = {
    token.typ match {
      case TokenType.NonAscii | TokenType.Eof => true
      case _ => false
    }
  }

  private def exceptionTmp2Exception(token: Token): Boolean = {
    token.typ == TokenType.AT
  }

  def analyze(): Unit = {
    tokens.toIterator.foreach { (token) => {
      currState match {
        case State.NORM => {
          if (norm2Temp(token)) {
            currState = State.TEMP
            tmpBuf += token
          } else {
            putToken(token, normalBuf)
          }
        }
        case State.TEMP => {
          if (temp2Temp(token)) {
            tmpBuf += token
          } else if (temp2Exception(token)) {
            tmpBuf += token
            copyTemp2Exception()
            tmpBuf = new ArrayBuffer[Token]
            currState = State.EXCEPTION_TMP
          } else if (temp2Norm(token)) {
            tmpBuf += token
            copyTemp2Norm()
            tmpBuf = new ArrayBuffer[Token]
            currState = State.NORM
          } else {
            throw new RuntimeException("unknown token for TEMP state")
          }
        }
        case State.EXCEPTION_TMP => {
          if (exceptionTmp2Exception(token)) {
            putToken(token, exceptionTmpBuf)
            exceptionBuf.append(exceptionTmpBuf.toString)
            exceptionTmpBuf = new StringBuilder
          } else if (exceptionTmp2Norm(token)) {
            exceptionBuf.append(exceptionTmpBuf.toString)
            exceptionTmpBuf = new StringBuilder
            currState = State.NORM
            putToken(token, normalBuf)
          } else if (temp2Temp(token)) {
            if (token.typ != TokenType.Num) {
              putToken(token, exceptionTmpBuf)
            }
          } else {
            throw new RuntimeException("unknown token for EXCEPTION_TMP state")
          }
        }
        case _ => {
          throw new RuntimeException("unknown state for analyzer")
        }
      }
    }
    }
  }


}

