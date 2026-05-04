/**
 * Entity Picker — replaces native <select> with a searchable card modal.
 *
 * Usage in HTML:
 *   <div class="entity-picker"
 *        data-entity="tutor"
 *        data-placeholder="Sin tutor asignado"
 *        data-clearable="true"
 *        data-current-id="5"
 *        data-current-text="Juan Pérez"
 *        data-current-sub="20400123"
 *        data-current-foto="/tutor/foto.jpg">
 *     <input type="hidden" name="tutor.id" value="5">
 *   </div>
 */

(function () {
    'use strict';

    const MODAL_ID = 'epModal';
    const PAGE_SIZE = 6;

    const ENTITY_TITLES = {
        tutor: 'Seleccionar Tutor',
        tutorado: 'Seleccionar Tutorado',
        grupo: 'Seleccionar Grupo',
        pat: 'Seleccionar PAT',
        semestre: 'Seleccionar Semestre',
        carrera: 'Seleccionar Carrera',
        actividad: 'Seleccionar Actividad',
        sesion: 'Seleccionar Sesión',
    };

    // ── Modal singleton ────────────────────────────────────────────────────────

    function getOrCreateModal() {
        let el = document.getElementById(MODAL_ID);
        if (el) return el;

        el = document.createElement('div');
        el.className = 'modal fade';
        el.id = MODAL_ID;
        el.tabIndex = -1;
        el.setAttribute('aria-hidden', 'true');
        el.innerHTML = `
            <div class="modal-dialog modal-dialog-centered" style="max-width: 850px;">
                <div class="modal-content">
                    <div class="modal-header border-0 pb-2">
                        <div style="flex:1;">
                            <h5 class="modal-title mb-3" id="epModalTitle">Seleccionar</h5>
                            <div style="position:relative;">
                                <svg style="position:absolute;left:12px;top:50%;transform:translateY(-50%);opacity:0.4;"
                                     width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                                </svg>
                                <input type="text" class="form-control" id="epSearch"
                                       placeholder="Buscar por nombre..."
                                       style="padding-left:36px; font-size:13.5px;">
                            </div>
                        </div>
                        <button type="button" class="btn-close ms-3 mb-0" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body pt-0">
                        <div id="epResults" style="min-height:450px;"></div>
                        <div id="epPagination" class="ep-pagination" style="display:none;"></div>
                    </div>
                </div>
            </div>`;
        document.body.appendChild(el);
        return el;
    }

    // ── Rendering helpers ──────────────────────────────────────────────────────

    function avatarHTML(nombre, fotoUrl, size) {
        size = size || 40;
        if (fotoUrl) {
            return `<img src="${escHtml(fotoUrl)}" class="avatar ep-avatar"
                        style="width:${size}px;height:${size}px;" alt="">`;
        }
        const initials = encodeURIComponent((nombre || '?').trim());
        return `<img src="https://ui-avatars.com/api/?name=${initials}&size=${size * 2}&background=random&color=fff&bold=true&rounded=true"
                     class="avatar ep-avatar" style="width:${size}px;height:${size}px;" alt="">`;
    }

    function escHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function renderResults(container, items, onSelect) {
        if (!items || items.length === 0) {
            container.innerHTML = `
                <div style="text-align:center;padding:40px 0;opacity:0.45;font-size:13px;">
                    Sin resultados para tu búsqueda
                </div>`;
            return;
        }

        const grid = document.createElement('div');
        grid.className = 'ep-grid';

        items.forEach(function (item) {
            const card = document.createElement('button');
            card.type = 'button';
            card.className = 'ep-card';
            card.innerHTML = `
                ${avatarHTML(item.nombre, item.foto, 40)}
                <div class="ep-card-info">
                    <div class="ep-card-name">${escHtml(item.nombre)}</div>
                    ${item.sub ? `<div class="ep-card-sub">${escHtml(item.sub)}</div>` : ''}
                </div>`;
            card.addEventListener('click', function () { onSelect(item); });
            grid.appendChild(card);
        });

        container.innerHTML = '';
        container.appendChild(grid);
    }

    function renderPagination(container, data, onPage) {
        if (!data || data.totalPages <= 1) {
            container.style.display = 'none';
            return;
        }

        const cur = data.number;
        const total = data.totalPages;

        let pagesHTML = '';
        for (let i = 0; i < total; i++) {
            pagesHTML += `<li class="page-item ${i === cur ? 'active' : ''}">
                <button type="button" class="page-link" data-p="${i}">${i + 1}</button>
            </li>`;
        }

        container.style.display = 'flex';
        container.innerHTML = `
            <small style="opacity:0.45;font-size:12px;">${data.totalElements} resultado${data.totalElements !== 1 ? 's' : ''}</small>
            <nav>
                <ul class="pagination pagination-sm mb-0">
                    <li class="page-item ${cur === 0 ? 'disabled' : ''}">
                        <button type="button" class="page-link" data-p="${cur - 1}">&laquo;</button>
                    </li>
                    ${pagesHTML}
                    <li class="page-item ${cur + 1 >= total ? 'disabled' : ''}">
                        <button type="button" class="page-link" data-p="${cur + 1}">&raquo;</button>
                    </li>
                </ul>
            </nav>`;

        container.querySelectorAll('[data-p]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                const p = parseInt(btn.dataset.p, 10);
                if (!isNaN(p) && p >= 0 && p < total) onPage(p);
            });
        });
    }

    // ── State ──────────────────────────────────────────────────────────────────

    let activePicker = null;
    let searchTimer  = null;
    let activeQuery  = '';
    let activePage   = 0;

    // ── Search ─────────────────────────────────────────────────────────────────

    function doSearch(entity, q, page) {
        const results    = document.getElementById('epResults');
        const pagination = document.getElementById('epPagination');

        results.innerHTML = `
            <div style="text-align:center;padding:40px 0;">
                <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
            </div>`;
        pagination.style.display = 'none';

        fetch('/api/search/' + entity + '?q=' + encodeURIComponent(q) + '&page=' + page + '&size=' + PAGE_SIZE)
            .then(function (r) { return r.json(); })
            .then(function (data) {
                renderResults(results, data.content, function (item) {
                    if (activePicker) {
                        activePicker.hiddenInput.value = item.id;
                        activePicker.updateDisplay(item.id, item.nombre, item.sub || '', item.foto || '');
                    }
                    bootstrap.Modal.getInstance(document.getElementById(MODAL_ID))?.hide();
                });
                renderPagination(pagination, data, function (p) {
                    activePage = p;
                    doSearch(entity, activeQuery, p);
                });
            })
            .catch(function () {
                results.innerHTML = `
                    <div style="text-align:center;padding:40px 0;opacity:0.45;font-size:13px;">
                        Error al cargar. Inténtalo de nuevo.
                    </div>`;
            });
    }

    // ── Trigger display ────────────────────────────────────────────────────────

    function buildTriggerContent(id, text, sub, foto, placeholder, clearable) {
        if (id && text) {
            const clearBtn = clearable
                ? `<button type="button" class="ep-clear" title="Limpiar selección">
                       <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                           <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                       </svg>
                   </button>`
                : '';
            return `
                <div class="ep-trigger-inner">
                    ${avatarHTML(text, foto, 28)}
                    <div class="ep-trigger-text">
                        <span class="ep-trigger-name">${escHtml(text)}</span>
                        ${sub ? `<span class="ep-trigger-sub">${escHtml(sub)}</span>` : ''}
                    </div>
                    ${clearBtn}
                    <svg class="ep-chevron ms-auto" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="6 9 12 15 18 9"/>
                    </svg>
                </div>`;
        }

        return `
            <div class="ep-trigger-inner">
                <span class="ep-placeholder">${escHtml(placeholder)}</span>
                <svg class="ep-chevron ms-auto" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="6 9 12 15 18 9"/>
                </svg>
            </div>`;
    }

    // ── Init picker ────────────────────────────────────────────────────────────

    function initPicker(el) {
        const entity      = el.dataset.entity;
        const placeholder = el.dataset.placeholder || 'Seleccionar...';
        const clearable   = el.dataset.clearable !== 'false';
        const hiddenInput = el.querySelector('input[type="hidden"]');

        if (!entity || !hiddenInput) return;

        let currentId   = el.dataset.currentId   || '';
        let currentText = el.dataset.currentText || '';
        let currentSub  = el.dataset.currentSub  || '';
        let currentFoto = el.dataset.currentFoto || '';

        // Resolve foto URL
        if (currentFoto && !currentFoto.startsWith('/') && !currentFoto.startsWith('http')) {
            currentFoto = '/' + entity + '/' + currentFoto;
        }

        const trigger = document.createElement('button');
        trigger.type = 'button';
        trigger.className = 'ep-trigger';

        function updateDisplay(id, text, sub, foto) {
            currentId   = id;
            currentText = text;
            currentSub  = sub;
            currentFoto = foto;

            // Resolve foto
            let resolvedFoto = foto;
            if (foto && !foto.startsWith('/') && !foto.startsWith('http')) {
                resolvedFoto = '/' + entity + '/' + foto;
            }

            trigger.innerHTML = buildTriggerContent(id, text, sub, resolvedFoto, placeholder, clearable);
            hiddenInput.value  = id || '';

            // Re-bind clear button
            const clearBtn = trigger.querySelector('.ep-clear');
            if (clearBtn) {
                clearBtn.addEventListener('click', function (e) {
                    e.stopPropagation();
                    updateDisplay('', '', '', '');
                });
            }
        }

        updateDisplay(currentId, currentText, currentSub, currentFoto);
        el.appendChild(trigger);

        trigger.addEventListener('click', function () {
            activePicker = { hiddenInput, updateDisplay, entity };
            openModal(entity);
        });
    }

    function openModal(entity) {
        const modalEl = getOrCreateModal();
        const bsModal = bootstrap.Modal.getOrCreateInstance(modalEl);

        document.getElementById('epModalTitle').textContent = ENTITY_TITLES[entity] || 'Seleccionar';

        const searchInput = document.getElementById('epSearch');
        searchInput.value = '';
        activeQuery = '';
        activePage  = 0;

        doSearch(entity, '', 0);

        searchInput.oninput = function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(function () {
                activeQuery = searchInput.value.trim();
                activePage  = 0;
                doSearch(entity, activeQuery, 0);
            }, 280);
        };

        bsModal.show();
        modalEl.addEventListener('shown.bs.modal', function handler() {
            searchInput.focus();
            modalEl.removeEventListener('shown.bs.modal', handler);
        });
    }

    // ── Bootstrap ──────────────────────────────────────────────────────────────

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.entity-picker').forEach(initPicker);
    });

})();
