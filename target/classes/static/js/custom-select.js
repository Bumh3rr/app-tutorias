(function () {
    'use strict';

    const DOT_COLORS = {
        'Pendiente':        '#f59e0b',
        'Realizada':        '#22c55e',
        'Validada':         '#22c55e',
        'Cancelada':        '#ef4444',
        'Rechazada':        '#ef4444',
        'General':          '#3b82f6',
        'Por carrera':      '#8b5cf6',
        'Enero-Junio':      '#06b6d4',
        'Agosto-Diciembre': '#f97316',
        'Lunes':            '#3b82f6',
        'Martes':           '#8b5cf6',
        'Miércoles':        '#06b6d4',
        'Jueves':           '#f59e0b',
        'Viernes':          '#22c55e',
        'Sí':               '#22c55e',
        'No':               '#ef4444',
    };

    let activeCS = null;

    function closeActive() {
        if (!activeCS) return;
        activeCS.panel.style.display = 'none';
        activeCS.trigger.classList.remove('cs-open');
        activeCS.trigger.setAttribute('aria-expanded', 'false');
        activeCS = null;
    }

    document.addEventListener('click', function (e) {
        if (activeCS && !activeCS.wrapper.contains(e.target)) closeActive();
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeActive();
    });

    function dotSpan(color) {
        return '<span class="cs-dot" style="background:' + color + '"></span>';
    }

    function triggerLabelHTML(text, isPlaceholder) {
        if (isPlaceholder) return text;
        var color = DOT_COLORS[text];
        if (color) return dotSpan(color) + '<span>' + text + '</span>';
        return '<span>' + text + '</span>';
    }

    function initSelect(select) {
        if (select.dataset.csInit) return;
        select.dataset.csInit = '1';

        var isSmall  = select.classList.contains('form-select-sm');
        var isInline = select.style.width === 'auto';

        // Wrap
        var wrapper = document.createElement('div');
        wrapper.className = 'cs-wrapper';
        if (isInline) {
            wrapper.style.display   = 'inline-flex';
            wrapper.style.minWidth  = '70px';
            wrapper.style.width     = 'auto';
        }
        select.parentNode.insertBefore(wrapper, select);
        wrapper.appendChild(select);

        // Hide native select while keeping it focusable for required validation
        select.classList.add('cs-native');

        // Trigger button
        var trigger = document.createElement('button');
        trigger.type = 'button';
        trigger.className = 'cs-trigger' + (isSmall ? ' cs-sm' : '');
        trigger.setAttribute('aria-haspopup', 'listbox');
        trigger.setAttribute('aria-expanded', 'false');

        var textSpan = document.createElement('span');
        textSpan.className = 'cs-trigger-text';
        trigger.appendChild(textSpan);
        trigger.insertAdjacentHTML('beforeend',
            '<svg class="cs-chevron" viewBox="0 0 16 16" fill="currentColor">' +
            '<path d="M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708z"/>' +
            '</svg>'
        );
        wrapper.appendChild(trigger);

        // Panel
        var panel = document.createElement('div');
        panel.className = 'cs-panel';
        panel.setAttribute('role', 'listbox');
        panel.style.display = 'none';
        wrapper.appendChild(panel);

        function buildPanel() {
            panel.innerHTML = '';
            var currentVal = select.value;
            Array.from(select.options).forEach(function (opt) {
                var isEmpty    = opt.value === '';
                var isSelected = opt.value === currentVal;
                var item = document.createElement('div');
                item.className = 'cs-item' +
                    (isSelected ? ' cs-item-selected'    : '') +
                    (isEmpty    ? ' cs-item-placeholder' : '');
                item.setAttribute('role', 'option');
                item.setAttribute('aria-selected', String(isSelected));
                var dot = (!isEmpty && DOT_COLORS[opt.text]) ? dotSpan(DOT_COLORS[opt.text]) : '';
                item.innerHTML = dot + '<span class="cs-item-label">' + opt.text + '</span>' +
                    '<span class="cs-item-check">✓</span>';
                item.addEventListener('click', function () {
                    select.value = opt.value;
                    select.dispatchEvent(new Event('change', { bubbles: true }));
                    closeActive();
                });
                panel.appendChild(item);
            });
        }

        function updateTrigger() {
            var opt = select.options[select.selectedIndex];
            if (!opt) { textSpan.innerHTML = ''; return; }
            var isEmpty = opt.value === '';
            textSpan.innerHTML = triggerLabelHTML(opt.text, isEmpty);
            textSpan.classList.toggle('cs-placeholder', isEmpty);
        }

        select.addEventListener('change', function () {
            updateTrigger();
            buildPanel();
        });

        trigger.addEventListener('click', function (e) {
            e.stopPropagation();
            if (activeCS && activeCS.trigger === trigger) { closeActive(); return; }
            closeActive();

            var rect = wrapper.getBoundingClientRect();
            if (window.innerHeight - rect.bottom < 240 && rect.top > 240) {
                panel.style.top    = 'auto';
                panel.style.bottom = 'calc(100% + 4px)';
            } else {
                panel.style.top    = 'calc(100% + 4px)';
                panel.style.bottom = 'auto';
            }

            panel.style.display = 'block';
            trigger.classList.add('cs-open');
            trigger.setAttribute('aria-expanded', 'true');
            activeCS = { wrapper: wrapper, trigger: trigger, panel: panel };
        });

        buildPanel();
        updateTrigger();
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('select.form-select').forEach(initSelect);
    });
})();
