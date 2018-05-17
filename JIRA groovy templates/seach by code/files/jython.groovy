def TEXT_FIND = """test"""
boolean IGNORE_CASE = true

def allScrips = Const.paths.collect{path->
  new File(path)?.listFiles()
}.flatten().findAll{
  it.isFile()
}

allScrips.findAll{script->
    if(IGNORE_CASE){
        return script.text.toLowerCase().contains(TEXT_FIND.toLowerCase())
    } else {
        return script.text.contains(TEXT_FIND)
    }
}*.name


class Const{
    static def paths = [
      '/data/jira-sharedhome/jss/jython/sys',
      '/data/jira-sharedhome/jss/jython/rest',
      '/data/jira-sharedhome/jss/jython/listener',
      '/data/jira-sharedhome/jss/jython/workflow',
    ]
}
