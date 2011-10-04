package org.kevoree;

/**
 * Created by Ecore Model Generator.
 * @author: Gregory NAIN
 * Date: 04 oct. 11 Time: 09:49
 * Meta-Model:NS_URI=http://kevoree/1.0
 */
trait DictionaryValue extends KevoreeContainer {
		private var value : java.lang.String = ""

		private var attribute : DictionaryAttribute = _


		def getValue : java.lang.String = {
				value
		}

		def setValue(value : java.lang.String) {
				this.value = value
		}

		def getAttribute : DictionaryAttribute = {
				attribute
		}

		def setAttribute(attribute : DictionaryAttribute ) {
				this.attribute = attribute
		}

}
