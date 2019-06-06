/**
 - Access variables in scripts - httpClient, templateEngine, log, issue, currentUser, runAsUser, cfValues (key - id as Long/cfName as String)

 - run as User - default currentUser
 */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.JiraUtils
import com.atlassian.jira.workflow.TransitionOptions
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl
import com.opensymphony.workflow.spi.WorkflowEntry
import org.ofbiz.core.entity.DelegatorInterface
import org.ofbiz.core.entity.GenericValue
import ru.mail.jira.plugins.groovy.api.script.*

@WithParam(displayName = 'issueKey', type = ParamType.STRING)
String issueKey
issue = getIssue(issueKey)

@WithParam(displayName = 'Condition', type = ParamType.SCRIPT, optional = true)
ScriptParam scriptCondition

@WithParam(displayName = 'Action id', type = ParamType.LONG)
Long actionId

@WithParam(displayName = 'Additional issue actions', type = ParamType.SCRIPT, optional = true)
ScriptParam additionalIssueActions

@WithParam(displayName = 'Skip Permissions', type = ParamType.BOOLEAN, optional = true)
Boolean skipPermissions

@WithParam(displayName = 'Skip Validators', type = ParamType.BOOLEAN, optional = true)
Boolean skipValidators

@WithParam(displayName = 'Skip Conditions', type = ParamType.BOOLEAN, optional = true)
Boolean skipConditions

@WithParam(displayName = 'run as User', type = ParamType.USER, optional = true)
ApplicationUser runAsUser

skipPermissions = skipPermissions ?: false
skipValidators = skipValidators ?: false
skipConditions = skipConditions ?: false
runAsUser = runAsUser ?: currentUser

def cfValues = new CfValues(issue)

def paramMap = [httpClient    : httpClient,
                templateEngine: templateEngine,
                log           : log,
                issue         : issue,
                runAsUser     : runAsUser,
                currentUser   : currentUser,
                cfValues      : cfValues,
]

if (scriptCondition && (scriptCondition.runScript(paramMap) == null || scriptCondition.runScript(paramMap) as Boolean == false)) {
    return
}

def result = doTransition(issue, actionId as int, runAsUser, skipConditions, skipPermissions, skipValidators)
if (result) {
    if (additionalIssueActions) {
        additionalIssueActions.runScript(paramMap)
    }
}

def doTransition(issue, int actionId, user, boolean skipConditions, boolean skipPermissions, boolean skipValidators) {
    DelegatorInterface gd = (DelegatorInterface) ComponentAccessor.getComponent(DelegatorInterface)
    GenericValue gv = gd.findByPrimaryKey("OSWorkflowEntry", ["id": issue.getWorkflowId()])
    if (!gv) {
        return
    }

    if (gv.get("state") == WorkflowEntry.CREATED) {
        gv.set("state", WorkflowEntry.ACTIVATED)
        gv.store()
    }

    def builder = new TransitionOptions.Builder()
    if (skipConditions) {
        builder.skipConditions()
    }
    if (skipPermissions) {
        builder.skipPermissions()
    }
    if (skipValidators) {
        builder.skipValidators()
    }
    def transitionOptions = builder.build()

    def workflowTransitionUtil = JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class)
    def params = getCurrentParamsForAction(issue, actionId)
    transitionOptions.getWorkflowParams().each { k, v ->
        workflowTransitionUtil.addAdditionalInput(k, v)
    }
    workflowTransitionUtil.setAction(actionId)
    workflowTransitionUtil.setIssue(issue)
    workflowTransitionUtil.setUserkey(user.name)
    workflowTransitionUtil.setParams(params)
    def errors = [validate: workflowTransitionUtil.validate()]
    if (errors.validate.hasAnyErrors()) {
        log.error("Fast Track Transition ${issue} errors.validate ${errors.validate}")
    }
    errors << [progress: workflowTransitionUtil.progress()]
    if (errors.progress.hasAnyErrors()) {
        log.error("Fast Track Transition ${issue} errors.progress ${errors.validate}")
    }
}

class CfValues {
    private issue
    private customFieldManager = ComponentAccessor.customFieldManager

    CfValues(issue) {
        this.issue = issue
    }

    def getAt(String fieldName) {
        def cfs = customFieldManager.getCustomFieldObjectsByName(fieldName)
        def value
        if (cfs) {
            for (def cf in cfs) {
                value = getCustomFieldValue(cf)
                if (value) {
                    return value
                }
            }
        }
    }

    def getAt(Long fieldId) {
        def cf = customFieldManager.getCustomFieldObject(fieldId)
        if (cf) {
            return getCustomFieldValue(cf)
        }
    }

    def getCustomFieldValue(cf) {
        def value = this.issue.getCustomFieldValue(cf)
        if (value && value instanceof Collection) {
            value = value.sort()
        }
        return value
    }
}

def getCurrentParamsForAction(issue, int actionId) {
    def fields = getFieldsFromTranstion(issue, actionId)
    def params = fields.collectEntries { fieldName ->
        [(fieldName): getFieldValueByFieldName(issue, fieldName)]
    }
    return params
}

def getFieldValueByFieldName(issue, fieldName) {
    if (fieldName.contains('customfield')) {
        def cf = getCustomFieldObject(fieldName)
        if (!cf) {
            return
        }
        return issue.getCustomFieldValue(cf)
    } else if (fieldName == 'priority') {
        return issue[fieldName].id
    } else if (fieldName in ['assignee', 'reporter']) {
        return issue[fieldName] ? issue[fieldName].name : null
    } else {
        return issue[fieldName]
    }
}

def getCustomFieldObject(String fieldName) {
    return ComponentAccessor.customFieldManager.getCustomFieldObject(fieldName)
}

def getFieldsFromTranstion(issue, int actionId) {
    String screenIdString = ComponentAccessor.workflowManager.getActionDescriptor(issue, actionId).getMetaAttributes()['jira.fieldscreen.id']
    Long screenId = screenIdString ? screenIdString as Long : null
    def fields = []
    if (screenId) {
        def screen = getScreen(screenId)
        if (screen) {
            fields = getFieldsFromScreen(screen)
        }
    }
    if (fields.contains('timetracking')) {
        fields.remove('timetracking')
        fields.add('originalEstimate')
        fields.add('estimate')
    }
    return fields
}

def getFieldsFromScreen(screen) {
    screen.getTabs()*.getFieldScreenLayoutItems().flatten()*.getFieldId() ?: []
}

def getScreen(Long id) {
    ComponentAccessor.fieldScreenManager.getFieldScreen(id)
}

def getWorkflow(issue) {
    ComponentAccessor.workflowManager.getWorkflow(issue)
}

def getIssue(String key) {
    ComponentAccessor.issueManager.getIssueObject(key)
}