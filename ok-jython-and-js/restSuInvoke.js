$('<a class="switch-user">SU</a>').insertAfter('.view-issue-field .user-hover');
$('.switch-user').click(function(){
  var username = $(this).parent().find('.user-hover').attr('rel');
    JIRA.SmartAjax.makeRequest({
    url: '/rest/jss/1.0/jython/invoke/switchToUser',
    type: 'POST',
    data: JSON.stringify({'user': username}),
    contentType: "application/json",
    complete: function(xhr, status, smartAjaxResult) {
      if(smartAjaxResult.successful) {
        if(smartAjaxResult.data.result) {
          JIRA.trigger(JIRA.Events.REFRESH_ISSUE_PAGE, JIRA.Issue.getIssueId())
        }
        else {
          JIRA.Messages.showErrorMsg(smartAjaxResult.data.error)
        }
        
      }
    }
  });
});

