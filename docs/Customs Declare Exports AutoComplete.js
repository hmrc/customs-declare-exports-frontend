// ==UserScript==
// @name         Customs Declare Exports AutoComplete
// @namespace    http://tampermonkey.net/
// @version      1.18
// @description  try to take over the world!
// @author       You
// @match        http*://*/customs-declare-exports*
// @grant GM_setValue
// @grant GM_getValue
// @updateURL    https://raw.githubusercontent.com/hmrc/customs-declare-exports-frontend/master/docs/Customs%20Declare%20Exports%20AutoComplete.js
// ==/UserScript==

(function() {
    'use strict';
    document.getElementsByTagName("body")[0].appendChild(createDropDown());
})();

function setDeclaration(choice) { GM_setValue("declaration", choice); }

function getDeclaration() { return GM_getValue("declaration"); }

function createDropDown() {
    var myParent = document.body;
    var panel = myParent.appendChild(document.createElement("div"));

    panel.appendChild(createQuickButton());

    // create array of options to be added
    let text = ["Standard-PreLodged",
                "Standard-Frontier",
                "Simplified-PreLodged",
                "Simplified-Frontier",
                "Supplementary-SDP",
                "Supplementary-EIDR",
                "Occasional-PreLodged",
                "Occasional-Frontier",
                "Clearance-PreLodged",
                "Clearance-Frontier"
               ];
    let value = ["D", "A", "F", "C", "Y", "Z", "E", "B", "K", "J"]

    // create and append select list
    var selectList = document.createElement("select");
    selectList.style.position = "absolute"
    selectList.style.top = "100px"
    selectList.id = "mySelect";
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
        selectList.value = getDeclaration();
    }
}

