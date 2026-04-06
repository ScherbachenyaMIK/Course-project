document.addEventListener('DOMContentLoaded', function () {
    var nav = document.getElementById('alphabet-nav');
    var headers = document.querySelectorAll('.letter-header');
    headers.forEach(function (header) {
        var letter = header.textContent.trim();
        var link = document.createElement('a');
        link.href = '#' + header.id;
        link.textContent = letter;
        link.addEventListener('click', function (e) {
            e.preventDefault();
            header.scrollIntoView({behavior: 'smooth', block: 'start'});
        });
        nav.appendChild(link);
    });
});
