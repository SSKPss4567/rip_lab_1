const API_BASE = 'http://localhost:8080/api';

let currentMovies = [];
let currentDirectors = [];
let currentGenres = [];
let currentReviews = [];

document.addEventListener('DOMContentLoaded', function() {
    setupTabs();
    loadAllData();
});

function setupTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabName = btn.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });

    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
    document.getElementById(`${tabName}-tab`).classList.add('active');
}

async function loadAllData() {
    await Promise.all([
        loadMovies(),
        loadDirectors(),
        loadGenres()
    ]);
    await loadReviews();
}

async function loadMovies() {
    try {
        const response = await fetch(`${API_BASE}/movies`);
        if (!response.ok) throw new Error('Не удалось загрузить фильмы');
        currentMovies = await response.json();
        renderMovies();
    } catch (error) {
        showError('movies-list', 'Ошибка загрузки фильмов: ' + error.message);
    }
}

async function loadDirectors() {
    try {
        const response = await fetch(`${API_BASE}/directors`);
        if (!response.ok) throw new Error('Не удалось загрузить режиссеров');
        currentDirectors = await response.json();
        renderDirectors();
    } catch (error) {
        showError('directors-list', 'Ошибка загрузки режиссеров: ' + error.message);
    }
}

async function loadGenres() {
    try {
        const response = await fetch(`${API_BASE}/genres`);
        if (!response.ok) throw new Error('Не удалось загрузить жанры');
        currentGenres = await response.json();
        renderGenres();
    } catch (error) {
        showError('genres-list', 'Ошибка загрузки жанров: ' + error.message);
    }
}

async function loadReviews() {
    try {
        const response = await fetch(`${API_BASE}/reviews`);
        if (!response.ok) throw new Error('Не удалось загрузить отзывы');
        currentReviews = await response.json();
        renderReviews();
    } catch (error) {
        showError('reviews-list', 'Ошибка загрузки отзывов: ' + error.message);
    }
}

function renderMovies() {
    const container = document.getElementById('movies-list');
    if (currentMovies.length === 0) {
        container.innerHTML = '<div class="empty">Фильмы не найдены</div>';
        return;
    }

    const sortedMovies = [...currentMovies].sort((a, b) => {
        const dateA = a.releaseDate ? new Date(a.releaseDate) : new Date(0);
        const dateB = b.releaseDate ? new Date(b.releaseDate) : new Date(0);
        return dateB - dateA;
    });

    container.innerHTML = sortedMovies.map(movie => {
        const genreIds = movie.genreIds || [];
        const genreNames = getGenreNames(genreIds);
        return `
        <div class="item-card">
            <div class="item-info">
                <div class="item-title">${escapeHtml(movie.title)}</div>
                <div class="item-detail">Режиссер: ${getDirectorName(movie.directorId)}</div>
                <div class="item-detail">Дата выпуска: ${movie.releaseDate || 'Не указана'}</div>
                <div class="item-detail">Длительность: ${movie.duration || 'Не указана'} минут</div>
                ${movie.averageRating ? `<div class="item-detail">Средний рейтинг: <span class="rating">${movie.averageRating.toFixed(1)}</span></div>` : ''}
                ${movie.description ? `<div class="item-detail">${escapeHtml(movie.description)}</div>` : ''}
                <div class="item-detail">
                    <strong>Жанры:</strong>
                    ${genreNames.length > 0 ? 
                        `<div class="genres-list">${genreIds.map(genreId => {
                            const genre = currentGenres.find(g => g.id === genreId);
                            return genre ? `
                            <span class="genre-tag">
                                ${escapeHtml(genre.name)}
                                <button class="genre-remove" onclick="removeGenreFromMovie(${movie.id}, ${genreId})" title="Удалить жанр">×</button>
                            </span>
                            ` : '';
                        }).filter(tag => tag).join('')}</div>` : 
                        '<span class="no-genres">Жанры не указаны</span>'
                    }
                    <button class="btn btn-small" onclick="showManageGenresForm(${movie.id})" style="margin-top: 8px;">Управление жанрами</button>
                </div>
            </div>
            <div class="item-actions">
                <button class="btn btn-primary" onclick="showRecommendations(${movie.id})">Рекомендации</button>
                <button class="btn btn-secondary" onclick="editMovie(${movie.id})">Редактировать</button>
                <button class="btn btn-danger" onclick="deleteMovie(${movie.id})">Удалить</button>
            </div>
        </div>
    `;
    }).join('');
}

