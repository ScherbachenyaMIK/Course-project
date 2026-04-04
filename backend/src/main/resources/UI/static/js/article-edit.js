(() => {
    const form = document.getElementById("edit-form");
    const titleInput = document.getElementById("title-input");
    const contentInput = document.getElementById("content-input");
    const errorBlock = document.getElementById("error-message");

    form.addEventListener("submit", function(event) {
        let hasError = false;

        if (titleInput.value.trim() === "") {
            titleInput.style.borderBottomColor = "red";
            hasError = true;
        } else {
            titleInput.style.borderBottomColor = "";
        }

        if (contentInput.value.trim() === "") {
            contentInput.style.borderColor = "red";
            hasError = true;
        } else {
            contentInput.style.borderColor = "";
        }

        if (hasError) {
            event.preventDefault();
            errorBlock.textContent = "Заполните обязательные поля";
            errorBlock.style.display = "block";
        }
    });
})();
