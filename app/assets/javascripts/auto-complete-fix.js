// =====================================================
// Fix for issue with accessible-autocomplete which prevents submission of empty values
// https://github.com/alphagov/accessible-autocomplete/issues/432
//
// Adapted from this fix in the amls-frontend project
// https://github.com/hmrc/amls-frontend/pull/1457/commits/974839e999c06083abafb1a402c3ea30fd4b57c6
// =====================================================


$(document).ready(function(){

    $('[data-gov-select-autocomplete]').each(function() {

        var enterKey = 13;

        var selectFieldName = $(this).attr('id').replace('[', '\\[').replace(']', '\\]');
        var nonSelectFieldName = selectFieldName.replace('-select','');

        var selectField = $('#' + selectFieldName)
        var nonSelectField = $('#' + nonSelectFieldName)

        nonSelectField.keydown(function(e) {
            if (e.keyCode === enterKey && $(this).val() === '') {
                selectField.val('')
            }
        }).keyup(function() {
            var menu = $('.autocomplete__menu')
            if (menu.text() === 'No results found') {
                selectField.val('')
            }
        }).attr('name', nonSelectFieldName + '-autocomp');

        $('button.govuk-button').click(function(){
            if(nonSelectField.val() === '')
                selectField.val('');
        })
    })

});