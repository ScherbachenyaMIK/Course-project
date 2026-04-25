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

    const editData = document.getElementById("edit-data");
    const articleId = editData ? editData.dataset.articleId : null;
    if (!articleId) {
        return;
    }

    const previewWrapper = document.getElementById("preview-wrapper");
    const previewInput = document.getElementById("preview-input");
    const previewImage = document.getElementById("preview-image");
    const previewError = document.getElementById("preview-error");

    function showPreviewError(message) {
        if (!previewError) return;
        previewError.textContent = message;
        previewError.classList.remove("hidden");
    }

    function hidePreviewError() {
        if (previewError) previewError.classList.add("hidden");
    }

    if (previewWrapper && previewInput && previewImage) {
        previewWrapper.addEventListener("click", function () {
            previewInput.click();
        });

        previewInput.addEventListener("change", function () {
            const file = previewInput.files && previewInput.files[0];
            if (!file) return;
            if (!file.type.startsWith("image/")) {
                showPreviewError("Выберите файл-изображение");
                previewInput.value = "";
                return;
            }
            hidePreviewError();
            const formData = new FormData();
            formData.append("file", file);
            previewWrapper.classList.add("uploading");
            fetch("/resources/preview/" + encodeURIComponent(articleId), {
                method: "POST",
                body: formData,
                credentials: "same-origin"
            }).then(function (response) {
                if (!response.ok) {
                    return response.text().then(function (text) {
                        throw new Error(text || ("Ошибка загрузки: " + response.status));
                    });
                }
                previewImage.src = "/resources/preview/" + encodeURIComponent(articleId)
                        + "?t=" + Date.now();
            }).catch(function (err) {
                showPreviewError(err.message || "Не удалось загрузить обложку");
            }).finally(function () {
                previewWrapper.classList.remove("uploading");
                previewInput.value = "";
            });
        });
    }

    const insertImageButton = document.getElementById("insert-image-button");
    const articleImageInput = document.getElementById("article-image-input");
    const insertImageStatus = document.getElementById("insert-image-status");

    function setInsertStatus(message, isError) {
        if (!insertImageStatus) return;
        insertImageStatus.textContent = message || "";
        insertImageStatus.classList.toggle("error", !!isError);
    }

    function insertAtCursor(textarea, snippet) {
        const start = textarea.selectionStart;
        const end = textarea.selectionEnd;
        const value = textarea.value;
        textarea.value = value.slice(0, start) + snippet + value.slice(end);
        const cursor = start + snippet.length;
        textarea.selectionStart = cursor;
        textarea.selectionEnd = cursor;
        textarea.focus();
    }

    if (insertImageButton && articleImageInput && contentInput) {
        insertImageButton.addEventListener("click", function () {
            articleImageInput.click();
        });

        articleImageInput.addEventListener("change", function () {
            const file = articleImageInput.files && articleImageInput.files[0];
            if (!file) return;
            if (!file.type.startsWith("image/")) {
                setInsertStatus("Выберите файл-изображение", true);
                articleImageInput.value = "";
                return;
            }
            setInsertStatus("Загрузка...", false);
            insertImageButton.disabled = true;
            const formData = new FormData();
            formData.append("file", file);
            fetch("/resources/article/" + encodeURIComponent(articleId) + "/images", {
                method: "POST",
                body: formData,
                credentials: "same-origin"
            }).then(function (response) {
                if (!response.ok) {
                    return response.text().then(function (text) {
                        throw new Error(text || ("Ошибка загрузки: " + response.status));
                    });
                }
                return response.json();
            }).then(function (body) {
                if (!body || body.imageId == null) {
                    throw new Error("Сервер вернул некорректный ответ");
                }
                const altText = (file.name || "image").replace(/"/g, "");
                const snippet = '<img src="/resources/article/' + articleId
                        + "/images/" + body.imageId + '" alt="' + altText
                        + '" loading="lazy" />';
                insertAtCursor(contentInput, snippet);
                setInsertStatus("Изображение добавлено", false);
            }).catch(function (err) {
                setInsertStatus(err.message || "Не удалось загрузить изображение", true);
            }).finally(function () {
                insertImageButton.disabled = false;
                articleImageInput.value = "";
            });
        });
    }
})();
