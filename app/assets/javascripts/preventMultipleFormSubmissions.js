(() => {
    let frm = document.querySelector('form')
    if (frm) {
        var allowSubmit = true;
        frm.onsubmit = () => {
            if (allowSubmit) {
                allowSubmit = false;
                return true;
            }
            else {
                return false;
            }
        }
    }
})();