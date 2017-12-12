(function($){
  if (JS_INCLUDER.params.userDetails.groupNames.indexOf('odnoklassniki') !== -1) {
    var issue = (JIRA.Issue.getIssueId() || JIRA.IssueNavigator.getSelectedIssueId());
    var filter =  'project = report and issue in linkedIssues('+issue+')';
    JIRA.SmartAjax.makeRequest({
      url: '/rest/api/2/search',
      data: {'jql': filter, 'fields': 'customfield_10014,customfield_10015'},
      contentType: "application/json",
      complete: function (xhr, textStatus, smartAjaxResult) {
        if(smartAjaxResult.successful) {
          var issues = smartAjaxResult.data.issues;
          var sum1 = 0;
          var sum2 = 0;
          jQuery.each(issues, function(i, e) {
            sum1 += Math.round(parseFloat(e.fields.customfield_10014.replace(',' , '.'))*100)/100;
            sum2 += Math.round(parseFloat(e.fields.customfield_10015.replace(',' , '.'))*100)/100;         
          });
        $('#rowForcustomfield_10407').after('\
          <div id="sum1" class="name">Σ по ОК: '+Math.round(sum1*100)/100+'</div>\
          <div id="sum1">Σ по прочим: '+Math.round(sum2*100)/100+'</div>') 
        }
      }
    });
  }
})(AJS.$);