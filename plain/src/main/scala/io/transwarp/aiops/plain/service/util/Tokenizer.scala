package io.transwarp.aiops.plain.service.util


import io.transwarp.aiops.plain.service.util.TokenType.TokenType
import io.transwarp.aiops.plain.service.util.CharType.CharType

import scala.collection.mutable.ArrayBuffer

object CharType extends Enumeration {
  type CharType = Value
  val Unknown = Value(-1)
  val Space = Value(0)
  val Dot = Value(1)
  val Num = Value(2)
  val Ascii = Value(3)
  val NonAscii = Value(4)
  val Eof = Value(5)
}


object TokenType extends Enumeration {
  type TokenType = Value
  val Unknown = Value(-1)
  val Space = Value(0)
  val Exception = Value(1)
  val Num = Value(2)
  val Ascii = Value(3)
  val AsciiDot = Value(4)
  val NonAscii = Value(5)
  val AT = Value(6)
  val Eof = Value(7)
}

case class Token(val typ: TokenType, value: String)

object Tokenizer {
  val EffectiveAsciiStart = 33
  val EffectiveAsciiEnd = 126
}


class Tokenizer(input: String) {
  var currWordType = TokenType.Unknown
  var wordBuf = new StringBuilder
  val tokenBuf = new ArrayBuffer[Token]


  private def isCharTokenSameType(charType: CharType, tokenType: TokenType): Boolean = {
    (charType == CharType.Num && tokenType == TokenType.Num) ||
      (charType == CharType.Ascii && tokenType == TokenType.Ascii) ||
      (charType == CharType.NonAscii && tokenType == TokenType.NonAscii)
  }

  private def updateWordTypeByCharType(typ: CharType): Unit = {
    typ match {
      case CharType.Num => currWordType = TokenType.Num
      case CharType.Ascii => currWordType = TokenType.Ascii
      case CharType.NonAscii => currWordType = TokenType.NonAscii
    }
  }


  def pushToken(typ: CharType, value: Char): Unit = {
    currWordType match {
      case TokenType.Unknown => {}
      case TokenType.Ascii => {
         val word = wordBuf.toString.toLowerCase
         // words contains exception then gen Exception Token
         if (word.equals("at")) {
           tokenBuf += Token(TokenType.AT, word)
         } else if (word.contains("exception") || word.contains("error")) {
           tokenBuf += Token(TokenType.Exception, word)
         } else if (typ == CharType.Dot) {
//           tokenBuf += Token(TokenType.AsciiDot, wordBuf.append(value).toString.toLowerCase)
           tokenBuf += Token(TokenType.AsciiDot, wordBuf.toString.toLowerCase)
         } else {
           tokenBuf += Token(TokenType.Ascii, word)
         }
      }
      case TokenType.NonAscii | TokenType.Num => {
         tokenBuf += Token(currWordType, wordBuf.toString.toLowerCase)
      }
    }
    wordBuf = new StringBuilder
    currWordType = TokenType.Unknown
  }

  def pushChar(typ: CharType, value: Char) = {
    if (currWordType == TokenType.Unknown) {
      updateWordTypeByCharType(typ)
      wordBuf.append(value)
    } else if (isCharTokenSameType(typ, currWordType)) {
      wordBuf.append(value)
    } else {
      pushToken(typ, value)
      updateWordTypeByCharType(typ)
      wordBuf.append(value)
    }
  }

  def parse(): ArrayBuffer[Token] = {
    var index = 0
    var curChar = input.charAt(index)
    while (index < input.length) {
      val char = input.charAt(index)
      char match {
        case ' ' => {
          pushToken(CharType.Space, ' ')
          while (index < input.length && input.charAt(index) == ' ') {
            index += 1
          }
        }
        case '.' => {
          pushToken(CharType.Dot, '.')
          while (index < input.length && input.charAt(index) == '.') {
            index += 1
          }
        }
        case num if (num >= '0' && num <= '9') => {
          pushChar(CharType.Num, num)
          index += 1
        }
        case ascii if (ascii >= Tokenizer.EffectiveAsciiStart && ascii <= Tokenizer.EffectiveAsciiEnd) => {
          pushChar(CharType.Ascii, ascii)
          index += 1
        }
        case x => {
          pushChar(CharType.NonAscii, x)
          index += 1
        }
      }

    }
    //push token in currword
    pushToken(CharType.Eof, ' ')
    // push eof token
    tokenBuf += Token(TokenType.Eof, " ")
    tokenBuf
  }
}
