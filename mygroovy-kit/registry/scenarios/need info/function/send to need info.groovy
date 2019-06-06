/**
 * Init map - the script should return a map with reverse transitions from the status "need info"
 example:
    return ["status id":123]

 Variables are available in scripts - httpClient, templateEngine, log, issue, currentUser, transientVars, componentAccessor,
 */
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.event.type.EventDispatchOption
import ru.mail.jira.plugins.groovy.api.script.ParamType
import ru.mail.jira.plugins.groovy.api.script.WithParam

@WithParam(displayName = 'Init map', type = ParamType.SCRIPT)
ScriptParam scriptInitMap

def paramMap = [httpClient       : httpClient,
                templateEngine   : templateEngine,
                log              : log,
                issue            : issue,
                currentUser      : currentUser,
                transientVars    : transientVars,
                componentAccessor: ComponentAccessor
]

def statusMapTransitionThisStatus = scriptInitMap.runScript(paramMap)

def cfIdTransition = 10000

updateCustomFieldValue(issue, cfIdTransition, statusMapTransitionThisStatus.get(issue.status.id) as Double)

def updateCustomFieldValue(issue, Long customFieldId, newValue){
    def customField = ComponentAccessor.customFieldManager.getCustomFieldObject(customFieldId)
    customField.updateValue(null, issue, new ModifiedValue(customField.getValue(issue), newValue), new DefaultIssueChangeHolder());
    return issue
}


def getIssue(String key){
    ComponentAccessor.issueManager.getIssueObject(key)
}

def getCustomFieldObject(Long id){
    ComponentAccessor.customFieldManager.getCustomFieldObject(id)
}

