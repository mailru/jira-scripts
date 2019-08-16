JIRA.Issue.getIssueKey()

JS_INCLUDER.params
JS_INCLUDER.params.context
JS_INCLUDER.params.issueStatusId
JS_INCLUDER.params.issueTypeId
JS_INCLUDER.params.parentId
JS_INCLUDER.params.projectId
JS_INCLUDER.params.projectKey

JS_INCLUDER.params.userDetails
JS_INCLUDER.params.userDetails.email
JS_INCLUDER.params.userDetails.groupNames

// onTransition/edit
function getFieldValue(fieldId) {
    return AJS.$('#' + fieldId).val()
}

function getFieldText(fieldId) {
    return AJS.$('#' + fieldId).text()
}

function setFieldValue(fieldId, value) {
    AJS.$('#' + fieldId).val(value)
}

function setFieldText(fieldId, text) {
    AJS.$('#' + fieldId).text(text)
}

function setVersion(id, name){
    AJS.$('select[id="fixVersions"]').trigger('selectOption', [{value: id, title: name, label: name }]);
}

function assignToUserFromField(fieldId) {
    AJS.$.getJSON('/rest/api/2/issue/' + JIRA.Issue.getIssueId()).done(function (data) {
        var userFromField = data.fields[fieldId];
        if (AJS.$(userFromField).length) {

            var avatarurl = userFromField.avatarUrls['16x16'];
            var userName = userFromField.displayName;
            var userKey = userFromField.name

            AJS.$('#assignee-single-select .aui-ss-entity-icon').attr("src", avatarurl);
            AJS.$('#assignee-field').val(userName);
            AJS.$('#assignee-group-suggested option:selected').attr("value", userKey);
        }
    });
}

function hideField(fieldId) {
    AJS.$('div.field-group:has(#' + fieldId + ')').hide()
}

function showField(fieldId) {
    AJS.$('div.field-group:has(#' + fieldId + ')').show()
}

function removeField(fieldId) {
    AJS.$('div.field-group:has(#' + fieldId + ')').remove()
}

function isSelected(fieldId, optionId) {
    return AJS.$('select#' + fieldId + ' option[value="' + optionId + '"]').is(':selected')
}

function haveIntersections(array1, array2) {
    for (var i = 0; i < array1.length; i++)
        for (var j = 0; j < array2.length; j++)
            if (array1[i] == array2[j])
                return true;
    return false;
}

/*
use on create
examples
hideIssueType('приложение') //original: Приложение
hideIssueType('fisheye\\/crucible') //original: FishEye/Crucible
hideIssueType('новость-на-сайт') //original: Новость на сайт
*/
function hideIssueType(issueTypeName) {
    var elem = "<style type=\"text/css\">#issuetype-suggestions .aui-list-item-li-" + issueTypeName + " {display: none}</style>";
    AJS.$("head").append(elem);
}

function getTransitionId() {
    return $('form#issue-workflow-transition input[name="action"]').val();
}

setInterval(function () {
}, 500)

setTimeout(function () {
}, 500)


function addLabel(name){
    var $container = $('#labels-multi-select ul.items');
    var e = '<li class="item-row" role="option" aria-describedby="label-0" id="item-row-1"><button type="button" tabindex="-1" class="value-item"><span><span class="value-text">' + name + '</span></span></button><em class="item-delete" aria-label=" " original-title=""></em></li>';
    $container.append(e);

    var options = 'select#labels optgroup[label="user inputted option"]';
    if( $(options).length == 0 ) $('select#labels').append('<optgroup label="user inputted option"></optgroup>');
    $(options).append('<option value="' + name + '" title="' + name + '" selected="selected">' + name + '</option>');

    $container.find('li.item-row .item-delete').click(function(){
       if( $(options).length > 0 ){
       	$(options).find('option[value="' + $(this).parent().text() + '"]').remove();
       }
       $(this).parent().remove();
    });
}
