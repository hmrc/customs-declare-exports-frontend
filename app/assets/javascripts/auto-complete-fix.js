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

    //check if any govuk-hint paragraphs has a 'withjs' attrib with specific text to replace the default JS disabled text
    updateContentToJsEnabledVersion([...document.getElementsByClassName('govuk-hint')])

    //check if any govuk-body paragraphs has a 'withjs' attrib with specific text to replace the default JS disabled text
    updateContentToJsEnabledVersion([...document.getElementsByClassName('govuk-body')])

    //grab the contents of the default select element's aria-describedby attribute and add its contents into the auto-complete's aria-describedby attribute
    let defaultSelectComp = $('select.govuk-form-group')[0]
    let fieldPrefix = defaultSelectComp.getAttribute('name').replace(/\./g, "_")
    let defaultAriaDescribedBy = defaultSelectComp.getAttribute('aria-describedby')
    let autoCompleteElement = document.getElementById(fieldPrefix)

    if (autoCompleteElement != null && defaultAriaDescribedBy != null)
    {
        let existingAriaValue= autoCompleteElement.getAttribute('aria-describedby')
        autoCompleteElement.setAttribute('aria-describedby', existingAriaValue + ' ' + defaultAriaDescribedBy)
    }
});

// ================================================================================
//  Function to enhance any select element into an accessible auto-complete (by id)
// ================================================================================
function enhanceSelectIntoAutoComplete(selectElementId, dataSource, submitOnConfirm = false) {
    selectElementId = selectElementId.replace( /(:|\.|\[|\]|,|=)/g, "\\$1" )
    let selectElementName = selectElementId.replace( /(\_)/g, "\." )
    accessibleAutocomplete.enhanceSelectElement({
        selectElement: document.querySelector('#' + selectElementId),
        displayMenu: 'inline',
        minLength: 2,
        source: customSuggest,
        confirmOnBlur: true,
        onConfirm: function(confirmed) {

            //Workaround the bug sending confirmed = undefined when confirmOnBlur == true
            let foundInData = dataSource.find(e => e.displayName === $('#'+selectElementId).val())
            let element = !!confirmed ? confirmed : foundInData

            if(!!element) {
                $('select[name="' + selectElementName + '"]').val(element.code);
                if(submitOnConfirm) {
                    window.setTimeout(function(){
                        $('form').submit();
                    }, 100);
                }
            }
            else {
                $('select[name="'+selectElementId+'"]').val('')
            }
        },
        templates: {
            inputValue: function(result) {
                return (!!result && result.displayName ? result.displayName : '');
            },
            suggestion: function(result) {
                return !!result.displayName ? result.displayName : result;
            }
        }
    })

    function customSuggest (query, syncResults) {
        var results = dataSource
        syncResults(query ? results.filter(function (result) {
            return (result.synonyms.findIndex( function(s) { return s.toLowerCase().indexOf(query.toLowerCase()) !== -1 } ) !== -1 ) || (result.displayName.toLowerCase().indexOf(query.toLowerCase()) !== -1)
        }) : [])
    }
};

function updateContentToJsEnabledVersion(elementsArray) {
    elementsArray.forEach(element => {
        if (element.getAttribute('withjs')) {
            element.innerHTML = element.getAttribute('withjs');
        }
    })
};

