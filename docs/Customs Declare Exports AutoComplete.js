// ==UserScript==
// @name         Customs Declare Exports AutoComplete
// @namespace    http://tampermonkey.net/
// @version      1.70
// @description  decs supported: (Std-Arrived A), (Occ-Arrived B), (Smp-Arrived C), (Std-PreLodged D), (Occ-PreLodged E), (Smp-PreLodged F), (Clr-Arrived J), (Clr-PreLodged K), (Sup-SDP Y), (Sup-EIDR Z)
// @author       You
// @match        http*://*/customs-declare-exports*
// @grant GM_setValue
// @grant GM_getValue
// @updateURL    https://raw.githubusercontent.com/hmrc/customs-declare-exports-frontend/master/docs/Customs%20Declare%20Exports%20AutoComplete.js
// @downloadURL  https://raw.githubusercontent.com/hmrc/customs-declare-exports-frontend/master/docs/Customs%20Declare%20Exports%20AutoComplete.js
// ==/UserScript==

(function() {
    'use strict'
    document.body.appendChild(dropDown())
})()

// helper methods
function setDeclaration(choice) {
    GM_setValue("declaration", choice)
    setAdditionalDocuments()
    setHoldersOfAuthorisation()
}

function decTypes() {
    const decTypes = [
        {"name": "Standard-PreLodged", "value": "D", "id": "STANDARD", "addId":"prelodged"},
        {"name": "Standard-Arrived", "value": "A", "id": "STANDARD", "addId":"arrived"},
        {"name": "Simplified-PreLodged", "value": "F", "id": "SIMPLIFIED", "addId":"prelodged"},
        {"name": "Simplified-Arrived", "value": "C", "id": "SIMPLIFIED", "addId":"arrived"},
        {"name": "Clearance-PreLodged", "value": "K", "id": "CLEARANCE", "addId":"prelodged"},
        {"name": "Clearance-Arrived", "value": "J", "id": "CLEARANCE", "addId":"arrived"},
        {"name": "Supplementary-SDP", "value": "Y", "id": "SUPPLEMENTARY", "addId":"simplified"},
        {"name": "Supplementary-EIDR", "value": "Z", "id": "SUPPLEMENTARY", "addId":"eidr"},
        {"name": "Occasional-PreLodged", "value": "E", "id": "OCCASIONAL", "addId":"prelodged"},
        {"name": "Occasional-Arrived", "value": "B", "id": "OCCASIONAL", "addId":"arrived"}
    ]
    return decTypes
}

function getDeclaration() { return GM_getValue("declaration") }

function submit() {
    document.getElementById('submit').click()
}

function dropDown() {
    var panel = document.createElement("div")

    panel.appendChild(createQuickButton())

    // create and append select list
    var selectList = document.createElement("select")
    selectList.style.position = "absolute"
    selectList.style.top = "100px"
    selectList.id = "mySelect"
    selectList.className = "govuk-!-display-none-print"
    panel.appendChild(selectList)

    // create and append the options
    decTypes().map(type => createAndAppend(type))

    function createAndAppend(type) {
        var option = document.createElement("option")
        option.value = type["value"]
        option.text = type["name"]
        selectList.appendChild(option)
    }

    selectList.onchange = function (e) { setDeclaration(this.value) }

    // empty on start
    if (currentPageIs('/customs-declare-exports/start')) {
        selectList.value = 0
        setDeclaration(0)
    } else {
        selectList.value = getDeclaration()
    }
    return panel
}

function createQuickButton() {
    let button = document.createElement('button')
    button.id="quickSubmit"

    if (!!document.getElementById('global-header')) {
        button.classList.add('button-start', 'govuk-!-display-none-print')
    } else {
        button.classList.add('govuk-button','govuk-!-display-none-print')
    }

    button.style.position = "absolute"
    button.style.top = "50px"
    button.innerHTML = 'Quick Submit'
    button.onclick = () => completeJourney()

    return button
}

function selectFromAutoPredict(element, selected) {
    let index = typeof selected == "number" ? selected : 0
    let selects = element.getElementsByTagName('select')
    let inputs = element.getElementsByTagName('input')

    for (let j = 0; j < selects.length; j++) {
        let options = selects[j].getElementsByTagName('option')
        let option = options[index]
        if (typeof selected == "string") {
            for (let o = 0; o < options.length; o++) {
                if (options[o].value === selected) {
                    option = options[o]
                }
            }
        }
        option.selected = "selected"
        selects[j].value = option.value
        inputs[j].value = option.value
    }
}

function selectDecType(type) {
    document.getElementById(decTypes().filter(type =>
        type["value"] == getDeclaration()).shift()[type]).checked = true
}

