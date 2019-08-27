// ==UserScript==
// @name         Customs Declare Exports AutoComplete
// @namespace    http://tampermonkey.net/
// @version      0.1
// @description  try to take over the world!
// @author       You
// @match        http*://*/customs-declare-exports*
// @grant        none
// ==/UserScript==

(function() {
    'use strict';
    document.getElementById('global-header').appendChild(createQuickButton());
})();

function createQuickButton() {
    let button = document.createElement('button');
    button.id="quickSubmit";
    button.classList.add('button-start');
    button.innerHTML = 'Quick Submit';
    button.onclick = () => completePage();
    return button;
}

// selected can be an index or a value
function selectFromAutoPredict(element, selected) {
    let index = typeof selected == "number" ? selected : 0;
    let selects = element.getElementsByTagName('select');
    let inputs = element.getElementsByTagName('input');
    for(let j = 0; j < selects.length; j++){
        let options = selects[j].getElementsByTagName('option');
        let option = options[index];
        if(typeof selected == "string"){
            for(let o = 0; o < options.length; o++) {
                if(options[o].value === selected) {
                    option = options[o];
                }
            }
        }
        option.selected = "selected";
        selects[j].value = option.value;
        inputs[j].value = option.value;
    }
}

function selectRadioOption(element, index){
    let inputs = element.getElementsByTagName('input');
    if (inputs && index < inputs.length) {
        inputs[index].checked = true
    }
}

function currentPageIs(path) {
    let matches = window.location.pathname.match(path);
    return matches && matches.length > 0
}