function renderDirectors() {
    const container = document.getElementById('directors-list');
    if (currentDirectors.length === 0) {
        container.innerHTML = '<div class="empty">Режиссеры не найдены</div>';
        return;
    }

    container.innerHTML = currentDirectors.map(director => `
        <div class="item-card">
            <div class="item-info">
                <div class="item-title">${escapeHtml(director.firstName)} ${escapeHtml(director.lastName)}</div>
                ${director.birthDate ? `<div class="item-detail">Дата рождения: ${director.birthDate}</div>` : ''}
                ${director.biography ? `<div class="item-detail">${escapeHtml(director.biography)}</div>` : ''}
            </div>
            <div class="item-actions">
                <button class="btn btn-secondary" onclick="editDirector(${director.id})">Редактировать</button>
                <button class="btn btn-danger" onclick="deleteDirector(${director.id})">Удалить</button>
            </div>
        </div>
    `).join('');
}

function renderGenres() {
    const container = document.getElementById('genres-list');
    if (currentGenres.length === 0) {
        container.innerHTML = '<div class="empty">Жанры не найдены</div>';
        return;
    }

    container.innerHTML = currentGenres.map(genre => `
        <div class="item-card">
            <div class="item-info">
                <div class="item-title">${escapeHtml(genre.name)}</div>
            </div>
            <div class="item-actions">
                <button class="btn btn-secondary" onclick="editGenre(${genre.id})">Редактировать</button>
                <button class="btn btn-danger" onclick="deleteGenre(${genre.id})">Удалить</button>
            </div>
        </div>
    `).join('');
}

function renderReviews() {
    const container = document.getElementById('reviews-list');
    if (currentReviews.length === 0) {
        container.innerHTML = '<div class="empty">Отзывы не найдены</div>';
        return;
    }

    container.innerHTML = currentReviews.map(review => `
        <div class="item-card">
            <div class="item-info">
                <div class="item-title">${escapeHtml(review.authorName)} - <span class="rating">${review.rating}/10</span></div>
                <div class="item-detail">Фильм: ${getMovieTitle(review.movieId)}</div>
                ${review.comment ? `<div class="item-detail">${escapeHtml(review.comment)}</div>` : ''}
                ${review.createdAt ? `<div class="item-detail">Дата: ${new Date(review.createdAt).toLocaleString('ru-RU')}</div>` : ''}
            </div>
            <div class="item-actions">
                <button class="btn btn-secondary" onclick="editReview(${review.id})">Редактировать</button>
                <button class="btn btn-danger" onclick="deleteReview(${review.id})">Удалить</button>
            </div>
        </div>
    `).join('');
}

function showMovieForm(movie = null) {
    const isEdit = movie !== null;
    const form = `
        <h2>${isEdit ? 'Редактировать' : 'Добавить'} фильм</h2>
        <form id="movie-form" onsubmit="saveMovie(event, ${movie?.id || 'null'})">
            <div class="form-group">
                <label>Название *</label>
                <input type="text" name="title" value="${movie?.title || ''}" required>
            </div>
            <div class="form-group">
                <label>Описание</label>
                <textarea name="description">${movie?.description || ''}</textarea>
            </div>
            <div class="form-group">
                <label>Дата выпуска *</label>
                <input type="date" name="releaseDate" value="${movie?.releaseDate || ''}" required>
            </div>
            <div class="form-group">
                <label>Длительность (минуты) *</label>
                <input type="number" name="duration" value="${movie?.duration || ''}" min="1" required>
            </div>
            <div class="form-group">
                <label>Режиссер *</label>
                <select name="directorId" required>
                    <option value="">Выберите режиссера</option>
                    ${currentDirectors.map(d => `<option value="${d.id}" ${movie?.directorId === d.id ? 'selected' : ''}>${escapeHtml(d.firstName)} ${escapeHtml(d.lastName)}</option>`).join('')}
                </select>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Сохранить</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Отмена</button>
            </div>
        </form>
    `;
    showModal(form);
}

