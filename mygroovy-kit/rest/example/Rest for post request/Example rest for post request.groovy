
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserManager
import com.atlassian.jira.web.bean.PagerFilter
//import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import org.apache.http.client.utils.URLEncodedUtils

import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import java.nio.charset.StandardCharsets

//@BaseScript CustomEndpointDelegate delegate



queryParams = uriInfo.getQueryParameters(true)
//log.warn("queryParams")
def statusIdReview = "10116"
def statusIdPreRelease = "13792"
def statusIdTODO = "10147"
def statusIdReadyForWork = "11092"
def statusIdBug = "13295"
def statusIdWork = "3"
def statusIdAwaitingForCheck = "10005"
def statusIdTesting = "10009"

def cfIdBranchesFromStash = 41300;
def cfIdBranch = 23100;

def secretProject = ["TEST": "TEST",]

def projectKey = secretProject.get(queryParams.getFirst("test"))
if (!projectKey) {
    log.warn("projectKey = null ${queryParams.getFirst('test')}")
    return Response.status(403).build()

}
//log.warn('secret is valid')
//return
Map<String, String> params = URLEncodedUtils.parse(body, StandardCharsets.UTF_8).collectEntries {
    [it.name, it.value]
}
IssueManager issueManager = ComponentAccessor.issueManager
CommentManager commentManager = ComponentAccessor.commentManager
UserManager userManager = ComponentAccessor.userManager

ApplicationUser test = getTest()
def jql = "Project='${projectKey}' and (cf[23100] ~ '${params["PULL_REQUEST_FROM_BRANCH"]}' or cf[41300] ~ '${params["PULL_REQUEST_FROM_BRANCH"]}') and status not in (Closed, Done)"
log.warn("jql = ${jql}")
def issues = _.getIssuesByJQL(jql, test)
log.warn("${queryParams.getFirst('secret')} ${issues}")
String action = params["PULL_REQUEST_ACTION"]
String user = params["PULL_REQUEST_USER_NAME"]


def appUser = ComponentAccessor.userManager.getUserByName(user)?.displayName?:user
log.warn("action = ${action} user = ${appUser}")
for (Issue issue in issues) {
    log.warn("issue = ${issue}")
    if (action == "APPROVED") {
        log.warn("${issue} action = ${action}")
        if(issue.projectObject.key != "NEWS"){
            commentManager.create(issue, test, "Approved by " + appUser, true)
        }
        def approvers = params["PULL_REQUEST_REVIEWERS_ID"].split(",")
        log.warn("${issue} approvers = ${approvers}")
        def approvedCount = params["PULL_REQUEST_REVIEWERS_APPROVED_COUNT"].toInteger()
        log.warn("${issue} approvers.length = ${approvers.length} approvedCount = ${approvedCount}")
        if (approvers.length == approvedCount) {
            //def cfReviewers = ComponentAccessor.customFieldManager.getCustomFieldObject(40821)
            /*if (approvers.length < cfReviewers.getValue(issue)?.size()?:0) {
                continue
            }*/
            if (issue.status.id == statusIdReview) {
                //need testing
                if(getCustomFieldValue(issue, 15912)){
                    doTransition(issue, 381, test)
                } else {
                    doTransition(issue, 211, test)
                }
            } else if (issue.status.id in [statusIdReadyForWork,
                                           statusIdAwaitingForCheck]) {
                if(getCustomFieldValue(issue, 15912)){//need testing
                    doTransition(issue, 381, test)
                } else {
                    doTransition(issue, 71, test)
                }
            } else if(issue.status.id in [statusIdTODO]){
                if(getCustomFieldValue(issue, 15912)){//need testing
                    doTransition(issue, 371, test)
                } else {
                    doTransition(issue, 71, test)
                }
            } else if(issue.status.id in [statusIdBug]){
                if(getCustomFieldValue(issue, 15912)){//need testing
                    doTransition(issue, 381, test)
                } else {
                    doTransition(issue, 71, test)
                }
            } else if (issue.status.id in [statusIdWork]) {
                if(getCustomFieldValue(issue, 15912)){//need testing
                    doTransition(issue, 381, test)
                } else {
                    doTransition(issue, 181, test)
                }
            } else if(issue.status.id in [statusIdTesting]){
                //doTransition(issue, 411, test)
            }
        }
    }
    if (action == "MERGED") {
        log.warn("${issue} action = ${action}")
        if (getCustomFieldValue(issue, cfIdBranch) || (!getCustomFieldValue(issue, cfIdBranch) && getCustomFieldValue(issue, cfIdBranchesFromStash) && !getCustomFieldValue(issue, cfIdBranchesFromStash)?.toString()?.contains(','))) {
            if(issue.projectObject.key != "NEWS"){
                commentManager.create(issue, test, "Merged by " + appUser, true)
            }
            if (issue.status.id == statusIdReview) {
                doTransition(issue, 241, test)
            } else if (issue.status.id in [statusIdPreRelease,
                                           statusIdTODO,
                                           statusIdReadyForWork,
                                           statusIdBug,
                                           statusIdAwaitingForCheck,
                                           statusIdTesting]) {
                doTransition(issue, 81, test)
            } else if(issue.status.id in [statusIdWork]){
                doTransition(issue, 191, test)
            }
        }
    }

    if (action == "DECLINED" || action == "REVIEWED") {
        log.warn("${issue} action = ${action}")
        if(action == "DECLINED"){
            if(issue.projectObject.key != "NEWS"){
                commentManager.create(issue, test, "DECLINED " + appUser, true)
            }
        }
        if (issue.status.id == statusIdReview) {
            doTransition(issue, 231, test)
        } else if (issue.status.id == statusIdAwaitingForCheck) {
            doTransition(issue, 141, test)
        } else if(issue.status.id in [statusIdTesting]){
            doTransition(issue, 391, test)
        } else if(issue.status.id in [statusIdWork]){
            doTransition(issue, 201, test)
        }
    }
}
//}

def ApplicationUser getTest() {
    ComponentAccessor.userManager.getUserByName('test')
}

def doTransition(issue, actionId, user) {
    def issueService = ComponentAccessor.getIssueService()
    def issueInputParameters = issueService.newIssueInputParameters();
    def transitionValidationResult = issueService.validateTransition(user, issue.id, actionId, issueInputParameters);
    if (transitionValidationResult.isValid()) {
        issueService.transition(user, transitionValidationResult);
        log.warn("${issue} actionId = ${actionId} result = true user = ${user}")
        return true
    } else {
        log.warn("doTransition ${issue} actionId = ${actionId} result = false user = ${user} errors = ${transitionValidationResult.getErrorCollection()} warnings = ${transitionValidationResult.getWarningCollection()}")
        return false
    }
}

def getCustomFieldValue(issue, Long fieldId) {
    ComponentAccessor.customFieldManager.getCustomFieldObject(fieldId)?.getValue(issue)
}
