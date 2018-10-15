
// download file
$('#odkl-module-file-download').on('click', function() {
    var link = document.createElement('a');
    link.download = issueKey+'-Modules.txt';
    link.href = 'data:application/octet;charset=UTF-8,'
      + encodeURIComponent(formattedText);
    document.body.appendChild(link); // firefox crutch
    link.click();
    document.body.removeChild(link); // firefox crutch
});
// copy to clipboard
$('#odkl-module-text-clipboard').on('click', function() {
  var hiddenField = document.createElement('textarea');
  hiddenField.setAttribute('type', 'hidden');
  hiddenField.value = formattedText;

  document.body.appendChild(hiddenField);
  hiddenField.select();
  document.execCommand('copy');
  document.body.removeChild(hiddenField);
});
