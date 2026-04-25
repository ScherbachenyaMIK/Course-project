document.addEventListener('DOMContentLoaded', function () {
    const editButton = document.getElementById('edit-button');
    const saveButton = document.getElementById('save-button');
    const cancelButton = document.getElementById('cancel-button');
    const editableFields = document.querySelectorAll('.field-editable');

    function enterEditMode() {
        editButton.classList.add('hidden');
        saveButton.classList.remove('hidden');
        cancelButton.classList.remove('hidden');

        editableFields.forEach(function (field) {
            field.querySelector('.field-value').classList.add('hidden');
            field.querySelector('.field-input').classList.remove('hidden');
            field.classList.add('editing');
        });
    }

    function exitEditMode() {
        editButton.classList.remove('hidden');
        saveButton.classList.add('hidden');
        cancelButton.classList.add('hidden');

        editableFields.forEach(function (field) {
            field.querySelector('.field-value').classList.remove('hidden');
            field.querySelector('.field-input').classList.add('hidden');
            field.classList.remove('editing');
        });
    }

    editButton.addEventListener('click', enterEditMode);

    const userIconWrapper = document.getElementById('user-icon-wrapper');
    const userIconInput = document.getElementById('user-icon-input');
    const userIconImg = document.getElementById('user-icon');
    const userIconError = document.getElementById('user-icon-error');
    const profileData = document.getElementById('profile-data');
    const userId = profileData ? profileData.dataset.userId : null;

    function showIconError(message) {
        if (!userIconError) return;
        userIconError.textContent = message;
        userIconError.classList.remove('hidden');
    }

    function hideIconError() {
        if (!userIconError) return;
        userIconError.classList.add('hidden');
    }

    if (userIconWrapper && userIconInput && userId) {
        userIconWrapper.addEventListener('click', function () {
            userIconInput.click();
        });

        userIconInput.addEventListener('change', function () {
            const file = userIconInput.files && userIconInput.files[0];
            if (!file) {
                return;
            }
            if (!file.type.startsWith('image/')) {
                showIconError('Выберите файл-изображение');
                userIconInput.value = '';
                return;
            }
            hideIconError();
            const formData = new FormData();
            formData.append('file', file);
            userIconWrapper.classList.add('uploading');
            fetch('/resources/user_icon/' + encodeURIComponent(userId), {
                method: 'POST',
                body: formData,
                credentials: 'same-origin'
            }).then(function (response) {
                if (!response.ok) {
                    return response.text().then(function (text) {
                        throw new Error(text || ('Ошибка загрузки: ' + response.status));
                    });
                }
                userIconImg.src = '/resources/user_icon/' + encodeURIComponent(userId)
                        + '?t=' + Date.now();
            }).catch(function (err) {
                showIconError(err.message || 'Не удалось загрузить аватар');
            }).finally(function () {
                userIconWrapper.classList.remove('uploading');
                userIconInput.value = '';
            });
        });
    }
    cancelButton.addEventListener('click', function () {
        editableFields.forEach(function (field) {
            var input = field.querySelector('.field-input');
            var value = field.querySelector('.field-value').textContent.trim();
            if (input.tagName === 'SELECT') {
                for (var i = 0; i < input.options.length; i++) {
                    if (input.options[i].text === value || input.options[i].value === value) {
                        input.selectedIndex = i;
                        break;
                    }
                }
            } else if (input.tagName === 'TEXTAREA') {
                input.value = value;
            } else {
                input.value = input.defaultValue;
            }
        });
        exitEditMode();
    });
});