function createQuickButton() {
    let button = document.createElement('button');
    button.id="quickSubmit";
    if (!!document.getElementById('global-header')) {
        button.classList.add('button-start');
    } else {
        button.classList.add('govuk-button');
    }
    button.style.position = "absolute"
    button.style.top = "50px"
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

function findRadioOption(fieldname) {
    var inputs = document.getElementById(fieldname).getElementsByTagName('input');
    var choice;
    for(var i = 0; i < inputs.length; i++){
        if(inputs[i].checked){
            choice = inputs[i].value;
        }
    }
    return choice;
}

function currentPageIs(path) {
    let matches = window.location.pathname.match(path);
    return matches && matches.length > 0
}

function completePage() {
    if (currentPageIs('/customs-declare-exports/start')) {

        if(getDeclaration() == 0){
            alert("Select journey type");
            return;
        }

        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-declare-exports/choice")){

        if(getDeclaration() == 0){
            alert("Select journey type");
            return;
        }

        document.getElementById("CRT").checked = true
        document.getElementsByClassName('govuk-button')[0].click()
    }
    if(currentPageIs("/customs-declare-exports/declaration/declaration-choice")){

        if(getDeclaration() == 0){
            alert("Select journey type");
            return;
        }

        switch(getDeclaration()) {
            case 'D':
            case 'A':
               selectRadioOption(document.getElementById("type"), 0);
            break;
            case 'F':
            case 'C':
               selectRadioOption(document.getElementById("type"), 1);
            break;
            case 'Y':
            case 'Z':
               selectRadioOption(document.getElementById("type"), 2);
            break;
            case 'E':
            case 'B':
               selectRadioOption(document.getElementById("type"), 3);
            break;
            case 'K':
            case 'J':
               selectRadioOption(document.getElementById("type"), 4);
            break;
        }
        document.getElementsByClassName('button')[0].click();
    }
    if(currentPageIs("/customs-declare-exports/declaration/dispatch-location")){

        selectRadioOption(document.getElementById("dispatchLocation"), 0);
        document.getElementsByClassName('button')[0].click()
    }
    if(currentPageIs("/customs-declare-exports/declaration/type")){
        // top values
        if (['D','F','Y','E','K'].indexOf(getDeclaration()) > -1) {
            selectRadioOption(document.getElementById("additionalDeclarationType"), 0);
        } else {
            selectRadioOption(document.getElementById("additionalDeclarationType"), 1);
        }
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/consignment-references")) {
        document.getElementById('lrn').value = 'QSLRN' + Math.floor(Math.random() * 8999) + 100;
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
        document.getElementById('details_address_fullName').value = 'XYZ Carrier';
        document.getElementById('details_address_addressLine').value = 'School Road';
        document.getElementById('details_address_townOrCity').value = 'London';
        document.getElementById('details_address_postCode').value = 'WS1 2AB';
        selectFromAutoPredict(document.getElementById('details.address.country-container'), "United Kingdom");
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/holder-of-authorisation")) {
        switch(getDeclaration())
        {
            // simplified
            case 'F':
            case 'C':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "SDE");
            break;
            // clearance frontier
            case 'J':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "EIR");
            break;
            default:
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "AEOC");
       }

        document.getElementById('eori').value = 'GB717572504502811';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/origination-country")) {
        selectFromAutoPredict(document.getElementById("country-container"), "GB");
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/destination-country")) {
        selectFromAutoPredict(document.getElementById("country-container"), "DE");
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/country-of-routing")) {
        document.getElementById('Yes').click()
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/countries-of-routing")) {
        selectFromAutoPredict(document.getElementById("country-container"), "FR");
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/countries-summary")) {
        document.getElementById('No').click()
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs("/customs-declare-exports/declaration/location-of-goods")) {
        document.getElementById('typeOfLocation').value ='A';
        document.getElementById('qualifierOfIdentification').value ='U';
        document.getElementById('identificationOfLocation').value ='FXTFXTFXT';
        document.getElementById('additionalIdentifier').value ='123';
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
        document.getElementById('procedureCode').value ='1040';
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
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/commodity-details')) {
        document.getElementById('combinedNomenclatureCode').value ='46021910';
        document.getElementById('descriptionOfGoods').value ='Straw for bottles';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/un-dangerous-goods-code')) {
        document.getElementById('code_yes').checked = 'checked';
        document.getElementById('dangerousGoodsCode').value ='1234';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/cus-code')) {
        document.getElementById('code_yes').checked = 'checked';
        document.getElementById('cusCode').value ='12345678';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/taric-codes')) {
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/nact-codes')) {
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/statistical-value')) {
        document.getElementById('statisticalValue').value ='1000';
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
        switch(getDeclaration())
        {
            // simplified
            case 'F':
            case 'C':
                document.getElementById('documentTypeCode').value ='C512';
                document.getElementById('documentIdentifier').value ='GBSDE717572504502811';
            break;
            // clearance frontier
            case 'J':
                document.getElementById('documentTypeCode').value ='C514';
                document.getElementById('documentIdentifier').value ='GBSDE717572504502811';
            break;
            default:
                document.getElementById('documentTypeCode').value ='C501';
                document.getElementById('documentIdentifier').value ='GBAEOC717572504502811';
       }

        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/warehouse-identification')) {
        //document.getElementById('identificationNumber').value ='R1234567GB';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/supervising-customs-office')) {
        selectFromAutoPredict(document.getElementById('supervisingCustomsOffice-container'), "GBLBA001");
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/inland-transport-details')) {
        selectRadioOption(document.getElementById('inlandModeOfTransportCode'), 0);
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/departure-transport')) {
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
    if (currentPageIs('/customs-declare-exports/declaration/border-transport')) {
        selectFromAutoPredict(document.getElementById('borderTransportNationality-container'), "United Kingdom");
        document.getElementById('nameOfVessel').checked = 'checked';
        document.getElementById("borderTransportReference_nameOfVessel").value = 'Boaty McBoatface';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/transport-payment')) {
        selectRadioOption(document.getElementById('paymentMethod'), 4)
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/containers/123456/seals')) {
        document.getElementById('No').checked = 'checked';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/containers')) {
        selectRadioOption(document.getElementById('yesNo'), 1);
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/container')) {
        document.getElementById('code_yes').checked = 'checked';
        document.getElementById('id').value = '123456';
        document.getElementsByClassName('button')[0].click()
    }
    if (currentPageIs('/customs-declare-exports/declaration/summary')) {
        document.getElementById("fullName").value = 'Tim Tester';
        document.getElementById("jobRole").value = 'Tester';
        document.getElementById("email").value = 'tim@testing.com';
        document.getElementById("confirmation").click()
        document.getElementsByClassName('button')[0].click()

        setDeclaration(0);
    }
    if (currentPageIs('/customs-declare-exports/declaration/confirmation')) {
        document.getElementsByClassName('button')[0].click()
    }
}