function showDirectorForm(director = null) {
    const isEdit = director !== null;
    const form = `
        <h2>${isEdit ? 'Редактировать' : 'Добавить'} режиссера</h2>
        <form id="director-form" onsubmit="saveDirector(event, ${director?.id || 'null'})">
            <div class="form-group">
                <label>Имя *</label>
                <input type="text" name="firstName" value="${director?.firstName || ''}" required>
            </div>
            <div class="form-group">
                <label>Фамилия *</label>
                <input type="text" name="lastName" value="${director?.lastName || ''}" required>
            </div>
            <div class="form-group">
                <label>Дата рождения</label>
                <input type="date" name="birthDate" value="${director?.birthDate || ''}">
            </div>
            <div class="form-group">
                <label>Биография</label>
                <textarea name="biography">${director?.biography || ''}</textarea>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Сохранить</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Отмена</button>
            </div>
        </form>
    `;
    showModal(form);
}

function showGenreForm(genre = null) {
    const isEdit = genre !== null;
    const form = `
        <h2>${isEdit ? 'Редактировать' : 'Добавить'} жанр</h2>
        <form id="genre-form" onsubmit="saveGenre(event, ${genre?.id || 'null'})">
            <div class="form-group">
                <label>Название *</label>
                <input type="text" name="name" value="${genre?.name || ''}" required>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Сохранить</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Отмена</button>
            </div>
        </form>
    `;
    showModal(form);
}

function showReviewForm(review = null) {
    const isEdit = review !== null;
    const form = `
        <h2>${isEdit ? 'Редактировать' : 'Добавить'} отзыв</h2>
        <form id="review-form" onsubmit="saveReview(event, ${review?.id || 'null'})">
            <div class="form-group">
                <label>Имя автора *</label>
                <input type="text" name="authorName" value="${review?.authorName || ''}" required>
            </div>
            <div class="form-group">
                <label>Фильм *</label>
                <select name="movieId" required>
                    <option value="">Выберите фильм</option>
                    ${currentMovies.map(m => `<option value="${m.id}" ${review?.movieId === m.id ? 'selected' : ''}>${escapeHtml(m.title)}</option>`).join('')}
                </select>
            </div>
            <div class="form-group">
                <label>Рейтинг *</label>
                <input type="number" name="rating" value="${review?.rating || ''}" min="1" max="10" required>
            </div>
            <div class="form-group">
                <label>Комментарий</label>
                <textarea name="comment">${review?.comment || ''}</textarea>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Сохранить</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Отмена</button>
            </div>
        </form>
    `;
    showModal(form);
}

async function saveMovie(event, id) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    
    const movie = id ? currentMovies.find(m => m.id === id) : null;
    const existingGenreIds = movie && movie.genreIds ? movie.genreIds : [];
    
    const title = formData.get('title')?.trim();
    const description = formData.get('description')?.trim();
    const releaseDate = formData.get('releaseDate');
    const duration = parseInt(formData.get('duration'));
    const directorId = parseInt(formData.get('directorId'));
    
    const data = {
        title: title || null,
        description: description || null,
        releaseDate: releaseDate || null,
        duration: isNaN(duration) ? null : duration,
        directorId: isNaN(directorId) ? null : directorId,
        genreIds: existingGenreIds.length > 0 ? existingGenreIds : null
    };

    try {
        const url = id ? `${API_BASE}/movies/${id}` : `${API_BASE}/movies`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Не удалось сохранить фильм' }));
            let errorMessage = errorData.message || 'Не удалось сохранить фильм';
            
            if (errorData.errors) {
                const errorList = Object.entries(errorData.errors)
                    .map(([field, msg]) => `${field}: ${msg}`)
                    .join('\n');
                errorMessage = `Ошибки валидации:\n${errorList}`;
            }
            
            throw new Error(errorMessage);
        }

        closeModal();
        await loadMovies();
        showSuccess('Фильм успешно сохранен');
    } catch (error) {
        showError('modal-body', error.message);
    }
}

