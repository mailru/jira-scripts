import com.atlassian.jira.component.ComponentAccessor

def doTransition(issue, int actionId, user){
    def issueService = ComponentAccessor.getIssueService()
    def issueInputParameters = issueService.newIssueInputParameters();
    def transitionValidationResult = issueService.validateTransition(user, issue.id, actionId, issueInputParameters);
    if (transitionValidationResult.isValid()){
       issueService.transition(user, transitionValidationResult);
        return true
    } else {
        return false
    }
}
