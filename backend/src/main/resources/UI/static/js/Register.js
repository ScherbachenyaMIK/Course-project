const pages = document.querySelectorAll(".page1, .page2, .page3");
const nextButton = document.getElementById("next-button");
const signUpButton = document.getElementById("sign-up-button");
let currentPage = 0;

async function validateData(index) {
    if (index === 0) {
        return true;
    }
    if (index === 1) {
        const usernameInput = document.getElementById("username");
        const nameInput = document.getElementById("name");
        const emailInput = document.getElementById("email");

        const username = document.getElementById("username-input").value.trim();
        const name = document.getElementById("name-input").value.trim();
        const email = document.getElementById("email-input").value.trim();

        const usernameRegex = /(^[A-Za-z0-9][A-Za-z0-9-_.]{2,63}$)/;
        const nameRegex = /^[a-zA-Z\s-]{1,64}$/;
        const emailRegex = /(^[^&=+<>,_'@.-][^&=+<>,_'@-]*@(mail|gmail|yandex)\.[a-z]+$)/;

        let isMistake = false;
        if (username === "" || !usernameRegex.test(username)) {
            usernameInput.style.outline = '2px solid red';
            isMistake = true;
        } else {
            usernameInput.style.outline = '2px solid green';
        }

        if (name === "" || !nameRegex.test(name)) {
            nameInput.style.outline = '2px solid red';
            isMistake = true;
        } else {
            nameInput.style.outline = '2px solid green';
        }

        if (email === "" || !emailRegex.test(email)) {
            emailInput.style.outline = '2px solid red';
            isMistake = true;
        } else {
            emailInput.style.outline = '2px solid green';
        }

        if (!isMistake) {
            const availability = await fetch('/api/availability', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: username,
                    email: email
                })
            });

            const usernameEmailErrorBlock = document.getElementById("username-email-error-message");
            let message = "Аккаунт с таким username, email уже существует";

            if (availability.redirected) {
                usernameInput.style.outline = '2px solid red';
                emailInput.style.outline = '2px solid red';
                usernameEmailErrorBlock.textContent = "Ошибка при проверке данных, повторите попытку позже";
                usernameEmailErrorBlock.style.display = "block";
                return false;
            }

            const availabilityData = await availability.json();

            if (!availabilityData.usernameAvailable) {
                usernameInput.style.outline = '2px solid red';
                isMistake = true;
                usernameEmailErrorBlock.style.display = "block";
            } else {
                usernameInput.style.outline = '2px solid green';
                message = message.replace("username, ", "");
            }

            if (!availabilityData.emailAvailable) {
                emailInput.style.outline = '2px solid red';
                isMistake = true;
                usernameEmailErrorBlock.style.display = "block";
            } else {
                emailInput.style.outline = '2px solid green';
                message = message.replace(", email", "");
            }

            usernameEmailErrorBlock.textContent = message;

            if (!isMistake) {
                usernameEmailErrorBlock.style.display = "none";
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    if (index === 2) {
        const passwordInput = document.getElementById("password");
        const passwordRepeatInput = document.getElementById("password-repeat");

        const password = document.getElementById("password-input").value.trim();
        const passwordRepeat = document.getElementById("password-repeat-input").value.trim();

        const passwordRegex = /(^.{8,64}$)/;

        let isMistake = false;
        if (password === "" || !passwordRegex.test(password)) {
            passwordInput.style.outline = '2px solid red';
            isMistake = true;
        } else {
            passwordInput.style.outline = '2px solid green';
        }

        if (password !== passwordRepeat) {
            passwordRepeatInput.style.outline = '2px solid red';
            isMistake = true;
        } else {
            passwordRepeatInput.style.outline = '2px solid green';
        }

        if (!isMistake) {
            return true;
        } else {
            return false;
        }
    }
    if (index === 3) {
        const birthdateInput = document.getElementById("birthdate");

        const birthdate = new Date(document.getElementById("birthdate-input").value.trim());

        let isMistake = false;
        if (birthdate.getTime() >= new Date().getTime()) {
            birthdateInput.style.outline = '2px solid red';
            isMistake = true;
        } else {
            birthdateInput.style.outline = '2px solid green';
        }

        if (!isMistake) {
            return true;
        } else {
            return false;
        }
    }
}

function showPage(index) {
    pages.forEach((page, i) => {
        page.style.display = i === index ? "flex" : "none";
    });
    if (index === pages.length - 1) {
        nextButton.style.display = "none";
        signUpButton.style.display = "flex";
    } else {
        nextButton.style.display = "flex";
        signUpButton.style.display = "none";
    }
}

nextButton.addEventListener("click", async function () {
    if (currentPage < pages.length - 1) {
        if (await validateData(currentPage + 1)) {
            showPage(++currentPage);
        }
    }
});

async function hashPassword(password) {
    const encoder = new TextEncoder();
    const data = encoder.encode(password);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(byte => byte.toString(16).padStart(2, '0')).join('');
}

signUpButton.addEventListener("click", async function () {
    if (await validateData(currentPage + 1)) {
        const form = document.getElementById("register-form");
        const passwordInput = document.getElementById("password-input");
        const hashedPasswordInput = document.getElementById("hashed-password-input");
        const hashedPassword = await hashPassword(passwordInput.value);
        hashedPasswordInput.value = hashedPassword;

        const formData = new FormData(form);
        const jsonData = Object.fromEntries(formData);

        const response = await fetch('/register', {
            method: 'POST',
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(jsonData)
        });

        const responseData = await response.json();

        const resultBlock = document.getElementById("result");

        if (responseData.success) {
            setTimeout(() => {
                window.location.href = "/login";
            }, 2000);
            resultBlock.textContent = "Регистрация прошла успешно, не забудьте подтвердить email.\nВы будете перенаправлены на страницу входа.";
            resultBlock.className = "success-message";
        } else {
            resultBlock.textContent = "Что-то пошло не так. Повторите попытку позже.";
            resultBlock.className = "error-message";
        }
        resultBlock.style.display = "block";
    }
});

showPage(currentPage);