function completePage() {
    if (currentPageIs('/customs-declare-exports/start')) {
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-declare-exports/choice")){
        selectRadioOption(document.getElementById("value"), 0);
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-declare-exports/declaration/dispatch-location")){
        selectRadioOption(document.getElementById("dispatchLocation"), 0);
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-declare-exports/declaration/type")){
        selectRadioOption(document.getElementById("additionalDeclarationType"), 0);
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/consignment-references")) {
        document.getElementById('lrn').value = 'JasTest4';
        document.getElementById('ducr_ducr').value = '8GB12345' + Math.floor(Math.random() * 8999) + 100 + '-101SHIP1';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/exporter-details")) {
        document.getElementById('details_eori').value = 'GB717572504502801';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/consignee-details")) {
        document.getElementById('details_address_fullName').value = 'Bags Export';
        document.getElementById('details_address_addressLine').value = '1 Bags Avenue';
        document.getElementById('details_address_townOrCity').value = 'New York';
        document.getElementById('details_address_postCode').value = 'NA';
        selectFromAutoPredict(document.getElementById('details.address.country-container'), "United States of America");
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/declarant-details")) {
        document.getElementById('details_eori').value = 'GB717572504502811';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/representative-details")) {
        document.getElementById('details_eori').value = 'GB717572504502809';
        selectRadioOption(document.getElementById("statusCode"), 1);
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/additional-actors")) {
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/carrier-details")) {
        document.getElementById('details_eori').value = 'GB025115100002';
        document.getElementById('details_address_fullName').value = 'XYZ Carrier';
        document.getElementById('details_address_addressLine').value = 'School Road';
        document.getElementById('details_address_townOrCity').value = 'London';
        document.getElementById('details_address_postCode').value = 'WS1 2AB';
        selectFromAutoPredict(document.getElementById('details.address.country-container'), "United Kingdom");
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/holder-of-authorisation")) {
        selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "AEOC");
        document.getElementById('eori').value = 'GB717572504502811';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/destination-countries")) {
        selectFromAutoPredict(document.getElementById("countryOfDispatch-container"), "GB");
        if(document.getElementById("countriesOfRouting-container")){
            selectFromAutoPredict(document.getElementById("countriesOfRouting-container"), "GB")
        }
        selectFromAutoPredict(document.getElementById("countryOfDestination-container"), "US");
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/location-of-goods")) {
        document.getElementById('typeOfLocation').value ='B';
        document.getElementById('qualifierOfIdentification').value ='Y';
        document.getElementById('identificationOfLocation').value ='FXT';
        document.getElementById('additionalQualifier').value ='U1234567GB';
        selectFromAutoPredict(document.getElementById('country-container'), "United Kingdom");
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/office-of-exit")) {
        selectFromAutoPredict(document.getElementById('officeId-container'), "GB000054");
        if(document.getElementById('presentationOfficeId-container')){
            selectFromAutoPredict(document.getElementById('presentationOfficeId-container'), "GBLBA003")
        }
        if(document.getElementById("circumstancesCode")) {
            selectRadioOption(document.getElementById("circumstancesCode"), 1);
        }
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/total-numbers-of-items')) {
        document.getElementById('totalAmountInvoiced').value ='56764';
        document.getElementById('exchangeRate').value ='1.49';
        document.getElementById('totalPackage').value ='1';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/nature-of-transaction")) {
        selectRadioOption(document.getElementById("natureType"), 0);
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/transaction-type')) {
        document.getElementById('documentTypeCode').value ='1';
        document.getElementById('identifier').value ='1';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/previous-documents')) {
        selectRadioOption(document.getElementById("documentCategory"), 1);
        selectFromAutoPredict(document.getElementById('documentType-container'), "IF3");
        document.getElementById('documentReference').value ='101SHIP2';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/export-items')) {
        let nextLink = document.getElementById('submit');
        if(nextLink) {
            nextLink.click()
        } else {
            document.getElementById('add').click()
        }
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/procedure-codes')) {
        document.getElementById('procedureCode').value ='1042';
        document.getElementById('additionalProcedureCode').value ='000';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/fiscal-information')) {
        document.getElementById('No').checked = 'checked';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-fiscal-references')) {
        selectFromAutoPredict(document.getElementById("country-container"), 'GB');
        document.getElementById("reference").value = '1234';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/item-type')) {
        document.getElementById('combinedNomenclatureCode').value ='46021910';
        document.getElementById('statisticalValue').value ='1000';
        document.getElementById('descriptionOfGoods').value ='Straw for bottles';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/package-information')) {
        if (document.getElementsByClassName('button--secondary').length > 1) {
            console.log("More than one secondary buttons");
            document.getElementsByClassName('button')[0].click()
        }
        else {
            selectFromAutoPredict(document.getElementById('typesOfPackages-container'), "PK");
            document.getElementById('numberOfPackages').value ='10';
            document.getElementById('shippingMarks').value = 'RICH123';
            document.getElementById('add').click();
            document.getElementById('add').on('click', function() {
                document.getElementsByClassName('button')[0].click()
            });
        }
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/commodity-measure')) {
        document.getElementById('supplementaryUnits').value ='10';
        document.getElementById('netMass').value ='500';
        document.getElementById('grossMass').value ='700';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-information')) {
        document.getElementById('code').value ='00400';
        document.getElementById('description').value ='EXPORTER';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/add-document')) {
        document.getElementById('documentTypeCode').value ='C501';
        document.getElementById('documentIdentifierAndPart_documentIdentifier').value ='GBAEOC71757250450281';
        document.getElementById('documentIdentifierAndPart_documentPart').value ='1';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/warehouse')) {
        selectFromAutoPredict(document.getElementById('supervisingCustomsOffice-container'), "GBLBA001");
        selectRadioOption(document.getElementById('inlandModeOfTransportCode'), 0);
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/border-transport')) {
        selectRadioOption(document.getElementById('borderModeOfTransportCode'), 0);
        selectRadioOption(document.getElementById('meansOfTransportOnDepartureType'), 1);
        if(document.getElementById("meansOfTransportCrossingTheBorderIDNumber")){
            document.getElementById("meansOfTransportCrossingTheBorderIDNumber").value = 'SHIP1'
        }
        if(document.getElementById("meansOfTransportOnDepartureIDNumber")){
            document.getElementById("meansOfTransportOnDepartureIDNumber").value = 'SHIP1'
        }
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/transport-details')) {
        selectFromAutoPredict(document.getElementById('meansOfTransportCrossingTheBorderNationality-container'), "United Kingdom");
        selectRadioOption(document.getElementById('meansOfTransportCrossingTheBorderType'), 1);
        document.getElementById("meansOfTransportCrossingTheBorderIDNumber").value = 'BOAT1';
        selectRadioOption(document.getElementById('container'), 0);
        if(document.getElementById('paymentMethod')){
            selectRadioOption(document.getElementById('paymentMethod'), 4)
        }
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/add-transport-containers')) {
        document.getElementById('id').value = '123456';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/add-seal')) {
        document.getElementById('id').value = 'SIN123';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/summary')) {
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/confirmation')) {
        document.getElementsByClassName('button')[0].click()
    }
}