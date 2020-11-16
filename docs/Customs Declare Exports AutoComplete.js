// ==UserScript==
// @name         Customs Declare Exports AutoComplete
// @namespace    http://tampermonkey.net/
// @version      1.63
// @description  decs supported: (Std-Arrived A), (Occ-Arrived B), (Smp-Arrived C), (Std-PreLodged D), (Occ-PreLodged E), (Smp-PreLodged F), (Clr-Arrived J), (Clr-PreLodged K), (Sup-SDP Y), (Sup-EIDR Z)
// @author       You
// @match        http*://*/customs-declare-exports*
// @grant GM_setValue
// @grant GM_getValue
// @updateURL    https://raw.githubusercontent.com/hmrc/customs-declare-exports-frontend/master/docs/Customs%20Declare%20Exports%20AutoComplete.js
// ==/UserScript==

(function() {
    'use strict';
    document.body.appendChild(dropDown());
})();

// helper methods
function setDeclaration(choice) { GM_setValue("declaration", choice); }

function getDeclaration() { return GM_getValue("declaration"); }

function dropDown() {
    var panel = document.createElement("div");

    panel.appendChild(createQuickButton());

    // create array of options to be added
    let text = [
        "Standard-PreLodged",
        "Standard-Arrived",
        "Simplified-PreLodged",
        "Simplified-Arrived",
        "Supplementary-SDP",
        "Supplementary-EIDR",
        "Occasional-PreLodged",
        "Occasional-Arrived",
        "Clearance-PreLodged",
        "Clearance-Arrived"
    ];
    let value = ["D", "A", "F", "C", "Y", "Z", "E", "B", "K", "J"]

    // create and append select list
    var selectList = document.createElement("select");
    selectList.style.position = "absolute"
    selectList.style.top = "100px"
    selectList.id = "mySelect";
    selectList.className = "govuk-!-display-none-print"
    panel.appendChild(selectList);

    // create and append the options
    for (var i = 0; i < text.length; i++) {
        var option = document.createElement("option");
        option.value = value[i];
        option.text = text[i];
        selectList.appendChild(option);
    }

    selectList.onchange = function (e) { setDeclaration(this.value); };

    // empty on start
    if (currentPageIs('/customs-declare-exports/start')) {
        selectList.value = 0;
        setDeclaration(0);
    } else {
        selectList.value = getDeclaration()
    }
    return panel;
}

function createQuickButton() {

    let button = document.createElement('button');
    button.id="quickSubmit";

    if (!!document.getElementById('global-header')) {
        button.classList.add('button-start', 'govuk-!-display-none-print');
    } else {
        button.classList.add('govuk-button','govuk-!-display-none-print');
    }

    button.style.position = "absolute"
    button.style.top = "50px"
    button.innerHTML = 'Quick Submit';
    button.onclick = () => completeJourney();

    return button;
}

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

function selectRadioOptionFromInputs(inputs, index){
    if (inputs && index < inputs.length) {
        inputs[index].checked = true
    }
}

function findRadioOption(fieldname) {

    let inputs = document.getElementById(fieldname).getElementsByTagName('input');
    let choice;

    for(var i = 0; i < inputs.length; i++){
        if(inputs[i].checked){
            choice = inputs[i].value;
        }
    }

    return choice;
}

function currentPageIs(path) {
    if(path.includes("*")) {
        let matches = window.location.pathname.match(path);
        return matches && matches.length > 0
    } else {
        return (path == window.location.pathname);
    }
}

// actual pages
function startPage(){
    if (currentPageIs('/customs-declare-exports/start')) {

        if(getDeclaration() == 0){
            alert("Select journey type");
            return;
        }

        document.getElementsByClassName('govuk-button')[0].click()
    }
}

function choicePage(){
    if(currentPageIs('/customs-declare-exports/choice')){

        if(getDeclaration() == 0){
            alert("Select journey type");
            return;
        }

        document.getElementById('CRT').checked = true
        document.getElementById('submit').click()
    }
}

function declarationChoice(){
    if(currentPageIs('/customs-declare-exports/declaration/declaration-choice')){

        if(getDeclaration() == 0){
            alert("Select journey type");
            return;
        }

        let inputs = document.getElementsByName('type');

        switch(getDeclaration()) {
            case 'D':
            case 'A':
                selectRadioOptionFromInputs(inputs, 0);
                break;
            case 'F':
            case 'C':
                selectRadioOptionFromInputs(inputs, 1);
                break;
            case 'Y':
            case 'Z':
                selectRadioOptionFromInputs(inputs, 2);
                break;
            case 'E':
            case 'B':
                selectRadioOptionFromInputs(inputs, 3);
                break;
            case 'K':
            case 'J':
                selectRadioOptionFromInputs(inputs, 4);
                break;
        }
        document.getElementById('submit').click()
    }
}

