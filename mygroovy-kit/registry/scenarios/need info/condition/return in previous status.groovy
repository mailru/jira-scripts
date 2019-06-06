import com.atlassian.jira.component.ComponentAccessor

def cfIdTransition = 10000

def actionId = transientVars.get('actionId')
if (!actionId) {
    return false
}
return issue.getCustomFieldValue(getCustomFieldObject(cfIdTransition)) == actionId as Double

def getCustomFieldObject(Long id) {
    ComponentAccessor.customFieldManager.getCustomFieldObject(id)
}