function selectRadioOption(element, index) {
    let inputs = element.getElementsByTagName('input')
    if (inputs && index < inputs.length) {
        inputs[index].checked = true
    }
}

function selectRadioOptionFromInputs(inputs, index) {
    if (inputs && index < inputs.length) {
        inputs[index].checked = true
    }
}

function findRadioOption(fieldname) {
    let inputs = document.getElementById(fieldname).getElementsByTagName('input')
    let choice

    for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].checked) {
            choice = inputs[i].value
        }
    }

    return choice
}

function currentPageIs(path) {
    if (path.includes("*")) {
        let matches = window.location.pathname.match(path)
        return (matches && window.location.pathname.endsWith(path.slice(-5)))
    } else {
        return (path == window.location.pathname)
    }
}

function getRandomInt(min, max) {
    min = Math.ceil(min)
    max = Math.floor(max)
    return Math.floor(Math.random() * (max - min + 1)) + min
}

// actual pages
function startPage() {
    if (currentPageIs('/customs-declare-exports/start')) {
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

function choicePage() {
    if (currentPageIs('/customs-declare-exports/choice')) {
        if (getDeclaration() == 0) {
            alert("Select journey type")
            return
        }

        document.getElementById('create-declaration').click()
    }
}

function declarationChoice() {
    if (currentPageIs('/customs-declare-exports/declaration/declaration-choice')){
        if (getDeclaration() == 0) {
            alert("Select journey type")
            return
        }

        selectDecType("id")
        submit()
    }
}

function dispatchLocation() {
    if (currentPageIs('/customs-declare-exports/declaration/dispatch-location')){
        selectRadioOptionFromInputs(document.getElementsByName('dispatchLocation'), 0)
        submit()
    }
}

function additionalDeclarationType() {
    if (currentPageIs('/customs-declare-exports/declaration/type')){
        selectDecType("addId")
        submit()
    }
}

function doYouHaveDucr() {
    if (currentPageIs('/customs-declare-exports/declaration/do-you-have-ducr')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
    }
}

function ducrEntry() {
    if (currentPageIs('/customs-declare-exports/declaration/ducr-entry')) {
        document.getElementById('ducr').value = '8GB123456' + Math.floor(Math.random() * 899999 + 100000) + '-101SHIP1'
        submit()
    }
}

function localReferenceNumber() {
    if (currentPageIs('/customs-declare-exports/declaration/local-reference-number')) {
        document.getElementById('lrn').value = 'QSLRN' + Math.floor(Math.random() * 8999) + 100
        submit()
    }
}

function traderReference() {
    if (currentPageIs('/customs-declare-exports/declaration/trader-reference')) {
        document.getElementById('traderReferenceInput').value = 'INV' + Math.floor(Math.random() * 899999 + 100000) + '/101SHIP1'
        submit()
    }
}

function confirmDucr() {
    if (currentPageIs('/customs-declare-exports/declaration/confirm-ducr')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
    }
}

function consignmentReferences() {
    if (currentPageIs('/customs-declare-exports/declaration/consignment-references')) {
        document.getElementById('lrn').value = 'QSLRN' + Math.floor(Math.random() * 8999) + 100
        document.getElementById('ducr_ducr').value = '8GB123456' + Math.floor(Math.random() * 899999 + 100000) + '-101SHIP1'

        switch(getDeclaration()) {
            case 'Y':
                document.getElementById('mrn').value = '20GB46J8TMJ4RF' + Math.floor(Math.random() * 8999)
                break
            case 'Z':
                document.getElementById('eidrDateStamp').value = '2021' + getRandomInt(10,12) + getRandomInt(1,30)
                break
        }

        submit()
    }
}

function linkDucrToMucr() {
    if (currentPageIs('/customs-declare-exports/declaration/link-to-mucr')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
    }
}

function enterMucr() {
    if (currentPageIs('/customs-declare-exports/declaration/enter-a-mucr')) {
        document.getElementById('MUCR').value = 'CZYX123A'
        submit()
    }
}

// clearance
function isEntryIntoDeclarantsRecords() {
    if (currentPageIs('/customs-declare-exports/declaration/entry-into-declarants-records')) {
        document.getElementById('answer_no').checked = 'checked'
        submit()
    }
}

// clearance
function personPresentingGoodsDetails() {
    if (currentPageIs('/customs-declare-exports/declaration/person-presenting-goods')) {
        document.getElementById('eori').value = 'GB614299894872549'
        submit()
    }
}

function declarantDetails() {
    if (currentPageIs('/customs-declare-exports/declaration/declarant-details')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
    }
}

function isDeclarantExporter() {
    if (currentPageIs('/customs-declare-exports/declaration/are-you-the-exporter')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
    }
}

function exporterDetails() {
    if (currentPageIs('/customs-declare-exports/declaration/exporter-address')) {
        document.getElementById('details_address_fullName').value = 'Exporters R Us'
        document.getElementById('details_address_addressLine').value = '1 Uxbridge Road'
        document.getElementById('details_address_townOrCity').value = 'London'
        document.getElementById('details_address_postCode').value = 'WC1 3RD'

        selectFromAutoPredict(document.getElementById('details_address_country-container'), "United Kingdom, Great Britain, Northern Ireland")
        submit()
    }
}

function exporterEoriNumber() {
    if (currentPageIs('/customs-declare-exports/declaration/exporter-eori-number')) {
        document.getElementById('Yes').checked = 'checked'
        document.getElementById('eori').value = 'GB123456789000'
        submit()
    }
}

// clearance
function isExs() {
    if (currentPageIs('/customs-declare-exports/declaration/is-this-exs')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
    }
}

function consignorAddress() {
    if (currentPageIs('/customs-declare-exports/declaration/consignor-address')) {
        document.getElementById('details_address_fullName').value = 'Bags Export'
        document.getElementById('details_address_addressLine').value = '1 Bags Avenue'
        document.getElementById('details_address_townOrCity').value = 'New York'
        document.getElementById('details_address_postCode').value = '10001'

        selectFromAutoPredict(document.getElementById('details_address_country-container'), "United States of America (the), Including Puerto Rico")
        submit()
    }
}

function consignorEoriNumber() {
    if (currentPageIs('/customs-declare-exports/declaration/consignor-eori-number')) {
        document.getElementById('Yes').checked = 'checked'
        document.getElementById('eori').value = 'GB123456789000'
        submit()
    }
}

function representingAnotherAgent() {
    if (currentPageIs('/customs-declare-exports/declaration/are-you-completing-this-declaration-on-behalf-of-another-agent')) {

        switch(getDeclaration()) {
            case 'A':
            case 'D':
                selectRadioOptionFromInputs(document.getElementsByName('representingAgent'), 1)
                break
            default:
                selectRadioOptionFromInputs(document.getElementsByName('representingAgent'), 0)
        }
        submit()
    }
}

function representativeEori() {
    if (currentPageIs('/customs-declare-exports/declaration/representatives-eori-number')) {
        switch(getDeclaration()) {
            case 'A':
            case 'D':
                break
            case 'B':
            case 'C':
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
            case 'Z':
                document.getElementById('details_eori').value = 'GB717572504502801'
                break
            case 'J':
                document.getElementById('details_eori').value = 'GB717572504502809'
                break
        }
        submit()
    }
}

function representativeType() {
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
                selectRadioOptionFromInputs(document.getElementsByName('statusCode'), 0)
                break
            case 'J':
                selectRadioOptionFromInputs(document.getElementsByName('statusCode'), 1)
                break
        }
        submit()
    }
}

