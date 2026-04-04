document.addEventListener('DOMContentLoaded', function () {
    var articleId = document.getElementById('article-data').getAttribute('data-article-id');
    var isAuthenticated = document.getElementById('article-data').getAttribute('data-authenticated') === 'true';
    var viewSent = false;

    // View counter: send after 30 seconds of reading
    var VIEW_DELAY_MS = 30000;
    setTimeout(function () {
        if (!viewSent) {
            viewSent = true;
            fetch('/api/articles/' + articleId + '/view', {method: 'POST'})
                .catch(function () {});
        }
    }, VIEW_DELAY_MS);

    // Like button
    var likeBtn = document.getElementById('like-button');
    var likeCount = document.getElementById('like-count');
    if (likeBtn) {
        likeBtn.addEventListener('click', function () {
            if (!isAuthenticated) {
                return;
            }
            if (likeBtn.classList.contains('liked')) {
                return;
            }
            likeBtn.classList.add('liked');
            var current = parseInt(likeCount.textContent, 10) || 0;
            likeCount.textContent = current + 1;
            fetch('/api/articles/' + articleId + '/like', {method: 'POST'})
                .catch(function () {});
        });
    }

    // Comment form
    var commentForm = document.getElementById('comment-form');
    var commentInput = document.getElementById('comment-input');
    var commentsList = document.getElementById('comments-list');
    if (commentForm) {
        commentForm.addEventListener('submit', function (e) {
            e.preventDefault();
            var text = commentInput.value.trim();
            if (!text) {
                return;
            }
            var formData = new URLSearchParams();
            formData.append('text', text);
            fetch('/api/articles/' + articleId + '/comments', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: formData.toString()
            })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Failed');
                }
                return response.json();
            })
            .then(function (comment) {
                var commentEl = createCommentElement(comment);
                commentsList.insertBefore(commentEl, commentsList.firstChild);
                commentInput.value = '';
                var countEl = document.getElementById('comments-count');
                if (countEl) {
                    var c = parseInt(countEl.textContent, 10) || 0;
                    countEl.textContent = c + 1;
                }
            })
            .catch(function () {});
        });
    }

    function createCommentElement(comment) {
        var div = document.createElement('div');
        div.className = 'comment-item';

        var header = document.createElement('div');
        header.className = 'comment-header';

        var icon = document.createElement('img');
        icon.className = 'comment-author-icon';
        icon.src = comment.authorIconUri;
        icon.alt = comment.author;
        header.appendChild(icon);

        var author = document.createElement('span');
        author.className = 'comment-author-name';
        author.textContent = comment.author;
        header.appendChild(author);

        var date = document.createElement('span');
        date.className = 'comment-date';
        date.textContent = comment.date;
        header.appendChild(date);

        div.appendChild(header);

        var body = document.createElement('div');
        body.className = 'comment-text';
        body.textContent = comment.text;
        div.appendChild(body);

        return div;
    }
});
