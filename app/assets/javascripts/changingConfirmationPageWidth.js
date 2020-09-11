function init()  {
    var iBase = TextResizeDetector.addEventListener(onFontResize, null);
    adjustColumnWidth(iBase);
}

function onFontResize(e, args) {
    var currentFontSize = args[0].iSize;
    adjustColumnWidth(currentFontSize)
}

function adjustColumnWidth(fontSize) {
    var bodyContentClass = "govuk-grid-column-full";
    if (fontSize < 30)
        bodyContentClass = "govuk-grid-column-two-thirds";

    document.getElementById("confirmation-content").setAttribute("class", bodyContentClass);
}

//id of element to check for and insert control
TextResizeDetector.TARGET_ELEMENT_ID = 'main-content';
//function to call once TextResizeDetector has init'd
TextResizeDetector.USER_INIT_FUNC = init;