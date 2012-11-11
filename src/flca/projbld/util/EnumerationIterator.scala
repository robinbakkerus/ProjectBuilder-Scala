package flca.projbld.util

class EnumerationIterator[A](e: java.util.Enumeration[A])
  extends Iterator[A] {
  def hasNext = e.hasMoreElements
  def next() = e.nextElement()
} 
