const timeoutDialog = document.querySelector("#timeout-dialog");

if (timeoutDialog && window.HMRCFrontend.TimeoutDialog) {
    new window.HMRCFrontend.TimeoutDialog(timeoutDialog).init();
}