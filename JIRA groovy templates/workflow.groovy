import com.atlassian.jira.component.ComponentAccessor;

def getAllStatusesFor(issue) {
    ComponentAccessor.workflowManager.getWorkflow(issue).getLinkedStatusObjects()
}

def getAllStatuessIdsFor(issue) {
    getWorkflow(issue).getLinkedStatusIds()
}

def getWorkflow(issue) {
    ComponentAccessor.workflowManager.getWorkflow(issue)
}

def getLinkedActions(issue) {
    ComponentAccessor.workflowManager.getWorkflow(issue).getLinkedStep(issue.status).getActions()
}
