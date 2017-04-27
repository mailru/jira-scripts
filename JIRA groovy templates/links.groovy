import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager


def getInwardLinks(issue){
	ComponentAccessor.getIssueLinkManager().getInwardLinks(issue.id);	
}

def getOutwardLinks(issue){
	ComponentAccessor.getIssueLinkManager().getOutwardLinks(issue.id);	
}

def getInwardIssues(issue){
	ComponentAccessor.getIssueLinkManager().getInwardLinks(issue.id)*.getSourceObject()
}

def getOutwardIssues(issue){
	ComponentAccessor.getIssueLinkManager().getOutwardLinks(issue.id)*.getDestinationObject()	
}

def createLink(fromIssue, toIssue, Long issueLinkTypeId, Long sequence, user){
    ComponentAccessor.issueLinkManager.createIssueLink(fromIssue.id, toIssue.id, issueLinkTypeId, sequence, user);
}

def getRemoteIssueLinks(issue){
	def rilm = ComponentAccessor.getComponentOfType(RemoteIssueLinkManager);
	rilm.getRemoteIssueLinksForIssue(issue);
}