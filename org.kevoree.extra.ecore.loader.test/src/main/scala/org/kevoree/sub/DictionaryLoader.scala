package org.kevoree.sub;

import xml.NodeSeq
import scala.collection.JavaConversions._
import org.kevoree._

trait DictionaryLoader extends DictionaryValueLoader {

		def loadDictionary(parentId: String, parentNode: NodeSeq, refNameInParent : String) : List[Dictionary] = {
				var loadedElements = List[Dictionary]()
				var i = 0
				val dictionaryList = (parentNode \\ refNameInParent)
				dictionaryList.foreach { xmiElem =>
						val currentElementId = parentId + "/@" + refNameInParent + "." + i
						loadedElements = loadedElements ++ List(loadDictionaryElement(currentElementId,xmiElem))
						i += 1
				}
				loadedElements
		}

		def loadDictionaryElement(elementId: String, elementNode: NodeSeq) : Dictionary = {
		
				val modelElem = KevoreePackage.createDictionary
				ContainerRootLoadContext.map += elementId -> modelElem

				val values = loadDictionaryValue(elementId, elementNode, "values")
				modelElem.setValues(values)
				values.foreach{ e => e.eContainer = modelElem }

				modelElem
		}

		def resolveDictionary(parentId: String, parentNode: NodeSeq, refNameInParent : String) {
				var i = 0
				val dictionaryList = (parentNode \\ refNameInParent)
				dictionaryList.foreach { xmiElem =>
						val currentElementId = parentId + "/@" + refNameInParent + "." + i
						resolveDictionaryElement(currentElementId,xmiElem)
						i += 1
				}
		}

		def resolveDictionaryElement(elementId: String, elementNode: NodeSeq) {

		val modelElem = ContainerRootLoadContext.map(elementId).asInstanceOf[Dictionary]


				(elementNode \ "@values").headOption match {
						case Some(head) => {
								head.text.split(" ").foreach {
										xmiRef =>
												ContainerRootLoadContext.map.get(xmiRef) match {
														case Some(s: DictionaryValue) => modelElem.addValues(s)
														case None => System.out.println("DictionaryValue not found in map ! xmiRef:" + xmiRef)
												}
										}
								}
						case None => //No subtype for this library
				}
		}

}
