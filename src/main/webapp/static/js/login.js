function sendInvalidLoginMessage() {
    const wrapper = document.querySelector("#wrapper");
    window.addEventListener("load", function (event) {
        let loginMessage = wrapper.dataset.login;
        if (loginMessage === "true") {
            $('#invalidLoginModal').modal('show')
        }
    });
}

sendInvalidLoginMessage();


function sendEmailAlreadyTakenMessage() {
    const wrapper = document.querySelector("#wrapper");
    window.addEventListener("load", function (event) {
        let emailAlreadyTaken = wrapper.dataset.registration;
        if (emailAlreadyTaken === "true") {
            $('#emailAlreadyTakenModal').modal('show')
        }
    });
}

sendEmailAlreadyTakenMessage();