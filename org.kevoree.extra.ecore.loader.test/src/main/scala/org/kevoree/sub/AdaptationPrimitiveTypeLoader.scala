package org.kevoree.sub;

import xml.NodeSeq
import scala.collection.JavaConversions._
import org.kevoree._

trait AdaptationPrimitiveTypeLoader{

		def loadAdaptationPrimitiveType(parentId: String, parentNode: NodeSeq, refNameInParent : String) : List[AdaptationPrimitiveType] = {
				var loadedElements = List[AdaptationPrimitiveType]()
				var i = 0
				val adaptationPrimitiveTypeList = (parentNode \\ refNameInParent)
				adaptationPrimitiveTypeList.foreach { xmiElem =>
						val currentElementId = parentId + "/@" + refNameInParent + "." + i
						loadedElements = loadedElements ++ List(loadAdaptationPrimitiveTypeElement(currentElementId,xmiElem))
						i += 1
				}
				loadedElements
		}

		def loadAdaptationPrimitiveTypeElement(elementId: String, elementNode: NodeSeq) : AdaptationPrimitiveType = {
		
				val modelElem = KevoreePackage.createAdaptationPrimitiveType
				ContainerRootLoadContext.map += elementId -> modelElem

				modelElem
		}

		def resolveAdaptationPrimitiveType(parentId: String, parentNode: NodeSeq, refNameInParent : String) {
				var i = 0
				val adaptationPrimitiveTypeList = (parentNode \\ refNameInParent)
				adaptationPrimitiveTypeList.foreach { xmiElem =>
						val currentElementId = parentId + "/@" + refNameInParent + "." + i
						resolveAdaptationPrimitiveTypeElement(currentElementId,xmiElem)
						i += 1
				}
		}

		def resolveAdaptationPrimitiveTypeElement(elementId: String, elementNode: NodeSeq) {

		val modelElem = ContainerRootLoadContext.map(elementId).asInstanceOf[AdaptationPrimitiveType]

		val nameVal = (elementNode \\ "@name").text
		if(!nameVal.equals("")){
				modelElem.setName(java.lang.String.valueOf(nameVal))
		}


		}

}
