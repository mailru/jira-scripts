(function($) {
 

// Преобразование полей и автозаполнение лейблов
new AJS.SingleSelect({
  element: $('#customfield_13001'),
  itemAttrDisplayed: 'label'
});
new AJS.MultiSelect({
  element: $('#customfield_13121'),
  itemAttrDisplayed: 'title'
});
$('#odkl-example-select-1').change(function(){
  $('#labels').trigger('clearSelection');
  $('#labels').trigger('selectOption', {'value': $(this).val()});
});

// Бессмысленный код для демонстрации JIRA AJS
var issueKey = JIRA.Meta.getIssueKey() || JIRA.IssueNavigator.getSelectedIssueKey();
var currentUser = AJS.params.loggedInUser
JIRA.Loading.showLoadingIndicator();

$.when($.getJSON('/rest/api/2/myself')).done(function(){
  JIRA.Messages.showSuccessMsg('Все хорошо у '+currentUser);
  JIRA.trigger(JIRA.Events.REFRESH_ISSUE_PAGE, issueKey)
}).fail(function(){
  JIRA.Issue.reload();
  JIRA.Messages.showReloadErrorMsg('Все плохо в '+JIRA.plugins.tzBanner.prefs.tzid);
}).always(function(){
  JIRA.Loading.hideLoadingIndicator();
});



})($);