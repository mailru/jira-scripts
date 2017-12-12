from com.atlassian.jira.component import ComponentAccessor

issueFactory = ComponentAccessor.getIssueFactory()
issueManager = ComponentAccessor.getIssueManager()
issueLinkManager = ComponentAccessor.getIssueLinkManager()
customFieldManager = ComponentAccessor.getCustomFieldManager()

actor = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
secLevelId = issue.getSecurityLevelId()

# object for clone
cloneIssue = issueFactory.getIssue()
cloneIssue.setProjectId(10201)
cloneIssue.setIssueTypeId('9')
cloneIssue.setSummary(issue.getSummary())
cloneIssue.setDescription(issue.getDescription())
cloneIssue.setReporter(actor)
if secLevelId:
  cloneIssue.setSecurityLevelId(secLevelId)
customFields = customFieldManager.getCustomFieldObjects(issue)
for cf in customFields:
  cfValue = issue.getCustomFieldValue(cf)
  if cfValue:
    cloneIssue.setCustomFieldValue(cf, cfValue)

#create
issueManager.createIssueObject(actor, cloneIssue)
issueLinkManager.createIssueLink(issue.getId(),newIssue.getId(),10003,1,actor)