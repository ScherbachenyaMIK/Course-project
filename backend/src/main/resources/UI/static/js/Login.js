(() => {
async function hashPassword(password) {
    const encoder = new TextEncoder();
    const data = encoder.encode(password);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(byte => byte.toString(16).padStart(2, '0')).join('');
}

document.getElementById("log-in-button-confirm").addEventListener("click", async function() {
    const regex = /(^[A-Za-z0-9][A-Za-z0-9-_.]{2,63}$)|(^[^&=+<>,_'@.-][^&=+<>,_'@-]*@(mail|gmail|yandex)\.[a-z]+$)/;

    const username = document.getElementById("login-input").value.trim();
    const password = document.getElementById("password-input").value.trim();

    const passwordInput = document.getElementById("password");
    const usernameInput = document.getElementById("login");

    let isMistake = false;
    if (username === "" || !regex.test(username)) {
        usernameInput.style.outline = '2px solid red';
        isMistake = true;
    } else {
        usernameInput.style.outline = '2px solid green';
    }

    if (password === "") {
        passwordInput.style.outline = '2px solid red';
        isMistake = true;
    } else {
        passwordInput.style.outline = '2px solid green';
    }

    if (!isMistake) {
        const hashedPassword = await hashPassword(password);

        const response = await fetch("/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username: username,
                password: hashedPassword
            })
        });

        const responseData = await response.json();

        if (responseData.success) {
            window.location.href = "/";
        } else {
            usernameInput.style.outline = '2px solid red';
            passwordInput.style.outline = '2px solid red';
            errorBlock = document.getElementById("error-message");

            if (responseData.cause == "BadCredentialsException") {
                errorBlock.textContent = "Введённые данные неверны";
            } else {
                errorBlock.textContent = "Что-то пошло не так, повторите попытку позже";
            }

            errorBlock.style.display = "block";
        }
    } else {
        return;
    }
});

const passwordInput = document.getElementById("password-input");
const eyeIcon = document.querySelector("#password .eye");

let visiblePassword = false;

eyeIcon.addEventListener("click", () => {
    visiblePassword = !visiblePassword;

    if (visiblePassword) {
        passwordInput.type = "text";
        eyeIcon.src = "/resources/eye_open.png";
    } else {
        passwordInput.type = "password";
        eyeIcon.src = "/resources/eye_closed.png";
    }
});
})();