async function saveDirector(event, id) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    
    const data = {
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        birthDate: formData.get('birthDate') || null,
        biography: formData.get('biography') || null
    };

    try {
        const url = id ? `${API_BASE}/directors/${id}` : `${API_BASE}/directors`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Не удалось сохранить режиссера' }));
            const errorMessage = errorData.message || (errorData.errors ? JSON.stringify(errorData.errors) : 'Не удалось сохранить режиссера');
            throw new Error(errorMessage);
        }

        closeModal();
        await loadDirectors();
        showSuccess('Режиссер успешно сохранен');
    } catch (error) {
        showError('modal-body', error.message);
    }
}

async function saveGenre(event, id) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    
    const data = {
        name: formData.get('name')
    };

    try {
        const url = id ? `${API_BASE}/genres/${id}` : `${API_BASE}/genres`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Не удалось сохранить жанр' }));
            const errorMessage = errorData.message || (errorData.errors ? JSON.stringify(errorData.errors) : 'Не удалось сохранить жанр');
            throw new Error(errorMessage);
        }

        closeModal();
        await loadGenres();
        showSuccess('Жанр успешно сохранен');
    } catch (error) {
        showError('modal-body', error.message);
    }
}

async function saveReview(event, id) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    
    const data = {
        authorName: formData.get('authorName'),
        movieId: parseInt(formData.get('movieId')),
        rating: parseInt(formData.get('rating')),
        comment: formData.get('comment') || null
    };

    try {
        const url = id ? `${API_BASE}/reviews/${id}` : `${API_BASE}/reviews`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Не удалось сохранить отзыв' }));
            const errorMessage = errorData.message || (errorData.errors ? JSON.stringify(errorData.errors) : 'Не удалось сохранить отзыв');
            throw new Error(errorMessage);
        }

        closeModal();
        await Promise.all([
            loadReviews(),
            loadMovies()
        ]);
        showSuccess('Отзыв успешно сохранен');
    } catch (error) {
        showError('modal-body', error.message);
    }
}

async function editMovie(id) {
    const movie = currentMovies.find(m => m.id === id);
    if (movie) {
        showMovieForm(movie);
    }
}

async function editDirector(id) {
    const director = currentDirectors.find(d => d.id === id);
    if (director) {
        showDirectorForm(director);
    }
}

async function editGenre(id) {
    const genre = currentGenres.find(g => g.id === id);
    if (genre) {
        showGenreForm(genre);
    }
}

async function editReview(id) {
    const review = currentReviews.find(r => r.id === id);
    if (review) {
        showReviewForm(review);
    }
}