function carrierDetails() {
    if (currentPageIs('/customs-declare-exports/declaration/carrier-address')) {
        document.getElementById('details_address_fullName').value = 'XYZ Carrier'
        document.getElementById('details_address_addressLine').value = 'School Road'
        document.getElementById('details_address_townOrCity').value = 'London'
        document.getElementById('details_address_postCode').value = 'WS1 2AB'
        selectFromAutoPredict(document.getElementById('details_address_country-container'), "United Kingdom, Great Britain, Northern Ireland")
        submit()
    }
}

function carrierEoriNumber() {
    if (currentPageIs('/customs-declare-exports/declaration/carrier-eori-number')) {
        document.getElementById('No').checked = 'checked'
        submit()
    }
}

function consigneeDetails() {
    if (currentPageIs('/customs-declare-exports/declaration/consignee-details')) {
        document.getElementById('details_address_fullName').value = 'Bags Export'
        document.getElementById('details_address_addressLine').value = '1 Bags Avenue'
        document.getElementById('details_address_townOrCity').value = 'New York'
        document.getElementById('details_address_postCode').value = '10001'

        selectFromAutoPredict(
            document.getElementById('details_address_country-container'),
            "United States of America (the), Including Puerto Rico"
        )
        submit()
    }
}

function otherPartiesInvolved() {
    if (currentPageIs('/customs-declare-exports/declaration/other-parties-involved')) {
        selectRadioOptionFromInputs(document.getElementsByName('partyType'), 4)
        submit()
    }
}

function otherPartiesList() {
    if (currentPageIs('/customs-declare-exports/declaration/other-parties-list')) {
        document.getElementById('code_no').checked = 'checked'
        submit()
    }
}

function authorisationProcedureCodeChoice() {
    if (currentPageIs('/customs-declare-exports/declaration/authorisation-choice')) {
        selectRadioOptionFromInputs(document.getElementsByName('authorisationProcedureCodeChoice'), 0)
        submit()
    }
}

