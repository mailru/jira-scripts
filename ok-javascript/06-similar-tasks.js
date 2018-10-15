
var jql = 'project = SOME and issuetype = SomeType and status != Cancelled and issue in linkedIssues("'+JIRA.Issue.getIssueId()+'") order by Stage, "Time" desc, key asc'
JIRA.SmartAjax.makeRequest({
  url: '/rest/api/2/search',
  data: {'jql': jql, 'fields': 'summary,assignee,status,description,customfield_12503,customfield_13102,customfield_13103,customfield_10509', 'maxResults': 100},
  contentType: "application/json",
  complete: function (xhr, textStatus, smartAjaxResult) {
    if(smartAjaxResult.successful) {
      if(smartAjaxResult.data.total > 0) {
        $('#odkl-module-table-empty').remove();
        var issues = smartAjaxResult.data.issues;
        jQuery.each(issues, function(i, e) {
          something
          xsx
          smartAjaxResultsx
        });
      }
    }
  }
});


$('a.odkl-wf-transition').on('click', function() {
  var issueId = $(this).attr('wf-issue-id');
  var transId = $(this).attr('wf-trans-id');
  var runTransition = JIRA.SmartAjax.makeRequest({
    url: '/rest/api/2/issue/'+issueId+'/transitions',
    type: 'POST',
    contentType: 'application/json',
    headers: {'X-Atlassian-Token': 'no-check'},
    data: JSON.stringify({ 'transition': {'id': transId} }),
    complete: function() {
      $module.find('.'+issueId).find('img').css('opacity', '0');
      $module.find('.'+issueId).find('.odkl-spinner').spin();
    }
  }).done(function(){
    JIRA.trigger(JIRA.Events.REFRESH_ISSUE_PAGE, JIRA.Issue.getIssueId());
  });
});
      