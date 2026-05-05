(function () {
    'use strict';

    function relativeTime(ms) {
        const diff = Math.floor((Date.now() - ms) / 1000);
        if (diff < 10)      return 'Ahora mismo';
        if (diff < 60)      return 'Hace ' + diff + ' seg';
        if (diff < 3600) {
            const m = Math.floor(diff / 60);
            return 'Hace ' + m + (m === 1 ? ' min' : ' min');
        }
        if (diff < 86400) {
            const h = Math.floor(diff / 3600);
            return 'Hace ' + h + (h === 1 ? ' hora' : ' horas');
        }
        if (diff < 604800) {
            const d = Math.floor(diff / 86400);
            return 'Hace ' + d + (d === 1 ? ' día' : ' días');
        }
        if (diff < 2592000) {
            const w = Math.floor(diff / 604800);
            return 'Hace ' + w + (w === 1 ? ' semana' : ' semanas');
        }
        if (diff < 31536000) {
            const mo = Math.floor(diff / 2592000);
            return 'Hace ' + mo + (mo === 1 ? ' mes' : ' meses');
        }
        const y = Math.floor(diff / 31536000);
        return 'Hace ' + y + (y === 1 ? ' año' : ' años');
    }

    function apply() {
        document.querySelectorAll('[data-ts]').forEach(function (td) {
            const ms = parseInt(td.dataset.ts, 10);
            if (isNaN(ms)) return;

            const diff = (Date.now() - ms) / 1000;
            const badge = td.querySelector('.rel-badge');

            if (badge) {
                badge.textContent = relativeTime(ms);
                badge.classList.remove('badge-new-hot', 'badge-new-today', 'badge-sin-asignar');
                if (diff < 3600)       badge.classList.add('badge-new-hot');
                else if (diff < 86400) badge.classList.add('badge-new-today');
                else                   badge.classList.add('badge-sin-asignar');
            }

            if (diff < 3600) {
                const row = td.closest('tr');
                if (row) row.classList.add('row-new');
            }
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        apply();
        setInterval(apply, 30000);
        document.querySelectorAll('.rel-badge[data-bs-toggle="tooltip"]').forEach(function (el) {
            new bootstrap.Tooltip(el);
        });
    });
})();