function isAuthorisationRequired() {
    if (currentPageIs('/customs-declare-exports/declaration/is-authorisation-required')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
    }
}

// holder of authorisation
function getHoACode(dec) {
    var decType = "hoa-code" + dec

    var codes = GM_getValue(decType)
    var code = codes.pop()
    GM_setValue(decType, codes)

    return code
}

function getHoAEori(dec) {
    var decType = "hoa-eori" + dec

    var eoris = GM_getValue(decType)
    var eori = eoris.pop()
    GM_setValue(decType, eoris)

    return eori
}

function setHoldersOfAuthorisation() {
    GM_setValue("hoa-codeC", ['CSE', 'SDE'])
    GM_setValue("hoa-eoriC", ['GB717572504502801', 'GB717572504502801'])
    GM_setValue("hoa-lengthC", 2)
}

function holderOfAuthorisation() {
    if (currentPageIs('/customs-declare-exports/declaration/add-authorisation-required')) {
        selectRadioOptionFromInputs(document.getElementsByName('eoriSource'), 1)
        switch(getDeclaration())
        {
            case 'C':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), getHoACode("C"))
                document.getElementById('eori').value = getHoAEori("C")
                submit()
                break
            case 'Z':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "EIR")
                document.getElementById('eori').value = 'GB717572504502802'
                submit()
                break
            case 'F':
            case 'Y':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "SDE")
                document.getElementById('eori').value = 'GB717572504502801'
                submit()
                break
            case 'J':
            case 'K':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "EIR")
                document.getElementById('eori').value = 'GB717572504502811'
                submit()
                break
            case 'B':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "CSE")
                document.getElementById('eori').value = 'GB717572504502801'
                submit()
                break
            case 'A':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "CSE")
                document.getElementById('eori').value = 'GB717572504502801'
                submit()
                break
            case 'D':
            case 'E':
                selectFromAutoPredict(document.getElementById('authorisationTypeCode-container'), "AEOC")
                document.getElementById('eori').value = 'GB717572504502801'
                submit()
                break
        }
    }
}

function authorisationsSummary() {
    if (currentPageIs('/customs-declare-exports/declaration/authorisations-required')) {
        switch(getDeclaration()) {
            case 'C':
                if (document.getElementsByClassName("govuk-table")[0].querySelectorAll("tbody tr").length < GM_getValue("hoa-lengthC")) {
                    document.getElementById('code_yes').checked = 'checked'
                    submit()
                } else {
                    document.getElementById('code_no').checked = 'checked'
                    submit()
                }
                break
            default:
                document.getElementById('code_no').checked = 'checked'
                submit()
                break
        }
    }
}

function originationCountry() {
    if (currentPageIs('/customs-declare-exports/declaration/origination-country')) {
        selectFromAutoPredict(document.getElementById('countryCode-container'), "GB")
        submit()
    }
}

function destinationCountry() {
    if (currentPageIs('/customs-declare-exports/declaration/destination-country')) {
        selectFromAutoPredict(document.getElementById('countryCode-container'), "US")
        submit()
    }
}

function countryOfRouting() {
    if (currentPageIs('/customs-declare-exports/declaration/country-of-routing')) {
        switch(getDeclaration()){
            case 'F':
                document.getElementById('Yes').click()
                break
            default:
                document.getElementById('No').click()
                break
        }

        submit()
    }
}

function countriesOfRouting() {
    if (currentPageIs('/customs-declare-exports/declaration/countries-of-routing')) {
        selectFromAutoPredict(document.getElementById('countryCode-container'), "CN")
        submit()
    }
}

function countriesOfRoutingSummary() {
    if (currentPageIs('/customs-declare-exports/declaration/countries-summary')) {
        document.getElementById('No').click()
        submit()
    }
}

function locationOfGoods() {
    if (currentPageIs('/customs-declare-exports/declaration/location-of-goods')) {
        document.getElementById('code_yes').checked = 'checked'
        selectFromAutoPredict(document.getElementById('glc-container'), "GBAUABDABDABDGVM")
        submit()
    }
}

function officeOfExit() {
    if (currentPageIs('/customs-declare-exports/declaration/office-of-exit')) {
        if (document.getElementById('Yes')) {
            document.getElementById('Yes').click()
        }

        switch(getDeclaration()){
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
                selectFromAutoPredict(document.getElementById('officeId-container'), "GB000041")
                break
            case 'J':
                selectFromAutoPredict(document.getElementById('officeId-container'), "GB000054")
                break
            case 'Z':
                selectFromAutoPredict(document.getElementById('officeId-container'), "GB000051")
                break
            default:
                selectFromAutoPredict(document.getElementById('officeId-container'), "GB000434")
        }
        submit()
    }
}