async function deleteMovie(id) {
    if (!confirm('Вы уверены, что хотите удалить этот фильм?')) return;

    try {
        const response = await fetch(`${API_BASE}/movies/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Не удалось удалить фильм');
        await loadMovies();
        showSuccess('Фильм успешно удален');
    } catch (error) {
        alert('Ошибка: ' + error.message);
    }
}

async function deleteDirector(id) {
    if (!confirm('Вы уверены, что хотите удалить этого режиссера?')) return;

    try {
        const response = await fetch(`${API_BASE}/directors/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Не удалось удалить режиссера');
        await loadDirectors();
        showSuccess('Режиссер успешно удален');
    } catch (error) {
        alert('Ошибка: ' + error.message);
    }
}

async function deleteGenre(id) {
    if (!confirm('Вы уверены, что хотите удалить этот жанр?')) return;

    try {
        const response = await fetch(`${API_BASE}/genres/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Не удалось удалить жанр');
        await loadGenres();
        await loadMovies();
        showSuccess('Жанр успешно удален');
    } catch (error) {
        alert('Ошибка: ' + error.message);
    }
}

async function deleteReview(id) {
    if (!confirm('Вы уверены, что хотите удалить этот отзыв?')) return;

    try {
        const response = await fetch(`${API_BASE}/reviews/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Не удалось удалить отзыв');
        await Promise.all([
            loadReviews(),
            loadMovies()
        ]);
        showSuccess('Отзыв успешно удален');
    } catch (error) {
        alert('Ошибка: ' + error.message);
    }
}

function showModal(content) {
    const modal = document.getElementById('modal');
    const body = document.getElementById('modal-body');
    body.innerHTML = content;
    modal.style.display = 'block';
}

function closeModal() {
    document.getElementById('modal').style.display = 'none';
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getDirectorName(directorId) {
    const director = currentDirectors.find(d => d.id === directorId);
    return director ? `${director.firstName} ${director.lastName}` : 'Неизвестно';
}

function getMovieTitle(movieId) {
    if (!movieId) return 'Неизвестно';
    const movie = currentMovies.find(m => m.id === Number(movieId) || m.id === movieId);
    return movie ? movie.title : 'Неизвестно';
}

function getGenreNames(genreIds) {
    if (!genreIds || genreIds.length === 0) return [];
    return genreIds.map(id => {
        const genre = currentGenres.find(g => g.id === id);
        return genre ? genre.name : null;
    }).filter(name => name !== null);
}

function showManageGenresForm(movieId) {
    const movie = currentMovies.find(m => m.id === movieId);
    if (!movie) {
        alert('Фильм не найден');
        return;
    }

    const currentGenreIds = new Set(movie.genreIds || []);
    const availableGenres = currentGenres.filter(g => !currentGenreIds.has(g.id));
    
    const form = `
        <h2>Управление жанрами фильма</h2>
        <p><strong>${escapeHtml(movie.title)}</strong></p>
        <div class="form-group">
            <label>Текущие жанры:</label>
            <div class="genres-list" style="margin: 10px 0;">
                ${movie.genreIds && movie.genreIds.length > 0 ? 
                    movie.genreIds.map(genreId => {
                        const genre = currentGenres.find(g => g.id === genreId);
                        return genre ? `
                            <span class="genre-tag">
                                ${escapeHtml(genre.name)}
                                <button class="genre-remove" onclick="removeGenreFromMovie(${movieId}, ${genreId})" title="Удалить жанр">×</button>
                            </span>
                        ` : '';
                    }).filter(tag => tag).join('') : 
                    '<span class="no-genres">Жанры не указаны</span>'
                }
            </div>
        </div>
        <div class="form-group">
            <label>Добавить жанр:</label>
            <select id="add-genre-select" style="width: 100%; padding: 8px; margin-bottom: 10px;">
                <option value="">Выберите жанр для добавления</option>
                ${availableGenres.map(g => `<option value="${g.id}">${escapeHtml(g.name)}</option>`).join('')}
            </select>
            <button type="button" class="btn btn-primary" onclick="addGenreToMovie(${movieId})">Добавить жанр</button>
        </div>
    `;
    showModal(form);
}

async function addGenreToMovie(movieId) {
    const select = document.getElementById('add-genre-select');
    const genreId = parseInt(select.value);
    
    if (!genreId) {
        alert('Выберите жанр для добавления');
        return;
    }

    const movie = currentMovies.find(m => m.id === movieId);
    if (!movie) {
        alert('Фильм не найден');
        return;
    }

    const currentGenreIds = new Set(movie.genreIds || []);
    if (currentGenreIds.has(genreId)) {
        alert('Этот жанр уже добавлен к фильму');
        return;
    }

    currentGenreIds.add(genreId);
    
    const data = {
        title: movie.title,
        description: movie.description,
        releaseDate: movie.releaseDate,
        duration: movie.duration,
        directorId: movie.directorId,
        genreIds: Array.from(currentGenreIds)
    };

    try {
        const response = await fetch(`${API_BASE}/movies/${movieId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Не удалось обновить жанры' }));
            throw new Error(errorData.message || 'Не удалось обновить жанры');
        }

        await loadMovies();
        await loadGenres();
        showManageGenresForm(movieId);
        showSuccess('Жанр успешно добавлен');
    } catch (error) {
        alert('Ошибка: ' + error.message);
    }
}

async function removeGenreFromMovie(movieId, genreId) {
    if (!confirm('Удалить этот жанр из фильма?')) return;

    const movie = currentMovies.find(m => m.id === movieId);
    if (!movie) {
        alert('Фильм не найден');
        return;
    }

    const currentGenreIds = new Set(movie.genreIds || []);
    currentGenreIds.delete(genreId);
    
    const data = {
        title: movie.title,
        description: movie.description,
        releaseDate: movie.releaseDate,
        duration: movie.duration,
        directorId: movie.directorId,
        genreIds: Array.from(currentGenreIds)
    };

    try {
        const response = await fetch(`${API_BASE}/movies/${movieId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Не удалось удалить жанр' }));
            throw new Error(errorData.message || 'Не удалось удалить жанр');
        }

        await loadMovies();
        await loadGenres();
        const movie = currentMovies.find(m => m.id === movieId);
        if (movie) {
            showManageGenresForm(movieId);
        }
        showSuccess('Жанр успешно удален');
    } catch (error) {
        alert('Ошибка: ' + error.message);
    }
}

function showError(containerId, message) {
    const container = document.getElementById(containerId);
    container.innerHTML = `<div class="error">${escapeHtml(message)}</div>`;
}

function showSuccess(message) {
    const activeTab = document.querySelector('.tab-content.active');
    const container = activeTab.querySelector('.list-container');
    const successDiv = document.createElement('div');
    successDiv.className = 'success';
    successDiv.textContent = message;
    container.insertBefore(successDiv, container.firstChild);
    setTimeout(() => successDiv.remove(), 3000);
}

async function showRecommendations(movieId) {
    const movie = currentMovies.find(m => m.id === movieId);
    if (!movie) {
        alert('Фильм не найден');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/movies/${movieId}/recommendations`);
        if (!response.ok) throw new Error('Не удалось загрузить рекомендации');
        const recommendations = await response.json();

        let content = `
            <h2>Рекомендации</h2>
            <p><strong>Если вам понравился "${escapeHtml(movie.title)}"</strong></p>
        `;

        if (recommendations.length === 0) {
            content += '<p>Похожих фильмов не найдено</p>';
        } else {
            content += '<div class="recommendations-list">';
            recommendations.forEach(rec => {
                const genreNames = getGenreNames(rec.genreIds || []);
                content += `
                    <div class="recommendation-card">
                        <div class="recommendation-title">${escapeHtml(rec.title)}</div>
                        <div class="recommendation-detail">Режиссер: ${getDirectorName(rec.directorId)}</div>
                        <div class="recommendation-detail">Дата выпуска: ${rec.releaseDate || 'Не указана'}</div>
                        ${rec.averageRating ? `<div class="recommendation-detail">Рейтинг: <span class="rating">${rec.averageRating.toFixed(1)}</span></div>` : ''}
                        ${genreNames.length > 0 ? `<div class="recommendation-detail">Жанры: ${genreNames.join(', ')}</div>` : ''}
                        ${rec.description ? `<div class="recommendation-detail">${escapeHtml(rec.description)}</div>` : ''}
                        <div class="recommendation-actions">
                            <button class="btn btn-primary" onclick="closeModal(); setTimeout(() => showRecommendations(${rec.id}), 100);">Рекомендации</button>
                            <button class="btn btn-secondary" onclick="closeModal(); setTimeout(() => editMovie(${rec.id}), 100);">Редактировать</button>
                        </div>
                    </div>
                `;
            });
            content += '</div>';
        }

        showModal(content);
    } catch (error) {
        alert('Ошибка загрузки рекомендаций: ' + error.message);
    }
}

