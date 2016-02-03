//contians all the common classes used by client and server
package chordLib
import scala.util.Random
import scala.io.Source
  

  class Utilities
  {


  def getId(str: String):BigInt=
    {
      val id=toHex(sha1(str))
        id
    }

    def sha1(s: String): String = 
    {
 
    var str=""
    val m = java.security.MessageDigest.getInstance("SHA-1").digest(s.getBytes("UTF-8"))
   
    m.map("%02x".format(_)).mkString
    }


     def toHex(hex: String): BigInt = 
     {
        hex.toLowerCase().toList.map(
          "0123456789abcdef".indexOf(_)).map(
          BigInt(_)).reduceLeft( _ * 16 + _)

    }

    def getFile(index:Int): String=
    {
      val dictionary = Source.fromFile("dictionary.txt").getLines.toList
       if(index < 0)
       {
       val random = new Random

         
         val file = (dictionary(random.nextInt(dictionary.length))).trim
         return file
       }
       else
       {
          val file = (dictionary(index)).trim


         return file
       }
       


    }


}