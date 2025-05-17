const ContextHolder = (function () {
    let instance;

    function createInstance() {
        return {
            isRegisterScriptLoaded: false,
            isRegisterStyleLoaded: false,
            isLoginScriptLoaded: false,
            isLoginStyleLoaded: false,
            isFormStyleLoaded: false
        };
    }

    return {
        getInstance: function () {
            if (!instance) {
                instance = createInstance();
            }
            return instance;
        }
    };
})();

async function playAnimation(object, e) {
    const circle = document.createElement("span");
    circle.classList.add("ripple");
    object.appendChild(circle);

    const d = Math.max(object.clientWidth, object.clientHeight);
    circle.style.width = circle.style.height = d + "px";
    circle.style.left = e.clientX - object.offsetLeft - d / 2 + "px";
    circle.style.top = e.clientY - object.offsetTop - d / 2 + "px";

    setTimeout(() => circle.remove(), 300);
}

async function resolveSignUp() {
    const response = await fetch("/register?fragment=true");

    const html = await response.text();
    const container = document.getElementById("register-form-container");

    if (!ContextHolder.isFormStyleLoaded) {
            ContextHolder.isFormStyleLoaded = true;
            const styleLink = document.createElement("link");
            styleLink.rel = "stylesheet";
            styleLink.href = "/styles/form.css";
            document.head.appendChild(styleLink);
    }

    if (!ContextHolder.isRegisterStyleLoaded) {
        ContextHolder.isRegisterStyleLoaded = true;
        const styleLink = document.createElement("link");
        styleLink.rel = "stylesheet";
        styleLink.href = "/styles/register.css";
        document.head.appendChild(styleLink);
    }

    if (!ContextHolder.isRegisterScriptLoaded) {
        ContextHolder.isRegisterScriptLoaded = true;
        const script = document.createElement("script");
        script.src = "/scripts/register.js";
        document.head.appendChild(script);
    } else {
        const old = document.querySelector('script[src="/scripts/register.js"]');
        if (old) old.remove();

        const newScript = document.createElement("script");
        newScript.src = "/scripts/register.js";
        document.head.appendChild(newScript);
    }

    container.innerHTML = html;
    container.className = "register-form-container";

    const form = document.getElementById("form");
    container.addEventListener("click", (e) => {
        if (e.target === form) {
            container.className = "";
            container.innerHTML = "";
            container.className = "hidden";
        }
    });
}

async function resolveLogIn() {
    const response = await fetch("/login?fragment=true");

    const html = await response.text();
    const container = document.getElementById("login-form-container");

    if (!ContextHolder.isFormStyleLoaded) {
            ContextHolder.isFormStyleLoaded = true;
            const styleLink = document.createElement("link");
            styleLink.rel = "stylesheet";
            styleLink.href = "/styles/form.css";
            document.head.appendChild(styleLink);
    }

    if (!ContextHolder.isLoginStyleLoaded) {
        ContextHolder.isLoginStyleLoaded = true;
        const styleLink = document.createElement("link");
        styleLink.rel = "stylesheet";
        styleLink.href = "/styles/login.css";
        document.head.appendChild(styleLink);
    }

    if (!ContextHolder.isLoginScriptLoaded) {
        ContextHolder.isLoginScriptLoaded = true;
        const script = document.createElement("script");
        script.src = "/scripts/login.js";
        document.head.appendChild(script);
    } else {
        const old = document.querySelector('script[src="/scripts/login.js"]');
        if (old) old.remove();

        const newScript = document.createElement("script");
        newScript.src = "/scripts/login.js";
        document.head.appendChild(newScript);
    }

    container.innerHTML = html;
    container.className = "login-form-container";

    const form = document.getElementById("form");
    container.addEventListener("click", (e) => {
        if (e.target === form) {
            container.className = "";
            container.innerHTML = "";
            container.className = "hidden";
        }
    });
}

async function resolveLogOut() {
    const response = await fetch("/login", {
            method: "DELETE",
            redirect: "manual"
    });

    window.location.reload();
}

const signUpBtn = document.getElementById("sign-up-button");
if (signUpBtn) {
    signUpBtn.addEventListener("click", async function(e) {
    playAnimation(this, e);

    setTimeout(() => resolveSignUp(), 300);
    });
}

const logInBtn = document.getElementById("log-in-button");
if (logInBtn) {
    logInBtn.addEventListener("click", async function(e) {
        playAnimation(this, e);

        setTimeout(() => resolveLogIn(), 300);
    });
}

const logOutBtn = document.getElementById("log-out-button");
if (logOutBtn) {
    logOutBtn.addEventListener("click", async function(e) {
        playAnimation(this, e);

        setTimeout(() => resolveLogOut(), 300);
    });
}

const profileBtn = document.getElementById("profile-button");
if (profileBtn) {
    profileBtn.addEventListener("click", async function(e) {
        playAnimation(this, e);
    });
}