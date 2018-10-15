
(function($) {
  var createSubtasks = function(event) {
    var $form = $(this);
    if($form.data('done-subtasks')) {
      $form.data('done-subtasks', null);
      return;
    }
    var assignees = [];
    jQuery.each($form.find('#customfield_10500').val().split(','), function(i, assignee) {
      if(assignee.trim()) {
        assignees.push(assignee.trim());
      }
    })
    if(assignees.length == 0) {
      return;
    }
    var createTask = function(data) {
      return JIRA.SmartAjax.makeRequest({
        url: '/rest/api/2/issue',
        type: 'POST',
        data: JSON.stringify({
          'fields': {
            'project': {'id': '11003'},
            'issuetype': {'id': '5'},
            'summary': data.summary,
            'description': data.description,
            'assignee': {'name': data.assignee},
            'parent': {'id': data.parent}
          }
        }),
        dataType: 'json',
        contentType: "application/json"
      });
    }

    var createTasks = jQuery.when();
    jQuery.each(assignees, function(i, assignee) {
      createTasks = createTasks.pipe(function() {
        return createTask({
          'assignee': assignee,
          'summary': 'Продление '+JIRA.Issue.getIssueKey(), 
          'description': desc, 
          'parent': JIRA.Issue.getIssueId()
        });}).pipe(null, function() {
        return jQuery.Deferred().resolve();
      });
    });

    createTasks.always(function() {
      JIRA.Loading.hideLoadingIndicator();
      $('#issue-workflow-transition').data('done-subtasks', true);
      $('#issue-workflow-transition').submit();
    });

    $form.find('#issue-workflow-transition-submit').prop('disabled', 'disabled');
    JIRA.Loading.showLoadingIndicator();
    return false;
  }
  $('#customfield_10500').closest('form').bind('before-submit', createSubtasks);
})($);