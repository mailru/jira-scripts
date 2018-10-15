(function($) {
  if((JIRA.Dialog.current && ['create-issue-dialog', 'edit-issue-dialog'].indexOf(JIRA.Dialog.current.options.id) > -1) || $('#issue-create,#issue-edit').length) {
    
    var required = [
      '12816', 
      '12817', 
      '12818', 
      '12819', 
      '12821', 
      '12822', 
      '12823', 
      '12824', 
      '12826', 
      '12827', 
      '12828', 
      '10605', 
      '13001', 
      '13107', 
      '13111', 
      '13300', 
      '13501', 
      '13701'
    ];
    var options = {
      'customfield_12815': {
        '11619': ['12816'],
        '11620': ['12817'],
        '11621': ['12818', '12819', '13107'],
        '11622': ['12821'],
        '11656': ['12821']
      },
      'customfield_12820': {
        '11623': ['12822', '10605', '13304'],
        '11624': ['12823', '12824', '12826', '13107', '13304'],
        '11625': ['10605', '13304']
      }
    }
    var subOptions = {
      '12822': {
        '11626': ['12827'],
        '11637': ['12828', '13111'],
        '11642': ['13501'], 
        '11708': ['12828'],
        '11712': ['13300'],
        '12005': ['13001'],
        '12202': ['12828'],
        '12203': ['12828'],
        '12234': ['12823', '13701'],        
        '12236': ['13500']
      }
    }

    // пометить важные поля звездочкой
    jQuery.each(required, function(i, fieldId) {
      $('#customfield_'+fieldId).closest('div.field-group').find('label').append('<span class="aui-icon icon-required">Required</span>');
    });
    
    // Когда меняются значения мета-полей, скрываем/раскрываем их зависимости
    $('#customfield_12815,#customfield_12820').change(function() {
      // Сначала спрячем все зависимые
      var sourceId = $(this).attr('id'); 
      jQuery.each(options[sourceId], function(o, optionIds) {
        jQuery.each(optionIds, function(i, depFieldId) {
          $('#customfield_'+depFieldId).closest('div.field-group').hide();
          if(depFieldId in subOptions) {
            jQuery.each(subOptions[depFieldId], function(so, subOptionIds) {
              jQuery.each(subOptionIds, function(soi, subDepFieldId) {
                $('#customfield_'+subDepFieldId).closest('div.field-group').hide();
              });
            });
          }
        });
      });
      // чтобы впоследствие открыть только те, что зависят от выбранных опций
      var sourceVal = $(this).val();
      if(sourceVal && sourceVal in options[sourceId]) {
        jQuery.each(options[sourceId][sourceVal], function(i, depFieldId) {
          var $customField = $('#customfield_'+depFieldId);
          $customField.closest('div.field-group').show();
          var customFieldValue = $customField.val();
          if(customFieldValue && depFieldId in subOptions && customFieldValue in subOptions[depFieldId]) {
            jQuery.each(subOptions[depFieldId][customFieldValue], function(soi, subDepFieldId) {
              $('#customfield_'+subDepFieldId).closest('div.field-group').show();
            });
          }
        });
      }
    });
  }
})($);
</script>