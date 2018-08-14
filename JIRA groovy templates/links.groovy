import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.link.RemoteIssueLinkManager

def getInwardLinks(issue) {
    ComponentAccessor.getIssueLinkManager().getInwardLinks(issue.id);
}

def getOutwardLinks(issue) {
    ComponentAccessor.getIssueLinkManager().getOutwardLinks(issue.id);
}

def getAllLinkedIssues(issue) {
    def allLinkedIssue = [] as Set
    allLinkedIssue.addAll(getInwardIssues(issue))
    allLinkedIssue.addAll(getOutwardIssues(issue))
    return allLinkedIssue
}

def getInwardIssues(issue) {
    ComponentAccessor.getIssueLinkManager().getInwardLinks(issue.id)*.getSourceObject()
}

def getOutwardIssues(issue) {
    ComponentAccessor.getIssueLinkManager().getOutwardLinks(issue.id)*.getDestinationObject()
}

def getAllLinkedIssues(issue, long linkTypeId) {
    def allLinkedIssue = [] as Set
    allLinkedIssue.addAll(getInwardIssues(issue, linkTypeId))
    allLinkedIssue.addAll(getOutwardIssues(issue, linkTypeId))
    return allLinkedIssue
}

def getInwardIssues(issue, long linkTypeId) {
    ComponentAccessor.getIssueLinkManager().getInwardLinks(issue.id).findAll {
        it.getLinkTypeId() == linkTypeId
    }*.getSourceObject()
}

def getOutwardIssues(issue, long linkTypeId) {
    ComponentAccessor.getIssueLinkManager().getOutwardLinks(issue.id).findAll {
        it.getLinkTypeId() == linkTypeId
    }*.getDestinationObject()
}

def createLink(fromIssue, toIssue, Long issueLinkTypeId, Long sequence, user) {
    ComponentAccessor.issueLinkManager.createIssueLink(fromIssue.id, toIssue.id, issueLinkTypeId, sequence, user);
}

def getRemoteIssueLinks(issue) {
    def rilm = ComponentAccessor.getComponentOfType(RemoteIssueLinkManager);
    rilm.getRemoteIssueLinksForIssue(issue);
}

def removeIssueLink(long issueLinkId){
  ComponentAccessor.getIssueLinkManager().with{
    	def issueLink = getIssueLink(issueLinkId)
      if(issueLink){
        removeIssueLink(issueLink, currentUser)
      }
  }
}
