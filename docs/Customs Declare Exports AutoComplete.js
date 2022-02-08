// ==UserScript==
// @name         Customs Declare Exports AutoComplete
// @namespace    http://tampermonkey.net/
// @version      1.67
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
function setDeclaration(choice) {

    GM_setValue("declaration", choice);
    setAdditionalDocuments()
    setHoldersOfAuthorisation()
}

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
        let matches = window.location.pathname.match(path)
        return (matches && window.location.pathname.endsWith(path.slice(-5)))
    } else {
        return (path == window.location.pathname);
    }
}

function getRandomInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

// actual pages
function startPage(){
    if (currentPageIs('/customs-declare-exports/start')) {
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
            case 'A': // Standard
                selectRadioOptionFromInputs(inputs, 0);
                break;
            case 'F':
            case 'C': // Simplified
                selectRadioOptionFromInputs(inputs, 1);
                break;
            case 'K':
            case 'J': // Clearance
                selectRadioOptionFromInputs(inputs, 2);
                break;
            case 'Y':
            case 'Z': // Supplementary
                selectRadioOptionFromInputs(inputs, 3);
                break;
            case 'E':
            case 'B': // Occasional
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
        if (['A','C','Y','B','J'].indexOf(getDeclaration()) > -1) {
            selectRadioOptionFromInputs(document.getElementsByName('additionalDeclarationType'), 0);
        } else {
            selectRadioOptionFromInputs(document.getElementsByName('additionalDeclarationType'), 1);
        }
        document.getElementById('submit').click()
    }
}

function consignmentReferences(){
    if (currentPageIs('/customs-declare-exports/declaration/consignment-references')) {
        document.getElementById('lrn').value = 'QSLRN' + Math.floor(Math.random() * 8999) + 100;
        document.getElementById('ducr_ducr').value = '8GB123456' + Math.floor(Math.random() * 899999 + 100000) + '-101SHIP1';

        switch(getDeclaration()) {
            case 'Y':
                document.getElementById('mrn').value = '20GB46J8TMJ4RF' + Math.floor(Math.random() * 8999);
                break;
            case 'Z':
                document.getElementById('eidrDateStamp').value = '2021' + getRandomInt(10,12) + getRandomInt(1,30);
                break;
        }

        document.getElementById('submit').click()
    }
}

