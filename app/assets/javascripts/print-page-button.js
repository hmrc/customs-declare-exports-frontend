(() => {
    document.onreadystatechange = function () {
        if (document.readyState == "complete") {
            const printButtons = document.getElementsByClassName("ceds-print-link");
            Array.from(printButtons).forEach(function (printButton) {
                printButton.addEventListener('click', function() {
                    window.print();
                });
            });
        }
    }
})();
