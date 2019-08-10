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