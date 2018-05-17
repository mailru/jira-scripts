import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def parseText(text) {
    def jsonSlurper = new JsonSlurper()
    return jsonSlurper.parseText(text)
}

def toJson(obj) {
    JsonOutput.toJson(obj)
}
