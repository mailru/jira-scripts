(function($) {
  var $depForReporter = $('#customfield_12816');
  var $depForCoworkers = $('#customfield_12817');

  $('#customfield_12815').change(function(){
    var fieldValue = $(this).val();
    if (fieldValue == '11619') {
       $depForReporter.closest('.field-group').show();
    }
    else {
      $depForReporter.closest('.field-group').hide();
    }

    if (fieldValue == '11620') {
      $depForCoworkers.closest('.field-group').show();
      $depForCoworkers.val('Для всех разом')
    }
    else {
      $depForCoworkers.closest('.field-group').hide();
      $depForCoworkers.val(null);
    }
  });
})(AJS.$);