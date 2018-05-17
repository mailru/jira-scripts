import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.IssueFactory

def getIssue(String key) {
    ComponentAccessor.issueManager.getIssueObject(key)
}

def update(user, issue) {
    ComponentAccessor.issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

def getIssue(Long id) {
    ComponentAccessor.issueManager.getIssueObject(id)
}

def createIssue(user) {
    def mutableIssue = createMutableIssue(user)
    ComponentAccessor.issueManager.createIssueObject(user, mutableIssue)
}

def createMutableIssue(user) {
    def issueFactory = ComponentAccessor.getComponentOfType(IssueFactory.class)
    def newIssue = issueFactory.getIssue()

    newIssue.projectId = 10001
    newIssue.issueTypeId = "1"
    newIssue.summary = ""
    newIssue.reporterId = ""
    newIssue.assigneeId = ""
    newIssue.priorityId = "3"
    newIssue.resolutionId = ""
    newIssue.description = ""

    return newIssue
}

def createSubIssue(perrentIssue, user) {
    def mutableSubIssue = createMutableIssue()
    mutableSubIssue.setParentObject(perrentIssue)
    ComponentAccessor.issueManager.createIssueObject(user, mutableSubIssue)
    ComponentAccessor.subTaskManager.createSubTaskIssueLink(perrentIssue, mutableSubIssue, user)
}
