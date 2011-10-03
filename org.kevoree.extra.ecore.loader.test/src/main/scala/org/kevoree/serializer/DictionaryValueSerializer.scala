package org.kevoree.serializer
import org.kevoree._
trait DictionaryValueSerializer 
{
def getDictionaryValueXmiAddr(selfObject : DictionaryValue,previousAddr : String): Map[Object,String] = {
var subResult = Map[Object,String]()
subResult
}
def DictionaryValuetoXmi(selfObject : DictionaryValue,refNameInParent : String, addrs : Map[Object,String]) : scala.xml.Node = {
new scala.xml.Node {
  def label = refNameInParent
    def child = {        
       var subresult: List[scala.xml.Node] = List()  
      subresult    
    }              
override def attributes  : scala.xml.MetaData =  { 
new scala.xml.UnprefixedAttribute("value",selfObject.getValue.toString,new scala.xml.UnprefixedAttribute("attribute",addrs.get(selfObject.getAttribute).getOrElse{"wtf"},scala.xml.Null))}
  }                                                  
}
}
