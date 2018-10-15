(function($) {
  $('#issue-create, #create-issue-dialog').find('.form-body').append(
  '<div class="field-group"> \
    <label>Город</label> \
    <select id="odkl-example-select-1" class="select"> \
      <option disabled selected style="display:none"></option>\
      <option>Москва</option> \
      <option>Санкт Петербург</option> \
      <option>Новосибирск</option> \
      <option>Екатеринбург</option> \
      <option>Самара</option> \
      <option>Уфа</option> \
      <option>Челябинск</option> \
    </select> \
  </div> \
  <div class="field-group"> \
  <label>Что скушать</label> \
    <select multiple id="odkl-example-select-2" class="multi-select"> \
      <option>Картошка</option> \
      <option>Капуста</option> \
      <option>Морковь</option> \
      <option>Говядина</option> \
      <option>Свинина</option> \
      <option>Рыба</option> \
      <option>Кальмары</option> \
      <option>Конфеты</option> \
      <option>Повидло</option> \
    </select> \
  </div>'
  );
})(AJS.$);