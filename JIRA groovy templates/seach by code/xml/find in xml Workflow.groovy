import com.atlassian.jira.component.ComponentAccessor

def findText = "test"
new FinderWorkflow().find(findText, true, true)

class FinderWorkflow {

    def find(String findText, boolean findOnlyActiveWorkflow, boolean ignoreCase) {
        def foundWorkflows = [:]
        def wfs = ComponentAccessor.workflowManager.getWorkflows()//.findAll{it.name == "SPECIFIC WF NAME"}
        if (findOnlyActiveWorkflow) {
            wfs = wfs.findAll { it.isActive() }
        }
        for (wf in wfs) {
            def findInWorkflow = findInWorkflow(wf, findText, ignoreCase)
            if (findInWorkflow) {
                foundWorkflows << [(wf.name): findInWorkflow]
            }
        }
        return foundWorkflows
    }

    def findInWorkflow(wf, String findText, boolean ignoreCase) {
        def finds = []
        for (action in workflowToJSON(wf).actions) {
            for (function in action.conditions) {
                for (arg in function.args.entrySet()) {
                    if (conditionOnFind(arg, findText, ignoreCase)) {
                        finds << convertFindInfoToJSON(wf, action, function, "condition", arg)
                    }
                }
            }
            for (function in action.validators) {
                for (arg in function.args.entrySet()) {
                    if (conditionOnFind(arg, findText, ignoreCase)) {
                        finds << convertFindInfoToJSON(wf, action, function, "validator", arg)
                    }
                }
            }
            for (function in action.functions) {
                for (arg in function.args.entrySet()) {
                    if (conditionOnFind(arg, findText, ignoreCase)) {
                        finds << convertFindInfoToJSON(wf, action, function, "post-function", arg)
                    }
                }
            }
        }
        return finds
    }

    def conditionOnFind(Map.Entry arg, String findText, boolean ignoreCase) {
        if (ignoreCase) {
            return arg.value.toLowerCase().contains(findText.toLowerCase()) ||
                    decodeStringFromBase64(arg.value).toLowerCase().contains(findText.toLowerCase())
        }
        return arg.value.contains(findText) || decodeStringFromBase64(arg.value).contains(findText)
    }

    def decodeStringFromBase64(String string) {
        String decodedString = string
        try {
            if (string.startsWith('b64_')) {
                string = string.replace('b64_', '')
            }
            decodedString = new String(Base64.getDecoder().decode(string));
        } catch (IllegalArgumentException e) {
            //ignore
        }
        return decodedString

    }

    def convertFindInfoToJSON(wf, action, function, functionType, arg) {
        return [workflowName: wf.name,
                actionName  : action.name,
                actionId    : action.id,
                functionType: functionType,
                class       : function.args['class.name'],
                argKey      : arg.key,
                argValue    : arg.value]
    }

    def workflowToJSON(jiraWorkflow) {
        def xml = jiraWorkflow.descriptor.asXML()
        def xmlWorkflow = new XmlSlurper().parseText(xml)

        def actions = []

        actions.addAll(xmlWorkflow.'initial-actions'.action)
        actions.addAll(xmlWorkflow.'global-actions'.action)
        actions.addAll(xmlWorkflow.'common-actions'.action)
        actions.addAll(xmlWorkflow.'steps'.'step'.'actions'.'action')
        actions.addAll(xmlWorkflow.'steps'.'step'.'actions'.'common-action'.'action')

        def workflow = [name    : jiraWorkflow.name,
                        isActive: jiraWorkflow.isActive(),
                        actions : actions.collect {
                            [name       : it.@name.toString(),
                             id         : it.@id.toString(),
                             conditions : getAllConditions(it.'restrict-to'.'conditions').collect { condition ->
                                 functionToJSON(condition)
                             },
                             validators : it.validators.validator.collect { validator ->
                                 functionToJSON(validator)
                             },
                             'functions': it.results.'unconditional-result'.'post-functions'.function.collect { function ->
                                 functionToJSON(function)
                             }
                            ]
                        },
        ]
        return workflow
    }


    def getAllConditions(conditions) {
        def listConditions = []
        listConditions.addAll(conditions.'condition')
        if (conditions.'conditions'.size() != 0) {
            listConditions.addAll(getAllConditions(conditions.'conditions'))
        }
        return listConditions
    }

    def functionToJSON(xmlFunction) {
        def args = [:]
        xmlFunction.arg.each { arg ->
            args << [(arg.@name.toString()): arg.toString()]
        }
        return [type: xmlFunction.@type.toString(),
                args: args,
        ]
    }

}
