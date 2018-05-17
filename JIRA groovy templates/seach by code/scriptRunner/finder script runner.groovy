/**
 run from ScriptRunner console
 */

import com.onresolve.scriptrunner.canned.jira.admin.ScriptRegistry

def TEXT_FIND = """test"""
boolean IGNORE_CASE = true

mapScriptAddresses = [:]
initScriptAddresses(mapScriptAddresses)

return findCodeAddressesByFragment(TEXT_FIND, IGNORE_CASE)

//показывает код и где он расположен
def findCodeByFragment(String fragment) {
    def codes = mapScriptAddresses.keySet().findAll { code ->
        code.contains(fragment)
    }
    def addresses = []
    return mapScriptAddresses.findAll { k, v ->
        return k in codes
    }
}

//показывает только расположение кода
def findCodeAddressesByFragment(String fragment, boolean ignoreCase) {
    def codes = mapScriptAddresses.keySet().findAll { code ->
        if (ignoreCase) {
            code.toLowerCase().contains(fragment.toLowerCase())
        } else {
            code.contains(fragment)
        }
    }
    def addresses = []
    codes.each {
        addresses << mapScriptAddresses.get(it)
    }
    return addresses
}


def initScriptAddresses(mapScriptAddresses) {
    def registry = new ScriptRegistry()

    registry.getScriptListenersInfo().each { info ->
        def code = getCode(info)
        def addresses = mapScriptAddresses.get(code) ?: []
        addresses << "listener - ${info.name}<br>"
        mapScriptAddresses.put(code, addresses)
    }

    registry.getScriptFieldsInfo().each { info ->
        def code = getCode(info)
        def addresses = mapScriptAddresses.get(code) ?: []
        addresses << "ScriptField - ${info.name}<br>"
        mapScriptAddresses.put(code, addresses)
    }

    registry.getRESTEndpointsInfo().each { info ->
        def code = getCode(info)
        def addresses = mapScriptAddresses.get(code) ?: []
        addresses << "RESTEndpointsInfo - ${info.name}<br>"
        mapScriptAddresses.put(code, addresses)
    }

    registry.getWorkflowsInfo().each { wf ->
        if(!(wf.isActive())){
            return
        }
        wf.getTransitions().each { transition ->
            transition.getScripts().each { info ->
                def code = getCode(info)
                def addresses = mapScriptAddresses.get(code) ?: []
                addresses << "workflow ${wf.name}, is active ${wf.isActive()}, transition <a href='${transition.getHref()}'>${transition.name}</a>, ${info.name}<br>"
                mapScriptAddresses.put(code, addresses)
            }
        }
    }

    registry.getBehavioursInfo().each { k, infos ->
        infos.each { info ->
            def code = getCode(info)
            def addresses = mapScriptAddresses.get(code) ?: []
            addresses << "Behaviours - ${k} ${info.name}<br>"
            mapScriptAddresses.put(code, addresses)
        }
    }
    return mapScriptAddresses
}


def getCode(info) {
    def code = info.getInlineScript() ?: ""
    code += info.getConditionScript()?.getInlineScript()
    code += info.getAdditionalScript()?.getInlineScript()
    return code
}