function dispatchLocation(){
    if(currentPageIs('/customs-declare-exports/declaration/dispatch-location')){

        selectRadioOptionFromInputs(document.getElementsByName('dispatchLocation'), 0);
        document.getElementById('submit').click()
    }
}

function additionalDeclarationType(){
    if(currentPageIs('/customs-declare-exports/declaration/type')){
        // top values
        if (['D','F','Y','E','K'].indexOf(getDeclaration()) > -1) {
            selectRadioOptionFromInputs(document.getElementsByName('additionalDeclarationType'), 0);
        } else {
            selectRadioOptionFromInputs(document.getElementsByName('additionalDeclarationType'), 1);
        }
        document.getElementById('submit').click()
    }
}

function consignmentRefereences(){
    if (currentPageIs('/customs-declare-exports/declaration/consignment-references')) {
        document.getElementById('lrn').value = 'QSLRN' + Math.floor(Math.random() * 8999) + 100;
        document.getElementById('ducr_ducr').value = '8GB123456' + Math.floor(Math.random() * 899999 + 100000) + '-101SHIP1';
        document.getElementById('submit').click()
    }
}

// clearance
function isEntryIntoDeclarantsRecords(){
    if (currentPageIs('/customs-declare-exports/declaration/entry-into-declarants-records')) {
        document.getElementById('answer_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

// clearance
function personPresentingGoodsDetails(){
    if (currentPageIs('/customs-declare-exports/declaration/person-presenting-goods')) {
        document.getElementById('eori').value = 'GB614299894872549';
        document.getElementById('submit').click()
    }
}

function declarantDetails(){
    if (currentPageIs('/customs-declare-exports/declaration/declarant-details')) {
        document.getElementById('code_yes').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function isDeclarantExporter(){
    if (currentPageIs('/customs-declare-exports/declaration/are-you-the-exporter')) {
        document.getElementById('answer_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function exporterDetails(){
    if (currentPageIs('/customs-declare-exports/declaration/exporter-details')) {
        document.getElementById('details_eori').value = 'GB717572504502801';
        document.getElementById('submit').click()
    }
}

// clearance
function isExs(){
    if (currentPageIs('/customs-declare-exports/declaration/is-this-exs')) {
        document.getElementById('code_yes').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function consignorAddress(){
    if (currentPageIs('/customs-declare-exports/declaration/consignor-address')) {
        document.getElementById('details_address_fullName').value = 'Bags Export';
        document.getElementById('details_address_addressLine').value = '1 Bags Avenue';
        document.getElementById('details_address_townOrCity').value = 'New York';
        document.getElementById('details_address_postCode').value = '10001';

        selectFromAutoPredict(document.getElementById('details_address_country-container'), "United States of America");
        document.getElementById('submit').click()
    }
}

function consignorEoriNumber(){
    if (currentPageIs('/customs-declare-exports/declaration/consignor-eori-number')) {
        document.getElementById('Yes').checked = 'checked';
        document.getElementById('eori').value = 'GB123456789000';
        document.getElementById('submit').click()
    }
}

function representingAnotherAgent(){
    if (currentPageIs('/customs-declare-exports/declaration/are-you-completing-this-declaration-on-behalf-of-another-agent')) {

        switch(getDeclaration()) {
            case 'A':
            case 'D':
                selectRadioOptionFromInputs(document.getElementsByName('representingAgent'), 1);
                break;
            default:
                selectRadioOptionFromInputs(document.getElementsByName('representingAgent'), 0);
        }
        document.getElementById('submit').click()
    }
}

function representativeEori(){
    if (currentPageIs('/customs-declare-exports/declaration/representatives-eori-number')) {

        switch(getDeclaration()) {
            case 'A':
            case 'D':
                break;
            case 'B':
            case 'C':
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
            case 'Z':
                document.getElementById('details_eori').value = 'GB717572504502801';
                break;
            case 'J':
                document.getElementById('details_eori').value = 'GB717572504502809';
                break;
        }
        document.getElementById('submit').click()
    }
}

function representativeType(){
    if (currentPageIs('/customs-declare-exports/declaration/representation-type-agreed')) {

        switch(getDeclaration()) {
            case 'A':
            case 'D':
            case 'B':
            case 'C':
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
            case 'Z':
                selectRadioOptionFromInputs(document.getElementsByName('statusCode'), 0);
                break;
            case 'J':
                selectRadioOptionFromInputs(document.getElementsByName('statusCode'), 1);
                break;
        }
        document.getElementById('submit').click()
    }
}

function carrierDetails(){
    if (currentPageIs('/customs-declare-exports/declaration/carrier-details')) {
        document.getElementById('details_address_fullName').value = 'XYZ Carrier';
        document.getElementById('details_address_addressLine').value = 'School Road';
        document.getElementById('details_address_townOrCity').value = 'London';
        document.getElementById('details_address_postCode').value = 'WS1 2AB';
        selectFromAutoPredict(document.getElementById('details_address_country-container'), "United Kingdom");
        document.getElementById('submit').click()
    }
}

function carrierEoriNumber(){
    if (currentPageIs('/customs-declare-exports/declaration/carrier-eori-number')) {
        document.getElementById('Yes').checked = 'checked';
        document.getElementById('eori').value = 'GB123456789000';
        document.getElementById('submit').click()
    }
}

function consigneeDetails(){
    if (currentPageIs('/customs-declare-exports/declaration/consignee-details')) {
        document.getElementById('details_address_fullName').value = 'Bags Export';
        document.getElementById('details_address_addressLine').value = '1 Bags Avenue';
        document.getElementById('details_address_townOrCity').value = 'New York';
        document.getElementById('details_address_postCode').value = '10001';

        selectFromAutoPredict(document.getElementById('details_address_country-container'), "United States of America");
        document.getElementById('submit').click()
    }
}

function otherPartiesInvolved(){
    if (currentPageIs('/customs-declare-exports/declaration/other-parties-involved')) {
        selectRadioOptionFromInputs(document.getElementsByName('partyType'), 4);
        document.getElementById('submit').click()
    }
}

function holderOfAuthorisation(){
    if (currentPageIs('/customs-declare-exports/declaration/add-authorisation-required')) {
        switch(getDeclaration())
        {
            case 'C':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "IPO");
                document.getElementById('eori').value = 'GB717572504502801';
                document.getElementById('submit').click();
                break;
            case 'Z':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "EIR");
                document.getElementById('eori').value = 'GB717572504502802';
                document.getElementById('submit').click();
                break;
            case 'F':
            case 'Y':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "SDE");
                document.getElementById('eori').value = 'GB717572504502801';
                document.getElementById('submit').click();
                break;
            case 'J':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "EIR");
                document.getElementById('eori').value = 'GB717572504502811';
                document.getElementById('submit').click();
                break;
            case 'B':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "CSE");
                document.getElementById('eori').value = 'GB717572504502801';
                document.getElementById('submit').click();
                break;
            case 'A':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "CSE");
                document.getElementById('eori').value = 'GB717572504502802';
                document.getElementById('submit').click();
                break;
            case 'D':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "AEOC");
                document.getElementById('eori').value = 'GB717572504502811';
                document.getElementById('submit').click();
                break;
            case 'E':
            case 'K':
                document.getElementById('submit').click();
                break;
        }
    }
}

function authorisationsSummary(){
    if (currentPageIs('/customs-declare-exports/declaration/authorisations-required')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function originationCountry(){
    if (currentPageIs('/customs-declare-exports/declaration/origination-country')) {
        selectFromAutoPredict(document.getElementById('countryCode-container'), "GB");
        document.getElementById('submit').click();
    }
}

function destinationCountry(){
    if (currentPageIs('/customs-declare-exports/declaration/destination-country')) {
        selectFromAutoPredict(document.getElementById('countryCode-container'), "US");
        document.getElementById('submit').click();
    }
}

function countryOfRouting(){
    if (currentPageIs('/customs-declare-exports/declaration/country-of-routing')) {
        document.getElementById('Yes').click();
        document.getElementById('submit').click();
    }
}

function countriesOfRouting(){
    if (currentPageIs('/customs-declare-exports/declaration/countries-of-routing')) {
        selectFromAutoPredict(document.getElementById('countryCode-container'), "CN");
        document.getElementById('submit').click();
    }
}

function countriesOfRoutingSummary(){
    if (currentPageIs('/customs-declare-exports/declaration/countries-summary')) {
        document.getElementById('No').click();
        document.getElementById('submit').click();
    }
}

function locationOfGoods(){
    if (currentPageIs('/customs-declare-exports/declaration/location-of-goods')) {
        switch(getDeclaration()){
            case 'B':
            case 'C':
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
                document.getElementById('code').value = 'GBAUBELBFSBEL';
                break;
            case 'J':
                document.getElementById('code').value = 'GBAUFXTFXTFXT';
                break;
            case 'Z':
                document.getElementById('code').value = 'GBAUABDABDABD';
                break;
            default:
                document.getElementById('code').value = 'GBAUFXTFXTFXT';
        }

        document.getElementById('submit').click()
    }
}

function officeOfExit(){
    if (currentPageIs('/customs-declare-exports/declaration/office-of-exit')) {

        if(document.getElementById('Yes')) {
            document.getElementById('Yes').click()
        }

        switch(getDeclaration()){
            case 'C':
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
                selectFromAutoPredict(document.getElementById('officeId-container'), "GB000041");
                break;
            case 'J':
                selectFromAutoPredict(document.getElementById('officeId-container'), "GB000054");
                break;
            case 'Z':
                selectFromAutoPredict(document.getElementById('officeId-container'), "GB000051");
                break;
            default:
                selectFromAutoPredict(document.getElementById('officeId-container'), "GB000434");
        }
        document.getElementById('submit').click();
    }
}

function totalNumberOfItems(){
    if (currentPageIs('/customs-declare-exports/declaration/total-numbers-of-items')) {
        document.getElementById('exchangeRate').value ='1.49';
        document.getElementById('totalAmountInvoiced').value ='56764';
        document.getElementById('submit').click();
    }
}

function totalPackageQuantity(){
    if (currentPageIs('/customs-declare-exports/declaration/total-package-quantity')) {
        document.getElementById('totalPackage').value ='1';
        document.getElementById('submit').click();
    }
}

function natureOfTransaction(){
    if (currentPageIs('/customs-declare-exports/declaration/nature-of-transaction')) {
        selectRadioOptionFromInputs(document.getElementsByName('natureType'), 0)
        document.getElementById('submit').click()
    }
}

function previousDocuments(){
    if (currentPageIs('/customs-declare-exports/declaration/add-previous-document')) {
        switch(getDeclaration()){
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
            case 'Z':
                break;
            case 'J':
                selectRadioOptionFromInputs(document.getElementsByName('documentCategory'), 1);
                selectFromAutoPredict(document.getElementById('documentType-container'), "IF3");
                document.getElementById('documentReference').value ='101SHIP2';
                break;
            default:
                selectRadioOptionFromInputs(document.getElementsByName('documentCategory'), 1);
                selectFromAutoPredict(document.getElementById('documentType-container'), "DCS");
                document.getElementById('documentReference').value ='9GB123456782317-BH1433A61';
        }
        document.getElementById('submit').click()
    }
}

function previousDocumentsSummary(){
    if (currentPageIs('/customs-declare-exports/declaration/previous-documents-list')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

// items
function addFirstItem(){
    if (currentPageIs('/customs-declare-exports/declaration/add-declaration-item')) {
        document.getElementById('add').click()
    }
}

function procedureCodes(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/procedure-codes')) {
        switch(getDeclaration()){
            case 'C':
                document.getElementById('procedureCode').value = '3151';
                document.getElementById('additionalProcedureCode').value ='1CS';
                break;
            case 'D':
            case 'F':
            case 'J':
            case 'Y':
                document.getElementById('procedureCode').value = '1040';
                document.getElementById('additionalProcedureCode').value ='000';
                break;
            case 'E':
                document.getElementById('procedureCode').value = '1040';
                document.getElementById('additionalProcedureCode').value ='3NS';
                break;
            case 'K':
                document.getElementById('procedureCode').value = '0012';
                document.getElementById('additionalProcedureCode').value ='000';
                break;
            case 'Z':
                document.getElementById('procedureCode').value = '1040';
                document.getElementById('additionalProcedureCode').value ='1MU';
                break;
            default:
                document.getElementById('procedureCode').value ='1040';
                document.getElementById('additionalProcedureCode').value ='1CS';
        }
        document.getElementById('submit').click();
    }
}

function fiscalInformation(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/fiscal-information')) {
        document.getElementById('No').checked = 'checked';
        document.getElementById('submit').click()
    }
}

function fiscalReferences(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-fiscal-references')) {
        selectFromAutoPredict(document.getElementById('country-container'), 'GB');
        document.getElementById('reference').value = '1234';
        document.getElementById('submit').click();
    }
}

function fiscalReferencesSummary(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-fiscal-references-list')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click()
    }
}

function commodityDetails(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/commodity-details')) {
        switch(getDeclaration()){
            case 'B':
            case 'C':
            case 'E':
            case 'F':
            case 'Y':
                document.getElementById('combinedNomenclatureCode').value ='84111100';
                document.getElementById('descriptionOfGoods').value ='Aircraft engine';
                break;
            case 'Z':
                document.getElementById('combinedNomenclatureCode').value ='85389099';
                document.getElementById('descriptionOfGoods').value ='Oilwell equipment';
                break;
            default:
                document.getElementById('combinedNomenclatureCode').value ='46021910';
                document.getElementById('descriptionOfGoods').value ='Straw for bottles';
        }
        document.getElementById('submit').click();
    }
}

function unDangerousGoodsCode(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/un-dangerous-goods-code')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function cusCode(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/cus-code')) {
        switch(getDeclaration()){
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
            case 'Z':
                document.getElementById('code_no').checked = 'checked';
                break;
            case 'J':
                document.getElementById('code_yes').checked = 'checked';
                document.getElementById('cusCode').value ='12345678';
                break;
        }
        document.getElementById('submit').click();
    }
}