function invoiceAndExchangeRateChoice() {
    if (currentPageIs('/customs-declare-exports/declaration/invoices-and-exchange-rate-choice')) {
        document.getElementById('code_no').checked = 'checked'
        submit()
    }
}

function invoiceAndExchangeRate() {
    if (currentPageIs('/customs-declare-exports/declaration/invoices-and-exchange-rate')) {
        selectRadioOptionFromInputs(document.getElementsByName('agreedExchangeRate'), 0)
        document.getElementById('code_yes').checked = 'checked'
        document.getElementById('exchangeRate').value ='1.49'
        document.getElementById('totalAmountInvoicedCurrency').value ='Pounds Sterling - GBP'
        document.getElementById('totalAmountInvoiced').value ='567640'
        submit()
    }
}

function totalPackageQuantity() {
    if (currentPageIs('/customs-declare-exports/declaration/total-package-quantity')) {
        document.getElementById('totalPackage').value ='1'
        submit()
    }
}

function natureOfTransaction() {
    if (currentPageIs('/customs-declare-exports/declaration/nature-of-transaction')) {
        selectRadioOptionFromInputs(document.getElementsByName('natureType'), 0)
        submit()
    }
}

function previousDocuments() {
    if (currentPageIs('/customs-declare-exports/declaration/add-previous-document')) {
        if (getDeclaration() == 'J') {
            selectFromAutoPredict(document.getElementById('documentType-container'), "IF3")
            document.getElementById('documentReference').value ='101SHIP2'
        } else {
            selectFromAutoPredict(document.getElementById('documentType-container'), "DCS")
            document.getElementById('documentReference').value ='9GB123456782317-BH1433A61'
        }

        submit()
    }
}

function previousDocumentsSummary() {
    if (currentPageIs('/customs-declare-exports/declaration/previous-documents-list')) {
        document.getElementById('code_no').checked = 'checked'
        submit()
    }
}

// items
function addFirstItem() {
    if (currentPageIs('/customs-declare-exports/declaration/add-declaration-item')) {
        document.getElementById('add').click()
    }
}

function procedureCodes() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/procedure-codes')) {
        switch(getDeclaration()){
            case 'D':
            case 'F':
            case 'J':
            case 'Y':
                selectFromAutoPredict(document.getElementById('procedureCode-container'), '1040')
                break
            case 'E':
                selectFromAutoPredict(document.getElementById('procedureCode-container'), '1040')
                break
            case 'K':
                selectFromAutoPredict(document.getElementById('procedureCode-container'), '0012')
                break
            case 'Z':
                selectFromAutoPredict(document.getElementById('procedureCode-container'), '1040')
                break
            default:
                selectFromAutoPredict(document.getElementById('procedureCode-container'), '1040')
        }
        submit()
    }
}

function additionalProcedureCodes() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-procedure-codes')) {
        selectFromAutoPredict(document.getElementById('additionalProcedureCode-container'), '000')
        submit()
    }
}

function fiscalInformation() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/fiscal-information')) {
        document.getElementById('No').checked = 'checked'
        submit()
    }
}

function fiscalReferences() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-fiscal-references')) {
        selectFromAutoPredict(document.getElementById('country-container'), 'GB')
        document.getElementById('reference').value = '1234'
        submit()
    }
}

function fiscalReferencesSummary() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-fiscal-references-list')) {
        document.getElementById('code_no').checked = 'checked'
        submit()
    }
}

function commodityDetails() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/commodity-details')) {
        switch(getDeclaration()){
            case 'A':
                document.getElementById("combinedNomenclatureCode").value = '2929100000'
                document.getElementById('descriptionOfGoods').value ='nonblockingdocumentary'
                break
            case 'B':
            case 'C':
            case 'E':
            case 'F':
            case 'Y':
                document.getElementById('combinedNomenclatureCode').value ='8411110000'
                document.getElementById('descriptionOfGoods').value ='Aircraft engine'
                break
            case 'Z':
                document.getElementById('combinedNomenclatureCode').value ='8538909900'
                document.getElementById('descriptionOfGoods').value ='Oilwell equipment'
                break
            default:
                document.getElementById('combinedNomenclatureCode').value ='4106920000'
                document.getElementById('descriptionOfGoods').value ='Straw for bottles'
        }
        submit()
    }
}

function unDangerousGoodsCode() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/un-dangerous-goods-code')) {
        document.getElementById('code_no').checked = 'checked'
        submit()
    }
}

function cusCode() {
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
                document.getElementById('code_no').checked = 'checked'
                break
            case 'J':
                document.getElementById('code_yes').checked = 'checked'
                document.getElementById('cusCode').value ='12345678'
                break
        }
        submit()
    }
}

