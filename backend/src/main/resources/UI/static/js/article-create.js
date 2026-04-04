(() => {
    const form = document.getElementById("article-create-form");
    const titleInput = document.getElementById("title-input");
    const contentInput = document.getElementById("content-input");
    const titleField = document.getElementById("title");
    const contentField = document.getElementById("content-field");
    const errorBlock = document.getElementById("error-message");

    form.addEventListener("submit", function(event) {
        let hasError = false;

        if (titleInput.value.trim() === "") {
            titleField.style.outline = "2px solid red";
            hasError = true;
        } else {
            titleField.style.outline = "2px solid green";
        }

        if (contentInput.value.trim() === "") {
            contentField.style.outline = "2px solid red";
            hasError = true;
        } else {
            contentField.style.outline = "2px solid green";
        }

        if (hasError) {
            event.preventDefault();
            errorBlock.textContent = "Заполните обязательные поля";
            errorBlock.style.display = "block";
        }
    });
})();
