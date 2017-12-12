import groovy.json.JsonSlurper
import groovy.json.JsonOutput


def parseText(text){
    def jsonSlurper = new JsonSlurper()
	return jsonSlurper.parseText(text)
}

def toJson(obj){
    JsonOutput.toJson(obj)
}
