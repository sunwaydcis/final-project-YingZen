package persistence

import java.io.{BufferedWriter, FileWriter, File}
import scala.io.Source
import scala.util.Try

// ai-assisted: #10
// why: Generic file-based data store managing entity loading and persistence using private file references.
class CSVDataStore[T](filePath: String)(using serializer: CSVSerializer[T]) {
  
  // Private file reference to safeguard direct access to raw persistence source path from outside the class.
  private val storeFile = new File(filePath)
  
  def loadAll(): Try[List[T]] = Try {
    if (!storeFile.exists()) {
      List.empty[T]
    } else {
      val fileSource = Source.fromFile(storeFile)
      try {
        val lines = fileSource.getLines().toList
        lines.map { line =>
          serializer.fromCSV(line).get
        }
      } finally {
        fileSource.close()
      }
    }
  }
  
  def saveAll(items: List[T]): Try[Unit] = Try {
    val parentDir = storeFile.getParentFile
    if (parentDir != null && !parentDir.exists()) {
      parentDir.mkdirs()
    }
    
    val writer = new BufferedWriter(new FileWriter(storeFile))
    try {
      items.foreach { item =>
        writer.write(serializer.toCSV(item))
        writer.newLine()
      }
    } finally {
      writer.close()
    }
  }
}