function additionalTaricCodes(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-taric-code')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function nactCodes(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/national-additional-code')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function statisticalValue(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/statistical-value')) {
        document.getElementById('statisticalValue').value ='1000';
        document.getElementById('submit').click();
    }
}

function addPackageInformation(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/package-information')) {
        selectFromAutoPredict(document.getElementById('typesOfPackages-container'), "XD");
        document.getElementById('numberOfPackages').value ='10';
        document.getElementById('shippingMarks').value = 'Shipping description';
        document.getElementById('submit').click();
    }
}

function packageInformationSummary(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/packages-list')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function commodityMeasurments(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/commodity-measure')) {
        switch(getDeclaration()){
            case 'D':
            case 'A':
            case 'F':
            case 'C':
            case 'Y':
            case 'Z':
            case 'E':
            case 'B':
                document.getElementById('supplementaryUnits').value ='10';
                document.getElementById('netMass').value ='500';
                document.getElementById('grossMass').value ='700';
                break;
            default:
                document.getElementById('netMass').value ='500';
                document.getElementById('grossMass').value ='700';
        }
        document.getElementById('submit').click()
    }
}

function isAdditionalInformationRequired(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/is-additional-information-required')) {
        document.getElementById('code_yes').checked = 'checked';
        document.getElementById('submit').click()
    }
}

