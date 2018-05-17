import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.util.JiraUtils
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl
import com.atlassian.jira.workflow.TransitionOptions

def doTransition(issue, int actionId, user) {
    def issueService = ComponentAccessor.getIssueService()
    def issueInputParameters = issueService.newIssueInputParameters();
    def transitionValidationResult = issueService.validateTransition(user, issue.id, actionId, issueInputParameters);
    if (transitionValidationResult.isValid()) {
        issueService.transition(user, transitionValidationResult);
        return true
    } else {
        return false
    }
}

def doTransition(issue, int actionId, user, boolean skipConditions, boolean skipPermissions, boolean skipValidators){
    def builder = new TransitionOptions.Builder()

    if(skipConditions){
      builder.skipConditions()
    }
    if(skipPermissions){
       builder.skipPermissions()
    }
    if(skipValidators){
       builder.skipValidators()
    }
    def transitionOptions = builder.build()
    def issueService = ComponentAccessor.getIssueService()
    def issueInputParameters = issueService.newIssueInputParameters();
    def transitionValidationResult = issueService.validateTransition(user, issue.id, actionId, issueInputParameters, transitionOptions);
    if (transitionValidationResult.isValid()) {
        issueService.transition(user, transitionValidationResult);
        return true
    } else {
        return false
    }
}

def doTransition(issue, int id, String userName) {
    def workflowTransitionUtil = JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class)
    def params = ["resolution": "1"]
    workflowTransitionUtil.setAction(id)
    workflowTransitionUtil.setIssue(issue)
    workflowTransitionUtil.setUserkey(userName)
    workflowTransitionUtil.setParams(params)
    workflowTransitionUtil.validate()
    workflowTransitionUtil.progress()
}
