const warning = {
    wrapper: document.querySelector("#wrapper"),

    sendInvalidLoginMessage() {
        window.addEventListener("load", function (event) {
            let loginMessage = this.wrapper.dataset.login;
            if (loginMessage === "true") {
                $('#warningModal').modal('show');
            }
        });
    },

    sendEmailAlreadyTakenMessage() {
        window.addEventListener("load", function (event) {
            let emailAlreadyTaken = this.wrapper.dataset.registration;
            if (emailAlreadyTaken === "true") {
                $('#warningModal').modal('show');
            }
        });
    }
};

warning.sendInvalidLoginMessage();
warning.sendEmailAlreadyTakenMessage();