function linkDucrToMucr() {
    if (currentPageIs('/customs-declare-exports/declaration/link-to-mucr')) {
        document.getElementById('code_yes').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function enterMucr() {
    if (currentPageIs('/customs-declare-exports/declaration/enter-a-mucr')) {
        document.getElementById('MUCR').value = 'CZYX123A';
        document.getElementById('submit').click();
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
        document.getElementById('answer_yes').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function exporterDetails(){
    if (currentPageIs('/customs-declare-exports/declaration/exporter-address')) {
        document.getElementById('details_address_fullName').value = 'Exporters R Us';
        document.getElementById('details_address_addressLine').value = '1 Uxbridge Road';
        document.getElementById('details_address_townOrCity').value = 'London';
        document.getElementById('details_address_postCode').value = 'WC1 3RD';

        selectFromAutoPredict(document.getElementById('details_address_country-container'), "United Kingdom, Great Britain, Northern Ireland");
        document.getElementById('submit').click()

    }
}

function exporterEoriNumber(){
    if (currentPageIs('/customs-declare-exports/declaration/exporter-eori-number')) {
        document.getElementById('Yes').checked = 'checked';
        document.getElementById('eori').value = 'GB123456789000';
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

        selectFromAutoPredict(document.getElementById('details_address_country-container'), "United States of America (the), Including Puerto Rico");
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
    if (currentPageIs('/customs-declare-exports/declaration/carrier-address')) {
        document.getElementById('details_address_fullName').value = 'XYZ Carrier';
        document.getElementById('details_address_addressLine').value = 'School Road';
        document.getElementById('details_address_townOrCity').value = 'London';
        document.getElementById('details_address_postCode').value = 'WS1 2AB';
        selectFromAutoPredict(document.getElementById('details_address_country-container'), "United Kingdom, Great Britain, Northern Ireland");
        document.getElementById('submit').click()
    }
}

function carrierEoriNumber(){
    if (currentPageIs('/customs-declare-exports/declaration/carrier-eori-number')) {
        document.getElementById('No').checked = 'checked';
        document.getElementById('submit').click()
    }
}

function consigneeDetails(){
    if (currentPageIs('/customs-declare-exports/declaration/consignee-details')) {
        document.getElementById('details_address_fullName').value = 'Bags Export';
        document.getElementById('details_address_addressLine').value = '1 Bags Avenue';
        document.getElementById('details_address_townOrCity').value = 'New York';
        document.getElementById('details_address_postCode').value = '10001';

        selectFromAutoPredict(
            document.getElementById('details_address_country-container'),
            "United States of America (the), Including Puerto Rico"
        );
        document.getElementById('submit').click()
    }
}

function otherPartiesInvolved(){
    if (currentPageIs('/customs-declare-exports/declaration/other-parties-involved')) {
        selectRadioOptionFromInputs(document.getElementsByName('partyType'), 4);
        document.getElementById('submit').click()
    }
}

function otherPartiesList(){
    if (currentPageIs('/customs-declare-exports/declaration/other-parties-list')) {
        document.getElementById('code_no').checked = 'checked';
        document.getElementById('submit').click();
    }
}

function authorisationProcedureCodeChoice(){
    if (currentPageIs('/customs-declare-exports/declaration/authorisation-choice')) {
        selectRadioOptionFromInputs(document.getElementsByName('authorisationProcedureCodeChoice'), 0);
        document.getElementById('submit').click();
    }
}

function isAuthorisationRequired(){
    if (currentPageIs('/customs-declare-exports/declaration/is-authorisation-required')) {
        document.getElementById('code_yes').checked = 'checked';
        document.getElementById('submit').click()
    }
}

// holder of authorisation
function getHoACode(dec) {

    var decType = "hoa-code" + dec

    var codes = GM_getValue(decType)
    var code = codes.pop()
    GM_setValue(decType, codes)

    return code;
}

function getHoAEori(dec) {

    var decType = "hoa-eori" + dec

    var eoris = GM_getValue(decType)
    var eori = eoris.pop()
    GM_setValue(decType, eoris)

    return eori;
}

function setHoldersOfAuthorisation() {

    GM_setValue("hoa-codeC", ['CSE', 'SDE'])
    GM_setValue("hoa-eoriC", ['GB717572504502801', 'GB717572504502801'])
    GM_setValue("hoa-lengthC", 2)
}

function holderOfAuthorisation(){
    if (currentPageIs('/customs-declare-exports/declaration/add-authorisation-required')) {
        selectRadioOptionFromInputs(document.getElementsByName('eoriSource'), 1)
        switch(getDeclaration())
        {
            case 'C':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), getHoACode("C"));
                document.getElementById('eori').value = getHoAEori("C");
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
            case 'K':
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
                document.getElementById('eori').value = 'GB717572504502801';
                document.getElementById('submit').click();
                break;
            case 'D':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "AEOC");
                document.getElementById('eori').value = 'GB717572504502801';
                document.getElementById('submit').click();
                break;
            case 'E':
                document.getElementById('submit').click();
                break;
        }
    }
}

function authorisationsSummary(){
    if (currentPageIs('/customs-declare-exports/declaration/authorisations-required')) {
        switch(getDeclaration()) {
            case 'C':
                if(document.getElementsByClassName("govuk-table")[0].querySelectorAll("tbody tr").length < GM_getValue("hoa-lengthC")) {
                    document.getElementById('code_yes').checked = 'checked';
                    document.getElementById('submit').click();
                } else {
                    document.getElementById('code_no').checked = 'checked';
                    document.getElementById('submit').click();
                }
                break;
            default:
                document.getElementById('code_no').checked = 'checked';
                document.getElementById('submit').click();
                break;
        }
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
        switch(getDeclaration()){
            case 'F':
                document.getElementById('Yes').click();
                break;
            default:
                document.getElementById('No').click();
                break;
        }

        document.getElementById('submit').click()
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
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
                document.getElementById('code').value = 'GBAUBELBFSBEL';
                break;
            case 'J':
            case 'C':
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
    if (currentPageIs('/customs-declare-exports/declaration/invoices-and-exchange-rate')) {
        document.getElementById('exchangeRate').value ='1.49';
        document.getElementById('totalAmountInvoicedCurrency').value ='GBP';
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

function previousDocuments() {
    if (currentPageIs('/customs-declare-exports/declaration/add-previous-document')) {
        if (getDeclaration() == 'J') {
            selectFromAutoPredict(document.getElementById('documentType-container'), "IF3");
            document.getElementById('documentReference').value ='101SHIP2';
        } else {
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
            case 'D':
            case 'F':
            case 'J':
            case 'Y':
                selectFromAutoPredict(document.getElementById('procedureCode-container'), '1040');
                break;
            case 'E':
                selectFromAutoPredict(document.getElementById('procedureCode-container'), '1040');
                break;
            case 'K':
                selectFromAutoPredict(document.getElementById('procedureCode-container'), '0012');
                break;
            case 'Z':
                selectFromAutoPredict(document.getElementById('procedureCode-container'), '1040');
                break;
            default:
                selectFromAutoPredict(document.getElementById('procedureCode-container'), '1040');
        }
        document.getElementById('submit').click();
    }
}

function additionalProcedureCodes(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-procedure-codes')) {
        selectFromAutoPredict(document.getElementById('additionalProcedureCode-container'), '000');
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
            case 'A':
                document.getElementById("combinedNomenclatureCode").value = '2929100000';
                document.getElementById('descriptionOfGoods').value ='nonblockingdocumentary';
                break;
            case 'B':
            case 'C':
            case 'E':
            case 'F':
            case 'Y':
                document.getElementById('combinedNomenclatureCode').value ='8411110000';
                document.getElementById('descriptionOfGoods').value ='Aircraft engine';
                break;
            case 'Z':
                document.getElementById('combinedNomenclatureCode').value ='8538909900';
                document.getElementById('descriptionOfGoods').value ='Oilwell equipment';
                break;
            default:
                document.getElementById('combinedNomenclatureCode').value ='4602191000';
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

function commodityMeasurements(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/commodity-measure')) {
        document.getElementById('grossMass').value ='700';
        document.getElementById('netMass').value ='500';
        document.getElementById('submit').click()
    }
}

function supplementaryUnits(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/supplementary-units')) {
        if (document.getElementById('Yes')) document.getElementById('Yes').checked = 'checked';
        document.getElementById('supplementaryUnits').value ='1000';
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

// additional documents
function getAdCode(dec) {

    var decType = "ad-code" + dec

    var codes = GM_getValue(decType)
    var code = codes.pop()
    GM_setValue(decType, codes)

    return code;
}

function getAdIdent(dec) {

    var decType = "ad-idents" + dec

    var idents = GM_getValue(decType)
    var ident = idents.pop()
    GM_setValue(decType, idents)

    return ident;
}

function areAdditionalDocumentsRequired() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/is-additional-documentation-required')) {
        document.getElementById('code_yes').checked = 'checked';
        document.getElementById('submit').click()
    }
}

function setAdditionalDocuments() {

    GM_setValue("ad-codeA", ['Y923', 'Y957', 'C676'])
    GM_setValue("ad-identsA", ['005345678', '005345678', 'GBCSE717572504502801'])
    GM_setValue("ad-lengthA", 3)

    GM_setValue("ad-codeC", ['C676', 'C512'])
    GM_setValue("ad-identsC", ['GBCSE717572504502801', 'GBCSE717572504502801'])
    GM_setValue("ad-lengthC", 2)
}

function addDocuments(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-documentation')) {
        switch(getDeclaration())
        {
            case 'A':
                document.getElementById('documentTypeCode').value = getAdCode("A");
                document.getElementById('documentIdentifier').value = getAdIdent("A");
                document.getElementById('documentStatusReason').value = "Reason";
                document.getElementById('submit').click();

                break;
            case 'C':
                document.getElementById('documentTypeCode').value = getAdCode("C");
                document.getElementById('documentIdentifier').value = getAdIdent("C");
                document.getElementById('submit').click();

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
            case 'B':
                document.getElementById('documentTypeCode').value = 'C676';
                document.getElementById('documentIdentifier').value = 'GBCSE717572504502801';
                document.getElementById('submit').click();
                break;
            case 'E':
            case 'K':
            default:
                document.getElementById('documentTypeCode').value ='C501';
                document.getElementById('documentIdentifier').value ='GBAEOC717572504502801';
                document.getElementById('submit').click();
        }
    }
}

function addDocumentsSummary(){
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-documentation-list')) {
        switch(getDeclaration()) {
            case 'A':
                if(document.getElementById("additional_documents").querySelectorAll("tbody tr").length < GM_getValue("ad-lengthA")) {
                    document.getElementById('code_yes').checked = 'checked';
                    document.getElementById('submit').click();
                } else {
                    document.getElementById('code_no').checked = 'checked';
                    document.getElementById('submit').click();
                }
                break;
            case 'C':
                if(document.getElementById("additional_documents").querySelectorAll("tbody tr").length < GM_getValue("ad-lengthC")) {
                    document.getElementById('code_yes').checked = 'checked';
                    document.getElementById('submit').click();
                } else {
                    document.getElementById('code_no').checked = 'checked';
                    document.getElementById('submit').click();
                }
                break;
            default:
                document.getElementById('code_no').checked = 'checked';
                document.getElementById('submit').click();
                break;
        }
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
        // K
        if(document.getElementById('code_no')) {
            document.getElementById('code_no').click()
        } else {
            document.getElementById('identificationNumber').value = 'R1234567GB';
        }

        document.getElementById('submit').click();
    }
}

function supervisingCustomsOffice(){
    if (currentPageIs('/customs-declare-exports/declaration/supervising-customs-office')) {
        switch(getDeclaration()){
            case 'A':
            case 'D':
            case 'Z':
                break;
            case 'B':
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

function inlandOrBorder(){
    if (currentPageIs('/customs-declare-exports/declaration/inland-or-border')) {
        selectRadioOptionFromInputs(document.getElementsByName('location'), 0);
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
        selectRadioOptionFromInputs(document.getElementsByName('departureTransportType'), 3)
        document.getElementById('flightNumber').value = 'BA3456220821'
        document.getElementById('submit').click();
    }
}

function borderTransport(){
    if (currentPageIs('/customs-declare-exports/declaration/border-transport')) {
        selectFromAutoPredict(document.getElementById('borderTransportNationality-container'), "United Kingdom, Great Britain, Northern Ireland");
        selectRadioOptionFromInputs(document.getElementsByName('borderTransportType'), 1)
        document.getElementById('nameOfVessel').value = 'Superfast Hawk Millenium';
        document.getElementById('submit').click();
    }
}

function expressConsignment() {
    if (currentPageIs('/customs-declare-exports/declaration/express-consignment')) {
        document.getElementById('code_yes').checked = 'checked';
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
    consignmentReferences();
    linkDucrToMucr();
    enterMucr();

    // parties
    isEntryIntoDeclarantsRecords();
    personPresentingGoodsDetails();
    declarantDetails();
    isDeclarantExporter();
    exporterEoriNumber();
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
    otherPartiesList();
    authorisationProcedureCodeChoice();
    isAuthorisationRequired();
    holderOfAuthorisation();
    authorisationsSummary();

    // locations
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
    additionalProcedureCodes();
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
    commodityMeasurements();
    supplementaryUnits();
    additionalInformationSummary();
    isAdditionalInformationRequired();
    addAdditionalInformation();
    areAdditionalDocumentsRequired();
    addDocumentsSummary();
    addDocuments();
    exportItems();

    // transport
    transportLeavingBorder();
    warehouseIdentification();
    supervisingCustomsOffice();
    inlandOrBorder();
    inlandTransportDetails();
    departureTransport();
    borderTransport();
    expressConsignment();
    transportPayment();

    // container
    addContainer();
    addSeals();
    containersSummary();

    // summary and confirmation
    summary();
}
