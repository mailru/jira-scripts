def xml = ""
def list = new XmlSlurper().parseText(xml)
list.getAt('Row').text()