function additionalTaricCodes() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-taric-code')) {
        document.getElementById('code_no').checked = 'checked'
        submit()
    }
}

function vatRating() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/vat-rating')) {
        selectRadioOptionFromInputs(document.getElementsByName('nactCode'), 0)
        submit()
    }
}

function nactCodes() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/national-additional-code')) {
        document.getElementById('code_no').checked = 'checked'
        submit()
    }
}

function statisticalValue() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/statistical-value')) {
        document.getElementById('statisticalValue').value ='1000'
        submit()
    }
}

function addPackageInformation() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/package-information')) {
        selectFromAutoPredict(document.getElementById('typesOfPackages-container'), "XD")
        document.getElementById('numberOfPackages').value ='10'
        document.getElementById('shippingMarks').value = 'Shipping description'
        submit()
    }
}

function packageInformationSummary() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/packages-list')) {
        document.getElementById('code_no').checked = 'checked'
        submit()
    }
}

function commodityMeasurements() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/commodity-measure')) {
        document.getElementById('grossMass').value ='700'
        document.getElementById('netMass').value ='500'
        submit()
    }
}

function supplementaryUnits() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/supplementary-units')) {
        document.getElementById('Yes').checked = 'checked'
        document.getElementById('supplementaryUnits').value ='10'
        submit()
    }
}

function isAdditionalInformationRequired() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/is-additional-information-required')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
    }
}

function addAdditionalInformation() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-information')) {
        switch(getDeclaration()){
            case 'K':
            case 'Z':
                document.getElementById('code').value ='00600'
                document.getElementById('description').value ='EXPORTER'
                submit()
                break
            default:
                document.getElementById('code').value ='00400'
                document.getElementById('description').value ='EXPORTER'
                submit()
        }
    }
}

function additionalInformationSummary() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-information-list')) {
        document.getElementById('code_no').checked = 'checked'
        submit()
    }
}

function isLicenceRequired() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/is-licence-required')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
    }
}

// additional documents
function getAdCode(dec) {
    var decType = "ad-code" + dec

    var codes = GM_getValue(decType)
    var code = codes.pop()
    GM_setValue(decType, codes)

    return code
}

function getAdIdent(dec) {
    var decType = "ad-idents" + dec

    var idents = GM_getValue(decType)
    var ident = idents.pop()
    GM_setValue(decType, idents)

    return ident
}

function areAdditionalDocumentsRequired() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/is-additional-documentation-required')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
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

function addDocuments() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-documentation')) {
        switch(getDeclaration())
        {
            case 'A':
                document.getElementById('documentTypeCode').value = getAdCode("A")
                document.getElementById('documentIdentifier').value = getAdIdent("A")
                document.getElementById('documentStatusReason').value = "Reason"
                submit()

                break
            case 'C':
                document.getElementById('documentTypeCode').value = getAdCode("C")
                document.getElementById('documentIdentifier').value = getAdIdent("C")
                submit()

                break
            case 'Z':
                if (!document.querySelector("#content>article>form>table>tbody>tr>th")) {
                    document.getElementById('documentTypeCode').value ='C514'
                    document.getElementById('documentIdentifier').value ='GBEIR717572504502802'
                    submit()
                } else if (!document.querySelector("#content>article>form>table>tbody>tr:nth-child(2)>th")) {
                    document.getElementById('documentTypeCode').value ='C676'
                    document.getElementById('documentIdentifier').value ='GBMOU717572504502802'
                    document.getElementById('add').click()
                } else {
                    submit()
                }
                break
            case 'Y':
                if (!document.querySelector("#content>article>form>table>tbody>tr>th")) {
                    document.getElementById('documentTypeCode').value ='C512'
                    document.getElementById('documentIdentifier').value ='GBSDE717572504502801'
                    submit()
                } else if (!document.querySelector("#content>article>form>table>tbody>tr:nth-child(2)>th")) {
                    document.getElementById('documentTypeCode').value ='Y901'
                    document.getElementById('documentIdentifier').value ='GB717572504502801'
                    document.getElementById('documentStatus').value = 'XX'
                    document.getElementById('add').click()
                } else if (!document.querySelector("#content>article>form>table>tbody>tr:nth-child(3)>th")) {
                    document.getElementById('documentTypeCode').value ='X002'
                    document.getElementById('documentIdentifier').value ='428/2009'
                    document.getElementById('documentStatus').value = 'AF'
                    document.getElementById('documentWriteOff_measurementUnit').value = 'KGM#G'
                    document.getElementById('documentWriteOff_documentQuantity').value = '100'
                    document.getElementById('add').click()
                } else {
                    submit()
                }
                break
            case 'F':
                document.getElementById('documentTypeCode').value ='C512'
                document.getElementById('documentIdentifier').value ='GBSDE717572504502801'
                submit()
                break
            case 'J':
                document.getElementById('documentTypeCode').value ='C514'
                document.getElementById('documentIdentifier').value ='GBEIR717572504502811'
                submit()
                break
            case 'B':
                document.getElementById('documentTypeCode').value = 'C676'
                document.getElementById('documentIdentifier').value = 'GBCSE717572504502801'
                submit()
                break
            case 'E':
            case 'K':
            default:
                document.getElementById('documentTypeCode').value ='C501'
                document.getElementById('documentIdentifier').value ='GBAEOC717572504502801'
                submit()
        }
    }
}

