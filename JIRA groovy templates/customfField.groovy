enableCache = {-> false}
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.event.type.EventDispatchOption

def updateCustomFieldValue(issue, Long customFieldId, newValue){
    def customField = ComponentAccessor.customFieldManager.getCustomFieldObject(customFieldId)
	customField.updateValue(null, issue, new ModifiedValue(customField.getValue(issue), newValue), new DefaultIssueChangeHolder());
    return issue
}

def updateCustomFieldValue(issue, Long customFieldId, newValue, user){
	def customField = ComponentAccessor.customFieldManager.getCustomFieldObject(customFieldId)
    issue.setCustomFieldValue(customField, newValue)
    ComponentAccessor.issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

def getCustomFieldObject(Long fieldId){
    ComponentAccessor.customFieldManager.getCustomFieldObject(fieldId)
}

def getCustomFieldValue(issue, Long fieldId){
    ComponentAccessor.customFieldManager.getCustomFieldObject(fieldId)?.getValue(issue)
}