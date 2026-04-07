function performSearch() {
    var query = document.getElementById('searching-input').value.trim();
    if (query) {
        window.location.href = '/search?query=' + encodeURIComponent(query);
    }
}

document.querySelector('.magnifier').addEventListener('click', performSearch);

document.getElementById('searching-input').addEventListener('keydown', function(event) {
    if (event.key === 'Enter') {
        performSearch();
    }
});

document.getElementById('sample-form').addEventListener('submit', function(event) {
    event.preventDefault();
    document.getElementById('sample-form').style.display = 'none';

    document.getElementById('result-block').style.display = 'flex';

    fetch('/api/ai/sample')
        .then(response => {
            if (!response.ok || response.redirected) {
                return "Ошибка при запросе";
            }
            return response.text();
        })
        .then(data => {
            document.getElementById('response-text').textContent = data;
        })
        .catch(error => {
            document.getElementById('response-text').textContent = "Произошла ошибка: " + error.message;
        });
});