function addDocumentsSummary() {
    if (currentPageIs('/customs-declare-exports/declaration/items/.*/additional-documentation-list')) {
        switch(getDeclaration()) {
            case 'A':
                if (document.getElementById("additional_documents").querySelectorAll("tbody tr").length < GM_getValue("ad-lengthA")) {
                    document.getElementById('code_yes').checked = 'checked'
                    submit()
                } else {
                    document.getElementById('code_no').checked = 'checked'
                    submit()
                }
                break
            case 'C':
                if (document.getElementById("additional_documents").querySelectorAll("tbody tr").length < GM_getValue("ad-lengthC")) {
                    document.getElementById('code_yes').checked = 'checked'
                    submit()
                } else {
                    document.getElementById('code_no').checked = 'checked'
                    submit()
                }
                break
            default:
                document.getElementById('code_no').checked = 'checked'
                submit()
                break
        }
    }
}

// items end
function exportItems() {
    if (currentPageIs('/customs-declare-exports/declaration/declaration-items-list')) {
        document.getElementById('code_no').checked = 'checked'
        submit()
    }
}

function transportLeavingBorder() {
    if (currentPageIs('/customs-declare-exports/declaration/transport-leaving-the-border')) {
        selectRadioOptionFromInputs(document.getElementsByName('transportLeavingTheBorder'), 1)
        submit()
    }
}

function warehouseIdentification() {
    if (currentPageIs('/customs-declare-exports/declaration/warehouse-details')) {
        // K
        if (document.getElementById('code_no')) {
            document.getElementById('code_no').click()
        } else {
            document.getElementById('identificationNumber').value = 'R1234567GB'
        }

        submit()
    }
}

function supervisingCustomsOffice() {
    if (currentPageIs('/customs-declare-exports/declaration/supervising-customs-office')) {
        switch(getDeclaration()){
            case 'A':
            case 'D':
            case 'Z':
                break
            case 'B':
            case 'E':
            case 'F':
            case 'K':
            case 'Y':
                selectFromAutoPredict(document.getElementById('supervisingCustomsOffice-container'), "GBBEL004")
                break
            default:
                selectFromAutoPredict(document.getElementById('supervisingCustomsOffice-container'), "GBLBA001")
        }
        submit()
    }
}

function inlandOrBorder() {
    if (currentPageIs('/customs-declare-exports/declaration/inland-or-border')) {
        selectRadioOptionFromInputs(document.getElementsByName('location'), 0)
        submit()
    }
}

function inlandTransportDetails() {
    if (currentPageIs('/customs-declare-exports/declaration/inland-transport-details')) {
        selectRadioOptionFromInputs(document.getElementsByName('inlandModeOfTransportCode'), 0)
        submit()
    }
}

function departureTransport() {
    if (currentPageIs('/customs-declare-exports/declaration/departure-transport')) {
        document.getElementById('radio_ShipOrRoroImoNumber').checked = 'checked'
        document.getElementById('ShipOrRoroImoNumber').value = '8888'
        submit()
    }
}

function borderTransport() {
    if (currentPageIs('/customs-declare-exports/declaration/border-transport')) {
        if (document.getElementById('NameOfVessel') !== null) {
            selectRadioOptionFromInputs(document.getElementsByName('borderTransportType'), 1)
            document.getElementById('NameOfVessel').value = 'Superfast Hawk Millenium'
        }

        submit()
    }
}

function transportCountry() {
    if (currentPageIs('/customs-declare-exports/declaration/transport-country')) {
        document.getElementById('code_yes').checked = 'checked'
        selectFromAutoPredict(document.getElementById('transportCountry-container'), 'South Africa')
        submit()
    }
}

function expressConsignment() {
    if (currentPageIs('/customs-declare-exports/declaration/express-consignment')) {
        document.getElementById('code_yes').checked = 'checked'
        submit()
    }
}