function addAdditionalInformation(){

    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-information')) {
        switch(getDeclaration()){
            case 'C':
                if (!document.getElementById("removable_elements__row0")) {
                    document.getElementById('code').value ='00400';
                    document.getElementById('description').value ='EXPORTER';
                    document.getElementById('submit').click();
                } else if (!document.getElementById("removable_elements__row1")) {
                    document.getElementById('code').value ='10200';
                    document.getElementById('description').value ='IPOSDE';
                    document.getElementById('add').click();
                } else {
                    document.getElementById('submit').click();
                }
                break;
            case 'K':
            case 'Z':
                document.getElementById('code').value ='00600';
                document.getElementById('description').value ='EXPORTER';
                document.getElementById('submit').click();
                break;
            default:
                document.getElementById('code').value ='00400';
                document.getElementById('description').value ='EXPORTER';
                document.getElementById('submit').click();
        }
    }
}

function additionalInformationSummary(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-information-list')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click()
    }
}

function addDocuments(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-documentation')) {
        switch(getDeclaration())
        {
            case 'C':
                if (!document.querySelector("#content>article>form>table>tbody>tr>th")) {
                    document.getElementById('documentTypeCode').value ='C601';
                    document.getElementById('documentIdentifier').value ='GBIPO717572504502801';
                    document.getElementById('submit').click();
                } else if (!document.querySelector("#content>article>form>table>tbody>tr:nth-child(2)>th")) {
                    document.getElementById('documentTypeCode').value ='C512';
                    document.getElementById('documentIdentifier').value ='GBSDE717572504502801';
                    document.getElementById('add').click();
                } else if (!document.querySelector("#content>article>form>table>tbody>tr:nth-child(3)>th")) {
                    document.getElementById('documentTypeCode').value ='C676';
                    document.getElementById('documentIdentifier').value ='GBCSE717572504502801';
                    document.getElementById('add').click();
                } else {
                    document.getElementById('submit').click();
                }
                break;
            case 'Z':
                if (!document.querySelector("#content>article>form>table>tbody>tr>th")) {
                    document.getElementById('documentTypeCode').value ='C514';
                    document.getElementById('documentIdentifier').value ='GBEIR717572504502802';
                    document.getElementById('submit').click();
                } else if (!document.querySelector("#content>article>form>table>tbody>tr:nth-child(2)>th")) {
                    document.getElementById('documentTypeCode').value ='C676';
                    document.getElementById('documentIdentifier').value ='GBMOU717572504502802';
                    document.getElementById('add').click();
                } else {
                    document.getElementById('submit').click();
                }
                break;
            case 'Y':
                if (!document.querySelector("#content>article>form>table>tbody>tr>th")) {
                    document.getElementById('documentTypeCode').value ='C512';
                    document.getElementById('documentIdentifier').value ='GBSDE717572504502801';
                    document.getElementById('submit').click();
                } else if (!document.querySelector("#content>article>form>table>tbody>tr:nth-child(2)>th")) {
                    document.getElementById('documentTypeCode').value ='Y901';
                    document.getElementById('documentIdentifier').value ='GB717572504502801';
                    document.getElementById('documentStatus').value = 'XX';
                    document.getElementById('add').click();
                } else if (!document.querySelector("#content>article>form>table>tbody>tr:nth-child(3)>th")) {
                    document.getElementById('documentTypeCode').value ='X002';
                    document.getElementById('documentIdentifier').value ='428/2009';
                    document.getElementById('documentStatus').value = 'AF';
                    document.getElementById('documentWriteOff_measurementUnit').value = 'KGM#G';
                    document.getElementById('documentWriteOff_documentQuantity').value = '100';
                    document.getElementById('add').click();
                } else {
                    document.getElementById('submit').click();
                }
                break;
            case 'F':
                document.getElementById('documentTypeCode').value ='C512';
                document.getElementById('documentIdentifier').value ='GBSDE717572504502801';
                document.getElementById('submit').click();
                break;
            case 'J':
                document.getElementById('documentTypeCode').value ='C514';
                document.getElementById('documentIdentifier').value ='GBEIR717572504502811';
                document.getElementById('submit').click();
                break;
            case 'A':
                document.getElementById('documentTypeCode').value ='C676';
                document.getElementById('documentIdentifier').value ='GBCSE717572504502802';
                document.getElementById('submit').click();
                break;
            case 'B':
                document.getElementById('documentTypeCode').value = 'C676';
                document.getElementById('documentIdentifier').value = 'GBCSE717572504502801';
                document.getElementById('submit').click();
                break;
            case 'E':
                document.getElementById('submit').click();
                break;
            case 'K':
                document.getElementById('submit').click();
                break;
            default:
                document.getElementById('documentTypeCode').value ='C501';
                document.getElementById('documentIdentifier').value ='GBAEOC717572504502811';
                document.getElementById('submit').click();
        }
    }
}

