async function hashPassword(password) {
    const encoder = new TextEncoder();
    const data = encoder.encode(password);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(byte => byte.toString(16).padStart(2, '0')).join('');
}

document.getElementById("log-in-button").addEventListener("click", async function() {
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

        fetch("/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username: username,
                password: hashedPassword
            })
        });
    } else {
        return;
    }
});
