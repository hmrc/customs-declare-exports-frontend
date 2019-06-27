package utils

import scala.io.Source

object FileUtil {

  def read(path: String): List[String] = {
    val source = Source.fromURL(getClass.getClassLoader.getResource(path), "UTF-8")
    try {
      source.getLines().toList
    } finally {
      source.close()
    }
  }

}
