(function () {
    'use strict';

    function fmt(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / 1048576).toFixed(1) + ' MB';
    }

    var UPLOAD_ICON =
        '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">' +
        '<path d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5"/>' +
        '</svg>';

    var EDIT_ICON =
        '<svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="20" height="20">' +
        '<path d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931z"/>' +
        '</svg>';

    function initPicker(wrapper) {
        var fileInput  = wrapper.querySelector('.fp-file-input');
        var hiddenFoto = wrapper.querySelector('input[type="hidden"]');
        if (!fileInput) return;

        var currentUrl  = (wrapper.dataset.currentUrl  || '').trim();
        var currentName = (wrapper.dataset.currentName || '').trim();

        // Hide the native file input
        fileInput.style.display = 'none';

        /* ── zone (dropzone / current image placeholder) ── */
        var zone = document.createElement('div');
        zone.className = 'fp-zone';
        wrapper.appendChild(zone);

        /* ── preview (shown after selecting a new file) ── */
        var preview = document.createElement('div');
        preview.className = 'fp-preview';
        preview.style.display = 'none';
        preview.innerHTML =
            '<img class="fp-thumb" alt="Vista previa">' +
            '<div class="fp-info"><span class="fp-filename"></span><span class="fp-filesize"></span></div>' +
            '<button type="button" class="fp-clear" title="Quitar selección">&#x2715;</button>';
        wrapper.appendChild(preview);

        // Hidden input appended last so the native file input value is read first by browsers
        wrapper.appendChild(fileInput);

        function renderEmpty() {
            zone.innerHTML =
                '<div class="fp-icon">' + UPLOAD_ICON + '</div>' +
                '<p class="fp-zone-text">Arrastra una imagen o <span class="fp-link">elige archivo</span></p>' +
                '<p class="fp-zone-hint">JPG, PNG &middot; m&aacute;x. 10 MB</p>';
            zone.style.display = 'block';
            preview.style.display = 'none';
        }

        function renderCurrent() {
            zone.innerHTML =
                '<div class="fp-current">' +
                    '<img src="' + currentUrl + '" alt="Foto actual" class="fp-current-thumb">' +
                    '<div class="fp-current-overlay">' + EDIT_ICON + '<span>Cambiar</span></div>' +
                '</div>' +
                '<p class="fp-zone-hint mt-2">Haz clic para cambiar la foto</p>';
            zone.style.display = 'block';
            preview.style.display = 'none';
        }

        function renderPreview(file) {
            var reader = new FileReader();
            reader.onload = function (e) {
                zone.style.display = 'none';
                preview.style.display = 'flex';
                preview.querySelector('.fp-thumb').src = e.target.result;
                preview.querySelector('.fp-filename').textContent = file.name;
                preview.querySelector('.fp-filesize').textContent = fmt(file.size);
            };
            reader.readAsDataURL(file);
        }

        // Initial state
        if (currentUrl) renderCurrent(); else renderEmpty();

        // Click zone → open file dialog
        zone.addEventListener('click', function () { fileInput.click(); });

        // Drag & drop
        zone.addEventListener('dragover', function (e) {
            e.preventDefault();
            zone.classList.add('fp-drag-over');
        });
        zone.addEventListener('dragleave', function () { zone.classList.remove('fp-drag-over'); });
        zone.addEventListener('drop', function (e) {
            e.preventDefault();
            zone.classList.remove('fp-drag-over');
            var f = e.dataTransfer.files[0];
            if (f && f.type.startsWith('image/')) {
                try {
                    var dt = new DataTransfer();
                    dt.items.add(f);
                    fileInput.files = dt.files;
                    fileInput.dispatchEvent(new Event('change'));
                } catch (_) { /* Safari fallback: open dialog */ fileInput.click(); }
            }
        });

        // File selected via input
        fileInput.addEventListener('change', function () {
            if (this.files && this.files[0]) renderPreview(this.files[0]);
        });

        // Clear new selection → revert to current (or empty)
        preview.querySelector('.fp-clear').addEventListener('click', function () {
            fileInput.value = '';
            if (currentUrl) renderCurrent(); else renderEmpty();
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.fp-wrapper').forEach(initPicker);
    });
})();
