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
