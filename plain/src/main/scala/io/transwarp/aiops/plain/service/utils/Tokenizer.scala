package io.transwarp.aiops.plain.service.utils

import io.transwarp.aiops.plain.service.utils.CharType.CharType
import io.transwarp.aiops.plain.service.utils.TokenType.TokenType

class Tokenizer(input: String) {
  private val array: Array[Char] = input.toCharArray
  private var pos: Int = _
  private var nextToken: Token = Token(TokenType.UNKNOWN, null)

  def hasNext: Boolean = {
    if (pos < array.length) {
      val charType = matchChar
      val startPos = pos
      pos += 1
      var break = false
      while (!break && pos < array.length) {
        if (matchChar == charType) {
          pos += 1
        } else break = true
      }

      nextToken = charType match {
        case CharType.SPACE => Token(TokenType.SPACE, " ")
        case CharType.LF => Token(TokenType.LF, "\n")
        case CharType.WORD => wordToken(startPos, pos)
        case CharType.NUMBER => Token(TokenType.NUMBER, new String(array, startPos, pos - startPos))
        case CharType.ASCII => Token(TokenType.ASCII, new String(array, startPos, pos - startPos))
        case CharType.NON_ASCII => Token(TokenType.NON_ASCII, new String(array, startPos, pos - startPos))
        case _ => Token(TokenType.UNKNOWN, null)
      }
      true
    } else if (nextToken.typ != TokenType.EOF) {
      nextToken = Token(TokenType.EOF, null)
      true
    } else false
  }

  def next: Token = nextToken

  private def wordToken(startPos: Int, endPos: Int): Token = {
    val curString = new String(array, startPos, endPos - startPos)
    curString match {
      case "at" => Token(TokenType.AT, curString)
      case "Caused" =>
        if ((hasNext && nextToken.typ == TokenType.SPACE)
          && (hasNext && nextToken.typ == TokenType.WORD && nextToken.value == "by")
          && (hasNext && nextToken.typ == TokenType.ASCII && nextToken.value == ":")) {
          Token(TokenType.CAUSED_BY, "Caused by") // this will omit ":"
        } else {
          pos = endPos
          Token(TokenType.WORD, curString)
        }
      case s if s.contains("Exception") || s.contains("Error") =>
        if (hasNext && nextToken.typ == TokenType.ASCII && (nextToken.value == ":" || nextToken.value == "):")) {
          Token(TokenType.EXCEPTION, curString) // this will omit ":" or "):"
        } else {
          pos = endPos
          Token(TokenType.WORD, curString)
        }
      case _ => Token(TokenType.WORD, curString)
    }
  }

  private def matchChar: CharType =
    array(pos) match {
      case ' ' | '\t' => CharType.SPACE
      case '\n' => CharType.LF
      case c if ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') => CharType.WORD
      case c if '0' <= c && c <= '9' => CharType.NUMBER
      case c if Tokenizer.EffectiveAsciiStart <= c && c <= Tokenizer.EffectiveAsciiEnd => CharType.ASCII
      case _ => CharType.NON_ASCII
    }
}

object Tokenizer {
  val EffectiveAsciiStart = 33
  val EffectiveAsciiEnd = 126
}

case class Token(typ: TokenType, value: String)

object TokenType extends Enumeration {
  type TokenType = Value
  val UNKNOWN = Value
  val EOF = Value
  val SPACE = Value // whitespace tab
  val LF = Value // \n
  val NUMBER = Value // 0-9
  val WORD = Value // a-zA-Z
  val ASCII = Value
  val NON_ASCII = Value
  // Special token
  val AT = Value // at
  val CAUSED_BY = Value // Caused by:
  val EXCEPTION = Value // Exception: | Exception): | Error: | Error):
}

object CharType extends Enumeration {
  type CharType = Value
  val UNKNOWN = Value
  val SPACE = Value
  val LF = Value
  val NUMBER = Value
  val WORD = Value
  val ASCII = Value
  val NON_ASCII = Value
}