function transportPayment() {
    if (currentPageIs('/customs-declare-exports/declaration/transport-payment')) {
        selectRadioOptionFromInputs(document.getElementsByName('paymentMethod'), 4)
        submit()
    }
}

function addContainer() {
    if (currentPageIs('/customs-declare-exports/declaration/container')) {
        document.getElementById('code_yes').checked = 'checked'
        document.getElementById('id').value = '123456'
        submit()
    }
}

function addSeals() {
    if (currentPageIs('/customs-declare-exports/declaration/containers/123456/seals')) {
        selectRadioOptionFromInputs(document.getElementsByName('yesNo'), 1)
        submit()
    }
}

function containersSummary() {
    if (currentPageIs('/customs-declare-exports/declaration/containers')) {
        selectRadioOptionFromInputs(document.getElementsByName('yesNo'), 1)
        submit()
    }
}

function summary() {
    if (currentPageIs('/customs-declare-exports/declaration/saved-summary')) {
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

function submission() {
    if (currentPageIs('/customs-declare-exports/declaration/submit-your-declaration')) {
        document.getElementById('fullName').value = 'Tim Tester'
        document.getElementById('jobRole').value = 'Tester'
        document.getElementById('email').value = 'tim@testing.com'
        document.getElementById('confirmation').click()

        setDeclaration(0)
    }
}

function submitAmendment() {
    if (currentPageIs('/customs-declare-exports/declaration/submit-your-amendment')
    ||  currentPageIs('/customs-declare-exports/declaration/resubmit-your-amendment')
    ||  currentPageIs('/customs-declare-exports/declaration/cancel-your-amendment')) {
        document.getElementById('fullName').value = 'Tim Tester'
        document.getElementById('jobRole').value = 'Tester'
        document.getElementById('email').value = 'tim@testing.com'
        document.getElementById('reason').value = 'Some reason'
        document.getElementById('confirmation').click()

        setDeclaration(0)
    }
}

function copyDeclaration() {
    if (currentPageIs('/customs-declare-exports/copy-declaration')) {
        document.getElementById('lrn').value = 'DSLRN' + Math.floor(Math.random() * 8999) + 100
        document.getElementById('ducr_ducr').value = '8GB654321' + Math.floor(Math.random() * 899999 + 100000) + '-101SHIP1'
        submit()
    }
}

// grand journey
function completeJourney() {
    // main
    startPage()
    choicePage()
    declarationChoice()
    dispatchLocation()
    additionalDeclarationType()
    doYouHaveDucr()
    ducrEntry()
    localReferenceNumber()
    traderReference()
    confirmDucr()
    consignmentReferences()
    linkDucrToMucr()
    enterMucr()

    // parties
    isEntryIntoDeclarantsRecords()
    personPresentingGoodsDetails()
    declarantDetails()
    isDeclarantExporter()
    exporterEoriNumber()
    exporterDetails()
    isExs()
    consigneeDetails()
    consignorEoriNumber()
    consignorAddress()
    representingAnotherAgent()
    representativeEori()
    representativeType()
    carrierEoriNumber()
    carrierDetails()
    otherPartiesInvolved()
    otherPartiesList()
    authorisationProcedureCodeChoice()
    isAuthorisationRequired()
    holderOfAuthorisation()
    authorisationsSummary()

    // locations
    originationCountry()
    destinationCountry()
    countryOfRouting()
    countriesOfRouting()
    countriesOfRoutingSummary()
    locationOfGoods()
    officeOfExit()

    // transaction
    invoiceAndExchangeRateChoice()
    invoiceAndExchangeRate()
    totalPackageQuantity()
    natureOfTransaction()
    previousDocuments()
    previousDocumentsSummary()

    // items
    addFirstItem()
    procedureCodes()
    additionalProcedureCodes()
    fiscalInformation()
    fiscalReferences()
    fiscalReferencesSummary()
    commodityDetails()
    unDangerousGoodsCode()
    cusCode()
    additionalTaricCodes()
    vatRating()
    nactCodes()
    statisticalValue()
    addPackageInformation()
    packageInformationSummary()
    commodityMeasurements()
    supplementaryUnits()
    additionalInformationSummary()
    isAdditionalInformationRequired()
    addAdditionalInformation()
    isLicenceRequired()
    areAdditionalDocumentsRequired()
    addDocumentsSummary()
    addDocuments()
    exportItems()

    // transport
    transportLeavingBorder()
    warehouseIdentification()
    supervisingCustomsOffice()
    inlandOrBorder()
    inlandTransportDetails()
    departureTransport()
    borderTransport()
    transportCountry()
    expressConsignment()
    transportPayment()

    // container
    addContainer()
    addSeals()
    containersSummary()

    // summary and confirmation
    summary()
    submission()
    submitAmendment()
}