function addDocumentsSummary(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-documentation-list')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

// items end
function exportItems(){
    if (currentPageIs('/customs-declare-exports/declaration/declaration-items-list')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function transportLeavingBorder(){
    if (currentPageIs('/customs-declare-exports/declaration/transport-leaving-the-border')) {
        selectRadioOptionFromInputs(document.getElementsByName('transportLeavingTheBorder'), 0);
        document.getElementById('submit').click();
    }
}

function warehouseIdentification(){
    if (currentPageIs('/customs-declare-exports/declaration/warehouse-details')) {

        if(document.getElementById('code_yes')) {
            document.getElementById('code_yes').click()
        }

        document.getElementById('identificationNumber').value ='R1234567GB';
        document.getElementById('submit').click();
    }
}

function supervisingCustomsOffice(){
    if (currentPageIs('/customs-declare-exports/declaration/supervising-customs-office')) {
        switch(getDeclaration()){
            case 'A':
            case 'Z':
                break;
            case 'B':
            case 'C':
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
                selectFromAutoPredict(document.getElementById('supervisingCustomsOffice-container'), "GBBEL004");
                break;
            default:
                selectFromAutoPredict(document.getElementById('supervisingCustomsOffice-container'), "GBLBA001");
        }
        document.getElementById('submit').click();
    }
}

function inlandTransportDetails(){
    if (currentPageIs('/customs-declare-exports/declaration/inland-transport-details')) {
        selectRadioOptionFromInputs(document.getElementsByName('inlandModeOfTransportCode'), 0);
        document.getElementById('submit').click();
    }
}

function departureTransport(){
    if (currentPageIs('/customs-declare-exports/declaration/departure-transport')) {
        selectRadioOptionFromInputs(document.getElementsByName('meansOfTransportOnDepartureType'), 1)
        document.getElementById('meansOfTransportOnDepartureIDNumber_11').value = 'SHIP1'
        document.getElementById('submit').click();
    }
}

function borderTransport(){
    if (currentPageIs('/customs-declare-exports/declaration/border-transport')) {
        selectFromAutoPredict(document.getElementById('borderTransportNationality-container'), "United Kingdom");
        document.getElementById('nameOfVessel').checked = 'checked';
        document.getElementById('borderTransportReference_nameOfVessel').value = 'Superfast Hawk Millenium';
        document.getElementById('submit').click();
    }
}

function transportPayment(){
    if (currentPageIs('/customs-declare-exports/declaration/transport-payment')) {
        selectRadioOptionFromInputs(document.getElementsByName('paymentMethod'), 4);
        document.getElementById('submit').click();
    }
}

function addContainer(){
    if (currentPageIs('/customs-declare-exports/declaration/container')) {
        document.getElementById('code_yes').checked = 'checked';
        document.getElementById('id').value = '123456';
        document.getElementById('submit').click()
    }
}

function addSeals(){
    if (currentPageIs('/customs-declare-exports/declaration/containers/123456/seals')) {
        selectRadioOptionFromInputs(document.getElementsByName('yesNo'), 1);
        document.getElementById('submit').click();
    }
}

function containersSummary() {
    if (currentPageIs('/customs-declare-exports/declaration/containers')) {
        selectRadioOptionFromInputs(document.getElementsByName('yesNo'), 1);
        document.getElementById('submit').click();
    }
}

function summary(){
    if (currentPageIs('/customs-declare-exports/declaration/summary')) {
        document.getElementById('fullName').value = 'Tim Tester';
        document.getElementById('jobRole').value = 'Tester';
        document.getElementById('email').value = 'tim@testing.com';
        document.getElementById('confirmation').click();

        setDeclaration(0);
    }
}

// grand journey
function completeJourney() {

    // main
    startPage();
    choicePage();
    declarationChoice();
    dispatchLocation();
    additionalDeclarationType();
    consignmentRefereences();

    // parties
    isEntryIntoDeclarantsRecords();
    personPresentingGoodsDetails();
    declarantDetails();
    isDeclarantExporter();
    exporterDetails();
    isExs();
    consigneeDetails();
    consignorEoriNumber();
    consignorAddress();
    representingAnotherAgent();
    representativeEori();
    representativeType()
    carrierEoriNumber();
    carrierDetails();
    otherPartiesInvolved();
    holderOfAuthorisation();
    authorisationsSummary();

    // locations
    originationCountry();
    destinationCountry();
    countryOfRouting();
    countriesOfRouting();
    countriesOfRoutingSummary();
    locationOfGoods();
    officeOfExit();

    // transaction
    totalNumberOfItems();
    totalPackageQuantity();
    natureOfTransaction();
    previousDocuments();
    previousDocumentsSummary();

    // items
    addFirstItem();
    procedureCodes();
    fiscalInformation();
    fiscalReferences();
    fiscalReferencesSummary();
    commodityDetails();
    unDangerousGoodsCode();
    cusCode();
    additionalTaricCodes();
    nactCodes();
    statisticalValue();
    addPackageInformation();
    packageInformationSummary();
    commodityMeasurments();
    additionalInformationSummary();
    isAdditionalInformationRequired();
    addAdditionalInformation();
    addDocumentsSummary();
    addDocuments();
    exportItems();

    // transport
    transportLeavingBorder();
    warehouseIdentification();
    supervisingCustomsOffice();
    inlandTransportDetails();
    departureTransport();
    borderTransport();
    transportPayment();

    // container
    addContainer();
    addSeals();
    containersSummary();

    // summary and confirmation
    summary();
}